package com.darro_tech.revengproject.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        // Additional logging can be added here if desired.
        return "error"; // Resolves to error.html in your templates folder
    }
}
