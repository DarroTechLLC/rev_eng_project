package com.darro_tech.revengproject.models.dto;

import java.util.List;

public class UserManagementDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private List<RoleDTO> roles;
    private List<CompanyDTO> companies;
    
    public static class RoleDTO {
        private String id;
        private String name;
        
        public RoleDTO(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
    }
    
    public static class CompanyDTO {
        private String id;
        private String name;
        
        public CompanyDTO(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public List<RoleDTO> getRoles() {
        return roles;
    }
    
    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }
    
    public List<CompanyDTO> getCompanies() {
        return companies;
    }
    
    public void setCompanies(List<CompanyDTO> companies) {
        this.companies = companies;
    }
} 