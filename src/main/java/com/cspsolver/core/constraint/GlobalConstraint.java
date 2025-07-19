package com.cspsolver.core.constraint;

import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.*;

/**
 * Abstract base class for global constraints (constraints over n variables).
 *
 * @param <T> the type of values in the variable domains
 */
public abstract class GlobalConstraint<T> implements Constraint<T> {

    protected final List<Variable<T>> scope;
    protected final String name;

    protected GlobalConstraint(List<Variable<T>> scope, String name) {
        if (scope == null || scope.isEmpty()) {
            throw new IllegalArgumentException("Scope cannot be null or empty");
        }
        this.scope = Collections.unmodifiableList(new ArrayList<>(scope));
        this.name = name;
    }

    protected GlobalConstraint(List<Variable<T>> scope) {
        this(scope, null);
    }

    @Override
    public List<Variable<T>> getScope() {
        return scope;
    }

    @Override
    public String getName() {
        if (name != null) {
            return name;
        }
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append("(");
        for (int i = 0; i < scope.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(scope.get(i).getName());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean isConsistent(Assignment<T> assignment) {
        // Collect assigned values
        List<T> assignedValues = new ArrayList<>();
        for (Variable<T> var : scope) {
            if (assignment.isAssigned(var)) {
                assignedValues.add(assignment.getValue(var));
            }
        }
        return checkPartial(assignedValues);
    }

    /**
     * Checks if the partial assignment (list of assigned values) is consistent.
     * To be implemented by subclasses.
     */
    protected abstract boolean checkPartial(List<T> assignedValues);

    @Override
    public boolean isConsistentWith(Variable<T> variable, T value, Assignment<T> assignment) {
        if (!scope.contains(variable)) {
            return true;
        }

        // Collect all values including the proposed one
        List<T> values = new ArrayList<>();
        values.add(value);
        for (Variable<T> var : scope) {
            if (!var.equals(variable) && assignment.isAssigned(var)) {
                values.add(assignment.getValue(var));
            }
        }
        return checkPartial(values);
    }

    @Override
    public List<Arc<T>> getArcs() {
        // Default: decompose into binary arcs
        List<Arc<T>> arcs = new ArrayList<>();
        for (int i = 0; i < scope.size(); i++) {
            for (int j = 0; j < scope.size(); j++) {
                if (i != j) {
                    arcs.add(new Arc<>(scope.get(i), scope.get(j), this));
                }
            }
        }
        return arcs;
    }

    @Override
    public boolean revise(Variable<T> x, Variable<T> y, Map<Variable<T>, Domain<T>> domains) {
        // Default binary decomposition revision
        Domain<T> dx = domains.get(x);
        Domain<T> dy = domains.get(y);

        if (dx == null || dy == null) {
            return false;
        }

        boolean revised = false;
        Iterator<T> it = dx.iterator();

        while (it.hasNext()) {
            T xValue = it.next();
            boolean hasSupport = false;

            for (T yValue : dy) {
                if (checkPair(x, xValue, y, yValue)) {
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

    /**
     * Checks if a pair of values for two variables is consistent.
     * Default implementation for binary decomposition.
     */
    protected boolean checkPair(Variable<T> x, T xValue, Variable<T> y, T yValue) {
        // Default: use isConsistentWith with a temporary assignment
        Assignment<T> temp = new Assignment<>(scope.stream()
                .mapToInt(Variable::getIndex)
                .max().orElse(0) + 1);
        temp.assign(y, yValue);
        return isConsistentWith(x, xValue, temp);
    }

    @Override
    public String toString() {
        return getName();
    }
}
