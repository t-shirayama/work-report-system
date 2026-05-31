using System.Security.Claims;
using WorkReport.Api.Security;
using WorkReport.Application.Mappers;
using WorkReport.Domain.Models.Identity;

namespace WorkReport.Tests.Application;

public sealed class CurrentUserTests
{
    [Fact]
    public void FromPrincipal_ReturnsCurrentUser_WhenRequiredClaimsExist()
    {
        var principal = new ClaimsPrincipal(new ClaimsIdentity(
        [
            new Claim(ClaimTypes.NameIdentifier, "12"),
            new Claim(ClaimTypes.Name, "佐藤 花子"),
            new Claim(ClaimTypes.Role, "USER"),
            new Claim("department_id", "3"),
            new Claim("department_name", "開発部"),
            new Claim("login_id", "sato")
        ], "Test"));

        var current = principal.ToCurrentUser();

        Assert.NotNull(current);
        Assert.Equal(12, current.UserId);
        Assert.Equal(3, current.DepartmentId);
        Assert.Equal("開発部", current.DepartmentName);
        Assert.Equal("sato", current.LoginId);
        Assert.Equal("佐藤 花子", current.EmployeeName);
        Assert.Equal("USER", current.RoleCode);
        Assert.False(current.IsAdmin);
    }

    [Fact]
    public void FromPrincipal_ReturnsNull_WhenPrincipalIsAnonymous()
    {
        var current = new ClaimsPrincipal(new ClaimsIdentity()).ToCurrentUser();

        Assert.Null(current);
    }

    [Fact]
    public void ToResponse_DoesNotExposePasswordFields()
    {
        var response = new CurrentUser(1, 2, "品質管理部", "tanaka", "田中 美咲", "ADMIN").ToResponse();

        Assert.Equal(1, response.UserId);
        Assert.Equal("ADMIN", response.RoleCode);
        Assert.DoesNotContain("Password", response.GetType().GetProperties().Select(property => property.Name));
    }
}
