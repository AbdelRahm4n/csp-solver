package com.cspsolver.core.propagation;

import com.cspsolver.core.constraint.Arc;
import com.cspsolver.core.constraint.Constraint;
import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.*;

/**
 * AC-3 (Arc Consistency 3) propagation algorithm.
 * Enforces arc consistency by removing values that have no support.
 *
 * @param <T> the type of values in the variable domains
 */
public class AC3Propagator<T> implements PropagationEngine<T> {

    @Override
    public PropagationResult propagate(CSP<T> csp, Map<Variable<T>, Domain<T>> domains) {
        // Initialize queue with all arcs
        Queue<Arc<T>> queue = new LinkedList<>();
        Set<Arc<T>> inQueue = new HashSet<>();

        for (Constraint<T> constraint : csp.getConstraints()) {
            for (Arc<T> arc : constraint.getArcs()) {
                if (!inQueue.contains(arc)) {
                    queue.add(arc);
                    inQueue.add(arc);
                }
            }
        }

        return processQueue(queue, inQueue, csp, domains);
    }

    @Override
    public PropagationResult propagateAfterAssignment(
            Variable<T> variable,
            T value,
            CSP<T> csp,
            Assignment<T> assignment,
            Map<Variable<T>, Domain<T>> domains) {

        // Reduce domain of assigned variable to singleton
        Domain<T> varDomain = domains.get(variable);
        if (varDomain != null) {
            varDomain.reduceTo(value);
        }

        // Initialize queue with arcs from neighbors to the assigned variable
        Queue<Arc<T>> queue = new LinkedList<>();
        Set<Arc<T>> inQueue = new HashSet<>();

        for (Constraint<T> constraint : csp.getNetwork().getConstraintsOn(variable)) {
            for (Variable<T> neighbor : constraint.getScope()) {
                if (!neighbor.equals(variable) && !assignment.isAssigned(neighbor)) {
                    for (Arc<T> arc : constraint.getArcs()) {
                        if (arc.getY().equals(variable) && !inQueue.contains(arc)) {
                            queue.add(arc);
                            inQueue.add(arc);
                        }
                    }
                }
            }
        }

        return processQueue(queue, inQueue, csp, domains);
    }

    private PropagationResult processQueue(
            Queue<Arc<T>> queue,
            Set<Arc<T>> inQueue,
            CSP<T> csp,
            Map<Variable<T>, Domain<T>> domains) {

        int arcRevisions = 0;
        int domainReductions = 0;

        while (!queue.isEmpty()) {
            Arc<T> arc = queue.poll();
            inQueue.remove(arc);
            arcRevisions++;

            Variable<T> x = arc.getX();
            Variable<T> y = arc.getY();
            Constraint<T> constraint = arc.getConstraint();

            int sizeBefore = domains.get(x).size();

            if (constraint.revise(x, y, domains)) {
                Domain<T> dx = domains.get(x);
                domainReductions += sizeBefore - dx.size();

                if (dx.isEmpty()) {
                    // Domain wipeout - contradiction found
                    return PropagationResult.contradiction(
                            domainReductions, 0, arcRevisions, constraint);
                }

                // Add arcs (k, x) for all neighbors k of x (except y)
                for (Constraint<T> c : csp.getNetwork().getConstraintsOn(x)) {
                    for (Variable<T> k : c.getScope()) {
                        if (!k.equals(x) && !k.equals(y)) {
                            // Find arc from k to x in this constraint
                            for (Arc<T> newArc : c.getArcs()) {
                                if (newArc.getX().equals(k) && newArc.getY().equals(x)) {
                                    if (!inQueue.contains(newArc)) {
                                        queue.add(newArc);
                                        inQueue.add(newArc);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return PropagationResult.success(domainReductions, 0, arcRevisions);
    }

    @Override
    public String getName() {
        return "AC-3";
    }
}
