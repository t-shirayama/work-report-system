package com.example.workreport.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.workreport.entity.User;

@Repository
public class UserDao {

    private static final String SELECT_BY_LOGIN_ID =
            "SELECT "
                    + "    u.user_id, "
                    + "    u.department_id, "
                    + "    d.department_name, "
                    + "    u.login_id, "
                    + "    u.password, "
                    + "    u.employee_name, "
                    + "    u.role_code, "
                    + "    u.created_at, "
                    + "    u.updated_at "
                    + "FROM users u "
                    + "INNER JOIN departments d "
                    + "    ON u.department_id = d.department_id "
                    + "WHERE u.login_id = :loginId";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public UserDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public User findByLoginId(String loginId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("loginId", loginId);

        try {
            return namedParameterJdbcTemplate.queryForObject(SELECT_BY_LOGIN_ID, params, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static class UserRowMapper implements RowMapper<User> {

        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setUserId(rs.getLong("user_id"));
            user.setDepartmentId(rs.getLong("department_id"));
            user.setDepartmentName(rs.getString("department_name"));
            user.setLoginId(rs.getString("login_id"));
            user.setPassword(rs.getString("password"));
            user.setEmployeeName(rs.getString("employee_name"));
            user.setRoleCode(rs.getString("role_code"));
            user.setCreatedAt(rs.getTimestamp("created_at"));
            user.setUpdatedAt(rs.getTimestamp("updated_at"));
            return user;
        }
    }
}
