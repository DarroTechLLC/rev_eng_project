package com.darro_tech.revengproject.controllers.admin;

import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.dto.RegisterFormDTO;
import com.darro_tech.revengproject.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController extends BaseController {

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public String manageUsers(Model model) {
        model.addAttribute("title", "User Management");
        model.addAttribute("registerFormDTO", new RegisterFormDTO());
        model.addAttribute("roles", roleRepository.findAll());
        return view("admin/users/user-management", model);
    }
} 