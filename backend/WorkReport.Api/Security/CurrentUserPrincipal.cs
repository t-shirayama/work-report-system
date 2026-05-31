using System.Security.Claims;
using WorkReport.Domain.Models.Identity;

namespace WorkReport.Api.Security;

public static class CurrentUserPrincipal
{
    public static CurrentUser RequireCurrentUser(this ClaimsPrincipal principal)
        => principal.ToCurrentUser() ?? throw new UnauthorizedAccessException("Login user is required.");

    public static CurrentUser? ToCurrentUser(this ClaimsPrincipal principal)
    {
        if (principal.Identity?.IsAuthenticated != true)
        {
            return null;
        }

        var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        var departmentId = principal.FindFirst("department_id")?.Value;
        var roleCode = principal.FindFirst(ClaimTypes.Role)?.Value;

        if (!int.TryParse(userId, out var parsedUserId) ||
            !int.TryParse(departmentId, out var parsedDepartmentId) ||
            string.IsNullOrWhiteSpace(roleCode))
        {
            return null;
        }

        return new CurrentUser(
            parsedUserId,
            parsedDepartmentId,
            principal.FindFirst("department_name")?.Value ?? "",
            principal.FindFirst("login_id")?.Value ?? "",
            principal.FindFirst(ClaimTypes.Name)?.Value ?? "",
            roleCode);
    }
}
