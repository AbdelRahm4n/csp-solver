package com.cspsolver.core.constraint.impl;

import com.cspsolver.core.constraint.Arc;
import com.cspsolver.core.constraint.GlobalConstraint;
import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.*;

/**
 * Global constraint: all variables must have different values.
 * This is one of the most commonly used constraints in CSP.
 *
 * @param <T> the type of values in the variable domains
 */
public class AllDifferent<T> extends GlobalConstraint<T> {

    public AllDifferent(List<Variable<T>> variables) {
        super(variables);
    }

    public AllDifferent(List<Variable<T>> variables, String name) {
        super(variables, name);
    }

    @SafeVarargs
    public AllDifferent(Variable<T>... variables) {
        super(Arrays.asList(variables));
    }

    @Override
    public boolean isSatisfied(Assignment<T> assignment) {
        Set<T> seen = new HashSet<>();
        for (Variable<T> var : scope) {
            if (!assignment.isAssigned(var)) {
                throw new IllegalArgumentException("All variables must be assigned");
            }
            T value = assignment.getValue(var);
            if (!seen.add(value)) {
                return false; // Duplicate found
            }
        }
        return true;
    }

    @Override
    protected boolean checkPartial(List<T> assignedValues) {
        Set<T> seen = new HashSet<>();
        for (T value : assignedValues) {
            if (!seen.add(value)) {
                return false; // Duplicate found
            }
        }
        return true;
    }

    @Override
    protected boolean checkPair(Variable<T> x, T xValue, Variable<T> y, T yValue) {
        return !Objects.equals(xValue, yValue);
    }

    @Override
    public boolean propagate(Variable<T> assignedVar, Map<Variable<T>, Domain<T>> domains,
                             Assignment<T> assignment) {
        if (!scope.contains(assignedVar)) {
            return false;
        }

        T assignedValue = assignment.getValue(assignedVar);
        boolean changed = false;

        // Remove the assigned value from all other variables' domains
        for (Variable<T> other : scope) {
            if (!other.equals(assignedVar) && !assignment.isAssigned(other)) {
                Domain<T> otherDomain = domains.get(other);
                if (otherDomain != null && otherDomain.remove(assignedValue)) {
                    changed = true;
                }
            }
        }

        return changed;
    }

    @Override
    public boolean revise(Variable<T> x, Variable<T> y, Map<Variable<T>, Domain<T>> domains) {
        Domain<T> dx = domains.get(x);
        Domain<T> dy = domains.get(y);

        if (dx == null || dy == null) {
            return false;
        }

        // Optimization: only prune if y's domain is a singleton
        if (dy.isSingleton()) {
            T yValue = dy.getFirst();
            return dx.remove(yValue);
        }

        return false;
    }

    @Override
    public List<Arc<T>> getArcs() {
        // Generate all pairwise arcs
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
    public String getName() {
        if (scope.size() <= 4) {
            return "AllDifferent(" + scope.stream()
                    .map(Variable::getName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("") + ")";
        }
        return "AllDifferent[" + scope.size() + " vars]";
    }
}
