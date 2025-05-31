package com.darro_tech.revengproject.controllers.api;

import com.darro_tech.revengproject.controllers.AuthenticationController;
import com.darro_tech.revengproject.dto.FarmVolumeData;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.ChartService;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.FarmService;
import com.darro_tech.revengproject.services.UserRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChartApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChartService chartService;

    @Mock
    private AuthenticationController authenticationController;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private CompanyService companyService;

    @Mock
    private FarmService farmService;

    @InjectMocks
    private ChartApiController chartApiController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock AuthenticationController behavior
        when(authenticationController.getUserFromSession(any(HttpSession.class))).thenReturn(null);

        // Mock UserRoleService behavior
        when(userRoleService.isAdmin(any(User.class))).thenReturn(false);
        when(userRoleService.isSuperAdmin(any(User.class))).thenReturn(false);

        // Set up MockMvc with controller
        mockMvc = MockMvcBuilders.standaloneSetup(chartApiController).build();
    }

    @Test
    void getFarmVolumesForDate_ShouldReturnFarmVolumeData() throws Exception {
        // Given
        List<FarmVolumeData> mockData = new ArrayList<>();

        FarmVolumeData farm1 = new FarmVolumeData();
        farm1.setFarmId("1");
        farm1.setFarmName("Test Farm 1");
        farm1.setVolume(1000.0);
        mockData.add(farm1);

        FarmVolumeData farm2 = new FarmVolumeData();
        farm2.setFarmId("2");
        farm2.setFarmName("Test Farm 2");
        farm2.setVolume(2000.0);
        mockData.add(farm2);

        when(chartService.getDailyVolumeByFarmForDate(anyString(), any(LocalDate.class)))
                .thenReturn(mockData);

        // When & Then
        mockMvc.perform(post("/api/charts/multi-farm/farm-volumes-for-date")
                .param("company_id", "1")
                .param("date", "2025-05-30")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].farmId").value("1"))
                .andExpect(jsonPath("$.data[0].farmName").value("Test Farm 1"))
                .andExpect(jsonPath("$.data[0].volume").value(1000.0))
                .andExpect(jsonPath("$.data[1].farmId").value("2"))
                .andExpect(jsonPath("$.data[1].farmName").value("Test Farm 2"))
                .andExpect(jsonPath("$.data[1].volume").value(2000.0));
    }

    @Test
    void getFarmVolumesForDate_WhenServiceThrowsException_ShouldReturnErrorResponse() throws Exception {
        // Given
        when(chartService.getDailyVolumeByFarmForDate(anyString(), any(LocalDate.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // When & Then
        mockMvc.perform(post("/api/charts/multi-farm/farm-volumes-for-date")
                .param("company_id", "1")
                .param("date", "2025-05-30")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.errorMessage").value("Test exception"));
    }

    @Test
    void getFarmVolumesForDate_WhenNoData_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(chartService.getDailyVolumeByFarmForDate(anyString(), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(post("/api/charts/multi-farm/farm-volumes-for-date")
                .param("company_id", "1")
                .param("date", "2025-05-30")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
