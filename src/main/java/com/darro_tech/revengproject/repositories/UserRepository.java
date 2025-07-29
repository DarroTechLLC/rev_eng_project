package com.darro_tech.revengproject.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.User;

/**
 * Repository for User entities
 */
@Repository
public interface UserRepository extends CrudRepository<User, String>, PagingAndSortingRepository<User, String> {

    List<User> findAll();

    Page<User> findAll(Pageable pageable);

    Optional<User> findByUsername(String username);

    Optional<User> findById(String id);

    /**
     * Find users by last name starting with the given letter
     * @param letter the first letter of the last name
     * @param pageable pagination information
     * @return page of users with last name starting with the given letter
     */
    Page<User> findByLastNameStartingWithIgnoreCase(String letter, Pageable pageable);

    /**
     * Find users by username starting with the given letter
     * @param letter the first letter of the username
     * @param pageable pagination information
     * @return page of users with username starting with the given letter
     */
    Page<User> findByUsernameStartingWithIgnoreCase(String letter, Pageable pageable);

    /**
     * Search for users by username, first name, last name, or email
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of users matching the search term
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search for users by username, first name, last name, or email without pagination
     * @param searchTerm the search term
     * @return list of users matching the search term
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsersWithoutPagination(@Param("searchTerm") String searchTerm);
}
