package com.cspsolver.problems;

import com.cspsolver.core.constraint.BinaryConstraint;
import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;
import com.cspsolver.core.constraint.impl.NotEqual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * N-Queens problem: place N queens on an NxN chessboard such that
 * no two queens attack each other (no two queens on same row, column, or diagonal).
 *
 * Model:
 * - N variables: Q0, Q1, ..., Q(N-1), one per row
 * - Domain for each Qi: {0, 1, ..., N-1} representing column position
 * - Constraints: for each pair (Qi, Qj) where i < j:
 *   - Qi != Qj (different columns)
 *   - |Qi - Qj| != |i - j| (different diagonals)
 */
public class NQueensProblem implements ProblemFactory<Integer> {

    private final int n;

    public NQueensProblem(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("N must be at least 1");
        }
        this.n = n;
    }

    @Override
    public CSP<Integer> create() {
        CSP.Builder<Integer> builder = CSP.builder("N-Queens-" + n);

        // Create variables: one per row
        List<Variable<Integer>> queens = new ArrayList<>(n);
        for (int row = 0; row < n; row++) {
            Domain<Integer> domain = Domain.range(0, n - 1);
            builder.addVariable("Q" + row, domain);
        }

        // Get the variables back for constraint creation
        for (int row = 0; row < n; row++) {
            queens.add(builder.getVariable("Q" + row));
        }

        // Add constraints for each pair of queens
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Variable<Integer> qi = queens.get(i);
                Variable<Integer> qj = queens.get(j);
                int rowDiff = j - i;

                // Different columns
                builder.addConstraint(new NotEqual<>(qi, qj));

                // Different diagonals (optimized constraint)
                builder.addConstraint(new NQueensDiagonalConstraint(qi, qj, rowDiff));
            }
        }

        return builder.build();
    }

    @Override
    public String getName() {
        return n + "-Queens";
    }

    @Override
    public String getDescription() {
        return "Place " + n + " queens on a " + n + "x" + n +
                " chessboard such that no two queens attack each other.";
    }

    /**
     * Returns the board size.
     */
    public int getN() {
        return n;
    }

    /**
     * Validates that a solution is correct.
     */
    public boolean validateSolution(Assignment<Integer> solution, List<Variable<Integer>> variables) {
        int[] columns = new int[n];

        // Extract column positions
        for (int row = 0; row < n; row++) {
            Integer col = solution.getValue(variables.get(row));
            if (col == null || col < 0 || col >= n) {
                return false;
            }
            columns[row] = col;
        }

        // Check all pairs
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                // Same column?
                if (columns[i] == columns[j]) {
                    return false;
                }
                // Same diagonal?
                if (Math.abs(columns[i] - columns[j]) == Math.abs(i - j)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Formats a solution as a visual board.
     */
    public String formatSolution(Assignment<Integer> solution, List<Variable<Integer>> variables) {
        StringBuilder sb = new StringBuilder();
        sb.append(n).append("-Queens Solution:\n");

        for (int row = 0; row < n; row++) {
            Integer col = solution.getValue(variables.get(row));
            for (int c = 0; c < n; c++) {
                sb.append(c == col ? "Q " : ". ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Optimized diagonal constraint for N-Queens.
     * Checks that |col1 - col2| != rowDiff.
     */
    public static class NQueensDiagonalConstraint extends BinaryConstraint<Integer> {

        private final int rowDiff;

        public NQueensDiagonalConstraint(Variable<Integer> var1, Variable<Integer> var2, int rowDiff) {
            super(var1, var2);
            this.rowDiff = rowDiff;
        }

        @Override
        protected boolean check(Integer col1, Integer col2) {
            return Math.abs(col1 - col2) != rowDiff;
        }

        @Override
        public boolean revise(Variable<Integer> x, Variable<Integer> y,
                              Map<Variable<Integer>, Domain<Integer>> domains) {
            Domain<Integer> dx = domains.get(x);
            Domain<Integer> dy = domains.get(y);

            // Optimization: if y's domain is a singleton, we can prune efficiently
            if (dy.isSingleton()) {
                int yCol = dy.getFirst();
                boolean changed = false;

                // Remove values that would place queens on same diagonal
                int diag1 = yCol - rowDiff;
                int diag2 = yCol + rowDiff;

                if (x.equals(var1)) {
                    // x is the earlier row, y is later
                    if (dx.remove(diag1)) changed = true;
                    if (dx.remove(diag2)) changed = true;
                } else {
                    // x is the later row, y is earlier
                    if (dx.remove(diag1)) changed = true;
                    if (dx.remove(diag2)) changed = true;
                }

                return changed;
            }

            return false;
        }

        @Override
        public String getName() {
            return var1.getName() + " diag " + var2.getName();
        }
    }
}
