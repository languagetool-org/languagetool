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
package org.languagetool.rules.en;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.languagetool.rules.AbstractWordCoherencyRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordCoherencyDataLoader;

/**
 * English version of {@link AbstractWordCoherencyRule}
 */
public class WordCoherencyRule extends AbstractWordCoherencyRule {

  private static final Map<String, Set<String>> wordMap = new WordCoherencyDataLoader().loadWords("/en/coherency.txt");

  public WordCoherencyRule(ResourceBundle messages) throws IOException {
    super(messages);
    addExamplePair(Example.wrong("He likes archaeology. Really? She likes <marker>archeology</marker>, too."),
                   Example.fixed("He likes archaeology. Really? She likes <marker>archaeology</marker>, too."));
  }

  @Override
  protected Map<String, Set<String>> getWordMap() {
    return wordMap;
  }

  @Override
  protected String getMessage(String word1, String word2) {
    return "Do not mix variants of the same word ('" + word1 + "' and '" + word2 + "') within a single text.";
  }
  
  @Override
  public String getId() {
    return "EN_WORD_COHERENCY";
  }

  @Override
  public String getDescription() {
    return "Coherent spelling of words with two admitted variants.";
  }

}
