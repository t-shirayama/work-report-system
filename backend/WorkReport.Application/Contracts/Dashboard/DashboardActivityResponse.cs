namespace WorkReport.Application.Contracts;

public sealed record DashboardActivityResponse(
    string ActivityAt,
    string ActivityType,
    string ActivityTypeName,
    string BadgeClass,
    string Content,
    string EmployeeName);
