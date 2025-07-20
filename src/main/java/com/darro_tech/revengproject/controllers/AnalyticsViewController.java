package com.darro_tech.revengproject.controllers;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.repositories.FarmRepository;

@Controller
@RequestMapping("/{companyName}/analytics")
public class AnalyticsViewController extends BaseController {

    @Autowired
    private FarmRepository farmRepository;

    @GetMapping("/anomaly-detection")
    public String showAnomalyDetection(@PathVariable String companyName, Model model) {
        // Add any necessary data to the model
        List<Farm> farms = farmRepository.findAll();
        model.addAttribute("farms", farms);
        model.addAttribute("selectedDate", new Date());

        return view("analytics/anomaly-detection", model);
    }

    @GetMapping("/trend-analysis")
    public String showTrendAnalysis(@PathVariable String companyName, Model model) {
        // Add any necessary data to the model
        List<Farm> farms = farmRepository.findAll();
        model.addAttribute("farms", farms);
        model.addAttribute("selectedDate", new Date());

        return view("analytics/trend-analysis", model);
    }

    @GetMapping("/production-forecasting")
    public String showProductionForecasting(@PathVariable String companyName, Model model) {
        // Add any necessary data to the model
        List<Farm> farms = farmRepository.findAll();
        model.addAttribute("farms", farms);
        model.addAttribute("selectedDate", new Date());

        return view("analytics/production-forecasting", model);
    }
}
