using WorkReport.Domain.Models.Reporting;

namespace WorkReport.Application.Interfaces;

public interface IMonthlyReportRepository
{
    Task<MonthlyReportData> BuildMonthlyReportAsync(string targetYearMonth, int targetUserId);
}
