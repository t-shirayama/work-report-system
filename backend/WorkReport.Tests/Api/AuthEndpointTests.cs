using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Mvc.Testing;

namespace WorkReport.Tests.Api;

public sealed class AuthEndpointTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly WebApplicationFactory<Program> _factory;

    public AuthEndpointTests(WebApplicationFactory<Program> factory)
    {
        _factory = factory;
    }

    [Fact]
    public async Task CsrfEndpoint_ReturnsToken()
    {
        using var client = _factory.CreateClient();

        var response = await client.GetAsync("/api/auth/csrf");
        var body = await response.Content.ReadFromJsonAsync<CsrfBody>();

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        Assert.False(string.IsNullOrWhiteSpace(body?.Token));
    }

    [Fact]
    public async Task ProtectedEndpoint_ReturnsUnauthorized_WhenNotSignedIn()
    {
        using var client = _factory.CreateClient(new WebApplicationFactoryClientOptions
        {
            AllowAutoRedirect = false
        });

        var response = await client.GetAsync("/api/dashboard");

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
    }

    private sealed record CsrfBody(string Token);
}
