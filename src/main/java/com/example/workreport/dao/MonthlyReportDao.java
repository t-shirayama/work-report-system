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

@Repository
public class MonthlyReportDao {

    private static final String CATEGORY_NAME_CASE =
            "CASE wr.work_category "
                    + "WHEN 'DESIGN' THEN '設計' "
                    + "WHEN 'DEVELOPMENT' THEN '開発' "
                    + "WHEN 'TEST' THEN 'テスト' "
                    + "WHEN 'MEETING' THEN '会議' "
                    + "WHEN 'DOCUMENT' THEN '資料作成' "
                    + "WHEN 'OTHER' THEN 'その他' "
                    + "ELSE wr.work_category END";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public MonthlyReportDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public MonthlyReportSummaryDto findSummary(MonthlyReportConditionDto condition) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    :targetYearMonth AS target_year_month, ");
        sql.append("    MAX(u.employee_name) AS employee_name, ");
        sql.append("    MAX(d.department_name) AS department_name, ");
        sql.append("    NVL(SUM(wr.work_hours), 0) AS total_work_hours, ");
        sql.append("    COUNT(DISTINCT wr.work_date) AS work_days ");
        sql.append("FROM work_reports wr ");
        sql.append("INNER JOIN users u ON wr.user_id = u.user_id ");
        sql.append("INNER JOIN departments d ON wr.department_id = d.department_id ");
        sql.append("WHERE wr.work_date >= :dateFrom ");
        sql.append("  AND wr.work_date <= :dateTo ");
        sql.append("  AND d.department_name = :departmentName ");
        sql.append("  AND u.employee_name = :employeeName ");

        return namedParameterJdbcTemplate.queryForObject(sql.toString(), createParams(condition), new SummaryRowMapper());
    }

    public List<MonthlyReportCategorySummaryDto> findCategorySummaries(MonthlyReportConditionDto condition) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    wr.work_category, ");
        sql.append("    ").append(CATEGORY_NAME_CASE).append(" AS work_category_name, ");
        sql.append("    SUM(wr.work_hours) AS total_hours ");
        sql.append("FROM work_reports wr ");
        sql.append("INNER JOIN users u ON wr.user_id = u.user_id ");
        sql.append("INNER JOIN departments d ON wr.department_id = d.department_id ");
        sql.append("WHERE wr.work_date >= :dateFrom ");
        sql.append("  AND wr.work_date <= :dateTo ");
        sql.append("  AND d.department_name = :departmentName ");
        sql.append("  AND u.employee_name = :employeeName ");
        sql.append("GROUP BY wr.work_category ");
        sql.append("ORDER BY wr.work_category ");

        return namedParameterJdbcTemplate.query(sql.toString(), createParams(condition), new CategorySummaryRowMapper());
    }

    public List<MonthlyReportDailyDetailDto> findDailyDetails(MonthlyReportConditionDto condition) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    TO_CHAR(wr.work_date, 'YYYY/MM/DD') AS work_date, ");
        sql.append("    TO_CHAR(wr.work_date, 'DY', 'NLS_DATE_LANGUAGE=JAPANESE') AS day_of_week, ");
        sql.append("    wr.project_name, ");
        sql.append("    wr.work_category, ");
        sql.append("    ").append(CATEGORY_NAME_CASE).append(" AS work_category_name, ");
        sql.append("    wr.work_hours, ");
        sql.append("    wr.work_content ");
        sql.append("FROM work_reports wr ");
        sql.append("INNER JOIN users u ON wr.user_id = u.user_id ");
        sql.append("INNER JOIN departments d ON wr.department_id = d.department_id ");
        sql.append("WHERE wr.work_date >= :dateFrom ");
        sql.append("  AND wr.work_date <= :dateTo ");
        sql.append("  AND d.department_name = :departmentName ");
        sql.append("  AND u.employee_name = :employeeName ");
        sql.append("ORDER BY wr.work_date, wr.work_report_id ");

        return namedParameterJdbcTemplate.query(sql.toString(), createParams(condition), new DailyDetailRowMapper());
    }

    private MapSqlParameterSource createParams(MonthlyReportConditionDto condition) {
        return new MapSqlParameterSource()
                .addValue("targetYearMonth", condition.getTargetYearMonth())
                .addValue("dateFrom", condition.getDateFrom())
                .addValue("dateTo", condition.getDateTo())
                .addValue("departmentName", condition.getDepartmentName())
                .addValue("employeeName", condition.getEmployeeName());
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
