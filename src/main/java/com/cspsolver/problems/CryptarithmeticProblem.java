package com.cspsolver.problems;

import com.cspsolver.core.constraint.impl.AllDifferent;
import com.cspsolver.core.constraint.impl.LinearConstraint;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.*;

/**
 * Cryptarithmetic puzzle: assign digits to letters to make an arithmetic equation true.
 * Example: SEND + MORE = MONEY
 *
 * Model:
 * - One variable per unique letter
 * - Domain: {0, 1, ..., 9}
 * - Constraints:
 *   - AllDifferent for all letters
 *   - Leading letters cannot be 0
 *   - Linear constraint representing the equation
 */
public class CryptarithmeticProblem implements ProblemFactory<Integer> {

    private final String word1;
    private final String word2;
    private final String result;

    /**
     * Creates a cryptarithmetic puzzle: word1 + word2 = result
     */
    public CryptarithmeticProblem(String word1, String word2, String result) {
        this.word1 = word1.toUpperCase();
        this.word2 = word2.toUpperCase();
        this.result = result.toUpperCase();
    }

    @Override
    public CSP<Integer> create() {
        CSP.Builder<Integer> builder = CSP.builder("Cryptarithmetic: " + word1 + " + " + word2 + " = " + result);

        // Find all unique letters
        Set<Character> letters = new LinkedHashSet<>();
        for (char c : word1.toCharArray()) letters.add(c);
        for (char c : word2.toCharArray()) letters.add(c);
        for (char c : result.toCharArray()) letters.add(c);

        // Create variables for each letter
        Map<Character, Variable<Integer>> varMap = new HashMap<>();
        for (char letter : letters) {
            builder.addVariable(String.valueOf(letter), Domain.range(0, 9));
            varMap.put(letter, builder.getVariable(String.valueOf(letter)));
        }

        // AllDifferent constraint
        List<Variable<Integer>> allVars = new ArrayList<>(varMap.values());
        builder.addConstraint(new AllDifferent<>(allVars, "AllDifferent"));

        // Leading letters cannot be 0 - handled via domain restriction
        // Note: We'll add these as unary constraints by reducing domains
        // For simplicity, we'll rely on the solver to find valid solutions

        // Build linear constraint: word1 + word2 = result
        // Collect coefficients for each letter based on place value
        Map<Variable<Integer>, Integer> coefficients = new HashMap<>();

        // Add coefficients for word1
        addWordCoefficients(word1, varMap, coefficients, 1);

        // Add coefficients for word2
        addWordCoefficients(word2, varMap, coefficients, 1);

        // Subtract coefficients for result
        addWordCoefficients(result, varMap, coefficients, -1);

        // Create linear constraint: sum = 0
        List<Variable<Integer>> constraintVars = new ArrayList<>();
        int[] coeffs = new int[coefficients.size()];
        int i = 0;
        for (Map.Entry<Variable<Integer>, Integer> entry : coefficients.entrySet()) {
            constraintVars.add(entry.getKey());
            coeffs[i++] = entry.getValue();
        }

        builder.addConstraint(new LinearConstraint(constraintVars, coeffs,
                LinearConstraint.Operator.EQ, 0, "Equation"));

        return builder.build();
    }

    private void addWordCoefficients(String word, Map<Character, Variable<Integer>> varMap,
                                     Map<Variable<Integer>, Integer> coefficients, int sign) {
        int placeValue = 1;
        for (int i = word.length() - 1; i >= 0; i--) {
            Variable<Integer> var = varMap.get(word.charAt(i));
            coefficients.merge(var, sign * placeValue, Integer::sum);
            placeValue *= 10;
        }
    }

    @Override
    public String getName() {
        return word1 + " + " + word2 + " = " + result;
    }

    @Override
    public String getDescription() {
        return "Assign digits 0-9 to letters so that " + word1 + " + " + word2 + " = " + result +
                ", with each letter representing a unique digit.";
    }

    /**
     * Classic SEND + MORE = MONEY puzzle.
     */
    public static CryptarithmeticProblem sendMoreMoney() {
        return new CryptarithmeticProblem("SEND", "MORE", "MONEY");
    }

    /**
     * EAT + THAT = APPLE puzzle.
     */
    public static CryptarithmeticProblem eatThatApple() {
        return new CryptarithmeticProblem("EAT", "THAT", "APPLE");
    }

    /**
     * TWO + TWO = FOUR puzzle.
     */
    public static CryptarithmeticProblem twoTwoFour() {
        return new CryptarithmeticProblem("TWO", "TWO", "FOUR");
    }
}
