package com.example.workreport.dto;

import java.util.ArrayList;
import java.util.List;

public class MonthlyReportDataDto {

    private MonthlyReportSummaryDto summary;

    private List<MonthlyReportCategorySummaryDto> categorySummaries = new ArrayList<MonthlyReportCategorySummaryDto>();

    private List<MonthlyReportDailyDetailDto> dailyDetails = new ArrayList<MonthlyReportDailyDetailDto>();

    public MonthlyReportSummaryDto getSummary() {
        return summary;
    }

    public void setSummary(MonthlyReportSummaryDto summary) {
        this.summary = summary;
    }

    public List<MonthlyReportCategorySummaryDto> getCategorySummaries() {
        return categorySummaries;
    }

    public void setCategorySummaries(List<MonthlyReportCategorySummaryDto> categorySummaries) {
        this.categorySummaries = categorySummaries;
    }

    public List<MonthlyReportDailyDetailDto> getDailyDetails() {
        return dailyDetails;
    }

    public void setDailyDetails(List<MonthlyReportDailyDetailDto> dailyDetails) {
        this.dailyDetails = dailyDetails;
    }
}
