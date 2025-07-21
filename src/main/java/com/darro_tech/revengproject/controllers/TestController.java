package com.darro_tech.revengproject.controllers;

import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Test Controller for debugging and testing features
 */
@Controller
public class TestController {

    private static final Logger logger = Logger.getLogger(TestController.class.getName());

    /**
     * Test page for company selector flash fix
     */
    @GetMapping("/test-company-selector")
    public String testCompanySelector(Model model) {
        logger.info("🧪 Loading company selector test page");

        model.addAttribute("pageTitle", "Company Selector Test");
        model.addAttribute("content", "test-company-selector");

        return "test-company-selector";
    }
}
