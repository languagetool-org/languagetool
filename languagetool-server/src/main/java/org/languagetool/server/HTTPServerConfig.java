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
import java.util.*;

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
  protected int cacheSize = 0;
  protected float maxErrorsPerWordRate = 0;
  protected int maxSpellingSuggestions = 0;
  protected List<String> blockedReferrers = new ArrayList<>();
  protected String hiddenMatchesServer;
  protected int hiddenMatchesServerTimeout;
  protected int hiddenMatchesServerFailTimeout;
  protected List<Language> hiddenMatchesLanguages = new ArrayList<>();
  protected String dbDriver = null;
  protected String dbUrl = null;
  protected String dbUsername = null;
  protected String dbPassword = null;
  protected boolean dbLogging;
  protected boolean prometheusMonitoring = false;
  protected int prometheusPort = 9301;
  protected GlobalConfig globalConfig = new GlobalConfig();

  protected boolean skipLoggingRuleMatches = false;
  protected boolean skipLoggingChecks = false;

  protected int slowRuleLoggingThreshold = -1; // threshold in milliseconds, used by SlowRuleLogger; < 0 - disabled

  protected String abTest = null;
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
        pipelineCaching = Boolean.parseBoolean(getOptionalProperty(props, "pipelineCaching", "false"));
        pipelinePrewarming = Boolean.parseBoolean(getOptionalProperty(props, "pipelinePrewarming", "false"));
        maxPipelinePoolSize = Integer.parseInt(getOptionalProperty(props, "maxPipelinePoolSize", "5"));
        pipelineExpireTime = Integer.parseInt(getOptionalProperty(props, "pipelineExpireTimeInSeconds", "10"));
        requestLimitPeriodInSeconds = Integer.parseInt(getOptionalProperty(props, "requestLimitPeriodInSeconds", "0"));
        ipFingerprintFactor = Integer.parseInt(getOptionalProperty(props, "ipFingerprintFactor", "1"));
        trustXForwardForHeader = Boolean.valueOf(getOptionalProperty(props, "trustXForwardForHeader", "false"));
        maxWorkQueueSize = Integer.parseInt(getOptionalProperty(props, "maxWorkQueueSize", "0"));
        if (maxWorkQueueSize < 0) {
          throw new IllegalArgumentException("maxWorkQueueSize must be >= 0: " + maxWorkQueueSize);
        }
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
            throw new RuntimeException("Rules Configuration file can not be found: " + rulesConfigFile);
          }
        }
        cacheSize = Integer.parseInt(getOptionalProperty(props, "cacheSize", "0"));
        if (cacheSize < 0) {
          throw new IllegalArgumentException("Invalid value for cacheSize: " + cacheSize + ", use 0 to deactivate cache");
        }
        if (props.containsKey("warmUp")) {
          System.err.println("Setting ignored: 'warmUp'. Look into using pipelineCaching and pipelinePrewarming instead.");
        }
        maxErrorsPerWordRate = Float.parseFloat(getOptionalProperty(props, "maxErrorsPerWordRate", "0"));
        maxSpellingSuggestions = Integer.parseInt(getOptionalProperty(props, "maxSpellingSuggestions", "0"));
        blockedReferrers = Arrays.asList(getOptionalProperty(props, "blockedReferrers", "").split(",\\s*"));
        hiddenMatchesServer = getOptionalProperty(props, "hiddenMatchesServer", null);
        hiddenMatchesServerTimeout = Integer.parseInt(getOptionalProperty(props, "hiddenMatchesServerTimeout", "1000"));
        hiddenMatchesServerFailTimeout = Integer.parseInt(getOptionalProperty(props, "hiddenMatchesServerFailTimeout", "10000"));
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
        dbLogging = Boolean.valueOf(getOptionalProperty(props, "dbLogging", "false"));
        prometheusMonitoring = Boolean.valueOf(getOptionalProperty(props, "prometheusMonitoring", "false"));
        prometheusPort = Integer.parseInt(getOptionalProperty(props, "prometheusPort", "9301"));
        skipLoggingRuleMatches = Boolean.valueOf(getOptionalProperty(props, "skipLoggingRuleMatches", "false"));
        skipLoggingChecks = Boolean.valueOf(getOptionalProperty(props, "skipLoggingChecks", "false"));
        if (dbLogging && (dbDriver == null || dbUrl == null || dbUsername == null || dbPassword == null)) {
          throw new IllegalArgumentException("dbLogging can only be true if dbDriver, dbUrl, dbUsername, and dbPassword are all set");
        }
        slowRuleLoggingThreshold = Integer.valueOf(getOptionalProperty(props,
          "slowRuleLoggingThreshold", "-1"));
        globalConfig.setGrammalecteServer(getOptionalProperty(props, "grammalecteServer", null));
        globalConfig.setGrammalecteUser(getOptionalProperty(props, "grammalecteUser", null));
        globalConfig.setGrammalectePassword(getOptionalProperty(props, "grammalectePassword", null));

        addDynamicLanguages(props);
        setAbTest(getOptionalProperty(props, "abTest", null));
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
        if (!dictPathFile.getName().endsWith(".dict")) {
          throw new IllegalArgumentException("dictionary file is supposed to be named *.dict: '" + dictPath + "'");
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
  @Experimental
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
  @Experimental
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
   * Cache initalized JLanguageTool instances and share between non-parallel requests with identical paramenters
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
  @Experimental
  String getHiddenMatchesServer() {
    return hiddenMatchesServer;
  }

  /**
   * Timeout in milliseconds for querying {@link #getHiddenMatchesServer()}.
   * @since 4.0
   */
  @Experimental
  int getHiddenMatchesServerTimeout() {
    return hiddenMatchesServerTimeout;
  }

  /**
   * Period to skip requests to hidden matches server after a timeout (in milliseconds)
   * @since 4.5
   */
  @Experimental
  int getHiddenMatchesServerFailTimeout() {
    return hiddenMatchesServerFailTimeout;
  }

  /**
   * Languages for which {@link #getHiddenMatchesServer()} will be queried.
   * @since 4.0
   */
  @Experimental
  List<Language> getHiddenMatchesLanguages() {
    return hiddenMatchesLanguages;
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
   * @return the database driver name like {@code org.mariadb.jdbc.Driver}, or {@code null}
   * @since 4.2
   */
  @Nullable
  @Experimental
  String getDatabaseDriver() {
    return dbDriver;
  }
  
  /**
   * @since 4.2
   */
  @Experimental
  void setDatabaseDriver(String dbDriver) {
    this.dbDriver = dbDriver;
  }

  /**
   * @return the database url like {@code jdbc:mysql://localhost:3306/languagetool}, or {@code null}
   * @since 4.2
   */
  @Nullable
  @Experimental
  String getDatabaseUrl() {
    return dbUrl;
  }

  /**
   * @since 4.2
   */
  @Experimental
  void setDatabaseUrl(String dbUrl) {
    this.dbUrl = dbUrl;
  }
  
  /**
   * @return the database username, or {@code null}
   * @since 4.2
   */
  @Nullable
  @Experimental
  String getDatabaseUsername() {
    return dbUsername;
  }

  /**
   * @since 4.2
   */
  @Experimental
  void setDatabaseUsername(String dbUsername) {
    this.dbUsername = dbUsername;
  }
  
  /**
   * @return the database password matching {@link #getDatabaseUsername()}, or {@code null}
   * @since 4.2
   */
  @Nullable
  @Experimental
  String getDatabasePassword() {
    return dbPassword;
  }

  /**
   * @since 4.2
   */
  @Experimental
  void setDatabasePassword(String dbPassword) {
    this.dbPassword = dbPassword;
  }
  
  /**
   * Whether meta data about each search (like in the logfile) should be logged to the database.
   * @since 4.4
   */
  @Experimental
  void setDatabaseLogging(boolean logging) {
    this.dbLogging = logging;
  }

  /**
   * @since 4.4
   */
  @Experimental
  boolean getDatabaseLogging() {
    return this.dbLogging;
  }


  /**
   * @since 4.6
   * @return
   */
  public boolean isPrometheusMonitoring() {
    return prometheusMonitoring;
  }

  /**
   * @since 4.6
   * @return
   */
  public int getPrometheusPort() {
    return prometheusPort;
  }

  /**
   * @since 4.5
   * @return threshold for rule computation time until a warning gets logged, in milliseconds
   */
  @Experimental
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
   * @since 4.4
   * See if a specific A/B-Test is to be run
   */
  @Experimental
  @Nullable
  public String getAbTest() {
    return abTest;
  }

  /**
   * @since 4.4
   * Enable a specific A/B-Test to be run (or null to disable all tests)
   */
  @Experimental
  public void setAbTest(@Nullable String abTest) {
    List<String> values = Arrays.asList("SuggestionsOrderer", "SuggestionsRanker");
    if (abTest != null && !values.contains(abTest)) {
        throw new IllegalConfigurationException("Unknown value for 'abTest' property: Must be one of: " + values);
    }
    this.abTest = abTest;
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
