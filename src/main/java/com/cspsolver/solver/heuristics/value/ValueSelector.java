package com.cspsolver.solver.heuristics.value;

import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.Map;

/**
 * Interface for value ordering heuristics.
 * Determines the order in which values should be tried for a variable.
 *
 * @param <T> the type of values in the variable domains
 */
public interface ValueSelector<T> {

    /**
     * Returns an ordered iterable of values to try for the given variable.
     *
     * @param variable   the variable to select values for
     * @param domain     the current domain of the variable
     * @param csp        the constraint satisfaction problem
     * @param assignment the current assignment
     * @param domains    all current domains
     * @return an iterable of values in the order they should be tried
     */
    Iterable<T> orderValues(
            Variable<T> variable,
            Domain<T> domain,
            CSP<T> csp,
            Assignment<T> assignment,
            Map<Variable<T>, Domain<T>> domains
    );

    /**
     * Returns the name of this selector.
     */
    String getName();
}
