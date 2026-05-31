using System.Security.Claims;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using WorkReport.Api.Contracts;
using WorkReport.Api.Infrastructure;

namespace WorkReport.Api.Application;

public sealed class AuthService(UserRepository userRepository)
{
    public async Task<UserResponse?> SignInAsync(HttpContext http, LoginRequest request)
    {
        var user = await userRepository.FindByLoginIdAsync(request.LoginId);
        if (user is null || !BCrypt.Net.BCrypt.Verify(request.Password, user.PasswordHash))
        {
            return null;
        }

        var claims = new List<Claim>
        {
            new(ClaimTypes.NameIdentifier, user.UserId.ToString()),
            new(ClaimTypes.Name, user.EmployeeName),
            new(ClaimTypes.Role, user.RoleCode),
            new("department_id", user.DepartmentId.ToString()),
            new("department_name", user.DepartmentName),
            new("login_id", user.LoginId)
        };

        var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);
        await http.SignInAsync(
            CookieAuthenticationDefaults.AuthenticationScheme,
            new ClaimsPrincipal(identity));

        return user.ToResponse();
    }

    public static Task SignOutAsync(HttpContext http)
        => http.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
}
