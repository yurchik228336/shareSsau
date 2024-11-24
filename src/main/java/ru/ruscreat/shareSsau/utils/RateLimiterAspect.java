package ru.ruscreat.shareSsau.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
public class RateLimiterAspect {

    private final RateLimiter rateLimiter;

    @Autowired
    public RateLimiterAspect(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restControllerMethods() {}

    @Before("restControllerMethods()")
    public void checkRateLimit(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (!rateLimiter.isAllowed(ip)) {
            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS, 
                "Rate limit exceeded. Maximum " + 10 + " requests per second allowed."
            );
        }
    }
}
