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
import com.optimaize.langdetect.text.*;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Identify the language of a text. Note that some languages might never be
 * detected because they are close to another language. Language variants like
 * en-US or en-GB are not detected, the result will be {@code en} for those.
 * By default, only the first 1000 characters of a text are considered.
 * Email signatures that use {@code \n-- \n} as a delimiter are ignored.
 *
 * @since 2.9
 */
public class LanguageIdentifier {

  private static final Logger logger = LoggerFactory.getLogger(LanguageIdentifier.class);
  private static final double MINIMAL_CONFIDENCE = 0.9;
  private static final int K_HIGHEST_SCORES = 5;
  private static final int SHORT_ALGO_THRESHOLD = 50;
  private static final Pattern SIGNATURE = Pattern.compile("\n-- \n.*", Pattern.DOTALL);

  // ast and gl often prevent the correct detection of Spanish (as the are quite similar
  // to Spanish, I assume) so we disable them for now. See LanguageDetectionEval.java:
  private static final List<String> ignoreLangCodes = Arrays.asList("ast", "gl");

  // languages that we offer profiles for as they are not yet supported by language-detector:
  private static final List<String> externalLangCodes = Arrays.asList("eo");

  private final LanguageDetector languageDetector;
  private final TextObjectFactory textObjectFactory;
  private final int maxLength;

  private boolean fasttextEnabled = false;
  private Process fasttextProcess;
  private BufferedReader fasttextIn;
  private BufferedWriter fasttextOut;

  public LanguageIdentifier() {
    this(1000);
  }

  /**
   * @param maxLength the maximum number of characters that will be considered - can help
   *                  with performance. Don't use values below 100, as this would decrease
   *                  accuracy.
   * @throws IllegalArgumentException if {@code maxLength} is less than 10
   * @since 4.2
   */
  public LanguageIdentifier(int maxLength) {
    if (maxLength < 10) {
      throw new IllegalArgumentException("maxLength must be >= 10 (but values > 100 are recommended): " + maxLength);
    }
    this.maxLength = maxLength;
    try {
      List<LanguageProfile> profiles = loadProfiles(getLanguageCodes());
      languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
        .minimalConfidence(MINIMAL_CONFIDENCE)
        .shortTextAlgorithm(SHORT_ALGO_THRESHOLD)
        .withProfiles(profiles)
        .build();
      textObjectFactory = new TextObjectFactoryBuilder()
        .maxTextLength(10000)
        .withTextFilter(UrlTextFilter.getInstance())
        .withTextFilter(RemoveMinorityScriptsTextFilter.forThreshold(0.3))
        .withTextFilter(new RemoveEMailSignatureFilter())
        .build();
    } catch (IOException e) {
      throw new RuntimeException("Could not set up language identifier", e);
    }
  }

  public void enableFasttext(File fasttextBinary, File fasttextModel) {
    if (fasttextBinary != null && fasttextModel != null) {
      try {
        startFasttext(fasttextModel, fasttextBinary);
        logger.info("Started fasttext process for language identification: Binary " + fasttextBinary + " with model @ " + fasttextModel);
        fasttextEnabled = true;
      } catch (IOException e) {
        fasttextEnabled = false;
        throw new RuntimeException("Could not start fasttext process for language identification @ " + fasttextBinary + " with model @ " + fasttextModel, e);
      }
    }
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
    return detectLanguage(text, Collections.emptyList());
  }
  
  /**
   * @return language or {@code null} if language could not be identified
   * @param noopLangs list of codes that are detected but will lead to the NoopLanguage that has no rules
   * @since 4.4
   */
  @Nullable
  public Language detectLanguage(String text, List<String> noopLangs) {
    String shortText = text.length() > maxLength ? text.substring(0, maxLength) : text;
    shortText = textObjectFactory.forText(shortText).toString();
    String languageCode = null;
    if (fasttextEnabled) {
      try {
        languageCode = getHighestScoringResult(runFasttext(shortText, noopLangs));
      } catch (Exception e) {
        fasttextEnabled = false;
        logger.error("Disabling fasttext language identification, got error for text: " + text, e);
        fasttextProcess.destroy();
      }
    }
    if (!fasttextEnabled) { // no else, value can change in if clause
      languageCode = detectLanguageCode(shortText);
      if (noopLangs.size() > 0) {
        logger.warn("Cannot consider noopLanguages because not in fastText mode: " + noopLangs);
      }
    }
    if (languageCode != null && canLanguageBeDetected(languageCode, noopLangs)) {
      return Languages.getLanguageForShortCode(languageCode, noopLangs);
    } else {
      return null;
    }
  }
  
  private boolean canLanguageBeDetected(String langCode, List<String> additionalLanguageCodes) {
    return Languages.isLanguageSupported(langCode) || additionalLanguageCodes.contains(langCode);
  }

  private void startFasttext(File modelPath, File binaryPath) throws IOException {
    fasttextProcess = new ProcessBuilder(binaryPath.getPath(), "predict-prob", modelPath.getPath(), "-", "" + K_HIGHEST_SCORES).start();
    fasttextIn = new BufferedReader(new InputStreamReader(fasttextProcess.getInputStream(), StandardCharsets.UTF_8));
    fasttextOut = new BufferedWriter(new OutputStreamWriter(fasttextProcess.getOutputStream(), StandardCharsets.UTF_8));
  }

  private String getHighestScoringResult(Map<String, Double> probs) {
    String result = null;
    double max = -1;
    for (Map.Entry<String, Double> entry : probs.entrySet()) {
      if (entry.getValue() > max) {
        max = entry.getValue();
        result = entry.getKey();
      }
    }
    return result;
  }

  private Map<String, Double> runFasttext(String text, List<String> additionalLanguageCodes) throws IOException {
    Map<String, Double> probabilities = new HashMap<>();
    String joined = text.replace("\n", " ");
    String buffer;
    synchronized(this) {
      fasttextOut.write(joined);
      fasttextOut.newLine();
      fasttextOut.flush();
      buffer = fasttextIn.readLine();
    }
    String[] values = buffer.split(" ");
    if (values.length % 2 != 0) {
      throw new RuntimeException("Error while parsing fasttext output: " + buffer);
    }
    for (int i = 0; i < values.length; i += 2) {
      String lang = values[i];
      String langCode = lang.substring(lang.lastIndexOf("__") + 2);
      String prob = values[i + 1];
      Double probValue = Double.parseDouble(prob);
      if (canLanguageBeDetected(langCode, additionalLanguageCodes)) {
        probabilities.put(langCode, probValue);
      }
    }
    return probabilities;
  }

  /**
   * @return language or {@code null} if language could not be identified
   */
  @Nullable
  private String detectLanguageCode(String text) {
    Optional<LdLocale> lang = languageDetector.detect(text);
    // comment in for debugging:
    //System.out.println(languageDetector.getProbabilities(textObject));
    if (lang.isPresent()) {
      return lang.get().getLanguage();
    } else {
      return null;
    }
  }

  class RemoveEMailSignatureFilter implements TextFilter {
    @Override
    public String filter(CharSequence text) {
      return SIGNATURE.matcher(text.toString()).replaceFirst("");
    }
  }
}
