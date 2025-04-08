package com.darro_tech.revengproject.controllers.admin;

import com.darro_tech.revengproject.controllers.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/companies")
public class CompanyController extends BaseController {

    @GetMapping
    public String listCompanies(Model model) {
        model.addAttribute("title", "Companies Management");
        return view("admin/companies/company-management", model);
    }
} 