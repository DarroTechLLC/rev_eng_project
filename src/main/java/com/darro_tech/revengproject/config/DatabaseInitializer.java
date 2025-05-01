package com.darro_tech.revengproject.config;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import com.darro_tech.revengproject.models.Company;
import com.darro_tech.revengproject.models.CompanyFarm;
import com.darro_tech.revengproject.models.Farm;
import com.darro_tech.revengproject.repositories.CompanyFarmRepository;
import com.darro_tech.revengproject.repositories.CompanyRepository;
import com.darro_tech.revengproject.repositories.FarmRepository;

@Configuration
public class DatabaseInitializer {

    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private FarmRepository farmRepository;
    
    @Autowired
    private CompanyFarmRepository companyFarmRepository;
    
    @Bean
    @Transactional
    public CommandLineRunner initDatabase() {
        return args -> {
            // Check if we already have data
            if (companyRepository.count() > 0 && farmRepository.count() > 0) {
                System.out.println("Database already initialized, skipping...");
                return;
            }
            
            System.out.println("Initializing database with sample data...");
            
            // Create sample companies
            Company company1 = createCompany("Align", "Align Renewable Natural Gas");
            Company company2 = createCompany("Acme Corp", "Acme Corporation");
            Company company3 = createCompany("FarmTech", "Farm Technology Inc.");
            
            // Create sample farms for Align
            Farm farm1 = createFarm("North Ridge Farm", "North Ridge", "Dairy");
            Farm farm2 = createFarm("Sunset Valley", "Sunset", "Beef");
            Farm farm3 = createFarm("Green Pastures", "Green P", "Dairy");
            
            // Create sample farms for Acme
            Farm farm4 = createFarm("Hilltop Ranch", "Hilltop", "Poultry");
            Farm farm5 = createFarm("River Meadows", "River M", "Swine");
            
            // Create sample farms for FarmTech
            Farm farm6 = createFarm("Oak Grove", "Oak", "Other");
            
            // Associate farms with companies
            assignFarmToCompany(farm1, company1);
            assignFarmToCompany(farm2, company1);
            assignFarmToCompany(farm3, company1);
            
            assignFarmToCompany(farm4, company2);
            assignFarmToCompany(farm5, company2);
            
            assignFarmToCompany(farm6, company3);
            
            System.out.println("Database initialization completed!");
        };
    }
    
    private Company createCompany(String name, String displayName) {
        Company company = new Company();
        company.setId(UUID.randomUUID().toString());
        company.setName(name);
        company.setDisplayName(displayName);
        company.setTimestamp(Instant.now());
        return companyRepository.save(company);
    }
    
    private Farm createFarm(String name, String displayName, String farmType) {
        Farm farm = new Farm();
        farm.setId(UUID.randomUUID().toString());
        farm.setName(name);
        farm.setDisplayName(displayName);
        farm.setFarmType(farmType);
        farm.setIsTempSource(false);
        farm.setTimestamp(Instant.now());
        return farmRepository.save(farm);
    }
    
    private void assignFarmToCompany(Farm farm, Company company) {
        CompanyFarm companyFarm = new CompanyFarm();
        companyFarm.setId(new Random().nextInt(1000000)); // Generate a random integer ID
        companyFarm.setCompany(company);
        companyFarm.setFarm(farm);
        companyFarm.setTimestamp(Instant.now());
        companyFarmRepository.save(companyFarm);
    }
} 