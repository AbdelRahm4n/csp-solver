package com.cspsolver.problems;

import java.util.BitSet;

/**
 * Ultra-fast N-Queens solver for performance testing.
 * No object allocation in hot path.
 */
public class NQueensFast {

    public static int[] solve(int n) {
        int[] solution = new int[n];
        BitSet columns = new BitSet(n);
        BitSet diag1 = new BitSet(2 * n);
        BitSet diag2 = new BitSet(2 * n);

        if (backtrack(0, n, solution, columns, diag1, diag2)) {
            return solution;
        }
        return null;
    }

    private static boolean backtrack(int row, int n, int[] solution,
                                      BitSet columns, BitSet diag1, BitSet diag2) {
        if (row == n) {
            return true;
        }

        for (int col = 0; col < n; col++) {
            int d1 = row + col;
            int d2 = row - col + n;

            if (!columns.get(col) && !diag1.get(d1) && !diag2.get(d2)) {
                solution[row] = col;
                columns.set(col);
                diag1.set(d1);
                diag2.set(d2);

                if (backtrack(row + 1, n, solution, columns, diag1, diag2)) {
                    return true;
                }

                columns.clear(col);
                diag1.clear(d1);
                diag2.clear(d2);
            }
        }
        return false;
    }

    public static void main(String[] args) {
        // Warmup
        for (int i = 0; i < 10; i++) {
            solve(20);
        }

        int[] sizes = {8, 50, 100, 200, 500, 1000, 2000};

        for (int n : sizes) {
            long start = System.nanoTime();
            int[] result = solve(n);
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("%d-Queens: %s in %dms%n",
                    n, result != null ? "SOLVED" : "FAILED", elapsed);
        }
    }
}
