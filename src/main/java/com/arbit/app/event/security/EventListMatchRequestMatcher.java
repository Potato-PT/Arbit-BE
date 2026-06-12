package com.arbit.app.event.security;

import com.arbit.app.event.service.EventListSortPolicy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class EventListMatchRequestMatcher implements RequestMatcher {

    private static final String EVENT_LIST_PATH = "/api/events";

    @Override
    public boolean matches(HttpServletRequest request) {
        return HttpMethod.GET.matches(request.getMethod())
                && EVENT_LIST_PATH.equals(request.getServletPath())
                && EventListSortPolicy.requiresAuthentication(request.getParameterValues("sort"));
    }
}
