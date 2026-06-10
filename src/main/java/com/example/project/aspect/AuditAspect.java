package com.example.project.aspect;

import com.example.project.model.AuditLog;
import com.example.project.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private static final int MAX_PAYLOAD_LOG_LENGTH = 2000;

    @Autowired
    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    // Intercept all controller methods
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        // Get request from context if available
        HttpServletRequest request = com.example.project.audit.RequestUtils.getCurrentHttpRequest().orElse(null);
        ContentCachingRequestWrapper cachingRequest = com.example.project.audit.RequestUtils.wrapRequest(request);
        ContentCachingResponseWrapper cachingResponse = com.example.project.audit.RequestUtils.wrapResponse();

        Long userId = null;
        String username = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            username = auth.getName();
        }

        String path = request != null ? request.getRequestURI() : "N/A";
        String method = request != null ? request.getMethod() : "N/A";
        String clientIp = request != null ? com.example.project.audit.RequestUtils.getClientIp(request) : null;

        String requestBody = com.example.project.audit.RequestUtils.getRequestBody(cachingRequest);
        requestBody = com.example.project.audit.RequestUtils.maskSensitive(requestBody);

        int status = 200;
        Object result;
        String responseBody = null;

        try {
            result = pjp.proceed();
            // if response wrapper present, try to read response body
            if (cachingResponse != null) {
                byte[] buf = cachingResponse.getContentAsByteArray();
                if (buf != null && buf.length > 0) {
                    int len = Math.min(buf.length, MAX_PAYLOAD_LOG_LENGTH);
                    responseBody = new String(buf, 0, len);
                }
            }
            if (responseBody == null) {
                // fallback to serializing result
                responseBody = com.example.project.audit.ResultSerializer.safeSerialize(result, MAX_PAYLOAD_LOG_LENGTH);
            }
            return result;
        } catch (Throwable ex) {
            status = com.example.project.audit.ExceptionMapper.toStatusCode(ex); // map exception to status
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("{} {} completed in {} ms with status {}", method, path, duration, status);

            AuditLog log = new AuditLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setHttpMethod(method);
            log.setPath(path);
            log.setRequestBody(requestBody);
            log.setResponseBody(responseBody);
            log.setResponseStatus(status);
            log.setDurationMs(duration);
            log.setTimestamp(LocalDateTime.now());
            log.setClientIp(clientIp);
            // Save asynchronously to avoid delaying response
            auditService.saveAsync(log);
        }
    }
}
