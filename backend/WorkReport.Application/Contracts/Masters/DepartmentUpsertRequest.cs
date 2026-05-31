namespace WorkReport.Application.Contracts;

public sealed record DepartmentUpsertRequest(
    string? DepartmentCode,
    string? DepartmentName,
    int? DisplayOrder);
