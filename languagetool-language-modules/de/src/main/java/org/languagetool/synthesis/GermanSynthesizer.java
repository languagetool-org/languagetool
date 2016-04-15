/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.synthesis;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedToken;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

/**
 * German word form synthesizer. Also supports compounds.
 *
 * @since 2.4
 */
public class GermanSynthesizer extends BaseSynthesizer {

  private final GermanCompoundTokenizer splitter;
  
  public GermanSynthesizer() {
    super("/de/german_synth.dict", "/de/german_tags.txt");
    try {
      splitter = new GermanCompoundTokenizer();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String[] synthesize(AnalyzedToken token, String posTag) throws IOException {
    String[] result = super.synthesize(token, posTag);
    if (result.length == 0) {
      return getCompoundForms(token, posTag, false);
    }
    return result;
  }
  
  @Override
  public String[] synthesize(AnalyzedToken token, String posTag, boolean posTagRegExp) throws IOException {
    String[] result = super.synthesize(token, posTag, posTagRegExp);
    if (result.length == 0) {
      return getCompoundForms(token, posTag, posTagRegExp);
    }
    return result;
  }

  @NotNull
  private String[] getCompoundForms(AnalyzedToken token, String posTag, boolean posTagRegExp) throws IOException {
    List<String> parts = splitter.tokenize(token.getToken());
    String firstPart = String.join("", parts.subList(0, parts.size() - 1));
    String lastPart = StringTools.uppercaseFirstChar(parts.get(parts.size() - 1));
    AnalyzedToken lastPartToken = new AnalyzedToken(lastPart, posTag, lastPart);
    String[] lastPartForms;
    if (posTagRegExp) {
      lastPartForms = super.synthesize(lastPartToken, posTag, true);
    } else {
      lastPartForms = super.synthesize(lastPartToken, posTag);
    }
    Set<String> results = new LinkedHashSet<>();  // avoid dupes
    for (String part : lastPartForms) {
      results.add(firstPart + StringTools.lowercaseFirstChar(part));
    }
    return results.toArray(new String[results.size()]);
  }

}
