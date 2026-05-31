using ClosedXML.Excel;
using WorkReport.Api.Reporting;

namespace WorkReport.Tests.Reporting;

public sealed class MonthlyReportWorkbookTests
{
    [Fact]
    public void Create_GeneratesWorkbookWithHeaderDetailsAndCategoryTotals()
    {
        var data = new MonthlyReportData(
            "202605",
            new MonthlyReportUser(2, "sato", "佐藤 花子", "開発部"),
            [
                new MonthlyReportDetail("2026-05-01", "作業日報システム", "DEVELOPMENT", 6.5m, "API実装"),
                new MonthlyReportDetail("2026-05-02", "作業日報システム", "TEST", 2.0m, "テスト追加")
            ],
            [
                new MonthlyReportCategory("DEVELOPMENT", 6.5m),
                new MonthlyReportCategory("TEST", 2.0m)
            ]);

        var bytes = MonthlyReportWorkbook.Create(data);

        Assert.NotEmpty(bytes);

        using var stream = new MemoryStream(bytes);
        using var workbook = new XLWorkbook(stream);
        var sheet = workbook.Worksheet("月次作業報告書");

        Assert.Equal("月次作業報告書", sheet.Cell("A1").GetString());
        Assert.Equal("202605", sheet.Cell("B3").GetString());
        Assert.Equal("佐藤 花子", sheet.Cell("B4").GetString());
        Assert.Equal("開発部", sheet.Cell("B5").GetString());
        Assert.Equal("2026-05-01", sheet.Cell("A8").GetString());
        Assert.Equal("作業日報システム", sheet.Cell("B8").GetString());
        Assert.Equal("DEVELOPMENT", sheet.Cell("C8").GetString());
        Assert.Equal(6.5m, sheet.Cell("D8").GetValue<decimal>());
        Assert.Equal("分類別集計", sheet.Cell("A12").GetString());
        Assert.Equal("DEVELOPMENT", sheet.Cell("A14").GetString());
        Assert.Equal(6.5m, sheet.Cell("B14").GetValue<decimal>());
    }
}
