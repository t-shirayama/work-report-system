package com.example.workreport.entity;

import java.math.BigDecimal;
import java.util.Date;

public class WorkReport {

    private Long workReportId;

    private Long userId;

    private Long departmentId;

    private Date workDate;

    private String projectName;

    private String workCategory;

    private BigDecimal workHours;

    private String workContent;

    public Long getWorkReportId() {
        return workReportId;
    }

    public void setWorkReportId(Long workReportId) {
        this.workReportId = workReportId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
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
