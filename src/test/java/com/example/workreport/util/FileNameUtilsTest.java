package com.example.workreport.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FileNameUtilsTest {

    @Test
    public void sanitizeNamePartReplacesUnsafeCharacters() {
        String actual = FileNameUtils.sanitizeNamePart("../山田/太郎:report", "default");

        assertEquals("_山田_太郎_report", actual);
    }

    @Test
    public void sanitizeNamePartUsesDefaultForBlankValue() {
        String actual = FileNameUtils.sanitizeNamePart("   ", "default");

        assertEquals("default", actual);
    }

    @Test
    public void isSafeFileNameRejectsPathTraversal() {
        assertFalse(FileNameUtils.isSafeFileName("../monthly-report.xlsx"));
        assertFalse(FileNameUtils.isSafeFileName("reports\\monthly-report.xlsx"));
        assertTrue(FileNameUtils.isSafeFileName("monthly-report.xlsx"));
    }
}
