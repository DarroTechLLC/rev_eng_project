package com.darro_tech.revengproject.controllers.admin;

import com.darro_tech.revengproject.controllers.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/alerts")
public class AlertController extends BaseController {

    @GetMapping
    public String manageAlerts(Model model) {
        model.addAttribute("title", "Website Alerts");
        return view("admin/alerts/alert-management", model);
    }
} 