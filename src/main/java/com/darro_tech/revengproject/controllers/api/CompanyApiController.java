package com.darro_tech.revengproject.controllers.api;

import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/companies")
public class CompanyApiController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/{id}/logo-upload")
    public ResponseEntity<?> uploadLogo(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        try {
            String logoUrl = fileStorageService.saveFile(file, "company_" + id);
            companyService.updateLogoUrl(id, logoUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("logoUrl", logoUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
} 