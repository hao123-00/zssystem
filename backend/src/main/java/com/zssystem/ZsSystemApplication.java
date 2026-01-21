package com.zssystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zssystem.mapper")
public class ZsSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZsSystemApplication.class, args);
    }
}
