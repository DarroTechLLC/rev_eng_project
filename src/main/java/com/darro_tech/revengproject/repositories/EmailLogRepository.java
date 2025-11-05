package com.darro_tech.revengproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.EmailLog;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, String> {
}



