package com.darro_tech.revengproject.repositories;

import com.darro_tech.revengproject.models.User;
import com.darro_tech.revengproject.models.UserContactInfo;
import com.darro_tech.revengproject.models.UserContactType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserContactInfoRepository extends JpaRepository<UserContactInfo, Integer> {
    UserContactInfo findByUserAndType(User user, UserContactType type);
} 