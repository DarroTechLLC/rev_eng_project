package com.darro_tech.revengproject.controllers.account;

import com.darro_tech.revengproject.controllers.AuthenticationController;
import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/account")
public class AccountController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/change-password")
    public String changePassword(Model model) {
        model.addAttribute("title", "Change Password");
        return view("account/change-password", model);
    }

    @PostMapping("/change-password")
    public String processChangePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Processing password change request");

        // Get the current user
        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            logger.warn("No user found in session when attempting to change password");
            redirectAttributes.addFlashAttribute("error", "You must be logged in to change your password");
            return "redirect:/login";
        }

        // Validate current password
        if (!user.isMatchingPassword(currentPassword)) {
            logger.warn("Current password validation failed for user: {}", user.getUsername());
            model.addAttribute("error", "Current password is incorrect");
            model.addAttribute("title", "Change Password");
            return view("account/change-password", model);
        }

        // Validate new password and confirmation match
        if (!newPassword.equals(confirmPassword)) {
            logger.warn("New password and confirmation do not match for user: {}", user.getUsername());
            model.addAttribute("error", "New password and confirmation do not match");
            model.addAttribute("title", "Change Password");
            return view("account/change-password", model);
        }

        // Validate new password is not empty and meets minimum requirements
        if (newPassword.length() < 5) {
            logger.warn("New password does not meet minimum length requirements");
            model.addAttribute("error", "New password must be at least 5 characters long");
            model.addAttribute("title", "Change Password");
            return view("account/change-password", model);
        }

        // Update the password
        try {
            user.setPassword(newPassword);
            // Save the user with the updated password
            userRepository.save(user);

            logger.info("Password successfully changed for user: {}", user.getUsername());
            redirectAttributes.addFlashAttribute("success", "Your password has been successfully updated");
            return "redirect:/account/change-password";
        } catch (Exception e) {
            logger.error("Error changing password: {}", e.getMessage(), e);
            model.addAttribute("error", "An error occurred while changing your password. Please try again.");
            model.addAttribute("title", "Change Password");
            return view("account/change-password", model);
        }
    }
}
