package com.cspsolver.problems;

import com.cspsolver.core.constraint.impl.AllDifferent;
import com.cspsolver.core.model.Assignment;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Sudoku puzzle: fill a 9x9 grid with digits 1-9 such that each row,
 * column, and 3x3 box contains each digit exactly once.
 *
 * Model:
 * - 81 variables: C00, C01, ..., C88 (row, column)
 * - Domain for empty cells: {1, 2, ..., 9}
 * - Domain for pre-filled cells: singleton with the given value
 * - Constraints:
 *   - 9 row AllDifferent constraints
 *   - 9 column AllDifferent constraints
 *   - 9 box AllDifferent constraints
 */
public class SudokuProblem implements ProblemFactory<Integer> {

    private final int[][] initial;

    /**
     * Creates a Sudoku problem from an initial grid.
     * Use 0 for empty cells.
     *
     * @param initial 9x9 grid with initial values (0 = empty)
     */
    public SudokuProblem(int[][] initial) {
        if (initial.length != 9) {
            throw new IllegalArgumentException("Grid must be 9x9");
        }
        for (int[] row : initial) {
            if (row.length != 9) {
                throw new IllegalArgumentException("Grid must be 9x9");
            }
        }
        this.initial = new int[9][9];
        for (int r = 0; r < 9; r++) {
            System.arraycopy(initial[r], 0, this.initial[r], 0, 9);
        }
    }

    /**
     * Creates an empty Sudoku puzzle.
     */
    public SudokuProblem() {
        this.initial = new int[9][9];
    }

    /**
     * Creates a Sudoku problem from a string representation.
     * Use '.' or '0' for empty cells, '1'-'9' for values.
     *
     * @param puzzle 81-character string or multi-line grid
     */
    public static SudokuProblem fromString(String puzzle) {
        String cleaned = puzzle.replaceAll("[^0-9.]", "");
        if (cleaned.length() != 81) {
            throw new IllegalArgumentException("Puzzle must have exactly 81 cells");
        }

        int[][] grid = new int[9][9];
        for (int i = 0; i < 81; i++) {
            char c = cleaned.charAt(i);
            grid[i / 9][i % 9] = (c == '.') ? 0 : (c - '0');
        }

        return new SudokuProblem(grid);
    }

    @Override
    public CSP<Integer> create() {
        CSP.Builder<Integer> builder = CSP.builder("Sudoku");
        Variable<Integer>[][] cells = new Variable[9][9];

        // Create variables
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                String name = "C" + r + c;
                Domain<Integer> domain;
                if (initial[r][c] != 0) {
                    // Pre-filled cell
                    domain = Domain.singleton(initial[r][c]);
                } else {
                    // Empty cell
                    domain = Domain.range(1, 9);
                }
                builder.addVariable(name, domain);
                cells[r][c] = builder.getVariable(name);
            }
        }

        // Row constraints
        for (int r = 0; r < 9; r++) {
            List<Variable<Integer>> row = new ArrayList<>(9);
            for (int c = 0; c < 9; c++) {
                row.add(cells[r][c]);
            }
            builder.addConstraint(new AllDifferent<>(row, "Row" + r));
        }

        // Column constraints
        for (int c = 0; c < 9; c++) {
            List<Variable<Integer>> col = new ArrayList<>(9);
            for (int r = 0; r < 9; r++) {
                col.add(cells[r][c]);
            }
            builder.addConstraint(new AllDifferent<>(col, "Col" + c));
        }

        // Box constraints (3x3 blocks)
        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                List<Variable<Integer>> box = new ArrayList<>(9);
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        box.add(cells[boxRow * 3 + r][boxCol * 3 + c]);
                    }
                }
                builder.addConstraint(new AllDifferent<>(box, "Box" + boxRow + boxCol));
            }
        }

        return builder.build();
    }

    @Override
    public String getName() {
        return "Sudoku";
    }

    @Override
    public String getDescription() {
        return "Fill the 9x9 grid so each row, column, and 3x3 box contains 1-9 exactly once.";
    }

    /**
     * Formats a solution as a visual grid.
     */
    public String formatSolution(Assignment<Integer> solution, CSP<Integer> csp) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sudoku Solution:\n");
        sb.append("+-------+-------+-------+\n");

        for (int r = 0; r < 9; r++) {
            sb.append("| ");
            for (int c = 0; c < 9; c++) {
                Variable<Integer> var = csp.getVariable("C" + r + c);
                Integer value = solution.getValue(var);
                sb.append(value != null ? value : ".");
                sb.append(" ");
                if (c % 3 == 2) {
                    sb.append("| ");
                }
            }
            sb.append("\n");
            if (r % 3 == 2) {
                sb.append("+-------+-------+-------+\n");
            }
        }

        return sb.toString();
    }

    /**
     * Returns the initial grid.
     */
    public int[][] getInitial() {
        int[][] copy = new int[9][9];
        for (int r = 0; r < 9; r++) {
            System.arraycopy(initial[r], 0, copy[r], 0, 9);
        }
        return copy;
    }

    /**
     * Returns some example Sudoku puzzles.
     */
    public static class Examples {

        public static SudokuProblem easy() {
            return SudokuProblem.fromString(
                    "530070000" +
                    "600195000" +
                    "098000060" +
                    "800060003" +
                    "400803001" +
                    "700020006" +
                    "060000280" +
                    "000419005" +
                    "000080079"
            );
        }

        public static SudokuProblem medium() {
            return SudokuProblem.fromString(
                    "000000680" +
                    "030080000" +
                    "900007253" +
                    "004000000" +
                    "200500009" +
                    "001074080" +
                    "070001004" +
                    "500040000" +
                    "060000017"
            );
        }

        public static SudokuProblem hard() {
            return SudokuProblem.fromString(
                    "800000000" +
                    "003600000" +
                    "070090200" +
                    "050007000" +
                    "000045700" +
                    "000100030" +
                    "001000068" +
                    "008500010" +
                    "090000400"
            );
        }
    }
}
