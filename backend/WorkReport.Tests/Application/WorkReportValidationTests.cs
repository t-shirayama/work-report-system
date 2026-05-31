using WorkReport.Api.Application;
using WorkReport.Api.Contracts;

namespace WorkReport.Tests.Application;

public sealed class WorkReportValidationTests
{
    [Fact]
    public void ValidateRegister_ReturnsNoErrors_WhenRequestIsValid()
    {
        var request = new WorkReportRegisterRequest(
            new DateTime(2026, 5, 31),
            "作業日報システム",
            "DEVELOPMENT",
            7.5m,
            "API移行とテスト追加");

        var errors = WorkReportService.ValidateRegister(request);

        Assert.Empty(errors);
    }

    [Fact]
    public void ValidateRegister_ReturnsRequiredErrors_WhenRequestIsBlank()
    {
        var request = new WorkReportRegisterRequest(null, "", "", null, "");

        var errors = WorkReportService.ValidateRegister(request);

        Assert.Contains("作業日は必須です。", errors);
        Assert.Contains("プロジェクト名は必須です。", errors);
        Assert.Contains("作業分類は必須です。", errors);
        Assert.Contains("作業時間は必須です。", errors);
        Assert.Contains("作業内容は必須です。", errors);
    }

    [Theory]
    [InlineData("INVALID", "作業分類の値が正しくありません。")]
    [InlineData("development", "作業分類の値が正しくありません。")]
    public void ValidateRegister_RejectsUnsupportedCategories(string category, string expected)
    {
        var request = new WorkReportRegisterRequest(
            new DateTime(2026, 5, 31),
            "作業日報システム",
            category,
            1m,
            "分類チェック");

        var errors = WorkReportService.ValidateRegister(request);

        Assert.Contains(expected, errors);
    }

    [Theory]
    [InlineData("0", "作業時間は0より大きい数値で入力してください。")]
    [InlineData("-1", "作業時間は0より大きい数値で入力してください。")]
    [InlineData("24.01", "作業時間は24時間以内で入力してください。")]
    public void ValidateRegister_RejectsOutOfRangeHours(string value, string expected)
    {
        var request = new WorkReportRegisterRequest(
            new DateTime(2026, 5, 31),
            "作業日報システム",
            "TEST",
            decimal.Parse(value),
            "時間チェック");

        var errors = WorkReportService.ValidateRegister(request);

        Assert.Contains(expected, errors);
    }

    [Fact]
    public void ValidateRegister_RejectsLongTextFields()
    {
        var request = new WorkReportRegisterRequest(
            new DateTime(2026, 5, 31),
            new string('A', 101),
            "DOCUMENT",
            2m,
            new string('B', 1001));

        var errors = WorkReportService.ValidateRegister(request);

        Assert.Contains("プロジェクト名は100文字以内で入力してください。", errors);
        Assert.Contains("作業内容は1000文字以内で入力してください。", errors);
    }

    [Fact]
    public void ValidateSearch_RejectsDateRangeInversion()
    {
        var request = new WorkReportSearchRequest(
            new DateTime(2026, 6, 1),
            new DateTime(2026, 5, 31),
            null,
            null,
            null,
            null);

        var errors = WorkReportService.ValidateSearch(request);

        Assert.Equal(["対象期間 From は To 以前の日付を入力してください。"], errors);
    }

    [Fact]
    public void ValidateSearch_RejectsUnsupportedCategory()
    {
        var request = new WorkReportSearchRequest(null, null, null, null, "BAD", null);

        var errors = WorkReportService.ValidateSearch(request);

        Assert.Equal(["作業分類の値が正しくありません。"], errors);
    }
}
