/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.languagetool.AnalyzedToken;
import org.languagetool.language.GermanyGerman;
import org.languagetool.synthesis.Synthesizer;

import java.io.IOException;
import java.util.*;

/**
 * Expand lines according to their suffix, e.g. {@code foo/S} becomes {@code [foo, foos]}.
 * @since 3.0
 */
public class LineExpander implements org.languagetool.rules.LineExpander {

  private final static Synthesizer synthesizer = Objects.requireNonNull(new GermanyGerman().getSynthesizer());

  @Override
  public List<String> expandLine(String line) {
    List<String> result = new ArrayList<>();
    if (!line.startsWith("#") && line.contains("_")) {
      handleLineWithPrefix(line, result);
    } else if (!line.startsWith("#") && line.contains("/")) {
      handleLineWithFlags(line, result);
    } else {
      result.add(cleanTags(line));
    }
    return result;
  }

  private void handleLineWithPrefix(String line, List<String> result) {
    String[] parts = cleanTags(line).split("_");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Unexpected line format, expected at most one '_': " + line);
    }
    if (parts[0].contains("/") || parts[1].contains("/")) {
      throw new IllegalArgumentException("Unexpected line format, '_' cannot be combined with '/': " + line);
    }
    try {
      String[] forms = synthesizer.synthesize(new AnalyzedToken(parts[1], "FAKE", parts[1]), "VER:.*", true);
      if (forms.length == 0) {
        throw new RuntimeException("Could not expand '" + parts[1] + "' from line '" + line + "', no forms found");
      }
      Set<String> formSet = new HashSet<>(Arrays.asList(forms));
      for (String form : formSet) {
        if (!form.contains("ß")) {
          // skip these, it's too risky to introduce old spellings like "gewußt" from the synthesizer
          result.add(parts[0] + form);
        }
      }
      result.add(parts[0] + "zu" + parts[1]);  //  "zu<verb>" is not part of forms from synthesizer
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void handleLineWithFlags(String line, List<String> result) {
    String[] parts = cleanTags(line).split("/");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Unexpected line format, expected at most one slash: " + line);
    }
    String word = parts[0];
    String suffix = parts[1];
    for (int i = 0; i < suffix.length(); i++) {
      char c = suffix.charAt(i);
      if (c == 'S') {
        add(result, word);
        result.add(word + "s");
      } else if (c == 'N') {
        add(result, word);
        result.add(word + "n");
      } else if (c == 'E') {
        add(result, word);
        result.add(word + "e");
      } else if (c == 'F') {
        add(result, word);
        result.add(word + "in"); // (m/f)
      } else if (c == 'A') { // Adjektiv
        add(result, word);
        if (word.endsWith("e")) {
          result.add(word + "r");
          result.add(word + "s");
          result.add(word + "n");
          result.add(word + "m");
        } else {
          result.add(word + "e");
          result.add(word + "er");
          result.add(word + "es");
          result.add(word + "en");
          result.add(word + "em");
        }
      } else {
        throw new IllegalArgumentException("Unknown suffix: " + suffix + " in line: " + line);
      }
    }
  }

  private void add(List<String> result, String word) {
    if (!result.contains(word)) {
      result.add(word);
    }
  }

  // ignore "#..." so it can be used as a tag:
  private String cleanTags(String s) {
    int idx = s.indexOf('#');
    if (idx != -1) {
      s = s.substring(0, idx);
    }
    return s.trim();
  }
}
