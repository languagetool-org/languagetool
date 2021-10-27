/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Jaume Ortolà
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
package org.languagetool.rules.fr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.French;
import org.languagetool.rules.AbstractFindSuggestionsFilter;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.fr.MorfologikFrenchSpellerRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.fr.FrenchTagger;

public class FindSuggestionsFilter extends AbstractFindSuggestionsFilter {

  private static MorfologikFrenchSpellerRule morfologikRule;

  public FindSuggestionsFilter() throws IOException {
    if (morfologikRule == null) {
      ResourceBundle messages = JLanguageTool.getDataBroker().getResourceBundle(JLanguageTool.MESSAGE_BUNDLE,
          new Locale("fr"));
      morfologikRule = new MorfologikFrenchSpellerRule(messages, new French(), null, Collections.emptyList());
    }
  }

  @Override
  protected Tagger getTagger() {
    return FrenchTagger.INSTANCE;
  }

  @Override
  protected List<String> getSpellingSuggestions(String w) throws IOException {
    List<String> suggestions = new ArrayList<>();
    List<String> wordsToCheck = new ArrayList<>();
    wordsToCheck.add(w);
    if (w.endsWith("s")) {
      wordsToCheck.add(w.substring(0, w.length() - 1));
    }
    if (w.matches("[aeioué]$")) {
      wordsToCheck.add(w + "s");
    }
    for (String word : wordsToCheck) {
      AnalyzedTokenReadings[] atk = new AnalyzedTokenReadings[1];
      AnalyzedToken token = new AnalyzedToken(word, null, null);
      atk[0] = new AnalyzedTokenReadings(token);
      AnalyzedSentence sentence = new AnalyzedSentence(atk);
      RuleMatch[] matches = morfologikRule.match(sentence);
      if (matches.length > 0) {
        suggestions.addAll(matches[0].getSuggestedReplacements());
      }  
    }
    return suggestions;
  }
  
  @Override
  protected String cleanSuggestion(String s) {
    //remove pronouns before verbs
    String output = s.replaceAll("^[smntl]'|^(nous|vous|le|la|les|me|te|se|leur|en|y) ", "");
    //check only first element 
    output = output.split(" ")[0];
    return output;
  }

}
