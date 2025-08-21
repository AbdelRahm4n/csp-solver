package com.cspsolver.api.dto.response;

import com.cspsolver.solver.SolverResult;
import com.cspsolver.solver.metrics.SolverMetrics;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * Response from solving a CSP.
 */
@Schema(description = "Response from solving a constraint satisfaction problem")
public record SolveResponse(
        @Schema(description = "Result status", example = "SATISFIABLE",
                allowableValues = {"SATISFIABLE", "UNSATISFIABLE", "TIMEOUT", "CANCELLED", "ERROR"})
        String status,

        @Schema(description = "Whether a solution was found", example = "true")
        boolean satisfiable,

        @Schema(description = "Number of solutions found", example = "1")
        int solutionCount,

        @Schema(description = "List of solutions, each mapping variable names to values")
        List<Map<String, Object>> solutions,

        @Schema(description = "Solver performance metrics")
        MetricsResponse metrics
) {
    public static <T> SolveResponse from(SolverResult<T> result, java.util.function.Function<com.cspsolver.core.model.Assignment<T>, Map<String, Object>> solutionMapper) {
        List<Map<String, Object>> solutionMaps = result.getSolutions().stream()
                .map(solutionMapper)
                .toList();

        return new SolveResponse(
                result.getStatus().name(),
                result.isSatisfiable(),
                result.getSolutionCount(),
                solutionMaps,
                MetricsResponse.from(result.getMetrics())
        );
    }

    @Schema(description = "Solver performance metrics")
    public record MetricsResponse(
            @Schema(description = "Number of nodes explored in search tree", example = "150")
            long nodesExplored,

            @Schema(description = "Number of backtracks during search", example = "45")
            long backtracks,

            @Schema(description = "Number of constraint checks performed", example = "1200")
            long constraintChecks,

            @Schema(description = "Number of arc revisions during AC-3", example = "320")
            long arcRevisions,

            @Schema(description = "Number of domain reductions", example = "85")
            long domainReductions,

            @Schema(description = "Number of solutions found", example = "1")
            long solutionsFound,

            @Schema(description = "Total solving time in milliseconds", example = "23")
            long elapsedTimeMs
    ) {
        public static MetricsResponse from(SolverMetrics.Snapshot metrics) {
            return new MetricsResponse(
                    metrics.nodesExplored(),
                    metrics.backtracks(),
                    metrics.constraintChecks(),
                    metrics.arcRevisions(),
                    metrics.domainReductions(),
                    metrics.solutionsFound(),
                    metrics.elapsedTimeMs()
            );
        }
    }
}
