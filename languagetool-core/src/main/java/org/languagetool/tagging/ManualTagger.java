/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.synthesis.ManualSynthesizer;
import org.languagetool.tools.MostlySingularMultiMap;
import org.languagetool.tools.StringTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

/**
 * A tagger that reads the POS information from a plain (UTF-8) text file. This
 * makes it possible for the user to edit the text file to let the system know
 * about new words or missing readings in the *.dict file.
 * 
 * <p>File Format: <tt>fullform baseform postags</tt> (tab separated)
 * 
 * @author Daniel Naber
 * @see ManualSynthesizer
 */
public class ManualTagger implements WordTagger {
  private final MostlySingularMultiMap<String, TaggedWord> mapping;
  private final static String DEFAULT_SEPARATOR = "\t";
  
  public ManualTagger(InputStream inputStream) throws IOException {
    this(inputStream, false);
  }

  public ManualTagger(InputStream inputStream, boolean internTags) throws IOException {
    mapping = new MostlySingularMultiMap<>(loadMapping(inputStream, internTags));
  }

  private static Map<String, List<TaggedWord>> loadMapping(InputStream inputStream, boolean internTags) throws IOException {
    Map<String, List<TaggedWord>> map = new HashMap<>();
    Map<String, String> interned = new HashMap<>();
    try (
      InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      int lineCount = 0;
      String separator = DEFAULT_SEPARATOR;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        lineCount++;
        if (line.startsWith("#separatorRegExp=")) {
          separator = line.replace("#separatorRegExp=", "");
        }
        if (StringTools.isEmpty(line) || line.charAt(0) == '#') {
          continue;
        }
        if (line.contains("\u00A0")) {
          throw new RuntimeException("Non-breaking space found in line #" + lineCount + ": '" + line + "', please remove it");
        }
        line = StringUtils.substringBefore(line, "#").trim();
        String[] parts = line.split(separator);
        if (parts.length != 3) {
          throw new IOException("Unknown line format in line " + lineCount + " when loading manual tagger dictionary, " +
            "expected three tab-separated fields: '" + line + "'");
        }
        String form = parts[0];

        String lemma = parts[1];
        if (lemma.equals(form)) lemma = form;
        lemma = interned.computeIfAbsent(lemma, Function.identity());

        String tag = parts[2].trim();
        String internedTag = internTags ? tag.intern() : interned.computeIfAbsent(tag, Function.identity());
        map.computeIfAbsent(form, __ -> new ArrayList<>()).add(new TaggedWord(lemma, internedTag));
      }
    }
    return map;
  }

  /**
   * Look up a word's baseform (lemma) and POS information.
   */
  @Override
  public List<TaggedWord> tag(String word) {
    List<TaggedWord> lookedUpTerms = mapping.getList(word);
    if (lookedUpTerms != null) {
      return Collections.unmodifiableList(lookedUpTerms);
    } else {
      return Collections.emptyList();
    }
  }

}
