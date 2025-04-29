package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.repositories.CompanyRepository;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Optional<Company> getCompanyById(String id) {
        return companyRepository.findById(id);
    }

    @Transactional
    public Company createCompany(String name, String displayName) {
        Company company = new Company();
        company.setId(UUID.randomUUID().toString());
        company.setName(name);
        company.setDisplayName(displayName);
        company.setTimestamp(Instant.now());
        return companyRepository.save(company);
    }

    @Transactional
    public Company updateCompany(String id, String name, String displayName) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setName(name);
        company.setDisplayName(displayName);
        company.setTimestamp(Instant.now());
        return companyRepository.save(company);
    }

    @Transactional
    public void updateLogoUrl(String companyId, String logoUrl) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setLogoUrl(logoUrl);
        companyRepository.save(company);
    }

    @Transactional
    public void removeLogoUrl(String companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setLogoUrl(null);
        companyRepository.save(company);
    }

    @Transactional
    public void deleteCompany(String companyId) {
        // Check if company exists first
        if (companyRepository.existsById(companyId)) {
            companyRepository.deleteById(companyId);
        } else {
            throw new RuntimeException("Company not found");
        }
    }
}
