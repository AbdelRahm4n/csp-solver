package com.cspsolver.core.constraint.impl;

import com.cspsolver.core.constraint.BinaryConstraint;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.Iterator;
import java.util.Map;

/**
 * Binary constraint: var1 <= var2
 * Only works with Comparable types.
 *
 * @param <T> the type of values in the variable domains (must be Comparable)
 */
public class LessThanOrEqual<T extends Comparable<T>> extends BinaryConstraint<T> {

    public LessThanOrEqual(Variable<T> var1, Variable<T> var2) {
        super(var1, var2);
    }

    public LessThanOrEqual(Variable<T> var1, Variable<T> var2, String name) {
        super(var1, var2, name);
    }

    @Override
    protected boolean check(T value1, T value2) {
        return value1.compareTo(value2) <= 0;
    }

    @Override
    public boolean revise(Variable<T> x, Variable<T> y, Map<Variable<T>, Domain<T>> domains) {
        Domain<T> dx = domains.get(x);
        Domain<T> dy = domains.get(y);
        boolean revised = false;

        if (x.equals(var1)) {
            // var1 <= var2: remove values from var1 > max(var2)
            T maxY = findMax(dy);
            if (maxY != null) {
                Iterator<T> it = dx.iterator();
                while (it.hasNext()) {
                    T xVal = it.next();
                    if (xVal.compareTo(maxY) > 0) {
                        it.remove();
                        revised = true;
                    }
                }
            }
        } else {
            // var1 <= var2: remove values from var2 < min(var1)
            T minY = findMin(dy);
            if (minY != null) {
                Iterator<T> it = dx.iterator();
                while (it.hasNext()) {
                    T xVal = it.next();
                    if (xVal.compareTo(minY) < 0) {
                        it.remove();
                        revised = true;
                    }
                }
            }
        }

        return revised;
    }

    private T findMax(Domain<T> domain) {
        T max = null;
        for (T val : domain) {
            if (max == null || val.compareTo(max) > 0) {
                max = val;
            }
        }
        return max;
    }

    private T findMin(Domain<T> domain) {
        T min = null;
        for (T val : domain) {
            if (min == null || val.compareTo(min) < 0) {
                min = val;
            }
        }
        return min;
    }

    @Override
    public String getName() {
        return var1.getName() + " <= " + var2.getName();
    }
}
