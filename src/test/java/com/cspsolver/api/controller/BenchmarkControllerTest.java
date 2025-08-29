package com.cspsolver.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BenchmarkController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BenchmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testBenchmarkNQueens_DefaultSizes() throws Exception {
        mockMvc.perform(get("/api/v1/benchmark/nqueens")
                        .param("sizes", "4,8")
                        .param("runs", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problem", is("N-Queens")))
                .andExpect(jsonPath("$.runs", is(1)))
                .andExpect(jsonPath("$.results", hasSize(2)))
                .andExpect(jsonPath("$.results[0].n", is(4)))
                .andExpect(jsonPath("$.results[0].solved", is(true)))
                .andExpect(jsonPath("$.results[1].n", is(8)))
                .andExpect(jsonPath("$.results[1].solved", is(true)));
    }

    @Test
    void testBenchmarkNQueens_SpecificSize() throws Exception {
        mockMvc.perform(get("/api/v1/benchmark/nqueens/8")
                        .param("runs", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.n", is(8)))
                .andExpect(jsonPath("$.solved", is(true)))
                .andExpect(jsonPath("$.runs", is(2)))
                .andExpect(jsonPath("$.avgTimeMs", greaterThanOrEqualTo(0.0)))
                .andExpect(jsonPath("$.avgNodes", greaterThan(0.0)))
                .andExpect(jsonPath("$.avgBacktracks", greaterThanOrEqualTo(0.0)));
    }

    @Test
    void testCompareHeuristics() throws Exception {
        mockMvc.perform(get("/api/v1/benchmark/compare-heuristics")
                        .param("n", "8")
                        .param("runs", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problem", is("8-Queens")))
                .andExpect(jsonPath("$.runs", is(1)))
                .andExpect(jsonPath("$.heuristics", notNullValue()))
                .andExpect(jsonPath("$.heuristics.MRV_DEGREE", notNullValue()));
    }

    @Test
    void testBenchmarkNQueens_IncludesConfiguration() throws Exception {
        mockMvc.perform(get("/api/v1/benchmark/nqueens")
                        .param("sizes", "4")
                        .param("runs", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configuration.variableHeuristic", is("MRV+Degree")))
                .andExpect(jsonPath("$.configuration.propagation", is("Forward Checking")))
                .andExpect(jsonPath("$.configuration.ac3Preprocessing", is(true)));
    }

    @Test
    void testBenchmarkNQueens_SingleSize() throws Exception {
        mockMvc.perform(get("/api/v1/benchmark/nqueens/4")
                        .param("runs", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.n", is(4)))
                .andExpect(jsonPath("$.solved", is(true)))
                .andExpect(jsonPath("$.minTimeMs", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.maxTimeMs", greaterThanOrEqualTo(0)));
    }
}
