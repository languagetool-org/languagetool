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
 * Russian version of {@link AbstractWordCoherencyRule}.
 *
 * @author Yakov Reztsov
 * @since 3.6
 */
public class RussianWordCoherencyRule extends AbstractWordCoherencyRule {

  private static final Map<String, Set<String>> wordMap = new WordCoherencyDataLoader().loadWords("/ru/coherency.txt");

  public RussianWordCoherencyRule(ResourceBundle messages) throws IOException {
    super(messages);
    addExamplePair(Example.wrong("Понятие «оффлайн» тоже имеет английские корни и связано со словом «offline», что означает «вне сети». Принтер перешёл в состояние <marker>офлайн</marker>."),
                   Example.fixed("Понятие «оффлайн» тоже имеет английские корни и связано со словом «offline», что означает «вне сети». Принтер перешёл в состояние <marker>оффлайн</marker>."));
  }

  @Override
  protected Map<String, Set<String>> getWordMap() {
    return wordMap;
  }

  @Override
  protected String getMessage(String word1, String word2) {
    return "'" + word1 + "' и '" + word2 + "' не следует использовать одновременно";
  }
  
  @Override
  public String getId() {
    return "RU_WORD_COHERENCY";
  }

  @Override
  public String getDescription() {
    return "Единообразное написание слов с более чем одним допустимым написанием";
  }

}
