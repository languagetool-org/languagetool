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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sun.net.httpserver.HttpExchange;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Handle requests to {@code /v2/} of the HTTP API. 
 * @since 3.4
 */
class ApiV2 {

  private static final String JSON_CONTENT_TYPE = "application/json";
  private static final String ENCODING = "UTF-8";

  private final TextChecker textChecker;
  private final String allowOriginUrl;
  private final JsonFactory factory = new JsonFactory();

  ApiV2(TextChecker textChecker, String allowOriginUrl) {
    this.textChecker = textChecker;
    this.allowOriginUrl = allowOriginUrl;
  }

  void handleRequest(String path, HttpExchange httpExchange, Map<String, String> parameters) throws Exception {
    if (path.equals("languages")) {
      String response = getLanguages();
      ServerTools.setCommonHeaders(httpExchange, JSON_CONTENT_TYPE, allowOriginUrl);
      httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes(ENCODING).length);
      httpExchange.getResponseBody().write(response.getBytes(ENCODING));
    } else if (path.equals("check")) {
      textChecker.checkText(parameters.get("text"), httpExchange, parameters);
    } else if (path.equals("log")) {
      // used so the client (especially the browser add-ons) can report internal issues:
      String message = parameters.get("message");
      if (message.length() > 250) {
        message = message.substring(0, 250) + "...";
      }
      ServerTools.print("Log message from client: " + message + " - User-Agent: " + httpExchange.getRequestHeaders().getFirst("User-Agent"));
      String response = "OK";
      httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes(ENCODING).length);
      httpExchange.getResponseBody().write(response.getBytes(ENCODING));
    } else {
      throw new RuntimeException("Unsupported action: '" + path + "'");
    }
  }

  String getLanguages() throws IOException {
    StringWriter sw = new StringWriter();
    try (JsonGenerator g = factory.createGenerator(sw)) {
      g.writeStartArray();
      List<Language> languages = new ArrayList<>(Languages.get());
      Collections.sort(languages, (o1, o2) -> o1.getName().compareTo(o2.getName()));
      for (Language lang : languages) {
        g.writeStartObject();
        g.writeStringField("name", lang.getName());
        g.writeStringField("code", lang.getShortCode());
        g.writeStringField("longCode", lang.getShortCodeWithCountryAndVariant());
        g.writeEndObject();
      }
      g.writeEndArray();
    }
    return sw.toString();
  }

}
