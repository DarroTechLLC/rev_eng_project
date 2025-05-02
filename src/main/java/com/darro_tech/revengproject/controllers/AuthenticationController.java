package com.darro_tech.revengproject.controllers;

import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.darro_tech.revengproject.models.Role;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.UserContactInfo;
import com.darro_tech.revengproject.models.UserContactType;
import com.darro_tech.revengproject.models.UserRole;
import com.darro_tech.revengproject.models.dto.LoginFormDTO;
import com.darro_tech.revengproject.models.dto.RegisterFormDTO;
import com.darro_tech.revengproject.repositories.RoleRepository;
import com.darro_tech.revengproject.repositories.UserContactInfoRepository;
import com.darro_tech.revengproject.repositories.UserContactTypeRepository;
import com.darro_tech.revengproject.repositories.UserRepository;
import com.darro_tech.revengproject.repositories.UserRoleRepository;
import com.darro_tech.revengproject.services.CompanySelectionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Created by Chris Bay
 */
@Controller
public class AuthenticationController {

    private static final Logger logger = Logger.getLogger(AuthenticationController.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserContactInfoRepository userContactInfoRepository;

    @Autowired
    private UserContactTypeRepository userContactTypeRepository;

    @Autowired
    private CompanySelectionService companySelectionService;

    private static final String userSessionKey = "user";

    public User getUserFromSession(HttpSession session) {
        String userId = (String) session.getAttribute(userSessionKey);
        if (userId == null) {
            return null;
        }

        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            return null;
        }

        return user.get();
    }

    private static void setUserInSession(HttpSession session, User user) {
        session.setAttribute(userSessionKey, user.getId());
    }

    @GetMapping("/register")
    public String displayRegistrationForm(Model model) {
        model.addAttribute(new RegisterFormDTO());
        model.addAttribute("roles", roleRepository.findAll());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistrationForm(@ModelAttribute @Valid RegisterFormDTO registerFormDTO,
            Errors errors, HttpServletRequest request,
            Model model) {

        if (errors.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll());
            return "fragments/register-form :: form";
        }

        User existingUser = userRepository.findByUsername(registerFormDTO.getUsername());

        if (existingUser != null) {
            errors.rejectValue("username", "username.alreadyexists", "A user with that username already exists");
            model.addAttribute("roles", roleRepository.findAll());
            return "fragments/register-form :: form";
        }

        String password = registerFormDTO.getPassword();
        String verifyPassword = registerFormDTO.getVerifyPassword();
        if (!password.equals(verifyPassword)) {
            errors.rejectValue("password", "passwords.mismatch", "Passwords do not match");
            model.addAttribute("roles", roleRepository.findAll());
            return "fragments/register-form :: form";
        }

        // Create new user using constructor that handles password hashing
        User newUser = new User(registerFormDTO.getUsername(), password);
        newUser.setFirstName(registerFormDTO.getFirstName());
        newUser.setLastName(registerFormDTO.getLastName());
        userRepository.save(newUser);

        // Assign roles
        if (registerFormDTO.getRoleIds() != null && registerFormDTO.getRoleIds().length > 0) {
            for (String roleId : registerFormDTO.getRoleIds()) {
                Optional<Role> role = roleRepository.findById(roleId);
                if (role.isPresent()) {
                    UserRole userRole = new UserRole();
                    userRole.setUser(newUser);
                    userRole.setRole(role.get());
                    userRole.setTimestamp(Instant.now());
                    userRoleRepository.save(userRole);
                }
            }
        } else {
            // Assign default "User" role if no roles selected
            Role defaultRole = roleRepository.findByName("User");
            if (defaultRole != null) {
                UserRole userRole = new UserRole();
                userRole.setUser(newUser);
                userRole.setRole(defaultRole);
                userRole.setTimestamp(Instant.now());
                userRoleRepository.save(userRole);
            }
        }

        // Save contact information
        if (registerFormDTO.getEmail() != null && !registerFormDTO.getEmail().isEmpty()) {
            UserContactType emailType = userContactTypeRepository.findByName("Email");
            if (emailType != null) {
                // Check if contact info already exists
                UserContactInfo existingEmail = userContactInfoRepository.findByUserAndType(newUser, emailType);
                if (existingEmail == null) {
                    UserContactInfo emailInfo = new UserContactInfo();
                    emailInfo.setUser(newUser);
                    emailInfo.setType(emailType);
                    emailInfo.setValue(registerFormDTO.getEmail());
                    emailInfo.setTimestamp(Instant.now());
                    userContactInfoRepository.save(emailInfo);
                }
            }
        }

        if (registerFormDTO.getPhone() != null && !registerFormDTO.getPhone().isEmpty()) {
            UserContactType phoneType = userContactTypeRepository.findByName("SMS");
            if (phoneType != null) {
                // Check if contact info already exists
                UserContactInfo existingPhone = userContactInfoRepository.findByUserAndType(newUser, phoneType);
                if (existingPhone == null) {
                    UserContactInfo phoneInfo = new UserContactInfo();
                    phoneInfo.setUser(newUser);
                    phoneInfo.setType(phoneType);
                    phoneInfo.setValue(registerFormDTO.getPhone());
                    phoneInfo.setTimestamp(Instant.now());
                    userContactInfoRepository.save(phoneInfo);
                }
            }
        }

        setUserInSession(request.getSession(), newUser);

        // Auto-select the first company for the new user
        companySelectionService.selectDefaultCompanyForUser(newUser, request.getSession());

        return "redirect:/admin/users";
    }

    @GetMapping("/login")
    public String displayLoginForm(Model model) {
        model.addAttribute(new LoginFormDTO());
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLoginForm(@ModelAttribute @Valid LoginFormDTO loginFormDTO,
            Errors errors, HttpServletRequest request,
            Model model) {

        if (errors.hasErrors()) {
            logger.warning("❌ Login failed due to validation errors");
            return "auth/login";
        }

        User theUser = userRepository.findByUsername(loginFormDTO.getUsername());

        if (theUser == null) {
            logger.warning("❌ Login failed: User not found - " + loginFormDTO.getUsername());
            errors.rejectValue("username", "user.invalid", "The given username does not exist");
            return "auth/login";
        }

        String password = loginFormDTO.getPassword();

        if (!theUser.isMatchingPassword(password)) {
            logger.warning("❌ Login failed: Invalid password for user - " + loginFormDTO.getUsername());
            errors.rejectValue("password", "password.invalid", "Invalid password");
            return "auth/login";
        }

        // User authenticated successfully
        logger.info("✅ User login successful: " + theUser.getUsername());

        // Store user info in session
        HttpSession session = request.getSession();
        setUserInSession(session, theUser);

        // Automatically select the first alphabetical company for the user
        boolean companySelected = companySelectionService.selectDefaultCompanyForUser(theUser, session);

        if (!companySelected) {
            logger.warning("⚠️ No company was automatically selected for user: " + theUser.getUsername());
            // Even if no company was selected, we'll redirect to the dashboard
            // The dashboard controller will handle the case where no company is selected
        }

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        logger.info("👋 User logged out: "
                + (getUserFromSession(request.getSession()) != null
                ? getUserFromSession(request.getSession()).getUsername() : "unknown"));
        request.getSession().invalidate();
        return "redirect:/login";
    }

}
