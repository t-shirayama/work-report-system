package com.example.workreport.dto;

public class DashboardActivityDto {

    private String activityAt;

    private String activityType;

    private String activityTypeName;

    private String badgeClass;

    private String content;

    private String employeeName;

    public String getActivityAt() {
        return activityAt;
    }

    public void setActivityAt(String activityAt) {
        this.activityAt = activityAt;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getActivityTypeName() {
        return activityTypeName;
    }

    public void setActivityTypeName(String activityTypeName) {
        this.activityTypeName = activityTypeName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public void setBadgeClass(String badgeClass) {
        this.badgeClass = badgeClass;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
}
