package com.cspsolver.core.constraint.impl;

import com.cspsolver.core.constraint.Arc;
import com.cspsolver.core.constraint.GlobalConstraint;
import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.*;

/**
 * Linear constraint: sum(coefficients[i] * variables[i]) op rhs
 * where op is one of: ==, <=, >=, <, >
 */
public class LinearConstraint extends GlobalConstraint<Integer> {

    public enum Operator {
        EQ("="),
        LE("<="),
        GE(">="),
        LT("<"),
        GT(">");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    private final int[] coefficients;
    private final Operator operator;
    private final int rhs;

    public LinearConstraint(List<Variable<Integer>> variables, int[] coefficients,
                            Operator operator, int rhs) {
        super(variables);
        if (coefficients.length != variables.size()) {
            throw new IllegalArgumentException("Coefficients length must match variables size");
        }
        this.coefficients = coefficients.clone();
        this.operator = operator;
        this.rhs = rhs;
    }

    public LinearConstraint(List<Variable<Integer>> variables, int[] coefficients,
                            Operator operator, int rhs, String name) {
        super(variables, name);
        if (coefficients.length != variables.size()) {
            throw new IllegalArgumentException("Coefficients length must match variables size");
        }
        this.coefficients = coefficients.clone();
        this.operator = operator;
        this.rhs = rhs;
    }

    /**
     * Creates a sum constraint: sum(variables) op rhs
     */
    public static LinearConstraint sum(List<Variable<Integer>> variables, Operator operator, int rhs) {
        int[] coeffs = new int[variables.size()];
        Arrays.fill(coeffs, 1);
        return new LinearConstraint(variables, coeffs, operator, rhs);
    }

    @Override
    public boolean isSatisfied(Assignment<Integer> assignment) {
        int sum = computeSum(assignment);
        return evaluate(sum);
    }

    @Override
    protected boolean checkPartial(List<Integer> assignedValues) {
        // For partial assignments, we need to check if constraint can still be satisfied
        // This is a simplified check - full bounds propagation would be more powerful
        return true; // Conservatively return true for partial assignments
    }

    @Override
    public boolean isConsistent(Assignment<Integer> assignment) {
        // Count assigned variables
        int assignedCount = 0;
        int partialSum = 0;
        int minPossible = 0;
        int maxPossible = 0;

        for (int i = 0; i < scope.size(); i++) {
            Variable<Integer> var = scope.get(i);
            int coef = coefficients[i];

            if (assignment.isAssigned(var)) {
                assignedCount++;
                partialSum += coef * assignment.getValue(var);
            } else {
                Domain<Integer> domain = var.getInitialDomain();
                if (coef > 0) {
                    minPossible += coef * findMin(domain);
                    maxPossible += coef * findMax(domain);
                } else {
                    minPossible += coef * findMax(domain);
                    maxPossible += coef * findMin(domain);
                }
            }
        }

        // If all assigned, check constraint
        if (assignedCount == scope.size()) {
            return evaluate(partialSum);
        }

        // Check if constraint can still be satisfied
        int minTotal = partialSum + minPossible;
        int maxTotal = partialSum + maxPossible;

        return switch (operator) {
            case EQ -> minTotal <= rhs && rhs <= maxTotal;
            case LE -> minTotal <= rhs;
            case LT -> minTotal < rhs;
            case GE -> maxTotal >= rhs;
            case GT -> maxTotal > rhs;
        };
    }

    @Override
    public boolean isConsistentWith(Variable<Integer> variable, Integer value,
                                    Assignment<Integer> assignment) {
        int varIndex = scope.indexOf(variable);
        if (varIndex < 0) {
            return true;
        }

        // Create temporary assignment with proposed value
        int partialSum = coefficients[varIndex] * value;
        int minPossible = 0;
        int maxPossible = 0;

        for (int i = 0; i < scope.size(); i++) {
            if (i == varIndex) continue;

            Variable<Integer> var = scope.get(i);
            int coef = coefficients[i];

            if (assignment.isAssigned(var)) {
                partialSum += coef * assignment.getValue(var);
            } else {
                Domain<Integer> domain = var.getInitialDomain();
                if (coef > 0) {
                    minPossible += coef * findMin(domain);
                    maxPossible += coef * findMax(domain);
                } else {
                    minPossible += coef * findMax(domain);
                    maxPossible += coef * findMin(domain);
                }
            }
        }

        int minTotal = partialSum + minPossible;
        int maxTotal = partialSum + maxPossible;

        return switch (operator) {
            case EQ -> minTotal <= rhs && rhs <= maxTotal;
            case LE -> minTotal <= rhs;
            case LT -> minTotal < rhs;
            case GE -> maxTotal >= rhs;
            case GT -> maxTotal > rhs;
        };
    }

    @Override
    public boolean propagate(Variable<Integer> assignedVar, Map<Variable<Integer>, Domain<Integer>> domains,
                             Assignment<Integer> assignment) {
        // Bounds propagation
        boolean changed = false;

        for (int i = 0; i < scope.size(); i++) {
            Variable<Integer> var = scope.get(i);
            if (assignment.isAssigned(var)) continue;

            Domain<Integer> domain = domains.get(var);
            if (domain == null) continue;

            Iterator<Integer> it = domain.iterator();
            while (it.hasNext()) {
                Integer val = it.next();
                if (!isConsistentWith(var, val, assignment)) {
                    it.remove();
                    changed = true;
                }
            }
        }

        return changed;
    }

    @Override
    public List<Arc<Integer>> getArcs() {
        // Linear constraints don't decompose well into binary arcs
        // Return empty list - propagation handles everything
        return Collections.emptyList();
    }

    @Override
    public boolean revise(Variable<Integer> x, Variable<Integer> y,
                          Map<Variable<Integer>, Domain<Integer>> domains) {
        // Not easily decomposable - use propagate instead
        return false;
    }

    private int computeSum(Assignment<Integer> assignment) {
        int sum = 0;
        for (int i = 0; i < scope.size(); i++) {
            sum += coefficients[i] * assignment.getValue(scope.get(i));
        }
        return sum;
    }

    private boolean evaluate(int sum) {
        return switch (operator) {
            case EQ -> sum == rhs;
            case LE -> sum <= rhs;
            case GE -> sum >= rhs;
            case LT -> sum < rhs;
            case GT -> sum > rhs;
        };
    }

    private int findMin(Domain<Integer> domain) {
        int min = Integer.MAX_VALUE;
        for (Integer val : domain) {
            if (val < min) min = val;
        }
        return min;
    }

    private int findMax(Domain<Integer> domain) {
        int max = Integer.MIN_VALUE;
        for (Integer val : domain) {
            if (val > max) max = val;
        }
        return max;
    }

    @Override
    public String getName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scope.size(); i++) {
            if (i > 0 && coefficients[i] >= 0) sb.append("+");
            if (coefficients[i] == 1) {
                sb.append(scope.get(i).getName());
            } else if (coefficients[i] == -1) {
                sb.append("-").append(scope.get(i).getName());
            } else {
                sb.append(coefficients[i]).append("*").append(scope.get(i).getName());
            }
        }
        sb.append(" ").append(operator.getSymbol()).append(" ").append(rhs);
        return sb.toString();
    }
}
