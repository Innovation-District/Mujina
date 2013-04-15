package nl.surfnet.mujina.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;

public class ProxyHttpServletRequestWrapper extends HttpServletRequestWrapper {

  private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
  private static final String X_FORWARDED_HOST = "X-Forwarded-Host";
  private static final String X_FORWARDED_PORT = "X-Forwarded-Port";
  private static final int DEFAULT_HTTP_PORT = 80;
  private static final int DEFAULT_HTTPS_PORT = 443;

  /**
   * Constructs a request object wrapping the given request.
   *
   * @throws IllegalArgumentException
   *          if the request is null
   */
  public ProxyHttpServletRequestWrapper(HttpServletRequest request) {
    super(request);
  }

  @Override
  public String getServerName() {
    final String forwardedHost = this.getHeader(X_FORWARDED_HOST);
    if (StringUtils.isNotBlank(forwardedHost)) {
      return forwardedHost;
    }
    return super.getServerName();
  }

  @Override
  public int getServerPort() {
    final String portHeader = this.getHeader(X_FORWARDED_PORT);

    if (portHeader == null) {
      return super.getServerPort();
    }

    final Integer forwardedPort = Integer.parseInt(portHeader);
    if (forwardedPort != null && forwardedPort != 0) {
      return forwardedPort;
    }
    return super.getServerPort();
  }

  @Override
  public String getProtocol() {
    final String forwardedProto = this.getHeader(X_FORWARDED_PROTO);
    if (StringUtils.isNotBlank(forwardedProto)) {
      return forwardedProto;
    }
    if (isSecure()) {
      return "https";
    }

    return "http";
  }

  @Override
  public StringBuffer getRequestURL() {
    final StringBuffer sb = new StringBuffer();

    sb.append(getProtocol());
    sb.append("://");
    sb.append(getServerName());

    final int serverPort = getServerPort();
    if (serverPort != DEFAULT_HTTP_PORT && serverPort != DEFAULT_HTTPS_PORT) {
      sb.append(':');
      sb.append(serverPort);
    }
    sb.append(getRequestURI());

    return sb;
  }
}
