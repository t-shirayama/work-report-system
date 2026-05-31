using WorkReport.Application.Contracts;

namespace WorkReport.Application.Masters;

public static class UserValidator
{
    public static MasterUserCreateRequest Normalize(MasterUserCreateRequest request)
        => new(
            request.DepartmentId,
            request.LoginId?.Trim(),
            request.Password,
            request.EmployeeName?.Trim(),
            request.RoleCode?.Trim().ToUpperInvariant());

    public static MasterUserUpdateRequest Normalize(MasterUserUpdateRequest request)
        => new(
            request.DepartmentId,
            request.LoginId?.Trim(),
            request.Password,
            request.EmployeeName?.Trim(),
            request.RoleCode?.Trim().ToUpperInvariant());

    public static IReadOnlyList<string> Validate(MasterUserCreateRequest request, bool requirePassword)
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
        else if (!RoleCode.IsSupported(request.RoleCode))
        {
            errors.Add("権限の値が正しくありません。");
        }

        return errors;
    }

    public static IReadOnlyList<string> Validate(MasterUserUpdateRequest request, bool requirePassword)
        => Validate(new MasterUserCreateRequest(
            request.DepartmentId,
            request.LoginId,
            request.Password,
            request.EmployeeName,
            request.RoleCode), requirePassword);
}
