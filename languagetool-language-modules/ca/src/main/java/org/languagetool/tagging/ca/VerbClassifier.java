/* LanguageTool, a natural language style checker
 * Copyright (C) 2024 Jaume Ortolà
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
package org.languagetool.tagging.ca;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.JLanguageTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class VerbClassifier {

  private static HashMap<String, Byte> verbClassifierMap;

  private final static String filePath = "/ca/verbs_classification.txt";

  public final static byte vtrCode = 0b00000001;
  public final static byte intrCode = 0b00000010;
  public final static byte pronCode = 0b00000100;

  public VerbClassifier() {

  }

  public static boolean isVerbCode(String lemma, byte codeByte) {
    initVerbClassifier();
    if (verbClassifierMap.containsKey(lemma)) {
      return (verbClassifierMap.get(lemma) & codeByte) != 0;
    }
    return false;
  }

  public static boolean isIntransitive(String lemma) {
    return isVerbCode(lemma, intrCode) && !isVerbCode(lemma, vtrCode);
  }

  private static void initVerbClassifier() {
    if (verbClassifierMap != null) {
      return;
    }
    verbClassifierMap = new HashMap<>();
    try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(filePath);
         BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = StringUtils.substringBefore(line, "#").trim();
        if (line.isEmpty()) {
          continue;
        }
        String[] parts = line.split("=");
        String lemma = parts[0];
        String tags = parts[1];
        //abs, aux, cop, imp, intr, pron, vtr
        byte code = 0;
        if (tags.contains("vtr")) {
          code |= vtrCode;
        }
        if (tags.contains("intr") || tags.contains("abs")) {
          code |= intrCode;
        }
        if (tags.contains("pron")) {
          code |= pronCode;
        }
        verbClassifierMap.put(lemma, code);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

