/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.nl;

import org.languagetool.*;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import static org.languagetool.JLanguageTool.getDataBroker;

public final class MorfologikDutchSpellerRule extends MorfologikSpellerRule {

  private final static CompoundAcceptor compoundAcceptor = new CompoundAcceptor();
  private final String englishDictFilepath;
  protected MorfologikMultiSpeller englishSpeller;
  private final UserConfig userConfig;

  public MorfologikDutchSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig) throws IOException {
    this(messages, language, userConfig, Collections.emptyList());
  }
  
  public MorfologikDutchSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    englishDictFilepath = "/en/hunspell/en_US" + JLanguageTool.DICTIONARY_FILENAME_EXTENSION;
    this.userConfig = userConfig;
    initEnglishSpeller();
  }

  @Override
  protected boolean ignorePotentiallyMisspelledWord(String word) throws IOException {
    return compoundAcceptor.acceptCompound(word);
  }

  @Override
  public String getFileName() {
    return "/nl/spelling/nl_NL.dict";
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_NL_NL";
  }

  @Override
  protected String getIgnoreFileName() {
    return "/nl/spelling/ignore.txt";
  }

  @Override
  public String getSpellingFileName() {
    return "/nl/spelling/spelling.txt";
  }

  @Override
  protected String getProhibitFileName() {
    return "/nl/spelling/prohibit.txt";
  }

  private void initEnglishSpeller() throws IOException {
    List<String> plainTextDicts = new ArrayList<>();
    String languageVariantPlainTextDict = null;
    if (getDataBroker().resourceExists(getSpellingFileName())) {
      plainTextDicts.add(getSpellingFileName());
    }
    for (String fileName : getAdditionalSpellingFileNames()) {
      if (getDataBroker().resourceExists(fileName)) {
        plainTextDicts.add(fileName);
      }
    }
    if (getLanguageVariantSpellingFileName() != null && getDataBroker().resourceExists(getLanguageVariantSpellingFileName())) {
      languageVariantPlainTextDict = getLanguageVariantSpellingFileName();
    }
    englishSpeller = new MorfologikMultiSpeller(englishDictFilepath, plainTextDicts, languageVariantPlainTextDict,
            userConfig, 1, language);
    setConvertsCase(englishSpeller.convertsCase());
  }

  @Override
  public List<RuleMatch> getRuleMatches(String word, int startPos, AnalyzedSentence sentence,
    List<RuleMatch> ruleMatchesSoFar, int idx,
    AnalyzedTokenReadings[] tokens) throws IOException {
    if (tokens[idx].hasPosTag("_FOREIGN_ENGLISH")) {
      System.out.println(word + " is assumed English");
      return Collections.emptyList();
      /* next step: offer suggestions
      if (englishSpeller.isMisspelled(word)) {
        String message = "Het lijkt erop dat dit een foutief gespeld Engels woord is.";
        String shortMessage = "Mogelijke fout in Engels woord";
        List<String> suggestions = englishSpeller.getSuggestions(word);
        RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, startPos + word.length(),
          message, shortMessage, suggestions);
        return Collections.singletonList(ruleMatch);
      } else {
        return Collections.emptyList();
      }
      */
    }
    return super.getRuleMatches(word, startPos, sentence, ruleMatchesSoFar, idx, tokens);
  }

}
