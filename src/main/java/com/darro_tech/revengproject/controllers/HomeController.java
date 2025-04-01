package com.darro_tech.revengproject.controllers;

import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class HomeController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    private AuthenticationController authenticationController;

    @GetMapping("/")  // Explicitly map to root path
    @Transactional
    public String index(Model model, HttpSession session) {
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E MM-dd-yyyy");

        // Uncomment if you want to add user to model
        User user = authenticationController.getUserFromSession(session);
        String wholeName = user.getFirstName() + " " + user.getLastName();
         model.addAttribute("user", wholeName);

        model.addAttribute("date", "Today is: " + dateFormat.format(today));
        return "/pages/dashboard";  // This should match your HTML file name in templates directory
    }
}