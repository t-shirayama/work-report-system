using WorkReport.Application.Contracts;
using WorkReport.Application.Interfaces;

namespace WorkReport.Application.Dashboard;

public sealed class DashboardService(IDashboardRepository repository)
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
