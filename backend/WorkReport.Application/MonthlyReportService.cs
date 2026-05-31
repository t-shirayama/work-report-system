using System.Text.RegularExpressions;
using WorkReport.Application.Contracts;
using WorkReport.Application.Interfaces;
using WorkReport.Domain.Models.Identity;

namespace WorkReport.Application;

public sealed class MonthlyReportService(
    IUserRepository userRepository,
    IMonthlyReportRepository monthlyReportRepository,
    IReportHistoryRepository reportHistoryRepository,
    IMonthlyReportWorkbookGenerator workbookGenerator,
    IReportFileStorage reportFileStorage)
{
    public Task<IReadOnlyList<UserResponse>> FindTargetUsersAsync()
        => userRepository.FindReportTargetUsersAsync();

    public static IReadOnlyList<string> ValidateExport(MonthlyReportExportRequest request)
    {
        var errors = new List<string>();
        if (string.IsNullOrWhiteSpace(request.TargetYearMonth) ||
            !Regex.IsMatch(request.TargetYearMonth, "^[0-9]{6}$"))
        {
            errors.Add("対象年月は6桁の年月で入力してください。");
        }

        if (request.TargetUserId <= 0)
        {
            errors.Add("対象者を選択してください。");
        }

        return errors;
    }

    public async Task<ReportFileResult> ExportAsync(MonthlyReportExportRequest request, CurrentUser currentUser)
    {
        if (!currentUser.IsAdmin && request.TargetUserId != currentUser.UserId)
        {
            throw new UnauthorizedAccessException("一般ユーザーは自分の帳票のみ出力できます。");
        }

        var report = await monthlyReportRepository.BuildMonthlyReportAsync(request.TargetYearMonth, request.TargetUserId);
        var fileName = $"monthly-report-{request.TargetYearMonth}-{report.User.LoginId}.xlsx";
        var bytes = workbookGenerator.Create(report);

        var relativePath = Path.Combine(request.TargetYearMonth, fileName);
        var storedPath = await reportFileStorage.SaveAsync(relativePath, bytes);

        await reportHistoryRepository.InsertSuccessAsync(
            request.TargetYearMonth,
            currentUser.UserId,
            request.TargetUserId,
            fileName,
            storedPath);

        return new ReportFileResult(bytes, fileName);
    }

    public async Task<ReportFileResult?> DownloadHistoryAsync(int historyId)
    {
        var detail = await reportHistoryRepository.FindDetailAsync(historyId);
        if (detail is null)
        {
            return null;
        }

        var content = await reportFileStorage.ReadAsync(detail.FilePath);
        return content is null ? null : new ReportFileResult(content, detail.FileName);
    }
}
