namespace WorkReport.Application.Contracts;

public sealed record WorkReportRegisterRequest(
    DateTime? WorkDate,
    string? ProjectName,
    string? WorkCategory,
    decimal? WorkHours,
    string? WorkContent);
