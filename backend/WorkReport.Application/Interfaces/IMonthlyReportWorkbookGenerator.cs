using WorkReport.Domain.Models.Reporting;

namespace WorkReport.Application.Interfaces;

public interface IMonthlyReportWorkbookGenerator
{
    byte[] Create(MonthlyReportData data);
}
