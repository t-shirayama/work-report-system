package com.example.workreport.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.example.workreport.dao.ReportHistoryDao;
import com.example.workreport.dto.MonthlyReportFileDto;
import com.example.workreport.dto.ReportHistoryDto;
import com.example.workreport.entity.ReportOutputHistory;
import com.example.workreport.entity.User;
import com.example.workreport.form.ReportHistorySearchForm;

public class ReportHistoryServiceTest {

    @Test
    public void readReportFileRejectsPathOutsideGeneratedReports() throws Exception {
        ReportHistoryService service = new ReportHistoryService(new ReportHistoryDao(null));
        ReportHistoryDto history = history("SUCCESS", "../outside.xlsx");

        byte[] actual = service.readReportFile(history);

        assertNull(actual);
    }

    @Test
    public void readReportFileReturnsNullForNullOrNonSuccessOrMissingFile() throws Exception {
        ReportHistoryService service = new ReportHistoryService(new ReportHistoryDao(null));

        assertNull(service.readReportFile(null));
        assertNull(service.readReportFile(history("ERROR", "generated-reports/test/report.xlsx")));
        assertNull(service.readReportFile(history("SUCCESS", "generated-reports/test/missing.xlsx")));
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

    @Test
    public void saveReportFileWritesSafeFileUnderTargetMonth() throws Exception {
        ReportHistoryService service = new ReportHistoryService(new ReportHistoryDao(null));
        MonthlyReportFileDto file = new MonthlyReportFileDto();
        file.setFileName("report.xlsx");
        file.setContent(new byte[] {4, 5, 6});
        Path expectedPath = Paths.get("generated-reports", "209912", "report.xlsx").toAbsolutePath().normalize();
        Files.deleteIfExists(expectedPath);

        Path reportPath = service.saveReportFile("209912", file);

        assertArrayEquals(new byte[] {4, 5, 6}, Files.readAllBytes(reportPath));
        Files.deleteIfExists(reportPath);
        Files.deleteIfExists(reportPath.getParent());
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

    @Test(expected = IllegalArgumentException.class)
    public void searchRejectsMissingLoginUser() {
        ReportHistoryService service = new ReportHistoryService(new ReportHistoryDao(null));

        service.search(new ReportHistorySearchForm(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findByIdRejectsMissingLoginUser() {
        ReportHistoryService service = new ReportHistoryService(new ReportHistoryDao(null));

        service.findById(100L, null);
    }

    @Test
    public void findAllUsesAllRowsForAdminAndOwnRowsForUser() {
        CapturingReportHistoryDao dao = new CapturingReportHistoryDao();
        ReportHistoryService service = new ReportHistoryService(dao);

        assertSame(dao.allRows, service.findAll(user(1L, "ADMIN")));
        assertSame(dao.ownRows, service.findAll(user(10L, "USER")));
        assertEquals(Long.valueOf(10L), dao.targetUserId);
    }

    @Test
    public void searchUsesAllRowsForAdminAndOwnRowsForUserAndNullFormFallsBack() {
        CapturingReportHistoryDao dao = new CapturingReportHistoryDao();
        ReportHistoryService service = new ReportHistoryService(dao);
        ReportHistorySearchForm form = new ReportHistorySearchForm();

        assertSame(dao.searchRows, service.search(form, user(1L, "ADMIN")));
        assertSame(dao.searchOwnRows, service.search(form, user(10L, "USER")));
        assertSame(dao.ownRows, service.search(null, user(10L, "USER")));
    }

    @Test
    public void findByIdAppliesUserScope() {
        CapturingReportHistoryDao dao = new CapturingReportHistoryDao();
        ReportHistoryService service = new ReportHistoryService(dao);
        dao.detail = history("SUCCESS", "generated-reports/test/report.xlsx");
        dao.detail.setCreatedBy(10L);
        dao.detail.setTargetUserId(10L);

        assertSame(dao.detail, service.findById(100L, user(1L, "ADMIN")));
        assertSame(dao.detail, service.findById(100L, user(10L, "USER")));
        assertNull(service.findById(100L, user(99L, "USER")));

        dao.detail = null;
        assertNull(service.findById(100L, user(1L, "ADMIN")));
    }

    @Test
    public void saveProcessingHistoryCreatesProcessingRow() {
        CapturingReportHistoryDao dao = new CapturingReportHistoryDao();
        ReportHistoryService service = new ReportHistoryService(dao);

        Long historyId = service.saveProcessingHistory(10L, 20L, "202605", "monthly-report.xlsx");

        assertEquals(Long.valueOf(100L), historyId);
        assertEquals("PROCESSING", dao.inserted.getStatus());
        assertEquals(Long.valueOf(10L), dao.inserted.getCreatedBy());
        assertEquals(Long.valueOf(20L), dao.inserted.getTargetUserId());
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

    @Test
    public void updateErrorHistoryTruncatesLongMessageAndAddsExtensionWhenNeeded() {
        CapturingReportHistoryDao dao = new CapturingReportHistoryDao();
        ReportHistoryService service = new ReportHistoryService(dao);
        String longMessage = repeat("E", 1001);

        service.updateErrorHistory(100L, "202605", "../bad", longMessage);

        assertEquals("_bad.xlsx", dao.updated.getFileName());
        assertEquals(1000, dao.updated.getErrorMessage().length());
    }

    @Test
    public void updateErrorHistoryAcceptsNullMessage() {
        CapturingReportHistoryDao dao = new CapturingReportHistoryDao();
        ReportHistoryService service = new ReportHistoryService(dao);

        service.updateErrorHistory(100L, "202605", "report.xlsx", null);

        assertEquals("report.xlsx", dao.updated.getFileName());
        assertNull(dao.updated.getErrorMessage());
    }

    @Test
    public void deleteReportFileIgnoresNullAndDeletesExistingFile() throws Exception {
        ReportHistoryService service = new ReportHistoryService(new ReportHistoryDao(null));
        Path reportDir = Paths.get("generated-reports", "delete-test").toAbsolutePath().normalize();
        Files.createDirectories(reportDir);
        Path reportPath = reportDir.resolve("report.xlsx");
        Files.write(reportPath, new byte[] {1});

        service.deleteReportFile(null);
        service.deleteReportFile(reportPath);

        assertEquals(false, Files.exists(reportPath));
        Files.deleteIfExists(reportDir);
    }

    private ReportHistoryDto history(String status, String filePath) {
        ReportHistoryDto history = new ReportHistoryDto();
        history.setStatus(status);
        history.setFilePath(filePath);
        return history;
    }

    private User user(Long userId, String roleCode) {
        User user = new User();
        user.setUserId(userId);
        user.setRoleCode(roleCode);
        return user;
    }

    private String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }

    private static class CapturingReportHistoryDao extends ReportHistoryDao {

        private ReportOutputHistory inserted;

        private ReportOutputHistory updated;

        private ReportHistoryDto detail;

        private Long targetUserId;

        private final List<ReportHistoryDto> allRows = Arrays.asList(new ReportHistoryDto());

        private final List<ReportHistoryDto> ownRows = Arrays.asList(new ReportHistoryDto());

        private final List<ReportHistoryDto> searchRows = Arrays.asList(new ReportHistoryDto());

        private final List<ReportHistoryDto> searchOwnRows = Arrays.asList(new ReportHistoryDto());

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

        @Override
        public List<ReportHistoryDto> findAll() {
            return allRows;
        }

        @Override
        public List<ReportHistoryDto> findAllByTargetUserId(Long targetUserId) {
            this.targetUserId = targetUserId;
            return ownRows;
        }

        @Override
        public List<ReportHistoryDto> search(ReportHistorySearchForm form) {
            return searchRows;
        }

        @Override
        public List<ReportHistoryDto> search(ReportHistorySearchForm form, Long targetUserId) {
            this.targetUserId = targetUserId;
            return searchOwnRows;
        }

        @Override
        public ReportHistoryDto findById(Long reportOutputHistoryId) {
            return detail;
        }
    }
}
