using Microsoft.Data.SqlClient;
using WorkReport.Api.Contracts;
using WorkReport.Api.Infrastructure;

namespace WorkReport.Api.Application;

public sealed class MasterDataService(MasterDataRepository repository)
{
    private static readonly HashSet<string> RoleCodes = new(StringComparer.Ordinal)
    {
        "ADMIN", "USER"
    };

    public Task<IReadOnlyList<DepartmentResponse>> FindDepartmentsAsync()
        => repository.FindDepartmentsAsync();

    public Task<IReadOnlyList<MasterUserResponse>> FindUsersAsync()
        => repository.FindUsersAsync();

    public async Task<MasterResult<DepartmentResponse>> CreateDepartmentAsync(DepartmentUpsertRequest request)
    {
        var normalized = NormalizeDepartment(request);
        var errors = ValidateDepartment(normalized);
        if (errors.Count > 0)
        {
            return MasterResult<DepartmentResponse>.Failed(errors);
        }

        try
        {
            var id = await repository.InsertDepartmentAsync(normalized);
            return MasterResult<DepartmentResponse>.Succeeded((await repository.FindDepartmentAsync(id))!);
        }
        catch (SqlException ex) when (IsUniqueConstraint(ex))
        {
            return MasterResult<DepartmentResponse>.Failed(["部署コードは既に使用されています。"]);
        }
    }

    public async Task<MasterResult<DepartmentResponse>> UpdateDepartmentAsync(int id, DepartmentUpsertRequest request)
    {
        var normalized = NormalizeDepartment(request);
        var errors = ValidateDepartment(normalized);
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
        catch (SqlException ex) when (IsUniqueConstraint(ex))
        {
            return MasterResult<DepartmentResponse>.Failed(["部署コードは既に使用されています。"]);
        }
    }

    public async Task<MasterResult<MasterUserResponse>> CreateUserAsync(MasterUserCreateRequest request)
    {
        var normalized = NormalizeCreateUser(request);
        var errors = ValidateUser(normalized, requirePassword: true);
        if (errors.Count > 0)
        {
            return MasterResult<MasterUserResponse>.Failed(errors);
        }

        var passwordHash = BCrypt.Net.BCrypt.HashPassword(normalized.Password);
        try
        {
            var id = await repository.InsertUserAsync(normalized, passwordHash);
            return MasterResult<MasterUserResponse>.Succeeded((await repository.FindUserAsync(id))!);
        }
        catch (SqlException ex) when (IsUniqueConstraint(ex))
        {
            return MasterResult<MasterUserResponse>.Failed(["ログインIDは既に使用されています。"]);
        }
        catch (SqlException ex) when (IsForeignKeyConstraint(ex))
        {
            return MasterResult<MasterUserResponse>.Failed(["部署が存在しません。"]);
        }
    }

    public async Task<MasterResult<MasterUserResponse>> UpdateUserAsync(int id, MasterUserUpdateRequest request)
    {
        var normalized = NormalizeUpdateUser(request);
        var errors = ValidateUser(normalized, requirePassword: false);
        if (errors.Count > 0)
        {
            return MasterResult<MasterUserResponse>.Failed(errors);
        }

        var passwordHash = string.IsNullOrWhiteSpace(normalized.Password)
            ? null
            : BCrypt.Net.BCrypt.HashPassword(normalized.Password);

        try
        {
            var updated = await repository.UpdateUserAsync(id, normalized, passwordHash);
            if (!updated)
            {
                return MasterResult<MasterUserResponse>.NotFound();
            }

            return MasterResult<MasterUserResponse>.Succeeded((await repository.FindUserAsync(id))!);
        }
        catch (SqlException ex) when (IsUniqueConstraint(ex))
        {
            return MasterResult<MasterUserResponse>.Failed(["ログインIDは既に使用されています。"]);
        }
        catch (SqlException ex) when (IsForeignKeyConstraint(ex))
        {
            return MasterResult<MasterUserResponse>.Failed(["部署が存在しません。"]);
        }
    }

    private static DepartmentUpsertRequest NormalizeDepartment(DepartmentUpsertRequest request)
        => new(
            request.DepartmentCode?.Trim().ToUpperInvariant(),
            request.DepartmentName?.Trim(),
            request.DisplayOrder ?? 0);

    private static MasterUserCreateRequest NormalizeCreateUser(MasterUserCreateRequest request)
        => new(
            request.DepartmentId,
            request.LoginId?.Trim(),
            request.Password,
            request.EmployeeName?.Trim(),
            request.RoleCode?.Trim().ToUpperInvariant());

    private static MasterUserUpdateRequest NormalizeUpdateUser(MasterUserUpdateRequest request)
        => new(
            request.DepartmentId,
            request.LoginId?.Trim(),
            request.Password,
            request.EmployeeName?.Trim(),
            request.RoleCode?.Trim().ToUpperInvariant());

    private static IReadOnlyList<string> ValidateDepartment(DepartmentUpsertRequest request)
    {
        var errors = new List<string>();
        if (string.IsNullOrWhiteSpace(request.DepartmentCode))
        {
            errors.Add("部署コードは必須です。");
        }
        else if (request.DepartmentCode.Length > 20)
        {
            errors.Add("部署コードは20文字以内で入力してください。");
        }

        if (string.IsNullOrWhiteSpace(request.DepartmentName))
        {
            errors.Add("部署名は必須です。");
        }
        else if (request.DepartmentName.Length > 100)
        {
            errors.Add("部署名は100文字以内で入力してください。");
        }

        if (request.DisplayOrder < 0)
        {
            errors.Add("表示順は0以上で入力してください。");
        }

        return errors;
    }

    private static IReadOnlyList<string> ValidateUser(MasterUserCreateRequest request, bool requirePassword)
    {
        var errors = new List<string>();
        if (!request.DepartmentId.HasValue || request.DepartmentId <= 0)
        {
            errors.Add("部署は必須です。");
        }

        if (string.IsNullOrWhiteSpace(request.LoginId))
        {
            errors.Add("ログインIDは必須です。");
        }
        else if (request.LoginId.Length > 50)
        {
            errors.Add("ログインIDは50文字以内で入力してください。");
        }

        if (requirePassword && string.IsNullOrWhiteSpace(request.Password))
        {
            errors.Add("パスワードは必須です。");
        }
        else if (!string.IsNullOrWhiteSpace(request.Password) && request.Password.Length < 8)
        {
            errors.Add("パスワードは8文字以上で入力してください。");
        }

        if (string.IsNullOrWhiteSpace(request.EmployeeName))
        {
            errors.Add("社員名は必須です。");
        }
        else if (request.EmployeeName.Length > 100)
        {
            errors.Add("社員名は100文字以内で入力してください。");
        }

        if (string.IsNullOrWhiteSpace(request.RoleCode))
        {
            errors.Add("権限は必須です。");
        }
        else if (!RoleCodes.Contains(request.RoleCode))
        {
            errors.Add("権限の値が正しくありません。");
        }

        return errors;
    }

    private static IReadOnlyList<string> ValidateUser(MasterUserUpdateRequest request, bool requirePassword)
        => ValidateUser(new MasterUserCreateRequest(
            request.DepartmentId,
            request.LoginId,
            request.Password,
            request.EmployeeName,
            request.RoleCode), requirePassword);

    private static bool IsUniqueConstraint(SqlException ex)
        => ex.Number is 2601 or 2627;

    private static bool IsForeignKeyConstraint(SqlException ex)
        => ex.Number == 547;
}

public sealed record MasterResult<T>(T? Value, IReadOnlyList<string> Errors, bool Missing)
{
    public static MasterResult<T> Succeeded(T value)
        => new(value, [], false);

    public static MasterResult<T> Failed(IReadOnlyList<string> errors)
        => new(default, errors, false);

    public static MasterResult<T> NotFound()
        => new(default, [], true);
}
