package com.example.workreport.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.workreport.dao.WorkReportDao;
import com.example.workreport.dto.WorkReportSearchResultDto;
import com.example.workreport.entity.User;
import com.example.workreport.entity.WorkReport;
import com.example.workreport.form.WorkReportForm;
import com.example.workreport.form.WorkReportSearchForm;

@Service
public class WorkReportService {

    private static final Set<String> WORK_CATEGORIES = new HashSet<String>(Arrays.asList(
            "DESIGN",
            "DEVELOPMENT",
            "TEST",
            "MEETING",
            "DOCUMENT",
            "OTHER"));

    private final WorkReportDao workReportDao;

    @Autowired
    public WorkReportService(WorkReportDao workReportDao) {
        this.workReportDao = workReportDao;
    }

    public List<String> validate(WorkReportForm form) {
        List<String> errors = new ArrayList<String>();

        if (!StringUtils.hasText(form.getWorkDate())) {
            errors.add("作業日は必須です。");
        } else if (parseWorkDate(form.getWorkDate()) == null) {
            errors.add("作業日は正しい日付を入力してください。");
        }

        if (!StringUtils.hasText(form.getProjectName())) {
            errors.add("プロジェクト名は必須です。");
        } else if (form.getProjectName().length() > 100) {
            errors.add("プロジェクト名は100文字以内で入力してください。");
        }

        if (!StringUtils.hasText(form.getWorkCategory())) {
            errors.add("作業分類は必須です。");
        } else if (!WORK_CATEGORIES.contains(form.getWorkCategory())) {
            errors.add("作業分類の値が正しくありません。");
        }

        BigDecimal workHours = parseWorkHours(form.getWorkHours());
        if (!StringUtils.hasText(form.getWorkHours())) {
            errors.add("作業時間は必須です。");
        } else if (workHours == null) {
            errors.add("作業時間は数値で入力してください。");
        } else if (BigDecimal.ZERO.compareTo(workHours) >= 0) {
            errors.add("作業時間は0より大きい数値で入力してください。");
        } else if (new BigDecimal("24").compareTo(workHours) < 0) {
            errors.add("作業時間は24時間以内で入力してください。");
        }

        if (!StringUtils.hasText(form.getWorkContent())) {
            errors.add("作業内容は必須です。");
        } else if (form.getWorkContent().length() > 1000) {
            errors.add("作業内容は1000文字以内で入力してください。");
        }

        return errors;
    }

    public void register(WorkReportForm form, User loginUser) {
        requireLoginUser(loginUser);
        WorkReport workReport = new WorkReport();
        workReport.setUserId(loginUser.getUserId());
        workReport.setDepartmentId(loginUser.getDepartmentId());
        workReport.setWorkDate(parseWorkDate(form.getWorkDate()));
        workReport.setProjectName(form.getProjectName());
        workReport.setWorkCategory(form.getWorkCategory());
        workReport.setWorkHours(parseWorkHours(form.getWorkHours()));
        workReport.setWorkContent(form.getWorkContent());

        workReportDao.insert(workReport);
    }

    public List<String> validateSearch(WorkReportSearchForm form) {
        List<String> errors = new ArrayList<String>();
        java.util.Date dateFrom = null;
        java.util.Date dateTo = null;

        if (StringUtils.hasText(form.getDateFrom())) {
            dateFrom = parseWorkDate(form.getDateFrom());
            if (dateFrom == null) {
                errors.add("対象期間 From は正しい日付を入力してください。");
            }
        }

        if (StringUtils.hasText(form.getDateTo())) {
            dateTo = parseWorkDate(form.getDateTo());
            if (dateTo == null) {
                errors.add("対象期間 To は正しい日付を入力してください。");
            }
        }

        if (dateFrom != null && dateTo != null && dateFrom.after(dateTo)) {
            errors.add("対象期間 From は To 以前の日付を入力してください。");
        }

        if (StringUtils.hasText(form.getWorkCategory()) && !WORK_CATEGORIES.contains(form.getWorkCategory())) {
            errors.add("作業分類の値が正しくありません。");
        }

        return errors;
    }

    public List<WorkReportSearchResultDto> search(WorkReportSearchForm form, User loginUser) {
        return workReportDao.search(
                toSqlDate(parseWorkDate(form.getDateFrom())),
                toSqlDate(parseWorkDate(form.getDateTo())),
                form.getEmployeeName(),
                form.getDepartmentName(),
                form.getWorkCategory(),
                form.getProjectName(),
                getSearchUserId(loginUser));
    }

    private Long getSearchUserId(User loginUser) {
        requireLoginUser(loginUser);
        if (isAdmin(loginUser)) {
            return null;
        }
        return loginUser.getUserId();
    }

    private void requireLoginUser(User loginUser) {
        if (loginUser == null) {
            throw new IllegalArgumentException("loginUser is required.");
        }
    }

    private boolean isAdmin(User user) {
        return user != null && "ADMIN".equals(user.getRoleCode());
    }

    private Date toSqlDate(java.util.Date value) {
        if (value == null) {
            return null;
        }
        return new Date(value.getTime());
    }

    private java.util.Date parseWorkDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setLenient(false);
        try {
            return format.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    private BigDecimal parseWorkHours(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
