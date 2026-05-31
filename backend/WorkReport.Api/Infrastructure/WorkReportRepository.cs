using Dapper;
using WorkReport.Api.Application;
using WorkReport.Api.Contracts;

namespace WorkReport.Api.Infrastructure;

public sealed class WorkReportRepository(SqlConnectionFactory connectionFactory)
{
    public async Task<int> InsertAsync(WorkReportRegisterRequest request, CurrentUser currentUser)
    {
        const string sql = """
            INSERT INTO work_reports (
                user_id, department_id, work_date, project_name, work_category,
                work_hours, work_content, created_at, updated_at
            )
            OUTPUT INSERTED.work_report_id
            VALUES (
                @UserId, @DepartmentId, @WorkDate, @ProjectName, @WorkCategory,
                @WorkHours, @WorkContent, sysdatetime(), sysdatetime()
            )
            """;
        await using var connection = connectionFactory.Create();
        return await connection.ExecuteScalarAsync<int>(sql, new
        {
            currentUser.UserId,
            currentUser.DepartmentId,
            WorkDate = request.WorkDate!.Value.Date,
            request.ProjectName,
            request.WorkCategory,
            request.WorkHours,
            request.WorkContent
        });
    }

    public async Task<IReadOnlyList<WorkReportSearchResultResponse>> SearchAsync(
        WorkReportSearchRequest request,
        CurrentUser currentUser)
    {
        var sql = """
            SELECT
                CONVERT(varchar(10), wr.work_date, 23) AS WorkDate,
                u.employee_name AS EmployeeName,
                d.department_name AS DepartmentName,
                wr.project_name AS ProjectName,
                wr.work_category AS WorkCategory,
                CASE wr.work_category
                    WHEN N'DESIGN' THEN N'設計'
                    WHEN N'DEVELOPMENT' THEN N'開発'
                    WHEN N'TEST' THEN N'テスト'
                    WHEN N'MEETING' THEN N'会議'
                    WHEN N'DOCUMENT' THEN N'ドキュメント'
                    ELSE N'その他'
                END AS WorkCategoryName,
                wr.work_hours AS WorkHours,
                wr.work_content AS WorkContent
            FROM work_reports wr
            INNER JOIN users u ON wr.user_id = u.user_id
            INNER JOIN departments d ON wr.department_id = d.department_id
            WHERE 1 = 1
            """;

        var parameters = new DynamicParameters();
        if (!currentUser.IsAdmin)
        {
            sql += " AND wr.user_id = @UserId";
            parameters.Add("UserId", currentUser.UserId);
        }

        if (request.DateFrom.HasValue)
        {
            sql += " AND wr.work_date >= @DateFrom";
            parameters.Add("DateFrom", request.DateFrom.Value.Date);
        }

        if (request.DateTo.HasValue)
        {
            sql += " AND wr.work_date <= @DateTo";
            parameters.Add("DateTo", request.DateTo.Value.Date);
        }

        AddLikeCondition(ref sql, parameters, "u.employee_name", "EmployeeName", request.EmployeeName);
        AddLikeCondition(ref sql, parameters, "d.department_name", "DepartmentName", request.DepartmentName);
        AddLikeCondition(ref sql, parameters, "wr.project_name", "ProjectName", request.ProjectName);

        if (!string.IsNullOrWhiteSpace(request.WorkCategory))
        {
            sql += " AND wr.work_category = @WorkCategory";
            parameters.Add("WorkCategory", request.WorkCategory);
        }

        sql += " ORDER BY wr.work_date DESC, wr.work_report_id DESC";

        await using var connection = connectionFactory.Create();
        var results = await connection.QueryAsync<WorkReportSearchResultResponse>(sql, parameters);
        return results.AsList();
    }

    private static void AddLikeCondition(
        ref string sql,
        DynamicParameters parameters,
        string column,
        string name,
        string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return;
        }

        sql += $" AND {column} LIKE @{name}";
        parameters.Add(name, $"%{value}%");
    }
}
