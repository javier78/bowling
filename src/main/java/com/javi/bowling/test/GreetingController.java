package com.javi.bowling.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.javi.bowling.db.DatabaseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@RestController
public class GreetingController {
    @RequestMapping(value = "/greeting", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity greeting(@RequestParam(value="name", defaultValue="World") String name) {
        Gson gson = new GsonBuilder().create();
        Connection conn = DatabaseUtil.connect();
        String json = gson.toJson(new Greeting(1, name));
        String sql = "CREATE TABLE IF NOT EXISTS warehouses (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	name text NOT NULL,\n"
                + "	capacity real\n"
                + ");";
        try {
            Statement statement = conn.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(json);
    }
}
