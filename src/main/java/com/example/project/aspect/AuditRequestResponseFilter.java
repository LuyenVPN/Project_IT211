package com.example.project.aspect;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuditRequestResponseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // create wrappers with a reasonable cache limit
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 10240);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        // store wrappers as request attributes so AuditAspect/RequestUtils can access them
        wrappedRequest.setAttribute("CACHED_REQUEST", wrappedRequest);
        wrappedRequest.setAttribute("CACHED_RESPONSE", wrappedResponse);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // copy response body back to the original response
            wrappedResponse.copyBodyToResponse();
        }
    }
}


