/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class LanguageAnnotatorTest {
  
  @Test
  public void test() throws IOException {
    Language en = Languages.getLanguageForShortCode("en-US");
    Language de = Languages.getLanguageForShortCode("de-DE");
    LanguageAnnotator annot = new LanguageAnnotator(en, de);
    JLanguageTool lt = new JLanguageTool(en);
    List<AnalyzedSentence> sentences = lt.analyzeText("This is a test, sagte der Engländer.");
    annot.annotateWithLanguage(sentences);
    for (AnalyzedSentence sentence : sentences) {
      for (AnalyzedTokenReadings token : sentence.getTokens()) {
        if (token.getLanguage() != null) {
          System.out.println(token.getLanguage().getShortCode() + " -- " + token.getToken());
        } else {
          System.out.println("// -- " + token.getToken());
        }
      }
    }
    System.out.println(sentences);
  }

}