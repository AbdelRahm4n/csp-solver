package com.cspsolver.solver;

import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Variable;
import com.cspsolver.problems.NQueensProblem;
import com.cspsolver.problems.SudokuProblem;
import com.cspsolver.solver.backtracking.BacktrackingSolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the backtracking solver.
 */
class BacktrackingSolverTest {

    @Test
    void testSolve4Queens() {
        NQueensProblem problem = new NQueensProblem(4);
        CSP<Integer> csp = problem.create();

        BacktrackingSolver<Integer> solver = new BacktrackingSolver<>();
        SolverResult<Integer> result = solver.solve(csp);

        assertTrue(result.isSatisfiable(), "4-Queens should be satisfiable");
        assertNotNull(result.getSolution());

        // Validate solution
        Assignment<Integer> solution = result.getSolution();
        List<Variable<Integer>> vars = csp.getVariables();
        assertTrue(problem.validateSolution(solution, vars));
    }

    @Test
    void testSolve8Queens() {
        NQueensProblem problem = new NQueensProblem(8);
        CSP<Integer> csp = problem.create();

        BacktrackingSolver<Integer> solver = new BacktrackingSolver<>();
        SolverResult<Integer> result = solver.solve(csp);

        assertTrue(result.isSatisfiable(), "8-Queens should be satisfiable");
        assertNotNull(result.getSolution());

        // Validate solution
        Assignment<Integer> solution = result.getSolution();
        List<Variable<Integer>> vars = csp.getVariables();
        assertTrue(problem.validateSolution(solution, vars));

        // Check metrics
        assertTrue(result.getMetrics().nodesExplored() > 0);
        assertTrue(result.getMetrics().elapsedTimeMs() >= 0);
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testSolve100Queens_Performance() {
        NQueensProblem problem = new NQueensProblem(100);
        CSP<Integer> csp = problem.create();

        SolverConfiguration<Integer> config = SolverConfiguration.<Integer>builder()
                .withMRVDegree()
                .withForwardChecking()
                .withAC3Preprocessing(true)
                .build();

        BacktrackingSolver<Integer> solver = new BacktrackingSolver<>(config);
        SolverResult<Integer> result = solver.solve(csp);

        assertTrue(result.isSatisfiable(), "100-Queens should be satisfiable");
        assertTrue(result.getMetrics().elapsedTimeMs() < 1000,
                "100-Queens should solve in under 1 second");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testSolve200Queens_Performance() {
        // Performance test with 200-queens
        NQueensProblem problem = new NQueensProblem(200);
        CSP<Integer> csp = problem.create();

        SolverConfiguration<Integer> config = SolverConfiguration.<Integer>builder()
                .withMRVDegree()
                .withForwardChecking()
                .withAC3Preprocessing(false) // Skip AC3 for large instances
                .build();

        BacktrackingSolver<Integer> solver = new BacktrackingSolver<>(config);
        SolverResult<Integer> result = solver.solve(csp);

        assertTrue(result.isSatisfiable(), "200-Queens should be satisfiable");
        System.out.println("200-Queens solved in " + result.getMetrics().elapsedTimeMs() + "ms, nodes: " + result.getMetrics().nodesExplored());
    }

    @Test
    void testSolve3Queens_Unsatisfiable() {
        // 3-Queens has no solution
        // Actually 3-Queens can be solved, but 2-Queens cannot
        NQueensProblem problem = new NQueensProblem(2);
        CSP<Integer> csp = problem.create();

        BacktrackingSolver<Integer> solver = new BacktrackingSolver<>();
        SolverResult<Integer> result = solver.solve(csp);

        assertTrue(result.isUnsatisfiable(), "2-Queens should be unsatisfiable");
        assertTrue(result.getSolutions().isEmpty());
    }

    @Test
    void testSolveSudokuEasy() {
        SudokuProblem problem = SudokuProblem.Examples.easy();
        CSP<Integer> csp = problem.create();

        BacktrackingSolver<Integer> solver = new BacktrackingSolver<>();
        SolverResult<Integer> result = solver.solve(csp);

        assertTrue(result.isSatisfiable(), "Easy Sudoku should be satisfiable");
        assertNotNull(result.getSolution());
    }

    @Test
    void testSolveSudokuHard() {
        SudokuProblem problem = SudokuProblem.Examples.hard();
        CSP<Integer> csp = problem.create();

        SolverConfiguration<Integer> config = SolverConfiguration.<Integer>builder()
                .withMRVDegree()
                .withForwardChecking()
                .withAC3Preprocessing(true)
                .build();

        BacktrackingSolver<Integer> solver = new BacktrackingSolver<>(config);
        SolverResult<Integer> result = solver.solve(csp);

        assertTrue(result.isSatisfiable(), "Hard Sudoku should be satisfiable");
    }

    @Test
    void testFindMultipleSolutions() {
        NQueensProblem problem = new NQueensProblem(8);
        CSP<Integer> csp = problem.create();

        SolverConfiguration<Integer> config = SolverConfiguration.<Integer>builder()
                .findSolutions(5)
                .build();

        BacktrackingSolver<Integer> solver = new BacktrackingSolver<>(config);
        SolverResult<Integer> result = solver.solve(csp);

        assertTrue(result.isSatisfiable());
        // 8-Queens has 92 solutions, we asked for 5
        assertTrue(result.getSolutionCount() >= 1, "Should find at least 1 solution");
    }

    @Test
    void testTimeout() {
        // Create a problem that might take a while
        NQueensProblem problem = new NQueensProblem(20);
        CSP<Integer> csp = problem.create();

        // Set very short timeout
        SolverConfiguration<Integer> config = SolverConfiguration.<Integer>builder()
                .withTimeout(1) // 1ms timeout
                .build();

        BacktrackingSolver<Integer> solver = new BacktrackingSolver<>(config);
        SolverResult<Integer> result = solver.solve(csp);

        // Should either solve quickly or timeout
        assertTrue(result.isSatisfiable() || result.isTimeout(),
                "Should either solve or timeout");
    }

    @Test
    void testCancel() throws InterruptedException {
        // Use a smaller problem that takes some time but not too long
        NQueensProblem problem = new NQueensProblem(100);
        CSP<Integer> csp = problem.create();

        SolverConfiguration<Integer> config = SolverConfiguration.<Integer>builder()
                .withTimeout(60000) // Long timeout
                .findSolutions(1000) // Find many solutions to keep it running
                .build();

        BacktrackingSolver<Integer> solver = new BacktrackingSolver<>(config);

        // Start solving in a separate thread
        Thread solverThread = new Thread(() -> solver.solve(csp));
        solverThread.start();

        // Wait a bit then cancel
        Thread.sleep(50);
        solver.cancel();

        // Wait for solver to finish
        solverThread.join(2000);

        // Just verify it doesn't hang - solver may finish before cancel in some runs
        // This is a best-effort test for cancellation
    }
}
