using Microsoft.AspNetCore.Antiforgery;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WorkReport.Application.Contracts;
using WorkReport.Application.MonthlyReports;

namespace WorkReport.Api.Controllers;

[Authorize]
[Route("api/monthly-reports")]
public sealed class MonthlyReportsController(
    MonthlyReportService monthlyReportService,
    IAntiforgery antiforgery) : ApiControllerBase
{
    [Authorize(Policy = "Admin")]
    [HttpGet("target-users")]
    public async Task<ActionResult<IReadOnlyList<UserResponse>>> GetTargetUsers()
        => Ok(await monthlyReportService.FindTargetUsersAsync());

    [HttpPost("export")]
    public async Task<IActionResult> Export(MonthlyReportExportRequest request)
    {
        await antiforgery.ValidateRequestAsync(HttpContext);
        var errors = MonthlyReportService.ValidateExport(request);
        if (errors.Count > 0)
        {
            return ValidationError(errors);
        }

        var result = await monthlyReportService.ExportAsync(request, CurrentUser);
        return File(
            result.Content,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            result.FileName);
    }
}
