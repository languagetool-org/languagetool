/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.spelling.CachingWordListLoader;
import org.languagetool.tools.StringTools;

import gnu.trove.THashMap;
import gnu.trove.THashSet;

/**
 * A rule that matches words which need a specific upper/lowercase spelling.
 * @author Nikos-Antonopoulos, giorgossideris
 */
public abstract class AbstractSpecificCaseRule extends Rule {
  // a map that has as keys the special case phrases into lowercase
  // and as values the special case phrases properly spelled:
  private static final Map<String,String> lcToProperSpelling = new THashMap<>();
  // the phrases that will be detected by the rule:
  private static Set<String> phrases;
  private static int maxLen;
  // used to speed up the server as the phrases are loaded in every initialization
  protected final CachingWordListLoader phrasesListLoader = new CachingWordListLoader();
  
  /**
   * The constructor of the abstract class AbstractSpecificCaseRule
   * @param messages     the messages to apply the rule
   */
  public AbstractSpecificCaseRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.CASING.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    loadPhrases();
  }
  
  /**
   * @return the path to the txt file that contains the phrases for the rule
   */
  public abstract String getPhrasesPath();
  
  /**
   * @return the message that will be shown if the words of the
   *         wrongly capitalized phrase must begin with capital
   */
  public String getInitialCapitalMessage() {
    return "The initials of the particular phrase must be capitals.";
  }
  
  /**
   * @return the message that will be shown if the wrongly capitalized phrase
   *         must not be written with capital initials 
   *         (another special kind of capitalization)
   */
  public String getOtherCapitalizationMessage() { 
    return "The particular expression should follow the suggested capitalization.";
  }
  
  /**
   * @return the short message of the rule
   */
  public String getShortMessage() {
    return "Special capitalization";
  }
  
  /**
   * Initializes the phrases that will be detected from the rule by the given path
   */
  private void loadPhrases() {
    List<String> l = new ArrayList<>();
    List<String> lines = phrasesListLoader.loadWords(getPhrasesPath());
    for (String line : lines) {
      int parts = line.split(" ").length;
      maxLen = Math.max(parts, maxLen);
      l.add(line.trim());
    }
    phrases = new THashSet<>(l);
    initializeLcToProperSpellingMap();
  }

  /**
   * Initializes the <code>lcToProperSpelling</code> Map
   */
  synchronized static private void initializeLcToProperSpellingMap() {
    for (String phrase : phrases) {
      lcToProperSpelling.put(phrase.toLowerCase(), phrase);
    }
  }

  @Override
  public String getId() {
    return "SPECIFIC_CASE";
  }

  @Override
  public String getDescription() {
    return "Checks upper/lower case expressions.";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> matches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    for (int i = 0; i < tokens.length; i++) {
      List<String> l = new ArrayList<>();
      int j = 0;
      while (l.size() < maxLen && i+j < tokens.length) {
        l.add(tokens[i+j].getToken());
        j++;
        String phrase = String.join(" ", l);
        String lcPhrase = phrase.toLowerCase();
        String properSpelling = lcToProperSpelling.get(lcPhrase);
        if (properSpelling != null && !StringTools.isAllUppercase(phrase) && !phrase.equals(properSpelling)) {
          if (i > 0 && tokens[i-1].isSentenceStart() && !StringTools.startsWithUppercase(properSpelling)) {
            // avoid suggesting e.g. "vitamin C" at sentence start:
            continue;
          }
          String msg;
          if (allWordsUppercase(properSpelling)) {
            msg = getInitialCapitalMessage();
          } else {
            msg = getOtherCapitalizationMessage();
          }
          RuleMatch match = new RuleMatch(this, sentence, tokens[i].getStartPos(), 
                                  tokens[i+j-1].getEndPos(), msg, getShortMessage());
          match.setSuggestedReplacement(properSpelling);
          matches.add(match);
        }
      }
    }
    return toRuleMatchArray(matches);
  }

  /**
   * Checks if all the words in the given string begin with a capital letter
   * @param s    the string to check
   * @return     <code>true</code> if all the words within the given string 
   *             begin with capital letter, else <code>false</code>
   */
  private boolean allWordsUppercase(String s) {
    return Arrays.stream(s.split(" ")).allMatch(StringTools::startsWithUppercase);
  }
}
