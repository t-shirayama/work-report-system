using WorkReport.Api.Contracts;
using WorkReport.Api.Infrastructure;

namespace WorkReport.Api.Application;

public sealed class WorkReportService(WorkReportRepository repository)
{
    private static readonly HashSet<string> WorkCategories = new(StringComparer.Ordinal)
    {
        "DESIGN", "DEVELOPMENT", "TEST", "MEETING", "DOCUMENT", "OTHER"
    };

    public async Task<RegisterResult> RegisterAsync(WorkReportRegisterRequest request, CurrentUser currentUser)
    {
        var errors = ValidateRegister(request);
        if (errors.Count > 0)
        {
            return new RegisterResult(null, errors);
        }

        var id = await repository.InsertAsync(request, currentUser);
        return new RegisterResult(id, []);
    }

    public Task<IReadOnlyList<WorkReportSearchResultResponse>> SearchAsync(
        WorkReportSearchRequest request,
        CurrentUser currentUser)
        => repository.SearchAsync(request, currentUser);

    public static IReadOnlyList<string> ValidateSearch(WorkReportSearchRequest request)
    {
        var errors = new List<string>();
        if (request.DateFrom.HasValue && request.DateTo.HasValue && request.DateFrom > request.DateTo)
        {
            errors.Add("対象期間 From は To 以前の日付を入力してください。");
        }

        if (!string.IsNullOrWhiteSpace(request.WorkCategory) && !WorkCategories.Contains(request.WorkCategory))
        {
            errors.Add("作業分類の値が正しくありません。");
        }

        return errors;
    }

    public static IReadOnlyList<string> ValidateRegister(WorkReportRegisterRequest request)
    {
        var errors = new List<string>();
        if (!request.WorkDate.HasValue)
        {
            errors.Add("作業日は必須です。");
        }

        if (string.IsNullOrWhiteSpace(request.ProjectName))
        {
            errors.Add("プロジェクト名は必須です。");
        }
        else if (request.ProjectName.Length > 100)
        {
            errors.Add("プロジェクト名は100文字以内で入力してください。");
        }

        if (string.IsNullOrWhiteSpace(request.WorkCategory))
        {
            errors.Add("作業分類は必須です。");
        }
        else if (!WorkCategories.Contains(request.WorkCategory))
        {
            errors.Add("作業分類の値が正しくありません。");
        }

        if (!request.WorkHours.HasValue)
        {
            errors.Add("作業時間は必須です。");
        }
        else if (request.WorkHours <= 0)
        {
            errors.Add("作業時間は0より大きい数値で入力してください。");
        }
        else if (request.WorkHours > 24)
        {
            errors.Add("作業時間は24時間以内で入力してください。");
        }

        if (string.IsNullOrWhiteSpace(request.WorkContent))
        {
            errors.Add("作業内容は必須です。");
        }
        else if (request.WorkContent.Length > 1000)
        {
            errors.Add("作業内容は1000文字以内で入力してください。");
        }

        return errors;
    }
}

public sealed record RegisterResult(int? WorkReportId, IReadOnlyList<string> Errors);
