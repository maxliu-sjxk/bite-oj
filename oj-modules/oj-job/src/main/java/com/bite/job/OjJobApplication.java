package com.bite.job;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.bite.job.mapper")
@SpringBootApplication
public class OjJobApplication {
    public static void main(String[] args) {
        SpringApplication.run(OjJobApplication.class, args);
    }
}
