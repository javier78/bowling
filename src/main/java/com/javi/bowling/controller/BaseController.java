package com.javi.bowling.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class BaseController {
    protected ResponseEntity generateErrorResponse(String message) {
        Gson gson = new GsonBuilder().create();
        JsonObject object = new JsonObject();
        object.addProperty("error_message", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(gson.toJson(object));
    }
}
