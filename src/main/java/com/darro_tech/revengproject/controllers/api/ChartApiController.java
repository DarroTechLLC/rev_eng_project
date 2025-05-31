package com.darro_tech.revengproject.controllers.api;

import com.darro_tech.revengproject.controllers.BaseController;
import com.darro_tech.revengproject.dto.FarmVolumeData;
import com.darro_tech.revengproject.services.ChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/charts")
public class ChartApiController extends BaseController {

    @Autowired
    private ChartService chartService;

    @PostMapping("/multi-farm/farm-volumes-for-date")
    public ResponseEntity<Map<String, Object>> getFarmVolumesForDate(
            @RequestParam("company_id") String companyId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        System.out.println("=== ChartApiController: getFarmVolumesForDate ===");
        System.out.println("Request parameters: companyId=" + companyId + ", date=" + date);

        try {
            List<FarmVolumeData> data = chartService.getDailyVolumeByFarmForDate(companyId, date);

            System.out.println("Data retrieved successfully: " + (data != null ? data.size() : 0) + " farm records");
            if (data != null && !data.isEmpty()) {
                System.out.println("Sample data: " + data.get(0).getFarmName() + " - " + data.get(0).getVolume());
            } else {
                System.out.println("WARNING: No data returned from service");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("ERROR in getFarmVolumesForDate: " + e.getMessage());
            e.printStackTrace();

            // Return empty data with error flag for frontend debugging
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("data", new ArrayList<>());
            errorResponse.put("error", true);
            errorResponse.put("errorMessage", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }
}
