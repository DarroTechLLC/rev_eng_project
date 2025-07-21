package com.darro_tech.revengproject.controllers;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
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
import com.darro_tech.revengproject.util.LoggerUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * Created by Chris Bay
 */
@Controller
public class AuthenticationController {

    private static final Logger logger = LoggerUtils.getLogger(AuthenticationController.class);

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
            logger.debug("🔍 No user ID found in session");
            return null;
        }

        logger.debug("🔍 Looking up user with ID: {}", userId);
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            logger.warn("⚠️ User with ID {} found in session but not in database", userId);
            return null;
        }

        logger.debug("✅ User found in database: {}", user.get().getUsername());
        return user.get();
    }

    public static void setUserInSession(HttpSession session, User user) {
        logger.debug("🔐 Setting user in session: {}", user.getUsername());
        session.setAttribute(userSessionKey, user.getId());
    }

    @GetMapping("/register")
    public String displayRegistrationForm(Model model) {
        LoggerUtils.logRequest(logger, "GET", "/register");
        logger.debug("📝 Displaying registration form");

        model.addAttribute(new RegisterFormDTO());
        LoggerUtils.logDatabase(logger, "SELECT", "Role", "Loading all roles for registration form");
        model.addAttribute("roles", roleRepository.findAll());

        LoggerUtils.logResponse(logger, "/register", "Registration form displayed");
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistrationForm(@ModelAttribute @Valid RegisterFormDTO registerFormDTO,
            Errors errors, HttpServletRequest request,
            Model model) {
        LoggerUtils.logRequest(logger, "POST", "/register");
        logger.debug("📝 Processing registration form for username: {}", registerFormDTO.getUsername());

        // Start timing for performance logging
        long startTime = System.currentTimeMillis();

        try {
            if (errors.hasErrors()) {
                logger.warn("❌ Registration form has validation errors: {}", errors.getAllErrors());
                LoggerUtils.logDatabase(logger, "SELECT", "Role", "Reloading roles for form with errors");
                model.addAttribute("roles", roleRepository.findAll());

                LoggerUtils.logResponse(logger, "/register", "Registration form redisplayed due to validation errors");
                return "fragments/register-form :: form";
            }

            logger.debug("🔍 Checking if username already exists: {}", registerFormDTO.getUsername());
            LoggerUtils.logDatabase(logger, "SELECT", "User", "Checking if username exists: " + registerFormDTO.getUsername());
            User existingUser = userRepository.findByUsername(registerFormDTO.getUsername()).orElse(null);

            if (existingUser != null) {
                logger.warn("⚠️ Registration attempted with existing username: {}", registerFormDTO.getUsername());
                errors.rejectValue("username", "username.alreadyexists", "A user with that username already exists");
                LoggerUtils.logDatabase(logger, "SELECT", "Role", "Reloading roles for form with username error");
                model.addAttribute("roles", roleRepository.findAll());

                LoggerUtils.logResponse(logger, "/register", "Registration form redisplayed due to existing username");
                return "fragments/register-form :: form";
            }

            String password = registerFormDTO.getPassword();
            String verifyPassword = registerFormDTO.getVerifyPassword();
            if (!password.equals(verifyPassword)) {
                logger.warn("⚠️ Registration attempted with mismatched passwords for user: {}", registerFormDTO.getUsername());
                errors.rejectValue("password", "passwords.mismatch", "Passwords do not match");
                LoggerUtils.logDatabase(logger, "SELECT", "Role", "Reloading roles for form with password error");
                model.addAttribute("roles", roleRepository.findAll());

                LoggerUtils.logResponse(logger, "/register", "Registration form redisplayed due to password mismatch");
                return "fragments/register-form :: form";
            }

            // Create new user using constructor that handles password hashing
            logger.info("👤 Creating new user: {}", registerFormDTO.getUsername());
            User newUser = new User(registerFormDTO.getUsername(), password);
            newUser.setFirstName(registerFormDTO.getFirstName());
            newUser.setLastName(registerFormDTO.getLastName());

            LoggerUtils.logDatabase(logger, "INSERT", "User", "Creating new user: " + registerFormDTO.getUsername());
            userRepository.save(newUser);
            logger.debug("✅ User created successfully: {}", newUser.getId());

            // Assign roles
            if (registerFormDTO.getRoleIds() != null && registerFormDTO.getRoleIds().length > 0) {
                logger.debug("👑 Assigning {} roles to user", registerFormDTO.getRoleIds().length);
                for (String roleId : registerFormDTO.getRoleIds()) {
                    LoggerUtils.logDatabase(logger, "SELECT", "Role", "Finding role by ID: " + roleId);
                    Optional<Role> role = roleRepository.findById(roleId);
                    if (role.isPresent()) {
                        logger.debug("👑 Assigning role: {} to user: {}", role.get().getName(), newUser.getUsername());
                        UserRole userRole = new UserRole();
                        userRole.setUser(newUser);
                        userRole.setRole(role.get());
                        userRole.setTimestamp(Instant.now());

                        LoggerUtils.logDatabase(logger, "INSERT", "UserRole", "Assigning role " + role.get().getName() + " to user");
                        userRoleRepository.save(userRole);
                    } else {
                        logger.warn("⚠️ Role with ID {} not found when assigning to user", roleId);
                    }
                }
            } else {
                // Assign default "User" role if no roles selected
                logger.debug("👑 No roles selected, assigning default 'User' role");
                LoggerUtils.logDatabase(logger, "SELECT", "Role", "Finding default 'User' role");
                Role defaultRole = roleRepository.findByName("User");
                if (defaultRole != null) {
                    logger.debug("👑 Assigning default role: User");
                    UserRole userRole = new UserRole();
                    userRole.setUser(newUser);
                    userRole.setRole(defaultRole);
                    userRole.setTimestamp(Instant.now());

                    LoggerUtils.logDatabase(logger, "INSERT", "UserRole", "Assigning default User role");
                    userRoleRepository.save(userRole);
                } else {
                    logger.error("❌ Default 'User' role not found in database");
                }
            }

            // Save contact information
            if (registerFormDTO.getEmail() != null && !registerFormDTO.getEmail().isEmpty()) {
                logger.debug("📧 Saving email contact info: {}", registerFormDTO.getEmail());
                LoggerUtils.logDatabase(logger, "SELECT", "UserContactType", "Finding Email contact type");
                UserContactType emailType = userContactTypeRepository.findByName("Email");
                if (emailType != null) {
                    // Check if contact info already exists
                    LoggerUtils.logDatabase(logger, "SELECT", "UserContactInfo", "Checking for existing email");
                    UserContactInfo existingEmail = userContactInfoRepository.findByUserAndType(newUser, emailType);
                    if (existingEmail == null) {
                        UserContactInfo emailInfo = new UserContactInfo();
                        emailInfo.setUser(newUser);
                        emailInfo.setType(emailType);
                        emailInfo.setValue(registerFormDTO.getEmail());
                        emailInfo.setTimestamp(Instant.now());

                        LoggerUtils.logDatabase(logger, "INSERT", "UserContactInfo", "Saving email contact info");
                        userContactInfoRepository.save(emailInfo);
                        logger.debug("✅ Email contact info saved");
                    } else {
                        logger.debug("⚠️ Email contact info already exists, skipping");
                    }
                }
            }

            if (registerFormDTO.getPhone() != null && !registerFormDTO.getPhone().isEmpty()) {
                logger.debug("📱 Saving phone contact info: {}", registerFormDTO.getPhone());
                LoggerUtils.logDatabase(logger, "SELECT", "UserContactType", "Finding SMS contact type");
                UserContactType phoneType = userContactTypeRepository.findByName("SMS");
                if (phoneType != null) {
                    // Check if contact info already exists
                    LoggerUtils.logDatabase(logger, "SELECT", "UserContactInfo", "Checking for existing phone");
                    UserContactInfo existingPhone = userContactInfoRepository.findByUserAndType(newUser, phoneType);
                    if (existingPhone == null) {
                        UserContactInfo phoneInfo = new UserContactInfo();
                        phoneInfo.setUser(newUser);
                        phoneInfo.setType(phoneType);
                        phoneInfo.setValue(registerFormDTO.getPhone());
                        phoneInfo.setTimestamp(Instant.now());

                        LoggerUtils.logDatabase(logger, "INSERT", "UserContactInfo", "Saving phone contact info");
                        userContactInfoRepository.save(phoneInfo);
                        logger.debug("✅ Phone contact info saved");
                    } else {
                        logger.debug("⚠️ Phone contact info already exists, skipping");
                    }
                }
            }

            logger.debug("🔐 Setting user in session after registration");
            setUserInSession(request.getSession(), newUser);

            // Auto-select the first company for the new user
            logger.debug("🏢 Attempting to auto-select company for new user");
            boolean companySelected = companySelectionService.selectDefaultCompanyForUser(newUser, request.getSession());
            if (companySelected) {
                logger.debug("✅ Company auto-selected for new user");
            } else {
                logger.warn("⚠️ No company could be auto-selected for new user");
            }

            // Log performance
            long endTime = System.currentTimeMillis();
            LoggerUtils.logPerformance(logger, "User registration", endTime - startTime);

            LoggerUtils.logUserAction(logger, newUser.getUsername(), "registered new account", null);
            LoggerUtils.logResponse(logger, "/register", "User registration successful, redirecting to users admin page");
            return "redirect:/admin/users";
        } catch (Exception e) {
            LoggerUtils.logException(logger, e, "user registration process");
            throw e; // Re-throw to let Spring handle it
        }
    }

    @GetMapping("/login")
    public String displayLoginForm(Model model) {
        LoggerUtils.logRequest(logger, "GET", "/login");
        logger.debug("🔑 Displaying login form");

        model.addAttribute(new LoginFormDTO());

        LoggerUtils.logResponse(logger, "/login", "Login form displayed");
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLoginForm(@ModelAttribute @Valid LoginFormDTO loginFormDTO,
            Errors errors, HttpServletRequest request,
            Model model) {
        LoggerUtils.logRequest(logger, "POST", "/login");
        logger.debug("🔐 Processing login attempt for username: {}", loginFormDTO.getUsername());

        try {
            if (errors.hasErrors()) {
                LoggerUtils.logValidation(logger, false, "login form", errors.getAllErrors().toString());
                LoggerUtils.logResponse(logger, "/login", "Login form redisplayed due to validation errors");
                return "auth/login";
            }

            logger.debug("🔍 Looking up user by username: {}", loginFormDTO.getUsername());
            LoggerUtils.logDatabase(logger, "SELECT", "User", "Finding user by username for login");
            User theUser = userRepository.findByUsername(loginFormDTO.getUsername()).orElse(null);

            if (theUser == null) {
                logger.warn("⚠️ Login attempt with non-existent username: {}", loginFormDTO.getUsername());
                LoggerUtils.logAuthentication(logger, false, loginFormDTO.getUsername(), "Username not found");

                errors.rejectValue("username", "user.invalid", "The given username does not exist");
                model.addAttribute("loginError", true);
                LoggerUtils.logResponse(logger, "/login", "Login failed: username not found");
                return "auth/login";
            }

            logger.debug("🔑 Verifying password for user: {}", loginFormDTO.getUsername());
            String password = loginFormDTO.getPassword();

            if (!theUser.isMatchingPassword(password)) {
                logger.warn("⚠️ Login attempt with incorrect password for user: {}", loginFormDTO.getUsername());
                LoggerUtils.logAuthentication(logger, false, loginFormDTO.getUsername(), "Invalid password");

                errors.rejectValue("password", "password.invalid", "Invalid password");
                model.addAttribute("loginError", true);
                LoggerUtils.logResponse(logger, "/login", "Login failed: invalid password");
                return "auth/login";
            }

            // User authenticated successfully
            LoggerUtils.logAuthentication(logger, true, theUser.getUsername(), "User authenticated successfully");

            // Store user info in session
            HttpSession session = request.getSession();
            setUserInSession(session, theUser);
            logger.debug("🔐 User stored in session: {}", theUser.getUsername());

            // Automatically select the first alphabetical company for the user
            logger.debug("🏢 Attempting to auto-select company for user");
            boolean companySelected = companySelectionService.selectDefaultCompanyForUser(theUser, session);

            if (companySelected) {
                logger.debug("✅ Company auto-selected for user: {}", theUser.getUsername());
            } else {
                logger.warn("⚠️ No company could be auto-selected for user: {}", theUser.getUsername());
                // Even if no company was selected, we'll redirect to the dashboard
                // The dashboard controller will handle the case where no company is selected
            }

            LoggerUtils.logUserAction(logger, theUser.getUsername(), "logged in", null);
            LoggerUtils.logResponse(logger, "/login", "Login successful, redirecting to dashboard");
            return "redirect:/";
        } catch (Exception e) {
            LoggerUtils.logException(logger, e, "login process");
            throw e; // Re-throw to let Spring handle it
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        LoggerUtils.logRequest(logger, "GET", "/logout");

        HttpSession session = request.getSession();
        User user = getUserFromSession(session);

        if (user != null) {
            LoggerUtils.logUserAction(logger, user.getUsername(), "logged out", null);
            logger.info("👋 User {} logged out", user.getUsername());
        } else {
            logger.warn("⚠️ Logout requested but no user in session");
        }

        session.invalidate();
        logger.debug("🔒 Session invalidated");

        LoggerUtils.logResponse(logger, "/logout", "Logout successful, redirecting to login");
        return "redirect:/login";
    }
}
