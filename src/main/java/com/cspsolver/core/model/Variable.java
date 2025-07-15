package com.cspsolver.core.model;

import java.util.Objects;

/**
 * Represents a variable in a Constraint Satisfaction Problem.
 * Each variable has a name, domain, unique index, and weight (for Dom/WDeg heuristic).
 *
 * @param <T> the type of values in the variable's domain
 */
public class Variable<T> {

    private final String name;
    private final Domain<T> initialDomain;
    private final int index;
    private double weight; // For Dom/WDeg heuristic

    /**
     * Creates a variable with the given name, domain, and index.
     *
     * @param name   the variable name
     * @param domain the initial domain of possible values
     * @param index  unique integer index for array-based lookups
     */
    public Variable(String name, Domain<T> domain, int index) {
        this.name = Objects.requireNonNull(name, "Variable name cannot be null");
        this.initialDomain = Objects.requireNonNull(domain, "Domain cannot be null");
        this.index = index;
        this.weight = 1.0;
    }

    /**
     * Creates a variable with auto-generated index (use CSP.Builder for proper indexing).
     */
    public Variable(String name, Domain<T> domain) {
        this(name, domain, -1);
    }

    public String getName() {
        return name;
    }

    public Domain<T> getInitialDomain() {
        return initialDomain;
    }

    /**
     * Returns the unique index of this variable.
     * Used for O(1) array-based lookups in Assignment and other data structures.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the weight of this variable (for Dom/WDeg heuristic).
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Sets the weight of this variable.
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Increments the weight by the given amount.
     */
    public void incrementWeight(double delta) {
        this.weight += delta;
    }

    /**
     * Creates a copy of the initial domain for use during solving.
     */
    public Domain<T> createWorkingDomain() {
        return initialDomain.copy();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Variable<?> variable)) return false;
        return index == variable.index && Objects.equals(name, variable.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, index);
    }
}
