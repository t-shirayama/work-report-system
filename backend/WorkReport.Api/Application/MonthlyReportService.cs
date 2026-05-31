using Microsoft.Extensions.Options;
using WorkReport.Api.Contracts;
using WorkReport.Api.Infrastructure;
using WorkReport.Api.Reporting;

namespace WorkReport.Api.Application;

public sealed class MonthlyReportService(
    MonthlyReportRepository monthlyReportRepository,
    ReportHistoryRepository reportHistoryRepository,
    IOptions<ReportStorageOptions> storageOptions)
{
    public async Task<ReportFileResult> ExportAsync(MonthlyReportExportRequest request, CurrentUser currentUser)
    {
        if (!currentUser.IsAdmin && request.TargetUserId != currentUser.UserId)
        {
            throw new UnauthorizedAccessException("一般ユーザーは自分の帳票のみ出力できます。");
        }

        var report = await monthlyReportRepository.BuildMonthlyReportAsync(request.TargetYearMonth, request.TargetUserId);
        var fileName = $"monthly-report-{request.TargetYearMonth}-{report.User.LoginId}.xlsx";
        var bytes = MonthlyReportWorkbook.Create(report);

        var relativePath = Path.Combine(request.TargetYearMonth, fileName);
        var basePath = storageOptions.Value.BasePath ?? "generated-reports";
        var fullPath = Path.Combine(basePath, relativePath);
        Directory.CreateDirectory(Path.GetDirectoryName(fullPath)!);
        await File.WriteAllBytesAsync(fullPath, bytes);

        await reportHistoryRepository.InsertSuccessAsync(
            request.TargetYearMonth,
            currentUser.UserId,
            request.TargetUserId,
            fileName,
            Path.Combine(basePath, relativePath).Replace('\\', '/'));

        return new ReportFileResult(bytes, fileName);
    }

    public async Task<ReportFileResult?> DownloadHistoryAsync(int historyId)
    {
        var detail = await reportHistoryRepository.FindDetailAsync(historyId);
        if (detail is null || !File.Exists(detail.FilePath))
        {
            return null;
        }

        return new ReportFileResult(await File.ReadAllBytesAsync(detail.FilePath), detail.FileName);
    }
}
