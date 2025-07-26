package com.cspsolver.core.propagation;

import com.cspsolver.core.constraint.Constraint;

/**
 * Result of a propagation operation.
 * Indicates whether propagation succeeded or found a contradiction,
 * along with statistics about the propagation.
 */
public class PropagationResult {

    private final boolean contradiction;
    private final int domainReductions;
    private final int constraintChecks;
    private final int arcRevisions;
    private final Constraint<?> failedConstraint;

    private PropagationResult(boolean contradiction, int domainReductions,
                              int constraintChecks, int arcRevisions,
                              Constraint<?> failedConstraint) {
        this.contradiction = contradiction;
        this.domainReductions = domainReductions;
        this.constraintChecks = constraintChecks;
        this.arcRevisions = arcRevisions;
        this.failedConstraint = failedConstraint;
    }

    /**
     * Creates a successful propagation result.
     */
    public static PropagationResult success() {
        return new PropagationResult(false, 0, 0, 0, null);
    }

    /**
     * Creates a successful propagation result with statistics.
     */
    public static PropagationResult success(int domainReductions, int constraintChecks, int arcRevisions) {
        return new PropagationResult(false, domainReductions, constraintChecks, arcRevisions, null);
    }

    /**
     * Creates a successful propagation result with constraint checks only.
     */
    public static PropagationResult success(int constraintChecks) {
        return new PropagationResult(false, 0, constraintChecks, 0, null);
    }

    /**
     * Creates a contradiction result (domain wipeout).
     */
    public static PropagationResult contradiction() {
        return new PropagationResult(true, 0, 0, 0, null);
    }

    /**
     * Creates a contradiction result with the failed constraint.
     */
    public static PropagationResult contradiction(Constraint<?> failedConstraint) {
        return new PropagationResult(true, 0, 0, 0, failedConstraint);
    }

    /**
     * Creates a contradiction result with statistics.
     */
    public static PropagationResult contradiction(int domainReductions, int constraintChecks,
                                                   int arcRevisions, Constraint<?> failedConstraint) {
        return new PropagationResult(true, domainReductions, constraintChecks, arcRevisions, failedConstraint);
    }

    /**
     * Returns true if propagation found a contradiction (empty domain).
     */
    public boolean isContradiction() {
        return contradiction;
    }

    /**
     * Returns true if propagation succeeded without contradiction.
     */
    public boolean isSuccessful() {
        return !contradiction;
    }

    /**
     * Returns the number of domain values removed during propagation.
     */
    public int getDomainReductions() {
        return domainReductions;
    }

    /**
     * Returns the number of constraint checks performed.
     */
    public int getConstraintChecks() {
        return constraintChecks;
    }

    /**
     * Returns the number of arc revisions performed.
     */
    public int getArcRevisions() {
        return arcRevisions;
    }

    /**
     * Returns the constraint that caused the contradiction, if any.
     */
    public Constraint<?> getFailedConstraint() {
        return failedConstraint;
    }

    /**
     * Combines this result with another, accumulating statistics.
     */
    public PropagationResult combine(PropagationResult other) {
        if (this.contradiction) {
            return this;
        }
        if (other.contradiction) {
            return new PropagationResult(
                    true,
                    this.domainReductions + other.domainReductions,
                    this.constraintChecks + other.constraintChecks,
                    this.arcRevisions + other.arcRevisions,
                    other.failedConstraint
            );
        }
        return new PropagationResult(
                false,
                this.domainReductions + other.domainReductions,
                this.constraintChecks + other.constraintChecks,
                this.arcRevisions + other.arcRevisions,
                null
        );
    }

    @Override
    public String toString() {
        if (contradiction) {
            return "PropagationResult[CONTRADICTION" +
                    (failedConstraint != null ? ", failed=" + failedConstraint.getName() : "") + "]";
        }
        return String.format("PropagationResult[SUCCESS, reductions=%d, checks=%d, revisions=%d]",
                domainReductions, constraintChecks, arcRevisions);
    }
}
