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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.tools.StringTools;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import static org.languagetool.server.ServerTools.print;
import static org.languagetool.tools.StringTools.escapeForXmlContent;

class LanguageToolHttpHandler implements HttpHandler {

  private static final String XML_CONTENT_TYPE = "text/xml; charset=UTF-8";
  private static final String ENCODING = "utf-8";

  private static int handleCount = 0;

  private final Set<String> allowedIps;  
  private final RequestLimiter requestLimiter;
  private final LinkedBlockingQueue<Runnable> workQueue;
  private final TextChecker textCheckerV1;
  private final TextChecker textCheckerV2;
  private final HTTPServerConfig config;
  private final boolean afterTheDeadlineMode;
  private final Set<String> ownIps;
  
  LanguageToolHttpHandler(HTTPServerConfig config, Set<String> allowedIps, boolean internal, RequestLimiter requestLimiter, LinkedBlockingQueue<Runnable> workQueue) {
    this.config = config;
    this.allowedIps = allowedIps;
    this.requestLimiter = requestLimiter;
    this.workQueue = workQueue;
    if (config.getTrustXForwardForHeader()) {
      this.ownIps = getServersOwnIps();
    } else {
      this.ownIps = new HashSet<>();
    }
    afterTheDeadlineMode = config.getMode() == HTTPServerConfig.Mode.AfterTheDeadline;
    this.textCheckerV1 = new V1EOLTextChecker(config, internal);
    this.textCheckerV2 = new V2TextChecker(config, internal);
  }

  /** @since 2.6 */
  void shutdown() {
    textCheckerV1.shutdownNow();
  }

  @Override
  public void handle(HttpExchange httpExchange) throws IOException {
    synchronized (this) {
      handleCount++;
    }
    String text = null;
    String remoteAddress = null;
    try {
      URI requestedUri = httpExchange.getRequestURI();
      String origAddress = httpExchange.getRemoteAddress().getAddress().getHostAddress();
      String realAddressOrNull = getRealRemoteAddressOrNull(httpExchange);
      remoteAddress = realAddressOrNull != null ? realAddressOrNull : origAddress;
      // According to the Javadoc, "Closing an exchange without consuming all of the request body is
      // not an error but may make the underlying TCP connection unusable for following exchanges.",
      // so we consume the request now, even before checking for request limits:
      Map<String, String> parameters = getRequestQuery(httpExchange, requestedUri);
      if (requestLimiter != null && !requestLimiter.isAccessOkay(remoteAddress)) {
        String errorMessage = "Error: Access from " + remoteAddress + " denied - too many requests." +
                " Allowed maximum requests: " + requestLimiter.getRequestLimit() +
                " requests per " + requestLimiter.getRequestLimitPeriodInSeconds() + " seconds";
        sendError(httpExchange, HttpURLConnection.HTTP_FORBIDDEN, errorMessage);
        print(errorMessage + " - useragent: " + parameters.get("useragent") +
              " - HTTP UserAgent: " + getHttpUserAgent(httpExchange));
        return;
      }
      if (config.getMaxWorkQueueSize() != 0 && workQueue.size() > config.getMaxWorkQueueSize()) {
        String response = "Error: There are currently too many parallel requests. Please try again later.";
        print(response + " Queue size: " + workQueue.size() + ", maximum size: " + config.getMaxWorkQueueSize());
        sendError(httpExchange, HttpURLConnection.HTTP_UNAVAILABLE, "Error: " + response);
        return;
      }
      if (allowedIps == null || allowedIps.contains(origAddress)) {
        if (requestedUri.getRawPath().startsWith("/v2/")) {
          ApiV2 apiV2 = new ApiV2(textCheckerV2, config.getAllowOriginUrl());
          String pathWithoutVersion = requestedUri.getRawPath().substring("/v2/".length());
          apiV2.handleRequest(pathWithoutVersion, httpExchange, parameters);
        } else if (requestedUri.getRawPath().endsWith("/Languages")) {
          // request type: list known languages
          printListOfLanguages(httpExchange);
        } else {
          // request type: text checking
          if (afterTheDeadlineMode) {
            text = parameters.get("data");
            if (text == null) {
              throw new IllegalArgumentException("Missing 'data' parameter");
            }
            text = text.replaceAll("</p>", "\n\n").replaceAll("<.*?>", "");  // clean up HTML, position changes don't matter for AtD
          } else {
            if (requestedUri.getRawPath().contains("/v2/")) {
              throw new IllegalArgumentException("You have '/v2/' in your path, but not at the root. Try an URL like 'http://server/v2/...' ");
            }
            text = parameters.get("text");
            if (text == null) {
              throw new IllegalArgumentException("Missing 'text' parameter");
            }
          }
          textCheckerV1.checkText(text, httpExchange, parameters);
        }
      } else {
        String errorMessage = "Error: Access from " + StringTools.escapeXML(origAddress) + " denied";
        sendError(httpExchange, HttpURLConnection.HTTP_FORBIDDEN, errorMessage);
        throw new RuntimeException(errorMessage);
      }
    } catch (Exception e) {
      String response;
      int errorCode;
      if (e instanceof TextTooLongException) {
        errorCode = HttpURLConnection.HTTP_ENTITY_TOO_LARGE;
        response = e.getMessage();
      } else if (e instanceof IllegalArgumentException) {
        errorCode = HttpURLConnection.HTTP_BAD_REQUEST;
        response = e.getMessage();
      } else if (e.getCause() != null && e.getCause() instanceof TimeoutException) {
        errorCode = HttpURLConnection.HTTP_UNAVAILABLE;
        response = "Checking took longer than " + config.getMaxCheckTimeMillis()/1000 + " seconds, which is this server's limit. " +
                   "Please make sure you have selected the proper language or consider submitting a shorter text.";
      } else {
        response = "Internal Error. Please contact the site administrator.";
        errorCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
      }
      logError(text, remoteAddress, e, errorCode, httpExchange);
      sendError(httpExchange, errorCode, "Error: " + response);
    } finally {
      synchronized (this) {
        handleCount--;
      }
      httpExchange.close();
    }
  }

  private void logError(String text, String remoteAddress, Exception e, int errorCode, HttpExchange httpExchange) {
    String message = "An error has occurred, sending HTTP code " + errorCode + ". ";
    if (text != null && remoteAddress != null) {
      message += "Access from " + remoteAddress + ", text length " + text.length() + ". ";
    }
    message += "HTTP user agent: " + getHttpUserAgent(httpExchange) + ", ";
    message += "Stacktrace follows:";
    print(message, System.err);
    if (config.isVerbose() && text != null) {
      print("Exception was caused by this text (" + text.length() + " chars, showing up to 500):\n" +
              StringUtils.abbreviate(text, 500), System.err);
    }
    //noinspection CallToPrintStackTrace
    e.printStackTrace();
  }

  private String getHttpUserAgent(HttpExchange httpExchange) {
    return httpExchange.getRequestHeaders().getFirst("User-Agent");
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
      throw new RuntimeException("Could not get the servers own IP addresses", e1);
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
    if (afterTheDeadlineMode) {
      String xmlResponse = "<results><message>" + escapeForXmlContent(response) + "</message></results>";
      httpExchange.sendResponseHeaders(httpReturnCode, xmlResponse.getBytes(ENCODING).length);
      httpExchange.getResponseBody().write(xmlResponse.getBytes(ENCODING));
    } else {
      ServerTools.setAllowOrigin(httpExchange, config.getAllowOriginUrl());
      httpExchange.sendResponseHeaders(httpReturnCode, response.getBytes(ENCODING).length);
      httpExchange.getResponseBody().write(response.getBytes(ENCODING));
    }
  }

  private Map<String, String> getRequestQuery(HttpExchange httpExchange, URI requestedUri) throws IOException {
    String query;
    if ("post".equalsIgnoreCase(httpExchange.getRequestMethod())) {
      try (InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), ENCODING)) {
        query = readerToString(isr, config.getMaxTextLength());
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
      int generousMaxLength = maxTextLength * 2;
      if (generousMaxLength < 0) {  // might happen as it can overflow
        generousMaxLength = Integer.MAX_VALUE;
      }
      if (sb.length() > 0 && sb.length() > generousMaxLength) {
        // don't stop at maxTextLength as that's the text length, but here also other parameters
        // are included (still we need this check here so we don't OOM if someone posts a few hundred MB)...
        throw new TextTooLongException("Your text exceeds this server's limit of " + maxTextLength + " characters.");
      }
      sb.append(new String(chars, 0, readBytes));
    }
    return sb.toString();
  }

  private void printListOfLanguages(HttpExchange httpExchange) throws IOException {
    ServerTools.setCommonHeaders(httpExchange, XML_CONTENT_TYPE, config.getAllowOriginUrl());
    String response = getSupportedLanguagesAsXML();
    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes(ENCODING).length);
    httpExchange.getResponseBody().write(response.getBytes(ENCODING));
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

  /**
   * Construct an XML string containing all supported languages. <br/>The XML format looks like this:<br/><br/>
   * &lt;languages&gt;<br/>
   *    &nbsp;&nbsp;&lt;language name="Catalan" abbr="ca" abbrWithVariant="ca-ES"/&gt;<br/>
   *    &nbsp;&nbsp;&lt;language name="German" abbr="de" abbrWithVariant="de"/&gt;<br/>
   *    &nbsp;&nbsp;&lt;language name="German (Germany)" abbr="de" abbrWithVariant="de-DE"/&gt;<br/>
   *  &lt;/languages&gt;<br/><br/>
   *  The languages are sorted alphabetically by their name.
   * @return an XML document listing all supported languages
   */
  public static String getSupportedLanguagesAsXML() {
    List<Language> languages = new ArrayList<>(Languages.get());
    Collections.sort(languages, (o1, o2) -> o1.getName().compareTo(o2.getName()));
    StringBuilder xmlBuffer = new StringBuilder("<?xml version='1.0' encoding='" + ENCODING + "'?>\n");
    xmlBuffer.append(V1TextChecker.getDeprecationWarning());
    xmlBuffer.append("\n<languages>\n");
    for (Language lang : languages) {
      xmlBuffer.append(String.format("\t<language name=\"%s\" abbr=\"%s\" abbrWithVariant=\"%s\"/> \n", lang.getName(),
              lang.getShortCode(), lang.getShortCodeWithCountryAndVariant()));
    }
    xmlBuffer.append("</languages>\n");
    return xmlBuffer.toString();
  }

}
