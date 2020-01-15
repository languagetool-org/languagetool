/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://danielnaber.de)
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
package org.languagetool.rules.spelling;

import morfologik.speller.Speller;
import morfologik.stemming.Dictionary;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A spell checker that's fast but not guaranteed to always agree with the
 * "real" spell checker used in LT. Doesn't offer corrections.
 * Can be used for guessing the language of shorts texts where ngram
 * or AI-based language identification isn't exact enough.
 */
public class VagueSpellChecker {

  private final static Map<Language, Rule> langToRule = new HashMap<>();
  private final static Map<Language, Dictionary> langToDict = new HashMap<>();
  
  public boolean isValidWord(String word, Language lang) {
    Rule rule = langToRule.get(lang);
    if (rule == null) {
      Rule tempRule = getSpellingCheckRule(lang);
      if (tempRule instanceof HunspellRule) {
        rule = tempRule;
      } else if (tempRule instanceof MorfologikSpellerRule) {
        rule = new NonThreadSafeSpellRule(JLanguageTool.getMessageBundle(), lang, null);
      }
      langToRule.put(lang, rule);
    }
    if (rule instanceof NonThreadSafeSpellRule) {
      // indicates a Morfologik-based speller - it's not thread-safe, so re-create Speller in isMisspelled():
      return ((NonThreadSafeSpellRule) rule).isMisspelled(word);
    } else if (rule instanceof HunspellRule) {
      // it's okay to use a cached rule, as hunspell-based "isMisspelled()" is thread-safe
      return !((HunspellRule) rule).isMisspelled(word);
    } else {
      throw new RuntimeException("Unknown rule type for language " + lang.getShortCodeWithCountryAndVariant() + ": " + rule);
    }
  }

  private SpellingCheckRule getSpellingCheckRule(Language lang) {
    JLanguageTool lt = new JLanguageTool(lang);
    SpellingCheckRule spellRule = null;
    for (Rule r : lt.getAllActiveRules()) {
      if (r instanceof HunspellRule || r instanceof MorfologikSpellerRule) {
        spellRule = (SpellingCheckRule) r;
        // TODO: what if there's more than one spell rule?
        break;
      }
    }
    if (spellRule == null) {
      throw new RuntimeException("No spelling rule found for language " + lang.getShortCodeWithCountryAndVariant() +
        " - make sure to set 'preferredVariants' so a variant with a spell checker can be selected");
    }
    return spellRule;
  }

  private class NonThreadSafeSpellRule extends SpellingCheckRule {

    private NonThreadSafeSpellRule(ResourceBundle messages, Language language, UserConfig userConfig) {
      super(messages, language, userConfig);
    }

    @Override
    public String getId() {
      return "FAKE_FOR_VAGUE_SPELL_CHECKER";
    }

    @Override
    public String getDescription() {
      return "internal";
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isMisspelled(String word) {
      try {
        Dictionary dict = langToDict.get(language);  // Dictionary itself is thread-safe, so it can be cached and re-used
        if (dict == null) {
          SpellingCheckRule spellingRule = getSpellingCheckRule(language);
          dict = Dictionary.read(JLanguageTool.getDataBroker().getFromResourceDirAsUrl(((MorfologikSpellerRule)spellingRule).getFileName()));
          langToDict.put(language, dict);
        }
        Speller speller = new Speller(dict, 1);
        return !speller.isMisspelled(word);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
}
