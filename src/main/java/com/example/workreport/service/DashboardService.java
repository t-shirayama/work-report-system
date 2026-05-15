package com.example.workreport.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.workreport.dao.DashboardDao;
import com.example.workreport.dto.DashboardDto;

@Service
public class DashboardService {

    private final DashboardDao dashboardDao;

    @Autowired
    public DashboardService(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    public DashboardDto getDashboard() {
        DashboardDto dashboard = new DashboardDto();
        dashboard.setTodayWorkReportCount(dashboardDao.countTodayWorkReports());
        dashboard.setCurrentMonthTotalHours(dashboardDao.sumCurrentMonthWorkHours());
        dashboard.setNotOutputMonthlyReportCount(dashboardDao.countNotOutputMonthlyReports());
        dashboard.setRecentActivities(dashboardDao.findRecentActivities());
        return dashboard;
    }
}
