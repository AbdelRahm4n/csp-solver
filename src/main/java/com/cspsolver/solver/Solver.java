package com.cspsolver.solver;

import com.cspsolver.core.model.CSP;

/**
 * Interface for CSP solvers.
 *
 * @param <T> the type of values in the variable domains
 */
public interface Solver<T> {

    /**
     * Solves the given CSP.
     *
     * @param csp the constraint satisfaction problem to solve
     * @return the solving result
     */
    SolverResult<T> solve(CSP<T> csp);

    /**
     * Cancels an ongoing solve operation.
     */
    void cancel();

    /**
     * Returns true if a solve operation is in progress.
     */
    boolean isSolving();

    /**
     * Returns the configuration used by this solver.
     */
    SolverConfiguration<T> getConfiguration();
}
