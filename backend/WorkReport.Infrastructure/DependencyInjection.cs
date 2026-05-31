using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using WorkReport.Application.Interfaces;
using WorkReport.Infrastructure.Persistence;
using WorkReport.Infrastructure.Reporting;
using WorkReport.Infrastructure.Security;

namespace WorkReport.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        services.Configure<ReportStorageOptions>(
            configuration.GetSection("ReportStorage"));
        services.AddSingleton<SqlConnectionFactory>();
        services.AddScoped<IUserRepository, UserRepository>();
        services.AddScoped<IDashboardRepository, DashboardRepository>();
        services.AddScoped<IWorkReportRepository, WorkReportRepository>();
        services.AddScoped<IMonthlyReportRepository, MonthlyReportRepository>();
        services.AddScoped<IReportHistoryRepository, ReportHistoryRepository>();
        services.AddScoped<IMasterDataRepository, MasterDataRepository>();
        services.AddScoped<IPasswordHasher, BcryptPasswordHasher>();
        services.AddScoped<IMonthlyReportWorkbookGenerator, MonthlyReportWorkbookGenerator>();
        services.AddScoped<IReportFileStorage, LocalReportFileStorage>();

        return services;
    }
}
