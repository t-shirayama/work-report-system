using System.Net;
using ClosedXML.Excel;
using WorkReport.IntegrationTests.Support;

namespace WorkReport.IntegrationTests.Api;

[Collection(IntegrationTestCollection.Name)]
public sealed class MonthlyReportsIntegrationTests(WorkReportApiFixture fixture)
{
    [SkippableFact]
    public async Task User_CanExportOwnReport_FileIsSaved_HistoryIsRegistered_AndCanDownload()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsUserAsync("sato");

        var export = await api.PostJsonWithCsrfAsync("/api/monthly-reports/export", new
        {
            targetYearMonth = "202605",
            targetUserId = 2
        });
        var bytes = await export.Content.ReadAsByteArrayAsync();

        Assert.Equal(HttpStatusCode.OK, export.StatusCode);
        Assert.NotEmpty(bytes);
        Assert.Contains("monthly-report-202605-sato.xlsx", export.Content.Headers.ContentDisposition?.FileNameStar ?? export.Content.Headers.ContentDisposition?.FileName);

        var history = await fixture.QuerySingleAsync<HistoryRow>("""
            SELECT TOP (1)
                report_output_history_id AS Id,
                file_name AS FileName,
                file_path AS FilePath,
                status AS Status
            FROM report_output_histories
            WHERE target_year_month = '202605'
              AND target_user_id = 2
              AND created_by = 2
            ORDER BY report_output_history_id DESC
            """);
        Assert.Equal("SUCCESS", history.Status);
        Assert.True(File.Exists(history.FilePath));

        using (var workbook = new XLWorkbook(new MemoryStream(bytes)))
        {
            var sheet = workbook.Worksheet("月次作業報告書");
            Assert.Equal("202605", sheet.Cell("B3").GetString());
            Assert.Equal("佐藤 花子", sheet.Cell("B4").GetString());
            Assert.Equal("開発部", sheet.Cell("B5").GetString());
        }

        var download = await api.HttpClient.GetAsync($"/api/report-histories/{history.Id}/download");
        Assert.Equal(HttpStatusCode.OK, download.StatusCode);
        Assert.NotEmpty(await download.Content.ReadAsByteArrayAsync());
    }

    [SkippableFact]
    public async Task Admin_CanExportAnyUserReport()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var export = await api.PostJsonWithCsrfAsync("/api/monthly-reports/export", new
        {
            targetYearMonth = "202605",
            targetUserId = 4
        });

        Assert.Equal(HttpStatusCode.OK, export.StatusCode);

        var count = await fixture.QuerySingleAsync<int>("""
            SELECT COUNT(*)
            FROM report_output_histories
            WHERE target_year_month = '202605'
              AND target_user_id = 4
              AND created_by = 1
              AND status = N'SUCCESS'
            """);
        Assert.Equal(1, count);
    }

    [SkippableFact]
    public async Task User_CannotExportOtherUsersReport()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsUserAsync("sato");

        var export = await api.PostJsonWithCsrfAsync("/api/monthly-reports/export", new
        {
            targetYearMonth = "202605",
            targetUserId = 3
        });

        Assert.Equal(HttpStatusCode.Forbidden, export.StatusCode);
    }

    [SkippableFact]
    public async Task Export_ReturnsBadRequest_WhenYearMonthIsInvalid()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var export = await api.PostJsonWithCsrfAsync("/api/monthly-reports/export", new
        {
            targetYearMonth = "2026AA",
            targetUserId = 2
        });

        Assert.Equal(HttpStatusCode.BadRequest, export.StatusCode);
    }

    [SkippableFact]
    public async Task Export_ReturnsNotFound_WhenTargetUserDoesNotExist()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var export = await api.PostJsonWithCsrfAsync("/api/monthly-reports/export", new
        {
            targetYearMonth = "202605",
            targetUserId = 999
        });

        Assert.Equal(HttpStatusCode.NotFound, export.StatusCode);
    }

    [SkippableFact]
    public async Task Export_Works_WhenMonthHasNoDetails()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var export = await api.PostJsonWithCsrfAsync("/api/monthly-reports/export", new
        {
            targetYearMonth = "202607",
            targetUserId = 2
        });
        var bytes = await export.Content.ReadAsByteArrayAsync();

        Assert.Equal(HttpStatusCode.OK, export.StatusCode);
        using var workbook = new XLWorkbook(new MemoryStream(bytes));
        var sheet = workbook.Worksheet("月次作業報告書");
        Assert.Equal("202607", sheet.Cell("B3").GetString());
        Assert.True(sheet.Cell("A8").IsEmpty());
    }

    [SkippableFact]
    public async Task Download_ReturnsNotFound_WhenHistoryFileDoesNotExist()
    {
        await fixture.ResetDatabaseAsync();
        var api = new ApiClient(fixture.CreateClient());
        await api.LoginAsAdminAsync();

        var download = await api.HttpClient.GetAsync("/api/report-histories/1/download");

        Assert.Equal(HttpStatusCode.NotFound, download.StatusCode);
    }

    private sealed record HistoryRow(int Id, string FileName, string FilePath, string Status);
}
