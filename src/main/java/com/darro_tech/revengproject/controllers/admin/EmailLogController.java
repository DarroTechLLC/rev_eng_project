package com.darro_tech.revengproject.controllers.admin;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.darro_tech.revengproject.controllers.AuthenticationController;
import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.EmailLogView;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.EmailLogService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/email-logs")
public class EmailLogController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(EmailLogController.class);

    @Autowired
    private EmailLogService emailLogService;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private com.darro_tech.revengproject.services.FakeDataService fakeDataService;

    @GetMapping
    public String listEmailLogs(
            Model model,
            HttpSession session,
            @RequestParam(required = false, defaultValue = "1000") Integer limit,
            @RequestParam(required = false, defaultValue = "false") Boolean useFakeData) {

        logger.info("📧 Accessing email logs page (useFakeData: {})", useFakeData);

        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            logger.warn("🔒 No authenticated user found, redirecting to login");
            return "redirect:/login";
        }

        try {
            List<EmailLogView> emailLogs;

            if (useFakeData) {
                logger.info("🎭 Using fake data for email logs");
                emailLogs = fakeDataService.loadFakeEmailLogs();
            } else {
                emailLogs = emailLogService.listEmailLogs(user, limit);
            }

            // Ensure emailLogs is never null
            if (emailLogs == null) {
                emailLogs = Collections.emptyList();
            }

            model.addAttribute("title", "Email Logs");
            model.addAttribute("emailLogs", emailLogs);
            model.addAttribute("useFakeData", useFakeData != null ? useFakeData : false);

            logger.info("✅ Successfully loaded {} email logs", emailLogs.size());
            return view("admin/email-logs/index", model);

        } catch (SecurityException e) {
            logger.warn("❌ Access denied: {}", e.getMessage());
            return "redirect:/access-denied";
        } catch (org.springframework.dao.DataAccessException e) {
            logger.error("❌ Database error loading email logs: {}", e.getMessage(), e);
            String errorMsg = "Database error: " + e.getClass().getSimpleName();
            if (e.getMessage() != null && e.getMessage().contains("email_logs_view")) {
                errorMsg += ". The email_logs_view database view may not exist or is inaccessible.";
            }
            model.addAttribute("errorMessage", errorMsg);
            model.addAttribute("emailLogs", Collections.emptyList());
            return view("admin/email-logs/index", model);
        } catch (NullPointerException e) {
            logger.error("❌ Null pointer error loading email logs: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Null pointer error: " + e.getMessage() + ". Check service initialization.");
            model.addAttribute("emailLogs", Collections.emptyList());
            return view("admin/email-logs/index", model);
        } catch (Exception e) {
            logger.error("❌ Error loading email logs: {} (Type: {})", e.getMessage(), e.getClass().getName(), e);
            String errorMsg = "Error: " + e.getClass().getSimpleName();
            if (e.getMessage() != null) {
                errorMsg += " - " + e.getMessage();
            }
            model.addAttribute("errorMessage", errorMsg);
            model.addAttribute("emailLogs", Collections.emptyList());
            return view("admin/email-logs/index", model);
        }
    }
}
