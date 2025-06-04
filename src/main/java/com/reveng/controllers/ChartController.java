package com.reveng.controllers;

import com.reveng.services.ChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class ChartController {
    private static final Logger logger = LoggerFactory.getLogger(ChartController.class);
    
    @Autowired
    private ChartService chartService;
    
    @GetMapping("/volume-chart")
    public String showVolumeChart(Model model) {
        logger.info("📊 Rendering volume chart page");
        
        try {
            // Get chart data from service
            Map<String, Object> chartData = chartService.getDailyVolumeData();
            
            // Add data to model
            model.addAttribute("chartLabels", chartData.get("labels"));
            model.addAttribute("chartData", chartData.get("data"));
            
            logger.info("📈 Successfully prepared chart data for rendering");
            return "dashboard/volume-chart";
            
        } catch (Exception e) {
            logger.error("❌ Error preparing chart data: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load chart data");
            return "error";
        }
    }
} 