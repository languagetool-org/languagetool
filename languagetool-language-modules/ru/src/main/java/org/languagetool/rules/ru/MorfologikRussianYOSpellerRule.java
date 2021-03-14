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

package org.languagetool.rules.ru;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Example;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;


/**
 * An experimental rule for spell check (only yo variant)(set default off).
 * @author Yakov Reztsov
 * @since 5.0
 */

public final class MorfologikRussianYOSpellerRule extends MorfologikSpellerRule {

  public static final String RULE_ID = "MORFOLOGIK_RULE_RU_RU_YO";

  private static final String RESOURCE_FILENAME = "/ru/hunspell/ru_RU_yo.dict";
  private static final Pattern RUSSIAN_LETTERS = Pattern.compile("[-а-яёА-ЯЁ]*");

  public MorfologikRussianYOSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    setDefaultOff(); 
    addExamplePair(Example.wrong("Все счастливые семьи похожи друг на друга, <marker>каждя</marker> несчастливая семья несчастлива по-своему."),
                   Example.fixed("Все счастливые семьи похожи друг на друга, <marker>каждая</marker> несчастливая семья несчастлива по-своему."));
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  protected boolean ignoreToken(AnalyzedTokenReadings[] tokens, int idx) throws IOException {
    String word = tokens[idx].getToken();  
    // don't check words that don't have  letters
    if (!RUSSIAN_LETTERS.matcher(word).matches()) {
      return true;
    }
      
    List<String> words = new ArrayList<>();
    for (AnalyzedTokenReadings token : tokens) {
      words.add(token.getToken());
    }
    
    return ignoreWord(words, idx);
  }
 
  @Override
  public String getDescription() {
    return "Проверка орфографии. Только «Ё» (экспериментальное правило).";
  }
  
  
}
