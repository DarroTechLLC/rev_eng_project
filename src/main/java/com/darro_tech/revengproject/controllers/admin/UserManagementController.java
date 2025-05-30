package com.darro_tech.revengproject.controllers.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.dto.RegisterFormDTO;
import com.darro_tech.revengproject.models.dto.UserManagementDTO;
import com.darro_tech.revengproject.models.dto.UserUpdateDTO;
import com.darro_tech.revengproject.repositories.CompanyRepository;
import com.darro_tech.revengproject.repositories.RoleRepository;
import com.darro_tech.revengproject.repositories.UserRepository;
import com.darro_tech.revengproject.services.UserManagementService;
import com.darro_tech.revengproject.services.UserManagementServiceInterface;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController extends BaseController {

    private static final Logger logger = Logger.getLogger(UserManagementController.class.getName());

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserManagementServiceInterface userManagementService;

    @Autowired
    private UserRepository userRepository;

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

            // Get companies for registration form
            var companies = companyRepository.findAll();
            model.addAttribute("companies", companies);
            logger.info("🏢 Added " + companies.size() + " companies to model for registration form");

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
    public String updateUser(@ModelAttribute("editUser") UserUpdateDTO userUpdateDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Log the exact format of the received ID
        logger.info("🔍 Raw user ID received: '" + userUpdateDTO.getId() + "'");

        // Extra cleaning for the ID value in case the form submission has issues
        if (userUpdateDTO.getId() != null && userUpdateDTO.getId().contains(",")) {
            String originalId = userUpdateDTO.getId();
            String cleanedId = originalId.split(",")[0].trim();
            userUpdateDTO.setId(cleanedId);
            logger.info("🛠️ Cleaned user ID from '" + originalId + "' to '" + cleanedId + "'");
        }

        logger.info("🔄 Processing user update for user ID: " + userUpdateDTO.getId());
        logger.info("🔄 Update details - Username: " + userUpdateDTO.getUsername()
                + ", First Name: " + userUpdateDTO.getFirstName()
                + ", Last Name: " + userUpdateDTO.getLastName());

        // Manual validation
        boolean hasErrors = false;
        List<String> errorMessages = new ArrayList<>();

        // Validate username
        if (userUpdateDTO.getUsername() == null || userUpdateDTO.getUsername().trim().isEmpty()) {
            errorMessages.add("Username: Username is required");
            hasErrors = true;
        } else if (userUpdateDTO.getUsername().length() < 3 || userUpdateDTO.getUsername().length() > 50) {
            errorMessages.add("Username: Username must be between 3 and 50 characters");
            hasErrors = true;
        }

        // Validate first name
        if (userUpdateDTO.getFirstName() == null || userUpdateDTO.getFirstName().trim().isEmpty()) {
            errorMessages.add("First Name: First name is required");
            hasErrors = true;
        } else if (userUpdateDTO.getFirstName().length() > 50) {
            errorMessages.add("First Name: First name cannot exceed 50 characters");
            hasErrors = true;
        }

        // Validate last name
        if (userUpdateDTO.getLastName() == null || userUpdateDTO.getLastName().trim().isEmpty()) {
            errorMessages.add("Last Name: Last name is required");
            hasErrors = true;
        } else if (userUpdateDTO.getLastName().length() > 50) {
            errorMessages.add("Last Name: Last name cannot exceed 50 characters");
            hasErrors = true;
        }

        // Validate email (basic validation)
        if (userUpdateDTO.getEmail() == null || userUpdateDTO.getEmail().trim().isEmpty()) {
            errorMessages.add("Email: Email is required");
            hasErrors = true;
        } else if (!userUpdateDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorMessages.add("Email: Invalid email format");
            hasErrors = true;
        }

        // Check for binding errors (if any)
        if (bindingResult.hasErrors() || hasErrors) {
            logger.warning("⚠️ Validation errors in user update form");

            // Add roles and companies back to the model for the form
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("companies", companyRepository.findAll());

            // Add validation error messages to model
            if (bindingResult.hasErrors()) {
                bindingResult.getFieldErrors().forEach(error
                        -> errorMessages.add(error.getField() + ": " + error.getDefaultMessage())
                );
            }
            model.addAttribute("validationErrors", errorMessages);

            return view("admin/users/edit-user", model);
        }

        try {
            // Log roles and companies for debugging
            if (userUpdateDTO.getRoleIds() != null) {
                logger.info("📋 Roles selected: " + userUpdateDTO.getRoleIds().size() + " roles");
                userUpdateDTO.getRoleIds().forEach(roleId
                        -> logger.info("👤 Role ID: " + roleId));
            } else {
                logger.warning("⚠️ No roles selected for user");
                userUpdateDTO.setRoleIds(new ArrayList<>());
            }

            if (userUpdateDTO.getCompanyIds() != null) {
                logger.info("🏢 Companies selected: " + userUpdateDTO.getCompanyIds().size() + " companies");
                userUpdateDTO.getCompanyIds().forEach(companyId
                        -> logger.info("🏢 Company ID: " + companyId));
            } else {
                logger.warning("⚠️ No companies selected for user");
                userUpdateDTO.setCompanyIds(new ArrayList<>());
            }

            // Proceed with updating the user
            boolean updateSuccess = userManagementService.updateUser(userUpdateDTO);

            if (updateSuccess) {
                logger.info("✅ Successfully updated user with ID: " + userUpdateDTO.getId());
                redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
            } else {
                logger.warning("⚠️ Failed to update user with ID: " + userUpdateDTO.getId());

                // Generic error message
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update user. Please try again.");
            }

            return "redirect:/admin/users";
        } catch (Exception e) {
            logger.severe("❌ Error updating user: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while updating user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute @Valid RegisterFormDTO registerFormDTO,
                             Errors errors, Model model, RedirectAttributes redirectAttributes) {
        logger.info("🔄 Processing user creation for username: " + registerFormDTO.getUsername());

        try {
            if (errors.hasErrors()) {
                logger.warning("⚠️ Validation errors in user creation form");

                // Add roles back to the model for the form
                model.addAttribute("roles", roleRepository.findAll());

                // Add error message
                model.addAttribute("errorMessage", "Please correct the errors in the form");

                // Get all users with their roles and companies
                logger.info("🔍 Fetching users with roles and companies");
                List<UserManagementDTO> users = userManagementService.getAllUsersWithRolesAndCompanies();
                logger.info("✅ Retrieved " + users.size() + " users for management page");
                model.addAttribute("users", users);

                // Add the form DTO back to the model
                model.addAttribute("registerFormDTO", registerFormDTO);

                return view("admin/users/user-management", model);
            }

            // Check if username already exists
            User existingUser = userRepository.findByUsername(registerFormDTO.getUsername());
            if (existingUser != null) {
                logger.warning("⚠️ User creation attempted with existing username: " + registerFormDTO.getUsername());
                errors.rejectValue("username", "username.alreadyexists", "A user with that username already exists");

                // Add roles back to the model for the form
                model.addAttribute("roles", roleRepository.findAll());

                // Add error message
                model.addAttribute("errorMessage", "A user with that username already exists");

                // Get all users with their roles and companies
                logger.info("🔍 Fetching users with roles and companies");
                List<UserManagementDTO> users = userManagementService.getAllUsersWithRolesAndCompanies();
                logger.info("✅ Retrieved " + users.size() + " users for management page");
                model.addAttribute("users", users);

                // Add the form DTO back to the model
                model.addAttribute("registerFormDTO", registerFormDTO);

                return view("admin/users/user-management", model);
            }

            // Verify passwords match
            String password = registerFormDTO.getPassword();
            String verifyPassword = registerFormDTO.getVerifyPassword();
            if (!password.equals(verifyPassword)) {
                logger.warning("⚠️ User creation attempted with mismatched passwords");
                errors.rejectValue("password", "passwords.mismatch", "Passwords do not match");

                // Add roles back to the model for the form
                model.addAttribute("roles", roleRepository.findAll());

                // Add error message
                model.addAttribute("errorMessage", "Passwords do not match");

                // Get all users with their roles and companies
                logger.info("🔍 Fetching users with roles and companies");
                List<UserManagementDTO> users = userManagementService.getAllUsersWithRolesAndCompanies();
                logger.info("✅ Retrieved " + users.size() + " users for management page");
                model.addAttribute("users", users);

                // Add the form DTO back to the model
                model.addAttribute("registerFormDTO", registerFormDTO);

                return view("admin/users/user-management", model);
            }

            // Create new user
            User newUser = new User(registerFormDTO.getUsername(), password);
            newUser.setFirstName(registerFormDTO.getFirstName());
            newUser.setLastName(registerFormDTO.getLastName());

            // Convert role IDs from array to list
            List<String> roleIds = registerFormDTO.getRoleIds() != null ? 
                Arrays.asList(registerFormDTO.getRoleIds()) : new ArrayList<>();

            // Get company IDs from form
            List<String> companyIds = registerFormDTO.getCompanyIds() != null ?
                registerFormDTO.getCompanyIds() : new ArrayList<>();

            // Create user with roles and companies
            User savedUser = userManagementService.createUser(newUser, roleIds, companyIds);

            if (savedUser != null) {
                // Update email and phone if provided
                String userId = savedUser.getId();

                // Update email
                if (registerFormDTO.getEmail() != null && !registerFormDTO.getEmail().isEmpty()) {
                    userManagementService.updateUser(Map.of(
                        "id", userId,
                        "email", registerFormDTO.getEmail()
                    ));
                }

                // Update phone
                if (registerFormDTO.getPhone() != null && !registerFormDTO.getPhone().isEmpty()) {
                    userManagementService.updateUser(Map.of(
                        "id", userId,
                        "phone", registerFormDTO.getPhone()
                    ));
                }

                logger.info("✅ Successfully created user: " + savedUser.getUsername());
                redirectAttributes.addFlashAttribute("successMessage", "User created successfully");
            } else {
                logger.severe("❌ Failed to create user");
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to create user");
            }

            return "redirect:/admin/users";
        } catch (Exception e) {
            logger.severe("❌ Error creating user: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while creating user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam String id, RedirectAttributes redirectAttributes) {
        logger.info("🗑️ Processing user deletion for user ID: " + id);

        try {
            // Find the user
            Optional<User> userOptional = userRepository.findById(id);
            if (!userOptional.isPresent()) {
                logger.warning("⚠️ User not found with ID: " + id);
                redirectAttributes.addFlashAttribute("errorMessage", "User not found");
                return "redirect:/admin/users";
            }

            User user = userOptional.get();

            // Delete the user
            userRepository.delete(user);

            logger.info("✅ Successfully deleted user with ID: " + id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            logger.severe("❌ Error deleting user: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }
}
