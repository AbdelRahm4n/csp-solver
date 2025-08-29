package com.cspsolver.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProblemController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testListProblems() throws Exception {
        mockMvc.perform(get("/api/v1/problems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("nqueens")))
                .andExpect(jsonPath("$[1].name", is("sudoku")));
    }

    @Test
    void testSolveNQueens_4Queens() throws Exception {
        String request = """
                {
                    "n": 4
                }
                """;

        mockMvc.perform(post("/api/v1/problems/nqueens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SATISFIABLE")))
                .andExpect(jsonPath("$.satisfiable", is(true)))
                .andExpect(jsonPath("$.solutionCount", is(1)))
                .andExpect(jsonPath("$.solutions", hasSize(1)))
                .andExpect(jsonPath("$.metrics.elapsedTimeMs", greaterThanOrEqualTo(0)));
    }

    @Test
    void testSolveNQueens_8Queens() throws Exception {
        String request = """
                {
                    "n": 8
                }
                """;

        mockMvc.perform(post("/api/v1/problems/nqueens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SATISFIABLE")))
                .andExpect(jsonPath("$.satisfiable", is(true)))
                .andExpect(jsonPath("$.solutions[0]", aMapWithSize(8)));
    }

    @Test
    void testSolveNQueens_LargeN_UsesMinConflicts() throws Exception {
        // N >= 50 uses min-conflicts algorithm
        String request = """
                {
                    "n": 100
                }
                """;

        mockMvc.perform(post("/api/v1/problems/nqueens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SATISFIABLE")))
                .andExpect(jsonPath("$.satisfiable", is(true)))
                .andExpect(jsonPath("$.metrics.elapsedTimeMs", lessThan(1000)));
    }

    @Test
    void testSolveNQueens_InvalidN_TooSmall() throws Exception {
        String request = """
                {
                    "n": 0
                }
                """;

        mockMvc.perform(post("/api/v1/problems/nqueens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSolveNQueens_InvalidN_TooLarge() throws Exception {
        String request = """
                {
                    "n": 10001
                }
                """;

        mockMvc.perform(post("/api/v1/problems/nqueens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSolveNQueensQuick() throws Exception {
        mockMvc.perform(get("/api/v1/problems/nqueens/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SATISFIABLE")))
                .andExpect(jsonPath("$.satisfiable", is(true)));
    }

    @Test
    void testSolveSudoku_Easy() throws Exception {
        String request = """
                {
                    "grid": [
                        [5, 3, 0, 0, 7, 0, 0, 0, 0],
                        [6, 0, 0, 1, 9, 5, 0, 0, 0],
                        [0, 9, 8, 0, 0, 0, 0, 6, 0],
                        [8, 0, 0, 0, 6, 0, 0, 0, 3],
                        [4, 0, 0, 8, 0, 3, 0, 0, 1],
                        [7, 0, 0, 0, 2, 0, 0, 0, 6],
                        [0, 6, 0, 0, 0, 0, 2, 8, 0],
                        [0, 0, 0, 4, 1, 9, 0, 0, 5],
                        [0, 0, 0, 0, 8, 0, 0, 7, 9]
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/problems/sudoku")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SATISFIABLE")))
                .andExpect(jsonPath("$.satisfiable", is(true)))
                .andExpect(jsonPath("$.solutions[0].grid", hasSize(9)));
    }

    @Test
    void testSolveSudoku_MissingGrid() throws Exception {
        String request = """
                {
                    "timeoutMs": 60000
                }
                """;

        mockMvc.perform(post("/api/v1/problems/sudoku")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSolveNQueens_WithHeuristics() throws Exception {
        String request = """
                {
                    "n": 8,
                    "variableHeuristic": "DOM_WDEG",
                    "useAC3": true
                }
                """;

        mockMvc.perform(post("/api/v1/problems/nqueens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SATISFIABLE")));
    }

    @Test
    void testSolveNQueens_MultipleSolutions() throws Exception {
        String request = """
                {
                    "n": 8,
                    "maxSolutions": 5
                }
                """;

        mockMvc.perform(post("/api/v1/problems/nqueens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SATISFIABLE")))
                .andExpect(jsonPath("$.solutionCount", greaterThanOrEqualTo(1)));
    }
}
