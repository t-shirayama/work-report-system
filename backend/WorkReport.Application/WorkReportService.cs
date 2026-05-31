using WorkReport.Application.Contracts;
using WorkReport.Application.Interfaces;
using WorkReport.Domain.Models.Identity;
using WorkReport.Application.Models.Results;
using WorkReport.Application.WorkReports;

namespace WorkReport.Application;

public sealed class WorkReportService(IWorkReportRepository repository)
{
    public async Task<RegisterResult> RegisterAsync(WorkReportRegisterRequest request, CurrentUser currentUser)
    {
        var errors = WorkReportValidator.ValidateRegister(request);
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
        => WorkReportValidator.ValidateSearch(request);

    public static IReadOnlyList<string> ValidateRegister(WorkReportRegisterRequest request)
        => WorkReportValidator.ValidateRegister(request);
}
