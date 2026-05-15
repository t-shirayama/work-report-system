package com.example.workreport.form;

public class ReportHistorySearchForm {

    private String targetYearMonth;

    private String reportType;

    private String createdByName;

    private String status;

    public String getTargetYearMonth() {
        return targetYearMonth;
    }

    public void setTargetYearMonth(String targetYearMonth) {
        this.targetYearMonth = targetYearMonth;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
