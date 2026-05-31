namespace WorkReport.Application.Contracts;

public sealed record ReportHistoryListResponse(
    int ReportOutputHistoryId,
    string TargetYearMonth,
    string TargetEmployeeName,
    string CreatedByEmployeeName,
    string ReportType,
    string FileName,
    string Status,
    string? ErrorMessage,
    DateTime CreatedAt);
