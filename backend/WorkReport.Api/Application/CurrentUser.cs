using System.Security.Claims;
using WorkReport.Api.Contracts;

namespace WorkReport.Api.Application;

public sealed record CurrentUser(
    int UserId,
    int DepartmentId,
    string DepartmentName,
    string LoginId,
    string EmployeeName,
    string RoleCode)
{
    public bool IsAdmin => RoleCode == "ADMIN";

    public UserResponse ToResponse()
        => new(UserId, DepartmentId, DepartmentName, LoginId, EmployeeName, RoleCode);

    public static CurrentUser Require(ClaimsPrincipal principal)
        => FromPrincipal(principal) ?? throw new UnauthorizedAccessException("Login user is required.");

    public static CurrentUser? FromPrincipal(ClaimsPrincipal principal)
    {
        if (principal.Identity?.IsAuthenticated != true)
        {
            return null;
        }

        var userId = principal.FindFirstValue(ClaimTypes.NameIdentifier);
        var departmentId = principal.FindFirstValue("department_id");
        var roleCode = principal.FindFirstValue(ClaimTypes.Role);

        if (!int.TryParse(userId, out var parsedUserId) ||
            !int.TryParse(departmentId, out var parsedDepartmentId) ||
            string.IsNullOrWhiteSpace(roleCode))
        {
            return null;
        }

        return new CurrentUser(
            parsedUserId,
            parsedDepartmentId,
            principal.FindFirstValue("department_name") ?? "",
            principal.FindFirstValue("login_id") ?? "",
            principal.FindFirstValue(ClaimTypes.Name) ?? "",
            roleCode);
    }
}
