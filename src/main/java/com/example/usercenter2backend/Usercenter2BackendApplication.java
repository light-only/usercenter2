package com.example.usercenter2backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan("com.example.usercenter2backend.mapper")
@SpringBootApplication
public class Usercenter2BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Usercenter2BackendApplication.class, args);
    }

}
