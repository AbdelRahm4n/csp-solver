package com.cspsolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CspSolverApplication {

    public static void main(String[] args) {
        SpringApplication.run(CspSolverApplication.class, args);
    }
}
