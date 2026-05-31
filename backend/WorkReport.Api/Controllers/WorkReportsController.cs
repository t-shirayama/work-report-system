using Microsoft.AspNetCore.Antiforgery;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WorkReport.Application;
using WorkReport.Application.Contracts;

namespace WorkReport.Api.Controllers;

[Authorize]
[Route("api/work-reports")]
public sealed class WorkReportsController(WorkReportService service, IAntiforgery antiforgery) : ApiControllerBase
{
    [HttpGet]
    public async Task<ActionResult<IReadOnlyList<WorkReportSearchResultResponse>>> Search(
        DateTime? dateFrom,
        DateTime? dateTo,
        string? employeeName,
        string? departmentName,
        string? workCategory,
        string? projectName)
    {
        var request = new WorkReportSearchRequest(
            dateFrom,
            dateTo,
            employeeName,
            departmentName,
            workCategory,
            projectName);
        var errors = WorkReportService.ValidateSearch(request);
        return errors.Count > 0
            ? ValidationError(errors)
            : Ok(await service.SearchAsync(request, CurrentUser));
    }

    [HttpPost]
    public async Task<ActionResult<WorkReportRegisterResponse>> Register(WorkReportRegisterRequest request)
    {
        await antiforgery.ValidateRequestAsync(HttpContext);
        var result = await service.RegisterAsync(request, CurrentUser);
        return result.Errors.Count > 0
            ? ValidationError(result.Errors)
            : Created($"/api/work-reports/{result.WorkReportId}", new WorkReportRegisterResponse(result.WorkReportId!.Value));
    }
}
