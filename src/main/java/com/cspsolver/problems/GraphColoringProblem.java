package com.cspsolver.problems;

import com.cspsolver.core.constraint.impl.NotEqual;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.*;

/**
 * Graph Coloring problem: assign colors to nodes such that no two
 * adjacent nodes have the same color.
 *
 * Model:
 * - N variables: one per node
 * - Domain for each variable: {0, 1, ..., k-1} representing colors
 * - Constraints: for each edge (u, v): u != v
 */
public class GraphColoringProblem implements ProblemFactory<Integer> {

    private final List<String> nodes;
    private final List<int[]> edges;  // Pairs of node indices
    private final int numColors;
    private final String name;

    /**
     * Creates a graph coloring problem.
     *
     * @param nodes     list of node names
     * @param edges     list of edges as pairs of node indices
     * @param numColors number of available colors
     */
    public GraphColoringProblem(List<String> nodes, List<int[]> edges, int numColors) {
        this(nodes, edges, numColors, "GraphColoring");
    }

    public GraphColoringProblem(List<String> nodes, List<int[]> edges, int numColors, String name) {
        this.nodes = new ArrayList<>(nodes);
        this.edges = new ArrayList<>();
        for (int[] edge : edges) {
            this.edges.add(edge.clone());
        }
        this.numColors = numColors;
        this.name = name;
    }

    @Override
    public CSP<Integer> create() {
        CSP.Builder<Integer> builder = CSP.builder(name);

        // Create variables
        for (String node : nodes) {
            builder.addVariable(node, Domain.range(0, numColors - 1));
        }

        // Add edge constraints
        for (int[] edge : edges) {
            Variable<Integer> u = builder.getVariable(nodes.get(edge[0]));
            Variable<Integer> v = builder.getVariable(nodes.get(edge[1]));
            builder.addConstraint(new NotEqual<>(u, v));
        }

        return builder.build();
    }

    @Override
    public String getName() {
        return name + " (" + nodes.size() + " nodes, " + edges.size() + " edges, " + numColors + " colors)";
    }

    @Override
    public String getDescription() {
        return "Color " + nodes.size() + " nodes with " + numColors +
                " colors such that no adjacent nodes have the same color.";
    }

    /**
     * Creates a random graph coloring problem.
     */
    public static GraphColoringProblem random(int numNodes, double edgeProbability, int numColors) {
        return random(numNodes, edgeProbability, numColors, new Random());
    }

    public static GraphColoringProblem random(int numNodes, double edgeProbability, int numColors, Random random) {
        List<String> nodes = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            nodes.add("N" + i);
        }

        List<int[]> edges = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                if (random.nextDouble() < edgeProbability) {
                    edges.add(new int[]{i, j});
                }
            }
        }

        return new GraphColoringProblem(nodes, edges, numColors, "RandomGraph");
    }

    /**
     * Creates a Petersen graph (classic 3-colorable graph).
     */
    public static GraphColoringProblem petersen() {
        List<String> nodes = Arrays.asList(
                "O0", "O1", "O2", "O3", "O4",  // Outer pentagon
                "I0", "I1", "I2", "I3", "I4"   // Inner pentagram
        );

        List<int[]> edges = Arrays.asList(
                // Outer pentagon
                new int[]{0, 1}, new int[]{1, 2}, new int[]{2, 3}, new int[]{3, 4}, new int[]{4, 0},
                // Inner pentagram
                new int[]{5, 7}, new int[]{7, 9}, new int[]{9, 6}, new int[]{6, 8}, new int[]{8, 5},
                // Spokes
                new int[]{0, 5}, new int[]{1, 6}, new int[]{2, 7}, new int[]{3, 8}, new int[]{4, 9}
        );

        return new GraphColoringProblem(nodes, edges, 3, "Petersen");
    }

    /**
     * Creates a complete graph K_n.
     */
    public static GraphColoringProblem complete(int n) {
        List<String> nodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            nodes.add("N" + i);
        }

        List<int[]> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                edges.add(new int[]{i, j});
            }
        }

        // K_n requires n colors
        return new GraphColoringProblem(nodes, edges, n, "K" + n);
    }

    /**
     * Creates a cycle graph C_n.
     */
    public static GraphColoringProblem cycle(int n) {
        List<String> nodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            nodes.add("N" + i);
        }

        List<int[]> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            edges.add(new int[]{i, (i + 1) % n});
        }

        // Cycle requires 2 colors if n is even, 3 if odd
        int colors = n % 2 == 0 ? 2 : 3;
        return new GraphColoringProblem(nodes, edges, colors, "C" + n);
    }

    public List<String> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<int[]> getEdges() {
        List<int[]> copy = new ArrayList<>();
        for (int[] edge : edges) {
            copy.add(edge.clone());
        }
        return copy;
    }

    public int getNumColors() {
        return numColors;
    }
}
