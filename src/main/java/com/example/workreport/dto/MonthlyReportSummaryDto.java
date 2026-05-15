package com.example.workreport.dto;

import java.math.BigDecimal;

public class MonthlyReportSummaryDto {

    private String targetYearMonth;

    private String employeeName;

    private String departmentName;

    private BigDecimal totalWorkHours = BigDecimal.ZERO;

    private int workDays;

    private BigDecimal averageWorkHours = BigDecimal.ZERO;

    public String getTargetYearMonth() {
        return targetYearMonth;
    }

    public void setTargetYearMonth(String targetYearMonth) {
        this.targetYearMonth = targetYearMonth;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public BigDecimal getTotalWorkHours() {
        return totalWorkHours;
    }

    public void setTotalWorkHours(BigDecimal totalWorkHours) {
        this.totalWorkHours = totalWorkHours;
    }

    public int getWorkDays() {
        return workDays;
    }

    public void setWorkDays(int workDays) {
        this.workDays = workDays;
    }

    public BigDecimal getAverageWorkHours() {
        return averageWorkHours;
    }

    public void setAverageWorkHours(BigDecimal averageWorkHours) {
        this.averageWorkHours = averageWorkHours;
    }
}
