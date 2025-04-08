package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by Chris Bay
 */
public interface UserRepository extends CrudRepository<User, String> {
    List<User> findAll();
    User findByUsername(String username);
    //User findById(String id);


}
