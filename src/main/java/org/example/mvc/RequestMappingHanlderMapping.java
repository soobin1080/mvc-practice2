package org.example.mvc;

import org.example.mvc.controller.Controller;
import org.example.mvc.controller.HomeController;

import java.util.HashMap;
import java.util.Map;

public class RequestMappingHanlderMapping {
    private Map<String, Controller> mapping = new HashMap<>();

    void init(){
        mapping.put("/",new HomeController());
    }

    public Controller findHanlder(String uriPath){
        return mapping.get(uriPath);
    }
}
