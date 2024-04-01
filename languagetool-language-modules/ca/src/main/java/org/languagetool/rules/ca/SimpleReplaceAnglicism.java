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
package org.languagetool.rules.ca;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Catalan;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A rule that matches words which should not be used and suggests correct ones
 * instead.
 * <p>
 * Loads the relevant words from <code>rules/ca/replace_anglicism.txt</code>.
 *
 * @author Jaume Ortolà
 */
public class SimpleReplaceAnglicism extends AbstractSimpleReplaceRule2 {

  private static final String FILE_NAME = "/ca/replace_anglicism.txt";
  private static final Locale CA_LOCALE = new Locale("ca");

  public SimpleReplaceAnglicism(final ResourceBundle messages) throws IOException {
    super(messages, new Catalan());
    setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    useSubRuleSpecificIds();
  }

  @Override
  public final String getId() {
    return "CA_SIMPLE_REPLACE_ANGLICISM";
  }

  @Override
  public String getDescription() {
    return "Anglicismes innecessaris: $match";
  }

  @Override
  public String getShort() {
    return "Anglicisme innecessari";
  }

  @Override
  public Locale getLocale() {
    return CA_LOCALE;
  }

  @Override
  public List<String> getFileNames() {
    return Arrays.asList(FILE_NAME);
  }

  @Override
  public String getMessage() {
    return "Anglicisme innecessari. Considereu fer servir una altra paraula.";
  }

  //private List<String> possibleExceptions = Arrays.asList("link", "links", "event", "events");

  @Override
  protected boolean isRuleMatchException(RuleMatch ruleMatch) {
    // accept English words in English sentences
    int startIndex = 0;
    AnalyzedTokenReadings[] tokens = ruleMatch.getSentence().getTokensWithoutWhitespace();
    while (startIndex < tokens.length && tokens[startIndex].getStartPos() < ruleMatch.getFromPos()) {
      startIndex++;
    }
    int endIndex = startIndex;
    while (endIndex < tokens.length && tokens[endIndex].getEndPos() < ruleMatch.getToPos()) {
      endIndex++;
    }
    if (startIndex > 1 && tokens[startIndex].hasPosTag("_english_ignore_")
      && tokens[startIndex - 1].hasPosTag("_english_ignore_")) {
      return true;
    }
    if (endIndex + 1 < tokens.length && tokens[endIndex].hasPosTag("_english_ignore_")
      && tokens[endIndex + 1].hasPosTag("_english_ignore_")) {
      return true;
    }
    return false;
  }

  @Override
  protected boolean isTokenException(AnalyzedTokenReadings atr) {
    // proper nouns tagged in multiwords are exceptions
    return (atr.hasPosTagStartingWith("NP") && atr.getToken().length()>1) || atr.isImmunized() || atr.isIgnoredBySpeller();
  }

}