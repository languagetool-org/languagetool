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
import org.languagetool.DetectedLanguage;
import org.languagetool.Language;
import org.languagetool.language.identifier.LanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifierService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ForeignLanguageChecker {

  private static final float ERROR_THRESHOLD = 0.45f;
  private static final int MIN_SENTENCE_THRESHOLD = 3;
  public static final String NO_FOREIGN_LANG_DETECTED = "NO_FOREIGN_LANG_DETECTED";

  private final String languageShortCode;
  private final String sentence;
  private final long sentenceLength;
  private final List<String> preferredLanguages;
  
  public ForeignLanguageChecker(String languageShortCode, String sentence, Long sentenceLength, List<String> preferredLanguages) {
    this.languageShortCode = languageShortCode;
    this.sentence = sentence;
    this.sentenceLength = sentenceLength;
    this.preferredLanguages = Collections.unmodifiableList(preferredLanguages);
  }

  public String check(int matchesSoFar) throws IOException {
    float errorRatio = (float) matchesSoFar / sentenceLength;
    if (sentenceLength >= MIN_SENTENCE_THRESHOLD && errorRatio >= ERROR_THRESHOLD) {
      LanguageIdentifier langIdent = LanguageIdentifierService.INSTANCE.getInitialized();
      if (langIdent != null) {
        DetectedLanguage langDetectResults = langIdent.detectLanguage(sentence, Collections.emptyList(), preferredLanguages);
        //for now, we just use the result if also in preferredLanguages to prevent false positive
        if (langDetectResults != null) {
          Language detectedLanguage = langDetectResults.getDetectedLanguage();
          if (detectedLanguage != null && !detectedLanguage.getShortCode().equals(languageShortCode) && preferredLanguages.contains(detectedLanguage.getShortCode())) {
            //DO NEVER enable traceLevel for this class in production @LanguageTool
            log.trace("Found '{}' sentence in '{}' text: '{}' with confidence {} from source '{}'",
                    detectedLanguage.getShortCode(),
                    languageShortCode,
                    sentence,
                    langDetectResults.getDetectionConfidence(),
                    langDetectResults.getDetectionSource());
            return detectedLanguage.getShortCode();
          } else if (detectedLanguage != null && detectedLanguage.getShortCode().equals(languageShortCode)) {
            return NO_FOREIGN_LANG_DETECTED;
          }
        }
      }
    }
    return null;
  }
}
