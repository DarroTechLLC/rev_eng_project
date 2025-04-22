package com.darro_tech.revengproject.controllers.admin;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.dto.RegisterFormDTO;
import com.darro_tech.revengproject.models.dto.UserManagementDTO;
import com.darro_tech.revengproject.repositories.RoleRepository;
import com.darro_tech.revengproject.services.UserManagementService;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController extends BaseController {

    private static final Logger logger = Logger.getLogger(UserManagementController.class.getName());

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserManagementService userManagementService;

    @GetMapping
    public String manageUsers(Model model) {
        logger.info("🚀 Accessing user management page");
        
        try {
            // Set page title and register form
            model.addAttribute("title", "User Management");
            logger.info("📝 Setting page title");
            
            model.addAttribute("registerFormDTO", new RegisterFormDTO());
            logger.info("📝 Added register form to model");
            
            // Get roles for registration form
            var roles = roleRepository.findAll();
            model.addAttribute("roles", roles);
            logger.info("🔑 Added " + roles.size() + " roles to model for registration form");
            
            // Get all users with their roles and companies
            logger.info("🔍 Fetching users with roles and companies");
            List<UserManagementDTO> users = userManagementService.getAllUsersWithRolesAndCompanies();
            logger.info("✅ Retrieved " + users.size() + " users for management page");
            model.addAttribute("users", users);
            
            logger.info("🎯 Successfully prepared user management page");
            return view("admin/users/user-management", model);
        } catch (Exception e) {
            logger.severe("❌ Error preparing user management page: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "An error occurred while loading users");
            return view("admin/users/user-management", model);
        }
    }
} 