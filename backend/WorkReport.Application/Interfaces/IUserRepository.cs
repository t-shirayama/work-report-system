using WorkReport.Application.Contracts;
using WorkReport.Domain.Models.Identity;

namespace WorkReport.Application.Interfaces;

public interface IUserRepository
{
    Task<UserRecord?> FindByLoginIdAsync(string loginId);

    Task<IReadOnlyList<UserResponse>> FindReportTargetUsersAsync();
}
