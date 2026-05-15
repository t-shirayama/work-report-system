package com.example.workreport.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.workreport.dao.MonthlyReportDao;
import com.example.workreport.dto.MonthlyReportCategorySummaryDto;
import com.example.workreport.dto.MonthlyReportConditionDto;
import com.example.workreport.dto.MonthlyReportDataDto;
import com.example.workreport.dto.MonthlyReportFileDto;
import com.example.workreport.dto.MonthlyReportSummaryDto;
import com.example.workreport.entity.User;
import com.example.workreport.form.MonthlyReportForm;
import com.example.workreport.util.FileNameUtils;

@Service
public class MonthlyReportService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final MonthlyReportDao monthlyReportDao;

    private final ExcelReportService excelReportService;

    private final ReportHistoryService reportHistoryService;

    @Autowired
    public MonthlyReportService(
            MonthlyReportDao monthlyReportDao,
            ExcelReportService excelReportService,
            ReportHistoryService reportHistoryService) {
        this.monthlyReportDao = monthlyReportDao;
        this.excelReportService = excelReportService;
        this.reportHistoryService = reportHistoryService;
    }

    public List<String> validate(MonthlyReportForm form, User loginUser) {
        List<String> errors = new ArrayList<String>();

        if (!StringUtils.hasText(form.getTargetYear())) {
            errors.add("対象年は必須です。");
        } else if (!form.getTargetYear().matches("^[0-9]{4}$")) {
            errors.add("対象年は4桁の数値で入力してください。");
        }

        if (!StringUtils.hasText(form.getTargetMonth())) {
            errors.add("対象月は必須です。");
        } else if (!form.getTargetMonth().matches("^[0-9]{1,2}$")) {
            errors.add("対象月は1から12の数値で入力してください。");
        } else {
            int month = Integer.parseInt(form.getTargetMonth());
            if (month < 1 || month > 12) {
                errors.add("対象月は1から12の範囲で入力してください。");
            }
        }

        if (!StringUtils.hasText(form.getDepartmentName())) {
            errors.add("部署は必須です。");
        } else if (form.getDepartmentName().length() > 100) {
            errors.add("部署は100文字以内で入力してください。");
        }

        if (!StringUtils.hasText(form.getEmployeeName())) {
            errors.add("社員は必須です。");
        } else if (form.getEmployeeName().length() > 100) {
            errors.add("社員は100文字以内で入力してください。");
        }

        if (!isAdmin(loginUser)) {
            if (StringUtils.hasText(form.getDepartmentName())
                    && !loginUser.getDepartmentName().equals(form.getDepartmentName())) {
                errors.add("一般ユーザーは自分の部署の月次報告書のみ出力できます。");
            }
            if (StringUtils.hasText(form.getEmployeeName())
                    && !loginUser.getEmployeeName().equals(form.getEmployeeName())) {
                errors.add("一般ユーザーは自分の月次報告書のみ出力できます。");
            }
        }

        return errors;
    }

    public MonthlyReportFileDto createReport(MonthlyReportForm form, User loginUser) throws IOException {
        String targetYearMonth = buildTargetYearMonth(form);
        String fileName = buildFileName(form);
        Path reportPath = null;
        Long reportOutputHistoryId = reportHistoryService.saveProcessingHistory(loginUser.getUserId(), targetYearMonth, fileName);

        try {
            MonthlyReportFileDto file = createReportFile(form, fileName);
            reportPath = reportHistoryService.saveReportFile(targetYearMonth, file);
            reportHistoryService.updateSuccessHistory(reportOutputHistoryId, file, reportPath);
            return file;
        } catch (IOException e) {
            reportHistoryService.deleteReportFile(reportPath);
            reportHistoryService.updateErrorHistory(reportOutputHistoryId, targetYearMonth, fileName, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            reportHistoryService.deleteReportFile(reportPath);
            reportHistoryService.updateErrorHistory(reportOutputHistoryId, targetYearMonth, fileName, e.getMessage());
            throw e;
        }
    }

    private MonthlyReportFileDto createReportFile(MonthlyReportForm form, String fileName) throws IOException {
        MonthlyReportConditionDto condition = createCondition(form);
        MonthlyReportDataDto reportData = new MonthlyReportDataDto();

        MonthlyReportSummaryDto summary = monthlyReportDao.findSummary(condition);
        List<MonthlyReportCategorySummaryDto> categorySummaries = monthlyReportDao.findCategorySummaries(condition);

        if (!StringUtils.hasText(summary.getEmployeeName())) {
            summary.setEmployeeName(condition.getEmployeeName());
        }
        if (!StringUtils.hasText(summary.getDepartmentName())) {
            summary.setDepartmentName(condition.getDepartmentName());
        }

        BigDecimal totalHours = summary.getTotalWorkHours();
        if (summary.getWorkDays() > 0) {
            summary.setAverageWorkHours(totalHours.divide(new BigDecimal(summary.getWorkDays()), 2, RoundingMode.HALF_UP));
        }

        for (MonthlyReportCategorySummaryDto categorySummary : categorySummaries) {
            if (BigDecimal.ZERO.compareTo(totalHours) < 0) {
                categorySummary.setRatio(
                        categorySummary.getTotalHours().multiply(new BigDecimal("100")).divide(totalHours, 1, RoundingMode.HALF_UP));
            }
        }

        reportData.setSummary(summary);
        reportData.setCategorySummaries(categorySummaries);
        reportData.setDailyDetails(monthlyReportDao.findDailyDetails(condition));

        MonthlyReportFileDto file = new MonthlyReportFileDto();
        file.setFileName(fileName);
        file.setContent(excelReportService.createMonthlyReport(reportData));
        return file;
    }

    private MonthlyReportConditionDto createCondition(MonthlyReportForm form) {
        int year = Integer.parseInt(form.getTargetYear());
        int month = Integer.parseInt(form.getTargetMonth());

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date dateFrom = new Date(calendar.getTimeInMillis());

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date dateTo = new Date(calendar.getTimeInMillis());

        MonthlyReportConditionDto condition = new MonthlyReportConditionDto();
        condition.setTargetYearMonth(String.format("%04d%02d", year, month));
        condition.setDateFrom(dateFrom);
        condition.setDateTo(dateTo);
        condition.setDepartmentName(form.getDepartmentName());
        condition.setEmployeeName(form.getEmployeeName());
        return condition;
    }

    private String buildFileName(MonthlyReportForm form) {
        String targetYearMonth = buildTargetYearMonth(form);
        String employeeName = form.getEmployeeName() == null ? "" : form.getEmployeeName().replaceAll("\\s", "");
        return "月次報告書_" + targetYearMonth + "_"
                + FileNameUtils.sanitizeNamePart(employeeName, "unknown") + ".xlsx";
    }

    private String buildTargetYearMonth(MonthlyReportForm form) {
        return String.format("%s%02d", form.getTargetYear(), Integer.parseInt(form.getTargetMonth()));
    }

    private boolean isAdmin(User user) {
        return user != null && ROLE_ADMIN.equals(user.getRoleCode());
    }
}
