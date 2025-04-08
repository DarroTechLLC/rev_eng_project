package com.darro_tech.revengproject.controllers.account;

import com.darro_tech.revengproject.controllers.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/account")
public class AccountController extends BaseController {

    @GetMapping("/change-password")
    public String changePassword(Model model) {
        model.addAttribute("title", "Change Password");
        return view("account/change-password", model);
    }
} 