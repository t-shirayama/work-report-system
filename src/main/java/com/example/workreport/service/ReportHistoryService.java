package com.example.workreport.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.workreport.dao.ReportHistoryDao;
import com.example.workreport.dto.MonthlyReportFileDto;
import com.example.workreport.dto.ReportHistoryDto;
import com.example.workreport.entity.ReportOutputHistory;
import com.example.workreport.form.ReportHistorySearchForm;

@Service
public class ReportHistoryService {

    public static final String REPORT_TYPE_MONTHLY_WORK_REPORT = "MONTHLY_WORK_REPORT";

    public static final String STATUS_SUCCESS = "SUCCESS";

    public static final String STATUS_ERROR = "ERROR";

    private static final String GENERATED_REPORTS_DIR = "generated-reports";

    private final ReportHistoryDao reportHistoryDao;

    @Autowired
    public ReportHistoryService(ReportHistoryDao reportHistoryDao) {
        this.reportHistoryDao = reportHistoryDao;
    }

    public List<ReportHistoryDto> findAll() {
        return reportHistoryDao.findAll();
    }

    public List<ReportHistoryDto> search(ReportHistorySearchForm form) {
        if (form == null) {
            return reportHistoryDao.findAll();
        }
        return reportHistoryDao.search(form);
    }

    public ReportHistoryDto findById(Long reportOutputHistoryId) {
        return reportHistoryDao.findById(reportOutputHistoryId);
    }

    public Path saveReportFile(String targetYearMonth, MonthlyReportFileDto reportFile) throws IOException {
        Path reportDir = Paths.get(GENERATED_REPORTS_DIR, targetYearMonth);
        Files.createDirectories(reportDir);

        Path reportPath = reportDir.resolve(reportFile.getFileName());
        Files.write(reportPath, reportFile.getContent());
        return reportPath;
    }

    public void saveSuccessHistory(Long createdBy, String targetYearMonth, MonthlyReportFileDto reportFile, Path reportPath) {
        ReportOutputHistory history = createHistory(createdBy, targetYearMonth, reportFile.getFileName(), reportPath.toString(), STATUS_SUCCESS, null);
        reportHistoryDao.insert(history);
    }

    public void saveErrorHistory(Long createdBy, String targetYearMonth, String fileName, String errorMessage) {
        String filePath = Paths.get(GENERATED_REPORTS_DIR, targetYearMonth, fileName).toString();
        ReportOutputHistory history = createHistory(createdBy, targetYearMonth, fileName, filePath, STATUS_ERROR, truncate(errorMessage));
        reportHistoryDao.insert(history);
    }

    public byte[] readReportFile(ReportHistoryDto history) throws IOException {
        if (history == null || !STATUS_SUCCESS.equals(history.getStatus())) {
            return null;
        }

        Path reportPath = Paths.get(history.getFilePath());
        if (!Files.exists(reportPath)) {
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
}
