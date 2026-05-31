using WorkReport.Application.Contracts;

namespace WorkReport.Application.Interfaces;

public interface IReportHistoryRepository
{
    Task<IReadOnlyList<ReportHistoryListResponse>> SearchAsync(
        string? targetYearMonth,
        int? targetUserId,
        string? status);

    Task<ReportHistoryDetailResponse?> FindDetailAsync(int id);

    Task<int> InsertSuccessAsync(
        string targetYearMonth,
        int createdBy,
        int targetUserId,
        string fileName,
        string filePath);
}
