package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.dto.CompanyDTO;
import com.darro_tech.revengproject.repositories.CompanyRepository;

@Service
public class CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyService.class);

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    /**
     * Get all companies for a user based on their admin status
     *
     * @param userId User ID
     * @param isSuperAdmin Whether the user is a super admin
     * @return List of CompanyDTO objects
     */
    public List<CompanyDTO> getUserCompanies(String userId, boolean isSuperAdmin) {
        logger.info("Fetching companies for user: {} (superAdmin: {})", userId, isSuperAdmin);

        try {
            if (isSuperAdmin) {
                return jdbcTemplate.query(
                        "SELECT id as company_id, name as company_name, display_name, logo_url "
                        + "FROM companies ORDER BY name",
                        (rs, rowNum) -> new CompanyDTO(
                                rs.getString("company_id"),
                                rs.getString("company_name"),
                                rs.getString("display_name"),
                                rs.getString("logo_url")
                        )
                );
            } else {
                return jdbcTemplate.query(
                        "SELECT company_id, company_name, display_name, logo_url "
                        + "FROM user_company_view WHERE user_id = ? ORDER BY company_name",
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
            logger.error("Error fetching companies: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch companies", e);
        }
    }

    /**
     * Check if a user has access to a specific company
     *
     * @param userId User ID
     * @param companyId Company ID
     * @return true if user has access, false otherwise
     */
    public boolean userHasCompanyAccess(String userId, String companyId) {
        logger.info("Checking company access for user: {}, company: {}", userId, companyId);

        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM company_users WHERE user_id = ? AND company_id = ?",
                    Integer.class,
                    userId, companyId
            );
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Error checking company access: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get company by ID
     *
     * @param companyId Company ID
     * @return Optional containing Company if found
     */
    public Optional<Company> getCompanyById(String companyId) {
        return companyRepository.findById(companyId);
    }

    /**
     * Get company by name (exact match)
     *
     * @param name The company name
     * @return Optional containing Company if found
     */
    public Optional<Company> getCompanyByName(String name) {
        logger.info("🔍 Looking up company by name: {}", name);

        try {
            // Find by exact name match
            return companyRepository.findAll().stream()
                    .filter(company -> company.getName() != null && company.getName().equalsIgnoreCase(name))
                    .findFirst();
        } catch (Exception e) {
            logger.error("❌ Error finding company by name: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get company by URL-friendly key (slug)
     *
     * @param key The URL-friendly version of the company name
     * @return Optional containing Company if found
     */
    public Optional<Company> getCompanyByKey(String key) {
        logger.info("Looking up company by key: {}", key);

        try {
            // First try the direct repository method
            Optional<Company> company = companyRepository.findByNameKey(key);

            if (company.isPresent()) {
                return company;
            }

            // As a fallback, try to normalize the key and compare with all companies
            List<Company> allCompanies = companyRepository.findAll();
            return allCompanies.stream()
                    .filter(c -> normalizeCompanyName(c.getName()).equals(key))
                    .findFirst();
        } catch (Exception e) {
            logger.error("Error finding company by key: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Helper method to normalize a company name into a URL-friendly slug
     */
    public String normalizeCompanyName(String name) {
        if (name == null) {
            return "";
        }
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
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
