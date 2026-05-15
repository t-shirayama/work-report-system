package com.example.workreport.dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.workreport.dto.WorkReportSearchResultDto;
import com.example.workreport.entity.WorkReport;

@Repository
public class WorkReportDao {

    private static final String INSERT_WORK_REPORT =
            "INSERT INTO work_reports ("
                    + "    work_report_id, "
                    + "    user_id, "
                    + "    department_id, "
                    + "    work_date, "
                    + "    project_name, "
                    + "    work_category, "
                    + "    work_hours, "
                    + "    work_content, "
                    + "    created_at, "
                    + "    updated_at "
                    + ") VALUES ("
                    + "    seq_work_reports.NEXTVAL, "
                    + "    :userId, "
                    + "    :departmentId, "
                    + "    :workDate, "
                    + "    :projectName, "
                    + "    :workCategory, "
                    + "    :workHours, "
                    + "    :workContent, "
                    + "    SYSDATE, "
                    + "    SYSDATE "
                    + ")";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public WorkReportDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public int insert(WorkReport workReport) {
        return namedParameterJdbcTemplate.update(
                INSERT_WORK_REPORT,
                new BeanPropertySqlParameterSource(workReport));
    }

    public List<WorkReportSearchResultDto> search(
            Date dateFrom,
            Date dateTo,
            String employeeName,
            String departmentName,
            String workCategory,
            String projectName) {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    TO_CHAR(wr.work_date, 'YYYY/MM/DD') AS work_date, ");
        sql.append("    u.employee_name, ");
        sql.append("    d.department_name, ");
        sql.append("    wr.project_name, ");
        sql.append("    wr.work_category, ");
        sql.append("    CASE wr.work_category ");
        sql.append("        WHEN 'DESIGN' THEN '設計' ");
        sql.append("        WHEN 'DEVELOPMENT' THEN '開発' ");
        sql.append("        WHEN 'TEST' THEN 'テスト' ");
        sql.append("        WHEN 'MEETING' THEN '会議' ");
        sql.append("        WHEN 'DOCUMENT' THEN '資料作成' ");
        sql.append("        WHEN 'OTHER' THEN 'その他' ");
        sql.append("        ELSE wr.work_category ");
        sql.append("    END AS work_category_name, ");
        sql.append("    wr.work_hours, ");
        sql.append("    wr.work_content ");
        sql.append("FROM work_reports wr ");
        sql.append("INNER JOIN users u ");
        sql.append("    ON wr.user_id = u.user_id ");
        sql.append("INNER JOIN departments d ");
        sql.append("    ON wr.department_id = d.department_id ");
        sql.append("WHERE 1 = 1 ");

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (dateFrom != null) {
            sql.append("  AND wr.work_date >= :dateFrom ");
            params.addValue("dateFrom", dateFrom);
        }

        if (dateTo != null) {
            sql.append("  AND wr.work_date <= :dateTo ");
            params.addValue("dateTo", dateTo);
        }

        if (StringUtils.hasText(employeeName)) {
            sql.append("  AND u.employee_name LIKE :employeeName ");
            params.addValue("employeeName", "%" + employeeName + "%");
        }

        if (StringUtils.hasText(departmentName)) {
            sql.append("  AND d.department_name LIKE :departmentName ");
            params.addValue("departmentName", "%" + departmentName + "%");
        }

        if (StringUtils.hasText(workCategory)) {
            sql.append("  AND wr.work_category = :workCategory ");
            params.addValue("workCategory", workCategory);
        }

        if (StringUtils.hasText(projectName)) {
            sql.append("  AND wr.project_name LIKE :projectName ");
            params.addValue("projectName", "%" + projectName + "%");
        }

        sql.append("ORDER BY wr.work_date DESC, wr.work_report_id DESC");

        return namedParameterJdbcTemplate.query(sql.toString(), params, new WorkReportSearchResultRowMapper());
    }

    private static class WorkReportSearchResultRowMapper implements RowMapper<WorkReportSearchResultDto> {

        public WorkReportSearchResultDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            WorkReportSearchResultDto dto = new WorkReportSearchResultDto();
            dto.setWorkDate(rs.getString("work_date"));
            dto.setEmployeeName(rs.getString("employee_name"));
            dto.setDepartmentName(rs.getString("department_name"));
            dto.setProjectName(rs.getString("project_name"));
            dto.setWorkCategory(rs.getString("work_category"));
            dto.setWorkCategoryName(rs.getString("work_category_name"));
            dto.setWorkHours(rs.getBigDecimal("work_hours"));
            dto.setWorkContent(rs.getString("work_content"));
            return dto;
        }
    }
}
