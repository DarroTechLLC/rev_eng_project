package com.darro_tech.revengproject.services;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.dto.UserDTO;
import com.darro_tech.revengproject.models.dto.UserManagementDTO;
import com.darro_tech.revengproject.models.dto.UserUpdateDTO;

public interface UserManagementServiceInterface {

    /**
     * Get all users with their roles and companies
     * @return list of users with their roles and companies
     */
    List<UserManagementDTO> getAllUsersWithRolesAndCompanies();

    /**
     * Get paginated users with their roles and companies
     * @param pageable pagination information
     * @return page of users with their roles and companies
     */
    Page<UserManagementDTO> getUsersWithRolesAndCompaniesPaginated(Pageable pageable);

    /**
     * Search for users by username, first name, last name, or email
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of users matching the search term
     */
    Page<UserManagementDTO> searchUsers(String searchTerm, Pageable pageable);

    /**
     * Get users with last name starting with the given letter
     * @param letter the first letter of the last name
     * @param pageable pagination information
     * @return page of users with last name starting with the given letter
     */
    Page<UserManagementDTO> getUsersByLastNameStartingWith(String letter, Pageable pageable);

    /**
     * Get users with username starting with the given letter
     * @param letter the first letter of the username
     * @param pageable pagination information
     * @return page of users with username starting with the given letter
     */
    Page<UserManagementDTO> getUsersByUsernameStartingWith(String letter, Pageable pageable);

    /**
     * Get all distinct first letters of user last names
     * @return list of distinct first letters of user last names
     */
    List<String> getDistinctLastNameFirstLetters();

    /**
     * Get all distinct first letters of usernames
     * @return list of distinct first letters of usernames
     */
    List<String> getDistinctUsernameFirstLetters();

    /**
     * Get user by ID (entity)
     * @param id the user ID
     * @return the user entity or null if not found
     */
    User getUserById(String id);

    /**
     * Get user management DTO by ID
     * @param id the user ID
     * @return the user management DTO or null if not found
     */
    UserManagementDTO getUserManagementDTOById(String id);

    /**
     * Get user phone number
     * @return the user's phone number or null if not found
     */
    String getPhone();

    /**
     * Set user phone number
     * @param phone the phone number to set
     */
    void setPhone(String phone);

    /**
     * Get user DTO by ID for API responses
     * @param id the user ID
     * @return the user DTO or null if not found
     */
    UserDTO getUserDTOById(String id);

    /**
     * Creates a new user
     * @param user the user entity
     * @param roleIds list of role IDs
     * @param companyIds list of company IDs
     * @return the created user entity
     */
    User createUser(User user, List<String> roleIds, List<String> companyIds);

    /**
     * Update user details
     * @param userUpdateDTO object containing user update data
     * @return true if update was successful, false otherwise
     */
    boolean updateUser(UserUpdateDTO userUpdateDTO);

    /**
     * Update user details from map
     * @param userData map containing user update data
     * @return true if update was successful, false otherwise
     */
    boolean updateUser(Map<String, Object> userData);

    /**
     * Delete a user
     * @param userId the user ID
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteUser(String userId);

    /**
     * Send password reset link to user
     * @param userId the user ID
     * @return true if the password reset link was sent successfully, false otherwise
     */
    boolean sendPasswordResetLink(String userId);

    /**
     * Set user password manually
     * @param userId the user ID
     * @param newPassword the new password
     * @return true if the password was set successfully, false otherwise
     */
    boolean setUserPassword(String userId, String newPassword);
} 
