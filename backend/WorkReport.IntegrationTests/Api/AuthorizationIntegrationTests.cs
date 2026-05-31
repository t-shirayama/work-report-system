using System.Net;
using WorkReport.IntegrationTests.Support;

namespace WorkReport.IntegrationTests.Api;

[Collection(IntegrationTestCollection.Name)]
public sealed class AuthorizationIntegrationTests(WorkReportApiFixture fixture)
{
    [SkippableFact]
    public async Task Admin_CanReadReportTargetUsers()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var response = await api.HttpClient.GetAsync("/api/monthly-reports/target-users");
        var json = await api.ReadJsonAsync(response);

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.Equal(4, json.GetArrayLength());
    }

    [SkippableFact]
    public async Task User_CannotReadReportTargetUsers()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsUserAsync();

        var response = await api.HttpClient.GetAsync("/api/monthly-reports/target-users");

        Assert.Equal(HttpStatusCode.Forbidden, response.StatusCode);
    }
}
