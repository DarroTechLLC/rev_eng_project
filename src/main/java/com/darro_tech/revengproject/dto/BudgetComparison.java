package com.darro_tech.revengproject.dto;

/**
 * Data Transfer Object for budget comparison data used in the weekly report.
 */
public class BudgetComparison {
    private Double weeklyActual = 0.0;
    private Double weeklyBudget = 0.0;
    private Double weeklyVariance = 0.0;
    private Double weeklyVariancePercent = 0.0;
    private Double weeklyForecast = 0.0;
    
    private Double monthlyActual = 0.0;
    private Double monthlyBudget = 0.0;
    private Double monthlyVariance = 0.0;
    private Double monthlyVariancePercent = 0.0;
    private Double monthlyForecast = 0.0;
    
    private Double yearlyActual = 0.0;
    private Double yearlyBudget = 0.0;
    private Double yearlyVariance = 0.0;
    private Double yearlyVariancePercent = 0.0;
    private Double yearlyForecast = 0.0;
    
    // Default constructor
    public BudgetComparison() {
        // Initialize with default values
    }
    
    // Getters and setters
    public Double getWeeklyActual() {
        return weeklyActual;
    }
    
    public void setWeeklyActual(Double weeklyActual) {
        this.weeklyActual = weeklyActual;
    }
    
    public Double getWeeklyBudget() {
        return weeklyBudget;
    }
    
    public void setWeeklyBudget(Double weeklyBudget) {
        this.weeklyBudget = weeklyBudget;
    }
    
    public Double getWeeklyVariance() {
        return weeklyVariance;
    }
    
    public void setWeeklyVariance(Double weeklyVariance) {
        this.weeklyVariance = weeklyVariance;
    }
    
    public Double getWeeklyVariancePercent() {
        return weeklyVariancePercent;
    }
    
    public void setWeeklyVariancePercent(Double weeklyVariancePercent) {
        this.weeklyVariancePercent = weeklyVariancePercent;
    }
    
    public Double getWeeklyForecast() {
        return weeklyForecast;
    }
    
    public void setWeeklyForecast(Double weeklyForecast) {
        this.weeklyForecast = weeklyForecast;
    }
    
    public Double getMonthlyActual() {
        return monthlyActual;
    }
    
    public void setMonthlyActual(Double monthlyActual) {
        this.monthlyActual = monthlyActual;
    }
    
    public Double getMonthlyBudget() {
        return monthlyBudget;
    }
    
    public void setMonthlyBudget(Double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }
    
    public Double getMonthlyVariance() {
        return monthlyVariance;
    }
    
    public void setMonthlyVariance(Double monthlyVariance) {
        this.monthlyVariance = monthlyVariance;
    }
    
    public Double getMonthlyVariancePercent() {
        return monthlyVariancePercent;
    }
    
    public void setMonthlyVariancePercent(Double monthlyVariancePercent) {
        this.monthlyVariancePercent = monthlyVariancePercent;
    }
    
    public Double getMonthlyForecast() {
        return monthlyForecast;
    }
    
    public void setMonthlyForecast(Double monthlyForecast) {
        this.monthlyForecast = monthlyForecast;
    }
    
    public Double getYearlyActual() {
        return yearlyActual;
    }
    
    public void setYearlyActual(Double yearlyActual) {
        this.yearlyActual = yearlyActual;
    }
    
    public Double getYearlyBudget() {
        return yearlyBudget;
    }
    
    public void setYearlyBudget(Double yearlyBudget) {
        this.yearlyBudget = yearlyBudget;
    }
    
    public Double getYearlyVariance() {
        return yearlyVariance;
    }
    
    public void setYearlyVariance(Double yearlyVariance) {
        this.yearlyVariance = yearlyVariance;
    }
    
    public Double getYearlyVariancePercent() {
        return yearlyVariancePercent;
    }
    
    public void setYearlyVariancePercent(Double yearlyVariancePercent) {
        this.yearlyVariancePercent = yearlyVariancePercent;
    }
    
    public Double getYearlyForecast() {
        return yearlyForecast;
    }
    
    public void setYearlyForecast(Double yearlyForecast) {
        this.yearlyForecast = yearlyForecast;
    }
}