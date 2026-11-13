package com.quantaida.mc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@MapperScan("com.quantaida.mc.mapper")
public class McApplication {
    public static void main(String[] args) {
        SpringApplication.run(McApplication.class, args);
    }
}
