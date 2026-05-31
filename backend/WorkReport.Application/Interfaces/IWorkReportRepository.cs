using WorkReport.Application.Contracts;
using WorkReport.Domain.Models.Identity;

namespace WorkReport.Application.Interfaces;

public interface IWorkReportRepository
{
    Task<int> InsertAsync(WorkReportRegisterRequest request, CurrentUser currentUser);

    Task<IReadOnlyList<WorkReportSearchResultResponse>> SearchAsync(
        WorkReportSearchRequest request,
        CurrentUser currentUser);
}
