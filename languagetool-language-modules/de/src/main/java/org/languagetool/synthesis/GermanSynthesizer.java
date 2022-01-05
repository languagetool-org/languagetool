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
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * German word form synthesizer. Also supports compounds.
 *
 * @since 2.4
 */
public class GermanSynthesizer extends BaseSynthesizer {
  public static final GermanSynthesizer INSTANCE = new GermanSynthesizer(Languages.getLanguageForShortCode("de-DE"));

  public GermanSynthesizer(Language lang) {
    super("de/de.sor", "/de/german_synth.dict", "/de/german_tags.txt", lang);
  }

  @Override
  protected List<String> lookup(String lemma, String posTag) {
    List<String> lookup = super.lookup(lemma, posTag);
    List<String> results = new ArrayList<>();
    for (String s : lookup) {
      // don't inflect a lowercase lemma to an uppercase word and vice versa
      // https://github.com/languagetool-org/languagetool/issues/4712
      boolean lcLemma = StringTools.startsWithLowercase(lemma);
      boolean lcLookup = StringTools.startsWithLowercase(s);
      if (lcLemma == lcLookup || lemma.equals("mein") || lemma.equals("ich")) {  // mein/ich wegen Ihr/Sie
        results.add(s);
      }
    }
    return results;
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
    List<String> parts = GermanCompoundTokenizer.getStrictInstance().tokenize(token.getLemma());
    if (parts.size() == 0) {
      return parts.toArray(new String[0]);
    }
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
    return results.toArray(new String[0]);
  }

}
