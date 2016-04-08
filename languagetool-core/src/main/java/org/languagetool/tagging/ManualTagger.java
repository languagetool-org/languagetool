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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.languagetool.synthesis.ManualSynthesizer;
import org.languagetool.tools.StringTools;

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

  private final Map<String, List<TaggedWord>> mapping;

  public ManualTagger(InputStream inputStream) throws IOException {
    mapping = loadMapping(inputStream, "utf8");
  }

  private Map<String, List<TaggedWord>> loadMapping(InputStream inputStream, String encoding) throws IOException {
    Map<String, List<TaggedWord>> map = new HashMap<>();
    try (
      InputStreamReader reader = new InputStreamReader(inputStream, encoding);
      BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        if (StringTools.isEmpty(line) || line.charAt(0) == '#') {
          continue;
        }
        String[] parts = line.split("\t");
        if (parts.length != 3) {
          throw new IOException("Unknown line format when loading manual tagger dictionary, expected three tab-separated fields: '" + line + "'");
        }
        List<TaggedWord> terms = map.get(parts[0]);
        if (terms == null) {
          terms = new ArrayList<>();
        }
        terms.add(new TaggedWord(parts[1], parts[2].trim()));
        map.put(parts[0], terms);
      }
    }
    return map;
  }

  /**
   * Look up a word's baseform (lemma) and POS information.
   */
  @Override
  public List<TaggedWord> tag(String word) {
    List<TaggedWord> lookedUpTerms = mapping.get(word);
    if (lookedUpTerms != null) {
      return Collections.unmodifiableList(lookedUpTerms);
    } else {
      return Collections.emptyList();
    }
  }

}
