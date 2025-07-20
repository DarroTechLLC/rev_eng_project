package com.darro_tech.revengproject.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for enhanced chart export functionality Demonstrates the enhanced
 * chart export features matching Next.js implementation
 */
@Controller
public class EnhancedChartController {

    private static final Logger log = LoggerFactory.getLogger(EnhancedChartController.class);

    /**
     * Enhanced chart export example page Shows how to use the enhanced export
     * functionality
     */
        @GetMapping("/enhanced-chart-example")
    public String enhancedChartExample(Model model) {
        log.info("📊 Serving enhanced chart export example page");
        
        // Add any necessary model attributes
        model.addAttribute("pageTitle", "Enhanced Chart Export Example");
        
        return "content/dashboard/enhanced-chart-example";
    }

    /**
     * Chart export error test page
     * Tests the error handling fixes
     */
    @GetMapping("/chart-export-test")
    public String chartExportTest(Model model) {
        log.info("🧪 Serving chart export error test page");
        
        // Add any necessary model attributes
        model.addAttribute("pageTitle", "Chart Export Error Test");
        
        return "content/dashboard/chart-export-test";
    }
}
