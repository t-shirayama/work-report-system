using WorkReport.Application.Contracts;

namespace WorkReport.Application.Interfaces;

public interface IDashboardRepository
{
    Task<int> CountTodayWorkReportsAsync();

    Task<decimal> SumCurrentMonthWorkHoursAsync();

    Task<int> CountNotOutputMonthlyReportsAsync();

    Task<IReadOnlyList<DashboardActivityResponse>> FindRecentActivitiesAsync();
}
