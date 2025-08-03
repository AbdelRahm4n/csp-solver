package com.cspsolver.solver.heuristics.variable;

import com.cspsolver.core.constraint.Constraint;
import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.List;
import java.util.Map;

/**
 * Degree heuristic.
 * Selects the variable involved in the most constraints with unassigned variables.
 *
 * @param <T> the type of values in the variable domains
 */
public class DegreeSelector<T> implements VariableSelector<T> {

    @Override
    public Variable<T> select(
            List<Variable<T>> unassigned,
            Map<Variable<T>, Domain<T>> domains,
            CSP<T> csp,
            Assignment<T> assignment) {

        Variable<T> best = null;
        int maxDegree = -1;

        for (Variable<T> var : unassigned) {
            int degree = countConstraintsWithUnassigned(var, unassigned, csp);

            if (degree > maxDegree) {
                maxDegree = degree;
                best = var;
            }
        }

        return best;
    }

    /**
     * Counts the number of constraints involving this variable and other unassigned variables.
     */
    private int countConstraintsWithUnassigned(Variable<T> var, List<Variable<T>> unassigned, CSP<T> csp) {
        int count = 0;

        for (Constraint<T> constraint : csp.getNetwork().getConstraintsOn(var)) {
            boolean hasOtherUnassigned = false;
            for (Variable<T> other : constraint.getScope()) {
                if (!other.equals(var) && unassigned.contains(other)) {
                    hasOtherUnassigned = true;
                    break;
                }
            }
            if (hasOtherUnassigned) {
                count++;
            }
        }

        return count;
    }

    @Override
    public String getName() {
        return "Degree";
    }
}
