package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.UserContactType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserContactTypeRepository extends JpaRepository<UserContactType, String> {
    UserContactType findByName(String name);
} 