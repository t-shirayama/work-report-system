using System.Net.Http.Json;
using System.Text.Json;

namespace WorkReport.IntegrationTests.Support;

public sealed class ApiClient(HttpClient client)
{
    public HttpClient HttpClient => client;

    public async Task<string> GetCsrfAsync()
    {
        var response = await client.GetAsync("/api/auth/csrf");
        response.EnsureSuccessStatusCode();
        var json = await response.Content.ReadFromJsonAsync<JsonElement>();
        return json.GetProperty("token").GetString() ?? throw new InvalidOperationException("CSRF token was empty.");
    }

    public async Task<HttpResponseMessage> LoginAsync(string loginId, string password)
    {
        var csrf = await GetCsrfAsync();
        return await PostJsonWithCsrfAsync("/api/auth/login", new { loginId, password }, csrf);
    }

    public async Task LoginAsAdminAsync()
    {
        var response = await LoginAsync("admin", "password");
        response.EnsureSuccessStatusCode();
    }

    public async Task LoginAsUserAsync(string loginId = "sato")
    {
        var response = await LoginAsync(loginId, "password");
        response.EnsureSuccessStatusCode();
    }

    public async Task<HttpResponseMessage> PostJsonWithCsrfAsync<T>(
        string path,
        T payload,
        string? csrf = null)
    {
        csrf ??= await GetCsrfAsync();
        using var request = new HttpRequestMessage(HttpMethod.Post, path)
        {
            Content = JsonContent.Create(payload)
        };
        request.Headers.Add("X-CSRF-TOKEN", csrf);
        return await client.SendAsync(request);
    }

    public async Task<HttpResponseMessage> PostJsonWithoutCsrfAsync<T>(string path, T payload)
        => await client.PostAsJsonAsync(path, payload);

    public async Task<HttpResponseMessage> PutJsonWithCsrfAsync<T>(
        string path,
        T payload,
        string? csrf = null)
    {
        csrf ??= await GetCsrfAsync();
        using var request = new HttpRequestMessage(HttpMethod.Put, path)
        {
            Content = JsonContent.Create(payload)
        };
        request.Headers.Add("X-CSRF-TOKEN", csrf);
        return await client.SendAsync(request);
    }

    public async Task<JsonElement> ReadJsonAsync(HttpResponseMessage response)
    {
        var text = await response.Content.ReadAsStringAsync();
        return JsonSerializer.Deserialize<JsonElement>(text, new JsonSerializerOptions
        {
            PropertyNameCaseInsensitive = true
        });
    }
}
