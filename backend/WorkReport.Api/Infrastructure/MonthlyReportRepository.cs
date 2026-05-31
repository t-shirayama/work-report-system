using Dapper;
using WorkReport.Api.Reporting;

namespace WorkReport.Api.Infrastructure;

public sealed class MonthlyReportRepository(SqlConnectionFactory connectionFactory)
{
    public async Task<MonthlyReportData> BuildMonthlyReportAsync(string targetYearMonth, int targetUserId)
    {
        const string userSql = """
            SELECT
                u.user_id AS UserId,
                u.login_id AS LoginId,
                u.employee_name AS EmployeeName,
                d.department_name AS DepartmentName
            FROM users u
            INNER JOIN departments d ON u.department_id = d.department_id
            WHERE u.user_id = @targetUserId
            """;
        const string detailsSql = """
            SELECT
                CONVERT(varchar(10), work_date, 23) AS WorkDate,
                project_name AS ProjectName,
                work_category AS WorkCategory,
                work_hours AS WorkHours,
                work_content AS WorkContent
            FROM work_reports
            WHERE user_id = @targetUserId
              AND CONVERT(char(6), work_date, 112) = @targetYearMonth
            ORDER BY work_date, work_report_id
            """;
        const string categoriesSql = """
            SELECT work_category AS WorkCategory, SUM(work_hours) AS TotalHours
            FROM work_reports
            WHERE user_id = @targetUserId
              AND CONVERT(char(6), work_date, 112) = @targetYearMonth
            GROUP BY work_category
            ORDER BY work_category
            """;

        await using var connection = connectionFactory.Create();
        var user = await connection.QuerySingleOrDefaultAsync<MonthlyReportUser>(userSql, new { targetUserId });
        if (user is null)
        {
            throw new KeyNotFoundException("対象ユーザーが見つかりません。");
        }
        var details = (await connection.QueryAsync<MonthlyReportDetail>(
            detailsSql,
            new { targetUserId, targetYearMonth })).AsList();
        var categories = (await connection.QueryAsync<MonthlyReportCategory>(
            categoriesSql,
            new { targetUserId, targetYearMonth })).AsList();

        return new MonthlyReportData(targetYearMonth, user, details, categories);
    }
}
