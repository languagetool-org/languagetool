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
import org.jetbrains.annotations.Nullable;
import org.languagetool.Experimental;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

  protected boolean verbose = false;
  protected boolean publicAccess = false;
  protected int port = DEFAULT_PORT;
  protected String allowOriginUrl = null;
  protected int maxTextLength = Integer.MAX_VALUE;
  protected int maxTextHardLength = Integer.MAX_VALUE;
  protected String secretTokenKey = null;
  protected long maxCheckTimeMillis = -1;
  protected int maxCheckThreads = 10;
  protected Mode mode;
  protected File languageModelDir = null;
  protected File word2vecModelDir = null;
  protected int requestLimit;
  protected int requestLimitInBytes;
  protected int timeoutRequestLimit;
  protected int requestLimitPeriodInSeconds;
  protected boolean trustXForwardForHeader;
  protected int maxWorkQueueSize;
  protected File rulesConfigFile = null;
  protected int cacheSize = 0;
  protected boolean warmUp = false;
  protected float maxErrorsPerWordRate = 0;
  protected String hiddenMatchesServer;
  protected int hiddenMatchesServerTimeout;
  protected List<Language> hiddenMatchesLanguages = new ArrayList<>();

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
          parseConfigFile(new File(args[++i]), !ArrayUtils.contains(args, LANGUAGE_MODEL_OPTION), !ArrayUtils.contains(args, WORD2VEC_MODEL_OPTION));
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

  private void parseConfigFile(File file, boolean loadLangModel, boolean loadWord2VecModel) {
    try {
      Properties props = new Properties();
      try (FileInputStream fis = new FileInputStream(file)) {
        props.load(fis);
        maxTextLength = Integer.parseInt(getOptionalProperty(props, "maxTextLength", Integer.toString(Integer.MAX_VALUE)));
        maxTextHardLength = Integer.parseInt(getOptionalProperty(props, "maxTextHardLength", Integer.toString(Integer.MAX_VALUE)));
        secretTokenKey = getOptionalProperty(props, "secretTokenKey", null);
        maxCheckTimeMillis = Long.parseLong(getOptionalProperty(props, "maxCheckTimeMillis", "-1"));
        requestLimit = Integer.parseInt(getOptionalProperty(props, "requestLimit", "0"));
        requestLimitInBytes = Integer.parseInt(getOptionalProperty(props, "requestLimitInBytes", "0"));
        timeoutRequestLimit = Integer.parseInt(getOptionalProperty(props, "timeoutRequestLimit", "0"));
        requestLimitPeriodInSeconds = Integer.parseInt(getOptionalProperty(props, "requestLimitPeriodInSeconds", "0"));
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
        String warmUpStr = getOptionalProperty(props, "warmUp", "false");
        if (warmUpStr.equals("true")) {
          warmUp = true;
        } else if (warmUpStr.equals("false")) {
          warmUp = false;
        } else {
          throw new IllegalArgumentException("Invalid value for warmUp: '" + warmUpStr + "', use 'true' or 'false'");
        }
        maxErrorsPerWordRate = Float.parseFloat(getOptionalProperty(props, "maxErrorsPerWordRate", "0"));
        hiddenMatchesServer = getOptionalProperty(props, "hiddenMatchesServer", null);
        hiddenMatchesServerTimeout = Integer.parseInt(getOptionalProperty(props, "hiddenMatchesServerTimeout", "1000"));
        String langCodes = getOptionalProperty(props, "hiddenMatchesLanguages", "");
        for (String code : langCodes.split(",\\s*")) {
          if (!code.isEmpty()) {
            hiddenMatchesLanguages.add(Languages.getLanguageForShortCode(code));
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not load properties from '" + file + "'", e);
    }
  }

  private void setLanguageModelDirectory(String langModelDir) {
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

  /** @since 3.7 */
  int getCacheSize() {
    return cacheSize;
  }

  /** @since 3.7 */
  boolean getWarmUp() {
    return warmUp;
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
