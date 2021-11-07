package com.github.balchua.temporalgrpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcStarterService {
    /**
     * Main launches the server from the command line.
     */
    public static void main(String... args) {

        SpringApplication.run(GrpcStarterService.class);
    }
}
