namespace WorkReport.Application.Contracts;

public sealed record MasterUserUpdateRequest(
    int? DepartmentId,
    string? LoginId,
    string? Password,
    string? EmployeeName,
    string? RoleCode);
