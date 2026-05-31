using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WorkReport.Application.Contracts;
using WorkReport.Application.MonthlyReports;
using WorkReport.Application.ReportHistories;

namespace WorkReport.Api.Controllers;

[Authorize]
[Route("api/report-histories")]
public sealed class ReportHistoriesController(
    ReportHistoryService reportHistoryService,
    MonthlyReportService monthlyReportService) : ApiControllerBase
{
    [HttpGet]
    public async Task<ActionResult<IReadOnlyList<ReportHistoryListResponse>>> Search(
        string? targetYearMonth,
        int? targetUserId,
        string? status)
        => Ok(await reportHistoryService.SearchAsync(targetYearMonth, targetUserId, status));

    [HttpGet("{id:int}")]
    public async Task<ActionResult<ReportHistoryDetailResponse>> Detail(int id)
    {
        var detail = await reportHistoryService.FindDetailAsync(id);
        return detail is null ? NotFound() : Ok(detail);
    }

    [HttpGet("{id:int}/download")]
    public async Task<IActionResult> Download(int id)
    {
        var result = await monthlyReportService.DownloadHistoryAsync(id);
        return result is null
            ? NotFound()
            : File(
                result.Content,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                result.FileName);
    }
}
