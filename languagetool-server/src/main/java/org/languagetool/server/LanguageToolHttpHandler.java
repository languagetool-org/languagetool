/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.ErrorRateTooHighException;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import static org.languagetool.server.ServerTools.print;

class LanguageToolHttpHandler implements HttpHandler {

  private static final String ENCODING = "utf-8";

  private final Set<String> allowedIps;  
  private final RequestLimiter requestLimiter;
  private final ErrorRequestLimiter errorRequestLimiter;
  private final LinkedBlockingQueue<Runnable> workQueue;
  private final TextChecker textCheckerV2;
  private final HTTPServerConfig config;
  private final Set<String> ownIps;
  private final RequestCounter reqCounter = new RequestCounter();
  
  LanguageToolHttpHandler(HTTPServerConfig config, Set<String> allowedIps, boolean internal, RequestLimiter requestLimiter, ErrorRequestLimiter errorLimiter, LinkedBlockingQueue<Runnable> workQueue) {
    this.config = config;
    this.allowedIps = allowedIps;
    this.requestLimiter = requestLimiter;
    this.errorRequestLimiter = errorLimiter;
    this.workQueue = workQueue;
    if (config.getTrustXForwardForHeader()) {
      this.ownIps = getServersOwnIps();
    } else {
      this.ownIps = new HashSet<>();
    }
    this.textCheckerV2 = new V2TextChecker(config, internal, workQueue, reqCounter);
  }

  /** @since 2.6 */
  void shutdown() {
  }

  @Override
  public void handle(HttpExchange httpExchange) throws IOException {
    long startTime = System.currentTimeMillis();
    String remoteAddress = null;
    Map<String, String> parameters = new HashMap<>();
    int reqId = reqCounter.incrementRequestCount();
    boolean printTime = false;
    try {
      URI requestedUri = httpExchange.getRequestURI();
      String origAddress = httpExchange.getRemoteAddress().getAddress().getHostAddress();
      String realAddressOrNull = getRealRemoteAddressOrNull(httpExchange);
      remoteAddress = realAddressOrNull != null ? realAddressOrNull : origAddress;
      reqCounter.incrementHandleCount(remoteAddress, reqId);
      // According to the Javadoc, "Closing an exchange without consuming all of the request body is
      // not an error but may make the underlying TCP connection unusable for following exchanges.",
      // so we consume the request now, even before checking for request limits:
      parameters = getRequestQuery(httpExchange, requestedUri);
      if (requestLimiter != null) {
        try {
          requestLimiter.checkAccess(remoteAddress, parameters);
        } catch (TooManyRequestsException e) {
          String errorMessage = "Error: Access from " + remoteAddress + " denied: " + e.getMessage();
          sendError(httpExchange, HttpURLConnection.HTTP_FORBIDDEN, errorMessage);
          print(errorMessage + " - useragent: " + parameters.get("useragent") +
                  " - HTTP UserAgent: " + getHttpUserAgent(httpExchange) + ", r:" + reqCounter.getRequestCount());
          return;
        }
      }
      if (errorRequestLimiter != null && !errorRequestLimiter.wouldAccessBeOkay(remoteAddress)) {
        String textSizeMessage = getTextOrDataSizeMessage(parameters);
        String errorMessage = "Error: Access from " + remoteAddress + " denied - too many recent timeouts. " +
                textSizeMessage +
                " Allowed maximum timeouts: " + errorRequestLimiter.getRequestLimit() +
                " per " + errorRequestLimiter.getRequestLimitPeriodInSeconds() + " seconds";
        sendError(httpExchange, HttpURLConnection.HTTP_FORBIDDEN, errorMessage);
        print(errorMessage + " - useragent: " + parameters.get("useragent") +
                " - HTTP UserAgent: " + getHttpUserAgent(httpExchange) + ", r:" + reqCounter.getRequestCount());
        return;
      }
      if (config.getMaxWorkQueueSize() != 0 && workQueue.size() > config.getMaxWorkQueueSize()) {
        String response = "Error: There are currently too many parallel requests. Please try again later.";
        print(response + " Queue size: " + workQueue.size() + ", maximum size: " + config.getMaxWorkQueueSize() +
                ", handlers:" + reqCounter.getHandleCount() + ", r:" + reqCounter.getRequestCount());
        sendError(httpExchange, HttpURLConnection.HTTP_UNAVAILABLE, "Error: " + response);
        return;
      }
      if (allowedIps == null || allowedIps.contains(origAddress)) {
        if (requestedUri.getRawPath().startsWith("/v2/")) {
          printTime = true;
          ApiV2 apiV2 = new ApiV2(textCheckerV2, config.getAllowOriginUrl());
          String pathWithoutVersion = requestedUri.getRawPath().substring("/v2/".length());
          apiV2.handleRequest(pathWithoutVersion, httpExchange, parameters, errorRequestLimiter, remoteAddress);
        } else if (requestedUri.getRawPath().endsWith("/Languages")) {
          throw new IllegalArgumentException("You're using an old version of our API that's not supported anymore. Please see https://languagetool.org/http-api/migration.php");
        } else if (requestedUri.getRawPath().equals("/")) {
          throw new IllegalArgumentException("Missing arguments for LanguageTool API. Please see https://languagetool.org/http-api/swagger-ui/#/default");
        } else if (requestedUri.getRawPath().contains("/v2/")) {
          throw new IllegalArgumentException("You have '/v2/' in your path, but not at the root. Try an URL like 'http://server/v2/...' ");
        } else if (requestedUri.getRawPath().equals("/favicon.ico")) {
          sendError(httpExchange, HttpURLConnection.HTTP_NOT_FOUND, "Not found");
        } else {
          throw new IllegalArgumentException("Seems like you're using an old version of our API that's not supported anymore. Please see https://languagetool.org/http-api/migration.php");
        }
      } else {
        String errorMessage = "Error: Access from " + StringTools.escapeXML(origAddress) + " denied";
        sendError(httpExchange, HttpURLConnection.HTTP_FORBIDDEN, errorMessage);
        throw new RuntimeException(errorMessage);
      }
    } catch (Exception e) {
      String response;
      int errorCode;
      boolean textLoggingAllowed = false;
      boolean logStacktrace = true;
      if (e instanceof TextTooLongException) {
        errorCode = HttpURLConnection.HTTP_ENTITY_TOO_LARGE;
        response = e.getMessage();
        logStacktrace = false;
      } else if (ExceptionUtils.getRootCause(e) instanceof ErrorRateTooHighException) {
        errorCode = HttpURLConnection.HTTP_BAD_REQUEST;
        response = ExceptionUtils.getRootCause(e).getMessage();
        logStacktrace = false;
      } else if (e instanceof AuthException || e.getCause() != null && e.getCause() instanceof AuthException) {
        errorCode = HttpURLConnection.HTTP_FORBIDDEN;
        response = e.getMessage();
      } else if (e instanceof IllegalArgumentException) {
        errorCode = HttpURLConnection.HTTP_BAD_REQUEST;
        response = e.getMessage();
      } else if (e.getCause() != null && e.getCause() instanceof TimeoutException) {
        errorCode = HttpURLConnection.HTTP_UNAVAILABLE;
        response = "Checking took longer than " + config.getMaxCheckTimeMillis()/1000.0f + " seconds, which is this server's limit. " +
                   "Please make sure you have selected the proper language or consider submitting a shorter text.";
      } else {
        response = "Internal Error: " + e.getMessage();
        errorCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
        textLoggingAllowed = true;
      }
      logError(remoteAddress, e, errorCode, httpExchange, parameters, textLoggingAllowed, logStacktrace);
      sendError(httpExchange, errorCode, "Error: " + response);
    } finally {
      httpExchange.close();
      reqCounter.decrementHandleCount(reqId);
      if (printTime) {
        ServerTools.print("Total check time: " + (System.currentTimeMillis() - startTime) + "ms, r:" + reqCounter.getRequestCount());
      }
    }
  }

  @NotNull
  private String getTextOrDataSizeMessage(Map<String, String> parameters) {
    String text = parameters.get("text");
    if (text != null) {
      return "Text size: " + text.length() + ".";
    } else {
      String data = parameters.get("data");
      if (data != null) {
        return "Data size: " + data.length() + ".";
      }
    }
    return "";
  }

  private void logError(String remoteAddress, Exception e, int errorCode, HttpExchange httpExchange, Map<String, String> params, 
                        boolean textLoggingAllowed, boolean logStacktrace) {
    String message = "An error has occurred: '" +  e.getMessage() + "', sending HTTP code " + errorCode + ". ";
    message += "Access from " + remoteAddress + ", ";
    message += "HTTP user agent: " + getHttpUserAgent(httpExchange) + ", ";
    message += "User agent param: " + params.get("useragent") + ", ";
    message += "Referrer: " + getHttpReferrer(httpExchange) + ", ";
    message += "language: " + params.get("language") + ", ";
    message += "h: " + reqCounter.getHandleCount() + ", ";
    message += "r: " + reqCounter.getRequestCount() + ", ";
    String text = params.get("text");
    if (text != null) {
      message += "text length: " + text.length() + ", ";
    }
    if (logStacktrace) {
      message += "Stacktrace follows:";
      print(message, System.err);
      //noinspection CallToPrintStackTrace
      e.printStackTrace();
    } else {
      message += "(no stacktrace logged)";
      print(message, System.err);
    }
    if (config.isVerbose() && text != null && textLoggingAllowed) {
      print("Exception was caused by this text (" + text.length() + " chars, showing up to 500):\n" +
              StringUtils.abbreviate(text, 500), System.err);
    }
  }

  @Nullable
  private String getHttpUserAgent(HttpExchange httpExchange) {
    return httpExchange.getRequestHeaders().getFirst("User-Agent");
  }

  @Nullable
  private String getHttpReferrer(HttpExchange httpExchange) {
    return httpExchange.getRequestHeaders().getFirst("Referer");
  }

  // Call only if really needed, seems to be slow on some Windows machines.
  private Set<String> getServersOwnIps() {
    Set<String> ownIps = new HashSet<>();
    try {
      Enumeration e = NetworkInterface.getNetworkInterfaces();
      while (e.hasMoreElements()) {
        NetworkInterface netInterface = (NetworkInterface) e.nextElement();
        Enumeration addresses = netInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          InetAddress address = (InetAddress) addresses.nextElement();
          ownIps.add(address.getHostAddress());
        }
      }
    } catch (SocketException e1) {
      throw new RuntimeException("Could not get the server's own IP addresses", e1);
    }
    return ownIps;
  }

  /**
   * A (reverse) proxy can set the 'X-forwarded-for' header so we can see a user's original IP.
   * But that's just a common header than can also be set by the client. So we can
   * only trust the last item in the list of proxies, as it was set by our proxy,
   * which we can trust.
   */
  @Nullable
  private String getRealRemoteAddressOrNull(HttpExchange httpExchange) {
    if (config.getTrustXForwardForHeader()) {
      List<String> forwardedIpsStr = httpExchange.getRequestHeaders().get("X-forwarded-for");
      if (forwardedIpsStr != null) {
        String allForwardedIpsStr = String.join(", ", forwardedIpsStr);
        List<String> allForwardedIps = Arrays.asList(allForwardedIpsStr.split(", "));
        return getLastIpIgnoringOwn(allForwardedIps);
      }
    }
    return null;
  }

  private String getLastIpIgnoringOwn(List<String> forwardedIps) {
    String lastIp = null;
    for (String ip : forwardedIps) {
      if (ownIps.contains(ip)) {
        // If proxy.php runs on this machine, our own IP will be listed. We want to ignore that
        // because otherwise all requests would seem to be coming from the same address (our own),
        // making the request limiter a bit useless: other users could send tons of requests and
        // stop the service for everybody else.
        continue;
      }
      lastIp = ip;  // use last in the list, we assume we can trust our own proxy (other items can be faked)
    }
    return lastIp;
  }

  private void sendError(HttpExchange httpExchange, int httpReturnCode, String response) throws IOException {
    ServerTools.setAllowOrigin(httpExchange, config.getAllowOriginUrl());
    httpExchange.sendResponseHeaders(httpReturnCode, response.getBytes(ENCODING).length);
    httpExchange.getResponseBody().write(response.getBytes(ENCODING));
  }

  private Map<String, String> getRequestQuery(HttpExchange httpExchange, URI requestedUri) throws IOException {
    String query;
    if ("post".equalsIgnoreCase(httpExchange.getRequestMethod())) {
      try (InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), ENCODING)) {
        query = readerToString(isr, config.getMaxTextHardLength());
      }
    } else {
      query = requestedUri.getRawQuery();
    }
    return parseQuery(query, httpExchange);
  }

  private String readerToString(Reader reader, int maxTextLength) throws IOException {
    StringBuilder sb = new StringBuilder();
    int readBytes = 0;
    char[] chars = new char[4000];
    while (readBytes >= 0) {
      readBytes = reader.read(chars, 0, 4000);
      if (readBytes <= 0) {
        break;
      }
      int generousMaxLength = maxTextLength * 3 + 1000;  // one character can be encoded as e.g. "%D8", plus space for other parameters
      if (generousMaxLength < 0) {  // might happen as it can overflow
        generousMaxLength = Integer.MAX_VALUE;
      }
      if (sb.length() > 0 && sb.length() > generousMaxLength) {
        // don't stop at maxTextLength as that's the text length, but here also other parameters
        // are included (still we need this check here so we don't OOM if someone posts a few hundred MB)...
        throw new TextTooLongException("Your text's length exceeds this server's hard limit of " + maxTextLength + " characters.");
      }
      sb.append(new String(chars, 0, readBytes));
    }
    return sb.toString();
  }


  private Map<String, String> parseQuery(String query, HttpExchange httpExchange) throws UnsupportedEncodingException {
    Map<String, String> parameters = new HashMap<>();
    if (query != null) {
      Map<String, String> parameterMap = getParameterMap(query, httpExchange);
      parameters.putAll(parameterMap);
    }
    return parameters;
  }

  private Map<String, String> getParameterMap(String query, HttpExchange httpExchange) throws UnsupportedEncodingException {
    String[] pairs = query.split("[&]");
    Map<String, String> parameters = new HashMap<>();
    for (String pair : pairs) {
      int delimPos = pair.indexOf('=');
      if (delimPos != -1) {
        String param = pair.substring(0, delimPos);
        String key = URLDecoder.decode(param, ENCODING);
        try {
          String value = URLDecoder.decode(pair.substring(delimPos + 1), ENCODING);
          parameters.put(key, value);
        } catch (IllegalArgumentException e) {
          throw new RuntimeException("Could not decode query. Query length: " + query.length() +
                                     " Request method: " + httpExchange.getRequestMethod(), e);
        }
      }
    }
    return parameters;
  }

}
