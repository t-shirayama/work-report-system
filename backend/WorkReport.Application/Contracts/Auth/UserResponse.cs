namespace WorkReport.Application.Contracts;

public sealed record UserResponse(
    int UserId,
    int DepartmentId,
    string DepartmentName,
    string LoginId,
    string EmployeeName,
    string RoleCode);
