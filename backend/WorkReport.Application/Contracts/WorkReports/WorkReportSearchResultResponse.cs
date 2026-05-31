namespace WorkReport.Application.Contracts;

public sealed record WorkReportSearchResultResponse(
    string WorkDate,
    string EmployeeName,
    string DepartmentName,
    string ProjectName,
    string WorkCategory,
    string WorkCategoryName,
    decimal WorkHours,
    string WorkContent);
