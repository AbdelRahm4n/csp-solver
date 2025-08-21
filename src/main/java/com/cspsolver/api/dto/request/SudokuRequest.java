package com.cspsolver.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request for solving Sudoku puzzle.
 */
@Schema(description = "Request to solve a Sudoku puzzle")
public record SudokuRequest(
        @Schema(description = "9x9 Sudoku grid with 0 for empty cells", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "[[5,3,0,0,7,0,0,0,0],[6,0,0,1,9,5,0,0,0],[0,9,8,0,0,0,0,6,0],[8,0,0,0,6,0,0,0,3],[4,0,0,8,0,3,0,0,1],[7,0,0,0,2,0,0,0,6],[0,6,0,0,0,0,2,8,0],[0,0,0,4,1,9,0,0,5],[0,0,0,0,8,0,0,7,9]]")
        @NotNull(message = "Grid is required")
        int[][] grid,

        @Schema(description = "Timeout in milliseconds", example = "60000", defaultValue = "60000")
        Long timeoutMs
) {
    public SudokuRequest {
        if (timeoutMs == null) timeoutMs = 60000L;
    }
}
