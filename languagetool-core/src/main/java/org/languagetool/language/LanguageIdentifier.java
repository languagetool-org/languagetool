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
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Identify the language of a text. Note that some languages might never be
 * detected because they are close to another language. Language variants like
 * en-US or en-GB are not detected, the result will be {@code en} for those.
 *
 * @since 2.9
 */
public class LanguageIdentifier {

  private static final double MINIMAL_CONFIDENCE = 0.9;

  // ast and gl often prevent the correct detection of Spanish (as the are quite similar
  // to Spanish, I assume) so we disable them for now. See LanguageDetectionEval.java:
  private static final List<String> ignoreLangNames = Arrays.asList("ast", "gl");

  // languages that we offer profiles for as they are not yet supported by language-detector:
  private static final List<String> externalLangNames = Arrays.asList("km", "eo");

  private final LanguageDetector languageDetector;
  private final TextObjectFactory textObjectFactory;

  public LanguageIdentifier() {
    this(getLanguageNames());
  }

  LanguageIdentifier(List<String> langNames) {
    try {
      List<LanguageProfile> profiles = loadProfiles(langNames);
      languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
              .minimalConfidence(MINIMAL_CONFIDENCE)
              .withProfiles(profiles)
              .build();
      textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
    } catch (IOException e) {
      throw new RuntimeException("Could not set up language identifier", e);
    }
  }

  private static List<String> getLanguageNames() {
    List<String> langNames = new ArrayList<>();
    for (Language lang : Language.REAL_LANGUAGES) {
      String langCode = lang.getShortName();
      boolean ignore = lang.isVariant() || ignoreLangNames.contains(langCode) || externalLangNames.contains(langCode);
      if (ignore) {
        continue;
      }
      if ("zh".equals(langCode)) {
        langNames.add("zh-cn");
        langNames.add("zh-tw");
      } else {
        langNames.add(langCode);
      }
    }
    return langNames;
  }

  private List<LanguageProfile> loadProfiles(List<String> langNames) throws IOException {
    LanguageProfileReader profileReader = new LanguageProfileReader();
    List<LanguageProfile> profiles = profileReader.read(langNames);
    for (String externalLangName : externalLangNames) {
      if (langNames.contains(externalLangName)) {
        String profilePath = "/" + externalLangName + "/" + externalLangName + ".profile";
        InputStream profile = JLanguageTool.getDataBroker().getFromResourceDirAsStream(profilePath);
        profiles.add(new LanguageProfileReader().read(profile));
      }
    }
    return profiles;
  }

  /**
   * @return language or {@code null} if language could not be identified
   */
  public Language detectLanguage(String text) {
    String languageCode = detectLanguageCode(text);
    if (languageCode != null) {
      return Language.getLanguageForShortName(languageCode);
    } else {
      return null;
    }
  }

  /**
   * @return language or {@code null} if language could not be identified
   */
  String detectLanguageCode(String text) {
    TextObject textObject = textObjectFactory.forText(text);
    Optional<String> lang = languageDetector.detect(textObject);
    if (lang.isPresent()) {
      if ("zh-cn".equals(lang.get()) || "zh-tw".equals(lang.get())) {
        return "zh";
      }
      return lang.get();
    } else {
      return null;
    }
  }

}
