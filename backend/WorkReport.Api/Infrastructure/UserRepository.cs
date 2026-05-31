using Dapper;
using WorkReport.Api.Contracts;

namespace WorkReport.Api.Infrastructure;

public sealed class UserRepository(SqlConnectionFactory connectionFactory)
{
    public async Task<UserRecord?> FindByLoginIdAsync(string loginId)
    {
        const string sql = """
            SELECT
                u.user_id AS UserId,
                u.department_id AS DepartmentId,
                d.department_name AS DepartmentName,
                u.login_id AS LoginId,
                u.password AS PasswordHash,
                u.employee_name AS EmployeeName,
                u.role_code AS RoleCode
            FROM users u
            INNER JOIN departments d ON u.department_id = d.department_id
            WHERE u.login_id = @loginId
            """;
        await using var connection = connectionFactory.Create();
        return await connection.QuerySingleOrDefaultAsync<UserRecord>(sql, new { loginId });
    }

    public async Task<IReadOnlyList<UserResponse>> FindReportTargetUsersAsync()
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
            WHERE u.role_code = N'USER'
            ORDER BY d.display_order, u.employee_name, u.user_id
            """;
        await using var connection = connectionFactory.Create();
        var users = await connection.QueryAsync<UserResponse>(sql);
        return users.AsList();
    }
}

public sealed record UserRecord(
    int UserId,
    int DepartmentId,
    string DepartmentName,
    string LoginId,
    string PasswordHash,
    string EmployeeName,
    string RoleCode)
{
    public UserResponse ToResponse()
        => new(UserId, DepartmentId, DepartmentName, LoginId, EmployeeName, RoleCode);
}
