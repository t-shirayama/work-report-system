using WorkReport.Application.Contracts;
using WorkReport.Application.Exceptions;
using WorkReport.Application.Interfaces;
using WorkReport.Application.Masters;
using WorkReport.Application.Models.Results;

namespace WorkReport.Application;

public sealed class MasterDataService(IMasterDataRepository repository, IPasswordHasher passwordHasher)
{
    public Task<IReadOnlyList<DepartmentResponse>> FindDepartmentsAsync()
        => repository.FindDepartmentsAsync();

    public Task<IReadOnlyList<MasterUserResponse>> FindUsersAsync()
        => repository.FindUsersAsync();

    public async Task<MasterResult<DepartmentResponse>> CreateDepartmentAsync(DepartmentUpsertRequest request)
    {
        var normalized = DepartmentValidator.Normalize(request);
        var errors = DepartmentValidator.Validate(normalized);
        if (errors.Count > 0)
        {
            return MasterResult<DepartmentResponse>.Failed(errors);
        }

        try
        {
            var id = await repository.InsertDepartmentAsync(normalized);
            return MasterResult<DepartmentResponse>.Succeeded((await repository.FindDepartmentAsync(id))!);
        }
        catch (RepositoryConstraintException ex) when (ex.Constraint == RepositoryConstraint.Unique)
        {
            return MasterResult<DepartmentResponse>.Failed(["部署コードは既に使用されています。"]);
        }
    }

    public async Task<MasterResult<DepartmentResponse>> UpdateDepartmentAsync(int id, DepartmentUpsertRequest request)
    {
        var normalized = DepartmentValidator.Normalize(request);
        var errors = DepartmentValidator.Validate(normalized);
        if (errors.Count > 0)
        {
            return MasterResult<DepartmentResponse>.Failed(errors);
        }

        try
        {
            var updated = await repository.UpdateDepartmentAsync(id, normalized);
            if (!updated)
            {
                return MasterResult<DepartmentResponse>.NotFound();
            }

            return MasterResult<DepartmentResponse>.Succeeded((await repository.FindDepartmentAsync(id))!);
        }
        catch (RepositoryConstraintException ex) when (ex.Constraint == RepositoryConstraint.Unique)
        {
            return MasterResult<DepartmentResponse>.Failed(["部署コードは既に使用されています。"]);
        }
    }

    public async Task<MasterResult<MasterUserResponse>> CreateUserAsync(MasterUserCreateRequest request)
    {
        var normalized = UserValidator.Normalize(request);
        var errors = UserValidator.Validate(normalized, requirePassword: true);
        if (errors.Count > 0)
        {
            return MasterResult<MasterUserResponse>.Failed(errors);
        }

        var passwordHash = passwordHasher.Hash(normalized.Password!);
        try
        {
            var id = await repository.InsertUserAsync(normalized, passwordHash);
            return MasterResult<MasterUserResponse>.Succeeded((await repository.FindUserAsync(id))!);
        }
        catch (RepositoryConstraintException ex) when (ex.Constraint == RepositoryConstraint.Unique)
        {
            return MasterResult<MasterUserResponse>.Failed(["ログインIDは既に使用されています。"]);
        }
        catch (RepositoryConstraintException ex) when (ex.Constraint == RepositoryConstraint.ForeignKey)
        {
            return MasterResult<MasterUserResponse>.Failed(["部署が存在しません。"]);
        }
    }

    public async Task<MasterResult<MasterUserResponse>> UpdateUserAsync(int id, MasterUserUpdateRequest request)
    {
        var normalized = UserValidator.Normalize(request);
        var errors = UserValidator.Validate(normalized, requirePassword: false);
        if (errors.Count > 0)
        {
            return MasterResult<MasterUserResponse>.Failed(errors);
        }

        var passwordHash = string.IsNullOrWhiteSpace(normalized.Password)
            ? null
            : passwordHasher.Hash(normalized.Password);

        try
        {
            var updated = await repository.UpdateUserAsync(id, normalized, passwordHash);
            if (!updated)
            {
                return MasterResult<MasterUserResponse>.NotFound();
            }

            return MasterResult<MasterUserResponse>.Succeeded((await repository.FindUserAsync(id))!);
        }
        catch (RepositoryConstraintException ex) when (ex.Constraint == RepositoryConstraint.Unique)
        {
            return MasterResult<MasterUserResponse>.Failed(["ログインIDは既に使用されています。"]);
        }
        catch (RepositoryConstraintException ex) when (ex.Constraint == RepositoryConstraint.ForeignKey)
        {
            return MasterResult<MasterUserResponse>.Failed(["部署が存在しません。"]);
        }
    }

}
