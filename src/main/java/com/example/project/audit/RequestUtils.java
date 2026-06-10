package com.example.project.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class RequestUtils {

    public static Optional<HttpServletRequest> getCurrentHttpRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return Optional.empty();
        return Optional.of(attrs.getRequest());
    }

    public static ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request == null) return null;
        if (request instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) request;
        }
        ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request, 10240);
        request.setAttribute("CACHED_REQUEST", wrapper);
        return wrapper;
    }

    public static ContentCachingResponseWrapper wrapResponse() {
        // Try to obtain response wrapper from the current request attributes
        Optional<HttpServletRequest> reqOpt = getCurrentHttpRequest();
        if (reqOpt.isEmpty()) return null;
        Object v = reqOpt.get().getAttribute("CACHED_RESPONSE");
        if (v instanceof ContentCachingResponseWrapper) return (ContentCachingResponseWrapper) v;
        return null;
    }

    public static String getRequestBody(ContentCachingRequestWrapper wrapper) {
        if (wrapper == null) return null;
        byte[] buf = wrapper.getContentAsByteArray();
        if (buf == null || buf.length == 0) return null;
        int length = Math.min(buf.length, 2000);
        return new String(buf, 0, length, StandardCharsets.UTF_8);
    }

    public static String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    public static String maskSensitive(String input) {
        if (input == null) return null;
        // rudimentary masking: remove "password": "..." and tokens
        return input.replaceAll("(?i)\"password\"\\s*:\\s*\"(.*?)\"", "\"password\":\"***\"")
                .replaceAll("(?i)\"token\"\\s*:\\s*\"(.*?)\"", "\"token\":\"***\"")
                .replaceAll("(?i)\"authorization\"\\s*:\\s*\"(.*?)\"", "\"authorization\":\"***\"");
    }
}