namespace WorkReport.Application.Contracts;

public sealed record DepartmentResponse(
    int DepartmentId,
    string DepartmentCode,
    string DepartmentName,
    int DisplayOrder);
