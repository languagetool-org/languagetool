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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.languagetool.synthesis.GermanSynthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Expand lines according to their suffix, e.g. {@code foo/S} becomes {@code [foo, foos]}.
 * @since 3.0
 */
public class LineExpander implements org.languagetool.rules.LineExpander {

  private static final LoadingCache<String, List<String>> cache = CacheBuilder.newBuilder()
    .expireAfterAccess(10, TimeUnit.MINUTES)
    .build(new CacheLoader<String, List<String>>() {
      @Override
      public List<String> load(@NotNull String line) {
        List<String> result = new ArrayList<>();
        String[] parts = cleanTagsAndEscapeChars(line).split("_");
        if (parts.length != 2) {
          throw new IllegalArgumentException("Unexpected line format, expected at most one '_': " + line);
        }
        if (parts[0].contains("/") || parts[1].contains("/")) {
          throw new IllegalArgumentException("Unexpected line format, '_' cannot be combined with '/': " + line);
        }
        if (parts[1].equals("in")) {
          // special case for the common gender gap characters:
          result.add(parts[0] + "_in");
          result.add(parts[0] + "_innen");
          result.add(parts[0] + "*in");
          result.add(parts[0] + "*innen");
          result.add(parts[0] + ":in");
          result.add(parts[0] + ":innen");
          //result.add(parts[0] + "in");   // see if we can comment in these cases, too
          //result.add(parts[0] + "innen");
          //result.add(parts[0] + "e");
          //result.add(parts[0] + "en");
        } else {
          try {
            String[] forms = GermanSynthesizer.INSTANCE.synthesizeForPosTags(parts[1], s -> s.startsWith("VER:"));
            if (forms.length == 0) {
              throw new RuntimeException("Could not expand '" + parts[1] + "' from line '" + line + "', no forms found");
            }
            Set<String> formSet = new HashSet<>(Arrays.asList(forms));
            for (String form : formSet) {
              if (!form.contains("ß") && form.length() > 0 && Character.isLowerCase(form.charAt(0))) {
                // skip these, it's too risky to introduce old spellings like "gewußt" from the synthesizer
                result.add(parts[0] + form);
              }
            }
            result.add(parts[0] + "zu" + parts[1]);  //  "zu<verb>" is not part of forms from synthesizer
            result.add(StringTools.uppercaseFirstChar(parts[0]) + parts[1] + "s");  //  Genitiv, e.g. "des Weitergehens"
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
        return result;
      }
    });

  @Override
  public List<String> expandLine(String line) {
    List<String> result = new ArrayList<>();
    if (isLineWithVerbPrefix(line)) {
      handleLineWithPrefix(line, result);
    } else if (isLineWithFlag(line)) {
      handleLineWithFlags(line, result);
    } else {
      result.add(cleanTagsAndEscapeChars(line));
    }
    return result;
  }

  private boolean isLineWithFlag(String line) {
    int idx = line.indexOf('/');
    return !line.startsWith("#") && idx > 0 && line.charAt(idx-1) != '\\';
  }

  private boolean isLineWithVerbPrefix(String line) {
    int idx = line.indexOf('_');
    return !line.startsWith("#") && idx > 0 && line.charAt(idx-1) != '\\';
  }

  private void handleLineWithPrefix(String line, List<String> result) {
    result.addAll(cache.getUnchecked(line));
  }

  private void handleLineWithFlags(String line, List<String> result) {
    String[] parts = cleanTagsAndEscapeChars(line).split("/");
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
  private static String cleanTagsAndEscapeChars(String s) {
    int idx = s.indexOf('#');
    if (idx != -1) {
      s = s.substring(0, idx);
    }
    return s.replaceAll("\\\\", "").trim();
  }
}
