using Microsoft.Extensions.DependencyInjection;
using WorkReport.Application.Auth;
using WorkReport.Application.Dashboard;
using WorkReport.Application.Masters;
using WorkReport.Application.MonthlyReports;
using WorkReport.Application.ReportHistories;
using WorkReport.Application.WorkReports;

namespace WorkReport.Application;

public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        services.AddScoped<AuthService>();
        services.AddScoped<DashboardService>();
        services.AddScoped<WorkReportService>();
        services.AddScoped<MonthlyReportService>();
        services.AddScoped<ReportHistoryService>();
        services.AddScoped<MasterDataService>();

        return services;
    }
}
