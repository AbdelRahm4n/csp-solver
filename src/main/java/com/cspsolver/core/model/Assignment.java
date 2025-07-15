package com.cspsolver.core.model;

import java.util.*;

/**
 * Represents a (partial or complete) assignment of values to variables.
 * Uses array-based storage for O(1) lookup by variable index.
 *
 * @param <T> the type of values being assigned
 */
public class Assignment<T> {

    private final Object[] values;      // Value for each variable by index
    private final BitSet assigned;      // Which variables are assigned
    private final int totalVariables;   // Total number of variables in CSP
    private int size;                   // Number of assigned variables

    /**
     * Creates an empty assignment for a CSP with the given number of variables.
     */
    public Assignment(int numVariables) {
        this.totalVariables = numVariables;
        this.values = new Object[numVariables];
        this.assigned = new BitSet(numVariables);
        this.size = 0;
    }

    /**
     * Private constructor for copying.
     */
    private Assignment(Object[] values, BitSet assigned, int totalVariables, int size) {
        this.values = values.clone();
        this.assigned = (BitSet) assigned.clone();
        this.totalVariables = totalVariables;
        this.size = size;
    }

    /**
     * Assigns a value to a variable.
     */
    public void assign(Variable<T> variable, T value) {
        int idx = variable.getIndex();
        if (!assigned.get(idx)) {
            size++;
        }
        values[idx] = value;
        assigned.set(idx);
    }

    /**
     * Removes the assignment for a variable.
     */
    public void unassign(Variable<T> variable) {
        int idx = variable.getIndex();
        if (assigned.get(idx)) {
            values[idx] = null;
            assigned.clear(idx);
            size--;
        }
    }

    /**
     * Returns true if the variable has been assigned a value.
     */
    public boolean isAssigned(Variable<T> variable) {
        return assigned.get(variable.getIndex());
    }

    /**
     * Returns the value assigned to a variable, or null if not assigned.
     */
    @SuppressWarnings("unchecked")
    public T getValue(Variable<T> variable) {
        return (T) values[variable.getIndex()];
    }

    /**
     * Returns the number of assigned variables.
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if all variables have been assigned.
     */
    public boolean isComplete() {
        return size == totalVariables;
    }

    /**
     * Returns true if no variables have been assigned.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the total number of variables (assigned + unassigned).
     */
    public int getTotalVariables() {
        return totalVariables;
    }

    /**
     * Creates a deep copy of this assignment.
     */
    public Assignment<T> copy() {
        return new Assignment<>(values, assigned, totalVariables, size);
    }

    /**
     * Returns all assigned variables with their values as a map.
     * Note: This creates a new map, use getValue() for performance-critical code.
     */
    @SuppressWarnings("unchecked")
    public Map<Integer, T> toMap() {
        Map<Integer, T> result = new HashMap<>();
        for (int i = assigned.nextSetBit(0); i >= 0; i = assigned.nextSetBit(i + 1)) {
            result.put(i, (T) values[i]);
        }
        return result;
    }

    /**
     * Returns the indices of all assigned variables.
     */
    public List<Integer> getAssignedIndices() {
        List<Integer> result = new ArrayList<>(size);
        for (int i = assigned.nextSetBit(0); i >= 0; i = assigned.nextSetBit(i + 1)) {
            result.add(i);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Assignment{");
        boolean first = true;
        for (int i = assigned.nextSetBit(0); i >= 0; i = assigned.nextSetBit(i + 1)) {
            if (!first) sb.append(", ");
            sb.append(i).append("=").append(values[i]);
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Assignment<?> that)) return false;
        if (size != that.size || totalVariables != that.totalVariables) return false;
        if (!assigned.equals(that.assigned)) return false;
        for (int i = assigned.nextSetBit(0); i >= 0; i = assigned.nextSetBit(i + 1)) {
            if (!Objects.equals(values[i], that.values[i])) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(size, totalVariables);
        for (int i = assigned.nextSetBit(0); i >= 0; i = assigned.nextSetBit(i + 1)) {
            result = 31 * result + Objects.hashCode(values[i]);
        }
        return result;
    }
}
