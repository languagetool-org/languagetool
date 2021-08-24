/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.ner;

import org.jetbrains.annotations.NotNull;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @since 5.5
 */
public class NERService {

  private static final Logger logger = LoggerFactory.getLogger(NERService.class);

  private final String urlStr;

  public NERService(String urlStr) {
    this.urlStr = urlStr;
  }


  private static String checkAtUrlByPost(URL url, String postData, Map<String, String> properties) throws IOException {
    String keepAlive = System.getProperty("http.keepAlive");
    try {
      System.setProperty("http.keepAlive", "false");  // without this, there's an overhead of about 1 second - not sure why
      URLConnection connection = url.openConnection();
      for (Map.Entry<String, String> entry : properties.entrySet()) {
        connection.setRequestProperty(entry.getKey(), entry.getValue());
      }
      connection.setDoOutput(true);
      try (Writer writer = new OutputStreamWriter(connection.getOutputStream(), UTF_8)) {
        writer.write(postData);
        writer.flush();
        return StringTools.streamToString(connection.getInputStream(), "UTF-8");
      }
    } finally {
      if (keepAlive != null) {
        System.setProperty("http.keepAlive", keepAlive);
      }
    }
  }

  public List<Span> runNER(String text) throws IOException {
    String joined = text.replace("\n", " ");
    String result = checkAtUrlByPost(Tools.getUrl(urlStr), "input=" + joined, new HashMap<>());
    return parseBuffer(result);
  }

  @NotNull
  List<Span> parseBuffer(String buffer) {
    String[] values = buffer.trim().split(" ");
    if (buffer.trim().contains("\n")) {
      logger.warn("Got multiple lines to read from external NER, this should not happen: '" + buffer.trim() + "'" );
    }
    List<Span> res = new ArrayList<>();
    for (String value : values) {
      if (value.isEmpty()) {
        continue;
      }
      int slash3 = getLastSlashFrom(value, value.length()-1);
      int slash2 = getLastSlashFrom(value, slash3-1);
      int slash1 = getLastSlashFrom(value, slash2-1);
      if (slash1 == -1 || slash2 == -1 || slash3 == -1) {
        logger.warn("SLASH NOT FOUND: '" + value + "'");
        continue;
      }
      String tag = value.substring(slash1 + 1, slash2);
      int fromPos = Integer.parseInt(value.substring(slash2 + 1, slash3));
      int toPos = Integer.parseInt(value.substring(slash3 + 1));
      if (tag.equals("PERSON")) {
        res.add(new Span(fromPos, toPos));
      }
    }
    return res;
  }

  private int getLastSlashFrom(String s, int startPos) {
    for (int i = startPos; i >= 0; i--) {
      char ch = s.charAt(i);
      if (ch == '/') {
        return i;
      }
    }
    return -1;
  }

  public static class Span {
    private final int fromPos;
    private final int toPos;
    Span(int fromPos, int toPos) {
      if (fromPos >= toPos) {
        throw new IllegalArgumentException("fromPos must be < toPos: fromPos: " + fromPos + ", toPos: " + toPos);
      }
      this.fromPos = fromPos;
      this.toPos = toPos;
    }
    public int getStart() {
      return fromPos;
    }
    public int getEnd() {
      return toPos;
    }
    @Override
    public String toString() {
      return fromPos + "-" + toPos;
    }
  }
}
