/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.eval;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;
import org.languagetool.DetectedLanguage;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.language.identifier.LanguageIdentifier;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Use of a remote (http/https) language detector that returns a JSON.
 */
public class RemoteLangDetect extends LanguageIdentifier {

  private static final JsonFactory factory = new JsonFactory();

  private final String endpoint;

  public RemoteLangDetect(int maxLength, String endpoint) {
    super(maxLength);
    this.endpoint = endpoint;
  }

  @Nullable
  @Override
  public DetectedLanguage detectLanguage(String cleanText, List<String> noopLangsTmp, List<String> preferredLangsTmp) {
    Language detectedLanguage = detectLanguage(cleanText);
    if (detectedLanguage != null) {
      return new DetectedLanguage(null, detectedLanguage);
    } else {
      return null;
    }
  }

  @Nullable
  @Override
  public Language detectLanguage(String text) {
    try {
      String result = getUrl(Tools.getUrl(endpoint), text);
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      JsonNode jsonNode = mapper.readTree(result);
      System.out.println(text + " => " + result);
      String langCode = jsonNode.get("language").asText();
      if (Languages.isLanguageSupported(langCode)) {
        return Languages.getLanguageForShortCode(langCode);
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getUrl(URL url, String text) throws IOException {
    URLConnection connection = url.openConnection();
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setDoOutput(true);
    StringWriter sw = new StringWriter();
    try (Writer writer = new OutputStreamWriter(connection.getOutputStream(), UTF_8)) {
      try (JsonGenerator g = factory.createGenerator(sw)) {
        g.writeStartObject();
        g.writeStringField("text", text);
        g.writeEndObject();
      }
      writer.write(sw.toString());
      writer.flush();
      return StringTools.streamToString(connection.getInputStream(), "UTF-8");
    }
  }

}
