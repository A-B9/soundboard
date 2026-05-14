package com.soundboard.soundboard.unit.security;

import com.soundboard.soundboard.config.LoginRateLimitProperties;
import com.soundboard.soundboard.security.filter.LoginRateLimitFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TestLoginRateLimitFilter {

    private static final String LOGIN_PATH = "/api/soundboard/user/login";
    private static final LoginRateLimitProperties CAPACITY_2 =
            new LoginRateLimitProperties(2, 2, 60L);

    private LoginRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new LoginRateLimitFilter(CAPACITY_2);
    }

    @Test
    void allowsRequests_withinCapacity() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = loginRequest("192.168.1.1");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
            assertThat(res.getStatus()).isEqualTo(200);
        }

        verify(chain, times(2)).doFilter(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void returns429_whenCapacityExceeded() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 2; i++) {
            filter.doFilter(loginRequest("10.0.0.1"), new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse over = new MockHttpServletResponse();
        filter.doFilter(loginRequest("10.0.0.1"), over, chain);

        assertThat(over.getStatus()).isEqualTo(429);
        assertThat(over.getContentAsString()).contains("Too many login attempts");
        verify(chain, times(2)).doFilter(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void nonLoginRequests_passThrough_withoutConsumingTokens() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/soundboard/sounds");
        req.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(200);
        verify(chain).doFilter(req, res);
    }

    @Test
    void differentIps_haveSeparateBuckets() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 2; i++) {
            filter.doFilter(loginRequest("1.1.1.1"), new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse diffIpRes = new MockHttpServletResponse();
        filter.doFilter(loginRequest("2.2.2.2"), diffIpRes, chain);

        assertThat(diffIpRes.getStatus()).isEqualTo(200);
        verify(chain, times(3)).doFilter(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void xForwardedFor_usedAsClientIp() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = loginRequest("127.0.0.1");
            req.addHeader("X-Forwarded-For", "203.0.113.1, 10.0.0.1");
            filter.doFilter(req, new MockHttpServletResponse(), chain);
        }

        MockHttpServletRequest thirdReq = loginRequest("127.0.0.1");
        thirdReq.addHeader("X-Forwarded-For", "203.0.113.1, 10.0.0.1");
        MockHttpServletResponse thirdRes = new MockHttpServletResponse();
        filter.doFilter(thirdReq, thirdRes, chain);

        assertThat(thirdRes.getStatus()).isEqualTo(429);
    }

    @Test
    void clearBuckets_resetsRateLimitState() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 2; i++) {
            filter.doFilter(loginRequest("5.5.5.5"), new MockHttpServletResponse(), chain);
        }

        filter.clearBuckets();

        MockHttpServletResponse afterClear = new MockHttpServletResponse();
        filter.doFilter(loginRequest("5.5.5.5"), afterClear, chain);
        assertThat(afterClear.getStatus()).isEqualTo(200);
    }

    private static MockHttpServletRequest loginRequest(String remoteAddr) {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", LOGIN_PATH);
        req.setRemoteAddr(remoteAddr);
        return req;
    }
}
