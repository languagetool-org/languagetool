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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @since 5.5
 */
public class ExternalNERViaPipe {

  private static final Logger logger = LoggerFactory.getLogger(ExternalNERViaPipe.class);

  private final BufferedReader nerIn;
  private final Writer nerOut;

  public ExternalNERViaPipe(File cmd) {
    try {
      Process nerProcess = new ProcessBuilder(cmd.getPath()).start();
      nerIn = new BufferedReader(new InputStreamReader(nerProcess.getInputStream(), StandardCharsets.UTF_8));
      nerOut = new OutputStreamWriter(nerProcess.getOutputStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // unit tests only
  ExternalNERViaPipe() {
    nerIn = null;
    nerOut = null;
  }

  public List<Span> runNER(String text) throws IOException {
    String joined = text.replace("\n", " ");
    String line;
    synchronized (this) {
      nerOut.write(joined + System.lineSeparator());
      nerOut.flush();
      line = nerIn.readLine();
      if (line == null) {
        logger.warn("Got null from external NER, this should not happen; NER results might be mixed up");
        return new ArrayList<>();
      }
      if (nerIn.ready()) {
        logger.warn("More input to read from external NER, this should not happen; NER results might be mixed up");
      }
    }
    return parseBuffer(line);
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
