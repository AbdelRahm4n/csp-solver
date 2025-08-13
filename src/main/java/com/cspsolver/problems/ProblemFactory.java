package com.cspsolver.problems;

import com.cspsolver.core.model.CSP;

/**
 * Interface for CSP problem factories.
 * Creates CSP instances for specific problem types.
 *
 * @param <T> the type of values in the variable domains
 */
public interface ProblemFactory<T> {

    /**
     * Creates a CSP instance for this problem.
     */
    CSP<T> create();

    /**
     * Returns the name of this problem.
     */
    String getName();

    /**
     * Returns a description of this problem.
     */
    String getDescription();
}
