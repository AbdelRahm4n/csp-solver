package com.cspsolver.problems;

import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;
import com.cspsolver.solver.SolverConfiguration;
import com.cspsolver.solver.SolverResult;
import com.cspsolver.solver.metrics.SolverMetrics;

import java.util.BitSet;

/**
 * Optimized N-Queens solver using specialized data structures.
 * Uses BitSet-based column and diagonal tracking for O(1) conflict checking.
 */
public class NQueensOptimized {

    private final int n;

    public NQueensOptimized(int n) {
        this.n = n;
    }

    /**
     * Solves N-Queens using an optimized backtracking approach.
     * Target: 1000-queens in < 2 seconds.
     */
    public SolverResult<Integer> solve() {
        SolverMetrics metrics = new SolverMetrics();
        metrics.start();

        int[] solution = new int[n];
        BitSet columns = new BitSet(n);
        BitSet diag1 = new BitSet(2 * n);  // row + col
        BitSet diag2 = new BitSet(2 * n);  // row - col + n

        boolean found = backtrack(0, solution, columns, diag1, diag2, metrics);

        metrics.stop();

        if (found) {
            // Convert to Assignment format
            Assignment<Integer> assignment = new Assignment<>(n);
            for (int row = 0; row < n; row++) {
                Variable<Integer> var = new Variable<>("Q" + row, Domain.singleton(solution[row]), row);
                assignment.assign(var, solution[row]);
            }
            return SolverResult.satisfiable(assignment, metrics.snapshot());
        } else {
            return SolverResult.unsatisfiable(metrics.snapshot());
        }
    }

    private boolean backtrack(int row, int[] solution, BitSet columns,
                               BitSet diag1, BitSet diag2, SolverMetrics metrics) {
        if (row == n) {
            metrics.incrementSolutionsFound();
            return true;
        }

        // Simple sequential column ordering - fastest for large N
        for (int col = 0; col < n; col++) {
            int d1 = row + col;
            int d2 = row - col + n;

            if (!columns.get(col) && !diag1.get(d1) && !diag2.get(d2)) {
                metrics.incrementNodesExplored();

                // Place queen
                solution[row] = col;
                columns.set(col);
                diag1.set(d1);
                diag2.set(d2);

                if (backtrack(row + 1, solution, columns, diag1, diag2, metrics)) {
                    return true;
                }

                // Remove queen (backtrack)
                columns.clear(col);
                diag1.clear(d1);
                diag2.clear(d2);
                metrics.incrementBacktracks();
            }
        }

        return false;
    }

    /**
     * Validates a solution.
     */
    public boolean validateSolution(int[] solution) {
        if (solution.length != n) return false;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                // Same column?
                if (solution[i] == solution[j]) return false;
                // Same diagonal?
                if (Math.abs(solution[i] - solution[j]) == Math.abs(i - j)) return false;
            }
        }
        return true;
    }

    /**
     * Quick benchmark method.
     */
    public static void main(String[] args) {
        int[] sizes = {8, 100, 500, 1000, 2000};

        for (int n : sizes) {
            NQueensOptimized solver = new NQueensOptimized(n);

            long start = System.currentTimeMillis();
            SolverResult<Integer> result = solver.solve();
            long elapsed = System.currentTimeMillis() - start;

            System.out.printf("%d-Queens: %s in %dms (nodes: %d, backtracks: %d)%n",
                    n,
                    result.isSatisfiable() ? "SOLVED" : "FAILED",
                    elapsed,
                    result.getMetrics().nodesExplored(),
                    result.getMetrics().backtracks());
        }
    }
}
