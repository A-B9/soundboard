package com.soundboard.soundboard.security.filter;

import com.soundboard.soundboard.config.LoginRateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/soundboard/user/login";

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final LoginRateLimitProperties properties;

    public LoginRateLimitFilter(LoginRateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            chain.doFilter(request, response);
            return;
        }

        String ip = extractClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> newBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"Too many login attempts. Please try again later.\"}");
        }
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && LOGIN_PATH.equals(request.getRequestURI());
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.capacity())
                .refillGreedy(properties.refillTokens(), Duration.ofSeconds(properties.refillPeriodSeconds()))
                .initialTokens(properties.capacity())
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    // vulnerable method
    // filter keeps 1 bucket per IP, the IP becomes the key that decides which bucket the request is drawn from.
    // X-Fordwarded-For is an ordinary HTTP request header, and anyone sending the request is able to set this header.
    
    // if i was an attacker trying to brute force a password login, i could change the x-forwarded-for header on each
    // request to get unlimited numebr of request attempts. -> this will bypass teh rate limiter
    
    // the IP a server reads from a header can be forged.
    /*
    ip that a server reads from tcp connection (getRemoteAddr()) cannot be forged. attacker would have to forge the source
    of where their packets originate from.
    
    stop reading the headers and instead move to read from servlet container which can resolve the real client IP using a
    list of proxies that we define as trustworthy sources.
    
    can tell spring boot to tell tomcat to process the header but only when the request has arrived from a proxy ip that
    is whitelisted, after that using the given function getRemoteAddr() returns the correct client ip
     */
    
    // the header is just text the client puts in request, nothing validated at tcp/ip layer will validate it
    
    // this now just needs to get the real client ip from the servlet request, because the request should come from
    // a white listed proxy server which we trust.
    private static String extractClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    public void clearBuckets() {
        buckets.clear();
    }
}
