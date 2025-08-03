package com.cspsolver.solver.heuristics.value;

import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.Map;

/**
 * Default value selector that returns values in their natural order.
 * Fast and simple - good default when domain sizes are large.
 *
 * @param <T> the type of values in the variable domains
 */
public class DefaultValueSelector<T> implements ValueSelector<T> {

    @Override
    public Iterable<T> orderValues(
            Variable<T> variable,
            Domain<T> domain,
            CSP<T> csp,
            Assignment<T> assignment,
            Map<Variable<T>, Domain<T>> domains) {

        // Return values in their natural iteration order
        return domain;
    }

    @Override
    public String getName() {
        return "Default";
    }
}
