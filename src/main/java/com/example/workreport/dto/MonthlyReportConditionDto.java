package com.example.workreport.dto;

import java.sql.Date;

public class MonthlyReportConditionDto {

    private String targetYearMonth;

    private Date dateFrom;

    private Date dateTo;

    private Long userId;

    private String departmentName;

    private String employeeName;

    public String getTargetYearMonth() {
        return targetYearMonth;
    }

    public void setTargetYearMonth(String targetYearMonth) {
        this.targetYearMonth = targetYearMonth;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
}
