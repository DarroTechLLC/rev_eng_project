package com.darro_tech.revengproject.controllers.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.services.WebsiteAlertService;

@Controller
@RequestMapping("/admin/alerts")
public class AlertController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);
    private final WebsiteAlertService websiteAlertService;

    @Autowired
    public AlertController(WebsiteAlertService websiteAlertService) {
        this.websiteAlertService = websiteAlertService;
    }

    /**
     * Display the list of website alerts
     */
    @GetMapping
    public String listAlerts(Model model) {
        logger.info("📢 Displaying website alerts list page");
        model.addAttribute("title", "Website Alerts");
        return view("admin/alerts/index", model);
    }

    /**
     * Display form to create a new website alert
     */
    @GetMapping("/create")
    public String createAlert(Model model) {
        logger.info("📢 Displaying create website alert page");
        model.addAttribute("title", "Create Website Alert");
        model.addAttribute("isNew", true);
        return view("admin/alerts/edit", model);
    }

    /**
     * Display form to edit an existing website alert
     */
    @GetMapping("/edit/{id}")
    public String editAlert(@PathVariable("id") Integer id, Model model) {
        logger.info("📢 Displaying edit website alert page for ID: {}", id);
        model.addAttribute("title", "Edit Website Alert");
        model.addAttribute("isNew", false);
        model.addAttribute("alertId", id);
        return view("admin/alerts/edit", model);
    }
}
