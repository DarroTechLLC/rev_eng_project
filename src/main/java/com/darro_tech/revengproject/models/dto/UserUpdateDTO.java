package com.darro_tech.revengproject.models.dto;

import java.util.List;

public class UserUpdateDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private List<String> roleIds;
    private List<String> companyIds;
    private String passOption;
    private String password;
    
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
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public List<String> getRoleIds() {
        return roleIds;
    }
    
    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }
    
    public List<String> getCompanyIds() {
        return companyIds;
    }
    
    public void setCompanyIds(List<String> companyIds) {
        this.companyIds = companyIds;
    }
    
    public String getPassOption() {
        return passOption;
    }
    
    public void setPassOption(String passOption) {
        this.passOption = passOption;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
} 