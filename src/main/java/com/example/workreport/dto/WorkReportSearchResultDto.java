package com.example.workreport.dto;

import java.math.BigDecimal;

public class WorkReportSearchResultDto {

    private String workDate;

    private String employeeName;

    private String departmentName;

    private String projectName;

    private String workCategory;

    private String workCategoryName;

    private BigDecimal workHours;

    private String workContent;

    public String getWorkDate() {
        return workDate;
    }

    public void setWorkDate(String workDate) {
        this.workDate = workDate;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getWorkCategory() {
        return workCategory;
    }

    public void setWorkCategory(String workCategory) {
        this.workCategory = workCategory;
    }

    public String getWorkCategoryName() {
        return workCategoryName;
    }

    public void setWorkCategoryName(String workCategoryName) {
        this.workCategoryName = workCategoryName;
    }

    public BigDecimal getWorkHours() {
        return workHours;
    }

    public void setWorkHours(BigDecimal workHours) {
        this.workHours = workHours;
    }

    public String getWorkContent() {
        return workContent;
    }

    public void setWorkContent(String workContent) {
        this.workContent = workContent;
    }
}
