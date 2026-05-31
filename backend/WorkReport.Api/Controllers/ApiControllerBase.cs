using Microsoft.AspNetCore.Mvc;
using WorkReport.Api.Security;
using WorkReport.Application.Contracts;
using WorkReport.Domain.Models.Identity;
using WorkReport.Application.Models.Results;

namespace WorkReport.Api.Controllers;

[ApiController]
public abstract class ApiControllerBase : ControllerBase
{
    protected CurrentUser CurrentUser => User.RequireCurrentUser();

    protected BadRequestObjectResult ValidationError(IReadOnlyList<string> errors)
        => BadRequest(new ErrorResponse(errors));

    protected ActionResult<T> MasterResult<T>(MasterResult<T> result, Func<T, ActionResult<T>> success)
    {
        if (result.Missing)
        {
            return NotFound();
        }

        return result.Errors.Count > 0
            ? ValidationError(result.Errors)
            : success(result.Value!);
    }
}
