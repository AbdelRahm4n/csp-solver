package com.cspsolver.api.controller;

import com.cspsolver.core.model.CSP;
import com.cspsolver.problems.NQueensProblem;
import com.cspsolver.solver.SolverConfiguration;
import com.cspsolver.solver.SolverResult;
import com.cspsolver.solver.backtracking.BacktrackingSolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for running benchmarks.
 */
@RestController
@RequestMapping("/api/v1/benchmark")
@Tag(name = "Benchmark", description = "Endpoints for performance benchmarking and heuristic comparison")
public class BenchmarkController {

    @GetMapping("/nqueens")
    @Operation(summary = "Benchmark N-Queens solver",
            description = "Run performance benchmarks for various N-Queens sizes. Returns timing and search statistics for each size.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Benchmark completed successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<Map<String, Object>> benchmarkNQueens(
            @Parameter(description = "Comma-separated list of board sizes to benchmark", example = "4,8,16,32,64,100")
            @RequestParam(defaultValue = "4,8,16,32,64,100,200") String sizes,
            @Parameter(description = "Number of runs per size for averaging", example = "3")
            @RequestParam(defaultValue = "3") int runs
    ) {
        List<Map<String, Object>> results = new ArrayList<>();

        String[] sizeStrs = sizes.split(",");
        for (String sizeStr : sizeStrs) {
            int n = Integer.parseInt(sizeStr.trim());
            Map<String, Object> result = benchmarkNQueensSize(n, runs);
            results.add(result);
        }

        return ResponseEntity.ok(Map.of(
                "problem", "N-Queens",
                "runs", runs,
                "configuration", Map.of(
                        "variableHeuristic", "MRV+Degree",
                        "propagation", "Forward Checking",
                        "ac3Preprocessing", true
                ),
                "results", results
        ));
    }

    @GetMapping("/nqueens/{n}")
    @Operation(summary = "Benchmark specific N-Queens size",
            description = "Run detailed benchmark for a specific N-Queens size with multiple runs for statistical accuracy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Benchmark completed successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid board size")
    })
    public ResponseEntity<Map<String, Object>> benchmarkNQueensSpecific(
            @Parameter(description = "Board size to benchmark", example = "100", required = true)
            @PathVariable int n,
            @Parameter(description = "Number of runs for averaging", example = "5")
            @RequestParam(defaultValue = "5") int runs
    ) {
        return ResponseEntity.ok(benchmarkNQueensSize(n, runs));
    }

    @GetMapping("/compare-heuristics")
    @Operation(summary = "Compare variable ordering heuristics",
            description = "Run the same N-Queens problem with different variable ordering heuristics to compare their performance. " +
                    "Compares MRV, Degree, MRV+Degree, and Dom/WDeg heuristics.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comparison completed successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<Map<String, Object>> compareHeuristics(
            @Parameter(description = "Board size for comparison", example = "50")
            @RequestParam(defaultValue = "50") int n,
            @Parameter(description = "Number of runs per heuristic", example = "3")
            @RequestParam(defaultValue = "3") int runs
    ) {
        Map<String, Object> results = new LinkedHashMap<>();

        // Test different heuristics
        String[] heuristics = {"MRV", "DEGREE", "MRV_DEGREE", "DOM_WDEG"};

        for (String heuristic : heuristics) {
            List<Long> times = new ArrayList<>();
            List<Long> nodes = new ArrayList<>();
            List<Long> backtracks = new ArrayList<>();

            for (int i = 0; i < runs; i++) {
                NQueensProblem problem = new NQueensProblem(n);
                CSP<Integer> csp = problem.create();

                SolverConfiguration.Builder<Integer> builder = SolverConfiguration.builder();
                switch (heuristic) {
                    case "MRV" -> builder.withMRV();
                    case "DEGREE" -> builder.withDegree();
                    case "DOM_WDEG" -> builder.withDomWDeg();
                    default -> builder.withMRVDegree();
                }

                SolverConfiguration<Integer> config = builder
                        .withForwardChecking()
                        .withAC3Preprocessing(true)
                        .withTimeout(60000)
                        .build();

                BacktrackingSolver<Integer> solver = new BacktrackingSolver<>(config);
                SolverResult<Integer> result = solver.solve(csp);

                if (result.isSatisfiable()) {
                    times.add(result.getMetrics().elapsedTimeMs());
                    nodes.add(result.getMetrics().nodesExplored());
                    backtracks.add(result.getMetrics().backtracks());
                }
            }

            if (!times.isEmpty()) {
                results.put(heuristic, Map.of(
                        "avgTimeMs", times.stream().mapToLong(Long::longValue).average().orElse(0),
                        "avgNodes", nodes.stream().mapToLong(Long::longValue).average().orElse(0),
                        "avgBacktracks", backtracks.stream().mapToLong(Long::longValue).average().orElse(0)
                ));
            }
        }

        return ResponseEntity.ok(Map.of(
                "problem", n + "-Queens",
                "runs", runs,
                "heuristics", results
        ));
    }

    private Map<String, Object> benchmarkNQueensSize(int n, int runs) {
        List<Long> times = new ArrayList<>();
        List<Long> nodes = new ArrayList<>();
        List<Long> backtracks = new ArrayList<>();
        List<Long> checks = new ArrayList<>();
        boolean solved = false;

        for (int i = 0; i < runs; i++) {
            NQueensProblem problem = new NQueensProblem(n);
            CSP<Integer> csp = problem.create();

            SolverConfiguration<Integer> config = SolverConfiguration.<Integer>builder()
                    .withMRVDegree()
                    .withForwardChecking()
                    .withAC3Preprocessing(true)
                    .withTimeout(60000)
                    .build();

            BacktrackingSolver<Integer> solver = new BacktrackingSolver<>(config);
            SolverResult<Integer> result = solver.solve(csp);

            if (result.isSatisfiable()) {
                solved = true;
                times.add(result.getMetrics().elapsedTimeMs());
                nodes.add(result.getMetrics().nodesExplored());
                backtracks.add(result.getMetrics().backtracks());
                checks.add(result.getMetrics().constraintChecks());
            }
        }

        if (!solved || times.isEmpty()) {
            return Map.of(
                    "n", n,
                    "solved", false
            );
        }

        return Map.of(
                "n", n,
                "solved", true,
                "runs", runs,
                "avgTimeMs", times.stream().mapToLong(Long::longValue).average().orElse(0),
                "minTimeMs", times.stream().mapToLong(Long::longValue).min().orElse(0),
                "maxTimeMs", times.stream().mapToLong(Long::longValue).max().orElse(0),
                "avgNodes", nodes.stream().mapToLong(Long::longValue).average().orElse(0),
                "avgBacktracks", backtracks.stream().mapToLong(Long::longValue).average().orElse(0),
                "avgConstraintChecks", checks.stream().mapToLong(Long::longValue).average().orElse(0)
        );
    }
}
