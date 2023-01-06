/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2023.  Stefan Viol (https://stevio.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool.rules.spelling;

import lombok.extern.slf4j.Slf4j;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.language.identifier.LanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifierService;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class ForeignLanguageChecker {
  private final Language language;
  private final AnalyzedSentence sentence;
  private final long sentenceLength;

  public ForeignLanguageChecker(Language language, AnalyzedSentence sentence) {
    this.language = language;
    this.sentence = sentence;
    this.sentenceLength = Arrays.stream(sentence.getTokensWithoutWhitespace()).filter(k -> !k.isNonWord()).count() - 1;  // -1 for the SENT_START token
  }

  public String check(int matchesSoFar) throws IOException {
    long timeStart = System.currentTimeMillis();
    float errorRatio = (float) matchesSoFar / this.sentenceLength;
    if (this.sentenceLength >= 3 || errorRatio >= 0.45) {
      LanguageIdentifier langIdent = LanguageIdentifierService.INSTANCE.getInitialized();
      if (langIdent != null) {
        Language detectLanguage = langIdent.detectLanguage(sentence.getText());
        if (detectLanguage != null && !detectLanguage.getShortCode().equals(this.language.getShortCode())) {
          long timeEnd = System.currentTimeMillis();
          log.debug("Time to find the correct language of the sentence: {} seconds", (timeEnd - timeStart) / 1000f);
          log.debug("Found {} sentence in {} text: {}", detectLanguage.getShortCode(), this.language.getShortCode(), sentence.getText());
          return detectLanguage.getShortCode();
        }
      }
    }
    long timeEnd = System.currentTimeMillis();
    log.debug("Time without find a other language in sentence {} seconds", (timeEnd - timeStart) / 1000f);
    return null;
  }
}
