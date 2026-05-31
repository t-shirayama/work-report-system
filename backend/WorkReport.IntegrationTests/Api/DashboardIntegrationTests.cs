using System.Net;
using WorkReport.IntegrationTests.Support;

namespace WorkReport.IntegrationTests.Api;

[Collection(IntegrationTestCollection.Name)]
public sealed class DashboardIntegrationTests(WorkReportApiFixture fixture)
{
    [SkippableFact]
    public async Task Dashboard_ReturnsAggregatesAndRecentActivities()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var response = await api.HttpClient.GetAsync("/api/dashboard");
        var json = await api.ReadJsonAsync(response);

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.True(json.GetProperty("todayWorkReportCount").GetInt32() >= 0);
        Assert.True(json.GetProperty("currentMonthTotalHours").GetDecimal() >= 0);
        Assert.True(json.GetProperty("notOutputMonthlyReportCount").GetInt32() >= 0);
        Assert.True(json.GetProperty("recentActivities").GetArrayLength() > 0);

        var previous = DateTime.MaxValue;
        foreach (var activity in json.GetProperty("recentActivities").EnumerateArray())
        {
            var current = DateTime.Parse(activity.GetProperty("activityAt").GetString()!);
            Assert.True(current <= previous);
            previous = current;
        }
    }
}
