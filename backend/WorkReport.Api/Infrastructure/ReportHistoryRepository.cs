using Dapper;
using WorkReport.Api.Contracts;

namespace WorkReport.Api.Infrastructure;

public sealed class ReportHistoryRepository(SqlConnectionFactory connectionFactory)
{
    public async Task<IReadOnlyList<ReportHistoryListResponse>> SearchAsync(
        string? targetYearMonth,
        int? targetUserId,
        string? status)
    {
        var sql = """
            SELECT
                h.report_output_history_id AS ReportOutputHistoryId,
                h.target_year_month AS TargetYearMonth,
                target_user.employee_name AS TargetEmployeeName,
                created_user.employee_name AS CreatedByEmployeeName,
                h.report_type AS ReportType,
                h.file_name AS FileName,
                h.status AS Status,
                h.error_message AS ErrorMessage,
                h.created_at AS CreatedAt
            FROM report_output_histories h
            INNER JOIN users target_user ON h.target_user_id = target_user.user_id
            INNER JOIN users created_user ON h.created_by = created_user.user_id
            WHERE 1 = 1
            """;
        var parameters = new DynamicParameters();
        if (!string.IsNullOrWhiteSpace(targetYearMonth))
        {
            sql += " AND h.target_year_month = @TargetYearMonth";
            parameters.Add("TargetYearMonth", targetYearMonth);
        }

        if (targetUserId.HasValue)
        {
            sql += " AND h.target_user_id = @TargetUserId";
            parameters.Add("TargetUserId", targetUserId);
        }

        if (!string.IsNullOrWhiteSpace(status))
        {
            sql += " AND h.status = @Status";
            parameters.Add("Status", status);
        }

        sql += " ORDER BY h.created_at DESC, h.report_output_history_id DESC";
        await using var connection = connectionFactory.Create();
        var histories = await connection.QueryAsync<ReportHistoryListResponse>(sql, parameters);
        return histories.AsList();
    }

    public async Task<ReportHistoryDetailResponse?> FindDetailAsync(int id)
    {
        const string sql = """
            SELECT
                h.report_output_history_id AS ReportOutputHistoryId,
                h.target_year_month AS TargetYearMonth,
                h.report_type AS ReportType,
                h.file_name AS FileName,
                h.file_path AS FilePath,
                h.status AS Status,
                h.error_message AS ErrorMessage,
                h.created_at AS CreatedAt,
                h.updated_at AS UpdatedAt,
                target_user.user_id AS UserId,
                target_user.department_id AS DepartmentId,
                target_dept.department_name AS DepartmentName,
                target_user.login_id AS LoginId,
                target_user.employee_name AS EmployeeName,
                target_user.role_code AS RoleCode,
                created_user.user_id AS UserId,
                created_user.department_id AS DepartmentId,
                created_dept.department_name AS DepartmentName,
                created_user.login_id AS LoginId,
                created_user.employee_name AS EmployeeName,
                created_user.role_code AS RoleCode
            FROM report_output_histories h
            INNER JOIN users target_user ON h.target_user_id = target_user.user_id
            INNER JOIN departments target_dept ON target_user.department_id = target_dept.department_id
            INNER JOIN users created_user ON h.created_by = created_user.user_id
            INNER JOIN departments created_dept ON created_user.department_id = created_dept.department_id
            WHERE h.report_output_history_id = @id
            """;

        await using var connection = connectionFactory.Create();
        var result = await connection.QueryAsync<ReportHistoryRaw, UserResponse, UserResponse, ReportHistoryDetailResponse>(
            sql,
            (history, targetUser, createdBy) => new ReportHistoryDetailResponse(
                history.ReportOutputHistoryId,
                history.TargetYearMonth,
                targetUser,
                createdBy,
                history.ReportType,
                history.FileName,
                history.FilePath,
                history.Status,
                history.ErrorMessage,
                history.CreatedAt,
                history.UpdatedAt),
            new { id },
            splitOn: "UserId,UserId");

        return result.SingleOrDefault();
    }

    public async Task<int> InsertSuccessAsync(
        string targetYearMonth,
        int createdBy,
        int targetUserId,
        string fileName,
        string filePath)
    {
        const string sql = """
            INSERT INTO report_output_histories (
                target_year_month, created_by, target_user_id, report_type,
                file_name, file_path, status, created_at, updated_at
            )
            OUTPUT INSERTED.report_output_history_id
            VALUES (
                @targetYearMonth, @createdBy, @targetUserId, N'MONTHLY_WORK_REPORT',
                @fileName, @filePath, N'SUCCESS', sysdatetime(), sysdatetime()
            )
            """;
        await using var connection = connectionFactory.Create();
        return await connection.ExecuteScalarAsync<int>(sql, new
        {
            targetYearMonth,
            createdBy,
            targetUserId,
            fileName,
            filePath
        });
    }

    private sealed record ReportHistoryRaw(
        int ReportOutputHistoryId,
        string TargetYearMonth,
        string ReportType,
        string FileName,
        string FilePath,
        string Status,
        string? ErrorMessage,
        DateTime CreatedAt,
        DateTime UpdatedAt);
}
