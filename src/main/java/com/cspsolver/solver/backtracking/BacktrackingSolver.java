package com.cspsolver.solver.backtracking;

import com.cspsolver.core.constraint.Constraint;
import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;
import com.cspsolver.core.propagation.AC3Propagator;
import com.cspsolver.core.propagation.PropagationResult;
import com.cspsolver.solver.Solver;
import com.cspsolver.solver.SolverConfiguration;
import com.cspsolver.solver.SolverResult;
import com.cspsolver.solver.metrics.SolverMetrics;
import com.cspsolver.websocket.SolverEventPublisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Backtracking search solver for Constraint Satisfaction Problems.
 * Supports configurable variable/value ordering heuristics and constraint propagation.
 *
 * @param <T> the type of values in the variable domains
 */
public class BacktrackingSolver<T> implements Solver<T> {

    private final SolverConfiguration<T> config;
    private final SolverMetrics metrics;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean solving = new AtomicBoolean(false);
    private volatile long deadline;

    public BacktrackingSolver() {
        this(SolverConfiguration.defaultConfig());
    }

    public BacktrackingSolver(SolverConfiguration<T> config) {
        this.config = config;
        this.metrics = new SolverMetrics();
    }

    @Override
    public SolverResult<T> solve(CSP<T> csp) {
        // Initialize solving state
        cancelled.set(false);
        solving.set(true);
        metrics.reset();
        deadline = System.currentTimeMillis() + config.getTimeoutMillis();

        // Reset variable selector state
        config.getVariableSelector().reset();

        // Publish start event
        SolverEventPublisher publisher = config.getEventPublisher();
        String sessionId = publisher != null ? publisher.getSessionId() : null;
        if (publisher != null) {
            publisher.onSolveStarted(sessionId, csp.getNumVariables(), csp.getNumConstraints());
        }

        try {
            // Initialize working domains
            Map<Variable<T>, Domain<T>> domains = csp.createWorkingDomains();

            // AC-3 preprocessing
            if (config.isUseAC3Preprocessing()) {
                AC3Propagator<T> ac3 = new AC3Propagator<>();
                PropagationResult preprocess = ac3.propagate(csp, domains);
                metrics.addArcRevisions(preprocess.getArcRevisions());
                metrics.addDomainReductions(preprocess.getDomainReductions());

                if (preprocess.isContradiction()) {
                    metrics.stop();
                    if (publisher != null) {
                        publisher.onSolveCompleted(sessionId, false, metrics.snapshot());
                    }
                    solving.set(false);
                    return SolverResult.unsatisfiable(metrics.snapshot());
                }
            }

            // Initialize assignment
            Assignment<T> assignment = csp.createEmptyAssignment();
            List<Assignment<T>> solutions = new ArrayList<>();

            // Start backtracking search
            boolean success = backtrack(assignment, domains, csp, solutions, 0, publisher, sessionId);

            metrics.stop();

            if (publisher != null) {
                publisher.onSolveCompleted(sessionId, !solutions.isEmpty(), metrics.snapshot());
            }

            // Determine result status
            if (cancelled.get()) {
                return SolverResult.cancelled(solutions, metrics.snapshot());
            }
            if (isTimedOut()) {
                return SolverResult.timeout(solutions, metrics.snapshot());
            }
            if (solutions.isEmpty()) {
                return SolverResult.unsatisfiable(metrics.snapshot());
            }
            return SolverResult.satisfiable(solutions, metrics.snapshot());

        } catch (Exception e) {
            metrics.stop();
            return SolverResult.error(e.getMessage(), metrics.snapshot());
        } finally {
            solving.set(false);
        }
    }

    /**
     * Recursive backtracking search.
     */
    private boolean backtrack(
            Assignment<T> assignment,
            Map<Variable<T>, Domain<T>> domains,
            CSP<T> csp,
            List<Assignment<T>> solutions,
            int depth,
            SolverEventPublisher publisher,
            String sessionId) {

        // Check termination conditions
        if (cancelled.get() || isTimedOut()) {
            return false;
        }

        // Check if complete
        if (assignment.isComplete()) {
            solutions.add(assignment.copy());
            metrics.incrementSolutionsFound();

            if (publisher != null) {
                publisher.onSolutionFound(sessionId, solutions.size(), metrics.snapshot());
            }

            // Return true if we've found enough solutions
            return !config.isFindAllSolutions() || solutions.size() >= config.getMaxSolutions();
        }

        // Select next variable
        List<Variable<T>> unassigned = getUnassignedVariables(assignment, csp);
        Variable<T> var = config.getVariableSelector().select(unassigned, domains, csp, assignment);

        if (var == null) {
            return false;
        }

        Domain<T> domain = domains.get(var);
        if (domain == null || domain.isEmpty()) {
            return false;
        }

        if (publisher != null) {
            publisher.onVariableSelected(sessionId, var.getName(), domain.size(), depth);
        }

        // Get ordered values
        Iterable<T> orderedValues = config.getValueSelector().orderValues(var, domain, csp, assignment, domains);

        // Try each value
        for (T value : orderedValues) {
            metrics.incrementNodesExplored();

            // Save domain states for backtracking
            Map<Variable<T>, Domain<T>> savedDomains = saveDomains(domains);

            // Make assignment
            assignment.assign(var, value);
            domain.reduceTo(value);

            if (publisher != null) {
                publisher.onValueAssigned(sessionId, var.getName(), value, depth);
            }

            // Propagate
            PropagationResult propResult = config.getPropagator().propagateAfterAssignment(
                    var, value, csp, assignment, domains
            );
            metrics.addConstraintChecks(propResult.getConstraintChecks());
            metrics.addArcRevisions(propResult.getArcRevisions());
            metrics.addDomainReductions(propResult.getDomainReductions());

            if (!propResult.isContradiction()) {
                // Recurse
                if (backtrack(assignment, domains, csp, solutions, depth + 1, publisher, sessionId)) {
                    return true;
                }
            }

            // Backtrack
            metrics.incrementBacktracks();
            assignment.unassign(var);
            restoreDomains(domains, savedDomains);

            // Record failure for learning heuristics
            if (propResult.isContradiction() && propResult.getFailedConstraint() != null) {
                @SuppressWarnings("unchecked")
                Constraint<T> failedConstraint = (Constraint<T>) propResult.getFailedConstraint();
                config.getVariableSelector().recordFailure(var, failedConstraint);
            }

            if (publisher != null) {
                publisher.onBacktrack(sessionId, var.getName(), depth);
            }

            // Periodically publish progress
            if (publisher != null && metrics.getNodesExplored() % 1000 == 0) {
                publisher.onProgress(sessionId, metrics.snapshot());
            }
        }

        return false;
    }

    /**
     * Returns all unassigned variables.
     */
    private List<Variable<T>> getUnassignedVariables(Assignment<T> assignment, CSP<T> csp) {
        List<Variable<T>> unassigned = new ArrayList<>();
        for (Variable<T> var : csp.getVariables()) {
            if (!assignment.isAssigned(var)) {
                unassigned.add(var);
            }
        }
        return unassigned;
    }

    /**
     * Saves current domain states.
     */
    private Map<Variable<T>, Domain<T>> saveDomains(Map<Variable<T>, Domain<T>> domains) {
        Map<Variable<T>, Domain<T>> saved = new HashMap<>();
        for (Map.Entry<Variable<T>, Domain<T>> entry : domains.entrySet()) {
            saved.put(entry.getKey(), entry.getValue().copy());
        }
        return saved;
    }

    /**
     * Restores domain states from saved copy.
     */
    private void restoreDomains(Map<Variable<T>, Domain<T>> domains, Map<Variable<T>, Domain<T>> saved) {
        for (Map.Entry<Variable<T>, Domain<T>> entry : saved.entrySet()) {
            domains.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Checks if the solving deadline has passed.
     */
    private boolean isTimedOut() {
        return System.currentTimeMillis() > deadline;
    }

    @Override
    public void cancel() {
        cancelled.set(true);
    }

    @Override
    public boolean isSolving() {
        return solving.get();
    }

    @Override
    public SolverConfiguration<T> getConfiguration() {
        return config;
    }

    /**
     * Returns the current metrics (may be incomplete if solving is in progress).
     */
    public SolverMetrics getMetrics() {
        return metrics;
    }
}
