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

import com.darro_tech.revengproject.controllers.AuthenticationController;
import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.UserSubscriptionView;
import com.darro_tech.revengproject.services.UserSubscriptionService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/user-subscriptions")
public class UserSubscriptionController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(UserSubscriptionController.class);

    @Autowired
    private UserSubscriptionService userSubscriptionService;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private com.darro_tech.revengproject.services.FakeDataService fakeDataService;

    @GetMapping
    public String listUserSubscriptions(
            Model model,
            HttpSession session,
            @org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "false") Boolean useFakeData) {

        logger.info("📋 Accessing user subscriptions page (useFakeData: {})", useFakeData);

        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            logger.warn("🔒 No authenticated user found, redirecting to login");
            return "redirect:/login";
        }

        try {
            List<UserSubscriptionView> subscriptions;

            if (useFakeData) {
                logger.info("🎭 Using fake data for user subscriptions");
                subscriptions = fakeDataService.loadFakeUserSubscriptions();
            } else {
                subscriptions = userSubscriptionService.listUserSubscriptions(user);
            }

            // Ensure subscriptions is never null
            if (subscriptions == null) {
                subscriptions = Collections.emptyList();
            }

            model.addAttribute("title", "User Subscriptions");
            model.addAttribute("subscriptions", subscriptions);
            model.addAttribute("useFakeData", useFakeData != null ? useFakeData : false);

            logger.info("✅ Successfully loaded {} user subscriptions", subscriptions.size());
            return view("admin/user-subscriptions/index", model);

        } catch (SecurityException e) {
            logger.warn("❌ Access denied: {}", e.getMessage());
            return "redirect:/access-denied";
        } catch (org.springframework.dao.DataAccessException e) {
            logger.error("❌ Database error loading user subscriptions: {}", e.getMessage(), e);
            String errorMsg = "Database error: " + e.getClass().getSimpleName();
            if (e.getMessage() != null && e.getMessage().contains("user_subscriptions_view")) {
                errorMsg += ". The user_subscriptions_view database view may not exist or is inaccessible.";
            }
            model.addAttribute("errorMessage", errorMsg);
            model.addAttribute("subscriptions", Collections.emptyList());
            return view("admin/user-subscriptions/index", model);
        } catch (NullPointerException e) {
            logger.error("❌ Null pointer error loading user subscriptions: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Null pointer error: " + e.getMessage() + ". Check service initialization.");
            model.addAttribute("subscriptions", Collections.emptyList());
            return view("admin/user-subscriptions/index", model);
        } catch (Exception e) {
            logger.error("❌ Error loading user subscriptions: {} (Type: {})", e.getMessage(), e.getClass().getName(), e);
            String errorMsg = "Error: " + e.getClass().getSimpleName();
            if (e.getMessage() != null) {
                errorMsg += " - " + e.getMessage();
            }
            model.addAttribute("errorMessage", errorMsg);
            model.addAttribute("subscriptions", Collections.emptyList());
            return view("admin/user-subscriptions/index", model);
        }
    }
}
