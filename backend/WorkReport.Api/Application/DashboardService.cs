using WorkReport.Api.Contracts;
using WorkReport.Api.Infrastructure;

namespace WorkReport.Api.Application;

public sealed class DashboardService(DashboardRepository repository)
{
    public async Task<DashboardResponse> GetDashboardAsync()
    {
        var today = await repository.CountTodayWorkReportsAsync();
        var monthTotal = await repository.SumCurrentMonthWorkHoursAsync();
        var notOutput = await repository.CountNotOutputMonthlyReportsAsync();
        var activities = await repository.FindRecentActivitiesAsync();
        return new DashboardResponse(today, monthTotal, notOutput, activities);
    }
}
