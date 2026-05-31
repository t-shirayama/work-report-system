using System.Net;
using WorkReport.IntegrationTests.Support;

namespace WorkReport.IntegrationTests.Api;

[Collection(IntegrationTestCollection.Name)]
public sealed class AuthIntegrationTests(WorkReportApiFixture fixture)
{
    [SkippableFact]
    public async Task LoginMeLogout_CookieLifecycle_Works()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());

        var login = await api.LoginAsync("admin", "password");
        Assert.Equal(HttpStatusCode.OK, login.StatusCode);
        Assert.Contains(login.Headers, header => header.Key.Equals("Set-Cookie", StringComparison.OrdinalIgnoreCase));

        var me = await api.HttpClient.GetAsync("/api/auth/me");
        Assert.Equal(HttpStatusCode.OK, me.StatusCode);
        var meJson = await api.ReadJsonAsync(me);
        Assert.Equal("admin", meJson.GetProperty("loginId").GetString());
        Assert.Equal("ADMIN", meJson.GetProperty("roleCode").GetString());

        var logout = await api.PostJsonWithCsrfAsync("/api/auth/logout", new { });
        Assert.Equal(HttpStatusCode.NoContent, logout.StatusCode);

        var afterLogout = await api.HttpClient.GetAsync("/api/auth/me");
        Assert.Equal(HttpStatusCode.Unauthorized, afterLogout.StatusCode);
    }

    [SkippableFact]
    public async Task Login_ReturnsUnauthorized_WhenPasswordIsInvalid()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());

        var login = await api.LoginAsync("admin", "wrong-password");

        Assert.Equal(HttpStatusCode.Unauthorized, login.StatusCode);
    }

    [SkippableFact]
    public async Task ProtectedApi_ReturnsUnauthorized_WhenNotSignedIn()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());

        var response = await api.HttpClient.GetAsync("/api/dashboard");

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
    }

    [SkippableFact]
    public async Task UnsafeApi_RejectsRequestWithoutCsrf()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsUserAsync();

        var response = await api.PostJsonWithoutCsrfAsync("/api/work-reports", new
        {
            workDate = "2026-05-31",
            projectName = "CSRF検証",
            workCategory = "TEST",
            workHours = 1,
            workContent = "CSRFなし"
        });

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [SkippableFact]
    public async Task Cors_ReturnsHeaderOnlyForAllowedOrigin()
    {
        await fixture.ResetDatabaseAsync();
        using var allowed = new HttpRequestMessage(HttpMethod.Options, "/api/dashboard");
        allowed.Headers.Add("Origin", "http://localhost:5173");
        allowed.Headers.Add("Access-Control-Request-Method", "GET");

        var allowedResponse = await fixture.CreateClient().SendAsync(allowed);
        Assert.True(allowedResponse.Headers.TryGetValues("Access-Control-Allow-Origin", out var origins));
        Assert.Contains("http://localhost:5173", origins);

        using var denied = new HttpRequestMessage(HttpMethod.Options, "/api/dashboard");
        denied.Headers.Add("Origin", "http://evil.example");
        denied.Headers.Add("Access-Control-Request-Method", "GET");

        var deniedResponse = await fixture.CreateClient().SendAsync(denied);
        Assert.False(deniedResponse.Headers.Contains("Access-Control-Allow-Origin"));
    }
}
