/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fa;

import org.languagetool.rules.AbstractWordCoherencyRule;
import org.languagetool.rules.WordCoherencyDataLoader;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Persian version of {@link org.languagetool.rules.AbstractWordCoherencyRule}.
 * 
 * @since 2.7
 */
public class WordCoherencyRule extends AbstractWordCoherencyRule {

  private static final Map<String, Set<String>> wordMap = new WordCoherencyDataLoader().loadWords("/fa/coherency.txt");

  public WordCoherencyRule(ResourceBundle messages) throws IOException {
    super(messages);
    // TODO:
    //addExamplePair(Example.wrong("من در <marker>اطاق</marker> تو را دیدم."),
    //               Example.fixed("من در <marker>اتاق</marker> تو را دیدم."));
  }

  @Override
  protected Map<String, Set<String>> getWordMap() {
    return wordMap;
  }

  @Override
  protected String getMessage(String word1, String word2) {
    return "'" + word1 + "' و '"  + word2 + "' نباید در یک جا استفاده شوند";
  }
  
  @Override
  public String getId() {
    return "FA_WORD_COHERENCY";
  }

  @Override
  public String getDescription() {
    return "چند املا برای یک کلمه که یکی از آنها اولویت بیشتری دارد";
  }

}