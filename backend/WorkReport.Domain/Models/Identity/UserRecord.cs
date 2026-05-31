namespace WorkReport.Domain.Models.Identity;

public sealed record UserRecord(
    int UserId,
    int DepartmentId,
    string DepartmentName,
    string LoginId,
    string PasswordHash,
    string EmployeeName,
    string RoleCode);
