/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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


import org.languagetool.rules.AbstractWordCoherencyRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordCoherencyDataLoader;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * WordRootRepeatRule.
 * This rule detects words in the same sentence with the same root that belong to different parts of speech.
 * @author Yakov Reztsov
 * @since 5.1
 */

public class RussianWordRootRepeatRule extends AbstractWordCoherencyRule {

  private static final Map<String, Set<String>> wordMap = new WordCoherencyDataLoader().loadWords("/ru/wordrootrep.txt");

  public RussianWordRootRepeatRule(ResourceBundle messages) throws IOException {
    super(messages);
    setDefaultOff();
    addExamplePair(Example.wrong("Абрикос рос в саду. У меня на столе стоит <marker>абрикосный</marker> сок."),
                   Example.fixed("Абрикос рос в саду. У меня на столе стоит сок из <marker>абрикосов</marker>."));
  }

  @Override
  protected Map<String, Set<String>> getWordMap() {
    return wordMap;
  }

  @Override
  protected String getMessage(String word1, String word2) {
    return "«" + word1 + "» и «" + word2 + "» – однокоренные слова, их не стоит использовать одновременно";
  }
  
  @Override
  public String getId() {
    return "RU_WORD_ROOT_REPEAT";
  }

  @Override
  public String getDescription() {
    return "Повтор однокоренных слов";
  }

}
