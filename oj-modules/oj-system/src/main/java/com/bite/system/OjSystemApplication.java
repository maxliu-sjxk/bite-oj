package com.bite.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * localhost:9201/swagger-ui/index.html
 */
@MapperScan("com.bite.system.mapper")
@SpringBootApplication
public class OjSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(OjSystemApplication.class, args);
    }
}
