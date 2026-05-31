using Microsoft.Extensions.DependencyInjection;

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
