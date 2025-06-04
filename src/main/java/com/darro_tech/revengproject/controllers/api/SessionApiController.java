package com.darro_tech.revengproject.controllers.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.darro_tech.revengproject.controllers.BaseController;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/session")
public class SessionApiController extends BaseController {

    static class CompanyRequest {

        private String companyId;

        public String getCompanyId() {
            return companyId;
        }

        public void setCompanyId(String companyId) {
            this.companyId = companyId;
        }
    }

    @PostMapping("/company")
    public ResponseEntity<Map<String, Object>> setCompany(@RequestBody CompanyRequest request, HttpSession session) {
        System.out.println("🔒 Setting company in session: " + request.getCompanyId());

        try {
            // Store the company ID directly in session
            session.setAttribute("selectedCompanyId", request.getCompanyId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Company set successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error setting company in session: " + e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to set company: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
