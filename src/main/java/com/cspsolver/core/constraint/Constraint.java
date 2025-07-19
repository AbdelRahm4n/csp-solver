package com.cspsolver.core.constraint;

import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.List;
import java.util.Map;

/**
 * Base interface for all constraints in a CSP.
 * A constraint defines a relation over a set of variables that must be satisfied.
 *
 * @param <T> the type of values in the variable domains
 */
public interface Constraint<T> {

    /**
     * Returns the list of variables involved in this constraint (the scope).
     */
    List<Variable<T>> getScope();

    /**
     * Returns the number of variables in the constraint (arity).
     */
    default int arity() {
        return getScope().size();
    }

    /**
     * Returns a descriptive name for this constraint.
     */
    String getName();

    /**
     * Checks if the constraint is satisfied by the given complete assignment.
     * All variables in the scope must be assigned.
     *
     * @param assignment a complete assignment
     * @return true if the constraint is satisfied
     */
    boolean isSatisfied(Assignment<T> assignment);

    /**
     * Checks if the constraint is consistent with a partial assignment.
     * Returns true if the constraint can still potentially be satisfied
     * (i.e., it's not violated by the current partial assignment).
     *
     * @param assignment a partial assignment
     * @return true if the constraint is not violated
     */
    boolean isConsistent(Assignment<T> assignment);

    /**
     * Checks if assigning a specific value to a variable is consistent
     * with the constraint given the current partial assignment.
     *
     * @param variable   the variable to check
     * @param value      the proposed value
     * @param assignment the current partial assignment (not including variable)
     * @return true if the assignment is consistent with this constraint
     */
    boolean isConsistentWith(Variable<T> variable, T value, Assignment<T> assignment);

    /**
     * Propagates the constraint to reduce domains.
     * Called after an assignment is made.
     *
     * @param assignedVar the variable that was just assigned
     * @param domains     the current domains (may be modified)
     * @param assignment  the current assignment
     * @return true if any domain was reduced
     */
    boolean propagate(Variable<T> assignedVar, Map<Variable<T>, Domain<T>> domains, Assignment<T> assignment);

    /**
     * Returns all arcs for this constraint (for AC-3).
     * An arc (Xi, Xj) represents a directed constraint from Xi to Xj.
     */
    List<Arc<T>> getArcs();

    /**
     * Revises the domain of variable x with respect to variable y.
     * Removes values from x's domain that have no support in y's domain.
     *
     * @param x       the variable whose domain to revise
     * @param y       the supporting variable
     * @param domains the current domains
     * @return true if the domain of x was changed
     */
    boolean revise(Variable<T> x, Variable<T> y, Map<Variable<T>, Domain<T>> domains);

    /**
     * Returns true if this constraint involves the given variable.
     */
    default boolean involves(Variable<T> variable) {
        return getScope().contains(variable);
    }

    /**
     * Returns true if this constraint involves both given variables.
     */
    default boolean involvesBoth(Variable<T> v1, Variable<T> v2) {
        List<Variable<T>> scope = getScope();
        return scope.contains(v1) && scope.contains(v2);
    }
}
