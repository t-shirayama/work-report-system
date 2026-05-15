package com.example.workreport.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.workreport.dao.ReportHistoryDao;
import com.example.workreport.dto.MonthlyReportFileDto;
import com.example.workreport.dto.ReportHistoryDto;
import com.example.workreport.entity.ReportOutputHistory;
import com.example.workreport.entity.User;
import com.example.workreport.form.ReportHistorySearchForm;
import com.example.workreport.util.FileNameUtils;

@Service
public class ReportHistoryService {

    public static final String REPORT_TYPE_MONTHLY_WORK_REPORT = "MONTHLY_WORK_REPORT";

    public static final String STATUS_SUCCESS = "SUCCESS";

    public static final String STATUS_ERROR = "ERROR";

    private static final String GENERATED_REPORTS_DIR = "generated-reports";

    private static final String ROLE_ADMIN = "ADMIN";

    private final ReportHistoryDao reportHistoryDao;

    @Autowired
    public ReportHistoryService(ReportHistoryDao reportHistoryDao) {
        this.reportHistoryDao = reportHistoryDao;
    }

    public List<ReportHistoryDto> findAll(User loginUser) {
        if (isAdmin(loginUser)) {
            return reportHistoryDao.findAll();
        }
        return reportHistoryDao.findAllByCreatedBy(loginUser.getUserId());
    }

    public List<ReportHistoryDto> search(ReportHistorySearchForm form, User loginUser) {
        if (form == null) {
            return findAll(loginUser);
        }
        if (isAdmin(loginUser)) {
            return reportHistoryDao.search(form);
        }
        return reportHistoryDao.search(form, loginUser.getUserId());
    }

    public ReportHistoryDto findById(Long reportOutputHistoryId, User loginUser) {
        ReportHistoryDto history = reportHistoryDao.findById(reportOutputHistoryId);
        if (history == null) {
            return null;
        }
        if (!isAdmin(loginUser) && !loginUser.getUserId().equals(history.getCreatedBy())) {
            return null;
        }
        return history;
    }

    public Path saveReportFile(String targetYearMonth, MonthlyReportFileDto reportFile) throws IOException {
        if (targetYearMonth == null || !targetYearMonth.matches("^[0-9]{6}$")) {
            throw new IOException("Invalid target year month: " + targetYearMonth);
        }
        if (!FileNameUtils.isSafeFileName(reportFile.getFileName())) {
            throw new IOException("Invalid report file name: " + reportFile.getFileName());
        }

        Path baseDir = Paths.get(GENERATED_REPORTS_DIR).toAbsolutePath().normalize();
        Path reportDir = baseDir.resolve(targetYearMonth).normalize();
        if (!reportDir.startsWith(baseDir)) {
            throw new IOException("Invalid report directory: " + reportDir);
        }
        Files.createDirectories(reportDir);

        Path reportPath = reportDir.resolve(reportFile.getFileName()).normalize();
        if (!reportPath.startsWith(reportDir)) {
            throw new IOException("Invalid report path: " + reportPath);
        }
        Files.write(reportPath, reportFile.getContent());
        return reportPath;
    }

    @Transactional
    public void saveSuccessHistory(Long createdBy, String targetYearMonth, MonthlyReportFileDto reportFile, Path reportPath) {
        ReportOutputHistory history = createHistory(createdBy, targetYearMonth, reportFile.getFileName(), reportPath.toString(), STATUS_SUCCESS, null);
        reportHistoryDao.insert(history);
    }

    @Transactional
    public void saveErrorHistory(Long createdBy, String targetYearMonth, String fileName, String errorMessage) {
        String safeFileName = FileNameUtils.isSafeFileName(fileName)
                ? fileName
                : FileNameUtils.sanitizeNamePart(fileName, "monthly-report") + ".xlsx";
        String filePath = Paths.get(GENERATED_REPORTS_DIR, targetYearMonth, safeFileName).toString();
        ReportOutputHistory history = createHistory(createdBy, targetYearMonth, safeFileName, filePath, STATUS_ERROR, truncate(errorMessage));
        reportHistoryDao.insert(history);
    }

    public void deleteReportFile(Path reportPath) {
        try {
            if (reportPath != null && Files.exists(reportPath) && Files.isRegularFile(reportPath)) {
                Files.delete(reportPath);
            }
        } catch (IOException e) {
            // 帳票出力失敗時の後片付けなので、元の例外を優先する。
        }
    }

    public byte[] readReportFile(ReportHistoryDto history) throws IOException {
        if (history == null || !STATUS_SUCCESS.equals(history.getStatus())) {
            return null;
        }

        Path baseDir = Paths.get(GENERATED_REPORTS_DIR).toAbsolutePath().normalize();
        Path reportPath = Paths.get(history.getFilePath()).toAbsolutePath().normalize();
        if (!reportPath.startsWith(baseDir) || !Files.exists(reportPath) || !Files.isRegularFile(reportPath)) {
            return null;
        }

        return Files.readAllBytes(reportPath);
    }

    private ReportOutputHistory createHistory(
            Long createdBy,
            String targetYearMonth,
            String fileName,
            String filePath,
            String status,
            String errorMessage) {

        ReportOutputHistory history = new ReportOutputHistory();
        history.setCreatedBy(createdBy);
        history.setTargetYearMonth(targetYearMonth);
        history.setReportType(REPORT_TYPE_MONTHLY_WORK_REPORT);
        history.setFileName(fileName);
        history.setFilePath(filePath);
        history.setStatus(status);
        history.setErrorMessage(errorMessage);
        return history;
    }

    private String truncate(String value) {
        if (value == null || value.length() <= 1000) {
            return value;
        }
        return value.substring(0, 1000);
    }

    private boolean isAdmin(User user) {
        return user != null && ROLE_ADMIN.equals(user.getRoleCode());
    }
}
