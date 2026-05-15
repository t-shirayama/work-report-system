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
            "SELECT COUNT(*) "
                    + "FROM users u "
                    + "WHERE u.role_code = 'USER' "
                    + "  AND NOT EXISTS ( "
                    + "    SELECT 1 "
                    + "    FROM report_output_histories roh "
                    + "    WHERE roh.target_user_id = u.user_id "
                    + "      AND roh.target_year_month = TO_CHAR(SYSDATE, 'YYYYMM') "
                    + "      AND roh.report_type = 'MONTHLY_WORK_REPORT' "
                    + "      AND roh.status = 'SUCCESS' "
                    + ")";

    private static final String SELECT_RECENT_ACTIVITIES =
            "SELECT * FROM ( "
                    + "    SELECT "
                    + "        activity_date, "
                    + "        activity_at, "
                    + "        activity_type, "
                    + "        activity_type_name, "
                    + "        badge_class, "
                    + "        content, "
                    + "        employee_name "
                    + "    FROM ( "
                    + "        SELECT "
                    + "            wr.created_at AS activity_date, "
                    + "            TO_CHAR(wr.created_at, 'YYYY/MM/DD HH24:MI') AS activity_at, "
                    + "            'WORK_REPORT' AS activity_type, "
                    + "            '登録' AS activity_type_name, "
                    + "            'blue' AS badge_class, "
                    + "            '作業日報を登録しました（' || TO_CHAR(wr.work_date, 'YYYY/MM/DD') || '）' AS content, "
                    + "            u.employee_name "
                    + "        FROM work_reports wr "
                    + "        INNER JOIN users u "
                    + "            ON wr.user_id = u.user_id "
                    + "        UNION ALL "
                    + "        SELECT "
                    + "            roh.created_at AS activity_date, "
                    + "            TO_CHAR(roh.created_at, 'YYYY/MM/DD HH24:MI') AS activity_at, "
                    + "            'REPORT_OUTPUT' AS activity_type, "
                    + "            CASE roh.status "
                    + "                WHEN 'ERROR' THEN 'エラー' "
                    + "                WHEN 'PROCESSING' THEN '処理中' "
                    + "                ELSE '出力' "
                    + "            END AS activity_type_name, "
                    + "            CASE roh.status "
                    + "                WHEN 'ERROR' THEN 'red' "
                    + "                WHEN 'PROCESSING' THEN 'purple' "
                    + "                ELSE 'green' "
                    + "            END AS badge_class, "
                    + "            CASE roh.status "
                    + "                WHEN 'ERROR' THEN '月次報告書の出力に失敗しました（' || SUBSTR(roh.target_year_month, 1, 4) || '年' || TO_NUMBER(SUBSTR(roh.target_year_month, 5, 2)) || '月分）' "
                    + "                WHEN 'PROCESSING' THEN '月次報告書を作成中です（' || SUBSTR(roh.target_year_month, 1, 4) || '年' || TO_NUMBER(SUBSTR(roh.target_year_month, 5, 2)) || '月分）' "
                    + "                ELSE '月次報告書を出力しました（' || SUBSTR(roh.target_year_month, 1, 4) || '年' || TO_NUMBER(SUBSTR(roh.target_year_month, 5, 2)) || '月分）' "
                    + "            END AS content, "
                    + "            u.employee_name "
                    + "        FROM report_output_histories roh "
                    + "        INNER JOIN users u "
                    + "            ON roh.created_by = u.user_id "
                    + "    ) activities "
                    + "    ORDER BY activity_date DESC "
                    + ") "
                    + "WHERE ROWNUM <= 6";

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
