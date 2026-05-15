package com.example.workreport.testsupport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public final class MockHttpObjects {

    private MockHttpObjects() {
    }

    public static MockSession session() {
        return new MockSession();
    }

    public static HttpServletRequest request(final MockSession session, final String contextPath) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[] {HttpServletRequest.class},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("getSession".equals(method.getName())) {
                            return session.asHttpSession();
                        }
                        if ("getContextPath".equals(method.getName())) {
                            return contextPath;
                        }
                        return defaultValue(method.getReturnType());
                    }
                });
    }

    public static MockResponse response() {
        return new MockResponse();
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (Boolean.TYPE.equals(returnType)) {
            return Boolean.FALSE;
        }
        if (Void.TYPE.equals(returnType)) {
            return null;
        }
        return Integer.valueOf(0);
    }

    public static final class MockSession {

        private final Map<String, Object> attributes = new HashMap<String, Object>();

        private final HttpSession session = (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[] {HttpSession.class},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("getAttribute".equals(method.getName())) {
                            return attributes.get(args[0]);
                        }
                        if ("setAttribute".equals(method.getName())) {
                            attributes.put((String) args[0], args[1]);
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    }
                });

        public HttpSession asHttpSession() {
            return session;
        }

        public Object getAttribute(String name) {
            return attributes.get(name);
        }
    }

    public static final class MockResponse {

        private final ByteArrayOutputStream body = new ByteArrayOutputStream();

        private final Map<String, String> headers = new HashMap<String, String>();

        private String contentType;

        private long contentLength;

        private String redirectLocation;

        private final HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[] {HttpServletResponse.class},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("setContentType".equals(method.getName())) {
                            contentType = (String) args[0];
                            return null;
                        }
                        if ("setHeader".equals(method.getName())) {
                            headers.put((String) args[0], (String) args[1]);
                            return null;
                        }
                        if ("setContentLengthLong".equals(method.getName())) {
                            contentLength = ((Long) args[0]).longValue();
                            return null;
                        }
                        if ("getOutputStream".equals(method.getName())) {
                            return new ByteArrayServletOutputStream(body);
                        }
                        if ("sendRedirect".equals(method.getName())) {
                            redirectLocation = (String) args[0];
                            return null;
                        }
                        return defaultValue(method.getReturnType());
                    }
                });

        public HttpServletResponse asHttpServletResponse() {
            return response;
        }

        public String getContentType() {
            return contentType;
        }

        public String getHeader(String name) {
            return headers.get(name);
        }

        public long getContentLength() {
            return contentLength;
        }

        public byte[] getBody() {
            return body.toByteArray();
        }

        public String getRedirectLocation() {
            return redirectLocation;
        }
    }

    private static final class ByteArrayServletOutputStream extends ServletOutputStream {

        private final ByteArrayOutputStream body;

        ByteArrayServletOutputStream(ByteArrayOutputStream body) {
            this.body = body;
        }

        @Override
        public void write(int b) throws IOException {
            body.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }
    }
}
