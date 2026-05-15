package com.example.workreport.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.example.workreport.dao.ReportHistoryDao;
import com.example.workreport.dto.MonthlyReportFileDto;
import com.example.workreport.dto.ReportHistoryDto;
import com.example.workreport.entity.ReportOutputHistory;

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

    @Test(expected = java.io.IOException.class)
    public void saveReportFileRejectsInvalidTargetYearMonth() throws Exception {
        ReportHistoryService service = new ReportHistoryService(new ReportHistoryDao(null));
        MonthlyReportFileDto file = new MonthlyReportFileDto();
        file.setFileName("report.xlsx");
        file.setContent(new byte[] {1});

        service.saveReportFile("../202605", file);
    }

    @Test(expected = java.io.IOException.class)
    public void saveReportFileRejectsUnsafeFileName() throws Exception {
        ReportHistoryService service = new ReportHistoryService(new ReportHistoryDao(null));
        MonthlyReportFileDto file = new MonthlyReportFileDto();
        file.setFileName("../report.xlsx");
        file.setContent(new byte[] {1});

        service.saveReportFile("202605", file);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllRejectsMissingLoginUser() {
        ReportHistoryService service = new ReportHistoryService(new ReportHistoryDao(null));

        service.findAll(null);
    }

    @Test
    public void saveProcessingHistoryCreatesProcessingRow() {
        CapturingReportHistoryDao dao = new CapturingReportHistoryDao();
        ReportHistoryService service = new ReportHistoryService(dao);

        Long historyId = service.saveProcessingHistory(10L, "202605", "monthly-report.xlsx");

        assertEquals(Long.valueOf(100L), historyId);
        assertEquals("PROCESSING", dao.inserted.getStatus());
        assertEquals("monthly-report.xlsx", dao.inserted.getFileName());
        assertEquals(Paths.get("generated-reports", "202605", "monthly-report.xlsx").toString(), dao.inserted.getFilePath());
    }

    @Test
    public void updateSuccessHistoryUpdatesSameRow() {
        CapturingReportHistoryDao dao = new CapturingReportHistoryDao();
        ReportHistoryService service = new ReportHistoryService(dao);
        MonthlyReportFileDto file = new MonthlyReportFileDto();
        file.setFileName("monthly-report.xlsx");

        service.updateSuccessHistory(100L, file, Paths.get("generated-reports", "202605", "monthly-report.xlsx"));

        assertEquals(Long.valueOf(100L), dao.updated.getReportOutputHistoryId());
        assertEquals("SUCCESS", dao.updated.getStatus());
        assertNull(dao.updated.getErrorMessage());
    }

    @Test
    public void updateErrorHistoryUpdatesSameRow() {
        CapturingReportHistoryDao dao = new CapturingReportHistoryDao();
        ReportHistoryService service = new ReportHistoryService(dao);

        service.updateErrorHistory(100L, "202605", "../bad.xlsx", "failed");

        assertEquals(Long.valueOf(100L), dao.updated.getReportOutputHistoryId());
        assertEquals("ERROR", dao.updated.getStatus());
        assertEquals("_bad.xlsx", dao.updated.getFileName());
        assertEquals("failed", dao.updated.getErrorMessage());
    }

    private ReportHistoryDto history(String status, String filePath) {
        ReportHistoryDto history = new ReportHistoryDto();
        history.setStatus(status);
        history.setFilePath(filePath);
        return history;
    }

    private static class CapturingReportHistoryDao extends ReportHistoryDao {

        private ReportOutputHistory inserted;

        private ReportOutputHistory updated;

        CapturingReportHistoryDao() {
            super(null);
        }

        @Override
        public Long insertProcessing(ReportOutputHistory history) {
            this.inserted = history;
            return 100L;
        }

        @Override
        public int updateStatus(ReportOutputHistory history) {
            this.updated = history;
            return 1;
        }
    }
}
