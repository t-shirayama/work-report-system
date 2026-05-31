namespace WorkReport.Domain.Models.Reporting;

public sealed record MonthlyReportData(
    string TargetYearMonth,
    MonthlyReportUser User,
    IReadOnlyList<MonthlyReportDetail> Details,
    IReadOnlyList<MonthlyReportCategory> Categories);

public sealed record MonthlyReportUser(int UserId, string LoginId, string EmployeeName, string DepartmentName);

public sealed record MonthlyReportDetail(
    string WorkDate,
    string ProjectName,
    string WorkCategory,
    decimal WorkHours,
    string WorkContent);

public sealed record MonthlyReportCategory(string WorkCategory, decimal TotalHours);
