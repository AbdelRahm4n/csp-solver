package com.cspsolver.solver;

import com.cspsolver.core.propagation.AC3Propagator;
import com.cspsolver.core.propagation.ForwardChecker;
import com.cspsolver.core.propagation.PropagationEngine;
import com.cspsolver.solver.heuristics.value.DefaultValueSelector;
import com.cspsolver.solver.heuristics.value.LCVSelector;
import com.cspsolver.solver.heuristics.value.ValueSelector;
import com.cspsolver.solver.heuristics.variable.*;
import com.cspsolver.websocket.SolverEventPublisher;

/**
 * Configuration for the CSP solver.
 * Uses builder pattern for fluent configuration.
 *
 * @param <T> the type of values in the variable domains
 */
public class SolverConfiguration<T> {

    private final VariableSelector<T> variableSelector;
    private final ValueSelector<T> valueSelector;
    private final PropagationEngine<T> propagator;
    private final boolean useAC3Preprocessing;
    private final long timeoutMillis;
    private final boolean findAllSolutions;
    private final int maxSolutions;
    private final SolverEventPublisher eventPublisher;

    private SolverConfiguration(Builder<T> builder) {
        this.variableSelector = builder.variableSelector;
        this.valueSelector = builder.valueSelector;
        this.propagator = builder.propagator;
        this.useAC3Preprocessing = builder.useAC3Preprocessing;
        this.timeoutMillis = builder.timeoutMillis;
        this.findAllSolutions = builder.findAllSolutions;
        this.maxSolutions = builder.maxSolutions;
        this.eventPublisher = builder.eventPublisher;
    }

    public VariableSelector<T> getVariableSelector() {
        return variableSelector;
    }

    public ValueSelector<T> getValueSelector() {
        return valueSelector;
    }

    public PropagationEngine<T> getPropagator() {
        return propagator;
    }

    public boolean isUseAC3Preprocessing() {
        return useAC3Preprocessing;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public boolean isFindAllSolutions() {
        return findAllSolutions;
    }

    public int getMaxSolutions() {
        return maxSolutions;
    }

    public SolverEventPublisher getEventPublisher() {
        return eventPublisher;
    }

    /**
     * Creates a new builder with default settings.
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Creates a default configuration optimized for performance.
     */
    public static <T> SolverConfiguration<T> defaultConfig() {
        return SolverConfiguration.<T>builder().build();
    }

    @Override
    public String toString() {
        return String.format(
                "SolverConfig[var=%s, val=%s, prop=%s, ac3=%s, timeout=%dms, findAll=%s, max=%d]",
                variableSelector.getName(),
                valueSelector.getName(),
                propagator.getName(),
                useAC3Preprocessing,
                timeoutMillis,
                findAllSolutions,
                maxSolutions
        );
    }

    /**
     * Builder for SolverConfiguration.
     */
    public static class Builder<T> {
        private VariableSelector<T> variableSelector = CompositeSelector.mrvWithDegree();
        private ValueSelector<T> valueSelector = new DefaultValueSelector<>();
        private PropagationEngine<T> propagator = new ForwardChecker<>();
        private boolean useAC3Preprocessing = true;
        private long timeoutMillis = 60_000; // 1 minute default
        private boolean findAllSolutions = false;
        private int maxSolutions = 1;
        private SolverEventPublisher eventPublisher = null;

        /**
         * Sets the variable selector to MRV (Minimum Remaining Values).
         */
        public Builder<T> withMRV() {
            this.variableSelector = new MRVSelector<>();
            return this;
        }

        /**
         * Sets the variable selector to MRV with Degree tie-breaking.
         */
        public Builder<T> withMRVDegree() {
            this.variableSelector = CompositeSelector.mrvWithDegree();
            return this;
        }

        /**
         * Sets the variable selector to Degree heuristic.
         */
        public Builder<T> withDegree() {
            this.variableSelector = new DegreeSelector<>();
            return this;
        }

        /**
         * Sets the variable selector to Dom/WDeg.
         */
        public Builder<T> withDomWDeg() {
            this.variableSelector = new DomWDegSelector<>();
            return this;
        }

        /**
         * Sets a custom variable selector.
         */
        public Builder<T> withVariableSelector(VariableSelector<T> selector) {
            this.variableSelector = selector;
            return this;
        }

        /**
         * Sets the value selector to LCV (Least Constraining Value).
         */
        public Builder<T> withLCV() {
            this.valueSelector = new LCVSelector<>();
            return this;
        }

        /**
         * Sets the value selector to LCV with a maximum domain size.
         */
        public Builder<T> withLCV(int maxDomainSize) {
            this.valueSelector = new LCVSelector<>(maxDomainSize);
            return this;
        }

        /**
         * Sets a custom value selector.
         */
        public Builder<T> withValueSelector(ValueSelector<T> selector) {
            this.valueSelector = selector;
            return this;
        }

        /**
         * Sets the propagation engine to Forward Checking.
         */
        public Builder<T> withForwardChecking() {
            this.propagator = new ForwardChecker<>();
            return this;
        }

        /**
         * Sets the propagation engine to AC-3 (MAC).
         */
        public Builder<T> withAC3() {
            this.propagator = new AC3Propagator<>();
            return this;
        }

        /**
         * Sets a custom propagation engine.
         */
        public Builder<T> withPropagator(PropagationEngine<T> propagator) {
            this.propagator = propagator;
            return this;
        }

        /**
         * Enables or disables AC-3 preprocessing.
         */
        public Builder<T> withAC3Preprocessing(boolean enabled) {
            this.useAC3Preprocessing = enabled;
            return this;
        }

        /**
         * Sets the solving timeout in milliseconds.
         */
        public Builder<T> withTimeout(long millis) {
            this.timeoutMillis = millis;
            return this;
        }

        /**
         * Configures the solver to find all solutions.
         */
        public Builder<T> findAllSolutions() {
            this.findAllSolutions = true;
            this.maxSolutions = Integer.MAX_VALUE;
            return this;
        }

        /**
         * Configures the solver to find up to maxSolutions solutions.
         */
        public Builder<T> findSolutions(int maxSolutions) {
            this.findAllSolutions = maxSolutions > 1;
            this.maxSolutions = maxSolutions;
            return this;
        }

        /**
         * Sets the event publisher for real-time updates.
         */
        public Builder<T> withEventPublisher(SolverEventPublisher publisher) {
            this.eventPublisher = publisher;
            return this;
        }

        /**
         * Builds the configuration.
         */
        public SolverConfiguration<T> build() {
            return new SolverConfiguration<>(this);
        }
    }
}
