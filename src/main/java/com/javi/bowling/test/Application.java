package com.javi.bowling.test;

import com.javi.bowling.db.DatabaseUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        Connection conn = DatabaseUtil.connect();
        try {
            Statement statement = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
