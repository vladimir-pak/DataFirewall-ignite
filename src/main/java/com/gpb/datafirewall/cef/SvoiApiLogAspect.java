package com.gpb.datafirewall.cef;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class SvoiApiLogAspect {

    private final SvoiLogger svoiCustomLogger;

    @Before("@annotation(svoiApiLog)")
    public void logApiRequest(JoinPoint joinPoint, SvoiApiLog svoiApiLog) {
        String endpoint = resolveEndpoint();
        String functionName = svoiApiLog.functionName();
        String username = svoiApiLog.username();

        svoiCustomLogger.sendApiRequest(
                endpoint,
                functionName,
                username
        );
    }

    private String resolveEndpoint() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return "UNKNOWN_ENDPOINT";
        }

        HttpServletRequest request = attributes.getRequest();

        return request.getMethod() + " " + request.getRequestURI();
    }
}