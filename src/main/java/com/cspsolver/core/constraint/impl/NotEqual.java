package com.cspsolver.core.constraint.impl;

import com.cspsolver.core.constraint.BinaryConstraint;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.Map;
import java.util.Objects;

/**
 * Binary constraint: var1 != var2
 *
 * @param <T> the type of values in the variable domains
 */
public class NotEqual<T> extends BinaryConstraint<T> {

    public NotEqual(Variable<T> var1, Variable<T> var2) {
        super(var1, var2);
    }

    public NotEqual(Variable<T> var1, Variable<T> var2, String name) {
        super(var1, var2, name);
    }

    @Override
    protected boolean check(T value1, T value2) {
        return !Objects.equals(value1, value2);
    }

    @Override
    public boolean revise(Variable<T> x, Variable<T> y, Map<Variable<T>, Domain<T>> domains) {
        Domain<T> dx = domains.get(x);
        Domain<T> dy = domains.get(y);

        // Optimization: only prune if y's domain is a singleton
        if (dy.isSingleton()) {
            T yValue = dy.getFirst();
            return dx.remove(yValue);
        }

        return false;
    }

    @Override
    public String getName() {
        return var1.getName() + " != " + var2.getName();
    }
}
