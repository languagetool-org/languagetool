/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.languagetool.JLanguageTool.*;

/**
 * @since 3.4
 */
final class ServerTools {

  private final static Pattern sentContentPattern = Pattern.compile("<sentcontent>.*</sentcontent>", Pattern.DOTALL);
  private static final Logger logger = LoggerFactory.getLogger(ServerTools.class);

  private ServerTools() {
  }

  @NotNull
  static String getLoggingInfo(String remoteAddress, Exception e, int errorCode, HttpExchange httpExchange, Map<String, String> params, long runtimeMillis, RequestCounter reqCounter) {
    String message = "";
    if (e != null) {
      message += "An error has occurred: '" + ServerTools.cleanUserTextFromMessage(e.getMessage(), params) + "', sending HTTP code " + errorCode + ". ";
    }
    message += "Access from " + remoteAddress + ", ";
    message += "HTTP user agent: " + getHttpUserAgent(httpExchange) + ", ";
    message += "User agent param: " + params.get("useragent") + ", ";
    if (params.get("v") != null) {
      message += "v: " + params.get("v") + ", ";
    }
    message += "Referrer: " + getHttpReferrer(httpExchange) + ", ";
    message += "language: " + params.get("language") + ", ";
    message += "h: " + reqCounter.getHandleCount() + ", ";
    message += "r: " + reqCounter.getRequestCount() + ", ";
    if (params.get("username") != null) {
      message += "user: " + params.get("username") + ", ";
    }
    if (params.get("apiKey") != null) {
      message += "apiKey: " + params.get("apiKey") + ", ";
    }
    if (params.get("tokenV2") != null) {
      message += "tokenV2: " + params.get("tokenV2") + ", ";
    }
    message += "time: " + runtimeMillis;
    return message;
  }

  @Nullable
  static String getHttpUserAgent(HttpExchange httpExchange) {
    return httpExchange.getRequestHeaders().getFirst("User-Agent");
  }
  
  @Nullable
  static String getHttpReferrer(HttpExchange httpExchange) {
    return httpExchange.getRequestHeaders().getFirst("Referer");
  }
  
  static String getSQLDatetimeString(Calendar date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    return dateFormat.format(date.getTime());
  }

  static String getSQLDateString(Calendar date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    return dateFormat.format(date.getTime());
  }

  static void print(String s) {
    print(s, System.out);
  }

  /* replace with structured logging:
  check done
  cache stats

  maybe: (could be combined in table)
  Access denied: request size / rate limit / ...
  more interesting:
  error rate too high
  text checking took longer than ...

  misc.:
  language code unknown
  missing arguments
  old api
  blacklisted referrer
  various other exceptions
   */

  static void print(String s, PrintStream outputStream) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZ");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    String now = dateFormat.format(new Date());
    outputStream.println(now + " " + s);
  }

  static void setCommonHeaders(HttpExchange httpExchange, String contentType, String allowOriginUrl) {
    httpExchange.getResponseHeaders().set("Content-Type", contentType);
    setAllowOrigin(httpExchange, allowOriginUrl);
  }

  static void setAllowOrigin(HttpExchange httpExchange, String allowOriginUrl) {
    if (allowOriginUrl != null) {
      httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", allowOriginUrl);
    }
  }

  static UserLimits getUserLimits(Map<String, String> params, HTTPServerConfig config) {
    if (params.get("username") != null) {
      if (params.get("apiKey") != null && params.get("password") != null) {
        throw new BadRequestException("apiKey AND password was set, set only apiKey");
      }
      if (params.get("apiKey") != null) {
        return UserLimits.getLimitsByApiKey(config, params.get("username"), params.get("apiKey"));
      } else if (params.get("password") != null) {
        return UserLimits.getLimitsFromUserAccount(config, params.get("username"), params.get("password"));
      } else if (params.get("tokenV2") != null) {
        return UserLimits.getLimitsByAddonToken(config, params.get("username"), params.get("tokenV2"));
      } else {
        throw new BadRequestException("With 'username' set, you also need to specify 'apiKey'");
      }
    } else {
      if (params.get("apiKey") != null) {
        throw new BadRequestException("apiKey was set, but username was not: " + params.get("apiKey"));
      }
      if (params.get("password") != null) {
        throw new BadRequestException("password was set, but username was not");
      }
      return UserLimits.getDefaultLimits(config);
    }
  }

  @NotNull
  static Mode getMode(Map<String, String> params) {
    Mode mode;
    if (params.get("mode") != null) {
      String modeParam = params.get("mode");
      if ("textLevelOnly".equals(modeParam)) {
        mode = Mode.TEXTLEVEL_ONLY;
      } else if ("allButTextLevelOnly".equals(modeParam)) {
        mode = Mode.ALL_BUT_TEXTLEVEL_ONLY;
      } else if ("all".equals(modeParam)) {
        mode = Mode.ALL;
      } else if ("batch".equals(modeParam)) {
        // used in undocumented API for /words/add, /words/delete; ignore
        mode = Mode.ALL;
      } else {
        throw new BadRequestException("Mode must be one of 'textLevelOnly', 'allButTextLevelOnly', or 'all' but was: '" + modeParam + "'");
      }
    } else {
      mode = Mode.ALL;
    }
    return mode;
  }

  @NotNull
  static String getModeForLog(Mode mode) {
    switch (mode) {
      case TEXTLEVEL_ONLY: return "tlo";
      case ALL_BUT_TEXTLEVEL_ONLY: return "!tlo";
      case ALL: return "all";
      default: return "?";
    }
  }

  @NotNull
  static Level getLevel(Map<String, String> params) {
    Level level;
    if (params.get("level") != null) {
      String param = params.get("level");
      if ("default".equals(param)) {
        level = Level.DEFAULT;
      } else if ("picky".equals(param)) {
        level = Level.PICKY;
      } else if ("academic".equals(param)) {
        level = Level.ACADEMIC;
      } else if ("clarity".equals(param)) {
        level = Level.CLARITY;
      } else if ("professional".equals(param)) {
        level = Level.PROFESSIONAL;
      } else if ("creative".equals(param)) {
        level = Level.CREATIVE;
      } else if ("customer".equals(param)) {
        level = Level.CUSTOMER;
      } else if ("jobapp".equals(param)) {
        level = Level.JOBAPP;
      } else if ("objective".equals(param)) {
        level = Level.OBJECTIVE;
      } else if ("elegant".equals(param)) {
        level = Level.ELEGANT;
      } else {
        throw new BadRequestException("Unknown value '" + param + "' for parameter 'level'. Valid values: " +
          Arrays.stream(Level.values()).map(k -> k.toString().toLowerCase()).collect(Collectors.joining(", ")));
      }
    } else {
      level = Level.DEFAULT;
    }
    return level;
  }

  /**
   * Remove user-content from message in case parameters require increased privacy.
   * @since 5.0
   */
  public static String cleanUserTextFromMessage(String s, Map<String, String> params) {
    if (params.getOrDefault("inputLogging", "").equals("no")) {
      return sentContentPattern.matcher(s).replaceAll("<< content removed >>");
    }
    return s;
  }
}
