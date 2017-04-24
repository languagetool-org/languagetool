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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

  protected boolean verbose = false;
  protected boolean publicAccess = false;
  protected int port = DEFAULT_PORT;
  protected String allowOriginUrl = null;
  protected int maxTextLength = Integer.MAX_VALUE;
  protected long maxCheckTimeMillis = -1;
  protected int maxCheckThreads = 10;
  protected Mode mode;
  protected File languageModelDir = null;
  protected int requestLimit;
  protected int requestLimitPeriodInSeconds;
  protected boolean trustXForwardForHeader;
  protected int maxWorkQueueSize;
  protected File rulesConfigFile = null;
  protected int cacheSize = 0;
  protected boolean warmUp = false;

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
          parseConfigFile(new File(args[++i]), !ArrayUtils.contains(args, LANGUAGE_MODEL_OPTION));
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
          allowOriginUrl = args[++i];
          if (allowOriginUrl.startsWith("--")) {
            throw new IllegalArgumentException("Missing argument for '--allow-origin'");
          }
          break;
        case LANGUAGE_MODEL_OPTION:
          setLanguageModelDirectory(args[++i]);
          break;
      }
    }
  }

  private void parseConfigFile(File file, boolean loadLangModel) {
    try {
      Properties props = new Properties();
      try (FileInputStream fis = new FileInputStream(file)) {
        props.load(fis);
        maxTextLength = Integer.parseInt(getOptionalProperty(props, "maxTextLength", Integer.toString(Integer.MAX_VALUE)));
        maxCheckTimeMillis = Long.parseLong(getOptionalProperty(props, "maxCheckTimeMillis", "-1"));
        requestLimit = Integer.parseInt(getOptionalProperty(props, "requestLimit", "0"));
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
   * @param maxTextLength the maximum text length allowed (in number of characters), texts that are longer
   *                      will cause an exception when being checked
   */
  public void setMaxTextLength(int maxTextLength) {
    this.maxTextLength = maxTextLength;
  }

  int getMaxTextLength() {
    return maxTextLength;
  }

  int getRequestLimit() {
    return requestLimit;
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
