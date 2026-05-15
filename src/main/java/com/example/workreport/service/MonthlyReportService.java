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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyReportService.class);

    private static final String ROLE_ADMIN = "ADMIN";

    private static final String ROLE_USER = "USER";

    private final MonthlyReportDao monthlyReportDao;

    private final ExcelReportService excelReportService;

    private final ReportHistoryService reportHistoryService;

    private final UserService userService;

    @Autowired
    public MonthlyReportService(
            MonthlyReportDao monthlyReportDao,
            ExcelReportService excelReportService,
            ReportHistoryService reportHistoryService,
            UserService userService) {
        this.monthlyReportDao = monthlyReportDao;
        this.excelReportService = excelReportService;
        this.reportHistoryService = reportHistoryService;
        this.userService = userService;
    }

    public List<String> validate(MonthlyReportForm form, User loginUser) {
        requireLoginUser(loginUser);
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

        if (isAdmin(loginUser)) {
            User targetUser = findValidReportTargetUser(form.getUserId());
            if (!StringUtils.hasText(form.getUserId())) {
                errors.add("社員は必須です。");
            } else if (targetUser == null) {
                errors.add("指定された社員が存在しません。");
            }
        } else if (StringUtils.hasText(form.getUserId()) && !loginUser.getUserId().toString().equals(form.getUserId())) {
            errors.add("一般ユーザーは自分の月次報告書のみ出力できます。");
        }

        return errors;
    }

    public MonthlyReportFileDto createReport(MonthlyReportForm form, User loginUser) throws IOException {
        requireLoginUser(loginUser);
        String targetYearMonth = buildTargetYearMonth(form);
        User targetUser = resolveTargetUser(form, loginUser);
        String processingFileName = buildProcessingFileName(targetYearMonth, targetUser);
        Path reportPath = null;
        Long reportOutputHistoryId = reportHistoryService.saveProcessingHistory(loginUser.getUserId(), targetYearMonth, processingFileName);
        String fileName = buildFileName(targetYearMonth, targetUser, reportOutputHistoryId);

        try {
            MonthlyReportFileDto file = createReportFile(form, targetUser, fileName);
            reportPath = reportHistoryService.saveReportFile(targetYearMonth, file);
            reportHistoryService.updateSuccessHistory(reportOutputHistoryId, file, reportPath);
            return file;
        } catch (IOException e) {
            reportHistoryService.deleteReportFile(reportPath);
            updateErrorHistoryBestEffort(reportOutputHistoryId, targetYearMonth, fileName, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            reportHistoryService.deleteReportFile(reportPath);
            updateErrorHistoryBestEffort(reportOutputHistoryId, targetYearMonth, fileName, e.getMessage());
            throw e;
        }
    }

    private void updateErrorHistoryBestEffort(Long reportOutputHistoryId, String targetYearMonth, String fileName, String errorMessage) {
        try {
            reportHistoryService.updateErrorHistory(reportOutputHistoryId, targetYearMonth, fileName, errorMessage);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to update error report history. historyId={}, targetYearMonth={}",
                    reportOutputHistoryId, targetYearMonth, e);
        }
    }

    private MonthlyReportFileDto createReportFile(MonthlyReportForm form, User targetUser, String fileName) throws IOException {
        MonthlyReportConditionDto condition = createCondition(form, targetUser);
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

    private MonthlyReportConditionDto createCondition(MonthlyReportForm form, User targetUser) {
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
        condition.setUserId(targetUser.getUserId());
        condition.setDepartmentName(targetUser.getDepartmentName());
        condition.setEmployeeName(targetUser.getEmployeeName());
        return condition;
    }

    private User resolveTargetUser(MonthlyReportForm form, User loginUser) {
        if (!isAdmin(loginUser)) {
            return loginUser;
        }
        Long targetUserId = parseUserId(form.getUserId());
        if (targetUserId == null) {
            throw new IllegalArgumentException("target user is required.");
        }
        User targetUser = userService.findById(targetUserId);
        if (!isReportTargetUser(targetUser)) {
            throw new IllegalArgumentException("target user not found.");
        }
        return targetUser;
    }

    private User findValidReportTargetUser(String userId) {
        Long targetUserId = parseUserId(userId);
        if (targetUserId == null) {
            return null;
        }
        User targetUser = userService.findById(targetUserId);
        return isReportTargetUser(targetUser) ? targetUser : null;
    }

    private boolean isReportTargetUser(User user) {
        return user != null && ROLE_USER.equals(user.getRoleCode());
    }

    private Long parseUserId(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildFileName(String targetYearMonth, User targetUser, Long reportOutputHistoryId) {
        String employeeName = targetUser.getEmployeeName() == null ? "" : targetUser.getEmployeeName().replaceAll("\\s", "");
        StringBuilder fileName = new StringBuilder();
        fileName.append("月次報告書_").append(targetYearMonth).append("_")
                .append(FileNameUtils.sanitizeNamePart(employeeName, "unknown"));
        if (reportOutputHistoryId != null) {
            fileName.append("_履歴ID").append(reportOutputHistoryId);
        }
        fileName.append(".xlsx");
        return fileName.toString();
    }

    private String buildProcessingFileName(String targetYearMonth, User targetUser) {
        String employeeName = targetUser.getEmployeeName() == null ? "" : targetUser.getEmployeeName().replaceAll("\\s", "");
        return "月次報告書_" + targetYearMonth + "_"
                + FileNameUtils.sanitizeNamePart(employeeName, "unknown") + "_PROCESSING.xlsx";
    }

    private String buildTargetYearMonth(MonthlyReportForm form) {
        return String.format("%s%02d", form.getTargetYear(), Integer.parseInt(form.getTargetMonth()));
    }

    private boolean isAdmin(User user) {
        return user != null && ROLE_ADMIN.equals(user.getRoleCode());
    }

    private void requireLoginUser(User loginUser) {
        if (loginUser == null) {
            throw new IllegalArgumentException("loginUser is required.");
        }
    }
}
