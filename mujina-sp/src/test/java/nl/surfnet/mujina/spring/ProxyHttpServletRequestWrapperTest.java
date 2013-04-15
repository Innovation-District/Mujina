package nl.surfnet.mujina.spring;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class ProxyHttpServletRequestWrapperTest {

  @Test
  public void testProxyRequest() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest("get", "/saml");
    request.addHeader("X-Forwarded-Host", "www.example.org");
    request.addHeader("X-Forwarded-Port", "443");
    request.addHeader("X-Forwarded-Proto", "https");

    final ProxyHttpServletRequestWrapper requestWrapper = new ProxyHttpServletRequestWrapper(request);

    assertEquals("https://www.example.org/saml", requestWrapper.getRequestURL().toString());
  }
}
