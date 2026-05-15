package com.example.workreport.service;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.example.workreport.dao.DashboardDao;
import com.example.workreport.dto.DashboardActivityDto;
import com.example.workreport.dto.DashboardDto;

public class DashboardServiceTest {

    @Test
    public void getDashboardAggregatesDaoValues() {
        DashboardService service = new DashboardService(new StubDashboardDao());

        DashboardDto dashboard = service.getDashboard();

        assertEquals(3, dashboard.getTodayWorkReportCount());
        assertEquals(new BigDecimal("12.5"), dashboard.getCurrentMonthTotalHours());
        assertEquals(2, dashboard.getNotOutputMonthlyReportCount());
        assertEquals(1, dashboard.getRecentActivities().size());
        assertEquals("登録", dashboard.getRecentActivities().get(0).getActivityTypeName());
    }

    private static class StubDashboardDao extends DashboardDao {

        StubDashboardDao() {
            super(null);
        }

        @Override
        public int countTodayWorkReports() {
            return 3;
        }

        @Override
        public BigDecimal sumCurrentMonthWorkHours() {
            return new BigDecimal("12.5");
        }

        @Override
        public int countNotOutputMonthlyReports() {
            return 2;
        }

        @Override
        public List<DashboardActivityDto> findRecentActivities() {
            DashboardActivityDto activity = new DashboardActivityDto();
            activity.setActivityTypeName("登録");
            return Arrays.asList(activity);
        }
    }
}
