namespace WorkReport.Domain.Models.Identity;

public sealed record CurrentUser(
    int UserId,
    int DepartmentId,
    string DepartmentName,
    string LoginId,
    string EmployeeName,
    string RoleCode)
{
    public bool IsAdmin => RoleCode == "ADMIN";
}
