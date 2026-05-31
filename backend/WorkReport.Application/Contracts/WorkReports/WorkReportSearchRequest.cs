namespace WorkReport.Application.Contracts;

public sealed record WorkReportSearchRequest(
    DateTime? DateFrom,
    DateTime? DateTo,
    string? EmployeeName,
    string? DepartmentName,
    string? WorkCategory,
    string? ProjectName);
