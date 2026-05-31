using WorkReport.Application.Contracts;

namespace WorkReport.Application.Masters;

public static class DepartmentValidator
{
    public static DepartmentUpsertRequest Normalize(DepartmentUpsertRequest request)
        => new(
            request.DepartmentCode?.Trim().ToUpperInvariant(),
            request.DepartmentName?.Trim(),
            request.DisplayOrder ?? 0);

    public static IReadOnlyList<string> Validate(DepartmentUpsertRequest request)
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
}
