using System.Security.Claims;
using Microsoft.AspNetCore.Diagnostics;
using Microsoft.AspNetCore.Antiforgery;
using Microsoft.AspNetCore.Authentication.Cookies;
using WorkReport.Api.Application;
using WorkReport.Api.Contracts;
using WorkReport.Api.Infrastructure;
using WorkReport.Api.Reporting;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddOpenApi();
builder.Services.AddCors(options =>
{
    options.AddPolicy("frontend", policy =>
    {
        var origins = builder.Configuration.GetSection("Cors:AllowedOrigins").Get<string[]>()
            ?? ["http://localhost:5173"];
        policy.WithOrigins(origins)
            .AllowAnyHeader()
            .AllowAnyMethod()
            .AllowCredentials()
            .WithExposedHeaders("Content-Disposition");
    });
});
builder.Services.AddAntiforgery(options =>
{
    options.Cookie.Name = "WORKREPORT-XSRF";
    options.HeaderName = "X-CSRF-TOKEN";
});
builder.Services
    .AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme)
    .AddCookie(options =>
    {
        options.Cookie.Name = "WORKREPORT-AUTH";
        options.Cookie.HttpOnly = true;
        options.Cookie.SameSite = SameSiteMode.Lax;
        options.SlidingExpiration = true;
        options.Events.OnRedirectToLogin = context =>
        {
            context.Response.StatusCode = StatusCodes.Status401Unauthorized;
            return Task.CompletedTask;
        };
        options.Events.OnRedirectToAccessDenied = context =>
        {
            context.Response.StatusCode = StatusCodes.Status403Forbidden;
            return Task.CompletedTask;
        };
    });
builder.Services.AddAuthorization(options =>
{
    options.AddPolicy("Admin", policy => policy.RequireRole("ADMIN"));
});

builder.Services.Configure<ReportStorageOptions>(
    builder.Configuration.GetSection("ReportStorage"));
builder.Services.AddSingleton<SqlConnectionFactory>();
builder.Services.AddScoped<UserRepository>();
builder.Services.AddScoped<DashboardRepository>();
builder.Services.AddScoped<WorkReportRepository>();
builder.Services.AddScoped<MonthlyReportRepository>();
builder.Services.AddScoped<ReportHistoryRepository>();
builder.Services.AddScoped<MasterDataRepository>();
builder.Services.AddScoped<AuthService>();
builder.Services.AddScoped<DashboardService>();
builder.Services.AddScoped<WorkReportService>();
builder.Services.AddScoped<MonthlyReportService>();
builder.Services.AddScoped<MasterDataService>();

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseExceptionHandler(exceptionApp =>
{
    exceptionApp.Run(async context =>
    {
        var error = context.Features.Get<IExceptionHandlerFeature>()?.Error;
        context.Response.ContentType = "application/json";
        context.Response.StatusCode = error switch
        {
            BadHttpRequestException => StatusCodes.Status400BadRequest,
            AntiforgeryValidationException => StatusCodes.Status400BadRequest,
            UnauthorizedAccessException => StatusCodes.Status403Forbidden,
            KeyNotFoundException => StatusCodes.Status404NotFound,
            _ => StatusCodes.Status500InternalServerError
        };
        await context.Response.WriteAsJsonAsync(new ErrorResponse([error?.Message ?? "処理に失敗しました。"]));
    });
});

app.UseCors("frontend");
app.UseAuthentication();
app.UseAuthorization();

var api = app.MapGroup("/api");

api.MapGet("/auth/csrf", (HttpContext http, IAntiforgery antiforgery) =>
{
    var tokens = antiforgery.GetAndStoreTokens(http);
    return Results.Ok(new CsrfResponse(tokens.RequestToken ?? ""));
});

api.MapPost("/auth/login", async (
    HttpContext http,
    IAntiforgery antiforgery,
    LoginRequest request,
    AuthService authService) =>
{
    await antiforgery.ValidateRequestAsync(http);
    var result = await authService.SignInAsync(http, request);
    return result is null ? Results.Unauthorized() : Results.Ok(result);
});

api.MapPost("/auth/logout", async (HttpContext http, IAntiforgery antiforgery) =>
{
    await antiforgery.ValidateRequestAsync(http);
    await AuthService.SignOutAsync(http);
    return Results.NoContent();
}).RequireAuthorization();

api.MapGet("/auth/me", (ClaimsPrincipal principal) =>
{
    var current = CurrentUser.FromPrincipal(principal);
    return current is null ? Results.Unauthorized() : Results.Ok(current.ToResponse());
}).RequireAuthorization();

api.MapGet("/dashboard", async (DashboardService service) =>
    Results.Ok(await service.GetDashboardAsync()))
    .RequireAuthorization();

api.MapGet("/work-reports", async (
    DateTime? dateFrom,
    DateTime? dateTo,
    string? employeeName,
    string? departmentName,
    string? workCategory,
    string? projectName,
    ClaimsPrincipal principal,
    WorkReportService service) =>
{
    var current = CurrentUser.Require(principal);
    var request = new WorkReportSearchRequest(dateFrom, dateTo, employeeName, departmentName, workCategory, projectName);
    var errors = WorkReportService.ValidateSearch(request);
    return errors.Count > 0
        ? Results.BadRequest(new ErrorResponse(errors))
        : Results.Ok(await service.SearchAsync(request, current));
}).RequireAuthorization();

api.MapPost("/work-reports", async (
    HttpContext http,
    IAntiforgery antiforgery,
    WorkReportRegisterRequest request,
    ClaimsPrincipal principal,
    WorkReportService service) =>
{
    await antiforgery.ValidateRequestAsync(http);
    var current = CurrentUser.Require(principal);
    var result = await service.RegisterAsync(request, current);
    return result.Errors.Count > 0
        ? Results.BadRequest(new ErrorResponse(result.Errors))
        : Results.Created($"/api/work-reports/{result.WorkReportId}", new WorkReportRegisterResponse(result.WorkReportId!.Value));
}).RequireAuthorization();

api.MapGet("/monthly-reports/target-users", async (UserRepository users) =>
    Results.Ok(await users.FindReportTargetUsersAsync()))
    .RequireAuthorization("Admin");

api.MapPost("/monthly-reports/export", async (
    HttpContext http,
    IAntiforgery antiforgery,
    MonthlyReportExportRequest request,
    ClaimsPrincipal principal,
    MonthlyReportService service) =>
{
    await antiforgery.ValidateRequestAsync(http);
    var errors = MonthlyReportService.ValidateExport(request);
    if (errors.Count > 0)
    {
        return Results.BadRequest(new ErrorResponse(errors));
    }

    var current = CurrentUser.Require(principal);
    var result = await service.ExportAsync(request, current);
    return Results.File(
        result.Content,
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        result.FileName);
}).RequireAuthorization();

api.MapGet("/report-histories", async (
    string? targetYearMonth,
    int? targetUserId,
    string? status,
    ReportHistoryRepository repository) =>
    Results.Ok(await repository.SearchAsync(targetYearMonth, targetUserId, status)))
    .RequireAuthorization();

api.MapGet("/report-histories/{id:int}", async (int id, ReportHistoryRepository repository) =>
{
    var detail = await repository.FindDetailAsync(id);
    return detail is null ? Results.NotFound() : Results.Ok(detail);
}).RequireAuthorization();

api.MapGet("/report-histories/{id:int}/download", async (int id, MonthlyReportService service) =>
{
    var result = await service.DownloadHistoryAsync(id);
    return result is null
        ? Results.NotFound()
        : Results.File(
            result.Content,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            result.FileName);
}).RequireAuthorization();

var master = api.MapGroup("/master").RequireAuthorization("Admin");

master.MapGet("/departments", async (MasterDataService service) =>
    Results.Ok(await service.FindDepartmentsAsync()));

master.MapPost("/departments", async (
    HttpContext http,
    IAntiforgery antiforgery,
    DepartmentUpsertRequest request,
    MasterDataService service) =>
{
    await antiforgery.ValidateRequestAsync(http);
    var result = await service.CreateDepartmentAsync(request);
    return ToMasterResult(result, value => Results.Created($"/api/master/departments/{value.DepartmentId}", value));
});

master.MapPut("/departments/{id:int}", async (
    int id,
    HttpContext http,
    IAntiforgery antiforgery,
    DepartmentUpsertRequest request,
    MasterDataService service) =>
{
    await antiforgery.ValidateRequestAsync(http);
    var result = await service.UpdateDepartmentAsync(id, request);
    return ToMasterResult(result, Results.Ok);
});

master.MapGet("/users", async (MasterDataService service) =>
    Results.Ok(await service.FindUsersAsync()));

master.MapPost("/users", async (
    HttpContext http,
    IAntiforgery antiforgery,
    MasterUserCreateRequest request,
    MasterDataService service) =>
{
    await antiforgery.ValidateRequestAsync(http);
    var result = await service.CreateUserAsync(request);
    return ToMasterResult(result, value => Results.Created($"/api/master/users/{value.UserId}", value));
});

master.MapPut("/users/{id:int}", async (
    int id,
    HttpContext http,
    IAntiforgery antiforgery,
    MasterUserUpdateRequest request,
    MasterDataService service) =>
{
    await antiforgery.ValidateRequestAsync(http);
    var result = await service.UpdateUserAsync(id, request);
    return ToMasterResult(result, Results.Ok);
});

app.Run();

static IResult ToMasterResult<T>(MasterResult<T> result, Func<T, IResult> success)
{
    if (result.Missing)
    {
        return Results.NotFound();
    }

    return result.Errors.Count > 0
        ? Results.BadRequest(new ErrorResponse(result.Errors))
        : success(result.Value!);
}

public partial class Program;
