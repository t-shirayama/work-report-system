using Microsoft.AspNetCore.Antiforgery;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WorkReport.Application.Contracts;
using WorkReport.Application.Masters;

namespace WorkReport.Api.Controllers;

[Authorize(Policy = "Admin")]
[Route("api/master")]
public sealed class MasterController(MasterDataService service, IAntiforgery antiforgery) : ApiControllerBase
{
    [HttpGet("departments")]
    public async Task<ActionResult<IReadOnlyList<DepartmentResponse>>> GetDepartments()
        => Ok(await service.FindDepartmentsAsync());

    [HttpPost("departments")]
    public async Task<ActionResult<DepartmentResponse>> CreateDepartment(DepartmentUpsertRequest request)
    {
        await antiforgery.ValidateRequestAsync(HttpContext);
        var result = await service.CreateDepartmentAsync(request);
        return MasterResult(result, value => Created($"/api/master/departments/{value.DepartmentId}", value));
    }

    [HttpPut("departments/{id:int}")]
    public async Task<ActionResult<DepartmentResponse>> UpdateDepartment(int id, DepartmentUpsertRequest request)
    {
        await antiforgery.ValidateRequestAsync(HttpContext);
        var result = await service.UpdateDepartmentAsync(id, request);
        return MasterResult(result, value => Ok(value));
    }

    [HttpGet("users")]
    public async Task<ActionResult<IReadOnlyList<MasterUserResponse>>> GetUsers()
        => Ok(await service.FindUsersAsync());

    [HttpPost("users")]
    public async Task<ActionResult<MasterUserResponse>> CreateUser(MasterUserCreateRequest request)
    {
        await antiforgery.ValidateRequestAsync(HttpContext);
        var result = await service.CreateUserAsync(request);
        return MasterResult(result, value => Created($"/api/master/users/{value.UserId}", value));
    }

    [HttpPut("users/{id:int}")]
    public async Task<ActionResult<MasterUserResponse>> UpdateUser(int id, MasterUserUpdateRequest request)
    {
        await antiforgery.ValidateRequestAsync(HttpContext);
        var result = await service.UpdateUserAsync(id, request);
        return MasterResult(result, value => Ok(value));
    }
}
