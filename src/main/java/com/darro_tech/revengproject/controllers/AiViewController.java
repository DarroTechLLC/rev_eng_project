package com.darro_tech.revengproject.controllers;

import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.repositories.FarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/{companyName}/ai")
public class AiViewController {
    
    @Autowired
    private FarmRepository farmRepository;
    
    @GetMapping("/anomaly-detection")
    public String showAnomalyDetection(@PathVariable String companyName, Model model) {
        // Add any necessary data to the model
        List<Farm> farms = farmRepository.findAll();
        model.addAttribute("farms", farms);
        model.addAttribute("selectedDate", new Date());
        
        return "content/ai/anomaly-detection";
    }
    
    @GetMapping("/trend-analysis")
    public String showTrendAnalysis(@PathVariable String companyName, Model model) {
        // Add any necessary data to the model
        List<Farm> farms = farmRepository.findAll();
        model.addAttribute("farms", farms);
        model.addAttribute("selectedDate", new Date());
        
        return "content/ai/trend-analysis";
    }
    
    @GetMapping("/production-forecasting")
    public String showProductionForecasting(@PathVariable String companyName, Model model) {
        // Add any necessary data to the model
        List<Farm> farms = farmRepository.findAll();
        model.addAttribute("farms", farms);
        model.addAttribute("selectedDate", new Date());
        
        return "content/ai/production-forecasting";
    }
}