using Dapper;
using WorkReport.Application.Contracts;
using WorkReport.Application.Interfaces;

namespace WorkReport.Infrastructure.Persistence;

public sealed class DashboardRepository(SqlConnectionFactory connectionFactory) : IDashboardRepository
{
    public async Task<int> CountTodayWorkReportsAsync()
    {
        const string sql = """
            SELECT COUNT(*)
            FROM work_reports
            WHERE CAST(created_at AS date) = CAST(sysdatetime() AS date)
            """;
        await using var connection = connectionFactory.Create();
        return await connection.ExecuteScalarAsync<int>(sql);
    }

    public async Task<decimal> SumCurrentMonthWorkHoursAsync()
    {
        const string sql = """
            SELECT COALESCE(SUM(work_hours), 0)
            FROM work_reports
            WHERE work_date >= DATEFROMPARTS(YEAR(sysdatetime()), MONTH(sysdatetime()), 1)
              AND work_date < DATEADD(month, 1, DATEFROMPARTS(YEAR(sysdatetime()), MONTH(sysdatetime()), 1))
            """;
        await using var connection = connectionFactory.Create();
        return await connection.ExecuteScalarAsync<decimal>(sql);
    }

    public async Task<int> CountNotOutputMonthlyReportsAsync()
    {
        const string sql = """
            DECLARE @targetYearMonth char(6) = FORMAT(sysdatetime(), 'yyyyMM');

            SELECT COUNT(*)
            FROM users u
            WHERE u.role_code = N'USER'
              AND NOT EXISTS (
                  SELECT 1
                  FROM report_output_histories h
                  WHERE h.target_user_id = u.user_id
                    AND h.target_year_month = @targetYearMonth
                    AND h.report_type = N'MONTHLY_WORK_REPORT'
                    AND h.status = N'SUCCESS'
              )
            """;
        await using var connection = connectionFactory.Create();
        return await connection.ExecuteScalarAsync<int>(sql);
    }

    public async Task<IReadOnlyList<DashboardActivityResponse>> FindRecentActivitiesAsync()
    {
        const string sql = """
            SELECT TOP (10)
                CONVERT(varchar(16), activity_at, 120) AS ActivityAt,
                activity_type AS ActivityType,
                activity_type_name AS ActivityTypeName,
                badge_class AS BadgeClass,
                content AS Content,
                employee_name AS EmployeeName
            FROM (
                SELECT
                    wr.created_at AS activity_at,
                    N'WORK_REPORT' AS activity_type,
                    N'日報登録' AS activity_type_name,
                    N'primary' AS badge_class,
                    wr.project_name + N' / ' + wr.work_category AS content,
                    u.employee_name
                FROM work_reports wr
                INNER JOIN users u ON wr.user_id = u.user_id
                UNION ALL
                SELECT
                    h.created_at AS activity_at,
                    N'REPORT_OUTPUT' AS activity_type,
                    N'帳票出力' AS activity_type_name,
                    CASE h.status WHEN N'SUCCESS' THEN N'success' WHEN N'ERROR' THEN N'danger' ELSE N'warning' END AS badge_class,
                    h.file_name AS content,
                    u.employee_name
                FROM report_output_histories h
                INNER JOIN users u ON h.created_by = u.user_id
            ) activities
            ORDER BY activity_at DESC
            """;
        await using var connection = connectionFactory.Create();
        var activities = await connection.QueryAsync<DashboardActivityResponse>(sql);
        return activities.AsList();
    }
}
