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
import org.languagetool.JLanguageTool;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 * @since 3.4
 */
final class ServerTools {

  private ServerTools() {
  }

  static String getSQLDatetimeString(Calendar date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
    if (params.get("token") != null) {
      return UserLimits.getLimitsFromToken(config, params.get("token"));
    } else if (params.get("username") != null) {
      if (params.get("apiKey") != null && params.get("password") != null) {
        // TODO: throw exception (but first log to see how often this happens)
        print("WARN: apiKey AND password was set: " + params.get("apiKey"), System.err);
      }
      if (params.get("apiKey") != null) {
        return UserLimits.getLimitsByApiKey(config, params.get("username"), params.get("apiKey"));
      } else if (params.get("password") != null) {
        return UserLimits.getLimitsFromUserAccount(config, params.get("username"), params.get("password"));
      } else {
        throw new IllegalArgumentException("With 'username' set, you also need to specify either 'apiKey' (recommended) or 'password'");
      }
    } else {
      // TODO: throw exception (but first log to see how often this happens)
      if (params.get("apiKey") != null) {
        print("WARN: apiKey was set, but username was not: " + params.get("apiKey"), System.err);
      }
      if (params.get("password") != null) {
        print("WARN: password was set, but username was not", System.err);
      }
      return UserLimits.getDefaultLimits(config);
    }
  }

  @NotNull
  public static JLanguageTool.Mode getMode(Map<String, String> params) {
    JLanguageTool.Mode mode;
    if (params.get("mode") != null) {
      String modeParam = params.get("mode");
      if ("textLevelOnly".equals(modeParam)) {
        mode = JLanguageTool.Mode.TEXTLEVEL_ONLY;
      } else if ("allButTextLevelOnly".equals(modeParam)) {
        mode = JLanguageTool.Mode.ALL_BUT_TEXTLEVEL_ONLY;
      } else if ("all".equals(modeParam)) {
        mode = JLanguageTool.Mode.ALL;
      } else {
        throw new IllegalArgumentException("Mode must be one of 'textLevelOnly', 'allButTextLevelOnly', or 'all' but was: '" + modeParam + "'");
      }
    } else {
      mode = JLanguageTool.Mode.ALL;
    }
    return mode;
  }

}
