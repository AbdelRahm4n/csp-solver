package com.cspsolver.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Request for solving N-Queens problem.
 */
@Schema(description = "Request to solve the N-Queens problem")
public record NQueensRequest(
        @Schema(description = "Board size (number of queens)", example = "8", minimum = "1", maximum = "10000", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(value = 1, message = "N must be at least 1")
        @Max(value = 10000, message = "N must be at most 10000")
        int n,

        @Schema(description = "Timeout in milliseconds", example = "60000", defaultValue = "60000")
        Long timeoutMs,

        @Schema(description = "Maximum number of solutions to find", example = "1", defaultValue = "1")
        Integer maxSolutions,

        @Schema(description = "Variable ordering heuristic", example = "MRV_DEGREE", defaultValue = "MRV_DEGREE",
                allowableValues = {"MRV", "DEGREE", "DOM_WDEG", "MRV_DEGREE"})
        String variableHeuristic,

        @Schema(description = "Whether to use AC-3 preprocessing", example = "true", defaultValue = "true")
        Boolean useAC3
) {
    public NQueensRequest {
        if (timeoutMs == null) timeoutMs = 60000L;
        if (maxSolutions == null) maxSolutions = 1;
        if (variableHeuristic == null) variableHeuristic = "MRV_DEGREE";
        if (useAC3 == null) useAC3 = true;
    }
}
