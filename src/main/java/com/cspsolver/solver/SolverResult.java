package com.cspsolver.solver;

import com.cspsolver.core.model.Assignment;
import com.cspsolver.solver.metrics.SolverMetrics;

import java.util.Collections;
import java.util.List;

/**
 * Result of solving a CSP.
 * Contains the solution(s) found and solving metrics.
 *
 * @param <T> the type of values in the variable domains
 */
public class SolverResult<T> {

    public enum Status {
        SATISFIABLE,       // At least one solution found
        UNSATISFIABLE,     // No solution exists
        TIMEOUT,           // Search timed out
        CANCELLED,         // Search was cancelled
        ERROR              // An error occurred
    }

    private final Status status;
    private final List<Assignment<T>> solutions;
    private final SolverMetrics.Snapshot metrics;
    private final String errorMessage;

    private SolverResult(Status status, List<Assignment<T>> solutions,
                         SolverMetrics.Snapshot metrics, String errorMessage) {
        this.status = status;
        this.solutions = solutions != null ? Collections.unmodifiableList(solutions) : Collections.emptyList();
        this.metrics = metrics;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a result for a satisfiable CSP with one solution.
     */
    public static <T> SolverResult<T> satisfiable(Assignment<T> solution, SolverMetrics.Snapshot metrics) {
        return new SolverResult<>(Status.SATISFIABLE, List.of(solution), metrics, null);
    }

    /**
     * Creates a result for a satisfiable CSP with multiple solutions.
     */
    public static <T> SolverResult<T> satisfiable(List<Assignment<T>> solutions, SolverMetrics.Snapshot metrics) {
        return new SolverResult<>(Status.SATISFIABLE, solutions, metrics, null);
    }

    /**
     * Creates a result for an unsatisfiable CSP.
     */
    public static <T> SolverResult<T> unsatisfiable(SolverMetrics.Snapshot metrics) {
        return new SolverResult<>(Status.UNSATISFIABLE, Collections.emptyList(), metrics, null);
    }

    /**
     * Creates a result for a timed out search.
     */
    public static <T> SolverResult<T> timeout(List<Assignment<T>> partialSolutions, SolverMetrics.Snapshot metrics) {
        return new SolverResult<>(Status.TIMEOUT, partialSolutions, metrics, null);
    }

    /**
     * Creates a result for a cancelled search.
     */
    public static <T> SolverResult<T> cancelled(List<Assignment<T>> partialSolutions, SolverMetrics.Snapshot metrics) {
        return new SolverResult<>(Status.CANCELLED, partialSolutions, metrics, null);
    }

    /**
     * Creates a result for an error.
     */
    public static <T> SolverResult<T> error(String message, SolverMetrics.Snapshot metrics) {
        return new SolverResult<>(Status.ERROR, Collections.emptyList(), metrics, message);
    }

    /**
     * Returns the solving status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns true if at least one solution was found.
     */
    public boolean isSatisfiable() {
        return status == Status.SATISFIABLE;
    }

    /**
     * Returns true if no solution exists.
     */
    public boolean isUnsatisfiable() {
        return status == Status.UNSATISFIABLE;
    }

    /**
     * Returns true if the search timed out.
     */
    public boolean isTimeout() {
        return status == Status.TIMEOUT;
    }

    /**
     * Returns true if the search was cancelled.
     */
    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }

    /**
     * Returns true if an error occurred.
     */
    public boolean isError() {
        return status == Status.ERROR;
    }

    /**
     * Returns the first solution, or null if no solution was found.
     */
    public Assignment<T> getSolution() {
        return solutions.isEmpty() ? null : solutions.get(0);
    }

    /**
     * Returns all solutions found.
     */
    public List<Assignment<T>> getSolutions() {
        return solutions;
    }

    /**
     * Returns the number of solutions found.
     */
    public int getSolutionCount() {
        return solutions.size();
    }

    /**
     * Returns the solving metrics.
     */
    public SolverMetrics.Snapshot getMetrics() {
        return metrics;
    }

    /**
     * Returns the error message, if any.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SolverResult[");
        sb.append(status);
        if (!solutions.isEmpty()) {
            sb.append(", solutions=").append(solutions.size());
        }
        if (metrics != null) {
            sb.append(", ").append(metrics);
        }
        if (errorMessage != null) {
            sb.append(", error=").append(errorMessage);
        }
        sb.append("]");
        return sb.toString();
    }
}
