using Dapper;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Data.SqlClient;
using Microsoft.Extensions.Configuration;
using Testcontainers.MsSql;

namespace WorkReport.IntegrationTests.Support;

public sealed class WorkReportApiFixture : IAsyncLifetime
{
    private MsSqlContainer? _database;
    private WebApplicationFactory<Program>? _factory;
    private string? _skipReason;

    public string ConnectionString => _database?.GetConnectionString()
        ?? throw new InvalidOperationException("SQL Server container is not available.");

    public string ReportStoragePath { get; } = Path.Combine(
        Path.GetTempPath(),
        "work-report-it",
        Guid.NewGuid().ToString("N"));

    public async Task InitializeAsync()
    {
        try
        {
            _database = new MsSqlBuilder("mcr.microsoft.com/mssql/server:2022-latest").Build();
            await _database.StartAsync();
        }
        catch (Exception ex)
        {
            _skipReason = $"Docker/Testcontainers is unavailable: {ex.Message}";
            return;
        }

        await ResetDatabaseAsync();

        _factory = new WebApplicationFactory<Program>()
            .WithWebHostBuilder(builder =>
            {
                builder.ConfigureAppConfiguration((_, config) =>
                {
                    config.AddInMemoryCollection(new Dictionary<string, string?>
                    {
                        ["ConnectionStrings:WorkReport"] = ConnectionString,
                        ["ReportStorage:BasePath"] = ReportStoragePath,
                        ["Cors:AllowedOrigins:0"] = "http://localhost:5173"
                    });
                });
            });
    }

    public async Task DisposeAsync()
    {
        _factory?.Dispose();
        if (Directory.Exists(ReportStoragePath))
        {
            Directory.Delete(ReportStoragePath, recursive: true);
        }

        if (_database is not null)
        {
            await _database.DisposeAsync();
        }
    }

    public HttpClient CreateClient() => (_factory ?? throw new InvalidOperationException("Fixture is not initialized."))
        .CreateClient(new WebApplicationFactoryClientOptions
        {
            AllowAutoRedirect = false
        });

    public async Task ResetDatabaseAsync()
    {
        Skip.If(_database is null, _skipReason ?? "Docker/Testcontainers is unavailable.");

        if (Directory.Exists(ReportStoragePath))
        {
            Directory.Delete(ReportStoragePath, recursive: true);
        }

        Directory.CreateDirectory(ReportStoragePath);

        await using var connection = new SqlConnection(ConnectionString);
        await connection.OpenAsync();

        await connection.ExecuteAsync("""
            IF OBJECT_ID('report_output_histories', 'U') IS NOT NULL DROP TABLE report_output_histories;
            IF OBJECT_ID('work_reports', 'U') IS NOT NULL DROP TABLE work_reports;
            IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;
            IF OBJECT_ID('departments', 'U') IS NOT NULL DROP TABLE departments;
            """);

        await connection.ExecuteAsync(ReadSql("schema.sql"));
        await connection.ExecuteAsync(ReadSql("seed.sql"));
    }

    public async Task<T> QuerySingleAsync<T>(string sql, object? parameters = null)
    {
        await using var connection = new SqlConnection(ConnectionString);
        return await connection.QuerySingleAsync<T>(sql, parameters);
    }

    public async Task<IReadOnlyList<T>> QueryAsync<T>(string sql, object? parameters = null)
    {
        await using var connection = new SqlConnection(ConnectionString);
        var rows = await connection.QueryAsync<T>(sql, parameters);
        return rows.AsList();
    }

    private static string ReadSql(string fileName)
    {
        var current = AppContext.BaseDirectory;
        while (!string.IsNullOrWhiteSpace(current))
        {
            var candidate = Path.Combine(current, "database", "sqlserver", fileName);
            if (File.Exists(candidate))
            {
                return File.ReadAllText(candidate);
            }

            current = Directory.GetParent(current)?.FullName;
        }

        throw new FileNotFoundException($"Could not find database/sqlserver/{fileName}.");
    }
}
