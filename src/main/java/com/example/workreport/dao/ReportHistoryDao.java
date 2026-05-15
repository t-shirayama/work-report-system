package com.example.workreport.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.workreport.dto.ReportHistoryDto;
import com.example.workreport.entity.ReportOutputHistory;

@Repository
public class ReportHistoryDao {

    private static final String INSERT_HISTORY =
            "INSERT INTO report_output_histories ("
                    + "    report_output_history_id, "
                    + "    target_year_month, "
                    + "    created_by, "
                    + "    report_type, "
                    + "    file_name, "
                    + "    file_path, "
                    + "    status, "
                    + "    error_message, "
                    + "    created_at, "
                    + "    updated_at "
                    + ") VALUES ("
                    + "    seq_report_output_histories.NEXTVAL, "
                    + "    :targetYearMonth, "
                    + "    :createdBy, "
                    + "    :reportType, "
                    + "    :fileName, "
                    + "    :filePath, "
                    + "    :status, "
                    + "    :errorMessage, "
                    + "    SYSDATE, "
                    + "    SYSDATE "
                    + ")";

    private static final String SELECT_HISTORY_LIST =
            "SELECT "
                    + "    roh.report_output_history_id, "
                    + "    TO_CHAR(roh.created_at, 'YYYY/MM/DD HH24:MI') AS output_at, "
                    + "    roh.target_year_month, "
                    + "    roh.report_type, "
                    + "    CASE roh.report_type "
                    + "        WHEN 'MONTHLY_WORK_REPORT' THEN '月次作業報告書' "
                    + "        ELSE roh.report_type "
                    + "    END AS report_type_name, "
                    + "    u.employee_name AS created_by_name, "
                    + "    roh.status, "
                    + "    CASE roh.status "
                    + "        WHEN 'SUCCESS' THEN '成功' "
                    + "        WHEN 'ERROR' THEN 'エラー' "
                    + "        WHEN 'PROCESSING' THEN '作成中' "
                    + "        ELSE roh.status "
                    + "    END AS status_name, "
                    + "    roh.file_name, "
                    + "    roh.file_path, "
                    + "    roh.error_message "
                    + "FROM report_output_histories roh "
                    + "INNER JOIN users u "
                    + "    ON roh.created_by = u.user_id "
                    + "ORDER BY roh.created_at DESC, roh.report_output_history_id DESC";

    private static final String SELECT_HISTORY_BY_ID =
            SELECT_HISTORY_LIST.replace(
                    "ORDER BY roh.created_at DESC, roh.report_output_history_id DESC",
                    "WHERE roh.report_output_history_id = :reportOutputHistoryId");

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public ReportHistoryDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public void insert(ReportOutputHistory history) {
        namedParameterJdbcTemplate.update(INSERT_HISTORY, new BeanPropertySqlParameterSource(history));
    }

    public List<ReportHistoryDto> findAll() {
        return namedParameterJdbcTemplate.query(SELECT_HISTORY_LIST, new MapSqlParameterSource(), new ReportHistoryRowMapper());
    }

    public ReportHistoryDto findById(Long reportOutputHistoryId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reportOutputHistoryId", reportOutputHistoryId);

        try {
            return namedParameterJdbcTemplate.queryForObject(SELECT_HISTORY_BY_ID, params, new ReportHistoryRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static class ReportHistoryRowMapper implements RowMapper<ReportHistoryDto> {

        public ReportHistoryDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            ReportHistoryDto dto = new ReportHistoryDto();
            dto.setReportOutputHistoryId(rs.getLong("report_output_history_id"));
            dto.setOutputAt(rs.getString("output_at"));
            dto.setTargetYearMonth(rs.getString("target_year_month"));
            dto.setReportType(rs.getString("report_type"));
            dto.setReportTypeName(rs.getString("report_type_name"));
            dto.setCreatedByName(rs.getString("created_by_name"));
            dto.setStatus(rs.getString("status"));
            dto.setStatusName(rs.getString("status_name"));
            dto.setFileName(rs.getString("file_name"));
            dto.setFilePath(rs.getString("file_path"));
            dto.setErrorMessage(rs.getString("error_message"));
            return dto;
        }
    }
}
