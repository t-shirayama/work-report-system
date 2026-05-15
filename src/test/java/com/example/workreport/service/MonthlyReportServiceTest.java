package com.example.workreport.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.example.workreport.dao.MonthlyReportDao;
import com.example.workreport.dao.ReportHistoryDao;
import com.example.workreport.dto.MonthlyReportCategorySummaryDto;
import com.example.workreport.dto.MonthlyReportConditionDto;
import com.example.workreport.dto.MonthlyReportDailyDetailDto;
import com.example.workreport.dto.MonthlyReportFileDto;
import com.example.workreport.dto.MonthlyReportSummaryDto;
import com.example.workreport.entity.User;
import com.example.workreport.form.MonthlyReportForm;

public class MonthlyReportServiceTest {

    @Test
    public void validateDetectsRequiredFields() {
        MonthlyReportService service = new MonthlyReportService(null, null, null);

        List<String> errors = service.validate(new MonthlyReportForm(), user("USER"));

        assertEquals(4, errors.size());
    }

    @Test
    public void validateDetectsInvalidMonthRange() {
        MonthlyReportService service = new MonthlyReportService(null, null, null);
        MonthlyReportForm form = validForm();
        form.setTargetMonth("13");

        List<String> errors = service.validate(form, user("USER"));

        assertTrue(errors.contains("対象月は1から12の範囲で入力してください。"));
    }

    @Test
    public void validateRejectsOtherEmployeeForGeneralUser() {
        MonthlyReportService service = new MonthlyReportService(null, null, null);
        MonthlyReportForm form = validForm();
        form.setEmployeeName("別ユーザー");

        List<String> errors = service.validate(form, user("USER"));

        assertTrue(errors.contains("一般ユーザーは自分の月次報告書のみ出力できます。"));
    }

    @Test
    public void validateAllowsOtherEmployeeForAdmin() {
        MonthlyReportService service = new MonthlyReportService(null, null, null);
        MonthlyReportForm form = validForm();
        form.setEmployeeName("別ユーザー");

        List<String> errors = service.validate(form, user("ADMIN"));

        assertTrue(errors.isEmpty());
    }

    @Test
    public void validateDetectsInvalidFormatsAndLengths() {
        MonthlyReportService service = new MonthlyReportService(null, null, null);
        MonthlyReportForm form = validForm();
        form.setTargetYear("20A6");
        form.setTargetMonth("AA");
        form.setDepartmentName(repeat("D", 101));
        form.setEmployeeName(repeat("E", 101));

        List<String> errors = service.validate(form, user("ADMIN"));

        assertTrue(errors.contains("対象年は4桁の数値で入力してください。"));
        assertTrue(errors.contains("対象月は1から12の数値で入力してください。"));
        assertTrue(errors.contains("部署は100文字以内で入力してください。"));
        assertTrue(errors.contains("社員は100文字以内で入力してください。"));
    }

    @Test
    public void validateDetectsDepartmentMismatchForGeneralUser() {
        MonthlyReportService service = new MonthlyReportService(null, null, null);
        MonthlyReportForm form = validForm();
        form.setDepartmentName("営業部");

        List<String> errors = service.validate(form, user("USER"));

        assertTrue(errors.contains("一般ユーザーは自分の部署の月次報告書のみ出力できます。"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateRejectsMissingLoginUser() {
        MonthlyReportService service = new MonthlyReportService(null, null, null);

        service.validate(validForm(), null);
    }

    @Test
    public void createReportUpdatesProcessingHistoryToSuccess() throws Exception {
        CapturingReportHistoryService historyService = new CapturingReportHistoryService();
        MonthlyReportService service = new MonthlyReportService(
                new StubMonthlyReportDao(),
                new StubExcelReportService(new byte[] {1, 2, 3}),
                historyService);

        MonthlyReportFileDto file = service.createReport(validForm(), user("USER"));

        assertEquals(Long.valueOf(100L), historyService.processingHistoryId);
        assertEquals(Long.valueOf(100L), historyService.successHistoryId);
        assertEquals("月次報告書_202605_佐藤花子.xlsx", file.getFileName());
        assertArrayEquals(new byte[] {1, 2, 3}, file.getContent());
    }

    @Test
    public void createReportBuildsSummaryAndCategoryRatio() throws Exception {
        StubMonthlyReportDao dao = new StubMonthlyReportDao();
        dao.summary.setTotalWorkHours(new java.math.BigDecimal("10"));
        dao.summary.setWorkDays(2);
        MonthlyReportCategorySummaryDto categorySummary = new MonthlyReportCategorySummaryDto();
        categorySummary.setTotalHours(new java.math.BigDecimal("2.5"));
        dao.categorySummaries = Collections.singletonList(categorySummary);
        StubExcelReportService excelReportService = new StubExcelReportService(new byte[] {9});
        MonthlyReportService service = new MonthlyReportService(
                dao,
                excelReportService,
                new CapturingReportHistoryService());

        service.createReport(validForm(), user("USER"));

        assertEquals(new java.math.BigDecimal("5.00"), excelReportService.reportData.getSummary().getAverageWorkHours());
        assertEquals(new java.math.BigDecimal("25.0"), excelReportService.reportData.getCategorySummaries().get(0).getRatio());
        assertEquals("202605", excelReportService.reportData.getSummary().getTargetYearMonth());
    }

    @Test
    public void createReportUsesConditionNamesWhenSummaryNamesAreBlank() throws Exception {
        BlankNameMonthlyReportDao dao = new BlankNameMonthlyReportDao();
        StubExcelReportService excelReportService = new StubExcelReportService(new byte[] {8});
        MonthlyReportService service = new MonthlyReportService(
                dao,
                excelReportService,
                new CapturingReportHistoryService());

        service.createReport(validForm(), user("USER"));

        assertEquals("佐藤 花子", excelReportService.reportData.getSummary().getEmployeeName());
        assertEquals("開発部", excelReportService.reportData.getSummary().getDepartmentName());
    }

    @Test
    public void createReportKeepsOriginalRuntimeExceptionWhenErrorHistoryUpdateFails() throws Exception {
        CapturingReportHistoryService historyService = new CapturingReportHistoryService();
        historyService.failErrorUpdate = true;
        MonthlyReportService service = new MonthlyReportService(
                new FailingMonthlyReportDao(),
                new StubExcelReportService(new byte[] {1}),
                historyService);

        try {
            service.createReport(validForm(), user("USER"));
            fail("RuntimeException is expected.");
        } catch (RuntimeException e) {
            assertEquals("summary failed", e.getMessage());
        }

        assertEquals(Long.valueOf(100L), historyService.errorHistoryId);
    }

    @Test
    public void createReportUpdatesErrorHistoryWhenExcelCreationFails() throws Exception {
        CapturingReportHistoryService historyService = new CapturingReportHistoryService();
        MonthlyReportService service = new MonthlyReportService(
                new StubMonthlyReportDao(),
                new StubExcelReportService(new IOException("excel failed")),
                historyService);

        try {
            service.createReport(validForm(), user("USER"));
            fail("IOException is expected.");
        } catch (IOException e) {
            assertEquals("excel failed", e.getMessage());
        }

        assertEquals(Long.valueOf(100L), historyService.errorHistoryId);
    }

    @Test
    public void createReportKeepsOriginalIOExceptionWhenErrorHistoryUpdateFails() throws Exception {
        CapturingReportHistoryService historyService = new CapturingReportHistoryService();
        historyService.failErrorUpdate = true;
        MonthlyReportService service = new MonthlyReportService(
                new StubMonthlyReportDao(),
                new StubExcelReportService(new IOException("excel failed")),
                historyService);

        try {
            service.createReport(validForm(), user("USER"));
            fail("IOException is expected.");
        } catch (IOException e) {
            assertEquals("excel failed", e.getMessage());
        }

        assertEquals(Long.valueOf(100L), historyService.errorHistoryId);
    }

    private MonthlyReportForm validForm() {
        MonthlyReportForm form = new MonthlyReportForm();
        form.setTargetYear("2026");
        form.setTargetMonth("5");
        form.setDepartmentName("開発部");
        form.setEmployeeName("佐藤 花子");
        return form;
    }

    private User user(String roleCode) {
        User user = new User();
        user.setUserId(10L);
        user.setRoleCode(roleCode);
        user.setDepartmentName("開発部");
        user.setEmployeeName("佐藤 花子");
        return user;
    }

    private String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }

    private static class StubMonthlyReportDao extends MonthlyReportDao {

        private MonthlyReportSummaryDto summary = new MonthlyReportSummaryDto();

        private List<MonthlyReportCategorySummaryDto> categorySummaries = Collections.emptyList();

        StubMonthlyReportDao() {
            super(null);
        }

        @Override
        public MonthlyReportSummaryDto findSummary(MonthlyReportConditionDto condition) {
            summary.setTargetYearMonth(condition.getTargetYearMonth());
            summary.setEmployeeName(condition.getEmployeeName());
            summary.setDepartmentName(condition.getDepartmentName());
            return summary;
        }

        @Override
        public List<MonthlyReportCategorySummaryDto> findCategorySummaries(MonthlyReportConditionDto condition) {
            return categorySummaries;
        }

        @Override
        public List<MonthlyReportDailyDetailDto> findDailyDetails(MonthlyReportConditionDto condition) {
            return Collections.emptyList();
        }
    }

    private static class FailingMonthlyReportDao extends StubMonthlyReportDao {

        @Override
        public MonthlyReportSummaryDto findSummary(MonthlyReportConditionDto condition) {
            throw new RuntimeException("summary failed");
        }
    }

    private static class BlankNameMonthlyReportDao extends StubMonthlyReportDao {

        @Override
        public MonthlyReportSummaryDto findSummary(MonthlyReportConditionDto condition) {
            MonthlyReportSummaryDto summary = new MonthlyReportSummaryDto();
            summary.setTargetYearMonth(condition.getTargetYearMonth());
            return summary;
        }
    }

    private static class StubExcelReportService extends ExcelReportService {

        private final byte[] content;

        private final IOException exception;

        private com.example.workreport.dto.MonthlyReportDataDto reportData;

        StubExcelReportService(byte[] content) {
            this.content = content;
            this.exception = null;
        }

        StubExcelReportService(IOException exception) {
            this.content = null;
            this.exception = exception;
        }

        @Override
        public byte[] createMonthlyReport(com.example.workreport.dto.MonthlyReportDataDto reportData) throws IOException {
            this.reportData = reportData;
            if (exception != null) {
                throw exception;
            }
            return content;
        }
    }

    private static class CapturingReportHistoryService extends ReportHistoryService {

        private Long processingHistoryId;

        private Long successHistoryId;

        private Long errorHistoryId;

        private boolean failErrorUpdate;

        CapturingReportHistoryService() {
            super(new ReportHistoryDao(null));
        }

        @Override
        public Long saveProcessingHistory(Long createdBy, String targetYearMonth, String fileName) {
            this.processingHistoryId = 100L;
            return processingHistoryId;
        }

        @Override
        public Path saveReportFile(String targetYearMonth, MonthlyReportFileDto reportFile) {
            return Paths.get("generated-reports", targetYearMonth, reportFile.getFileName());
        }

        @Override
        public void updateSuccessHistory(Long reportOutputHistoryId, MonthlyReportFileDto reportFile, Path reportPath) {
            this.successHistoryId = reportOutputHistoryId;
        }

        @Override
        public void updateErrorHistory(Long reportOutputHistoryId, String targetYearMonth, String fileName, String errorMessage) {
            this.errorHistoryId = reportOutputHistoryId;
            if (failErrorUpdate) {
                throw new RuntimeException("history failed");
            }
        }

        @Override
        public void deleteReportFile(Path reportPath) {
        }
    }
}
