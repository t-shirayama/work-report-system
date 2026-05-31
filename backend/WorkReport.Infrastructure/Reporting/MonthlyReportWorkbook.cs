using ClosedXML.Excel;
using WorkReport.Domain.Models.Reporting;

namespace WorkReport.Infrastructure.Reporting;

public static class MonthlyReportWorkbook
{
    public static byte[] Create(MonthlyReportData data)
    {
        using var workbook = new XLWorkbook();
        var sheet = workbook.Worksheets.Add("月次作業報告書");

        sheet.Cell("A1").Value = "月次作業報告書";
        sheet.Cell("A3").Value = "対象年月";
        sheet.Cell("B3").Value = data.TargetYearMonth;
        sheet.Cell("A4").Value = "氏名";
        sheet.Cell("B4").Value = data.User.EmployeeName;
        sheet.Cell("A5").Value = "部署";
        sheet.Cell("B5").Value = data.User.DepartmentName;

        sheet.Cell("A7").Value = "作業日";
        sheet.Cell("B7").Value = "プロジェクト";
        sheet.Cell("C7").Value = "分類";
        sheet.Cell("D7").Value = "時間";
        sheet.Cell("E7").Value = "作業内容";

        var row = 8;
        foreach (var detail in data.Details)
        {
            sheet.Cell(row, 1).Value = detail.WorkDate;
            sheet.Cell(row, 2).Value = detail.ProjectName;
            sheet.Cell(row, 3).Value = detail.WorkCategory;
            sheet.Cell(row, 4).Value = detail.WorkHours;
            sheet.Cell(row, 5).Value = detail.WorkContent;
            row++;
        }

        row += 2;
        sheet.Cell(row, 1).Value = "分類別集計";
        row++;
        sheet.Cell(row, 1).Value = "分類";
        sheet.Cell(row, 2).Value = "合計時間";
        row++;
        foreach (var category in data.Categories)
        {
            sheet.Cell(row, 1).Value = category.WorkCategory;
            sheet.Cell(row, 2).Value = category.TotalHours;
            row++;
        }

        sheet.Columns().AdjustToContents();
        using var stream = new MemoryStream();
        workbook.SaveAs(stream);
        return stream.ToArray();
    }
}
