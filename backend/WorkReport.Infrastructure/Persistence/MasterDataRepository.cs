using Dapper;
using Microsoft.Data.SqlClient;
using WorkReport.Application.Contracts;
using WorkReport.Application.Exceptions;
using WorkReport.Application.Interfaces;

namespace WorkReport.Infrastructure.Persistence;

public sealed class MasterDataRepository(SqlConnectionFactory connectionFactory) : IMasterDataRepository
{
    public async Task<IReadOnlyList<DepartmentResponse>> FindDepartmentsAsync()
    {
        const string sql = """
            SELECT
                department_id AS DepartmentId,
                department_code AS DepartmentCode,
                department_name AS DepartmentName,
                display_order AS DisplayOrder
            FROM departments
            ORDER BY display_order, department_id
            """;
        await using var connection = connectionFactory.Create();
        var rows = await connection.QueryAsync<DepartmentResponse>(sql);
        return rows.AsList();
    }

    public async Task<DepartmentResponse?> FindDepartmentAsync(int departmentId)
    {
        const string sql = """
            SELECT
                department_id AS DepartmentId,
                department_code AS DepartmentCode,
                department_name AS DepartmentName,
                display_order AS DisplayOrder
            FROM departments
            WHERE department_id = @departmentId
            """;
        await using var connection = connectionFactory.Create();
        return await connection.QuerySingleOrDefaultAsync<DepartmentResponse>(sql, new { departmentId });
    }

    public async Task<int> InsertDepartmentAsync(DepartmentUpsertRequest request)
    {
        const string sql = """
            INSERT INTO departments (department_code, department_name, display_order)
            OUTPUT INSERTED.department_id
            VALUES (@DepartmentCode, @DepartmentName, @DisplayOrder)
            """;
        try
        {
            await using var connection = connectionFactory.Create();
            return await connection.ExecuteScalarAsync<int>(sql, request);
        }
        catch (SqlException ex) when (IsUniqueConstraint(ex))
        {
            throw new RepositoryConstraintException(RepositoryConstraint.Unique);
        }
    }

    public async Task<bool> UpdateDepartmentAsync(int departmentId, DepartmentUpsertRequest request)
    {
        const string sql = """
            UPDATE departments
            SET department_code = @DepartmentCode,
                department_name = @DepartmentName,
                display_order = @DisplayOrder,
                updated_at = sysdatetime()
            WHERE department_id = @departmentId
            """;
        try
        {
            await using var connection = connectionFactory.Create();
            var affected = await connection.ExecuteAsync(sql, new
            {
                departmentId,
                request.DepartmentCode,
                request.DepartmentName,
                request.DisplayOrder
            });
            return affected > 0;
        }
        catch (SqlException ex) when (IsUniqueConstraint(ex))
        {
            throw new RepositoryConstraintException(RepositoryConstraint.Unique);
        }
    }

    public async Task<IReadOnlyList<MasterUserResponse>> FindUsersAsync()
    {
        const string sql = """
            SELECT
                u.user_id AS UserId,
                u.department_id AS DepartmentId,
                d.department_name AS DepartmentName,
                u.login_id AS LoginId,
                u.employee_name AS EmployeeName,
                u.role_code AS RoleCode
            FROM users u
            INNER JOIN departments d ON u.department_id = d.department_id
            ORDER BY d.display_order, u.user_id
            """;
        await using var connection = connectionFactory.Create();
        var rows = await connection.QueryAsync<MasterUserResponse>(sql);
        return rows.AsList();
    }

    public async Task<MasterUserResponse?> FindUserAsync(int userId)
    {
        const string sql = """
            SELECT
                u.user_id AS UserId,
                u.department_id AS DepartmentId,
                d.department_name AS DepartmentName,
                u.login_id AS LoginId,
                u.employee_name AS EmployeeName,
                u.role_code AS RoleCode
            FROM users u
            INNER JOIN departments d ON u.department_id = d.department_id
            WHERE u.user_id = @userId
            """;
        await using var connection = connectionFactory.Create();
        return await connection.QuerySingleOrDefaultAsync<MasterUserResponse>(sql, new { userId });
    }

    public async Task<int> InsertUserAsync(MasterUserCreateRequest request, string passwordHash)
    {
        const string sql = """
            INSERT INTO users (department_id, login_id, password, employee_name, role_code)
            OUTPUT INSERTED.user_id
            VALUES (@DepartmentId, @LoginId, @passwordHash, @EmployeeName, @RoleCode)
            """;
        try
        {
            await using var connection = connectionFactory.Create();
            return await connection.ExecuteScalarAsync<int>(sql, new
            {
                request.DepartmentId,
                request.LoginId,
                passwordHash,
                request.EmployeeName,
                request.RoleCode
            });
        }
        catch (SqlException ex) when (IsUniqueConstraint(ex))
        {
            throw new RepositoryConstraintException(RepositoryConstraint.Unique);
        }
        catch (SqlException ex) when (IsForeignKeyConstraint(ex))
        {
            throw new RepositoryConstraintException(RepositoryConstraint.ForeignKey);
        }
    }

    public async Task<bool> UpdateUserAsync(int userId, MasterUserUpdateRequest request, string? passwordHash)
    {
        var sql = """
            UPDATE users
            SET department_id = @DepartmentId,
                login_id = @LoginId,
                employee_name = @EmployeeName,
                role_code = @RoleCode,
                updated_at = sysdatetime()
            """;
        if (!string.IsNullOrWhiteSpace(passwordHash))
        {
            sql += ", password = @passwordHash";
        }

        sql += " WHERE user_id = @userId";

        try
        {
            await using var connection = connectionFactory.Create();
            var affected = await connection.ExecuteAsync(sql, new
            {
                userId,
                request.DepartmentId,
                request.LoginId,
                request.EmployeeName,
                request.RoleCode,
                passwordHash
            });
            return affected > 0;
        }
        catch (SqlException ex) when (IsUniqueConstraint(ex))
        {
            throw new RepositoryConstraintException(RepositoryConstraint.Unique);
        }
        catch (SqlException ex) when (IsForeignKeyConstraint(ex))
        {
            throw new RepositoryConstraintException(RepositoryConstraint.ForeignKey);
        }
    }

    private static bool IsUniqueConstraint(SqlException ex)
        => ex.Number is 2601 or 2627;

    private static bool IsForeignKeyConstraint(SqlException ex)
        => ex.Number == 547;
}
