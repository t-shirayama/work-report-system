using WorkReport.Application.Contracts;

namespace WorkReport.Application.Interfaces;

public interface IMasterDataRepository
{
    Task<IReadOnlyList<DepartmentResponse>> FindDepartmentsAsync();

    Task<DepartmentResponse?> FindDepartmentAsync(int departmentId);

    Task<int> InsertDepartmentAsync(DepartmentUpsertRequest request);

    Task<bool> UpdateDepartmentAsync(int departmentId, DepartmentUpsertRequest request);

    Task<IReadOnlyList<MasterUserResponse>> FindUsersAsync();

    Task<MasterUserResponse?> FindUserAsync(int userId);

    Task<int> InsertUserAsync(MasterUserCreateRequest request, string passwordHash);

    Task<bool> UpdateUserAsync(int userId, MasterUserUpdateRequest request, string? passwordHash);
}
