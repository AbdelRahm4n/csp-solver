package com.cspsolver.solver.heuristics.variable;

import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.List;
import java.util.Map;

/**
 * Minimum Remaining Values (MRV) heuristic.
 * Selects the variable with the smallest current domain.
 * Also known as the "fail-first" principle.
 *
 * @param <T> the type of values in the variable domains
 */
public class MRVSelector<T> implements VariableSelector<T> {

    @Override
    public Variable<T> select(
            List<Variable<T>> unassigned,
            Map<Variable<T>, Domain<T>> domains,
            CSP<T> csp,
            Assignment<T> assignment) {

        Variable<T> best = null;
        int minDomainSize = Integer.MAX_VALUE;

        for (Variable<T> var : unassigned) {
            Domain<T> domain = domains.get(var);
            int size = domain != null ? domain.size() : 0;

            if (size < minDomainSize) {
                minDomainSize = size;
                best = var;
            }
        }

        return best;
    }

    @Override
    public String getName() {
        return "MRV";
    }
}
