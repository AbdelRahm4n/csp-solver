package com.cspsolver.solver.heuristics.variable;

import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.List;
import java.util.Map;

/**
 * Interface for variable ordering heuristics.
 * Determines which unassigned variable to select next during search.
 *
 * @param <T> the type of values in the variable domains
 */
public interface VariableSelector<T> {

    /**
     * Selects the next variable to assign.
     *
     * @param unassigned the list of unassigned variables
     * @param domains    the current domains
     * @param csp        the constraint satisfaction problem
     * @param assignment the current assignment
     * @return the selected variable
     */
    Variable<T> select(
            List<Variable<T>> unassigned,
            Map<Variable<T>, Domain<T>> domains,
            CSP<T> csp,
            Assignment<T> assignment
    );

    /**
     * Returns the name of this selector.
     */
    String getName();

    /**
     * Called when a constraint causes a domain wipeout.
     * Useful for learning-based heuristics like Dom/WDeg.
     */
    default void recordFailure(Variable<T> variable, com.cspsolver.core.constraint.Constraint<T> constraint) {
        // Default: do nothing
    }

    /**
     * Resets any learned state.
     */
    default void reset() {
        // Default: do nothing
    }
}
