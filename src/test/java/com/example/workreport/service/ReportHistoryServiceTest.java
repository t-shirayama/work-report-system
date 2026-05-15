package com.example.workreport.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.example.workreport.dao.ReportHistoryDao;
import com.example.workreport.dto.ReportHistoryDto;

public class ReportHistoryServiceTest {

    @Test
    public void readReportFileRejectsPathOutsideGeneratedReports() throws Exception {
        ReportHistoryService service = new ReportHistoryService(new ReportHistoryDao(null));
        ReportHistoryDto history = history("SUCCESS", "../outside.xlsx");

        byte[] actual = service.readReportFile(history);

        assertNull(actual);
    }

    @Test
    public void readReportFileReadsFileUnderGeneratedReports() throws Exception {
        ReportHistoryService service = new ReportHistoryService(new ReportHistoryDao(null));
        Path reportDir = Paths.get("generated-reports", "test").toAbsolutePath().normalize();
        Files.createDirectories(reportDir);
        Path reportPath = reportDir.resolve("report.xlsx");
        byte[] content = new byte[] {1, 2, 3};
        Files.write(reportPath, content);

        byte[] actual = service.readReportFile(history("SUCCESS", reportPath.toString()));

        assertArrayEquals(content, actual);
        Files.deleteIfExists(reportPath);
        Files.deleteIfExists(reportDir);
    }

    private ReportHistoryDto history(String status, String filePath) {
        ReportHistoryDto history = new ReportHistoryDto();
        history.setStatus(status);
        history.setFilePath(filePath);
        return history;
    }
}
