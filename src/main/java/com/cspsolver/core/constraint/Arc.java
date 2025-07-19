package com.cspsolver.core.constraint;

import com.cspsolver.core.model.Variable;

import java.util.Objects;

/**
 * Represents a directed arc (Xi, Xj) for arc consistency algorithms.
 * An arc indicates that Xi must be made consistent with respect to Xj.
 *
 * @param <T> the type of values in the variable domains
 */
public class Arc<T> {

    private final Variable<T> x;        // Variable to make consistent
    private final Variable<T> y;        // Supporting variable
    private final Constraint<T> constraint; // The constraint connecting them

    public Arc(Variable<T> x, Variable<T> y, Constraint<T> constraint) {
        this.x = Objects.requireNonNull(x, "Variable x cannot be null");
        this.y = Objects.requireNonNull(y, "Variable y cannot be null");
        this.constraint = Objects.requireNonNull(constraint, "Constraint cannot be null");
    }

    /**
     * Returns the variable to make consistent.
     */
    public Variable<T> getX() {
        return x;
    }

    /**
     * Returns the supporting variable.
     */
    public Variable<T> getY() {
        return y;
    }

    /**
     * Returns the constraint connecting the variables.
     */
    public Constraint<T> getConstraint() {
        return constraint;
    }

    @Override
    public String toString() {
        return "Arc(" + x.getName() + " -> " + y.getName() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Arc<?> arc)) return false;
        return Objects.equals(x, arc.x) &&
               Objects.equals(y, arc.y) &&
               Objects.equals(constraint, arc.constraint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, constraint);
    }
}
