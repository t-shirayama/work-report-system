namespace WorkReport.Application.Contracts;

public sealed record MasterUserCreateRequest(
    int? DepartmentId,
    string? LoginId,
    string? Password,
    string? EmployeeName,
    string? RoleCode);
