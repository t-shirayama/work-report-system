package com.example.workreport.dao;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.workreport.dto.MonthlyReportCategorySummaryDto;
import com.example.workreport.dto.MonthlyReportConditionDto;
import com.example.workreport.dto.MonthlyReportDailyDetailDto;
import com.example.workreport.dto.MonthlyReportSummaryDto;
import com.example.workreport.util.SqlFileLoader;

@Repository
public class MonthlyReportDao {

    private static final String FIND_SUMMARY =
            SqlFileLoader.load("sql/dao/monthly-report/find-summary.sql");

    private static final String FIND_CATEGORY_SUMMARIES =
            SqlFileLoader.load("sql/dao/monthly-report/find-category-summaries.sql");

    private static final String FIND_DAILY_DETAILS =
            SqlFileLoader.load("sql/dao/monthly-report/find-daily-details.sql");

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public MonthlyReportDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public MonthlyReportSummaryDto findSummary(MonthlyReportConditionDto condition) {
        return namedParameterJdbcTemplate.queryForObject(FIND_SUMMARY, createParams(condition), new SummaryRowMapper());
    }

    public List<MonthlyReportCategorySummaryDto> findCategorySummaries(MonthlyReportConditionDto condition) {
        return namedParameterJdbcTemplate.query(FIND_CATEGORY_SUMMARIES, createParams(condition), new CategorySummaryRowMapper());
    }

    public List<MonthlyReportDailyDetailDto> findDailyDetails(MonthlyReportConditionDto condition) {
        return namedParameterJdbcTemplate.query(FIND_DAILY_DETAILS, createParams(condition), new DailyDetailRowMapper());
    }

    private MapSqlParameterSource createParams(MonthlyReportConditionDto condition) {
        return new MapSqlParameterSource()
                .addValue("targetYearMonth", condition.getTargetYearMonth())
                .addValue("dateFrom", condition.getDateFrom())
                .addValue("dateTo", condition.getDateTo())
                .addValue("userId", condition.getUserId());
    }

    private static class SummaryRowMapper implements RowMapper<MonthlyReportSummaryDto> {

        public MonthlyReportSummaryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            MonthlyReportSummaryDto dto = new MonthlyReportSummaryDto();
            dto.setTargetYearMonth(rs.getString("target_year_month"));
            dto.setEmployeeName(rs.getString("employee_name"));
            dto.setDepartmentName(rs.getString("department_name"));
            dto.setTotalWorkHours(defaultZero(rs.getBigDecimal("total_work_hours")));
            dto.setWorkDays(rs.getInt("work_days"));
            return dto;
        }
    }

    private static class CategorySummaryRowMapper implements RowMapper<MonthlyReportCategorySummaryDto> {

        public MonthlyReportCategorySummaryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            MonthlyReportCategorySummaryDto dto = new MonthlyReportCategorySummaryDto();
            dto.setWorkCategory(rs.getString("work_category"));
            dto.setWorkCategoryName(rs.getString("work_category_name"));
            dto.setTotalHours(defaultZero(rs.getBigDecimal("total_hours")));
            return dto;
        }
    }

    private static class DailyDetailRowMapper implements RowMapper<MonthlyReportDailyDetailDto> {

        public MonthlyReportDailyDetailDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            MonthlyReportDailyDetailDto dto = new MonthlyReportDailyDetailDto();
            dto.setWorkDate(rs.getString("work_date"));
            dto.setDayOfWeek(rs.getString("day_of_week"));
            dto.setProjectName(rs.getString("project_name"));
            dto.setWorkCategory(rs.getString("work_category"));
            dto.setWorkCategoryName(rs.getString("work_category_name"));
            dto.setWorkHours(defaultZero(rs.getBigDecimal("work_hours")));
            dto.setWorkContent(rs.getString("work_content"));
            return dto;
        }
    }

    private static BigDecimal defaultZero(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value;
    }
}
