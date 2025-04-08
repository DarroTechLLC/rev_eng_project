package com.darro_tech.revengproject.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    // Use JPQL query instead of method naming convention
    @Query("SELECT ur FROM UserRole ur WHERE ur.user = :user")
    List<UserRole> findByUser(@Param("user") User user);
    
    // Alternative method that uses user ID directly
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId")
    List<UserRole> findByUserId(@Param("userId") String userId);

    // Add custom queries if needed
} 