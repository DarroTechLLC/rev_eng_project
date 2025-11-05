package com.darro_tech.revengproject.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.EmailLogView;

@Repository
public interface EmailLogViewRepository extends JpaRepository<EmailLogView, String> {

    @Query("SELECT e FROM EmailLogView e ORDER BY e.createdAt DESC")
    List<EmailLogView> findAllOrderByCreatedAtDesc(Pageable pageable);

    List<EmailLogView> findAllByOrderByCreatedAtDesc();
}



