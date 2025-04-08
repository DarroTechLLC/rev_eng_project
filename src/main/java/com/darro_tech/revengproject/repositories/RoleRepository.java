package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Role findByName(String name);
} 