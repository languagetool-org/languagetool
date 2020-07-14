/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.rules.spelling.morfologik.suggestions_ordering.SuggestionsOrdererConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @since 2.0
 */
public class HTTPServerConfig {

  enum Mode { LanguageTool }

  public static final String DEFAULT_HOST = "localhost";

  /** The default port on which the server is running (8081). */
  public static final int DEFAULT_PORT = 8081;
  
  static final String LANGUAGE_MODEL_OPTION = "--languageModel";
  static final String WORD2VEC_MODEL_OPTION = "--word2vecModel";
  static final String NN_MODEL_OPTION = "--neuralNetworkModel";

  protected boolean verbose = false;
  protected boolean publicAccess = false;
  protected int port = DEFAULT_PORT;
  protected String allowOriginUrl = null;

  protected URI serverURL = null;
  protected int maxTextLength = Integer.MAX_VALUE;
  protected int maxTextHardLength = Integer.MAX_VALUE;
  protected int maxTextLengthWithApiKey = Integer.MAX_VALUE;
  protected String secretTokenKey = null;
  protected long maxCheckTimeMillis = -1;
  protected long maxCheckTimeWithApiKeyMillis = -1;
  protected int maxCheckThreads = 10;
  protected Mode mode;
  protected File languageModelDir = null;
  protected File word2vecModelDir = null;
  protected boolean pipelineCaching = false;
  protected boolean pipelinePrewarming = false;

  protected int maxPipelinePoolSize;
  protected int pipelineExpireTime;
  protected File fasttextModel = null;
  protected File fasttextBinary = null;
  protected File neuralNetworkModelDir = null;
  protected int requestLimit;
  protected int requestLimitInBytes;
  protected int timeoutRequestLimit;
  protected int requestLimitPeriodInSeconds;
  protected int ipFingerprintFactor = 1;
  protected boolean trustXForwardForHeader;
  protected int maxWorkQueueSize;
  protected File rulesConfigFile = null;
  protected File remoteRulesConfigFile = null;
  protected int cacheSize = 0;
  protected long cacheTTLSeconds = 300;
  protected float maxErrorsPerWordRate = 0;
  protected int maxSpellingSuggestions = 0;
  protected List<String> blockedReferrers = new ArrayList<>();
  protected String hiddenMatchesServer;
  protected int hiddenMatchesServerTimeout;
  protected int hiddenMatchesServerFailTimeout;
  protected int hiddenMatchesServerFall;
  protected List<Language> hiddenMatchesLanguages = new ArrayList<>();
  protected String dbDriver = null;
  protected String dbUrl = null;
  protected String dbUsername = null;
  protected String dbPassword = null;
  protected boolean dbLogging;
  protected boolean prometheusMonitoring = false;
  protected int prometheusPort = 9301;
  protected GlobalConfig globalConfig = new GlobalConfig();
  protected List<String> disabledRuleIds = new ArrayList<>();
  protected boolean stoppable = false;

  protected boolean skipLoggingRuleMatches = false;
  protected boolean skipLoggingChecks = false;

  protected int slowRuleLoggingThreshold = -1; // threshold in milliseconds, used by SlowRuleLogger; < 0 - disabled

  protected String abTest = null;
  protected Pattern abTestClients = null;
  protected int abTestRollout = 100; // percentage [0,100]

  private static final List<String> KNOWN_OPTION_KEYS = Arrays.asList("abTest", "abTestClients", "abTestRollout",
    "beolingusFile", "blockedReferrers", "cacheSize", "cacheTTLSeconds",
    "dbDriver", "dbPassword", "dbUrl", "dbUsername", "disabledRuleIds", "fasttextBinary", "fasttextModel", "grammalectePassword",
    "grammalecteServer", "grammalecteUser", "hiddenMatchesLanguages", "hiddenMatchesServer", "hiddenMatchesServerFailTimeout",
    "hiddenMatchesServerTimeout", "hiddenMatchesServerFall", "ipFingerprintFactor", "languageModel", "maxCheckThreads", "maxCheckTimeMillis",
    "maxCheckTimeWithApiKeyMillis", "maxErrorsPerWordRate", "maxPipelinePoolSize", "maxSpellingSuggestions", "maxTextHardLength",
    "maxTextLength", "maxTextLengthWithApiKey", "maxWorkQueueSize", "neuralNetworkModel", "pipelineCaching",
    "pipelineExpireTimeInSeconds", "pipelinePrewarming", "prometheusMonitoring", "prometheusPort", "remoteRulesFile",
    "requestLimit", "requestLimitInBytes", "requestLimitPeriodInSeconds", "rulesFile", "secretTokenKey", "serverURL",
    "skipLoggingChecks", "skipLoggingRuleMatches", "timeoutRequestLimit", "trustXForwardForHeader", "warmUp", "word2vecModel",
    "keystore", "password", "maxTextLengthPremium", "maxTextLengthAnonymous", "maxTextLengthLoggedIn", "gracefulDatabaseFailure",
    "redisPassword", "redisHost", "dbLogging", "premiumOnly");

  /**
   * Create a server configuration for the default port ({@link #DEFAULT_PORT}).
   */
  public HTTPServerConfig() {
    this(DEFAULT_PORT, false);
  }

  /**
   * @param serverPort the port to bind to
   * @since 2.8
   */
  public HTTPServerConfig(int serverPort) {
    this(serverPort, false);
  }

  /**
   * @param serverPort the port to bind to
   * @param verbose when set to <tt>true</tt>, the input text will be logged in case there is an exception
   */
  public HTTPServerConfig(int serverPort, boolean verbose) {
    this.port = serverPort;
    this.verbose = verbose;
  }

  /**
   * Parse command line options.
   */
  HTTPServerConfig(String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].matches("--[a-zA-Z]+=.+")) {
        System.err.println("WARNING: use `--option value`, not `--option=value`, parameters will be ignored otherwise: " + args[i]);
      }
      switch (args[i]) {
        case "--config":
          parseConfigFile(new File(args[++i]), !ArrayUtils.contains(args, LANGUAGE_MODEL_OPTION),
            !ArrayUtils.contains(args, WORD2VEC_MODEL_OPTION), !ArrayUtils.contains(args, NN_MODEL_OPTION));
          break;
        case "-p":
        case "--port":
          port = Integer.parseInt(args[++i]);
          break;
        case "-v":
        case "--verbose":
          verbose = true;
          break;
        case "--public":
          publicAccess = true;
          break;
        case "--allow-origin":
          try {
            allowOriginUrl = args[++i];
            if (allowOriginUrl.startsWith("--")) {
              throw new IllegalArgumentException("Missing argument for '--allow-origin' (e.g. an URL or '*')");
            }
          } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Missing argument for '--allow-origin' (e.g. an URL or '*')");
          }
          break;
        case LANGUAGE_MODEL_OPTION:
          setLanguageModelDirectory(args[++i]);
          break;
        case WORD2VEC_MODEL_OPTION:
          setWord2VecModelDirectory(args[++i]);
          break;
        case NN_MODEL_OPTION:
          setNeuralNetworkModelDir(args[++i]);
          break;
        case "--stoppable":  // internal only, doesn't need to be documented
          stoppable = true;
          break;
        default:
          if (args[i].contains("=")) {
            System.out.println("WARNING: unknown option: " + args[i] +
                    " - please note that parameters are given as '--arg param', i.e. without '=' between argument and parameter");
          } else {
            System.out.println("WARNING: unknown option: " + args[i]);
          }
      }
    }
  }

  private void parseConfigFile(File file, boolean loadLangModel, boolean loadWord2VecModel, boolean loadNeuralNetworkModel) {
    try {
      Properties props = new Properties();
      try (FileInputStream fis = new FileInputStream(file)) {
        props.load(fis);
        maxTextLength = Integer.parseInt(getOptionalProperty(props, "maxTextLength", Integer.toString(Integer.MAX_VALUE)));
        maxTextLengthWithApiKey = Integer.parseInt(getOptionalProperty(props, "maxTextLengthWithApiKey", Integer.toString(Integer.MAX_VALUE)));
        maxTextHardLength = Integer.parseInt(getOptionalProperty(props, "maxTextHardLength", Integer.toString(Integer.MAX_VALUE)));
        secretTokenKey = getOptionalProperty(props, "secretTokenKey", null);
        maxCheckTimeMillis = Long.parseLong(getOptionalProperty(props, "maxCheckTimeMillis", "-1"));
        maxCheckTimeWithApiKeyMillis = Long.parseLong(getOptionalProperty(props, "maxCheckTimeWithApiKeyMillis", "-1"));
        requestLimit = Integer.parseInt(getOptionalProperty(props, "requestLimit", "0"));
        requestLimitInBytes = Integer.parseInt(getOptionalProperty(props, "requestLimitInBytes", "0"));
        timeoutRequestLimit = Integer.parseInt(getOptionalProperty(props, "timeoutRequestLimit", "0"));
        pipelineCaching = Boolean.parseBoolean(getOptionalProperty(props, "pipelineCaching", "false").trim());
        pipelinePrewarming = Boolean.parseBoolean(getOptionalProperty(props, "pipelinePrewarming", "false").trim());
        maxPipelinePoolSize = Integer.parseInt(getOptionalProperty(props, "maxPipelinePoolSize", "5"));
        pipelineExpireTime = Integer.parseInt(getOptionalProperty(props, "pipelineExpireTimeInSeconds", "10"));
        requestLimitPeriodInSeconds = Integer.parseInt(getOptionalProperty(props, "requestLimitPeriodInSeconds", "0"));
        ipFingerprintFactor = Integer.parseInt(getOptionalProperty(props, "ipFingerprintFactor", "1"));
        trustXForwardForHeader = Boolean.valueOf(getOptionalProperty(props, "trustXForwardForHeader", "false").trim());
        maxWorkQueueSize = Integer.parseInt(getOptionalProperty(props, "maxWorkQueueSize", "0"));
        if (maxWorkQueueSize < 0) {
          throw new IllegalArgumentException("maxWorkQueueSize must be >= 0: " + maxWorkQueueSize);
        }
        String url = getOptionalProperty(props, "serverURL", null);
        setServerURL(url);
        String langModel = getOptionalProperty(props, "languageModel", null);
        if (langModel != null && loadLangModel) {
          setLanguageModelDirectory(langModel);
        }
        String word2vecModel = getOptionalProperty(props, "word2vecModel", null);
        if (word2vecModel != null && loadWord2VecModel) {
          setWord2VecModelDirectory(word2vecModel);
        }
        String neuralNetworkModel = getOptionalProperty(props, "neuralNetworkModel", null);
        if (neuralNetworkModel != null && loadNeuralNetworkModel) {
          setNeuralNetworkModelDir(neuralNetworkModel);
        }
        String fasttextModel = getOptionalProperty(props, "fasttextModel", null);
        String fasttextBinary = getOptionalProperty(props, "fasttextBinary", null);
        if (fasttextBinary != null && fasttextModel != null) {
          setFasttextPaths(fasttextModel, fasttextBinary);
        }
        maxCheckThreads = Integer.parseInt(getOptionalProperty(props, "maxCheckThreads", "10"));
        if (maxCheckThreads < 1) {
          throw new IllegalArgumentException("Invalid value for maxCheckThreads, must be >= 1: " + maxCheckThreads);
        }
        boolean atdMode = getOptionalProperty(props, "mode", "LanguageTool").equalsIgnoreCase("AfterTheDeadline");
        if (atdMode) {
          throw new IllegalArgumentException("The AfterTheDeadline mode is not supported anymore in LanguageTool 3.8 or later");
        }
        String rulesConfigFilePath = getOptionalProperty(props, "rulesFile", null);
        if (rulesConfigFilePath != null) {
          rulesConfigFile = new File(rulesConfigFilePath);
          if (!rulesConfigFile.exists() || !rulesConfigFile.isFile()) {
            throw new RuntimeException("Rules Configuration file cannot be found: " + rulesConfigFile);
          }
        }
        String remoteRulesConfigFilePath = getOptionalProperty(props, "remoteRulesFile", null);
        if (remoteRulesConfigFilePath != null) {
          remoteRulesConfigFile = new File(remoteRulesConfigFilePath);
          if (!remoteRulesConfigFile.exists() || !remoteRulesConfigFile.isFile()) {
            throw new RuntimeException("Remote rules configuration file cannot be found: " + remoteRulesConfigFile);
          }
        }
        cacheSize = Integer.parseInt(getOptionalProperty(props, "cacheSize", "0"));
        if (cacheSize < 0) {
          throw new IllegalArgumentException("Invalid value for cacheSize: " + cacheSize + ", use 0 to deactivate cache");
        }
        if (props.containsKey("cacheTTLSeconds") && !props.containsKey("cacheSize")) {
          throw new IllegalArgumentException("Use of cacheTTLSeconds without also setting cacheSize has no effect.");
        }
        cacheTTLSeconds = Integer.parseInt(getOptionalProperty(props, "cacheTTLSeconds", "300"));
        if (props.containsKey("warmUp")) {
          System.err.println("Setting ignored: 'warmUp'. Look into using pipelineCaching and pipelinePrewarming instead.");
        }
        maxErrorsPerWordRate = Float.parseFloat(getOptionalProperty(props, "maxErrorsPerWordRate", "0"));
        maxSpellingSuggestions = Integer.parseInt(getOptionalProperty(props, "maxSpellingSuggestions", "0"));
        blockedReferrers = Arrays.asList(getOptionalProperty(props, "blockedReferrers", "").split(",\\s*"));
        hiddenMatchesServer = getOptionalProperty(props, "hiddenMatchesServer", null);
        hiddenMatchesServerTimeout = Integer.parseInt(getOptionalProperty(props, "hiddenMatchesServerTimeout", "1000"));
        hiddenMatchesServerFailTimeout = Integer.parseInt(getOptionalProperty(props, "hiddenMatchesServerFailTimeout", "10000"));
        hiddenMatchesServerFall = Integer.parseInt(getOptionalProperty(props, "hiddenMatchesServerFall", "1"));
        String langCodes = getOptionalProperty(props, "hiddenMatchesLanguages", "");
        for (String code : langCodes.split(",\\s*")) {
          if (!code.isEmpty()) {
            hiddenMatchesLanguages.add(Languages.getLanguageForShortCode(code));
          }
        }
        dbDriver = getOptionalProperty(props, "dbDriver", null);
        dbUrl = getOptionalProperty(props, "dbUrl", null);
        dbUsername = getOptionalProperty(props, "dbUsername", null);
        dbPassword = getOptionalProperty(props, "dbPassword", null);
        dbLogging = Boolean.valueOf(getOptionalProperty(props, "dbLogging", "false").trim());
        prometheusMonitoring = Boolean.valueOf(getOptionalProperty(props, "prometheusMonitoring", "false").trim());
        prometheusPort = Integer.parseInt(getOptionalProperty(props, "prometheusPort", "9301"));
        skipLoggingRuleMatches = Boolean.valueOf(getOptionalProperty(props, "skipLoggingRuleMatches", "false").trim());
        skipLoggingChecks = Boolean.valueOf(getOptionalProperty(props, "skipLoggingChecks", "false").trim());
        if (dbLogging && (dbDriver == null || dbUrl == null || dbUsername == null || dbPassword == null)) {
          throw new IllegalArgumentException("dbLogging can only be true if dbDriver, dbUrl, dbUsername, and dbPassword are all set");
        }
        slowRuleLoggingThreshold = Integer.valueOf(getOptionalProperty(props, "slowRuleLoggingThreshold", "-1"));
        disabledRuleIds = Arrays.asList(getOptionalProperty(props, "disabledRuleIds", "").split(",\\s*"));
        globalConfig.setGrammalecteServer(getOptionalProperty(props, "grammalecteServer", null));
        globalConfig.setGrammalecteUser(getOptionalProperty(props, "grammalecteUser", null));
        globalConfig.setGrammalectePassword(getOptionalProperty(props, "grammalectePassword", null));
        String beolingusFile = getOptionalProperty(props, "beolingusFile", null);
        if (beolingusFile != null) {
          if (new File(beolingusFile).exists()) {
            globalConfig.setBeolingusFile(new File(beolingusFile));
          } else {
            throw new IllegalArgumentException("beolingusFile not found: " + beolingusFile);
          }
        }
        for (Object o : props.keySet()) {
          String key = (String)o;
          if (!KNOWN_OPTION_KEYS.contains(key) && !key.matches("lang-[a-z]+-dictPath") && !key.matches("lang-[a-z]+")) {
            System.err.println("***** WARNING: ****");
            System.err.println("Key '" + key + "' from configuration file '" + file + "' is unknown. Please check the key's spelling (case is significant).");
            System.err.println("Known keys: " + KNOWN_OPTION_KEYS);
          }
        }

        addDynamicLanguages(props);
        setAbTest(getOptionalProperty(props, "abTest", null));
        setAbTestClients(getOptionalProperty(props, "abTestClients", null));
        setAbTestRollout(Integer.parseInt(getOptionalProperty(props, "abTestRollout", "100")));
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not load properties from '" + file + "'", e);
    }
  }

  private void addDynamicLanguages(Properties props) throws IOException {
    for (Object keyObj : props.keySet()) {
      String key = (String)keyObj;
      if (key.startsWith("lang-") && !key.contains("-dictPath")) {
        String code = key.substring("lang-".length());
        if (!code.contains("-") && code.length() != 2 && code.length() != 3) {
          throw new IllegalArgumentException("code is supposed to be a 2 (or rarely 3) character code (unless it uses a format with variant, like xx-YY): '" + code + "'");
        }
        String nameKey = "lang-" + code;
        String name = props.getProperty(nameKey);
        String dictPathKey = "lang-" + code + "-dictPath";
        String dictPath = props.getProperty(dictPathKey);
        if (dictPath == null) {
          throw new IllegalArgumentException(dictPathKey + " must be set");
        }
        File dictPathFile = new File(dictPath);
        if (!dictPathFile.exists() || !dictPathFile.isFile()) {
          throw new IllegalArgumentException("dictionary file does not exist or is not a file: '" + dictPath + "'");
        }
        ServerTools.print("Adding dynamic spell checker language " + name + ", code: " + code + ", dictionary: " + dictPath);
        Language lang = Languages.addLanguage(name, code, new File(dictPath));
        // better fail early in case of misconfiguration, so use the language now:
        if (!new File(lang.getCommonWordsPath()).exists()) {
          throw new IllegalArgumentException("Common words path not found: '" + lang.getCommonWordsPath() + "'");
        }
        JLanguageTool lt = new JLanguageTool(lang);
        lt.check("test");
      }
    }
  }

  public void setLanguageModelDirectory(String langModelDir) {
    SuggestionsOrdererConfig.setNgramsPath(langModelDir);
    languageModelDir = new File(langModelDir);
    if (!languageModelDir.exists() || !languageModelDir.isDirectory()) {
      throw new RuntimeException("LanguageModel directory not found or is not a directory: " + languageModelDir);
    }
  }

  private void setWord2VecModelDirectory(String w2vModelDir) {
    word2vecModelDir = new File(w2vModelDir);
    if (!word2vecModelDir.exists() || !word2vecModelDir.isDirectory()) {
      throw new RuntimeException("Word2Vec directory not found or is not a directory: " + word2vecModelDir);
    }
  }

  private void setNeuralNetworkModelDir(String nnModelDir) {
    neuralNetworkModelDir = new File(nnModelDir);
    if (!neuralNetworkModelDir.exists() || !neuralNetworkModelDir.isDirectory()) {
      throw new RuntimeException("Neural network model directory not found or is not a directory: " + neuralNetworkModelDir);
    }
  }

  private void setFasttextPaths(String fasttextModelPath, String fasttextBinaryPath) {
    fasttextModel = new File(fasttextModelPath);
    fasttextBinary = new File(fasttextBinaryPath);
    if (!fasttextModel.exists() || fasttextModel.isDirectory()) {
      throw new RuntimeException("Fasttext model path not valid (file doesn't exist or is a directory): " + fasttextModelPath);
    }
    if (!fasttextBinary.exists() || fasttextBinary.isDirectory() || !fasttextBinary.canExecute()) {
      throw new RuntimeException("Fasttext binary path not valid (file doesn't exist, is a directory or not executable): " + fasttextBinaryPath);
    }
  }

  /*
   * @param verbose if true, the text to be checked will be displayed in case of exceptions
   */
  public boolean isVerbose() {
    return verbose;
  }

  public boolean isPublicAccess() {
    return publicAccess;
  }

  public int getPort() {
    return port;
  }

  /**
   * Value to set as the "Access-Control-Allow-Origin" http header. {@code null}
   * will not return that header at all. With {@code *} your server can be used from any other web site
   * from Javascript/Ajax (search Cross-origin resource sharing (CORS) for details).
   */
  @Nullable
  public String getAllowOriginUrl() {
    return allowOriginUrl;
  }

  /**
   * @since 4.2
   */
  public void setAllowOriginUrl(String allowOriginUrl) {
    this.allowOriginUrl = allowOriginUrl;
  }

  /**
   * @since 4.8
   * @return prefix / base URL for API requests
   */
  @Nullable
  public URI getServerURL() {
    return serverURL;
  }

  /**
   * @since 4.8
   * @param url prefix / base URL for API requests
   */
  public void setServerURL(@Nullable String url) {
    if (url != null) {
      try {
        // ignore different protocols, ports,... just use path for relative requests
        serverURL = new URI(new URI(url).getPath());
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException("Could not parse provided serverURL: '" + url + "'", e);
      }
    } else {
      serverURL = null;
    }
  }

  /**
   * @param len the maximum text length allowed (in number of characters), texts that are longer
   *            will cause an exception when being checked, unless the user can provide
   *            a JWT 'token' parameter with a 'maxTextLength' claim          
   */
  public void setMaxTextLength(int len) {
    this.maxTextLength = len;
  }

  /**
   * @param len the maximum text length allowed (in number of characters), texts that are longer
   *            will cause an exception when being checked even if the user can provide a JWT token
   * @since 3.9
   */
  public void setMaxTextHardLength(int len) {
    this.maxTextHardLength = len;
  }

  int getMaxTextLength() {
    return maxTextLength;
  }

  /**
   * Maximum text length for users that can identify themselves with an API key.
   * @since 4.2
   */
  int getMaxTextLengthWithApiKey() {
    return maxTextLengthWithApiKey;
  }

  /**
   * Limit for maximum text length - text cannot be longer than this, even if user has valid secret token.
   * @since 3.9
   */
  int getMaxTextHardLength() {
    return maxTextHardLength;
  }

  /**
   * Optional JWT token key. Can be used to circumvent the maximum text length (but not maxTextHardLength).
   * @since 3.9
   */
  @Nullable
  String getSecretTokenKey() {
    return secretTokenKey;
  }

  /**
   * @since 4.0
   */
  void setSecretTokenKey(String secretTokenKey) {
    this.secretTokenKey = secretTokenKey;
  }

  int getRequestLimit() {
    return requestLimit;
  }

  /** @since 4.0 */
  int getTimeoutRequestLimit() {
    return timeoutRequestLimit;
  }

  /** @since 4.0 */
  int getRequestLimitInBytes() {
    return requestLimitInBytes;
  }

  int getRequestLimitPeriodInSeconds() {
    return requestLimitPeriodInSeconds;
  }

  /** since 4.4 */
  int getIpFingerprintFactor() {
    return ipFingerprintFactor;
  }

  /**
   * @param maxCheckTimeMillis The maximum duration allowed for a single check in milliseconds, checks that take longer
   *                      will stop with an exception. Use {@code -1} for no limit.
   * @since 2.6
   */
  void setMaxCheckTimeMillis(int maxCheckTimeMillis) {
    this.maxCheckTimeMillis = maxCheckTimeMillis;
  }

  /** @since 2.6 */
  long getMaxCheckTimeMillis() {
    return maxCheckTimeMillis;
  }

  /** @since 4.2 */
  long getMaxCheckTimeWithApiKeyMillis() {
    return maxCheckTimeWithApiKeyMillis;
  }

  /**
   * Get language model directory (which contains '3grams' sub directory) or {@code null}.
   * @since 2.7
   */
  @Nullable
  File getLanguageModelDir() {
    return languageModelDir;
  }

  /**
   * Get word2vec model directory (which contains 'en' sub directories and final_embeddings.txt and dictionary.txt) or {@code null}.
   * @since 4.0
   */
  @Nullable
  File getWord2VecModelDir() {
    return word2vecModelDir;
  }

  /**
   * Get base directory for neural network models or {@code null}
   * @since 4.4
   */
  @Deprecated
  public File getNeuralNetworkModelDir() {
    return neuralNetworkModelDir;
  }


  /**
   * Get model path for fasttext language detection
   * @since 4.3
   */
  @Nullable
  public File getFasttextModel() {
    return fasttextModel;
  }

  /**
   * Set model path for fasttext language detection
   * @since 4.4
   */
  public void setFasttextModel(File model) {
    fasttextModel = Objects.requireNonNull(model);
  }

  /**
   * Get binary path for fasttext language detection
   * @since 4.3
   */
  @Nullable
  public File getFasttextBinary() {
    return fasttextBinary;
  }

  /**
   * Set binary path for fasttext language detection
   * @since 4.4
   */
  public void setFasttextBinary(File binary) {
    fasttextBinary = Objects.requireNonNull(binary);
  }

  /** @since 2.7 */
  Mode getMode() {
    return mode;
  }

  /**
   * @param maxCheckThreads The maximum number of threads serving requests running at the same time.
   * If there are more requests, they will be queued until a thread can work on them.
   * @since 2.7
   */
  void setMaxCheckThreads(int maxCheckThreads) {
    this.maxCheckThreads = maxCheckThreads;
  }

  /** @since 2.7 */
  int getMaxCheckThreads() {
    return maxCheckThreads;
  }

  /**
   * Set to {@code true} if this is running behind a (reverse) proxy which
   * sets the {@code X-forwarded-for} HTTP header. The last IP address (but not local IP addresses)
   * in that header will then be used for enforcing a request limitation.
   * @since 2.8
   */
  void setTrustXForwardForHeader(boolean trustXForwardForHeader) {
    this.trustXForwardForHeader = trustXForwardForHeader;
  }

  /** @since 2.8 */
  boolean getTrustXForwardForHeader() {
    return trustXForwardForHeader;
  }

  /** @since 2.9 */
  int getMaxWorkQueueSize() {
    return maxWorkQueueSize;
  }


  /**
   * @since 4.4
   * Cache initialized JLanguageTool instances and share between non-parallel requests with identical parameters.
   * Improves response time (especially when dealing with many small requests without specific settings),
   * but increases memory usage
   */
  public boolean isPipelineCachingEnabled() {
    return pipelineCaching;
  }

  /**
   * @since 4.4
   * Before starting to listen for requests, create a few pipelines for frequently used request settings
   * and run simple checks on them; prevents long response time / request overload on the first real incoming requests
   */
  public boolean isPipelinePrewarmingEnabled() {
    return pipelinePrewarming;
  }

  /**
   * @since 4.4
   * Keep pipelines ready for this many different request settings
   */
  public int getMaxPipelinePoolSize() {
    return maxPipelinePoolSize;
  }

  /**
   * @since 4.4
   * Expire pipelines for a specific request setting after this many seconds without any matching request elapsed
   */
  public int getPipelineExpireTime() {
    return pipelineExpireTime;
  }


  /** @since 4.4 */
  public void setPipelineCaching(boolean pipelineCaching) {
    this.pipelineCaching = pipelineCaching;
  }

  /** @since 4.4 */
  public void setPipelinePrewarming(boolean pipelinePrewarming) {
    this.pipelinePrewarming = pipelinePrewarming;
  }

  /** @since 4.4 */
  public void setMaxPipelinePoolSize(int maxPipelinePoolSize) {
    this.maxPipelinePoolSize = maxPipelinePoolSize;
  }

  /** @since 4.4 */
  public void setPipelineExpireTime(int pipelineExpireTime) {
    this.pipelineExpireTime = pipelineExpireTime;
  }

  /**
   * Cache size (in number of sentences).
   * @since 3.7
   */
  int getCacheSize() {
    return cacheSize;
  }

  /** 
   * Set cache size (in number of sentences).
   * @since 4.2
   */
  void setCacheSize(int sentenceCacheSize) {
    this.cacheSize = sentenceCacheSize;
  }

  /**
   * Cache entry TTL; refreshed on access; in seconds
   * @since 4.6
   */
  long getCacheTTLSeconds() {
    return cacheTTLSeconds;
  }

  /**
   * Set cache entry TTL in seconds
   * @since 4.6
   */
  void setCacheTTLSeconds(long cacheTTLSeconds) {
    this.cacheTTLSeconds = cacheTTLSeconds;
  }

  /**
   * Maximum errors per word rate, checking will stop if the rate is higher.
   * For example, with a rate of 0.33, the checking would stop if the user's
   * text has so many errors that more than every 3rd word causes a rule match.
   * Note that this may not apply for very short texts.
   * @since 4.0
   */
  float getMaxErrorsPerWordRate() {
    return maxErrorsPerWordRate;
  }

  /**
   * Maximum number of spelling errors for which a suggestion will be generated
   * per check. It makes sense to limit this as generating suggestions is a CPU-heavy task.
   * @since 4.2
   */
  int getMaxSpellingSuggestions() {
    return maxSpellingSuggestions;
  }

  /**
   * A list of HTTP referrers that are blocked and will only get an error message.
   * @since 4.2
   */
  @NotNull
  List<String> getBlockedReferrers() {
    return blockedReferrers;
  }

  /**
   * @since 4.2
   */
  void setBlockedReferrers(List<String> blockedReferrers) {
    this.blockedReferrers = Objects.requireNonNull(blockedReferrers);
  }
  
  /**
   * URL of server that is queried to add additional (but hidden) matches to the result.
   * @since 4.0
   */
  @Nullable
  String getHiddenMatchesServer() {
    return hiddenMatchesServer;
  }

  /**
   * Timeout in milliseconds for querying {@link #getHiddenMatchesServer()}.
   * @since 4.0
   */
  int getHiddenMatchesServerTimeout() {
    return hiddenMatchesServerTimeout;
  }

  /**
   * Period to skip requests to hidden matches server after a timeout (in milliseconds)
   * @since 4.5
   */
  int getHiddenMatchesServerFailTimeout() {
    return hiddenMatchesServerFailTimeout;
  }

  /**
   * Languages for which {@link #getHiddenMatchesServer()} will be queried.
   * @since 4.0
   */
  List<Language> getHiddenMatchesLanguages() {
    return hiddenMatchesLanguages;
  }

  /**
   * Number of failed/timed out requests after which server gets marked as down
   * @since 5.1
   */
  @Experimental
  int getHiddenMatchesServerFall() {
    return hiddenMatchesServerFall;
  }

  /**
   * @return the file from which server rules configuration should be loaded, or {@code null}
   * @since 3.0
   */
  @Nullable
  File getRulesConfigFile() {
    return rulesConfigFile;
  }

  /**
   * @return the file from which remote rules should be configured, or {@code null}
   * @since 4.9
   */
  @Nullable
  File getRemoteRulesConfigFile() {
    return remoteRulesConfigFile;
  }

  /**
   * @return the database driver name like {@code org.mariadb.jdbc.Driver}, or {@code null}
   * @since 4.2
   */
  @Nullable
  String getDatabaseDriver() {
    return dbDriver;
  }
  
  /**
   * @since 4.2
   */
  void setDatabaseDriver(String dbDriver) {
    this.dbDriver = dbDriver;
  }

  /**
   * @return the database url like {@code jdbc:mysql://localhost:3306/languagetool}, or {@code null}
   * @since 4.2
   */
  @Nullable
  String getDatabaseUrl() {
    return dbUrl;
  }

  /**
   * @since 4.2
   */
  void setDatabaseUrl(String dbUrl) {
    this.dbUrl = dbUrl;
  }
  
  /**
   * @return the database username, or {@code null}
   * @since 4.2
   */
  @Nullable
  String getDatabaseUsername() {
    return dbUsername;
  }

  /**
   * @since 4.2
   */
  void setDatabaseUsername(String dbUsername) {
    this.dbUsername = dbUsername;
  }
  
  /**
   * @return the database password matching {@link #getDatabaseUsername()}, or {@code null}
   * @since 4.2
   */
  @Nullable
  String getDatabasePassword() {
    return dbPassword;
  }

  /**
   * @since 4.2
   */
  void setDatabasePassword(String dbPassword) {
    this.dbPassword = dbPassword;
  }
  
  /**
   * Whether meta data about each search (like in the logfile) should be logged to the database.
   * @since 4.4
   */
  void setDatabaseLogging(boolean logging) {
    this.dbLogging = logging;
  }

  /**
   * @since 4.4
   */
  boolean getDatabaseLogging() {
    return this.dbLogging;
  }


  /**
   * @since 4.6
   */
  public boolean isPrometheusMonitoring() {
    return prometheusMonitoring;
  }

  /**
   * @since 4.6
   */
  public int getPrometheusPort() {
    return prometheusPort;
  }

  /**
   * @since 4.5
   * @return threshold for rule computation time until a warning gets logged, in milliseconds
   */
  public int getSlowRuleLoggingThreshold() {
    return slowRuleLoggingThreshold;
  }

  /**
   * @since 4.5
   */
  boolean isSkipLoggingRuleMatches() {
    return this.skipLoggingRuleMatches;
  }


  /**
   * @since 4.6
   */
  public boolean isSkipLoggingChecks() {
    return skipLoggingChecks;
  }

  /**
   * @since 4.7
   */
  public List<String> getDisabledRuleIds() {
    return disabledRuleIds;
  }

  /**
   * Whether the server can be stopped by sending a command (useful for tests only).
   */
  boolean isStoppable() {
    return stoppable;
  }

  /**
   * @since 4.4
   * See if a specific A/B-Test is to be run
   */
  @Nullable
  public String getAbTest() {
    return abTest;
  }

  /**
   * @since 4.4
   * Enable a specific A/B-Test to be run (or null to disable all tests)
   */
  public void setAbTest(@Nullable String abTest) {
    if (abTest != null && abTest.trim().isEmpty()) {
      this.abTest = null;
    } else {
      this.abTest = abTest;
    }
  }

  /**
   * Clients that a A/B test runs on; null -&gt; disabled
   * @since 4.9
   */
  @Experimental
  @Nullable
  public Pattern getAbTestClients() {
    return abTestClients;
  }

  /**
   * Clients that a A/B test runs on; null -&gt; disabled
   * @since 4.9
   */
  @Experimental
  public void setAbTestClients(@Nullable String pattern) {
    if (pattern == null) {
      this.abTestClients = null;
    } else {
      this.abTestClients = Pattern.compile(pattern);
    }
  }

  /**
   * @param abTestRollout percentage [0,100] of users to include in ab test rollout
   * @since 4.9
   */
  @Experimental
  public void setAbTestRollout(int abTestRollout) {
    this.abTestRollout = abTestRollout;
  }

  /**
   * @since 4.9
   */
  @Experimental
  public int getAbTestRollout() {
    return abTestRollout;
  }

  /**
   * @throws IllegalConfigurationException if property is not set 
   */
  protected String getProperty(Properties props, String propertyName, File config) {
    String propertyValue = (String)props.get(propertyName);
    if (propertyValue == null || propertyValue.trim().isEmpty()) {
      throw new IllegalConfigurationException("Property '" + propertyName + "' must be set in " + config);
    }
    return propertyValue;
  }

  protected String getOptionalProperty(Properties props, String propertyName, String defaultValue) {
    String propertyValue = (String)props.get(propertyName);
    if (propertyValue == null) {
      return defaultValue;
    }
    return propertyValue;
  }

}
