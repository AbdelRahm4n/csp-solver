package com.cspsolver.solver.heuristics.variable;

import com.cspsolver.core.constraint.Constraint;
import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Composite variable selector that uses a primary heuristic with tie-breaking.
 * When the primary heuristic produces ties, uses the secondary heuristic to break them.
 *
 * @param <T> the type of values in the variable domains
 */
public class CompositeSelector<T> implements VariableSelector<T> {

    private final VariableSelector<T> primary;
    private final VariableSelector<T> tieBreaker;

    public CompositeSelector(VariableSelector<T> primary, VariableSelector<T> tieBreaker) {
        this.primary = primary;
        this.tieBreaker = tieBreaker;
    }

    /**
     * Creates an MRV selector with Degree tie-breaking.
     */
    public static <T> CompositeSelector<T> mrvWithDegree() {
        return new CompositeSelector<>(new MRVSelector<>(), new DegreeSelector<>());
    }

    @Override
    public Variable<T> select(
            List<Variable<T>> unassigned,
            Map<Variable<T>, Domain<T>> domains,
            CSP<T> csp,
            Assignment<T> assignment) {

        if (unassigned.isEmpty()) {
            return null;
        }

        if (unassigned.size() == 1) {
            return unassigned.get(0);
        }

        // Find the best value according to primary heuristic
        Variable<T> primaryBest = primary.select(unassigned, domains, csp, assignment);

        if (primaryBest == null) {
            return null;
        }

        // Collect all variables that tie with the best
        List<Variable<T>> ties = findTies(primaryBest, unassigned, domains, csp);

        if (ties.size() == 1) {
            return ties.get(0);
        }

        // Use tie-breaker on the tied variables
        return tieBreaker.select(ties, domains, csp, assignment);
    }

    /**
     * Finds all variables that have the same primary score as the best variable.
     */
    private List<Variable<T>> findTies(
            Variable<T> best,
            List<Variable<T>> unassigned,
            Map<Variable<T>, Domain<T>> domains,
            CSP<T> csp) {

        List<Variable<T>> ties = new ArrayList<>();

        // For MRV, compare domain sizes
        if (primary instanceof MRVSelector) {
            int bestSize = domains.get(best).size();
            for (Variable<T> var : unassigned) {
                if (domains.get(var).size() == bestSize) {
                    ties.add(var);
                }
            }
        }
        // For Degree, compare degrees
        else if (primary instanceof DegreeSelector) {
            int bestDegree = csp.getNetwork().getDegree(best);
            for (Variable<T> var : unassigned) {
                if (csp.getNetwork().getDegree(var) == bestDegree) {
                    ties.add(var);
                }
            }
        }
        // Default: no tie-breaking information available
        else {
            ties.add(best);
        }

        return ties;
    }

    @Override
    public void recordFailure(Variable<T> variable, Constraint<T> constraint) {
        primary.recordFailure(variable, constraint);
        tieBreaker.recordFailure(variable, constraint);
    }

    @Override
    public void reset() {
        primary.reset();
        tieBreaker.reset();
    }

    @Override
    public String getName() {
        return primary.getName() + "+" + tieBreaker.getName();
    }
}
