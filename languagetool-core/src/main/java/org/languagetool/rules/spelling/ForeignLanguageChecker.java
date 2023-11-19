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
import org.jetbrains.annotations.NotNull;
import org.languagetool.DetectedLanguage;
import org.languagetool.Language;
import org.languagetool.language.identifier.LanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifierService;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ForeignLanguageChecker {

  private static final float ERROR_THRESHOLD = 0.45f;
  private static final int MIN_SENTENCE_THRESHOLD = 3;
  private static final int MAX_SCORING_LANGUAGES = 5;
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

  @NotNull
  public Map<String, Float> check(int matchesSoFar) throws IOException {
    float errorRatio = (float) matchesSoFar / sentenceLength;
    if (sentenceLength >= MIN_SENTENCE_THRESHOLD && errorRatio >= ERROR_THRESHOLD) {
      LanguageIdentifier langIdent = LanguageIdentifierService.INSTANCE.getInitialized();
      if (langIdent != null) {
        //for now, we just use the result if also in preferredLanguages to prevent false positive
        List<DetectedLanguage> detectedLanguageScores = langIdent.getDetectedLanguageScores(sentence, Collections.emptyList(), preferredLanguages, true, MAX_SCORING_LANGUAGES);
        Map<String, Float> results = new LinkedHashMap<>(MAX_SCORING_LANGUAGES);
        if (!detectedLanguageScores.isEmpty()) {
          for (int i = 0; i < detectedLanguageScores.size(); i++) {
            DetectedLanguage detectedLanguage = detectedLanguageScores.get(i);
            Language language = detectedLanguage.getDetectedLanguage();
            //The text main language still has the highest threshold
            if (i == 0 && language.getShortCode().equals(languageShortCode)) {
              return Collections.singletonMap(NO_FOREIGN_LANG_DETECTED, 0.99f);
            }
            //DO NEVER enable traceLevel for this class in production @LanguageTool
            log.trace("Found '{}' sentence in '{}' text: '{}' with confidence {} from source '{}'",
              language.getShortCode(),
              languageShortCode,
              sentence,
              detectedLanguage.getDetectionConfidence(),
              detectedLanguage.getDetectionSource());
            results.put(language.getShortCode(), detectedLanguage.getDetectionConfidence());
          }
          return results;
        } else {
          return Collections.singletonMap(NO_FOREIGN_LANG_DETECTED, 0.99f);
        }
      }
    }
    return Collections.emptyMap();
  }
}
