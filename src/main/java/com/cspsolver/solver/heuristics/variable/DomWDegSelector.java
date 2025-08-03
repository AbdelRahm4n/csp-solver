package com.cspsolver.solver.heuristics.variable;

import com.cspsolver.core.constraint.Constraint;
import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain over Weighted Degree (Dom/WDeg) heuristic.
 * Selects the variable with the smallest ratio of domain size to weighted degree.
 * Weights are learned from constraint failures during search.
 *
 * @param <T> the type of values in the variable domains
 */
public class DomWDegSelector<T> implements VariableSelector<T> {

    private final Map<Constraint<T>, Double> constraintWeights = new HashMap<>();
    private static final double INITIAL_WEIGHT = 1.0;
    private static final double EPSILON = 0.0001; // Avoid division by zero

    @Override
    public Variable<T> select(
            List<Variable<T>> unassigned,
            Map<Variable<T>, Domain<T>> domains,
            CSP<T> csp,
            Assignment<T> assignment) {

        Variable<T> best = null;
        double minRatio = Double.MAX_VALUE;

        for (Variable<T> var : unassigned) {
            Domain<T> domain = domains.get(var);
            double domSize = domain != null ? domain.size() : 0;

            if (domSize == 0) {
                // Empty domain - should be caught by propagation, but handle just in case
                return var;
            }

            double wDeg = computeWeightedDegree(var, unassigned, csp);
            double ratio = domSize / Math.max(wDeg, EPSILON);

            if (ratio < minRatio) {
                minRatio = ratio;
                best = var;
            }
        }

        return best;
    }

    /**
     * Computes the weighted degree of a variable.
     * Sum of weights of constraints involving this variable and other unassigned variables.
     */
    private double computeWeightedDegree(Variable<T> var, List<Variable<T>> unassigned, CSP<T> csp) {
        double wDeg = 0;

        for (Constraint<T> constraint : csp.getNetwork().getConstraintsOn(var)) {
            boolean hasOtherUnassigned = false;
            for (Variable<T> other : constraint.getScope()) {
                if (!other.equals(var) && unassigned.contains(other)) {
                    hasOtherUnassigned = true;
                    break;
                }
            }

            if (hasOtherUnassigned) {
                wDeg += constraintWeights.getOrDefault(constraint, INITIAL_WEIGHT);
            }
        }

        return wDeg;
    }

    @Override
    public void recordFailure(Variable<T> variable, Constraint<T> constraint) {
        constraintWeights.merge(constraint, 1.0, Double::sum);
    }

    @Override
    public void reset() {
        constraintWeights.clear();
    }

    /**
     * Gets the current weight of a constraint.
     */
    public double getWeight(Constraint<T> constraint) {
        return constraintWeights.getOrDefault(constraint, INITIAL_WEIGHT);
    }

    /**
     * Sets the weight of a constraint.
     */
    public void setWeight(Constraint<T> constraint, double weight) {
        constraintWeights.put(constraint, weight);
    }

    @Override
    public String getName() {
        return "Dom/WDeg";
    }
}
