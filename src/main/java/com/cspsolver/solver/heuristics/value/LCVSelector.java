package com.cspsolver.solver.heuristics.value;

import com.cspsolver.core.constraint.Constraint;
import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.*;

/**
 * Least Constraining Value (LCV) heuristic.
 * Orders values by how many values they rule out in neighboring variables.
 * Prefers values that leave the most flexibility for future assignments.
 *
 * Note: This heuristic is expensive (O(d^2 * n) per variable) and is
 * typically only worthwhile for small domains.
 *
 * @param <T> the type of values in the variable domains
 */
public class LCVSelector<T> implements ValueSelector<T> {

    private final int maxDomainSize;

    /**
     * Creates an LCV selector that only activates for small domains.
     *
     * @param maxDomainSize maximum domain size for which to compute LCV
     */
    public LCVSelector(int maxDomainSize) {
        this.maxDomainSize = maxDomainSize;
    }

    /**
     * Creates an LCV selector with default max domain size of 20.
     */
    public LCVSelector() {
        this(20);
    }

    @Override
    public Iterable<T> orderValues(
            Variable<T> variable,
            Domain<T> domain,
            CSP<T> csp,
            Assignment<T> assignment,
            Map<Variable<T>, Domain<T>> domains) {

        // Skip LCV for large domains - too expensive
        if (domain.size() > maxDomainSize) {
            return domain;
        }

        // Compute conflict count for each value
        List<Map.Entry<T, Integer>> valueCounts = new ArrayList<>();

        for (T value : domain) {
            int ruledOut = countRuledOut(variable, value, csp, assignment, domains);
            valueCounts.add(new AbstractMap.SimpleEntry<>(value, ruledOut));
        }

        // Sort by fewest ruled out (ascending)
        valueCounts.sort(Comparator.comparingInt(Map.Entry::getValue));

        // Extract ordered values
        List<T> orderedValues = new ArrayList<>(valueCounts.size());
        for (Map.Entry<T, Integer> entry : valueCounts) {
            orderedValues.add(entry.getKey());
        }

        return orderedValues;
    }

    /**
     * Counts how many values in neighboring domains would be ruled out
     * if we assign this value to the variable.
     */
    private int countRuledOut(
            Variable<T> variable,
            T value,
            CSP<T> csp,
            Assignment<T> assignment,
            Map<Variable<T>, Domain<T>> domains) {

        int ruledOut = 0;

        // Check all constraints involving this variable
        for (Constraint<T> constraint : csp.getNetwork().getConstraintsOn(variable)) {
            // For each unassigned neighbor in the constraint
            for (Variable<T> neighbor : constraint.getScope()) {
                if (neighbor.equals(variable) || assignment.isAssigned(neighbor)) {
                    continue;
                }

                Domain<T> neighborDomain = domains.get(neighbor);
                if (neighborDomain == null) {
                    continue;
                }

                // Count values in neighbor's domain that would become inconsistent
                for (T neighborValue : neighborDomain) {
                    // Temporarily create an assignment with the proposed value
                    Assignment<T> tempAssignment = assignment.copy();
                    tempAssignment.assign(variable, value);

                    if (!constraint.isConsistentWith(neighbor, neighborValue, tempAssignment)) {
                        ruledOut++;
                    }
                }
            }
        }

        return ruledOut;
    }

    @Override
    public String getName() {
        return "LCV";
    }
}
