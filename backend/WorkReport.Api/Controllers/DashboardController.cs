using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WorkReport.Application.Contracts;
using WorkReport.Application.Dashboard;

namespace WorkReport.Api.Controllers;

[Authorize]
[Route("api/dashboard")]
public sealed class DashboardController(DashboardService service) : ApiControllerBase
{
    [HttpGet]
    public async Task<ActionResult<DashboardResponse>> Get()
        => Ok(await service.GetDashboardAsync());
}
