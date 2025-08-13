package com.cspsolver.problems;

import com.cspsolver.core.constraint.impl.NotEqual;
import com.cspsolver.core.model.CSP;
import com.cspsolver.core.model.Domain;
import com.cspsolver.core.model.Variable;

import java.util.*;

/**
 * Map Coloring problem: assign colors to regions of a map such that
 * no two adjacent regions have the same color.
 *
 * This is a specific application of graph coloring with geographic context.
 */
public class MapColoringProblem implements ProblemFactory<String> {

    private final Map<String, List<String>> adjacency;
    private final List<String> colors;
    private final String name;

    /**
     * Creates a map coloring problem.
     *
     * @param adjacency map from region name to list of adjacent region names
     * @param colors    list of available color names
     */
    public MapColoringProblem(Map<String, List<String>> adjacency, List<String> colors, String name) {
        this.adjacency = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : adjacency.entrySet()) {
            this.adjacency.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        this.colors = new ArrayList<>(colors);
        this.name = name;
    }

    @Override
    public CSP<String> create() {
        CSP.Builder<String> builder = CSP.builder(name);

        // Create variables for each region
        Domain<String> colorDomain = new Domain<>(colors);
        for (String region : adjacency.keySet()) {
            builder.addVariable(region, colorDomain.copy());
        }

        // Add adjacency constraints
        Set<String> addedConstraints = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : adjacency.entrySet()) {
            String region = entry.getKey();
            for (String neighbor : entry.getValue()) {
                // Avoid adding duplicate constraints
                String key = region.compareTo(neighbor) < 0 ?
                        region + "-" + neighbor : neighbor + "-" + region;
                if (!addedConstraints.contains(key)) {
                    Variable<String> v1 = builder.getVariable(region);
                    Variable<String> v2 = builder.getVariable(neighbor);
                    builder.addConstraint(new NotEqual<>(v1, v2));
                    addedConstraints.add(key);
                }
            }
        }

        return builder.build();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "Color " + adjacency.size() + " regions with " + colors.size() +
                " colors such that no adjacent regions have the same color.";
    }

    /**
     * Classic Australia map coloring problem.
     */
    public static MapColoringProblem australia() {
        Map<String, List<String>> adjacency = new LinkedHashMap<>();
        adjacency.put("WA", Arrays.asList("NT", "SA"));
        adjacency.put("NT", Arrays.asList("WA", "SA", "Q"));
        adjacency.put("SA", Arrays.asList("WA", "NT", "Q", "NSW", "V"));
        adjacency.put("Q", Arrays.asList("NT", "SA", "NSW"));
        adjacency.put("NSW", Arrays.asList("Q", "SA", "V"));
        adjacency.put("V", Arrays.asList("SA", "NSW"));
        adjacency.put("T", Collections.emptyList());  // Tasmania (not adjacent to mainland)

        List<String> colors = Arrays.asList("Red", "Green", "Blue");
        return new MapColoringProblem(adjacency, colors, "Australia");
    }

    /**
     * US map coloring (simplified - 48 contiguous states).
     * Note: This is a subset; full US map would be more complex.
     */
    public static MapColoringProblem usaSample() {
        Map<String, List<String>> adjacency = new LinkedHashMap<>();

        // West Coast and neighbors
        adjacency.put("WA", Arrays.asList("OR", "ID"));
        adjacency.put("OR", Arrays.asList("WA", "ID", "NV", "CA"));
        adjacency.put("CA", Arrays.asList("OR", "NV", "AZ"));
        adjacency.put("NV", Arrays.asList("OR", "CA", "AZ", "UT", "ID"));
        adjacency.put("ID", Arrays.asList("WA", "OR", "NV", "UT", "WY", "MT"));
        adjacency.put("AZ", Arrays.asList("CA", "NV", "UT", "NM"));
        adjacency.put("UT", Arrays.asList("ID", "NV", "AZ", "CO", "WY"));
        adjacency.put("MT", Arrays.asList("ID", "WY", "ND", "SD"));
        adjacency.put("WY", Arrays.asList("MT", "ID", "UT", "CO", "NE", "SD"));
        adjacency.put("CO", Arrays.asList("WY", "UT", "NM", "KS", "NE", "OK"));
        adjacency.put("NM", Arrays.asList("AZ", "CO", "TX", "OK"));

        List<String> colors = Arrays.asList("Red", "Green", "Blue", "Yellow");
        return new MapColoringProblem(adjacency, colors, "USA-Sample");
    }

    /**
     * Simple Europe subset (some countries).
     */
    public static MapColoringProblem europeSample() {
        Map<String, List<String>> adjacency = new LinkedHashMap<>();

        adjacency.put("France", Arrays.asList("Spain", "Belgium", "Germany", "Switzerland", "Italy"));
        adjacency.put("Spain", Arrays.asList("France", "Portugal"));
        adjacency.put("Portugal", Arrays.asList("Spain"));
        adjacency.put("Belgium", Arrays.asList("France", "Germany", "Netherlands"));
        adjacency.put("Netherlands", Arrays.asList("Belgium", "Germany"));
        adjacency.put("Germany", Arrays.asList("France", "Belgium", "Netherlands", "Poland", "Austria", "Switzerland"));
        adjacency.put("Switzerland", Arrays.asList("France", "Germany", "Austria", "Italy"));
        adjacency.put("Italy", Arrays.asList("France", "Switzerland", "Austria"));
        adjacency.put("Austria", Arrays.asList("Germany", "Switzerland", "Italy", "Poland"));
        adjacency.put("Poland", Arrays.asList("Germany", "Austria"));

        List<String> colors = Arrays.asList("Red", "Green", "Blue", "Yellow");
        return new MapColoringProblem(adjacency, colors, "Europe-Sample");
    }

    public Map<String, List<String>> getAdjacency() {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : adjacency.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    public List<String> getColors() {
        return Collections.unmodifiableList(colors);
    }
}
