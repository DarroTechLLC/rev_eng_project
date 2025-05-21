package com.darro_tech.revengproject.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.darro_tech.revengproject.models.WebsiteAlert;

@Repository
public interface WebsiteAlertRepository extends JpaRepository<WebsiteAlert, Integer> {

    /**
     * Find all active alerts that are associated with a specific company ID or
     * 'all' companies
     *
     * @param companyId The company ID to find alerts for
     * @return List of active alerts for the company
     */
    @Query(value = "SELECT * FROM website_alerts w WHERE w.is_active = true AND "
            + "(JSON_CONTAINS(w.company_ids, '\"all\"') = 1 OR "
            + "JSON_SEARCH(w.company_ids, 'one', :companyId) IS NOT NULL)",
            nativeQuery = true)
    List<WebsiteAlert> findActiveAlertsByCompanyId(String companyId);

    /**
     * Find all active alerts
     *
     * @return List of all active website alerts
     */
    List<WebsiteAlert> findByIsActiveTrue();
}
