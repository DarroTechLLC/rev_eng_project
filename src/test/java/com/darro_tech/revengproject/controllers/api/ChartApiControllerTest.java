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
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"company_id\":\"1\",\"date\":\"2025-05-30\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].farm_id").value("1"))
                .andExpect(jsonPath("$.data[0].farm_name").value("Test Farm 1"))
                .andExpect(jsonPath("$.data[0].volume").value(1000.0))
                .andExpect(jsonPath("$.data[1].farm_id").value("2"))
                .andExpect(jsonPath("$.data[1].farm_name").value("Test Farm 2"))
                .andExpect(jsonPath("$.data[1].volume").value(2000.0));
    }

    @Test
    void getFarmVolumesForDate_WhenServiceThrowsException_ShouldReturnErrorResponse() throws Exception {
        // Given
        when(chartService.getDailyVolumeByFarmForDate(anyString(), any(LocalDate.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // When & Then
        mockMvc.perform(post("/api/charts/multi-farm/farm-volumes-for-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"company_id\":\"1\",\"date\":\"2025-05-30\"}"))
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
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"company_id\":\"1\",\"date\":\"2025-05-30\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getProductionPopulationTimeline_ShouldReturnPopulationData() throws Exception {
        // Given
        List<Map<String, Object>> mockData = new ArrayList<>();

        // Create sample data for Farm 1
        Map<String, Object> farm1Data1 = new java.util.HashMap<>();
        farm1Data1.put("farm_id", "1");
        farm1Data1.put("farm_name", "Test Farm 1");
        farm1Data1.put("timestamp", "2023-01-01");
        farm1Data1.put("population", 2.5);
        mockData.add(farm1Data1);

        Map<String, Object> farm1Data2 = new java.util.HashMap<>();
        farm1Data2.put("farm_id", "1");
        farm1Data2.put("farm_name", "Test Farm 1");
        farm1Data2.put("timestamp", "2023-02-01");
        farm1Data2.put("population", 3.2);
        mockData.add(farm1Data2);

        // Create sample data for Farm 2
        Map<String, Object> farm2Data1 = new java.util.HashMap<>();
        farm2Data1.put("farm_id", "2");
        farm2Data1.put("farm_name", "Test Farm 2");
        farm2Data1.put("timestamp", "2023-01-01");
        farm2Data1.put("population", 1.8);
        mockData.add(farm2Data1);

        when(chartService.getProductionPopulationTimeline(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockData);

        // When & Then
        mockMvc.perform(post("/api/charts/multi-farm/production-population-52week-timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"company_id\":\"1\",\"from\":\"2023-01-01\",\"to\":\"2023-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].farm_id").value("1"))
                .andExpect(jsonPath("$.data[0].farm_name").value("Test Farm 1"))
                .andExpect(jsonPath("$.data[0].timestamp").value("2023-01-01"))
                .andExpect(jsonPath("$.data[0].population").value(2.5))
                .andExpect(jsonPath("$.data[1].farm_id").value("1"))
                .andExpect(jsonPath("$.data[1].timestamp").value("2023-02-01"))
                .andExpect(jsonPath("$.data[2].farm_id").value("2"));
    }

    @Test
    void getProductionPopulationTimeline_WhenServiceThrowsException_ShouldReturnErrorResponse() throws Exception {
        // Given
        when(chartService.getProductionPopulationTimeline(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // When & Then
        mockMvc.perform(post("/api/charts/multi-farm/production-population-52week-timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"company_id\":\"1\",\"from\":\"2023-01-01\",\"to\":\"2023-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.errorMessage").value("Test exception"));
    }

    @Test
    void getProductionPopulationTimeline_WhenNoData_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(chartService.getProductionPopulationTimeline(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(post("/api/charts/multi-farm/production-population-52week-timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"company_id\":\"1\",\"from\":\"2023-01-01\",\"to\":\"2023-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getCompanyPopulationTimeline_ShouldReturnPopulationData() throws Exception {
        // Given
        List<Map<String, Object>> mockData = new ArrayList<>();

        // Create sample data
        Map<String, Object> data1 = new java.util.HashMap<>();
        data1.put("timestamp", "2023-01-01");
        data1.put("value", 1500.0);
        mockData.add(data1);

        Map<String, Object> data2 = new java.util.HashMap<>();
        data2.put("timestamp", "2023-02-01");
        data2.put("value", 1600.0);
        mockData.add(data2);

        when(chartService.getCompanyPopulationTimeline(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockData);

        // When & Then
        mockMvc.perform(post("/api/charts/company/population-timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"company_id\":\"1\",\"from\":\"2023-01-01\",\"to\":\"2023-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].timestamp").value("2023-01-01"))
                .andExpect(jsonPath("$.data[0].value").value(1500.0))
                .andExpect(jsonPath("$.data[1].timestamp").value("2023-02-01"))
                .andExpect(jsonPath("$.data[1].value").value(1600.0));
    }

    @Test
    void getCompanyPopulationTimeline_WhenServiceThrowsException_ShouldReturnErrorResponse() throws Exception {
        // Given
        when(chartService.getCompanyPopulationTimeline(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // When & Then
        mockMvc.perform(post("/api/charts/company/population-timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"company_id\":\"1\",\"from\":\"2023-01-01\",\"to\":\"2023-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.errorMessage").value("Test exception"));
    }

    @Test
    void getCompanyPopulationForecastTimeline_ShouldReturnForecastData() throws Exception {
        // Given
        List<Map<String, Object>> mockData = new ArrayList<>();

        // Create sample data
        Map<String, Object> data1 = new java.util.HashMap<>();
        data1.put("timestamp", "2023-06-01");
        data1.put("value", 1700.0);
        mockData.add(data1);

        Map<String, Object> data2 = new java.util.HashMap<>();
        data2.put("timestamp", "2023-07-01");
        data2.put("value", 1800.0);
        mockData.add(data2);

        when(chartService.getCompanyPopulationForecastTimeline(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockData);

        // When & Then
        mockMvc.perform(post("/api/charts/company/population-forecast-timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"company_id\":\"1\",\"from\":\"2023-06-01\",\"to\":\"2023-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].timestamp").value("2023-06-01"))
                .andExpect(jsonPath("$.data[0].value").value(1700.0))
                .andExpect(jsonPath("$.data[1].timestamp").value("2023-07-01"))
                .andExpect(jsonPath("$.data[1].value").value(1800.0));
    }

    @Test
    void getCompanyPopulationBudgetTimeline_ShouldReturnBudgetData() throws Exception {
        // Given
        List<Map<String, Object>> mockData = new ArrayList<>();

        // Create sample data
        Map<String, Object> data1 = new java.util.HashMap<>();
        data1.put("timestamp", "2023-01-01");
        data1.put("value", 1600.0);
        mockData.add(data1);

        Map<String, Object> data2 = new java.util.HashMap<>();
        data2.put("timestamp", "2023-02-01");
        data2.put("value", 1650.0);
        mockData.add(data2);

        when(chartService.getCompanyPopulationBudgetTimeline(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockData);

        // When & Then
        mockMvc.perform(post("/api/charts/company/population-budget-timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"company_id\":\"1\",\"from\":\"2023-01-01\",\"to\":\"2023-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].timestamp").value("2023-01-01"))
                .andExpect(jsonPath("$.data[0].value").value(1600.0))
                .andExpect(jsonPath("$.data[1].timestamp").value("2023-02-01"))
                .andExpect(jsonPath("$.data[1].value").value(1650.0));
    }

    @Test
    void getMarketPricesMonthlyTimeline_ShouldReturnPricesData() throws Exception {
        // Given
        List<Map<String, Object>> mockData = new ArrayList<>();

        // Create sample data
        Map<String, Object> data1 = new java.util.HashMap<>();
        data1.put("timestamp", "2023-01-01T00:00:00Z");
        data1.put("lcfs", 150.0);
        data1.put("d3", 1.5);
        data1.put("d5", 1.2);
        data1.put("natural_gas", 3.5);
        mockData.add(data1);

        Map<String, Object> data2 = new java.util.HashMap<>();
        data2.put("timestamp", "2023-02-01T00:00:00Z");
        data2.put("lcfs", 155.0);
        data2.put("d3", 1.6);
        data2.put("d5", 1.3);
        data2.put("natural_gas", 3.6);
        mockData.add(data2);

        when(chartService.getMarketPricesMonthlyTimeline(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockData);

        // When & Then
        mockMvc.perform(post("/api/charts/market/market-prices-monthly-timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"from\":\"2023-01-01\",\"to\":\"2023-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].timestamp").value("2023-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.data[0].lcfs").value(150.0))
                .andExpect(jsonPath("$.data[0].d3").value(1.5))
                .andExpect(jsonPath("$.data[0].d5").value(1.2))
                .andExpect(jsonPath("$.data[0].natural_gas").value(3.5))
                .andExpect(jsonPath("$.data[1].timestamp").value("2023-02-01T00:00:00Z"))
                .andExpect(jsonPath("$.data[1].lcfs").value(155.0));
    }

    @Test
    void getMarketPricesDailyTimeline_ShouldReturnPricesData() throws Exception {
        // Given
        List<Map<String, Object>> mockData = new ArrayList<>();

        // Create sample data
        Map<String, Object> data1 = new java.util.HashMap<>();
        data1.put("timestamp", "2023-01-01T00:00:00Z");
        data1.put("lcfs", 150.0);
        data1.put("d3", 1.5);
        data1.put("d5", 1.2);
        data1.put("natural_gas", 3.5);
        mockData.add(data1);

        Map<String, Object> data2 = new java.util.HashMap<>();
        data2.put("timestamp", "2023-01-02T00:00:00Z");
        data2.put("lcfs", 151.0);
        data2.put("d3", 1.55);
        data2.put("d5", 1.25);
        data2.put("natural_gas", 3.55);
        mockData.add(data2);

        when(chartService.getMarketPricesDailyTimeline(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockData);

        // When & Then
        mockMvc.perform(post("/api/charts/market/market-prices-daily-timeline")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"from\":\"2023-01-01\",\"to\":\"2023-01-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].timestamp").value("2023-01-01T00:00:00Z"))
                .andExpect(jsonPath("$.data[0].lcfs").value(150.0))
                .andExpect(jsonPath("$.data[0].d3").value(1.5))
                .andExpect(jsonPath("$.data[0].d5").value(1.2))
                .andExpect(jsonPath("$.data[0].natural_gas").value(3.5))
                .andExpect(jsonPath("$.data[1].timestamp").value("2023-01-02T00:00:00Z"))
                .andExpect(jsonPath("$.data[1].lcfs").value(151.0));
    }
}
