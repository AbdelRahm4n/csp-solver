package com.cspsolver.websocket;

import com.cspsolver.solver.metrics.SolverMetrics;
import com.cspsolver.websocket.message.SolverMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Implementation of SolverEventPublisher that sends events via STOMP/WebSocket.
 */
public class StompSolverEventPublisher implements SolverEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final String sessionId;
    private final SolverMetrics metrics;

    public StompSolverEventPublisher(SimpMessagingTemplate messagingTemplate, String sessionId) {
        this.messagingTemplate = messagingTemplate;
        this.sessionId = sessionId;
        this.metrics = new SolverMetrics();
    }

    private void send(SolverMessage message) {
        messagingTemplate.convertAndSend("/topic/solver/" + sessionId, message);
    }

    @Override
    public void onSolveStarted(String sessionId, int numVariables, int numConstraints) {
        metrics.start();
        send(SolverMessage.started(sessionId, numVariables, numConstraints));
    }

    @Override
    public void onVariableSelected(String sessionId, String variableName, int domainSize, int depth) {
        send(SolverMessage.variableSelected(sessionId, variableName, domainSize, depth, metrics.snapshot()));
    }

    @Override
    public void onValueAssigned(String sessionId, String variableName, Object value, int depth) {
        send(SolverMessage.valueAssigned(sessionId, variableName, value, depth, metrics.snapshot()));
    }

    @Override
    public void onBacktrack(String sessionId, String variableName, int depth) {
        send(SolverMessage.backtrack(sessionId, variableName, depth, metrics.snapshot()));
    }

    @Override
    public void onSolutionFound(String sessionId, int solutionNumber, SolverMetrics.Snapshot metricsSnapshot) {
        send(SolverMessage.solutionFound(sessionId, solutionNumber, metricsSnapshot));
    }

    @Override
    public void onSolveCompleted(String sessionId, boolean satisfiable, SolverMetrics.Snapshot metricsSnapshot) {
        send(SolverMessage.completed(sessionId, satisfiable, metricsSnapshot));
    }

    @Override
    public void onProgress(String sessionId, SolverMetrics.Snapshot metricsSnapshot) {
        send(SolverMessage.progress(sessionId, metricsSnapshot));
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }
}
