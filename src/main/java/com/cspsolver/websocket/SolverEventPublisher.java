package com.cspsolver.websocket;

import com.cspsolver.core.model.Variable;
import com.cspsolver.solver.metrics.SolverMetrics;

/**
 * Interface for publishing solver events for real-time updates.
 * Implementations can send events via WebSocket or other mechanisms.
 */
public interface SolverEventPublisher {

    /**
     * Called when solving starts.
     */
    void onSolveStarted(String sessionId, int numVariables, int numConstraints);

    /**
     * Called when a variable is selected for assignment.
     */
    void onVariableSelected(String sessionId, String variableName, int domainSize, int depth);

    /**
     * Called when a value is assigned to a variable.
     */
    void onValueAssigned(String sessionId, String variableName, Object value, int depth);

    /**
     * Called when backtracking occurs.
     */
    void onBacktrack(String sessionId, String variableName, int depth);

    /**
     * Called when a solution is found.
     */
    void onSolutionFound(String sessionId, int solutionNumber, SolverMetrics.Snapshot metrics);

    /**
     * Called when solving completes.
     */
    void onSolveCompleted(String sessionId, boolean satisfiable, SolverMetrics.Snapshot metrics);

    /**
     * Called periodically to report progress.
     */
    void onProgress(String sessionId, SolverMetrics.Snapshot metrics);

    /**
     * Returns the session ID for this publisher.
     */
    String getSessionId();
}
