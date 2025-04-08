package com.darro_tech.revengproject.controllers.admin;

import com.darro_tech.revengproject.controllers.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/farms")
public class FarmController extends BaseController {

    @GetMapping
    public String manageFarms(Model model) {
        model.addAttribute("title", "Farm Management");
        return view("admin/farms/farm-management", model);
    }
} 