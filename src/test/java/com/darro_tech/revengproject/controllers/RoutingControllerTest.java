package com.darro_tech.revengproject.controllers;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.services.CompanyService;
import com.darro_tech.revengproject.services.FarmService;
import com.darro_tech.revengproject.services.UserRoleService;

import jakarta.servlet.http.HttpSession;

/**
 * Test for the RoutingController to verify routing patterns work correctly and
 * there are no ambiguous mappings
 */
@WebMvcTest(RoutingController.class)
public class RoutingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private FarmService farmService;

    @MockBean
    private UserRoleService userRoleService;

    @MockBean
    private AuthenticationController authController;

    @MockBean
    private DashboardController dashboardController;

    private User testUser;
    private Company testCompany;
    private Farm testFarm;

    @BeforeEach
    public void setup() {
        // Setup test user
        testUser = new User();
        testUser.setId("user-id-1");
        testUser.setUsername("testuser");

        // Setup test company
        testCompany = new Company();
        testCompany.setId("company-id-1");
        testCompany.setName("Test Company");
        testCompany.setDisplayName("test-company");

        // Setup test farm
        testFarm = new Farm();
        testFarm.setId("farm-id-1");
        testFarm.setName("Test Farm");
        testFarm.setDisplayName("Test Farm");

        // Mock authentication
        when(authController.getUserFromSession(any(HttpSession.class))).thenReturn(testUser);

        // Mock company access
        when(companyService.getCompanyByName(anyString())).thenReturn(Optional.of(testCompany));
        when(companyService.getCompanyByKey(anyString())).thenReturn(Optional.of(testCompany));
        when(companyService.userHasCompanyAccess(anyString(), anyString())).thenReturn(true);
        when(companyService.getUserCompanies(anyString(), eq(true))).thenReturn(new ArrayList<>());
        when(companyService.getUserCompanies(anyString(), eq(false))).thenReturn(new ArrayList<>());

        // Mock farm access
        when(farmService.getFarmByName(anyString(), anyString())).thenReturn(Optional.of(testFarm));
        when(farmService.getFarmById(anyString())).thenReturn(Optional.of(testFarm));

        // Mock user role
        when(userRoleService.isSuperAdmin(any(User.class))).thenReturn(false);
    }

    @Test
    public void testRootRedirect() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    public void testCompanyDashboard() throws Exception {
        mockMvc.perform(get("/test-company/dashboard/daily-volume"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/daily-volume"));
    }

    @Test
    public void testCompanyDashboardRoot() throws Exception {
        mockMvc.perform(get("/test-company/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/test-company/dashboard/daily-volume"));
    }

    @Test
    public void testMtdVolume() throws Exception {
        mockMvc.perform(get("/test-company/mtd-volume"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/mtd-volume"));
    }

    @Test
    public void testYtdVolume() throws Exception {
        mockMvc.perform(get("/test-company/ytd-volume"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/ytd-volume"));
    }

    @Test
    public void testProductionHeadcount() throws Exception {
        mockMvc.perform(get("/test-company/production-headcount"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/production-headcount"));
    }

    @Test
    public void testAnimalHeadcount() throws Exception {
        mockMvc.perform(get("/test-company/animal-headcount"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/animal-headcount"));
    }

    @Test
    public void testWeeklyReport() throws Exception {
        mockMvc.perform(get("/test-company/weekly-report"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/weekly-report"));
    }

    @Test
    public void testDailyReport() throws Exception {
        mockMvc.perform(get("/test-company/daily-report"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/daily-report"));
    }

    @Test
    public void testProductionDetail() throws Exception {
        mockMvc.perform(get("/test-company/production-detail"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/production-detail"));
    }

    @Test
    public void testMarketData() throws Exception {
        mockMvc.perform(get("/test-company/market-data"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/market-data"));
    }

    @Test
    public void testCompanyProjects() throws Exception {
        mockMvc.perform(get("/test-company/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/index"));
    }

    @Test
    public void testFarmProject() throws Exception {
        mockMvc.perform(get("/test-company/projects/test-farm/production"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/production"));
    }

    @Test
    public void testInvalidProjectType() throws Exception {
        mockMvc.perform(get("/test-company/projects/test-farm/invalid-type"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/test-company/projects/test-farm/production"));
    }
}
