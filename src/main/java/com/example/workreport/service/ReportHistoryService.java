package com.example.workreport.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportHistoryService.class);

    public static final String REPORT_TYPE_MONTHLY_WORK_REPORT = "MONTHLY_WORK_REPORT";

    public static final String STATUS_SUCCESS = "SUCCESS";

    public static final String STATUS_ERROR = "ERROR";

    public static final String STATUS_PROCESSING = "PROCESSING";

    private static final String GENERATED_REPORTS_DIR = "generated-reports";

    private static final String ROLE_ADMIN = "ADMIN";

    private final ReportHistoryDao reportHistoryDao;

    @Autowired
    public ReportHistoryService(ReportHistoryDao reportHistoryDao) {
        this.reportHistoryDao = reportHistoryDao;
    }

    public List<ReportHistoryDto> findAll(User loginUser) {
        requireLoginUser(loginUser);
        if (isAdmin(loginUser)) {
            return reportHistoryDao.findAll();
        }
        return reportHistoryDao.findAllByCreatedBy(loginUser.getUserId());
    }

    public List<ReportHistoryDto> search(ReportHistorySearchForm form, User loginUser) {
        requireLoginUser(loginUser);
        if (form == null) {
            return findAll(loginUser);
        }
        if (isAdmin(loginUser)) {
            return reportHistoryDao.search(form);
        }
        return reportHistoryDao.search(form, loginUser.getUserId());
    }

    public ReportHistoryDto findById(Long reportOutputHistoryId, User loginUser) {
        requireLoginUser(loginUser);
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
    public Long saveProcessingHistory(Long createdBy, String targetYearMonth, String fileName) {
        String safeFileName = safeFileName(fileName);
        String filePath = buildReportFilePath(targetYearMonth, safeFileName);
        ReportOutputHistory history = createHistory(createdBy, targetYearMonth, safeFileName, filePath, STATUS_PROCESSING, null);
        return reportHistoryDao.insertProcessing(history);
    }

    @Transactional
    public void updateSuccessHistory(Long reportOutputHistoryId, MonthlyReportFileDto reportFile, Path reportPath) {
        ReportOutputHistory history = new ReportOutputHistory();
        history.setReportOutputHistoryId(reportOutputHistoryId);
        history.setFileName(reportFile.getFileName());
        history.setFilePath(reportPath.toString());
        history.setStatus(STATUS_SUCCESS);
        history.setErrorMessage(null);
        reportHistoryDao.updateStatus(history);
    }

    @Transactional
    public void updateErrorHistory(Long reportOutputHistoryId, String targetYearMonth, String fileName, String errorMessage) {
        String safeFileName = safeFileName(fileName);
        ReportOutputHistory history = new ReportOutputHistory();
        history.setReportOutputHistoryId(reportOutputHistoryId);
        history.setFileName(safeFileName);
        history.setFilePath(buildReportFilePath(targetYearMonth, safeFileName));
        history.setStatus(STATUS_ERROR);
        history.setErrorMessage(truncate(errorMessage));
        reportHistoryDao.updateStatus(history);
    }

    public void deleteReportFile(Path reportPath) {
        try {
            if (reportPath != null && Files.exists(reportPath) && Files.isRegularFile(reportPath)) {
                Files.delete(reportPath);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to delete report file. path={}", reportPath, e);
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

    private String safeFileName(String fileName) {
        return FileNameUtils.isSafeFileName(fileName)
                ? fileName
                : FileNameUtils.sanitizeNamePart(fileName, "monthly-report") + ".xlsx";
    }

    private String buildReportFilePath(String targetYearMonth, String safeFileName) {
        return Paths.get(GENERATED_REPORTS_DIR, targetYearMonth, safeFileName).toString();
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
