using WorkReport.Application.Interfaces;
using WorkReport.Domain.Models.Reporting;

namespace WorkReport.Infrastructure.Reporting;

public sealed class MonthlyReportWorkbookGenerator : IMonthlyReportWorkbookGenerator
{
    public byte[] Create(MonthlyReportData data)
        => MonthlyReportWorkbook.Create(data);
}
