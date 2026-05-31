namespace WorkReport.Application.Contracts;

public sealed record DashboardResponse(
    int TodayWorkReportCount,
    decimal CurrentMonthTotalHours,
    int NotOutputMonthlyReportCount,
    IReadOnlyList<DashboardActivityResponse> RecentActivities);
