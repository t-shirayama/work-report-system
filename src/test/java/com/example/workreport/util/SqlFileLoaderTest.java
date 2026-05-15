package com.example.workreport.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SqlFileLoaderTest {

    @Test
    public void loadReturnsSqlTextFromClasspath() {
        String sql = SqlFileLoader.load("sql/dao/work-report/search-base.sql");

        assertTrue(sql.contains("FROM work_reports wr"));
        assertTrue(sql.contains("WHERE 1 = 1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadThrowsExceptionWhenSqlFileDoesNotExist() {
        SqlFileLoader.load("sql/dao/missing.sql");
    }
}
