package com.example.workreport.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardDto {

    private int todayWorkReportCount;

    private BigDecimal currentMonthTotalHours = BigDecimal.ZERO;

    private int notOutputMonthlyReportCount;

    private List<DashboardActivityDto> recentActivities = new ArrayList<DashboardActivityDto>();

    public int getTodayWorkReportCount() {
        return todayWorkReportCount;
    }

    public void setTodayWorkReportCount(int todayWorkReportCount) {
        this.todayWorkReportCount = todayWorkReportCount;
    }

    public BigDecimal getCurrentMonthTotalHours() {
        return currentMonthTotalHours;
    }

    public void setCurrentMonthTotalHours(BigDecimal currentMonthTotalHours) {
        this.currentMonthTotalHours = currentMonthTotalHours;
    }

    public int getNotOutputMonthlyReportCount() {
        return notOutputMonthlyReportCount;
    }

    public void setNotOutputMonthlyReportCount(int notOutputMonthlyReportCount) {
        this.notOutputMonthlyReportCount = notOutputMonthlyReportCount;
    }

    public List<DashboardActivityDto> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<DashboardActivityDto> recentActivities) {
        this.recentActivities = recentActivities;
    }
}
