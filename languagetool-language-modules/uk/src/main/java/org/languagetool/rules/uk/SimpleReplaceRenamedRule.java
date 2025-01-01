/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.uk;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.JLanguageTool;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.tagging.uk.PosTagHelper;

/**
 * A rule that matches proper names that has been renamed
 * Loads the relevant words from <code>rules/uk/replace_renamed.txt</code>.
 * 
 * @author Andriy Rysin
 */
public class SimpleReplaceRenamedRule extends Rule {

  private static final Map<String, List<String>> RENAMED_LIST = ExtraDictionaryLoader.loadLists("/uk/replace_renamed.txt");
  private static final Pattern GEO_POSTAG_PATTERN = Pattern.compile("noun:inanim.*?:prop.*|adj.*");
  private static final URL DECOMUNIZATION_URL = createUrl();

  private static URL createUrl() {
    try {
      return URI.create("https://uk.wikipedia.org/wiki/%D0%A1%D0%BF%D0%B8%D1%81%D0%BE%D0%BA_%D1%82%D0%BE%D0%BF%D0%BE%D0%BD%D1%96%D0%BC%D1%96%D0%B2_%D0%A3%D0%BA%D1%80%D0%B0%D1%97%D0%BD%D0%B8,_%D0%BF%D0%B5%D1%80%D0%B5%D0%B9%D0%BC%D0%B5%D0%BD%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D1%85_%D0%B2%D0%BD%D0%B0%D1%81%D0%BB%D1%96%D0%B4%D0%BE%D0%BA_%D0%B4%D0%B5%D0%BA%D0%BE%D0%BC%D1%83%D0%BD%D1%96%D0%B7%D0%B0%D1%86%D1%96%D1%97").toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public SimpleReplaceRenamedRule(ResourceBundle messages) {
    super(messages);
    setLocQualityIssueType(ITSIssueType.Style);
  }

  @Override
  public final String getId() {
    return "UK_SIMPLE_REPLACE_RENAMED";
  }

  @Override
  public String getDescription() {
    return "Пропозиція поточної назви для перейменованих власних назв";
  }

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (AnalyzedTokenReadings tokenReadings: tokens) {

      LinkedHashSet<String> renamedLemmas = new LinkedHashSet<>();
      for(AnalyzedToken reading: tokenReadings.getReadings()) {
        String lemma = reading.getLemma();
        
        if( JLanguageTool.SENTENCE_END_TAGNAME.equals(reading.getPOSTag()) )
          continue;


        if( lemma != null ) {
          if( RENAMED_LIST.containsKey(lemma)
              && PosTagHelper.hasPosTag(reading, GEO_POSTAG_PATTERN) ) {
            renamedLemmas.add(lemma);
          }
          else {
            // overlaps with normal lemma
            renamedLemmas.clear();
            break;
          }
        }
      }

      if( renamedLemmas.size() > 0 ) {
        String info = "";
        List<String> replacements = new ArrayList<>();
        for(String lemma: renamedLemmas) {
          List<String> repl = RENAMED_LIST.get(lemma);
          replacements.add(repl.get(0));
          
          for(int i=1; i<repl.size()-1; i++) {
            replacements.add(repl.get(i));
          }

          // kinda cheating - getting first explanation we find
          // but usually we'll get a noun and adj for the same name so it's ok
          if( info.isEmpty() && repl.size() > 1 ) {
            info = repl.get(repl.size()-1);
          }
        }

        RuleMatch match = createRuleMatch(tokenReadings, replacements, renamedLemmas.iterator().next(), info, sentence);
        ruleMatches.add(match);
      }

    }
    return ruleMatches.toArray(new RuleMatch[0]);
  }


  private String getMessage(String tokenStr, String info) {
    String msg = "«" + tokenStr + "» було перейменовано";
    if( ! info.isEmpty() ) {
      msg += " (" + info + ")";
    }
    return msg;
  }

  private RuleMatch createRuleMatch(AnalyzedTokenReadings readings, List<String> replacements, String msg, String info, AnalyzedSentence sentence) {
    msg = getMessage(msg, info);
    RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, readings.getStartPos(), readings.getEndPos(), msg, "Перейменована назва");
    potentialRuleMatch.setSuggestedReplacements(replacements);
    if( info.contains("декомуніз") ) {
      potentialRuleMatch.setUrl(DECOMUNIZATION_URL);
    }
    return potentialRuleMatch;
  }

}
