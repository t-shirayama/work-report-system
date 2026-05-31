using System.Net;
using WorkReport.IntegrationTests.Support;

namespace WorkReport.IntegrationTests.Api;

[Collection(IntegrationTestCollection.Name)]
public sealed class ReportHistoriesIntegrationTests(WorkReportApiFixture fixture)
{
    [SkippableFact]
    public async Task SearchHistories_CanFilterByYearMonthStatusAndTargetUser()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var response = await api.HttpClient.GetAsync("/api/report-histories?targetYearMonth=202605&targetUserId=3&status=PROCESSING");
        var json = await api.ReadJsonAsync(response);

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.Single(json.EnumerateArray());
        var row = json[0];
        Assert.Equal(2, row.GetProperty("reportOutputHistoryId").GetInt32());
        Assert.Equal("202605", row.GetProperty("targetYearMonth").GetString());
        Assert.Equal("鈴木 一郎", row.GetProperty("targetEmployeeName").GetString());
        Assert.Equal("PROCESSING", row.GetProperty("status").GetString());
    }

    [SkippableFact]
    public async Task HistoryDetail_ReturnsJoinedUsersAndFilePath()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var response = await api.HttpClient.GetAsync("/api/report-histories/3");
        var json = await api.ReadJsonAsync(response);

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.Equal(3, json.GetProperty("reportOutputHistoryId").GetInt32());
        Assert.Equal("ERROR", json.GetProperty("status").GetString());
        Assert.Equal("田中 美咲", json.GetProperty("targetUser").GetProperty("employeeName").GetString());
        Assert.Equal("テンプレートファイルが見つかりません。", json.GetProperty("errorMessage").GetString());
    }

    [SkippableFact]
    public async Task HistoryDetail_ReturnsNotFound_WhenHistoryDoesNotExist()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var response = await api.HttpClient.GetAsync("/api/report-histories/999");

        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
    }
}
