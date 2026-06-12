package com.soundboard.soundboard.unit.security;

import com.soundboard.soundboard.config.LoginRateLimitProperties;
import com.soundboard.soundboard.security.filter.LoginRateLimitFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testcontainers.shaded.org.bouncycastle.asn1.cmp.Challenge;

import java.util.Random;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    
    @Test
    void testXForwardedForHeaderIsNotRead() throws Exception {
        // mock the security filter chain, this is the spy.
        FilterChain chain = mock(FilterChain.class);
        
        // mock the attacker request with a fake ip set in the header
        MockHttpServletRequest req1 = loginRequest("10.0.0.1");
        req1.addHeader("X-Forwarded-For", "203.0.113.1");
        
        // send the request, filter should ignore the header ip and only read remote addr ip.
        filter.doFilter(req1, new MockHttpServletResponse(), chain);
        
        // same for second request
        MockHttpServletRequest req2 = loginRequest("10.0.0.1");
        req2.addHeader("X-Forwarded-For", "302.0.311.2");
        filter.doFilter(req2, new MockHttpServletResponse(), chain);
        
        // same for third request but we create a response object to read the expected status code.
        MockHttpServletRequest req3 = loginRequest("10.0.0.1");
        req3.addHeader("X-Forwarded-For", "302.1.311.999");
        MockHttpServletResponse thirdRes = new MockHttpServletResponse();
        filter.doFilter(req3, thirdRes, chain);
        
        // capacity for rate limiter set to 2 so 3rd request is rejected.
        assertThat(thirdRes.getStatus()).isEqualTo(429);
        verify(chain, times(2)).doFilter(any(), any());
    }
    
    @Test
    void testFakeIpsDontCreateNewBuckets() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        
        MockHttpServletRequest req = loginRequest("10.0.0.1");
        int blocked = 0;
        for  (int i = 0; i < 5; i++) {
            req.addHeader("X-Forwarded-For", randomIp());
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
            if (res.getStatus() == 429) blocked++;
        }
        
        verify(chain, times(CAPACITY_2.capacity())).doFilter(any(), any());
        assertThat(blocked).isEqualTo(3);
    }
    
    
    @Test
    void testFilter() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        
        for  (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = loginRequest("10.0.0.1");
            req.addHeader("X-Forwarded-For", randomIp() + ", 10.0.0.1");
            filter.doFilter(req, new MockHttpServletResponse(), chain);
        }
        
        MockHttpServletRequest reqOverLimit = loginRequest("10.0.0.1");
        reqOverLimit.addHeader("X-Forwarded-For", randomIp() + ", 10.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(reqOverLimit, res, chain);
        
        verify(chain, times(2)).doFilter(any(), any());
        assertThat(res.getStatus()).isEqualTo(429);
        
        
    }
    

    private static MockHttpServletRequest loginRequest(String remoteAddr) {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", LOGIN_PATH);
        req.setRemoteAddr(remoteAddr);
        return req;
    }
    
    private static String randomIp() {
        RandomGenerator random = new Random();
        return String.format("%d.%d.%d.%d",
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
        );
    }
}
