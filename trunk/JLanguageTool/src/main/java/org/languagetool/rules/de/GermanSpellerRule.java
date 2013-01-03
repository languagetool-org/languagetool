/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import de.abelssoft.wordtools.jwordsplitter.AbstractWordSplitter;
import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import org.languagetool.Language;
import org.languagetool.rules.spelling.hunspell.CompoundAwareHunspellRule;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class GermanSpellerRule extends CompoundAwareHunspellRule {

  public static final String RULE_ID = "GERMAN_SPELLER_RULE";
  
  private static final int MAX_EDIT_DISTANCE = 2;

  public GermanSpellerRule(ResourceBundle messages, Language language) {
    super(messages, language, getCompoundSplitter(), getSpeller(language));
  }

  @Override
  public String getId() {
    return RULE_ID;
  }
  
  private static AbstractWordSplitter getCompoundSplitter() {
    try {
      final AbstractWordSplitter wordSplitter = new GermanWordSplitter(false);
      wordSplitter.setStrictMode(false); // there's a spelling mistake in (at least) one part, so strict mode wouldn't split the word
      ((GermanWordSplitter)wordSplitter).setMinimumWordLength(3);
      return wordSplitter;
    } catch (IOException e) {
      throw new RuntimeException("Could not set up German compound splitter", e);
    }
  }

  private static MorfologikSpeller getSpeller(Language language) {
    if (!language.getShortName().equals("de")) {
      throw new RuntimeException("Language is not a variant of German: " + language);
    }
    try {
      final String morfoFile = "/de/hunspell/de_" + language.getCountryVariants()[0] + ".dict";
      return new MorfologikSpeller(morfoFile, Locale.getDefault(), MAX_EDIT_DISTANCE);
    } catch (IOException e) {
      throw new RuntimeException("Could not set up morfologik spell checker", e);
    }
  }

}
