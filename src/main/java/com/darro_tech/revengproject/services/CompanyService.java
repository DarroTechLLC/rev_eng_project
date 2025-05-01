package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.dto.CompanyDTO;
import com.darro_tech.revengproject.repositories.CompanyRepository;

@Service
public class CompanyService {
    private static final Logger logger = Logger.getLogger(CompanyService.class.getName());

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    /**
     * Get all companies for a user based on their admin status
     * @param userId User ID
     * @param isSuperAdmin Whether the user is a super admin
     * @return List of CompanyDTO objects
     */
    public List<CompanyDTO> getUserCompanies(String userId, boolean isSuperAdmin) {
        logger.info("Fetching companies for user: " + userId + " (superAdmin: " + isSuperAdmin + ")");

        try {
            if (isSuperAdmin) {
                return jdbcTemplate.query(
                    "SELECT id as company_id, name as company_name, display_name, logo_url " +
                    "FROM companies ORDER BY name",
                    (rs, rowNum) -> new CompanyDTO(
                        rs.getString("company_id"),
                        rs.getString("company_name"),
                        rs.getString("display_name"),
                        rs.getString("logo_url")
                    )
                );
            } else {
                return jdbcTemplate.query(
                    "SELECT company_id, company_name, display_name, logo_url " +
                    "FROM user_company_view WHERE user_id = ? ORDER BY company_name",
                    (rs, rowNum) -> new CompanyDTO(
                        rs.getString("company_id"),
                        rs.getString("company_name"),
                        rs.getString("display_name"),
                        rs.getString("logo_url")
                    ),
                    userId
                );
            }
        } catch (Exception e) {
            logger.severe("Error fetching companies: " + e.getMessage());
            throw new RuntimeException("Failed to fetch companies", e);
        }
    }

    /**
     * Check if a user has access to a specific company
     * @param userId User ID
     * @param companyId Company ID
     * @return true if user has access, false otherwise
     */
    public boolean userHasCompanyAccess(String userId, String companyId) {
        logger.info("Checking company access for user: " + userId + ", company: " + companyId);

        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM company_users WHERE user_id = ? AND company_id = ?",
                Integer.class,
                userId, companyId
            );
            return count != null && count > 0;
        } catch (Exception e) {
            logger.severe("Error checking company access: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get company by ID
     * @param companyId Company ID
     * @return Optional containing Company if found
     */
    public Optional<Company> getCompanyById(String companyId) {
        return companyRepository.findById(companyId);
    }

    @Transactional
    public Company createCompany(String name, String displayName) {
        Company company = new Company();
        company.setId(UUID.randomUUID().toString());
        company.setName(name);
        company.setDisplayName(displayName);
        company.setTimestamp(Instant.now());
        return companyRepository.save(company);
    }

    @Transactional
    public Company updateCompany(String id, String name, String displayName) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setName(name);
        company.setDisplayName(displayName);
        company.setTimestamp(Instant.now());
        return companyRepository.save(company);
    }

    @Transactional
    public void updateLogoUrl(String companyId, String logoUrl) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setLogoUrl(logoUrl);
        companyRepository.save(company);
    }

    @Transactional
    public void removeLogoUrl(String companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setLogoUrl(null);
        companyRepository.save(company);
    }

    @Transactional
    public void deleteCompany(String companyId) {
        // Check if company exists first
        if (companyRepository.existsById(companyId)) {
            companyRepository.deleteById(companyId);
        } else {
            throw new RuntimeException("Company not found");
        }
    }
}
