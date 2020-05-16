/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar;

import org.languagetool.rules.AbstractWordCoherencyRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordCoherencyDataLoader;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Arabic version of {@link AbstractWordCoherencyRule}
 *
 * @author Sohaib Afifi
 * @author Taha Zerrouki
 * @since 5.0
 */
public class ArabicWordCoherencyRule extends AbstractWordCoherencyRule {

  public static final String AR_WORD_COHERENCY = "AR_WORD_COHERENCY";

  private static final String FILE_NAME = "/ar/coherency.txt";

  private static final Map<String, Set<String>> wordMap = new WordCoherencyDataLoader().loadWords(FILE_NAME);

  public ArabicWordCoherencyRule(ResourceBundle messages) throws IOException {
    super(messages);
    addExamplePair(Example.wrong("وزارة الشؤون الخارجية تهتم  بكل <marker>شئون</marker> العالم."),
      Example.fixed("وزارة الشؤون الخارجية تهتم  بكل <marker>شؤون</marker> العالم."));
  }

  @Override
  protected Map<String, Set<String>> getWordMap() {
    return wordMap;
  }

  @Override
  protected String getMessage(String word1, String word2) {
    return "تجنب استعمال شكلين للكلمة نفسها ('" + word1 + "' و '" + word2 + "') في  النص نفسه.";
  }

  @Override
  public String getId() {
    return AR_WORD_COHERENCY;
  }

  @Override
  public String getDescription() {
    return "ضبط انسجام التهجئة للكلمات التي تكتب بطرق مختلفة مقبولة.";
  }
}
