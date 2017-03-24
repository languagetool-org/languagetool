/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.server;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.util.*;

/**
 * Provides example sentences from the XML rules.
 */
class ExampleSentenceProvider {

  private final int minSentences;
  private final int maxSentences;
  private final Random rnd = new Random(12345);
  private final Map<Language, List<ExampleSentence>> languageToExamples = new HashMap<>();

  ExampleSentenceProvider(int minSentences, int maxSentences) {
    if (minSentences > maxSentences) {
      throw new IllegalArgumentException("min > max: " + minSentences + " > " + maxSentences);
    }
    this.minSentences = minSentences;
    this.maxSentences = maxSentences;
    for (Language language : Languages.get()) {
      try {
        initExampleSentences(language);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void initExampleSentences(Language language) throws IOException {
    JLanguageTool lt = new JLanguageTool(language);
    List<Rule> rules = lt.getAllActiveRules();
    List<ExampleSentence> sentences = new ArrayList<>();
    for (Rule rule : rules) {
      if (rule instanceof AbstractPatternRule && !rule.isDefaultOff()) {
        List<IncorrectExample> incorrectExamples = rule.getIncorrectExamples();
        for (IncorrectExample incorrectExample : incorrectExamples) {
          ExampleSentence sentence = new ExampleSentence(incorrectExample.getExample(), rule.getId());
          sentences.add(sentence);
        }
      }
    }
    languageToExamples.put(language, sentences);
  }

  List<ExampleSentence> getRandomSentences(Language lang) {
    List<ExampleSentence> sentences = new ArrayList<>(languageToExamples.get(lang));
    int sentenceCount = rnd.nextInt(Math.max(1, maxSentences - minSentences)) + minSentences;
    Collections.shuffle(sentences, rnd);
    return sentences.subList(0, Math.min(sentences.size(), sentenceCount));
  }

}
