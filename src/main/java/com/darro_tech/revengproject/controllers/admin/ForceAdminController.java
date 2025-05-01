package com.darro_tech.revengproject.controllers.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.darro_tech.revengproject.controllers.AuthenticationController;
import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.User;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/debug/force-admin")
public class ForceAdminController extends BaseController {

    @Autowired
    private AuthenticationController authenticationController;

    @GetMapping
    public String forceAdminPage(Model model, HttpSession session) {
        // Get the current user
        User user = authenticationController.getUserFromSession(session);
        if (user != null) {
            // Explicitly add these attributes before any other processing
            model.addAttribute("isAdmin", Boolean.TRUE);
            model.addAttribute("isSuperAdmin", Boolean.TRUE);

            // Add additional debug info
            model.addAttribute("forceAdminApplied", "YES - Admin privileges have been force-applied");

            // Basic attributes
            model.addAttribute("title", "Force Admin Test");

            // Return the custom debug page using view method
            return view("admin/force-admin", model);
        }

        return "redirect:/login";
    }
}
