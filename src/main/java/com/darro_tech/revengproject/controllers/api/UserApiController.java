package com.darro_tech.revengproject.controllers.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.darro_tech.revengproject.models.dto.UserManagementDTO;
import com.darro_tech.revengproject.repositories.CompanyRepository;
import com.darro_tech.revengproject.repositories.RoleRepository;
import com.darro_tech.revengproject.services.UserManagementServiceInterface;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private static final Logger logger = Logger.getLogger(UserApiController.class.getName());

    @Autowired
    private UserManagementServiceInterface userManagementService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String id) {
        logger.info("🔍 Fetching user details for ID: " + id);

        try {
            UserManagementDTO user = userManagementService.getUserManagementDTOById(id);

            if (user == null) {
                logger.warning("⚠️ User not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }

            // Transform roles and companies into the format expected by the frontend
            // Keep existing roleIds and companyIds format for backward compatibility
            List<String> roleIds = user.getRoles().stream()
                .map(r -> r.getId())
                .collect(Collectors.toList());

            List<String> companyIds = user.getCompanies().stream()
                .map(c -> c.getId())
                .collect(Collectors.toList());

            // Include role and company objects for the dropdown display
            List<Map<String, String>> roleDropdownData = user.getRoles().stream()
                .map(r -> {
                    Map<String, String> roleMap = new HashMap<>();
                    roleMap.put("value", r.getId());
                    roleMap.put("label", r.getName());
                    return roleMap;
                })
                .collect(Collectors.toList());

            List<Map<String, String>> companyDropdownData = user.getCompanies().stream()
                .map(c -> {
                    Map<String, String> companyMap = new HashMap<>();
                    companyMap.put("value", c.getId());
                    companyMap.put("label", c.getName());
                    return companyMap;
                })
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("email", user.getEmail());
            response.put("roleIds", roleIds);
            response.put("companyIds", companyIds);
            response.put("roleDropdownData", roleDropdownData);
            response.put("companyDropdownData", companyDropdownData);

            logger.info("✅ Successfully fetched user details for ID: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("❌ Error fetching user details: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sort) {

        logger.info("🔍 Fetching paginated users. Page: " + page + ", Size: " + size + ", Sort: " + sort);

        try {
            // Create pageable object with sorting
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort));

            // Get paginated users
            Page<UserManagementDTO> userPage = userManagementService.getUsersWithRolesAndCompaniesPaginated(pageable);

            // Transform data for frontend
            List<Map<String, Object>> users = userPage.getContent().stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("firstName", user.getFirstName());
                    userMap.put("lastName", user.getLastName());
                    userMap.put("email", user.getEmail());

                    // Transform roles to simple format
                    List<Map<String, String>> roles = user.getRoles().stream()
                        .map(role -> {
                            Map<String, String> roleMap = new HashMap<>();
                            roleMap.put("id", role.getId());
                            roleMap.put("name", role.getName());
                            return roleMap;
                        })
                        .collect(Collectors.toList());
                    userMap.put("roles", roles);

                    // Transform companies to simple format
                    List<Map<String, String>> companies = user.getCompanies().stream()
                        .map(company -> {
                            Map<String, String> companyMap = new HashMap<>();
                            companyMap.put("id", company.getId());
                            companyMap.put("name", company.getName());
                            return companyMap;
                        })
                        .collect(Collectors.toList());
                    userMap.put("companies", companies);

                    return userMap;
                })
                .collect(Collectors.toList());

            // Create response with pagination metadata
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("currentPage", userPage.getNumber());
            response.put("totalItems", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());

            logger.info("✅ Successfully fetched " + users.size() + " users (page " + page + " of " + userPage.getTotalPages() + ")");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("❌ Error fetching paginated users: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Map<String, String>>> getRoles() {
        logger.info("🔍 Fetching roles for dropdown select");

        try {
            // Get all roles directly from the repository
            List<Map<String, String>> roles = roleRepository.findAll().stream()
                .map(role -> {
                    Map<String, String> roleMap = new HashMap<>();
                    roleMap.put("value", role.getId());
                    roleMap.put("label", role.getName());
                    return roleMap;
                })
                .collect(Collectors.toList());

            logger.info("✅ Successfully fetched " + roles.size() + " roles");
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            logger.severe("❌ Error fetching roles: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/companies")
    public ResponseEntity<List<Map<String, String>>> getCompanies() {
        logger.info("🔍 Fetching companies for dropdown select");

        try {
            // Get all companies from the repository
            List<Map<String, String>> companies = companyRepository.findAll().stream()
                .map(company -> {
                    Map<String, String> companyMap = new HashMap<>();
                    companyMap.put("value", company.getId());
                    companyMap.put("label", company.getName());
                    return companyMap;
                })
                .collect(Collectors.toList());

            logger.info("✅ Successfully fetched " + companies.size() + " companies");
            return ResponseEntity.ok(companies);
        } catch (Exception e) {
            logger.severe("❌ Error fetching companies: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName") String sort) {

        logger.info("🔍 Searching users with query: " + query + ", Page: " + page + ", Size: " + size);

        try {
            // Create pageable object with sorting
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort));

            // Search users
            Page<UserManagementDTO> userPage = userManagementService.searchUsers(query, pageable);

            // Transform data for frontend
            List<Map<String, Object>> users = userPage.getContent().stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("firstName", user.getFirstName());
                    userMap.put("lastName", user.getLastName());
                    userMap.put("email", user.getEmail());

                    // Transform roles to simple format
                    List<Map<String, String>> roles = user.getRoles().stream()
                        .map(role -> {
                            Map<String, String> roleMap = new HashMap<>();
                            roleMap.put("id", role.getId());
                            roleMap.put("name", role.getName());
                            return roleMap;
                        })
                        .collect(Collectors.toList());
                    userMap.put("roles", roles);

                    // Transform companies to simple format
                    List<Map<String, String>> companies = user.getCompanies().stream()
                        .map(company -> {
                            Map<String, String> companyMap = new HashMap<>();
                            companyMap.put("id", company.getId());
                            companyMap.put("name", company.getName());
                            return companyMap;
                        })
                        .collect(Collectors.toList());
                    userMap.put("companies", companies);

                    return userMap;
                })
                .collect(Collectors.toList());

            // Create response with pagination metadata
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("currentPage", userPage.getNumber());
            response.put("totalItems", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());

            logger.info("✅ Successfully found " + users.size() + " users matching query: " + query);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("❌ Error searching users: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/alphabet")
    public ResponseEntity<List<String>> getAlphabetLetters() {
        logger.info("🔍 Fetching alphabet letters for pagination");

        try {
            List<String> letters = userManagementService.getDistinctUsernameFirstLetters();
            logger.info("✅ Successfully fetched " + letters.size() + " alphabet letters");
            return ResponseEntity.ok(letters);
        } catch (Exception e) {
            logger.severe("❌ Error fetching alphabet letters: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-letter")
    public ResponseEntity<Map<String, Object>> getUsersByLetter(
            @RequestParam String letter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sort) {

        logger.info("🔍 Fetching users with username starting with: " + letter + ", Page: " + page + ", Size: " + size);

        try {
            // Create pageable object with sorting
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort));

            // Get users by letter
            Page<UserManagementDTO> userPage = userManagementService.getUsersByUsernameStartingWith(letter, pageable);

            // Transform data for frontend
            List<Map<String, Object>> users = userPage.getContent().stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("firstName", user.getFirstName());
                    userMap.put("lastName", user.getLastName());
                    userMap.put("email", user.getEmail());

                    // Transform roles to simple format
                    List<Map<String, String>> roles = user.getRoles().stream()
                        .map(role -> {
                            Map<String, String> roleMap = new HashMap<>();
                            roleMap.put("id", role.getId());
                            roleMap.put("name", role.getName());
                            return roleMap;
                        })
                        .collect(Collectors.toList());
                    userMap.put("roles", roles);

                    // Transform companies to simple format
                    List<Map<String, String>> companies = user.getCompanies().stream()
                        .map(company -> {
                            Map<String, String> companyMap = new HashMap<>();
                            companyMap.put("id", company.getId());
                            companyMap.put("name", company.getName());
                            return companyMap;
                        })
                        .collect(Collectors.toList());
                    userMap.put("companies", companies);

                    return userMap;
                })
                .collect(Collectors.toList());

            // Create response with pagination metadata
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("currentPage", userPage.getNumber());
            response.put("totalItems", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());
            response.put("letter", letter);

            logger.info("✅ Successfully fetched " + users.size() + " users with last name starting with: " + letter);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("❌ Error fetching users by letter: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody Map<String, Object> userData) {
        logger.info("🔄 Updating user with ID: " + userData.get("id"));

        try {
            // Call service method to update user
            boolean success = userManagementService.updateUser(userData);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                logger.info("✅ Successfully updated user with ID: " + userData.get("id"));
                response.put("success", true);
                response.put("message", "User updated successfully");
                return ResponseEntity.ok(response);
            } else {
                logger.warning("⚠️ Failed to update user with ID: " + userData.get("id"));
                response.put("success", false);
                response.put("error", "Failed to update user");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.severe("❌ Error updating user: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String id) {
        logger.info("❌ Deleting user with ID: " + id);

        try {
            boolean success = userManagementService.deleteUser(id);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                logger.info("✅ Successfully deleted user with ID: " + id);
                response.put("success", true);
                response.put("message", "User deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                logger.warning("⚠️ Failed to delete user with ID: " + id);
                response.put("success", false);
                response.put("error", "Failed to delete user");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.severe("❌ Error deleting user: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Send a password reset link to a user
     * 
     * @param userId the ID of the user to send the password reset link to
     * @return a response indicating success or failure
     */
    @PostMapping("/send-password-link")
    public ResponseEntity<Map<String, Object>> sendPasswordResetLink(@RequestParam String userId) {
        logger.info("📧 Sending password reset link to user with ID: " + userId);

        try {
            boolean success = userManagementService.sendPasswordResetLink(userId);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                logger.info("✅ Successfully sent password reset link to user with ID: " + userId);
                response.put("success", true);
                response.put("message", "Password reset link sent successfully");
                return ResponseEntity.ok(response);
            } else {
                logger.warning("⚠️ Failed to send password reset link to user with ID: " + userId);
                response.put("success", false);
                response.put("error", "Failed to send password reset link");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.severe("❌ Error sending password reset link: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 
