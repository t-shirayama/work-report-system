package com.example.workreport.dto;

public class ReportHistoryDto {

    private Long reportOutputHistoryId;

    private String outputAt;

    private String targetYearMonth;

    private String reportType;

    private String reportTypeName;

    private Long createdBy;

    private String createdByName;

    private Long targetUserId;

    private String targetUserName;

    private String status;

    private String statusName;

    private String fileName;

    private String filePath;

    private String errorMessage;

    public Long getReportOutputHistoryId() {
        return reportOutputHistoryId;
    }

    public void setReportOutputHistoryId(Long reportOutputHistoryId) {
        this.reportOutputHistoryId = reportOutputHistoryId;
    }

    public String getOutputAt() {
        return outputAt;
    }

    public void setOutputAt(String outputAt) {
        this.outputAt = outputAt;
    }

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

    public String getReportTypeName() {
        return reportTypeName;
    }

    public void setReportTypeName(String reportTypeName) {
        this.reportTypeName = reportTypeName;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
