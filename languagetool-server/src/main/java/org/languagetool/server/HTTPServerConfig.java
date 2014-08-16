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

import org.languagetool.Language;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @since 2.0
 */
public class HTTPServerConfig {

  enum Mode { LanguageTool, AfterTheDeadline }

  public static final String DEFAULT_HOST = "localhost";

  /** The default port on which the server is running (8081). */
  public static final int DEFAULT_PORT = 8081;

  protected boolean verbose = false;
  protected boolean publicAccess = false;
  protected int port = DEFAULT_PORT;
  protected String allowOriginUrl = null;
  protected int maxTextLength = Integer.MAX_VALUE;
  protected long maxCheckTimeMillis = -1;
  protected Mode mode;
  protected Language atdLanguage;
  protected File languageModelDir = null;

  public HTTPServerConfig() {
    this.port = DEFAULT_PORT;
    this.verbose = false;
    this.publicAccess = false;
  }

  /**
   * @param serverPort the port to bind to
   * @param verbose when set to <tt>true</tt>, the input text will be logged in case there is an exception
   */
  public HTTPServerConfig(int serverPort, boolean verbose) {
    this.port = serverPort;
    this.verbose = verbose;
    this.publicAccess = false;
  }

  /**
   * Parse command line options.
   */
  HTTPServerConfig(String[] args) {
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "--config":
          parseConfigFile(new File(args[++i]));
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
          break;
      }
    }
  }

  private void parseConfigFile(File file) {
    try {
      final Properties props = new Properties();
      try (FileInputStream fis = new FileInputStream(file)) {
        props.load(fis);
        maxTextLength = Integer.parseInt(getOptionalProperty(props, "maxTextLength", Integer.toString(Integer.MAX_VALUE)));
        maxCheckTimeMillis = Long.parseLong(getOptionalProperty(props, "maxCheckTimeMillis", "-1"));
        String langModel = getOptionalProperty(props, "languageModel", null);
        if (langModel != null) {
          languageModelDir = new File(langModel);
          if (!languageModelDir.exists() || !languageModelDir.isDirectory()) {
            throw new RuntimeException("LanguageModel directory not found or is not a directory: " + languageModelDir);
          }
        }
        mode = getOptionalProperty(props, "mode", "LanguageTool").equalsIgnoreCase("AfterTheDeadline") ? Mode.AfterTheDeadline : Mode.LanguageTool;
        if (mode == Mode.AfterTheDeadline) {
          atdLanguage = Language.getLanguageForShortName(getProperty(props, "afterTheDeadlineLanguage", file));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not load properties from '" + file + "'", e);
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
   * URL of server whose visitors may request data via Ajax, or {@code *} (= anyone) or {@code null} (= no support for CORS).
   */
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

  /** @since 2.7 */
  File getLanguageModelDir() {
    return languageModelDir;
  }

  /** @since 2.7 */
  Mode getMode() {
    return mode;
  }

  /** @since 2.7 */
  Language getAfterTheDeadlineLanguage() {
    return atdLanguage;
  }

  /**
   * @throws IllegalConfigurationException if property is not set 
   */
  protected String getProperty(Properties props, String propertyName, File config) {
    final String propertyValue = (String)props.get(propertyName);
    if (propertyValue == null || propertyValue.trim().isEmpty()) {
      throw new IllegalConfigurationException("Property '" + propertyName + "' must be set in " + config);
    }
    return propertyValue;
  }

  protected String getOptionalProperty(Properties props, String propertyName, String defaultValue) {
    final String propertyValue = (String)props.get(propertyName);
    if (propertyValue == null) {
      return defaultValue;
    }
    return propertyValue;
  }

}
