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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger logger = LoggerFactory.getLogger(HTTPServerConfig.class);

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

  protected boolean logIp = true;
  protected String logIpMatchingPattern = "__logIPNowForLanguageTool__";

  protected URI serverURL = null;
  protected int maxTextLengthAnonymous = Integer.MAX_VALUE;
  protected int maxTextLengthLoggedIn = Integer.MAX_VALUE;
  protected int maxTextLengthPremium = Integer.MAX_VALUE;
  protected int maxTextHardLength = Integer.MAX_VALUE;
  protected String secretTokenKey = null;
  protected long maxCheckTimeMillisAnonymous = -1;
  protected long maxCheckTimeMillisLoggedIn = -1;
  protected long maxCheckTimeMillisPremium = -1;
  protected int maxCheckThreads = 10;
  protected int maxTextCheckerThreads; // default to same value as maxCheckThreads
  protected int textCheckerQueueSize = 8;
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
  protected List<String> requestLimitWhitelistUsers;
  protected int requestLimitWhitelistLimit;
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
  protected boolean premiumAlways;
  protected boolean premiumOnly;

  public void setPremiumOnly(boolean premiumOnly) {
    this.premiumOnly = premiumOnly;
  }
  boolean anonymousAccessAllowed = true;
  public boolean isAnonymousAccessAllowed() {
    return anonymousAccessAllowed;
  }
  protected boolean gracefulDatabaseFailure = false;

  /**
   * @since 4.9
   * @return whether user creation should be restricted (e.g. according to subscriptions in cloud usage) or be unlimited (for self-hosted installations)
   */
  public boolean isRestrictManagedAccounts() {
    return restrictManagedAccounts;
  }

  public void setRestrictManagedAccounts(boolean restrictManagedAccounts) {
    this.restrictManagedAccounts = restrictManagedAccounts;
  }
  // NOTE: offer option to set this in configuration file; document for customers
  protected boolean restrictManagedAccounts = true;
  protected String dbDriver = null;
  protected String dbUrl = null;
  protected String dbUsername = null;
  protected String dbPassword = null;
  protected long dbTimeoutSeconds = 10;
  protected int databaseTimeoutRateThreshold = 100;
  protected int databaseErrorRateThreshold = 50;
  protected int databaseDownIntervalSeconds = 10;

  protected boolean dbLogging;
  protected boolean prometheusMonitoring = false;
  protected int prometheusPort = 9301;
  protected GlobalConfig globalConfig = new GlobalConfig();
  protected List<String> disabledRuleIds = new ArrayList<>();
  protected boolean stoppable = false;
  
  protected String passwortLoginAccessListPath = "";
  /**
   * caching to avoid database hits for e.g. dictionaries
   * null -&gt; disabled
   */
  @Nullable
  protected String redisHost = null;
  protected int redisPort = 6379;
  protected int redisDatabase = 0;
  protected boolean redisUseSSL = true;
  protected String redisCertificate;
  protected String redisKey;
  protected String redisKeyPassword;
  @Nullable
  protected String redisPassword = null;
  protected long redisDictTTL = 600; // in seconds
  protected long redisTimeout = 100; // in milliseconds
  protected long redisConnectionTimeout = 5000; // in milliseconds
  protected boolean redisUseSentinel = false;
  protected String sentinelHost;
  protected int sentinelPort = 26379;
  protected String sentinelPassword;
  protected String sentinelMasterId;

  protected boolean skipLoggingRuleMatches = false;
  protected boolean skipLoggingChecks = false;

  protected int slowRuleLoggingThreshold = -1; // threshold in milliseconds, used by SlowRuleLogger; < 0 - disabled

  protected String abTest = null;
  protected Pattern abTestClients = null;
  protected int abTestRollout = 100; // percentage [0,100]
  protected File ngramLangIdentData;

  private static final List<String> KNOWN_OPTION_KEYS = Arrays.asList("abTest", "abTestClients", "abTestRollout",
    "beolingusFile", "blockedReferrers", "cacheSize", "cacheTTLSeconds",
    "dbDriver", "dbPassword", "dbUrl", "dbUsername", "disabledRuleIds", "fasttextBinary", "fasttextModel", "grammalectePassword",
    "grammalecteServer", "grammalecteUser", "ipFingerprintFactor", "languageModel", "maxCheckThreads", "maxTextCheckerThreads", "textCheckerQueueSize", "maxCheckTimeMillis",
    "maxCheckTimeWithApiKeyMillis", "maxErrorsPerWordRate", "maxPipelinePoolSize", "maxSpellingSuggestions", "maxTextHardLength",
    "maxTextLength", "maxTextLengthWithApiKey", "maxWorkQueueSize", "neuralNetworkModel", "pipelineCaching",
    "pipelineExpireTimeInSeconds", "pipelinePrewarming", "prometheusMonitoring", "prometheusPort", "remoteRulesFile",
    "requestLimit", "requestLimitInBytes", "requestLimitPeriodInSeconds", "requestLimitWhitelistUsers", "requestLimitWhitelistLimit",
    "rulesFile", "secretTokenKey", "serverURL",
    "skipLoggingChecks", "skipLoggingRuleMatches", "timeoutRequestLimit", "trustXForwardForHeader", "warmUp", "word2vecModel",
    "keystore", "password", "maxTextLengthPremium", "maxTextLengthAnonymous", "maxTextLengthLoggedIn", "gracefulDatabaseFailure",
    "ngramLangIdentData",
    "dbTimeoutSeconds", "dbErrorRateThreshold", "dbTimeoutRateThreshold", "dbDownIntervalSeconds",
    "redisUseSSL", "redisTimeoutMilliseconds", "redisConnectionTimeoutMilliseconds",
    "anonymousAccessAllowed",
    "premiumAlways",
    "redisPassword", "redisHost", "redisCertificate", "redisKey", "redisKeyPassword",
    "redisUseSentinel", "sentinelHost", "sentinelPort", "sentinelPassword", "sentinelMasterId",
    "dbLogging", "premiumOnly", "nerUrl");

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
        case "--premiumAlways":
          if (!Premium.isPremiumVersion()) {
            throw new IllegalArgumentException("Cannot use --premiumAlways with non-premium version");
          }
          premiumAlways = true;
          System.out.println("*** Running in PREMIUM-ALWAYS mode, premium features are available without authentication");
          break;
        case "--allow-origin":
          try {
            allowOriginUrl = args[++i];
            if (allowOriginUrl.startsWith("--")) {
              allowOriginUrl = "*";
            }
          } catch (ArrayIndexOutOfBoundsException e) {
            allowOriginUrl = "*";
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
        case "--notLogIP":
          logIp = false;
          break;
        case "--logIpMatchingPattern":
          try {
            logIpMatchingPattern = args[++i];
            if (logIpMatchingPattern.startsWith("--")) {
              throw new IllegalArgumentException("Missing argument for '--logIpMatchingPattern' (e.g. any random String)");
            }
          } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Missing argument for '--logIpMatchingPattern' (e.g. any random String)");
          }
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
        maxTextHardLength = Integer.parseInt(getOptionalProperty(props, "maxTextHardLength", Integer.toString(Integer.MAX_VALUE)));
        secretTokenKey = getOptionalProperty(props, "secretTokenKey", null);

        maxTextLengthAnonymous = maxTextLengthLoggedIn = maxTextLengthPremium = Integer.parseInt(getOptionalProperty(props, "maxTextLength", Integer.toString(Integer.MAX_VALUE)));
        maxTextLengthAnonymous = Integer.parseInt(getOptionalProperty(props, "maxTextLengthAnonymous", String.valueOf(maxTextLengthAnonymous)));
        maxTextLengthLoggedIn = Integer.parseInt(getOptionalProperty(props, "maxTextLengthLoggedIn", String.valueOf(maxTextLengthLoggedIn)));
        maxTextLengthPremium = Integer.parseInt(getOptionalProperty(props, "maxTextLengthPremium", String.valueOf(maxTextLengthPremium)));

        maxCheckTimeMillisAnonymous = maxCheckTimeMillisLoggedIn = maxCheckTimeMillisPremium = Integer.parseInt(getOptionalProperty(props, "maxCheckTimeMillis", "-1"));
        maxCheckTimeMillisAnonymous = Long.parseLong(getOptionalProperty(props, "maxCheckTimeMillisAnonymous", String.valueOf(maxCheckTimeMillisAnonymous)));
        maxCheckTimeMillisLoggedIn = Long.parseLong(getOptionalProperty(props, "maxCheckTimeMillisLoggedIn", String.valueOf(maxCheckTimeMillisLoggedIn)));
        maxCheckTimeMillisPremium = Long.parseLong(getOptionalProperty(props, "maxCheckTimeMillisPremium", String.valueOf(maxCheckTimeMillisPremium)));
        requestLimit = Integer.parseInt(getOptionalProperty(props, "requestLimit", "0"));
        requestLimitInBytes = Integer.parseInt(getOptionalProperty(props, "requestLimitInBytes", "0"));
        timeoutRequestLimit = Integer.parseInt(getOptionalProperty(props, "timeoutRequestLimit", "0"));
        requestLimitWhitelistUsers = Arrays.asList(getOptionalProperty(props, "requestLimitWhitelistUsers", "").split(",\\s*"));
        requestLimitWhitelistLimit = Integer.parseInt(getOptionalProperty(props, "requestLimitWhitelistLimit", "0"));
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
        // default value 0 = use maxCheckThreads setting (for compatibility)
        maxTextCheckerThreads = Integer.parseInt(getOptionalProperty(props, "maxTextCheckerThreads", "0"));
        if (maxTextCheckerThreads < 0) {
          throw new IllegalArgumentException("Invalid value for maxTextCheckerThreads, must be >= 1: " + maxTextCheckerThreads);
        }
        textCheckerQueueSize = Integer.parseInt(getOptionalProperty(props, "textCheckerQueueSize", "8"));
        if (textCheckerQueueSize < 0) {
          throw new IllegalArgumentException("Invalid value for textCheckerQueueSize, must be >= 1: " + textCheckerQueueSize);
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
        String premiumAlwaysValue = props.getProperty("premiumAlways");
        if (premiumAlwaysValue != null) {
          premiumAlways = Boolean.parseBoolean(premiumAlwaysValue.trim());
          if (premiumAlways) {
            System.out.println("*** Running in PREMIUM-ALWAYS mode");
          }
        }
        premiumOnly = Boolean.valueOf(getOptionalProperty(props, "premiumOnly", "false").trim());
        if (premiumOnly) {
          if (!Premium.isPremiumVersion()) {
            throw new IllegalArgumentException("Cannot use premiumOnly=true with non-premium version");
          }
          System.out.println("*** Running in PREMIUM-ONLY mode");
        }
        anonymousAccessAllowed = Boolean.valueOf(getOptionalProperty(props, "anonymousAccessAllowed", "true").trim());
        if (!anonymousAccessAllowed) {
          System.out.println("*** Running in RESTRICTED-ACCESS mode");
        }

        redisHost = getOptionalProperty(props, "redisHost", null);
        redisPort = Integer.parseInt(getOptionalProperty(props, "redisPort", "6379"));
        redisDatabase = Integer.parseInt(getOptionalProperty(props, "redisDatabase", "0"));
        redisUseSSL = Boolean.valueOf(getOptionalProperty(props, "redisUseSSL", "true").trim());
        redisPassword = getOptionalProperty(props, "redisPassword", null);
        redisDictTTL = Integer.parseInt(getOptionalProperty(props, "redisDictTTLSeconds", "600"));
        redisTimeout = Integer.parseInt(getOptionalProperty(props, "redisTimeoutMilliseconds", "100"));
        redisConnectionTimeout = Integer.parseInt(getOptionalProperty(props, "redisConnectionTimeoutMilliseconds", "5000"));

        redisCertificate = getOptionalProperty(props, "redisCertificate", null);
        redisKey = getOptionalProperty(props, "redisKey", null);
        redisKeyPassword = getOptionalProperty(props, "redisKeyPassword", null);

        redisUseSentinel = Boolean.parseBoolean(getOptionalProperty(props, "redisUseSentinel", "false").trim());
        sentinelHost = getOptionalProperty(props, "sentinelHost", null);
        sentinelPort = Integer.parseInt(getOptionalProperty(props, "sentinelPort", "26379"));
        sentinelPassword = getOptionalProperty(props, "sentinelPassword", null);
        sentinelMasterId = getOptionalProperty(props, "sentinelMasterId", null);

        gracefulDatabaseFailure = Boolean.parseBoolean(getOptionalProperty(props, "gracefulDatabaseFailure", "false").trim());
        dbDriver = getOptionalProperty(props, "dbDriver", null);
        dbUrl = getOptionalProperty(props, "dbUrl", null);
        dbUsername = getOptionalProperty(props, "dbUsername", null);
        dbPassword = getOptionalProperty(props, "dbPassword", null);
        dbTimeoutSeconds = Integer.parseInt(getOptionalProperty(props, "dbTimeoutSeconds", "10"));
        databaseErrorRateThreshold = Integer.parseInt(getOptionalProperty(props, "dbErrorRateThreshold", "50"));
        databaseTimeoutRateThreshold = Integer.parseInt(getOptionalProperty(props, "dbTimeoutRateThreshold", "100"));
        databaseDownIntervalSeconds = Integer.parseInt(getOptionalProperty(props, "dbDownIntervalSeconds", "10"));
        dbLogging = Boolean.valueOf(getOptionalProperty(props, "dbLogging", "false").trim());
        passwortLoginAccessListPath = getOptionalProperty(props, "passwortLoginAccessListPath", "");
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
        String nerUrl = getOptionalProperty(props, "nerUrl", null);
        if (nerUrl != null) {
          globalConfig.setNERUrl(nerUrl);
          logger.info("Using NER service: " + globalConfig.getNerUrl());
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
        String ngramLangIdentData = getOptionalProperty(props, "ngramLangIdentData", null);
        if (ngramLangIdentData != null) {
          File dir = new File(ngramLangIdentData);
          if (!dir.exists() || dir.isDirectory()) {
            throw new IllegalArgumentException("ngramLangIdentData does not exist or is a directory (needs to be a ZIP file): " + ngramLangIdentData);
          }
          setNgramLangIdentData(dir);
        }
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

  void setFasttextPaths(String fasttextModelPath, String fasttextBinaryPath) {
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
  public void setMaxTextLengthAnonymous(int len) {
    this.maxTextLengthAnonymous = len;
  }

  public void setMaxTextLengthLoggedIn(int len) {
    this.maxTextLengthLoggedIn = len;
  }

  public void setMaxTextLengthPremium(int len) {
    this.maxTextLengthPremium = len;
  }

  /**
   * @param len the maximum text length allowed (in number of characters), texts that are longer
   *            will cause an exception when being checked even if the user can provide a JWT token
   * @since 3.9
   */
  public void setMaxTextHardLength(int len) {
    this.maxTextHardLength = len;
  }

  int getMaxTextLengthAnonymous() {
    return maxTextLengthAnonymous;
  }

  /**
   * For users that have an account, but no premium subscription
   */
  int getMaxTextLengthLoggedIn() {
    return maxTextLengthLoggedIn;
  }

  int getMaxTextLengthPremium() {
    return maxTextLengthPremium;
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

  /**
    @since 5.3
    use a higher request limit for a list of users
   */
  public List<String> getRequestLimitWhitelistUsers() {
    return requestLimitWhitelistUsers;
  }

  public void setRequestLimitWhitelistUsers(List<String> requestLimitWhitelistUsers) {
    this.requestLimitWhitelistUsers = requestLimitWhitelistUsers;
  }

  /**
   @since 5.3
   use a higher request limit for a list of users
   */
  public int getRequestLimitWhitelistLimit() {
    return requestLimitWhitelistLimit;
  }

  public void setRequestLimitWhitelistLimit(int requestLimitWhitelistLimit) {
    this.requestLimitWhitelistLimit = requestLimitWhitelistLimit;
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

  /** since 4.4
   * @return
   * if > 0: allow n more requests per IP if fingerprints differ
   * if <= 0: disable fingerprinting, only rely on IP address
   *  */
  int getIpFingerprintFactor() {
    return ipFingerprintFactor;
  }

  /**
   * @param maxCheckTimeMillis The maximum duration allowed for a single check in milliseconds, checks that take longer
   *                      will stop with an exception. Use {@code -1} for no limit.
   * @since 4.4
   */
  void setMaxCheckTimeMillisAnonymous(int maxCheckTimeMillis) {
    this.maxCheckTimeMillisAnonymous = maxCheckTimeMillis;
  }

  /** @since 4.4 */
  long getMaxCheckTimeMillisAnonymous() {
    return maxCheckTimeMillisAnonymous;
  }


  /** @since 4.4 */
  void setMaxCheckTimeMillisLoggedIn(int maxCheckTimeMillis) {
    this.maxCheckTimeMillisLoggedIn = maxCheckTimeMillis;
  }

  /** @since 4.4 */
  long getMaxCheckTimeMillisLoggedIn() {
    return maxCheckTimeMillisLoggedIn;
  }

  /** @since 4.4 */
  void setMaxCheckTimeMillisPremium(int maxCheckTimeMillis) {
    this.maxCheckTimeMillisPremium = maxCheckTimeMillis;
  }

  /** @since 4.4 */
  @Experimental
  long getMaxCheckTimeMillisPremium() {
    return maxCheckTimeMillisPremium;
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
   * @param maxTextCheckerThreads The maximum number of threads in the worker pool processing text checks running at the same time.
   * @since 5.6
   */
  void setMaxTextCheckerThreads(int maxTextCheckerThreads) {
    this.maxTextCheckerThreads = maxTextCheckerThreads;
  }

  /** @since 5.6 */
  int getMaxTextCheckerThreads() {
    // unset - use maxCheckThreads
    return maxTextCheckerThreads != 0 ? maxTextCheckerThreads : maxCheckThreads;
  }

  public int getTextCheckerQueueSize() {
    return textCheckerQueueSize;
  }

  public void setTextCheckerQueueSize(int textCheckerQueueSize) {
    this.textCheckerQueueSize = textCheckerQueueSize;
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
   * timeout for database requests (for now, only requests for credentials to log in)
   * @since 4.7
   */
  public long getDbTimeoutSeconds() {
    return dbTimeoutSeconds;
  }

  /**
   * timeout for database requests (for now, only requests for credentials to log in)
   * @since 4.7
   */
  public void setDbTimeoutSeconds(long dbTimeoutSeconds) {
    this.dbTimeoutSeconds = dbTimeoutSeconds;
  }


  /**
   * Rate in percent of requests (0-100) of timeouts during database queries until circuit breaker opens
   * @since 5.5
   */
  public int getDatabaseTimeoutRateThreshold() {
    return databaseTimeoutRateThreshold;
  }

  public void setDatabaseTimeoutRateThreshold(int databaseTimeoutRateThreshold) {
    this.databaseTimeoutRateThreshold = databaseTimeoutRateThreshold;
  }

  /**
   * Rate in percent of requests (0-100) of errors during database queries until circuit breaker opens
   * @since 5.5
   */
  public int getDatabaseErrorRateThreshold() {
    return databaseErrorRateThreshold;
  }

  public void setDatabaseErrorRateThreshold(int databaseErrorRateThreshold) {
    this.databaseErrorRateThreshold = databaseErrorRateThreshold;
  }

  /**
   * Number of seconds to skip database requests when a potential downtime has been detected
   * @since 5.5
   */
  public int getDatabaseDownIntervalSeconds() {
    return databaseDownIntervalSeconds;
  }

  public void setDatabaseDownIntervalSeconds(int databaseDownIntervalSeconds) {
    this.databaseDownIntervalSeconds = databaseDownIntervalSeconds;
  }


  /**
   * Whether requests with credentials should be treated as anonymous requests in case of DB errors/timeout or
   * throw an error
   * @since 4.7
   */
  public boolean getGracefulDatabaseFailure() {
    return gracefulDatabaseFailure;
  }

  /**
   * Whether requests with credentials should be treated as anonymous requests in case of DB errors/timeout or
   * throw an error
   * @since 4.7
   */
  public void setGracefulDatabaseFailure(boolean gracefulDatabaseFailure) {
    this.gracefulDatabaseFailure = gracefulDatabaseFailure;
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


  @Nullable
  public String getRedisHost() {
    return redisHost;
  }

  public int getRedisPort() {
    return redisPort;
  }

  public int getRedisDatabase() {
    return redisDatabase;
  }

  public boolean isRedisUseSSL() {
    return redisUseSSL;
  }
  @Nullable
  public String getRedisPassword() {
    return redisPassword;
  }

  public long getRedisDictTTLSeconds() {
    return redisDictTTL;
  }

  /**
   * Timeout for regular commands
   * @return
   */
  public long getRedisTimeoutMilliseconds() {
    return redisTimeout;
  }

  /**
   * Timeout for establishing the initial connection, including e.g. SSL handshake
   * and commands like SENTINEL, ...
   */
  public long getRedisConnectionMilliseconds() {
    return redisConnectionTimeout;
  }
  // TODO could introduce 'expire after access' logic, i.e. refresh expire when reading

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

  /** @since 5.2 */
  public void setNgramLangIdentData(File ngramLangIdentData) {
    this.ngramLangIdentData = ngramLangIdentData;
  }

  /** @since 5.2 */
  @Nullable
  public File getNgramLangIdentData() {
    return ngramLangIdentData;
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

  /** @since 5.1 */
  boolean isPremiumAlways() {
    return premiumAlways;
  }

  /** @since 5.1 */
  void setPremiumAlways(boolean premiumAlways) {
    this.premiumAlways = premiumAlways;
  }

  public boolean isPremiumOnly() {
    return premiumOnly;
  }

  /**
   * Allow using redis sentinel for automated failover */
  public boolean isRedisUseSentinel() {
    return redisUseSentinel;
  }

  public void setRedisUseSentinel(boolean redisUseSentinel) {
    this.redisUseSentinel = redisUseSentinel;
  }

  public String getSentinelHost() {
    return sentinelHost;
  }

  public void setSentinelHost(String sentinelHost) {
    this.sentinelHost = sentinelHost;
  }

  public int getSentinelPort() {
    return sentinelPort;
  }

  public void setSentinelPort(int sentinelPort) {
    this.sentinelPort = sentinelPort;
  }

  public String getSentinelPassword() {
    return sentinelPassword;
  }

  public void setSentinelPassword(String sentinelPassword) {
    this.sentinelPassword = sentinelPassword;
  }

  public String getSentinelMasterId() {
    return sentinelMasterId;
  }

  public void setSentinelMasterId(String sentinelMasterId) {
    this.sentinelMasterId = sentinelMasterId;
  }

  public String getRedisCertificate() {
    return redisCertificate;
  }

  public void setRedisCertificate(String redisCertificate) {
    this.redisCertificate = redisCertificate;
  }

  public String getRedisKey() {
    return redisKey;
  }

  public void setRedisKey(String redisKey) {
    this.redisKey = redisKey;
  }

  public String getRedisKeyPassword() {
    return redisKeyPassword;
  }

  public void setRedisKeyPassword(String redisKeyPassword) {
    this.redisKeyPassword = redisKeyPassword;
  }
  
  public String getPasswortLoginAccessListPath() { return passwortLoginAccessListPath; }
}
