namespace WorkReport.Application.Contracts;

public sealed record ReportHistoryDetailResponse(
    int ReportOutputHistoryId,
    string TargetYearMonth,
    UserResponse TargetUser,
    UserResponse CreatedBy,
    string ReportType,
    string FileName,
    string FilePath,
    string Status,
    string? ErrorMessage,
    DateTime CreatedAt,
    DateTime UpdatedAt);
