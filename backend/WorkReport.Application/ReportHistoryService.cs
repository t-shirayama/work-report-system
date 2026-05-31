using WorkReport.Application.Contracts;
using WorkReport.Application.Interfaces;

namespace WorkReport.Application;

public sealed class ReportHistoryService(IReportHistoryRepository repository)
{
    public Task<IReadOnlyList<ReportHistoryListResponse>> SearchAsync(
        string? targetYearMonth,
        int? targetUserId,
        string? status)
        => repository.SearchAsync(targetYearMonth, targetUserId, status);

    public Task<ReportHistoryDetailResponse?> FindDetailAsync(int id)
        => repository.FindDetailAsync(id);
}
