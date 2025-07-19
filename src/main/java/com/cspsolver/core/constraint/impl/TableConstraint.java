package com.cspsolver.core.constraint.impl;

import com.cspsolver.core.constraint.Arc;
import com.cspsolver.core.constraint.GlobalConstraint;
import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.*;

/**
 * Extensional constraint defined by a set of allowed (or disallowed) tuples.
 * Each tuple specifies a valid (or invalid) combination of values for the variables.
 *
 * @param <T> the type of values in the variable domains
 */
public class TableConstraint<T> extends GlobalConstraint<T> {

    private final Set<List<T>> tuples;
    private final boolean allowed; // true = allowed tuples, false = disallowed tuples

    // Index for faster lookup: variable index -> value -> supporting tuples
    private final Map<Integer, Map<T, Set<List<T>>>> supports;

    /**
     * Creates a table constraint with allowed tuples.
     */
    public TableConstraint(List<Variable<T>> variables, Set<List<T>> allowedTuples) {
        this(variables, allowedTuples, true);
    }

    /**
     * Creates a table constraint.
     *
     * @param variables the variables in scope
     * @param tuples    the tuples
     * @param allowed   true if tuples are allowed, false if disallowed
     */
    public TableConstraint(List<Variable<T>> variables, Set<List<T>> tuples, boolean allowed) {
        super(variables);
        this.tuples = new HashSet<>();
        for (List<T> tuple : tuples) {
            if (tuple.size() != variables.size()) {
                throw new IllegalArgumentException("Tuple size must match number of variables");
            }
            this.tuples.add(new ArrayList<>(tuple));
        }
        this.allowed = allowed;

        // Build support index
        this.supports = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            supports.put(i, new HashMap<>());
        }

        if (allowed) {
            for (List<T> tuple : this.tuples) {
                for (int i = 0; i < tuple.size(); i++) {
                    T value = tuple.get(i);
                    supports.get(i)
                            .computeIfAbsent(value, k -> new HashSet<>())
                            .add(tuple);
                }
            }
        }
    }

    /**
     * Creates a table constraint with allowed tuples specified as varargs.
     */
    @SafeVarargs
    public static <T> TableConstraint<T> allowed(List<Variable<T>> variables, List<T>... tuples) {
        return new TableConstraint<>(variables, new HashSet<>(Arrays.asList(tuples)));
    }

    /**
     * Creates a table constraint with disallowed tuples.
     */
    @SafeVarargs
    public static <T> TableConstraint<T> disallowed(List<Variable<T>> variables, List<T>... tuples) {
        return new TableConstraint<>(variables, new HashSet<>(Arrays.asList(tuples)), false);
    }

    @Override
    public boolean isSatisfied(Assignment<T> assignment) {
        List<T> tuple = new ArrayList<>(scope.size());
        for (Variable<T> var : scope) {
            if (!assignment.isAssigned(var)) {
                throw new IllegalArgumentException("All variables must be assigned");
            }
            tuple.add(assignment.getValue(var));
        }

        boolean inTable = tuples.contains(tuple);
        return allowed ? inTable : !inTable;
    }

    @Override
    protected boolean checkPartial(List<T> assignedValues) {
        // For partial assignments, check if any allowed tuple is still compatible
        if (!allowed) {
            // For disallowed tuples, partial is always potentially satisfiable
            return true;
        }

        // This is a simplified check - full GAC would be more powerful
        return true;
    }

    @Override
    public boolean isConsistent(Assignment<T> assignment) {
        // Build partial tuple and check
        List<T> partialTuple = new ArrayList<>(scope.size());
        List<Integer> unassignedIndices = new ArrayList<>();

        for (int i = 0; i < scope.size(); i++) {
            Variable<T> var = scope.get(i);
            if (assignment.isAssigned(var)) {
                partialTuple.add(assignment.getValue(var));
            } else {
                partialTuple.add(null);
                unassignedIndices.add(i);
            }
        }

        if (unassignedIndices.isEmpty()) {
            // All assigned - check directly
            boolean inTable = tuples.contains(partialTuple);
            return allowed ? inTable : !inTable;
        }

        if (!allowed) {
            // For disallowed tuples, check if partial matches any disallowed tuple
            return true; // Conservatively return true
        }

        // For allowed tuples, check if any tuple matches the partial assignment
        for (List<T> tuple : tuples) {
            boolean matches = true;
            for (int i = 0; i < scope.size(); i++) {
                T partialValue = partialTuple.get(i);
                if (partialValue != null && !Objects.equals(partialValue, tuple.get(i))) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isConsistentWith(Variable<T> variable, T value, Assignment<T> assignment) {
        int varIndex = scope.indexOf(variable);
        if (varIndex < 0) {
            return true;
        }

        if (!allowed) {
            // For disallowed tuples, conservatively return true
            // unless we can check the complete tuple
            int assignedCount = 0;
            for (Variable<T> v : scope) {
                if (v.equals(variable) || assignment.isAssigned(v)) {
                    assignedCount++;
                }
            }
            if (assignedCount == scope.size()) {
                List<T> tuple = new ArrayList<>(scope.size());
                for (Variable<T> v : scope) {
                    tuple.add(v.equals(variable) ? value : assignment.getValue(v));
                }
                return !tuples.contains(tuple);
            }
            return true;
        }

        // For allowed tuples, check if any tuple supports this value
        Set<List<T>> supportingTuples = supports.get(varIndex).get(value);
        if (supportingTuples == null || supportingTuples.isEmpty()) {
            return false;
        }

        for (List<T> tuple : supportingTuples) {
            boolean matches = true;
            for (int i = 0; i < scope.size(); i++) {
                if (i == varIndex) continue;
                Variable<T> var = scope.get(i);
                if (assignment.isAssigned(var)) {
                    if (!Objects.equals(assignment.getValue(var), tuple.get(i))) {
                        matches = false;
                        break;
                    }
                }
            }
            if (matches) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean propagate(Variable<T> assignedVar, Map<Variable<T>, Domain<T>> domains,
                             Assignment<T> assignment) {
        if (!allowed) {
            return false; // Limited propagation for disallowed tuples
        }

        boolean changed = false;

        for (int i = 0; i < scope.size(); i++) {
            Variable<T> var = scope.get(i);
            if (assignment.isAssigned(var)) continue;

            Domain<T> domain = domains.get(var);
            if (domain == null) continue;

            Iterator<T> it = domain.iterator();
            while (it.hasNext()) {
                T value = it.next();
                if (!isConsistentWith(var, value, assignment)) {
                    it.remove();
                    changed = true;
                }
            }
        }

        return changed;
    }

    @Override
    public boolean revise(Variable<T> x, Variable<T> y, Map<Variable<T>, Domain<T>> domains) {
        if (!allowed) {
            return false;
        }

        int xIndex = scope.indexOf(x);
        int yIndex = scope.indexOf(y);

        if (xIndex < 0 || yIndex < 0) {
            return false;
        }

        Domain<T> dx = domains.get(x);
        Domain<T> dy = domains.get(y);

        boolean revised = false;

        Iterator<T> it = dx.iterator();
        while (it.hasNext()) {
            T xValue = it.next();
            boolean hasSupport = false;

            Set<List<T>> supportingTuples = supports.get(xIndex).get(xValue);
            if (supportingTuples != null) {
                for (List<T> tuple : supportingTuples) {
                    T yValueInTuple = tuple.get(yIndex);
                    if (dy.contains(yValueInTuple)) {
                        hasSupport = true;
                        break;
                    }
                }
            }

            if (!hasSupport) {
                it.remove();
                revised = true;
            }
        }

        return revised;
    }

    @Override
    public List<Arc<T>> getArcs() {
        if (!allowed) {
            return Collections.emptyList();
        }

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

    public Set<List<T>> getTuples() {
        return Collections.unmodifiableSet(tuples);
    }

    public boolean isAllowed() {
        return allowed;
    }

    @Override
    public String getName() {
        return "Table" + (allowed ? "+" : "-") + "[" + tuples.size() + " tuples]";
    }
}
