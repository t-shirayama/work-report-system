using System.Net;
using WorkReport.IntegrationTests.Support;

namespace WorkReport.IntegrationTests.Api;

[Collection(IntegrationTestCollection.Name)]
public sealed class WorkReportsIntegrationTests(WorkReportApiFixture fixture)
{
    [SkippableFact]
    public async Task RegisterWorkReport_WithCsrf_PersistsToDatabase()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsUserAsync("sato");

        var response = await api.PostJsonWithCsrfAsync("/api/work-reports", new
        {
            workDate = "2026-05-31",
            projectName = "IT登録",
            workCategory = "DEVELOPMENT",
            workHours = 2.25m,
            workContent = "登録後DB確認"
        });
        var json = await api.ReadJsonAsync(response);
        var id = json.GetProperty("workReportId").GetInt32();

        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
        Assert.True(id > 0);

        var stored = await fixture.QuerySingleAsync<StoredWorkReport>(
            "SELECT project_name AS ProjectName, work_hours AS WorkHours FROM work_reports WHERE work_report_id = @id",
            new { id });
        Assert.Equal("IT登録", stored.ProjectName);
        Assert.Equal(2.25m, stored.WorkHours);
    }

    [SkippableFact]
    public async Task RegisterWorkReport_ReturnsBadRequest_WhenInputIsInvalid()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsUserAsync();

        var response = await api.PostJsonWithCsrfAsync("/api/work-reports", new
        {
            workDate = (string?)null,
            projectName = "",
            workCategory = "BAD",
            workHours = 25,
            workContent = ""
        });
        var json = await api.ReadJsonAsync(response);

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.Contains(json.GetProperty("errors").EnumerateArray(), e => e.GetString() == "作業日は必須です。");
        Assert.Contains(json.GetProperty("errors").EnumerateArray(), e => e.GetString() == "作業分類の値が正しくありません。");
    }

    [SkippableFact]
    public async Task SearchWorkReports_AdminCanUseAllFilters()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var response = await api.HttpClient.GetAsync(
            "/api/work-reports?dateFrom=2026-05-01&dateTo=2026-05-08&employeeName=%E4%BD%90%E8%97%A4&departmentName=%E9%96%8B%E7%99%BA&workCategory=DEVELOPMENT&projectName=%E4%BD%9C%E6%A5%AD%E6%97%A5%E5%A0%B1");
        var json = await api.ReadJsonAsync(response);

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.True(json.GetArrayLength() >= 1);
        Assert.All(json.EnumerateArray(), row =>
        {
            Assert.Contains("佐藤", row.GetProperty("employeeName").GetString());
            Assert.Contains("開発", row.GetProperty("departmentName").GetString());
            Assert.Equal("DEVELOPMENT", row.GetProperty("workCategory").GetString());
            Assert.Contains("作業日報", row.GetProperty("projectName").GetString());
        });
    }

    [SkippableFact]
    public async Task SearchWorkReports_UserCanSeeOnlyOwnReports()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsUserAsync("sato");

        var response = await api.HttpClient.GetAsync("/api/work-reports");
        var json = await api.ReadJsonAsync(response);

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.True(json.GetArrayLength() > 0);
        Assert.All(json.EnumerateArray(), row =>
        {
            Assert.Equal("佐藤 花子", row.GetProperty("employeeName").GetString());
        });
    }

    [SkippableFact]
    public async Task SearchWorkReports_ReturnsBadRequest_WhenDateRangeIsInvalid()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var response = await api.HttpClient.GetAsync("/api/work-reports?dateFrom=2026-06-01&dateTo=2026-05-01");
        var json = await api.ReadJsonAsync(response);

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.Contains(json.GetProperty("errors").EnumerateArray(), e => e.GetString() == "対象期間 From は To 以前の日付を入力してください。");
    }

    private sealed record StoredWorkReport(string ProjectName, decimal WorkHours);
}
