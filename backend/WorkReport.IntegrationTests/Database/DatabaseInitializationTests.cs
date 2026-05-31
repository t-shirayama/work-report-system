using WorkReport.IntegrationTests.Support;

namespace WorkReport.IntegrationTests.Database;

[Collection(IntegrationTestCollection.Name)]
public sealed class DatabaseInitializationTests(WorkReportApiFixture fixture)
{
    [SkippableFact]
    public async Task SchemaAndSeed_AreApplied()
    {
        await fixture.ResetDatabaseAsync();

        var departments = await fixture.QuerySingleAsync<int>("SELECT COUNT(*) FROM departments");
        var users = await fixture.QuerySingleAsync<int>("SELECT COUNT(*) FROM users");
        var reports = await fixture.QuerySingleAsync<int>("SELECT COUNT(*) FROM work_reports");
        var histories = await fixture.QuerySingleAsync<int>("SELECT COUNT(*) FROM report_output_histories");

        Assert.Equal(3, departments);
        Assert.Equal(5, users);
        Assert.Equal(8, reports);
        Assert.Equal(3, histories);
    }

    [SkippableFact]
    public async Task Constraints_RejectInvalidWorkReportData()
    {
        await fixture.ResetDatabaseAsync();

        await Assert.ThrowsAsync<Microsoft.Data.SqlClient.SqlException>(() =>
            fixture.QuerySingleAsync<int>("""
                INSERT INTO work_reports (
                    user_id, department_id, work_date, project_name, work_category, work_hours, work_content
                )
                OUTPUT INSERTED.work_report_id
                VALUES (2, 1, '2026-05-31', N'不正データ', N'BAD', 1.00, N'分類エラー')
                """));

        await Assert.ThrowsAsync<Microsoft.Data.SqlClient.SqlException>(() =>
            fixture.QuerySingleAsync<int>("""
                INSERT INTO report_output_histories (
                    target_year_month, created_by, target_user_id, report_type, file_name, file_path, status
                )
                OUTPUT INSERTED.report_output_history_id
                VALUES ('2026AA', 1, 2, N'MONTHLY_WORK_REPORT', N'a.xlsx', N'a.xlsx', N'SUCCESS')
                """));
    }
}
