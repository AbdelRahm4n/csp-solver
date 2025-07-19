package com.cspsolver.core.constraint;

import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.*;

/**
 * Abstract base class for binary constraints (constraints over exactly two variables).
 *
 * @param <T> the type of values in the variable domains
 */
public abstract class BinaryConstraint<T> implements Constraint<T> {

    protected final Variable<T> var1;
    protected final Variable<T> var2;
    protected final List<Variable<T>> scope;
    protected final String name;

    protected BinaryConstraint(Variable<T> var1, Variable<T> var2, String name) {
        this.var1 = Objects.requireNonNull(var1, "var1 cannot be null");
        this.var2 = Objects.requireNonNull(var2, "var2 cannot be null");
        this.scope = List.of(var1, var2);
        this.name = name;
    }

    protected BinaryConstraint(Variable<T> var1, Variable<T> var2) {
        this(var1, var2, null);
    }

    @Override
    public List<Variable<T>> getScope() {
        return scope;
    }

    @Override
    public String getName() {
        return name != null ? name :
                getClass().getSimpleName() + "(" + var1.getName() + ", " + var2.getName() + ")";
    }

    /**
     * Core check method to be implemented by subclasses.
     * Returns true if the pair of values satisfies the constraint.
     */
    protected abstract boolean check(T value1, T value2);

    @Override
    public boolean isSatisfied(Assignment<T> assignment) {
        if (!assignment.isAssigned(var1) || !assignment.isAssigned(var2)) {
            throw new IllegalArgumentException("Assignment must include both variables");
        }
        return check(assignment.getValue(var1), assignment.getValue(var2));
    }

    @Override
    public boolean isConsistent(Assignment<T> assignment) {
        // If both are assigned, check the constraint
        if (assignment.isAssigned(var1) && assignment.isAssigned(var2)) {
            return check(assignment.getValue(var1), assignment.getValue(var2));
        }
        // If only one or neither is assigned, constraint is not violated yet
        return true;
    }

    @Override
    public boolean isConsistentWith(Variable<T> variable, T value, Assignment<T> assignment) {
        if (variable.equals(var1)) {
            if (assignment.isAssigned(var2)) {
                return check(value, assignment.getValue(var2));
            }
        } else if (variable.equals(var2)) {
            if (assignment.isAssigned(var1)) {
                return check(assignment.getValue(var1), value);
            }
        }
        // Variable not in scope or other variable not assigned
        return true;
    }

    @Override
    public boolean propagate(Variable<T> assignedVar, Map<Variable<T>, Domain<T>> domains,
                             Assignment<T> assignment) {
        if (!scope.contains(assignedVar)) {
            return false;
        }

        Variable<T> otherVar = assignedVar.equals(var1) ? var2 : var1;

        // If other variable is already assigned, no propagation needed
        if (assignment.isAssigned(otherVar)) {
            return false;
        }

        T assignedValue = assignment.getValue(assignedVar);
        Domain<T> otherDomain = domains.get(otherVar);
        boolean changed = false;

        // Remove inconsistent values from other variable's domain
        Iterator<T> it = otherDomain.iterator();
        while (it.hasNext()) {
            T otherValue = it.next();
            boolean consistent = assignedVar.equals(var1) ?
                    check(assignedValue, otherValue) :
                    check(otherValue, assignedValue);
            if (!consistent) {
                it.remove();
                changed = true;
            }
        }

        return changed;
    }

    @Override
    public List<Arc<T>> getArcs() {
        return List.of(
                new Arc<>(var1, var2, this),
                new Arc<>(var2, var1, this)
        );
    }

    @Override
    public boolean revise(Variable<T> x, Variable<T> y, Map<Variable<T>, Domain<T>> domains) {
        if (!scope.contains(x) || !scope.contains(y)) {
            return false;
        }

        Domain<T> dx = domains.get(x);
        Domain<T> dy = domains.get(y);
        boolean revised = false;

        Iterator<T> it = dx.iterator();
        while (it.hasNext()) {
            T xValue = it.next();
            boolean hasSupport = false;

            for (T yValue : dy) {
                boolean consistent = x.equals(var1) ?
                        check(xValue, yValue) :
                        check(yValue, xValue);
                if (consistent) {
                    hasSupport = true;
                    break;
                }
            }

            if (!hasSupport) {
                it.remove();
                revised = true;
            }
        }

        return revised;
    }

    public Variable<T> getVar1() {
        return var1;
    }

    public Variable<T> getVar2() {
        return var2;
    }

    /**
     * Returns the other variable in the constraint.
     */
    public Variable<T> getOther(Variable<T> var) {
        if (var.equals(var1)) return var2;
        if (var.equals(var2)) return var1;
        throw new IllegalArgumentException("Variable not in constraint: " + var);
    }

    @Override
    public String toString() {
        return getName();
    }
}
