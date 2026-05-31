namespace WorkReport.Api.Contracts;

public sealed record CsrfResponse(string Token);

public sealed record LoginRequest(string LoginId, string Password);

public sealed record UserResponse(
    int UserId,
    int DepartmentId,
    string DepartmentName,
    string LoginId,
    string EmployeeName,
    string RoleCode);

public sealed record ErrorResponse(IReadOnlyList<string> Errors);

public sealed record DashboardResponse(
    int TodayWorkReportCount,
    decimal CurrentMonthTotalHours,
    int NotOutputMonthlyReportCount,
    IReadOnlyList<DashboardActivityResponse> RecentActivities);

public sealed record DashboardActivityResponse(
    string ActivityAt,
    string ActivityType,
    string ActivityTypeName,
    string BadgeClass,
    string Content,
    string EmployeeName);

public sealed record WorkReportRegisterRequest(
    DateTime? WorkDate,
    string? ProjectName,
    string? WorkCategory,
    decimal? WorkHours,
    string? WorkContent);

public sealed record WorkReportRegisterResponse(int WorkReportId);

public sealed record WorkReportSearchRequest(
    DateTime? DateFrom,
    DateTime? DateTo,
    string? EmployeeName,
    string? DepartmentName,
    string? WorkCategory,
    string? ProjectName);

public sealed record WorkReportSearchResultResponse(
    string WorkDate,
    string EmployeeName,
    string DepartmentName,
    string ProjectName,
    string WorkCategory,
    string WorkCategoryName,
    decimal WorkHours,
    string WorkContent);

public sealed record MonthlyReportExportRequest(string TargetYearMonth, int TargetUserId);

public sealed record ReportFileResult(byte[] Content, string FileName);

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
