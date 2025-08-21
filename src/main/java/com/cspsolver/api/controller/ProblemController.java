package com.cspsolver.api.controller;

import com.cspsolver.api.dto.request.NQueensRequest;
import com.cspsolver.api.dto.request.SudokuRequest;
import com.cspsolver.api.dto.response.SolveResponse;
import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Variable;
import com.cspsolver.core.model.Domain;
import com.cspsolver.problems.NQueensMinConflicts;
import com.cspsolver.problems.NQueensProblem;
import com.cspsolver.problems.SudokuProblem;
import com.cspsolver.solver.metrics.SolverMetrics;
import com.cspsolver.solver.SolverConfiguration;
import com.cspsolver.solver.SolverResult;
import com.cspsolver.solver.backtracking.BacktrackingSolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for solving built-in CSP problems.
 */
@RestController
@RequestMapping("/api/v1/problems")
@Tag(name = "Problems", description = "Endpoints for solving built-in CSP problems")
public class ProblemController {

    @PostMapping("/nqueens")
    @Operation(summary = "Solve N-Queens problem",
            description = "Place N queens on an NxN chessboard such that no two queens attack each other. " +
                    "For N >= 50, uses the optimized min-conflicts algorithm for faster solving.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Problem solved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SolveResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<SolveResponse> solveNQueens(@Valid @RequestBody NQueensRequest request) {
        int n = request.n();

        // Use min-conflicts for large N (much faster)
        if (n >= 50) {
            return solveNQueensMinConflicts(n);
        }

        // Use backtracking for small N (can find all solutions)
        NQueensProblem problem = new NQueensProblem(n);
        CSP<Integer> csp = problem.create();

        SolverConfiguration<Integer> config = buildConfig(
                request.timeoutMs(),
                request.maxSolutions(),
                request.variableHeuristic(),
                request.useAC3()
        );

        BacktrackingSolver<Integer> solver = new BacktrackingSolver<>(config);
        SolverResult<Integer> result = solver.solve(csp);

        return ResponseEntity.ok(SolveResponse.from(result, assignment ->
                assignmentToMap(assignment, csp.getVariables())));
    }

    private ResponseEntity<SolveResponse> solveNQueensMinConflicts(int n) {
        SolverMetrics metrics = new SolverMetrics();
        metrics.start();

        NQueensMinConflicts solver = new NQueensMinConflicts(n);
        int[] solution = solver.solve(n * 50); // Max iterations

        metrics.stop();

        if (solution != null) {
            // Build assignment
            Assignment<Integer> assignment = new Assignment<>(n);
            Map<String, Object> solutionMap = new HashMap<>();
            for (int row = 0; row < n; row++) {
                Variable<Integer> var = new Variable<>("Q" + row, Domain.singleton(solution[row]), row);
                assignment.assign(var, solution[row]);
                solutionMap.put("Q" + row, solution[row]);
            }

            metrics.incrementSolutionsFound();

            return ResponseEntity.ok(new SolveResponse(
                    "SATISFIABLE",
                    true,
                    1,
                    List.of(solutionMap),
                    new SolveResponse.MetricsResponse(
                            n, 0, 0, 0, 0, 1, metrics.getElapsedTimeMs()
                    )
            ));
        } else {
            return ResponseEntity.ok(new SolveResponse(
                    "TIMEOUT",
                    false,
                    0,
                    List.of(),
                    new SolveResponse.MetricsResponse(
                            0, 0, 0, 0, 0, 0, metrics.getElapsedTimeMs()
                    )
            ));
        }
    }

    @PostMapping("/sudoku")
    @Operation(summary = "Solve Sudoku puzzle",
            description = "Fill a 9x9 grid so each row, column, and 3x3 box contains digits 1-9 exactly once. " +
                    "Use 0 to represent empty cells in the input grid.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Puzzle solved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SolveResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid puzzle grid",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<SolveResponse> solveSudoku(@Valid @RequestBody SudokuRequest request) {
        SudokuProblem problem = new SudokuProblem(request.grid());
        CSP<Integer> csp = problem.create();

        SolverConfiguration<Integer> config = SolverConfiguration.<Integer>builder()
                .withMRVDegree()
                .withForwardChecking()
                .withAC3Preprocessing(true)
                .withTimeout(request.timeoutMs())
                .build();

        BacktrackingSolver<Integer> solver = new BacktrackingSolver<>(config);
        SolverResult<Integer> result = solver.solve(csp);

        return ResponseEntity.ok(SolveResponse.from(result, assignment -> {
            // Format solution as 9x9 grid
            int[][] grid = new int[9][9];
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    Variable<Integer> var = csp.getVariable("C" + r + c);
                    Integer value = assignment.getValue(var);
                    grid[r][c] = value != null ? value : 0;
                }
            }
            return Map.of("grid", grid);
        }));
    }

    @GetMapping("/nqueens/{n}")
    @Operation(summary = "Solve N-Queens with default settings",
            description = "Quick solve for N-Queens with optimized default configuration (MRV+Degree heuristic, AC-3 preprocessing)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Problem solved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SolveResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid board size",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<SolveResponse> solveNQueensQuick(
            @Parameter(description = "Board size (1-10000)", example = "8", required = true)
            @PathVariable int n) {
        NQueensRequest request = new NQueensRequest(n, 60000L, 1, "MRV_DEGREE", true);
        return solveNQueens(request);
    }

    @GetMapping
    @Operation(summary = "List available problems",
            description = "Returns information about available built-in problems and their endpoints")
    @ApiResponse(responseCode = "200", description = "List of available problems")
    public ResponseEntity<List<Map<String, Object>>> listProblems() {
        return ResponseEntity.ok(List.of(
                Map.of(
                        "name", "nqueens",
                        "description", "N-Queens puzzle",
                        "endpoint", "/api/v1/problems/nqueens",
                        "parameters", Map.of(
                                "n", "Board size (1-10000)",
                                "timeoutMs", "Solving timeout in milliseconds",
                                "maxSolutions", "Maximum solutions to find"
                        )
                ),
                Map.of(
                        "name", "sudoku",
                        "description", "Sudoku puzzle",
                        "endpoint", "/api/v1/problems/sudoku",
                        "parameters", Map.of(
                                "grid", "9x9 grid with 0 for empty cells",
                                "timeoutMs", "Solving timeout in milliseconds"
                        )
                )
        ));
    }

    private <T> SolverConfiguration<T> buildConfig(Long timeoutMs, Integer maxSolutions,
                                                    String heuristic, Boolean useAC3) {
        SolverConfiguration.Builder<T> builder = SolverConfiguration.builder();

        // Set variable heuristic
        switch (heuristic != null ? heuristic.toUpperCase() : "MRV_DEGREE") {
            case "MRV" -> builder.withMRV();
            case "DEGREE" -> builder.withDegree();
            case "DOM_WDEG" -> builder.withDomWDeg();
            default -> builder.withMRVDegree();
        }

        builder.withForwardChecking();
        builder.withAC3Preprocessing(useAC3 != null && useAC3);
        builder.withTimeout(timeoutMs != null ? timeoutMs : 60000L);

        if (maxSolutions != null && maxSolutions > 1) {
            builder.findSolutions(maxSolutions);
        }

        return builder.build();
    }

    private <T> Map<String, Object> assignmentToMap(Assignment<T> assignment, List<Variable<T>> variables) {
        Map<String, Object> result = new HashMap<>();
        for (Variable<T> var : variables) {
            if (assignment.isAssigned(var)) {
                result.put(var.getName(), assignment.getValue(var));
            }
        }
        return result;
    }
}
