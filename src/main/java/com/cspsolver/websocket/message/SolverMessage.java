package com.cspsolver.websocket.message;

import com.cspsolver.solver.metrics.SolverMetrics;

import java.time.Instant;
import java.util.Map;

/**
 * WebSocket message for solver events.
 */
public record SolverMessage(
        MessageType type,
        String sessionId,
        long timestamp,
        long nodesExplored,
        long backtracks,
        long elapsedTimeMs,
        Map<String, Object> data
) {
    public enum MessageType {
        STARTED,
        VARIABLE_SELECTED,
        VALUE_ASSIGNED,
        BACKTRACK,
        SOLUTION_FOUND,
        PROGRESS,
        COMPLETED,
        ERROR
    }

    public static SolverMessage started(String sessionId, int numVariables, int numConstraints) {
        return new SolverMessage(
                MessageType.STARTED,
                sessionId,
                Instant.now().toEpochMilli(),
                0, 0, 0,
                Map.of(
                        "numVariables", numVariables,
                        "numConstraints", numConstraints
                )
        );
    }

    public static SolverMessage variableSelected(String sessionId, String variableName,
                                                  int domainSize, int depth, SolverMetrics.Snapshot metrics) {
        return new SolverMessage(
                MessageType.VARIABLE_SELECTED,
                sessionId,
                Instant.now().toEpochMilli(),
                metrics.nodesExplored(),
                metrics.backtracks(),
                metrics.elapsedTimeMs(),
                Map.of(
                        "variable", variableName,
                        "domainSize", domainSize,
                        "depth", depth
                )
        );
    }

    public static SolverMessage valueAssigned(String sessionId, String variableName,
                                               Object value, int depth, SolverMetrics.Snapshot metrics) {
        return new SolverMessage(
                MessageType.VALUE_ASSIGNED,
                sessionId,
                Instant.now().toEpochMilli(),
                metrics.nodesExplored(),
                metrics.backtracks(),
                metrics.elapsedTimeMs(),
                Map.of(
                        "variable", variableName,
                        "value", value.toString(),
                        "depth", depth
                )
        );
    }

    public static SolverMessage backtrack(String sessionId, String variableName,
                                           int depth, SolverMetrics.Snapshot metrics) {
        return new SolverMessage(
                MessageType.BACKTRACK,
                sessionId,
                Instant.now().toEpochMilli(),
                metrics.nodesExplored(),
                metrics.backtracks(),
                metrics.elapsedTimeMs(),
                Map.of(
                        "variable", variableName,
                        "depth", depth
                )
        );
    }

    public static SolverMessage solutionFound(String sessionId, int solutionNumber,
                                               SolverMetrics.Snapshot metrics) {
        return new SolverMessage(
                MessageType.SOLUTION_FOUND,
                sessionId,
                Instant.now().toEpochMilli(),
                metrics.nodesExplored(),
                metrics.backtracks(),
                metrics.elapsedTimeMs(),
                Map.of("solutionNumber", solutionNumber)
        );
    }

    public static SolverMessage progress(String sessionId, SolverMetrics.Snapshot metrics) {
        return new SolverMessage(
                MessageType.PROGRESS,
                sessionId,
                Instant.now().toEpochMilli(),
                metrics.nodesExplored(),
                metrics.backtracks(),
                metrics.elapsedTimeMs(),
                Map.of(
                        "constraintChecks", metrics.constraintChecks(),
                        "domainReductions", metrics.domainReductions()
                )
        );
    }

    public static SolverMessage completed(String sessionId, boolean satisfiable,
                                           SolverMetrics.Snapshot metrics) {
        return new SolverMessage(
                MessageType.COMPLETED,
                sessionId,
                Instant.now().toEpochMilli(),
                metrics.nodesExplored(),
                metrics.backtracks(),
                metrics.elapsedTimeMs(),
                Map.of(
                        "satisfiable", satisfiable,
                        "solutionsFound", metrics.solutionsFound(),
                        "constraintChecks", metrics.constraintChecks()
                )
        );
    }

    public static SolverMessage error(String sessionId, String errorMessage) {
        return new SolverMessage(
                MessageType.ERROR,
                sessionId,
                Instant.now().toEpochMilli(),
                0, 0, 0,
                Map.of("error", errorMessage)
        );
    }
}
