package com.cspsolver.core.constraint;

import com.cspsolver.core.model.Variable;

import java.util.*;

/**
 * Efficient data structure for constraint lookups.
 * Pre-computes adjacency information for O(1) constraint retrieval.
 *
 * @param <T> the type of values in the variable domains
 */
public class ConstraintNetwork<T> {

    // All constraints in the network
    private final List<Constraint<T>> constraints;

    // Constraints indexed by variable
    private final Map<Variable<T>, List<Constraint<T>>> constraintsByVariable;

    // Binary constraints indexed by variable pair
    private final Map<VariablePair<T>, List<Constraint<T>>> binaryConstraints;

    // Neighbors (variables connected by constraints)
    private final Map<Variable<T>, Set<Variable<T>>> neighbors;

    public ConstraintNetwork(List<Variable<T>> variables, List<Constraint<T>> constraints) {
        this.constraints = new ArrayList<>(constraints);
        this.constraintsByVariable = new HashMap<>();
        this.binaryConstraints = new HashMap<>();
        this.neighbors = new HashMap<>();

        // Initialize maps for all variables
        for (Variable<T> var : variables) {
            constraintsByVariable.put(var, new ArrayList<>());
            neighbors.put(var, new HashSet<>());
        }

        // Index constraints
        for (Constraint<T> constraint : constraints) {
            List<Variable<T>> scope = constraint.getScope();

            // Add to per-variable index
            for (Variable<T> var : scope) {
                constraintsByVariable.get(var).add(constraint);
            }

            // Add neighbor relationships
            for (int i = 0; i < scope.size(); i++) {
                for (int j = i + 1; j < scope.size(); j++) {
                    Variable<T> v1 = scope.get(i);
                    Variable<T> v2 = scope.get(j);
                    neighbors.get(v1).add(v2);
                    neighbors.get(v2).add(v1);

                    // Index binary constraints
                    if (constraint.arity() == 2) {
                        VariablePair<T> pair = new VariablePair<>(v1, v2);
                        binaryConstraints.computeIfAbsent(pair, k -> new ArrayList<>()).add(constraint);
                    }
                }
            }
        }
    }

    /**
     * Returns all constraints involving the given variable.
     */
    public List<Constraint<T>> getConstraintsOn(Variable<T> variable) {
        return constraintsByVariable.getOrDefault(variable, Collections.emptyList());
    }

    /**
     * Returns all binary constraints between two variables.
     */
    public List<Constraint<T>> getConstraintsBetween(Variable<T> v1, Variable<T> v2) {
        VariablePair<T> pair = new VariablePair<>(v1, v2);
        return binaryConstraints.getOrDefault(pair, Collections.emptyList());
    }

    /**
     * Returns all variables connected to the given variable by constraints.
     */
    public Set<Variable<T>> getNeighbors(Variable<T> variable) {
        return neighbors.getOrDefault(variable, Collections.emptySet());
    }

    /**
     * Returns the degree of a variable (number of constraints it participates in).
     */
    public int getDegree(Variable<T> variable) {
        return constraintsByVariable.getOrDefault(variable, Collections.emptyList()).size();
    }

    /**
     * Returns the number of neighbors for a variable.
     */
    public int getNeighborCount(Variable<T> variable) {
        return neighbors.getOrDefault(variable, Collections.emptySet()).size();
    }

    /**
     * Returns all constraints in the network.
     */
    public List<Constraint<T>> getAllConstraints() {
        return Collections.unmodifiableList(constraints);
    }

    /**
     * Returns all arcs in the network for AC-3.
     */
    public List<Arc<T>> getAllArcs() {
        List<Arc<T>> arcs = new ArrayList<>();
        for (Constraint<T> constraint : constraints) {
            arcs.addAll(constraint.getArcs());
        }
        return arcs;
    }

    /**
     * Immutable pair of variables for indexing (order-independent).
     */
    private static class VariablePair<T> {
        private final Variable<T> v1;
        private final Variable<T> v2;

        VariablePair(Variable<T> v1, Variable<T> v2) {
            // Ensure consistent ordering for lookup
            if (v1.getIndex() <= v2.getIndex()) {
                this.v1 = v1;
                this.v2 = v2;
            } else {
                this.v1 = v2;
                this.v2 = v1;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VariablePair<?> that)) return false;
            return Objects.equals(v1, that.v1) && Objects.equals(v2, that.v2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(v1, v2);
        }
    }
}
