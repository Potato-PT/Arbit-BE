package com.arbit.app.event.security;

import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.event.service.EventListSortPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class EventListSortValidationFilter extends OncePerRequestFilter {

    private static final String EVENT_LIST_PATH = "/api/events";

    private final ObjectMapper objectMapper;

    public EventListSortValidationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.GET.matches(request.getMethod())
                || !EVENT_LIST_PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            EventListSortPolicy.normalize(request.getParameterValues("sort"));
            filterChain.doFilter(request, response);
        } catch (BusinessException exception) {
            response.setStatus(exception.getErrorCode().status().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                    ApiResponse.error(exception.getErrorCode().name(), exception.getMessage()));
        }
    }
}
