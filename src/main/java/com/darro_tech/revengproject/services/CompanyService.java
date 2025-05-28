package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.util.Arrays;
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

    // Special company names that need custom handling
    private static final List<String> SPECIAL_COMPANIES = Arrays.asList("tuls", "texhoma", "vendor");

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
        logger.info("🏢 Fetching companies for user: {} (superAdmin: {})", userId, isSuperAdmin);

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
                        "SELECT c.id as company_id, c.name as company_name, c.display_name, c.logo_url "
                        + "FROM companies c "
                        + "INNER JOIN company_users cu ON cu.company_id = c.id "
                        + "WHERE cu.user_id = ? "
                        + "ORDER BY c.name",
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
            logger.error("❌ Error fetching companies: {}", e.getMessage());
            logger.debug("Detailed error:", e); // Add stack trace in debug level
            return List.of(); // Return empty list instead of throwing
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
            return companyRepository.findByNameIgnoreCase(name);
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
        logger.info("🔍 Looking up company by key: {}", key);

        try {
            // First check if we have an exact match by name
            Optional<Company> companyByName = companyRepository.findByNameIgnoreCase(key);
            if (companyByName.isPresent()) {
                logger.info("✅ Found company by exact name match: {}", key);
                return companyByName;
            }

            // Handle special cases
            String lowercaseKey = key.toLowerCase();
            if (SPECIAL_COMPANIES.contains(lowercaseKey)) {
                logger.info("⚠️ Special case handling for company key: {}", key);

                // Try to find all special companies and match by lowercase name
                List<Company> specialNameCompanies = companyRepository.findByNameIgnoreCaseIn(SPECIAL_COMPANIES);
                for (Company company : specialNameCompanies) {
                    if (company.getName().equalsIgnoreCase(key)) {
                        logger.info("✅ Found special company: {}", company.getName());
                        return Optional.of(company);
                    }
                }
            }

            // Look through all companies for more complex key matching
            List<Company> allCompanies = companyRepository.findAll();

            // First try a simple replacement of space with dash in company name
            for (Company company : allCompanies) {
                if (company.getName() != null) {
                    String companyNameLower = company.getName().toLowerCase();
                    String simpleDashKey = companyNameLower.replace(" ", "-");

                    if (simpleDashKey.equals(lowercaseKey)) {
                        logger.info("✅ Found company by simple key match: {} -> {}", company.getName(), key);
                        return Optional.of(company);
                    }
                }
            }

            // Next try to find by our normalizeCompanyName method
            for (Company company : allCompanies) {
                String normalizedName = normalizeCompanyName(company.getName());
                if (normalizedName.equalsIgnoreCase(lowercaseKey)) {
                    logger.info("✅ Found company by normalized name: {} -> {}", company.getName(), key);
                    return Optional.of(company);
                }
            }

            logger.info("❌ No company found with key: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("❌ Error finding company by key: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Find a company by its URL slug
     *
     * @param slug The URL-friendly slug for the company
     * @return The company, or null if not found
     */
    public Company findCompanyBySlug(String slug) {
        try {
            logger.info("🔍 Looking up company by slug: {}", slug);

            // First try direct name matching (with URL decoding)
            Optional<Company> companyByName = getCompanyByName(slug);
            if (companyByName.isPresent()) {
                logger.info("✅ Found company by exact name: {}", slug);
                return companyByName.get();
            }

            // Then try key lookup
            Optional<Company> companyByKey = getCompanyByKey(slug);
            if (companyByKey.isPresent()) {
                logger.info("✅ Found company by key: {}", slug);
                return companyByKey.get();
            }

            logger.info("❌ No company found with slug: {}", slug);
            return null;
        } catch (Exception e) {
            logger.error("💥 Error finding company by slug: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Normalize a company name for URL usage
     *
     * Note: This method is designed to match the NextJS app's formatCompanyKey
     * function which only replaces the FIRST space with a dash, not all spaces.
     */
    public String normalizeCompanyName(String name) {
        if (name == null) {
            return "";
        }

        // First, check for exact matches for known problematic companies
        if (name.equalsIgnoreCase("Tuls")) {
            return "tuls";
        }
        if (name.equalsIgnoreCase("Texhoma")) {
            return "texhoma";
        }
        if (name.equalsIgnoreCase("Vendor")) {
            return "vendor";
        }

        // Replace only the first space with a dash to match NextJS behavior
        String result = name.toLowerCase();
        int firstSpaceIndex = result.indexOf(' ');
        if (firstSpaceIndex >= 0) {
            result = result.substring(0, firstSpaceIndex)
                    + "-"
                    + result.substring(firstSpaceIndex + 1);
        }

        // Log for debugging problematic companies
        logger.debug("🔄 Normalized company name: '{}' -> '{}'", name, result);

        return result;
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
