package com.cspsolver.core.propagation;

import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.Map;

/**
 * Interface for constraint propagation engines.
 * Propagation reduces domains by eliminating inconsistent values.
 *
 * @param <T> the type of values in the variable domains
 */
public interface PropagationEngine<T> {

    /**
     * Performs initial propagation on the CSP (preprocessing).
     * Called before search begins.
     *
     * @param csp     the constraint satisfaction problem
     * @param domains the current domains (may be modified)
     * @return the propagation result
     */
    PropagationResult propagate(CSP<T> csp, Map<Variable<T>, Domain<T>> domains);

    /**
     * Propagates constraints after a variable has been assigned.
     * Called during search after each assignment.
     *
     * @param variable   the variable that was just assigned
     * @param value      the value that was assigned
     * @param csp        the constraint satisfaction problem
     * @param assignment the current assignment
     * @param domains    the current domains (may be modified)
     * @return the propagation result
     */
    PropagationResult propagateAfterAssignment(
            Variable<T> variable,
            T value,
            CSP<T> csp,
            Assignment<T> assignment,
            Map<Variable<T>, Domain<T>> domains
    );

    /**
     * Returns the name of this propagation engine.
     */
    String getName();
}
