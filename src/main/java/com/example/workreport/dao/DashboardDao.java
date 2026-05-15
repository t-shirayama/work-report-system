package com.example.workreport.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.workreport.dto.DashboardActivityDto;
import com.example.workreport.util.SqlFileLoader;

@Repository
public class DashboardDao {

    private static final String COUNT_TODAY_WORK_REPORTS =
            "SELECT COUNT(*) "
                    + "FROM work_reports "
                    + "WHERE TRUNC(created_at) = TRUNC(SYSDATE)";

    private static final String SUM_CURRENT_MONTH_WORK_HOURS =
            "SELECT NVL(SUM(work_hours), 0) "
                    + "FROM work_reports "
                    + "WHERE work_date >= TRUNC(SYSDATE, 'MM') "
                    + "  AND work_date < ADD_MONTHS(TRUNC(SYSDATE, 'MM'), 1)";

    private static final String COUNT_NOT_OUTPUT_MONTHLY_REPORTS =
            SqlFileLoader.load("sql/dao/dashboard/count-not-output-monthly-reports.sql");

    private static final String SELECT_RECENT_ACTIVITIES =
            SqlFileLoader.load("sql/dao/dashboard/select-recent-activities.sql");

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DashboardDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public int countTodayWorkReports() {
        return namedParameterJdbcTemplate.queryForObject(
                COUNT_TODAY_WORK_REPORTS,
                new MapSqlParameterSource(),
                Integer.class);
    }

    public BigDecimal sumCurrentMonthWorkHours() {
        BigDecimal value = namedParameterJdbcTemplate.queryForObject(
                SUM_CURRENT_MONTH_WORK_HOURS,
                new MapSqlParameterSource(),
                BigDecimal.class);
        return value == null ? BigDecimal.ZERO : value;
    }

    public int countNotOutputMonthlyReports() {
        return namedParameterJdbcTemplate.queryForObject(
                COUNT_NOT_OUTPUT_MONTHLY_REPORTS,
                new MapSqlParameterSource(),
                Integer.class);
    }

    public List<DashboardActivityDto> findRecentActivities() {
        return namedParameterJdbcTemplate.query(
                SELECT_RECENT_ACTIVITIES,
                new MapSqlParameterSource(),
                new DashboardActivityRowMapper());
    }

    private static class DashboardActivityRowMapper implements RowMapper<DashboardActivityDto> {

        public DashboardActivityDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            DashboardActivityDto dto = new DashboardActivityDto();
            dto.setActivityAt(rs.getString("activity_at"));
            dto.setActivityType(rs.getString("activity_type"));
            dto.setActivityTypeName(rs.getString("activity_type_name"));
            dto.setBadgeClass(rs.getString("badge_class"));
            dto.setContent(rs.getString("content"));
            dto.setEmployeeName(rs.getString("employee_name"));
            return dto;
        }
    }
}
