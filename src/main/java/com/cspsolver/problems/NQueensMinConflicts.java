package com.cspsolver.problems;

import java.util.Random;

/**
 * Min-Conflicts solver for N-Queens.
 * Uses local search with random initialization - can solve millions of queens in seconds.
 */
public class NQueensMinConflicts {

    private final int n;
    private final int[] queens;      // queens[row] = column
    private final int[] rowConflicts; // conflicts per row
    private final int[] colCounts;   // queens per column
    private final int[] diag1Counts; // queens per / diagonal (row + col)
    private final int[] diag2Counts; // queens per \ diagonal (row - col + n - 1)
    private final Random random;
    private int totalConflicts;

    public NQueensMinConflicts(int n) {
        this.n = n;
        this.queens = new int[n];
        this.rowConflicts = new int[n];
        this.colCounts = new int[n];
        this.diag1Counts = new int[2 * n - 1];
        this.diag2Counts = new int[2 * n - 1];
        this.random = new Random(42);
    }

    public int[] solve(int maxIterations) {
        // Greedy initialization: place each queen in min-conflict column
        initializeGreedy();

        if (totalConflicts == 0) {
            return queens.clone();
        }

        // Min-conflicts iterations
        for (int iter = 0; iter < maxIterations; iter++) {
            // Pick a random conflicted row
            int row = pickConflictedRow();
            if (row == -1) {
                return queens.clone(); // Solved!
            }

            // Move queen to min-conflict column
            int bestCol = findMinConflictColumn(row);
            if (bestCol != queens[row]) {
                moveQueen(row, bestCol);
            }

            if (totalConflicts == 0) {
                return queens.clone();
            }
        }

        return null; // Failed to find solution
    }

    private void initializeGreedy() {
        // Reset counts
        java.util.Arrays.fill(colCounts, 0);
        java.util.Arrays.fill(diag1Counts, 0);
        java.util.Arrays.fill(diag2Counts, 0);
        totalConflicts = 0;

        for (int row = 0; row < n; row++) {
            int bestCol = 0;
            int minConflicts = Integer.MAX_VALUE;

            // Find column with minimum conflicts
            for (int col = 0; col < n; col++) {
                int conflicts = colCounts[col] +
                               diag1Counts[row + col] +
                               diag2Counts[row - col + n - 1];
                if (conflicts < minConflicts ||
                    (conflicts == minConflicts && random.nextBoolean())) {
                    minConflicts = conflicts;
                    bestCol = col;
                }
            }

            queens[row] = bestCol;
            colCounts[bestCol]++;
            diag1Counts[row + bestCol]++;
            diag2Counts[row - bestCol + n - 1]++;
        }

        // Calculate conflicts for each row
        for (int row = 0; row < n; row++) {
            int col = queens[row];
            // Conflicts = other queens in same col/diag (subtract 1 for self)
            int conflicts = (colCounts[col] - 1) +
                           (diag1Counts[row + col] - 1) +
                           (diag2Counts[row - col + n - 1] - 1);
            rowConflicts[row] = conflicts;
            totalConflicts += conflicts;
        }
        totalConflicts /= 2; // Each conflict counted twice
    }

    private int pickConflictedRow() {
        // Collect conflicted rows and pick randomly
        int count = 0;
        for (int row = 0; row < n; row++) {
            if (rowConflicts[row] > 0) count++;
        }
        if (count == 0) return -1;

        int target = random.nextInt(count);
        for (int row = 0; row < n; row++) {
            if (rowConflicts[row] > 0) {
                if (target == 0) return row;
                target--;
            }
        }
        return -1;
    }

    private int findMinConflictColumn(int row) {
        int oldCol = queens[row];
        int bestCol = oldCol;
        int minConflicts = Integer.MAX_VALUE;

        for (int col = 0; col < n; col++) {
            // Calculate conflicts if we move to this column
            int conflicts = colCounts[col] +
                           diag1Counts[row + col] +
                           diag2Counts[row - col + n - 1];
            // Subtract self if staying in same position
            if (col == oldCol) {
                conflicts -= 3; // Remove self from col, diag1, diag2
            }

            if (conflicts < minConflicts ||
                (conflicts == minConflicts && random.nextBoolean())) {
                minConflicts = conflicts;
                bestCol = col;
            }
        }

        return bestCol;
    }

    private void moveQueen(int row, int newCol) {
        int oldCol = queens[row];

        // Remove from old position
        colCounts[oldCol]--;
        diag1Counts[row + oldCol]--;
        diag2Counts[row - oldCol + n - 1]--;

        // Add to new position
        queens[row] = newCol;
        colCounts[newCol]++;
        diag1Counts[row + newCol]++;
        diag2Counts[row - newCol + n - 1]++;

        // Update conflicts for all affected rows
        updateConflicts();
    }

    private void updateConflicts() {
        totalConflicts = 0;
        for (int row = 0; row < n; row++) {
            int col = queens[row];
            int conflicts = (colCounts[col] - 1) +
                           (diag1Counts[row + col] - 1) +
                           (diag2Counts[row - col + n - 1] - 1);
            rowConflicts[row] = conflicts;
            totalConflicts += conflicts;
        }
        totalConflicts /= 2;
    }

    public static void main(String[] args) {
        int[] sizes = {8, 100, 500, 1000, 2000, 5000, 10000};

        for (int n : sizes) {
            NQueensMinConflicts solver = new NQueensMinConflicts(n);

            long start = System.nanoTime();
            int[] result = solver.solve(n * 10); // Max iterations
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            if (result != null) {
                System.out.printf("%d-Queens: SOLVED in %dms%n", n, elapsed);
            } else {
                System.out.printf("%d-Queens: FAILED after %dms%n", n, elapsed);
            }
        }
    }
}
