package com.example.workreport.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.workreport.dto.MonthlyReportCategorySummaryDto;
import com.example.workreport.dto.MonthlyReportDailyDetailDto;
import com.example.workreport.dto.MonthlyReportDataDto;
import com.example.workreport.dto.MonthlyReportSummaryDto;

@Service
public class ExcelReportService {

    private static final String TEMPLATE_PATH = "/templates/monthly-report-template.xlsx";

    private static final int CATEGORY_TEMPLATE_ROW_INDEX = 14;

    private static final int DETAIL_TEMPLATE_ROW_INDEX = 21;

    public byte[] createMonthlyReport(MonthlyReportDataDto reportData) throws IOException {
        try (InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (templateStream == null) {
                throw new IOException("Excel template not found: " + TEMPLATE_PATH);
            }

            try (Workbook workbook = new XSSFWorkbook(templateStream);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                Sheet sheet = workbook.getSheetAt(0);
                writeBasicInfo(sheet, reportData.getSummary());
                writeSummary(sheet, reportData.getSummary());
                writeCategorySummaries(sheet, reportData.getCategorySummaries());
                writeDailyDetails(
                        sheet,
                        reportData.getDailyDetails(),
                        DETAIL_TEMPLATE_ROW_INDEX + Math.max(0, reportData.getCategorySummaries().size() - 1));

                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    private void writeBasicInfo(Sheet sheet, MonthlyReportSummaryDto summary) {
        setCellValue(sheet, 2, 1, formatTargetYearMonth(summary.getTargetYearMonth()));
        setCellValue(sheet, 3, 1, summary.getEmployeeName());
        setCellValue(sheet, 4, 1, summary.getDepartmentName());
        setCellValue(sheet, 5, 1, new Date());
    }

    private void writeSummary(Sheet sheet, MonthlyReportSummaryDto summary) {
        setCellValue(sheet, 8, 1, summary.getTotalWorkHours());
        setCellValue(sheet, 9, 1, summary.getWorkDays());
        setCellValue(sheet, 10, 1, summary.getAverageWorkHours());
    }

    private void writeCategorySummaries(Sheet sheet, List<MonthlyReportCategorySummaryDto> categorySummaries) {
        Row templateRow = sheet.getRow(CATEGORY_TEMPLATE_ROW_INDEX);
        int rowIndex = CATEGORY_TEMPLATE_ROW_INDEX;

        if (categorySummaries.size() > 1) {
            sheet.shiftRows(rowIndex + 1, sheet.getLastRowNum(), categorySummaries.size() - 1, true, false);
        }

        for (MonthlyReportCategorySummaryDto categorySummary : categorySummaries) {
            Row row = createRowWithStyle(sheet, rowIndex, templateRow);
            setCellValue(row, 0, categorySummary.getWorkCategoryName());
            setCellValue(row, 1, categorySummary.getTotalHours());
            setCellValue(row, 2, categorySummary.getRatio());
            rowIndex++;
        }
    }

    private void writeDailyDetails(Sheet sheet, List<MonthlyReportDailyDetailDto> dailyDetails, int detailTemplateRowIndex) {
        Row templateRow = sheet.getRow(detailTemplateRowIndex);
        int rowIndex = detailTemplateRowIndex;

        if (dailyDetails.size() > 1) {
            sheet.shiftRows(detailTemplateRowIndex + 1, sheet.getLastRowNum(), dailyDetails.size() - 1, true, false);
        }

        for (MonthlyReportDailyDetailDto dailyDetail : dailyDetails) {
            Row row = createRowWithStyle(sheet, rowIndex, templateRow);
            setCellValue(row, 0, dailyDetail.getWorkDate());
            setCellValue(row, 1, dailyDetail.getDayOfWeek());
            setCellValue(row, 2, dailyDetail.getProjectName());
            setCellValue(row, 3, dailyDetail.getWorkCategoryName());
            setCellValue(row, 4, dailyDetail.getWorkHours());
            setCellValue(row, 5, dailyDetail.getWorkContent());
            rowIndex++;
        }
    }

    private Row createRowWithStyle(Sheet sheet, int rowIndex, Row templateRow) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        if (templateRow != null) {
            row.setHeight(templateRow.getHeight());
            for (int i = 0; i < templateRow.getLastCellNum(); i++) {
                Cell templateCell = templateRow.getCell(i);
                Cell cell = row.getCell(i);
                if (cell == null) {
                    cell = row.createCell(i);
                }
                if (templateCell != null) {
                    CellStyle style = templateCell.getCellStyle();
                    cell.setCellStyle(style);
                }
            }
        }

        return row;
    }

    private void setCellValue(Sheet sheet, int rowIndex, int columnIndex, Object value) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        setCellValue(row, columnIndex, value);
    }

    private void setCellValue(Row row, int columnIndex, Object value) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }

        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private String formatTargetYearMonth(String targetYearMonth) {
        if (targetYearMonth == null || targetYearMonth.length() != 6) {
            return targetYearMonth;
        }
        return targetYearMonth.substring(0, 4) + "年" + targetYearMonth.substring(4, 6) + "月";
    }
}
