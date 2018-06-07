package com.javi.bowling.controller;

import com.javi.bowling.model.DatabaseUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        DatabaseUtil.initDB();
    }
}
