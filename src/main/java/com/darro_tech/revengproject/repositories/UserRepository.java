package com.darro_tech.revengproject.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.User;

/**
 * Repository for User entities
 */
@Repository
public interface UserRepository extends CrudRepository<User, String> {

    List<User> findAll();

    User findByUsername(String username);

    Optional<User> findById(String id);
}
