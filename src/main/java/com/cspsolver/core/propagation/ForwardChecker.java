package com.cspsolver.core.propagation;

import com.cspsolver.core.constraint.Constraint;
import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.Iterator;
import java.util.Map;

/**
 * Forward Checking propagation algorithm.
 * After each assignment, removes inconsistent values from future variables.
 *
 * @param <T> the type of values in the variable domains
 */
public class ForwardChecker<T> implements PropagationEngine<T> {

    @Override
    public PropagationResult propagate(CSP<T> csp, Map<Variable<T>, Domain<T>> domains) {
        // Forward checking doesn't do preprocessing
        // Could optionally run node consistency here
        return PropagationResult.success();
    }

    @Override
    public PropagationResult propagateAfterAssignment(
            Variable<T> variable,
            T value,
            CSP<T> csp,
            Assignment<T> assignment,
            Map<Variable<T>, Domain<T>> domains) {

        int constraintChecks = 0;
        int domainReductions = 0;

        // Reduce domain of assigned variable to singleton
        Domain<T> varDomain = domains.get(variable);
        if (varDomain != null && varDomain.size() > 1) {
            varDomain.reduceTo(value);
        }

        // Check all constraints involving the assigned variable
        for (Constraint<T> constraint : csp.getNetwork().getConstraintsOn(variable)) {
            // For each unassigned variable in the constraint
            for (Variable<T> futureVar : constraint.getScope()) {
                if (futureVar.equals(variable) || assignment.isAssigned(futureVar)) {
                    continue;
                }

                Domain<T> futureDomain = domains.get(futureVar);
                if (futureDomain == null || futureDomain.isEmpty()) {
                    continue;
                }

                // Remove values that are inconsistent with the assignment
                Iterator<T> it = futureDomain.iterator();
                while (it.hasNext()) {
                    T futureValue = it.next();
                    constraintChecks++;

                    if (!constraint.isConsistentWith(futureVar, futureValue, assignment)) {
                        it.remove();
                        domainReductions++;
                    }
                }

                // Check for domain wipeout
                if (futureDomain.isEmpty()) {
                    return PropagationResult.contradiction(
                            domainReductions, constraintChecks, 0, constraint);
                }
            }
        }

        return PropagationResult.success(domainReductions, constraintChecks, 0);
    }

    @Override
    public String getName() {
        return "Forward Checking";
    }
}
