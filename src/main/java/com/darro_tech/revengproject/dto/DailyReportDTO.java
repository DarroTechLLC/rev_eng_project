package com.darro_tech.revengproject.dto;

import java.time.LocalDate;
import java.util.List;

public class DailyReportDTO {

    private String companyId;
    private LocalDate reportDate;
    private String companyName;
    private List<FarmProduction> dailyProduction;
    private List<FarmProduction> mtdProduction;
    private List<FarmPerformance> farmPerformance;
    private double dailyTotal;
    private double mtdTotal;
    private double ytdTotal;

    // Default constructor
    public DailyReportDTO() {
    }

    // Constructor with all fields
    public DailyReportDTO(String companyId, LocalDate reportDate, String companyName,
            List<FarmProduction> dailyProduction, List<FarmProduction> mtdProduction,
            List<FarmPerformance> farmPerformance, double dailyTotal, double mtdTotal, double ytdTotal) {
        this.companyId = companyId;
        this.reportDate = reportDate;
        this.companyName = companyName;
        this.dailyProduction = dailyProduction;
        this.mtdProduction = mtdProduction;
        this.farmPerformance = farmPerformance;
        this.dailyTotal = dailyTotal;
        this.mtdTotal = mtdTotal;
        this.ytdTotal = ytdTotal;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String companyId;
        private LocalDate reportDate;
        private String companyName;
        private List<FarmProduction> dailyProduction;
        private List<FarmProduction> mtdProduction;
        private List<FarmPerformance> farmPerformance;
        private double dailyTotal;
        private double mtdTotal;
        private double ytdTotal;

        public Builder companyId(String companyId) {
            this.companyId = companyId;
            return this;
        }

        public Builder reportDate(LocalDate reportDate) {
            this.reportDate = reportDate;
            return this;
        }

        public Builder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder dailyProduction(List<FarmProduction> dailyProduction) {
            this.dailyProduction = dailyProduction;
            return this;
        }

        public Builder mtdProduction(List<FarmProduction> mtdProduction) {
            this.mtdProduction = mtdProduction;
            return this;
        }

        public Builder farmPerformance(List<FarmPerformance> farmPerformance) {
            this.farmPerformance = farmPerformance;
            return this;
        }

        public Builder dailyTotal(double dailyTotal) {
            this.dailyTotal = dailyTotal;
            return this;
        }

        public Builder mtdTotal(double mtdTotal) {
            this.mtdTotal = mtdTotal;
            return this;
        }

        public Builder ytdTotal(double ytdTotal) {
            this.ytdTotal = ytdTotal;
            return this;
        }

        public DailyReportDTO build() {
            return new DailyReportDTO(companyId, reportDate, companyName, dailyProduction, mtdProduction, farmPerformance, dailyTotal, mtdTotal, ytdTotal);
        }
    }

    // Getters and setters
    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public List<FarmProduction> getDailyProduction() {
        return dailyProduction;
    }

    public void setDailyProduction(List<FarmProduction> dailyProduction) {
        this.dailyProduction = dailyProduction;
    }

    public List<FarmProduction> getMtdProduction() {
        return mtdProduction;
    }

    public void setMtdProduction(List<FarmProduction> mtdProduction) {
        this.mtdProduction = mtdProduction;
    }

    public List<FarmPerformance> getFarmPerformance() {
        return farmPerformance;
    }

    public void setFarmPerformance(List<FarmPerformance> farmPerformance) {
        this.farmPerformance = farmPerformance;
    }

    public double getDailyTotal() {
        return dailyTotal;
    }

    public void setDailyTotal(double dailyTotal) {
        this.dailyTotal = dailyTotal;
    }

    public double getMtdTotal() {
        return mtdTotal;
    }

    public void setMtdTotal(double mtdTotal) {
        this.mtdTotal = mtdTotal;
    }

    public double getYtdTotal() {
        return ytdTotal;
    }

    public void setYtdTotal(double ytdTotal) {
        this.ytdTotal = ytdTotal;
    }

    public static class FarmProduction {

        private String farmName;
        private double volume;
        private double percentOfTotal;

        public FarmProduction() {
        }

        public FarmProduction(String farmName, double volume, double percentOfTotal) {
            this.farmName = farmName;
            this.volume = volume;
            this.percentOfTotal = percentOfTotal;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private String farmName;
            private double volume;
            private double percentOfTotal;

            public Builder farmName(String farmName) {
                this.farmName = farmName;
                return this;
            }

            public Builder volume(double volume) {
                this.volume = volume;
                return this;
            }

            public Builder percentOfTotal(double percentOfTotal) {
                this.percentOfTotal = percentOfTotal;
                return this;
            }

            public FarmProduction build() {
                return new FarmProduction(farmName, volume, percentOfTotal);
            }
        }

        public String getFarmName() {
            return farmName;
        }

        public void setFarmName(String farmName) {
            this.farmName = farmName;
        }

        public double getVolume() {
            return volume;
        }

        public void setVolume(double volume) {
            this.volume = volume;
        }

        public double getPercentOfTotal() {
            return percentOfTotal;
        }

        public void setPercentOfTotal(double percentOfTotal) {
            this.percentOfTotal = percentOfTotal;
        }
    }

    public static class FarmPerformance {

        private String farmName;
        private double dailyVolume;
        private double mtdVolume;
        private double ytdVolume;

        public FarmPerformance() {
        }

        public FarmPerformance(String farmName, double dailyVolume, double mtdVolume, double ytdVolume) {
            this.farmName = farmName;
            this.dailyVolume = dailyVolume;
            this.mtdVolume = mtdVolume;
            this.ytdVolume = ytdVolume;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private String farmName;
            private double dailyVolume;
            private double mtdVolume;
            private double ytdVolume;

            public Builder farmName(String farmName) {
                this.farmName = farmName;
                return this;
            }

            public Builder dailyVolume(double dailyVolume) {
                this.dailyVolume = dailyVolume;
                return this;
            }

            public Builder mtdVolume(double mtdVolume) {
                this.mtdVolume = mtdVolume;
                return this;
            }

            public Builder ytdVolume(double ytdVolume) {
                this.ytdVolume = ytdVolume;
                return this;
            }

            public FarmPerformance build() {
                return new FarmPerformance(farmName, dailyVolume, mtdVolume, ytdVolume);
            }
        }

        public String getFarmName() {
            return farmName;
        }

        public void setFarmName(String farmName) {
            this.farmName = farmName;
        }

        public double getDailyVolume() {
            return dailyVolume;
        }

        public void setDailyVolume(double dailyVolume) {
            this.dailyVolume = dailyVolume;
        }

        public double getMtdVolume() {
            return mtdVolume;
        }

        public void setMtdVolume(double mtdVolume) {
            this.mtdVolume = mtdVolume;
        }

        public double getYtdVolume() {
            return ytdVolume;
        }

        public void setYtdVolume(double ytdVolume) {
            this.ytdVolume = ytdVolume;
        }
    }
}
