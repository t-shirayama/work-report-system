using WorkReport.Application.Contracts;
using WorkReport.Domain.Models.Identity;

namespace WorkReport.Application.Mappers;

public static class UserResponseMapper
{
    public static UserResponse ToResponse(this CurrentUser user)
        => new(user.UserId, user.DepartmentId, user.DepartmentName, user.LoginId, user.EmployeeName, user.RoleCode);

    public static UserResponse ToResponse(this UserRecord user)
        => new(user.UserId, user.DepartmentId, user.DepartmentName, user.LoginId, user.EmployeeName, user.RoleCode);
}
