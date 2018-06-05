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
package org.languagetool.language;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Identify the language of a text. Note that some languages might never be
 * detected because they are close to another language. Language variants like
 * en-US or en-GB are not detected, the result will be {@code en} for those.
 * By default, only the first 1000 characters of a text are considered.
 * @since 2.9
 */
public class LanguageIdentifier {

  private static final double MINIMAL_CONFIDENCE = 0.9;

  // ast and gl often prevent the correct detection of Spanish (as the are quite similar
  // to Spanish, I assume) so we disable them for now. See LanguageDetectionEval.java:
  private static final List<String> ignoreLangCodes = Arrays.asList("ast", "gl");

  // languages that we offer profiles for as they are not yet supported by language-detector:
  private static final List<String> externalLangCodes = Arrays.asList("eo");

  private final LanguageDetector languageDetector;
  private final TextObjectFactory textObjectFactory;
  private final int maxLength;

  public LanguageIdentifier() {
    this(1000);
  }

  /**
   * @param maxLength the maximum number of characters that will be considered - can help
   *                  with performance. Don't use values below 100, as this would decrease
   *                  accuracy.
   * @throws IllegalArgumentException if {@code maxLength} is < 10
   * @since 4.2
   */
  public LanguageIdentifier(int maxLength) {
    try {
      List<LanguageProfile> profiles = loadProfiles(getLanguageCodes());
      languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
              .minimalConfidence(MINIMAL_CONFIDENCE)
              .withProfiles(profiles)
              .build();
      textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
    } catch (IOException e) {
      throw new RuntimeException("Could not set up language identifier", e);
    }
    if (maxLength < 10) {
      throw new IllegalArgumentException("maxLength must be >= 10 (but values > 100 are recommended): " + maxLength);
    }
    this.maxLength = maxLength;
  }

  private static List<String> getLanguageCodes() {
    List<String> langCodes = new ArrayList<>();
    for (Language lang : Languages.get()) {
      String langCode = lang.getShortCode();
      boolean ignore = lang.isVariant() || ignoreLangCodes.contains(langCode) || externalLangCodes.contains(langCode);
      if (ignore) {
        continue;
      }
      if ("zh".equals(langCode)) {
        langCodes.add("zh-CN");
        langCodes.add("zh-TW");
      } else {
        langCodes.add(langCode);
      }
    }
    return langCodes;
  }

  private List<LanguageProfile> loadProfiles(List<String> langCodes) throws IOException {
    LanguageProfileReader profileReader = new LanguageProfileReader();
    List<LanguageProfile> profiles = profileReader.read(langCodes);
    for (String externalLangCode : externalLangCodes) {
      String profilePath = "/" + externalLangCode + "/" + externalLangCode + ".profile";
      if (JLanguageTool.getDataBroker().resourceExists(profilePath)) {  // not all languages are always available
        try (InputStream profile = JLanguageTool.getDataBroker().getFromResourceDirAsStream(profilePath)) {
          profiles.add(new LanguageProfileReader().read(profile));
        }
      }
    }
    return profiles;
  }

  /**
   * @return language or {@code null} if language could not be identified
   */
  @Nullable
  public Language detectLanguage(String text) {
    String languageCode = detectLanguageCode(text);
    if (languageCode != null) {
      return Languages.getLanguageForShortCode(languageCode);
    } else {
      return null;
    }
  }

  /**
   * @return language or {@code null} if language could not be identified
   */
  @Nullable
  private String detectLanguageCode(String text) {
    String shortText = text.length() > maxLength ? text.substring(0, maxLength) : text;
    TextObject textObject = textObjectFactory.forText(shortText);
    Optional<LdLocale> lang = languageDetector.detect(textObject);
    // comment in for debugging:
    //System.out.println(languageDetector.getProbabilities(textObject));
    if (lang.isPresent()) {
      return lang.get().getLanguage();
    } else {
      return null;
    }
  }

}
