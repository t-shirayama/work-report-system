package com.example.workreport.util;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

public final class DownloadResponseUtil {

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private DownloadResponseUtil() {
    }

    public static void writeExcel(HttpServletResponse response, String fileName, byte[] content) throws IOException {
        response.setContentType(EXCEL_CONTENT_TYPE);
        response.setHeader("Content-Disposition", buildContentDisposition(fileName));
        response.setContentLength(content.length);
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }

    private static String buildContentDisposition(String fileName) throws IOException {
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        return "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;
    }
}
