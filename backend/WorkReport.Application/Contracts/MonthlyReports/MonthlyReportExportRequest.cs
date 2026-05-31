namespace WorkReport.Application.Contracts;

public sealed record MonthlyReportExportRequest(string TargetYearMonth, int TargetUserId);
