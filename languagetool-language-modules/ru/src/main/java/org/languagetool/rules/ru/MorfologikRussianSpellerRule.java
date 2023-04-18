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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Arrays;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Example;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.rules.SuggestedReplacement;


public final class MorfologikRussianSpellerRule extends MorfologikSpellerRule {

  public static final String RULE_ID = "MORFOLOGIK_RULE_RU_RU";

  private static final String RESOURCE_FILENAME = "/ru/hunspell/ru_RU.dict";
  private static final Pattern RUSSIAN_LETTERS = Pattern.compile("[-а-яёо́а́е́у́и́ы́э́ю́я́о̀а̀ѐу̀ѝы̀э̀ю̀я̀ʼА-ЯЁ]*");
  private static final int DEFAULT_MIN_RU_VALUE = 0;

  private int conf_ru_Value = DEFAULT_MIN_RU_VALUE;

  private final static Set <String> lcDoNotSuggestWords = new HashSet <> (Arrays.asList(
    // words with 'NOSUGGEST' flag:
    "блоггер","дрочим","анальный","орочем"
 ));

  public MorfologikRussianSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    addExamplePair(Example.wrong("Все счастливые семьи похожи друг на друга, <marker>каждя</marker> несчастливая семья несчастлива по-своему."),
                   Example.fixed("Все счастливые семьи похожи друг на друга, <marker>каждая</marker> несчастливая семья несчастлива по-своему."));

    if (userConfig != null) {
      int confValue = userConfig.getConfigValueByID(getId());
      if(confValue >= 0) {
        this.conf_ru_Value = confValue;
      }
    }

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
  protected List<SuggestedReplacement> filterNoSuggestWords(List<SuggestedReplacement> l) {
    return l.stream().filter(k -> !lcDoNotSuggestWords.contains(k.getReplacement().toLowerCase())).collect(Collectors.toList());
  }

  @Override
  protected boolean ignoreToken(AnalyzedTokenReadings[] tokens, int idx) throws IOException {
    String word = tokens[idx].getToken();  
    // don't check words that don't have  letters
    if ((conf_ru_Value != 1) && !RUSSIAN_LETTERS.matcher(word).matches()) {
      return true;
    }
      
    List<String> words = new ArrayList<>();
    for (AnalyzedTokenReadings token : tokens) {
      words.add(token.getToken());
    }
    
    return ignoreWord(words, idx);
  }
  
  @Override
  protected boolean isLatinScript() {
    return false;
  }
  
  
  @Override
  public int getDefaultValue() {
    return DEFAULT_MIN_RU_VALUE;
  }

  @Override
  public boolean hasConfigurableValue() {
    return true;
  }

  @Override
  public int getMinConfigurableValue() {
    return 0;
  }

  @Override
  public int getMaxConfigurableValue() {
    return 1;
  }
  
    @Override
  public String getConfigureText() {
    return  "Проверять слова на латинице, только термины (0/1)";
  }
}
