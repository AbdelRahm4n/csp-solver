package com.cspsolver.core.model;

import com.cspsolver.core.constraint.Constraint;
import com.cspsolver.core.constraint.ConstraintNetwork;

import java.util.*;

/**
 * Represents a Constraint Satisfaction Problem.
 * Contains variables, their domains, and constraints.
 *
 * @param <T> the type of values in variable domains
 */
public class CSP<T> {

    private final String name;
    private final List<Variable<T>> variables;
    private final List<Constraint<T>> constraints;
    private final ConstraintNetwork<T> network;
    private final Map<String, Variable<T>> variablesByName;

    private CSP(String name, List<Variable<T>> variables, List<Constraint<T>> constraints) {
        this.name = name;
        this.variables = Collections.unmodifiableList(new ArrayList<>(variables));
        this.constraints = Collections.unmodifiableList(new ArrayList<>(constraints));
        this.network = new ConstraintNetwork<>(this.variables, this.constraints);

        this.variablesByName = new HashMap<>();
        for (Variable<T> var : variables) {
            variablesByName.put(var.getName(), var);
        }
    }

    /**
     * Returns the name of this CSP.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns all variables in this CSP.
     */
    public List<Variable<T>> getVariables() {
        return variables;
    }

    /**
     * Returns the number of variables.
     */
    public int getNumVariables() {
        return variables.size();
    }

    /**
     * Returns a variable by its name.
     */
    public Variable<T> getVariable(String name) {
        return variablesByName.get(name);
    }

    /**
     * Returns a variable by its index.
     */
    public Variable<T> getVariable(int index) {
        return variables.get(index);
    }

    /**
     * Returns all constraints in this CSP.
     */
    public List<Constraint<T>> getConstraints() {
        return constraints;
    }

    /**
     * Returns the number of constraints.
     */
    public int getNumConstraints() {
        return constraints.size();
    }

    /**
     * Returns the constraint network for efficient lookups.
     */
    public ConstraintNetwork<T> getNetwork() {
        return network;
    }

    /**
     * Creates initial working domains for all variables.
     */
    public Map<Variable<T>, Domain<T>> createWorkingDomains() {
        Map<Variable<T>, Domain<T>> domains = new HashMap<>();
        for (Variable<T> var : variables) {
            domains.put(var, var.createWorkingDomain());
        }
        return domains;
    }

    /**
     * Creates an empty assignment for this CSP.
     */
    public Assignment<T> createEmptyAssignment() {
        return new Assignment<>(variables.size());
    }

    @Override
    public String toString() {
        return String.format("CSP[%s: %d variables, %d constraints]",
                name, variables.size(), constraints.size());
    }

    /**
     * Creates a new CSP builder.
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Creates a new CSP builder with a name.
     */
    public static <T> Builder<T> builder(String name) {
        return new Builder<T>().name(name);
    }

    /**
     * Builder for creating CSP instances.
     */
    public static class Builder<T> {
        private String name = "CSP";
        private final List<Variable<T>> variables = new ArrayList<>();
        private final List<Constraint<T>> constraints = new ArrayList<>();
        private final Map<String, Variable<T>> variablesByName = new HashMap<>();
        private int nextIndex = 0;

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Adds a variable with the specified name and domain.
         * The variable is assigned an index automatically.
         */
        public Builder<T> addVariable(String name, Domain<T> domain) {
            if (variablesByName.containsKey(name)) {
                throw new IllegalArgumentException("Variable already exists: " + name);
            }
            Variable<T> variable = new Variable<>(name, domain, nextIndex++);
            variables.add(variable);
            variablesByName.put(name, variable);
            return this;
        }

        /**
         * Adds a pre-created variable.
         * If the variable has no index (-1), one is assigned automatically.
         */
        public Builder<T> addVariable(Variable<T> variable) {
            if (variablesByName.containsKey(variable.getName())) {
                throw new IllegalArgumentException("Variable already exists: " + variable.getName());
            }
            Variable<T> indexedVar = variable.getIndex() >= 0 ? variable :
                    new Variable<>(variable.getName(), variable.getInitialDomain(), nextIndex++);
            variables.add(indexedVar);
            variablesByName.put(indexedVar.getName(), indexedVar);
            return this;
        }

        /**
         * Creates multiple integer variables with range domains.
         */
        public Builder<T> addIntegerVariables(String prefix, int count, int minValue, int maxValue) {
            @SuppressWarnings("unchecked")
            Builder<Integer> intBuilder = (Builder<Integer>) this;
            for (int i = 0; i < count; i++) {
                intBuilder.addVariable(prefix + i, Domain.range(minValue, maxValue));
            }
            return this;
        }

        /**
         * Adds a constraint to the CSP.
         */
        public Builder<T> addConstraint(Constraint<T> constraint) {
            // Validate that all variables in the constraint are in this CSP
            for (Variable<T> var : constraint.getScope()) {
                if (!variablesByName.containsKey(var.getName())) {
                    throw new IllegalArgumentException(
                            "Constraint references unknown variable: " + var.getName());
                }
            }
            constraints.add(constraint);
            return this;
        }

        /**
         * Adds multiple constraints.
         */
        public Builder<T> addConstraints(Collection<? extends Constraint<T>> constraints) {
            for (Constraint<T> c : constraints) {
                addConstraint(c);
            }
            return this;
        }

        /**
         * Returns a variable by name (for use in constraint creation).
         */
        public Variable<T> getVariable(String name) {
            Variable<T> var = variablesByName.get(name);
            if (var == null) {
                throw new IllegalArgumentException("Unknown variable: " + name);
            }
            return var;
        }

        /**
         * Returns all variables added so far.
         */
        public List<Variable<T>> getVariables() {
            return Collections.unmodifiableList(variables);
        }

        /**
         * Builds the CSP.
         */
        public CSP<T> build() {
            if (variables.isEmpty()) {
                throw new IllegalStateException("CSP must have at least one variable");
            }
            return new CSP<>(name, variables, constraints);
        }
    }
}
