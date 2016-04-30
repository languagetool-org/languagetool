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

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.gui.Configuration;
import org.languagetool.language.LanguageIdentifier;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.tools.RuleMatchAsXmlSerializer;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import static org.languagetool.tools.StringTools.escapeForXmlContent;

class LanguageToolHttpHandler implements HttpHandler {

  private static final String CONTENT_TYPE_VALUE = "text/xml; charset=UTF-8";
  private static final String ENCODING = "utf-8";
  private static final int CONTEXT_SIZE = 40; // characters

  private static int handleCount = 0;

  private final Set<String> allowedIps;  
  private final boolean verbose;
  private final boolean internalServer;
  private final RequestLimiter requestLimiter;
  private final LinkedBlockingQueue<Runnable> workQueue;
  private final ExecutorService executorService;
  private final LanguageIdentifier identifier;

  private long maxCheckTimeMillis = -1;
  private int maxTextLength = Integer.MAX_VALUE;
  private String allowOriginUrl;
  private boolean afterTheDeadlineMode;
  private Language afterTheDeadlineLanguage;
  private File languageModelDir;
  private int maxWorkQueueSize;
  private boolean trustXForwardForHeader = false;
  private Set<String> ownIps;
  private File rulesConfigurationFile = null;
  
  /**
   * Create an instance. Call {@link #shutdown()} when done.
   * @param verbose print the input text in case of exceptions
   * @param allowedIps set of IPs that may connect or <tt>null</tt> to allow any IP
   * @param requestLimiter may be null
   */
  LanguageToolHttpHandler(boolean verbose, Set<String> allowedIps, boolean internal, RequestLimiter requestLimiter, LinkedBlockingQueue<Runnable> workQueue) {
    this.verbose = verbose;
    this.allowedIps = allowedIps;
    this.internalServer = internal;
    this.requestLimiter = requestLimiter;
    this.workQueue = workQueue;
    this.executorService = Executors.newCachedThreadPool();
    this.identifier = new LanguageIdentifier();
  }

  /** @since 2.6 */
  void shutdown() {
    executorService.shutdownNow();
  }

  void setMaxTextLength(int maxTextLength) {
    this.maxTextLength = maxTextLength;
  }

  /**
   * Maximum time allowed per check in milliseconds. If the checking takes longer, it will stop with
   * an exception. Use {@code -1} for no limit.
   * @since 2.6
   */
  void setMaxCheckTimeMillis(long maxCheckTimeMillis) {
    this.maxCheckTimeMillis = maxCheckTimeMillis;
  }

  /**
   * Set to {@code true} if this is running behind a (reverse) proxy which
   * sets the {@code X-forwarded-for} HTTP header. The last IP address (but not local IP addresses)
   * in that header will then be used for enforcing a request limitation.
   * @since 2.8
   */
  void setTrustXForwardForHeader(boolean trustXForwardForHeader) {
    this.trustXForwardForHeader = trustXForwardForHeader;
    if (trustXForwardForHeader) {
      this.ownIps = getServersOwnIps();
    }
  }

  /**
   * Value to set as the "Access-Control-Allow-Origin" http header. Use {@code null}
   * to not return that header at all. Use {@code *} to run a server that any other web site
   * can use from Javascript/Ajax (search Cross-origin resource sharing (CORS) for details).
   */
  void setAllowOriginUrl(String allowOriginUrl) {
    this.allowOriginUrl = allowOriginUrl;
  }

  /** @since 2.7 */
  void setAfterTheDeadlineMode(Language defaultLanguage) {
    System.out.println("Running in After the Deadline mode, default language: " + defaultLanguage);
    this.afterTheDeadlineMode = true;
    this.afterTheDeadlineLanguage = defaultLanguage;
  }

  /** @since 2.7 */
  void setLanguageModel(File languageModelDir) {
    this.languageModelDir = languageModelDir;
  }

  /**
   * @param size maximum queue size - if the queue is larger, the user will get an error. Use {@code 0} for no limit.
   * @since 2.9
   */
  void setMaxWorkQueueSize(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Max queue size must be >= 0: " + size);
    }
    this.maxWorkQueueSize = size;
  }

  /**
   * @since 3.0
   */
  void setRulesConfigurationFile(File configFile) {
    this.rulesConfigurationFile = configFile;
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
      if (maxWorkQueueSize != 0 && workQueue.size() > maxWorkQueueSize) {
        String response = "Error: There are currently too many parallel requests. Please try again later.";
        print(response + " Queue size: " + workQueue.size() + ", maximum size: " + maxWorkQueueSize);
        sendError(httpExchange, HttpURLConnection.HTTP_UNAVAILABLE, "Error: " + response);
        return;
      }
      if (allowedIps == null || allowedIps.contains(origAddress)) {
        if (requestedUri.getRawPath().endsWith("/Languages")) {
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
            text = parameters.get("text");
            if (text == null) {
              throw new IllegalArgumentException("Missing 'text' parameter");
            }
          }
          checkText(text, httpExchange, parameters);
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
        response = "Checking took longer than " + maxCheckTimeMillis/1000 + " seconds, which is this server's limit. " +
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
    if (verbose && text != null) {
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
    if (trustXForwardForHeader) {
      List<String> forwardedIpsStr = httpExchange.getRequestHeaders().get("X-forwarded-for");
      if (forwardedIpsStr != null) {
        String allForwardedIpsStr = StringUtils.join(forwardedIpsStr, ", ");
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
      setAllowOrigin(httpExchange);
      httpExchange.sendResponseHeaders(httpReturnCode, response.getBytes(ENCODING).length);
      httpExchange.getResponseBody().write(response.getBytes(ENCODING));
    }
  }

  private Map<String, String> getRequestQuery(HttpExchange httpExchange, URI requestedUri) throws IOException {
    String query;
    if ("post".equalsIgnoreCase(httpExchange.getRequestMethod())) {
      try (InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), ENCODING)) {
        query = readerToString(isr, maxTextLength);
      }
    } else {
      query = requestedUri.getRawQuery();
    }
    return parseQuery(query);
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
    setCommonHeaders(httpExchange);
    String response = getSupportedLanguagesAsXML();
    httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes(ENCODING).length);
    httpExchange.getResponseBody().write(response.getBytes(ENCODING));
  }

  private void setCommonHeaders(HttpExchange httpExchange) {
    httpExchange.getResponseHeaders().set("Content-Type", CONTENT_TYPE_VALUE);
    setAllowOrigin(httpExchange);
  }

  private void setAllowOrigin(HttpExchange httpExchange) {
    if (allowOriginUrl != null) {
      httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", allowOriginUrl);
    }
  }

  private Language detectLanguageOfString(String text, String fallbackLanguage) {
    Language lang = identifier.detectLanguage(text);
    if (lang == null) {
      lang = Languages.getLanguageForShortName(fallbackLanguage != null ? fallbackLanguage : "en");
    }
    if (lang.getDefaultLanguageVariant() != null) {
      lang = lang.getDefaultLanguageVariant();
    }
    return lang;
  }

  private void checkText(String text, HttpExchange httpExchange, Map<String, String> parameters) throws Exception {
    long timeStart = System.currentTimeMillis();
    if (text.length() > maxTextLength) {
      throw new TextTooLongException("Your text exceeds this server's limit of " + maxTextLength +
              " characters (it's " + text.length() + " characters). Please submit a shorter text.");
    }
    //print("Check start: " + text.length() + " chars, " + langParam);
    boolean autoDetectLanguage = getLanguageAutoDetect(parameters);
    Language lang = getLanguage(text, parameters.get("language"), autoDetectLanguage);
    String motherTongueParam = parameters.get("motherTongue");
    Language motherTongue = motherTongueParam != null ? Languages.getLanguageForShortName(motherTongueParam) : null;
    boolean useEnabledOnly = "yes".equals(parameters.get("enabledOnly"));
    String enabledParam = parameters.get("enabled");
    List<String> enabledRules = new ArrayList<>();
    if (enabledParam != null) {
      enabledRules.addAll(Arrays.asList(enabledParam.split(",")));
    }

    List<String> disabledRules = getCommaSeparatedStrings("disabled", parameters);
    List<CategoryId> enabledCategories = getCategoryIds("enabledCategories", parameters);
    List<CategoryId> disabledCategories = getCategoryIds("disabledCategories", parameters);

    if ((disabledRules.size() > 0 || disabledCategories.size() > 0) && useEnabledOnly) {
      throw new IllegalArgumentException("You cannot specify disabled rules or categories using enabledOnly=yes");
    }
    
    boolean useQuerySettings = enabledRules.size() > 0 || disabledRules.size() > 0 ||
                                     enabledCategories.size() > 0 || disabledCategories.size() > 0;
    QueryParams params = new QueryParams(enabledRules, disabledRules, enabledCategories, disabledCategories, useEnabledOnly, useQuerySettings);
    
    Future<List<RuleMatch>> future = executorService.submit(new Callable<List<RuleMatch>>() {
      @Override
      public List<RuleMatch> call() throws Exception {
        // use to fake OOM in thread for testing:
        /*if (Math.random() < 0.1) {
          throw new OutOfMemoryError();
        }*/
        return getRuleMatches(text, parameters, lang, motherTongue, params);
      }
    });
    List<RuleMatch> matches;
    if (maxCheckTimeMillis < 0) {
      matches = future.get();
    } else {
      try {
        matches = future.get(maxCheckTimeMillis, TimeUnit.MILLISECONDS);
      } catch (ExecutionException e) {
        if (e.getCause() != null && e.getCause() instanceof OutOfMemoryError) {
          throw (OutOfMemoryError)e.getCause();
        } else {
          throw e;
        }
      } catch (TimeoutException e) {
        boolean cancelled = future.cancel(true);
        throw new RuntimeException("Text checking took longer than allowed maximum of " + maxCheckTimeMillis +
                " milliseconds (cancelled: " + cancelled + ", handleCount: " + handleCount + ", queue size: " + workQueue.size() +
                ", language: " + lang.getShortNameWithCountryAndVariant() +
                ", " + text.length() + " characters of text)", e);
      }
    }
    
    setCommonHeaders(httpExchange);
    String xmlResponse = getXmlResponse(text, lang, motherTongue, matches);
    String messageSent = "sent";
    String languageMessage = lang.getShortNameWithCountryAndVariant();
    String referrer = httpExchange.getRequestHeaders().getFirst("Referer");
    try {
      httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, xmlResponse.getBytes(ENCODING).length);
      httpExchange.getResponseBody().write(xmlResponse.getBytes(ENCODING));
      if (motherTongue != null) {
        languageMessage += " (mother tongue: " + motherTongue.getShortNameWithCountryAndVariant() + ")";
      }
      if (autoDetectLanguage) {
        languageMessage += "[auto]";
      }
    } catch (IOException exception) {
      // the client is disconnected
      messageSent = "notSent: " + exception.getMessage();
    }
    String agent = parameters.get("useragent") != null ? parameters.get("useragent") : "-";
    print("Check done: " + text.length() + " chars, " + languageMessage + ", " + referrer + ", "
            + "handlers:" + handleCount + ", queue:" + workQueue.size() + ", " + matches.size() + " matches, "
            + (System.currentTimeMillis() - timeStart) + "ms, agent:" + agent
            + ", " + messageSent);
  }

  @NotNull
  private List<String> getCommaSeparatedStrings(String paramName, Map<String, String> parameters) {
    String disabledParam = parameters.get(paramName);
    List<String> result = new ArrayList<>();
    if (disabledParam != null) {
      result.addAll(Arrays.asList(disabledParam.split(",")));
    }
    return result;
  }

  @NotNull
  private List<CategoryId> getCategoryIds(String paramName, Map<String, String> parameters) {
    List<String> stringIds = getCommaSeparatedStrings(paramName, parameters);
    List<CategoryId> ids = new ArrayList<>();
    for (String stringId : stringIds) {
      ids.add(new CategoryId(stringId));
    }
    return ids;
  }

  private boolean getLanguageAutoDetect(Map<String, String> parameters) {
    if (afterTheDeadlineMode) {
      return "true".equals(parameters.get("guess"));
    } else {
      boolean autoDetect = "1".equals(parameters.get("autodetect")) || "yes".equals(parameters.get("autodetect"));
      if (parameters.get("language") == null && !autoDetect) {
        throw new IllegalArgumentException("Missing 'language' parameter. Specify language or use 'autodetect=yes' for auto-detecting the language of the input text.");
      }
      return autoDetect;
    }
  }

  private Language getLanguage(String text, String langParam, boolean autoDetect) {
    Language lang;
    if (autoDetect) {
      lang = detectLanguageOfString(text, langParam);
    } else {
      if (afterTheDeadlineMode) {
        lang = afterTheDeadlineLanguage;
      } else {
        lang = Languages.getLanguageForShortName(langParam);
      }
    }
    return lang;
  }

  private List<RuleMatch> getRuleMatches(String text, Map<String, String> parameters, Language lang,
                                         Language motherTongue, QueryParams params) throws Exception {
    String sourceText = parameters.get("srctext");
    if (sourceText == null) {
      JLanguageTool lt = getLanguageToolInstance(lang, motherTongue, params);
      return lt.check(text);
    } else {
      if (parameters.get("motherTongue") == null) {
        throw new IllegalArgumentException("Missing 'motherTongue' parameter for bilingual checks");
      }
      print("Checking bilingual text, with source length " + sourceText.length() +
          " and target length " + text.length() + " (characters), source language " +
          motherTongue + " and target language " + lang.getShortNameWithCountryAndVariant());
      JLanguageTool sourceLt = getLanguageToolInstance(motherTongue, null, params);
      JLanguageTool targetLt = getLanguageToolInstance(lang, null, params);
      List<BitextRule> bRules = Tools.selectBitextRules(Tools.getBitextRules(motherTongue, lang),
          params.disabledRules, params.enabledRules, params.useEnabledOnly);
      return Tools.checkBitext(sourceText, text, sourceLt, targetLt, bRules);
    }
  }

  private String getXmlResponse(String text, Language lang, Language motherTongue, List<RuleMatch> matches) {
    if (afterTheDeadlineMode) {
      AtDXmlSerializer serializer = new AtDXmlSerializer();
      return serializer.ruleMatchesToXml(matches, text);
    } else {
      RuleMatchAsXmlSerializer serializer = new RuleMatchAsXmlSerializer();
      return serializer.ruleMatchesToXml(matches, text, CONTEXT_SIZE, lang, motherTongue);
    }
  }

  private Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
    Map<String, String> parameters = new HashMap<>();
    if (query != null) {
      String[] pairs = query.split("[&]");
      Map<String, String> parameterMap = getParameterMap(pairs);
      parameters.putAll(parameterMap);
    }
    return parameters;
  }

  private Map<String, String> getParameterMap(String[] pairs) throws UnsupportedEncodingException {
    Map<String, String> parameters = new HashMap<>();
    for (String pair : pairs) {
      int delimPos = pair.indexOf('=');
      if (delimPos != -1) {
        String param = pair.substring(0, delimPos);
        String key = URLDecoder.decode(param, ENCODING);
        String value = URLDecoder.decode(pair.substring(delimPos + 1), ENCODING);
        parameters.put(key, value);
      }
    }
    return parameters;
  }

  private static void print(String s) {
    print(s, System.out);
  }

  private static void print(String s, PrintStream outputStream) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String now = dateFormat.format(new Date());
    outputStream.println(now + " " + s);
  }

  /**
   * Create a JLanguageTool instance for a specific language, mother tongue, and rule configuration.
   *
   * @param lang the language to be used
   * @param motherTongue the user's mother tongue or {@code null}
   */
  private JLanguageTool getLanguageToolInstance(Language lang, Language motherTongue, QueryParams params) throws Exception {
    JLanguageTool lt = new JLanguageTool(lang, motherTongue);
    if (languageModelDir != null) {
      lt.activateLanguageModelRules(languageModelDir);
    }
    if (params.useQuerySettings) {
      Tools.selectRules(lt, new HashSet<>(params.disabledCategories), new HashSet<>(params.enabledCategories),
                        new HashSet<>(params.disabledRules), new HashSet<>(params.enabledRules), params.useEnabledOnly);
    } else {
      if (rulesConfigurationFile != null) {
        configureFromRulesFile(lt, lang);
      } else {
        configureFromGUI(lt, lang);
      }
    }
    return lt;
  }

  private void configureFromRulesFile(JLanguageTool langTool, Language lang) throws IOException {
    print("Using options configured in " + rulesConfigurationFile);
    // If we are explicitly configuring from rules, ignore the useGUIConfig flag
    org.languagetool.gui.Tools.configureFromRules(langTool, new Configuration(rulesConfigurationFile.getParentFile(),
                       rulesConfigurationFile.getName(), lang));
  }

  private void configureFromGUI(JLanguageTool langTool, Language lang) throws IOException {
    Configuration config = new Configuration(lang);
    if (internalServer && config.getUseGUIConfig()) {
      print("Using options configured in the GUI");
      org.languagetool.gui.Tools.configureFromRules(langTool, config);
    }
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
    StringBuilder xmlBuffer = new StringBuilder("<?xml version='1.0' encoding='" + ENCODING + "'?>\n<languages>\n");
    for (Language lang : languages) {
      xmlBuffer.append(String.format("\t<language name=\"%s\" abbr=\"%s\" abbrWithVariant=\"%s\"/> \n", lang.getName(),
              lang.getShortName(), lang.getShortNameWithCountryAndVariant()));
    }
    xmlBuffer.append("</languages>\n");
    return xmlBuffer.toString();
  }

  private static class QueryParams {
    final List<String> enabledRules;
    final List<String> disabledRules;
    final List<CategoryId> enabledCategories;
    final List<CategoryId> disabledCategories;
    final boolean useEnabledOnly;
    final boolean useQuerySettings;

    QueryParams(List<String> enabledRules, List<String> disabledRules, List<CategoryId> enabledCategories, List<CategoryId> disabledCategories,
                boolean useEnabledOnly, boolean useQuerySettings) {
      this.enabledRules = enabledRules;
      this.disabledRules = disabledRules;
      this.enabledCategories = enabledCategories;
      this.disabledCategories = disabledCategories;
      this.useEnabledOnly = useEnabledOnly;
      this.useQuerySettings = useQuerySettings;
    }
  }

}
