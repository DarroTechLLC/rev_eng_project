package com.darro_tech.revengproject.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.FarmService;

import jakarta.servlet.http.HttpSession;

@Controller("farmSearchController")
public class FarmSearchController extends BaseController {

    @Autowired
    private FarmService farmService;

    @Autowired
    private AuthenticationController authenticationController;

    /**
     * API endpoint to search farms by name with user authentication
     */
    @GetMapping("/api/farms/user-search")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchFarms(
            @RequestParam(required = false) String query,
            HttpSession session) {

        User user = authenticationController.getUserFromSession(session);
        if (user == null) {
            return ResponseEntity.status(401).body(null);
        }

        String selectedCompanyId = (String) session.getAttribute("selectedCompanyId");
        if (selectedCompanyId == null) {
            return ResponseEntity.status(400).body(null);
        }

        List<Map<String, Object>> results = farmService.searchFarmsByNameForCompany(
                query, selectedCompanyId);

        return ResponseEntity.ok(results);
    }
}
