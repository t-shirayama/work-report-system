using System.Security.Claims;
using Microsoft.AspNetCore.Antiforgery;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WorkReport.Api.Security;
using WorkReport.Application;
using WorkReport.Application.Contracts;
using WorkReport.Application.Mappers;

namespace WorkReport.Api.Controllers;

[Route("api/auth")]
public sealed class AuthController(AuthService authService, IAntiforgery antiforgery) : ApiControllerBase
{
    [AllowAnonymous]
    [HttpGet("csrf")]
    public ActionResult<CsrfResponse> GetCsrf()
    {
        var tokens = antiforgery.GetAndStoreTokens(HttpContext);
        return Ok(new CsrfResponse(tokens.RequestToken ?? ""));
    }

    [AllowAnonymous]
    [HttpPost("login")]
    public async Task<ActionResult<UserResponse>> Login(LoginRequest request)
    {
        await antiforgery.ValidateRequestAsync(HttpContext);
        var user = await authService.AuthenticateAsync(request);
        if (user is null)
        {
            return Unauthorized();
        }

        await SignInAsync(user);
        return Ok(user);
    }

    [Authorize]
    [HttpPost("logout")]
    public async Task<IActionResult> Logout()
    {
        await antiforgery.ValidateRequestAsync(HttpContext);
        await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
        return NoContent();
    }

    [Authorize]
    [HttpGet("me")]
    public ActionResult<UserResponse> Me()
    {
        var current = User.ToCurrentUser();
        return current is null ? Unauthorized() : Ok(current.ToResponse());
    }

    private async Task SignInAsync(UserResponse user)
    {
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
        await HttpContext.SignInAsync(
            CookieAuthenticationDefaults.AuthenticationScheme,
            new ClaimsPrincipal(identity));
    }
}
