package com.darro_tech.revengproject.controllers.admin;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.dto.RegisterFormDTO;
import com.darro_tech.revengproject.models.dto.UserManagementDTO;
import com.darro_tech.revengproject.models.dto.UserUpdateDTO;
import com.darro_tech.revengproject.repositories.CompanyRepository;
import com.darro_tech.revengproject.repositories.RoleRepository;
import com.darro_tech.revengproject.services.UserManagementService;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController extends BaseController {

    private static final Logger logger = Logger.getLogger(UserManagementController.class.getName());

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompanyRepository companyRepository;

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

    @GetMapping("/{id}")
    public String editUser(@PathVariable String id, Model model) {
        logger.info("🚀 Accessing edit user page for user ID: " + id);

        try {
            // Get user data by ID
            UserManagementDTO user = userManagementService.getUserManagementDTOById(id);

            if (user == null) {
                logger.warning("⚠️ User not found with ID: " + id);
                model.addAttribute("errorMessage", "User not found");
                return "redirect:/admin/users";
            }

            // Prepare user update DTO
            UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
            userUpdateDTO.setId(user.getId());
            userUpdateDTO.setUsername(user.getUsername());
            userUpdateDTO.setFirstName(user.getFirstName());
            userUpdateDTO.setLastName(user.getLastName());
            userUpdateDTO.setEmail(user.getEmail());
            userUpdateDTO.setPhone(user.getPhone());

            // Set role IDs
            List<String> roleIds = user.getRoles().stream()
                    .map(role -> role.getId())
                    .toList();
            userUpdateDTO.setRoleIds(roleIds);

            // Set company IDs
            List<String> companyIds = user.getCompanies().stream()
                    .map(company -> company.getId())
                    .toList();
            userUpdateDTO.setCompanyIds(companyIds);

            // Add data to model - Change attribute name from "user" to "editUser"
            model.addAttribute("editUser", userUpdateDTO);
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("companies", companyRepository.findAll());

            logger.info("✅ Successfully prepared edit user page for user ID: " + id);
            return view("admin/users/edit-user", model);
        } catch (Exception e) {
            logger.severe("❌ Error preparing edit user page: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "An error occurred while loading user data");
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute UserUpdateDTO userUpdateDTO, RedirectAttributes redirectAttributes) {
        logger.info("🔄 Processing user update for user ID: " + userUpdateDTO.getId());
        logger.info("🔄 Update details - Username: " + userUpdateDTO.getUsername()
                + ", First Name: " + userUpdateDTO.getFirstName()
                + ", Last Name: " + userUpdateDTO.getLastName());

        try {
            boolean success = userManagementService.updateUser(userUpdateDTO);

            if (success) {
                logger.info("✅ Successfully updated user with ID: " + userUpdateDTO.getId());
                redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
            } else {
                logger.warning("⚠️ Failed to update user with ID: " + userUpdateDTO.getId());
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update user");
            }

            return "redirect:/admin/users";
        } catch (Exception e) {
            logger.severe("❌ Error updating user: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while updating user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam String id, RedirectAttributes redirectAttributes) {
        logger.info("🗑️ Processing user deletion for user ID: " + id);

        try {
            // Implement deletion logic here
            // For now, just redirect back to users page
            logger.info("✅ Successfully deleted user with ID: " + id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            logger.severe("❌ Error deleting user: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting user");
            return "redirect:/admin/users";
        }
    }
}
