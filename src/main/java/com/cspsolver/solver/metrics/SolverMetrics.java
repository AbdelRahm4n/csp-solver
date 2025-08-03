package com.cspsolver.solver.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics collected during CSP solving.
 * Thread-safe for use with async solving.
 */
public class SolverMetrics {

    private final AtomicLong nodesExplored = new AtomicLong(0);
    private final AtomicLong backtracks = new AtomicLong(0);
    private final AtomicLong constraintChecks = new AtomicLong(0);
    private final AtomicLong arcRevisions = new AtomicLong(0);
    private final AtomicLong domainReductions = new AtomicLong(0);
    private final AtomicLong solutionsFound = new AtomicLong(0);
    private volatile long startTimeNanos;
    private volatile long endTimeNanos;

    public SolverMetrics() {
        this.startTimeNanos = System.nanoTime();
    }

    public void start() {
        this.startTimeNanos = System.nanoTime();
    }

    public void stop() {
        this.endTimeNanos = System.nanoTime();
    }

    public void incrementNodesExplored() {
        nodesExplored.incrementAndGet();
    }

    public void incrementBacktracks() {
        backtracks.incrementAndGet();
    }

    public void addConstraintChecks(long count) {
        constraintChecks.addAndGet(count);
    }

    public void addArcRevisions(long count) {
        arcRevisions.addAndGet(count);
    }

    public void addDomainReductions(long count) {
        domainReductions.addAndGet(count);
    }

    public void incrementSolutionsFound() {
        solutionsFound.incrementAndGet();
    }

    public long getNodesExplored() {
        return nodesExplored.get();
    }

    public long getBacktracks() {
        return backtracks.get();
    }

    public long getConstraintChecks() {
        return constraintChecks.get();
    }

    public long getArcRevisions() {
        return arcRevisions.get();
    }

    public long getDomainReductions() {
        return domainReductions.get();
    }

    public long getSolutionsFound() {
        return solutionsFound.get();
    }

    /**
     * Returns the elapsed time in milliseconds.
     */
    public long getElapsedTimeMs() {
        long end = endTimeNanos > 0 ? endTimeNanos : System.nanoTime();
        return (end - startTimeNanos) / 1_000_000;
    }

    /**
     * Returns the elapsed time in nanoseconds.
     */
    public long getElapsedTimeNanos() {
        long end = endTimeNanos > 0 ? endTimeNanos : System.nanoTime();
        return end - startTimeNanos;
    }

    /**
     * Returns the effective branching factor.
     * Approximate measure of search efficiency.
     */
    public double getEffectiveBranchingFactor() {
        long nodes = nodesExplored.get();
        if (nodes <= 1) {
            return 1.0;
        }
        // Approximation: b* where nodes ≈ (b*)^depth
        // Using log approximation
        long depth = solutionsFound.get() > 0 ? nodesExplored.get() - backtracks.get() : nodesExplored.get();
        if (depth <= 0) {
            return 1.0;
        }
        return Math.pow(nodes, 1.0 / Math.max(depth, 1));
    }

    /**
     * Resets all metrics.
     */
    public void reset() {
        nodesExplored.set(0);
        backtracks.set(0);
        constraintChecks.set(0);
        arcRevisions.set(0);
        domainReductions.set(0);
        solutionsFound.set(0);
        startTimeNanos = System.nanoTime();
        endTimeNanos = 0;
    }

    /**
     * Creates an immutable snapshot of current metrics.
     */
    public Snapshot snapshot() {
        return new Snapshot(
                nodesExplored.get(),
                backtracks.get(),
                constraintChecks.get(),
                arcRevisions.get(),
                domainReductions.get(),
                solutionsFound.get(),
                getElapsedTimeMs()
        );
    }

    @Override
    public String toString() {
        return String.format(
                "SolverMetrics[nodes=%d, backtracks=%d, checks=%d, revisions=%d, time=%dms]",
                nodesExplored.get(), backtracks.get(), constraintChecks.get(),
                arcRevisions.get(), getElapsedTimeMs()
        );
    }

    /**
     * Immutable snapshot of metrics at a point in time.
     */
    public record Snapshot(
            long nodesExplored,
            long backtracks,
            long constraintChecks,
            long arcRevisions,
            long domainReductions,
            long solutionsFound,
            long elapsedTimeMs
    ) {
        @Override
        public String toString() {
            return String.format(
                    "Metrics[nodes=%d, backtracks=%d, checks=%d, time=%dms]",
                    nodesExplored, backtracks, constraintChecks, elapsedTimeMs
            );
        }
    }
}
