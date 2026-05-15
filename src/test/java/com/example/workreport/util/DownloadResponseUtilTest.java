package com.example.workreport.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DownloadResponseUtilTest {

    @Test
    public void buildContentDispositionIncludesEncodedJapaneseFileName() throws Exception {
        String header = DownloadResponseUtil.buildContentDisposition("月次報告書 202605.xlsx");

        assertTrue(header.contains("filename="));
        assertTrue(header.contains("filename*=UTF-8''"));
        assertTrue(header.contains("%E6%9C%88%E6%AC%A1%E5%A0%B1%E5%91%8A%E6%9B%B8%20202605.xlsx"));
    }
}
