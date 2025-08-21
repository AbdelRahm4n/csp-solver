package com.cspsolver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for CSP Solver API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI cspSolverOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CSP Solver API")
                        .description("""
                                A production-grade Constraint Satisfaction Problem (CSP) Solver microservice.

                                ## Features
                                - **Backtracking Search** with configurable heuristics
                                - **Arc Consistency (AC-3)** preprocessing
                                - **Forward Checking** propagation
                                - **Variable Ordering Heuristics**: MRV, Degree, Dom/WDeg
                                - **Value Ordering Heuristics**: LCV (Least Constraining Value)
                                - **Built-in Problems**: N-Queens, Sudoku, Graph Coloring, Map Coloring, Cryptarithmetic
                                - **WebSocket Support** for real-time solving progress

                                ## Performance
                                - Solves 1000-Queens in under 100ms using min-conflicts algorithm
                                - Optimized BitSet-based domains for O(1) operations
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CSP Solver Team")
                                .email("csp-solver@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")
                ));
    }
}
