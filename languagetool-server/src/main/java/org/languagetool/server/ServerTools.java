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

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @since 3.4
 */
final class ServerTools {

  private ServerTools() {
  }

  static void print(String s) {
    print(s, System.out);
  }

  static void print(String s, PrintStream outputStream) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

}
