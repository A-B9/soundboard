package com.soundboard.soundboard.integration.rateLimiter;

import com.soundboard.soundboard.integration.BaseIntegrationTest;
import com.soundboard.soundboard.security.filter.LoginRateLimitFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

// random port will start the full container, so RemoteIpCalue runs
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "server.forward-headers-strategy=native",
        "server.tomcat.remoteip.internal-proxies=198\\.51\\.100\\.7",
        "app.rate-limit.login.capacity=2",
        "app.rate-limit.login.refill-tokens=2",
        "app.rate-limit.login.refill-period-seconds=60"
})
@AutoConfigureTestRestTemplate
public class RateLimitTest extends BaseIntegrationTest {
  
  // ssend genuine http with whatever headers needed.
  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private LoginRateLimitFilter  loginRateLimitFilter;
  
  private static final String X_FORWARDED_FOR = "x-forwarded-for";
  private static final String REQ_BODY = "{\"username\":\"x\",\"password\":\"y\"}";
  private static final String ENDPOINT = "/api/soundboard/user/login";
  
  @BeforeEach
  void reset() {loginRateLimitFilter.clearBuckets();}
  
  @Test
  void fakeHeaderFromUntrustedClientCannotBypassLimit() {
    int blocked = 0;
    for (int i = 0; i < 5; i++) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.add(X_FORWARDED_FOR, i +"." + i +"." + i + "." + i);
      
      HttpEntity<String> body =
              new HttpEntity<>(REQ_BODY, headers);
      
      ResponseEntity<String> response = restTemplate.postForEntity(
              ENDPOINT,
              body,
              String.class);
      
      if (response.getStatusCode().value() == 429) {
        blocked++;
      }
      
    }
    assertEquals(3, blocked);
  }
}
