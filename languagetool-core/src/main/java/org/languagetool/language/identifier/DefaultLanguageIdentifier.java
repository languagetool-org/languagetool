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
package org.languagetool.language.identifier;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.RemoveMinorityScriptsTextFilter;
import com.optimaize.langdetect.text.TextObjectFactory;
import com.optimaize.langdetect.text.TextObjectFactoryBuilder;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.languagetool.DetectedLanguage;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.language.identifier.detector.FastTextDetector;
import org.languagetool.language.identifier.detector.NGramDetector;
import org.languagetool.noop.NoopLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Identify the language of a text. Note that some languages might never be
 * detected because they are close to another language. Language variants like
 * en-US or en-GB are not detected, the result will be {@code en} for those.
 * By default, only the first 1000 characters of a text are considered.
 * Email signatures that use {@code \n-- \n} as a delimiter are ignored.
 *
 * @since 2.9
 */
public class DefaultLanguageIdentifier extends LanguageIdentifier {

  private static final Logger logger = LoggerFactory.getLogger(DefaultLanguageIdentifier.class);
  private static final double MINIMAL_CONFIDENCE = 0.9;
  private static final int SHORT_ALGO_THRESHOLD = 50;
  // texts shorter than this will *only* consider preferred languages (if set):
  private static final int CONSIDER_ONLY_PREFERRED_THRESHOLD = 50;

  // ast and gl often prevent the correct detection of Spanish (as they are quite similar
  // to Spanish, I assume) so we disable them for now. See LanguageDetectionEval.java:
  private static final List<String> ignoreLangCodes = Arrays.asList("ast", "gl");

  // languages that we offer profiles for as they are not yet supported by language-detector:
  private static final List<String> externalLangCodes = Arrays.asList("eo");
  // fall back to checking against list of common words if fasttext probability is lower than this:
  private static final float FASTTEXT_CONFIDENCE_THRESHOLD = 0.85f;
  // Result ('Avg. minimum chars') of LanguageDetectionMinLengthEval with MIN_INPUT_LEN=5 and MAX_INPUT_LEN=100,
  // lower values = better:
  //private static final float FASTTEXT_CONFIDENCE_THRESHOLD = 0.7f;    // 8.363
  //private static final float FASTTEXT_CONFIDENCE_THRESHOLD = 0.85f;   // 8.282
  //private static final float FASTTEXT_CONFIDENCE_THRESHOLD = 0.90f;   // 8.271
  //private static final float FASTTEXT_CONFIDENCE_THRESHOLD = 0.95f;   // 8.249
  //private static final float FASTTEXT_CONFIDENCE_THRESHOLD = 1.0f;    // 8.282

  private final LanguageDetector languageDetector;
  private final TextObjectFactory textObjectFactory;

  private FastTextDetector fastTextDetector;
  private AtomicInteger fasttextInitCounter = new AtomicInteger(0);
  private NGramDetector ngram;

  DefaultLanguageIdentifier() {
    this(1000);
  }

  /**
   * @param maxLength the maximum number of characters that will be considered - can help
   *                  with performance. Don't use values below 100, as this would decrease
   *                  accuracy.
   * @throws IllegalArgumentException if {@code maxLength} is less than 10
   * @since 4.2
   */
  DefaultLanguageIdentifier(int maxLength) {
    super(maxLength);
    try {
      List<LanguageProfile> profiles = loadProfiles(getLanguageCodes());
      languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
              .minimalConfidence(MINIMAL_CONFIDENCE)
              .shortTextAlgorithm(SHORT_ALGO_THRESHOLD)
              .withProfiles(profiles)
              .build();
      textObjectFactory = new TextObjectFactoryBuilder()
              .maxTextLength(10000)
              // note: keep these in sync with if(fasttextEnabled) in detectLanguage:
              .withTextFilter(LanguageIdentifier.REMOVE_URL_FILTER)
              .withTextFilter(RemoveMinorityScriptsTextFilter.forThreshold(0.3))
              .withTextFilter(LanguageIdentifier.REMOVE_EMAIL_SIGNATURE_FILTER)
              .withTextFilter(LanguageIdentifier.REMOVE_MENTION_FILTER)
              .withTextFilter(LanguageIdentifier.REMOVE_NON_BREAKING_SPACES_FILTER)
              .build();
    } catch (IOException e) {
      throw new RuntimeException("Could not set up language identifier", e);
    }
  }

  void enableFasttext(File fasttextBinary, File fasttextModel) {
      try {
        fastTextDetector = new FastTextDetector(fasttextModel, fasttextBinary);
        logger.info("Started fasttext process for language identification: Binary {} with model @ {}", fasttextBinary, fasttextModel);
      } catch (IOException e) {
        throw new RuntimeException("Could not start fasttext process for language identification @ " + fasttextBinary + " with model @ " + fasttextModel, e);
      }
  }

  /**
   * For test only
   * @param fastTextDetector
   */
  @TestOnly
  public void setFastTextDetector(FastTextDetector fastTextDetector) {
    this.fastTextDetector = fastTextDetector;
  }

  /**
   * For test only
   * @return a counter how often fasttext was already recreated after a failure
   */
  @TestOnly
  public AtomicInteger getFasttextInitCounter() {
    return fasttextInitCounter;
  }
  
  /**
   * @since 5.2
   */
  public boolean isFastTextEnabled() {
    return fastTextDetector != null;
  }

  void enableNgrams(File ngramDir) {
    if (ngramDir != null) {
      try {
        logger.info("Loading ngram data for language identification from " + ngramDir + "...");
        ngram = new NGramDetector(ngramDir, 50);
        logger.info("Loaded ngram data for language identification from " + ngramDir);
      } catch (IOException e) {
        throw new RuntimeException("Could not load ngram data language identification from " + ngramDir, e);
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
        if (!langCodes.contains(langCode)) {
          langCodes.add(langCode);
        }
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
   * @param cleanText a cleanText as returned by {@link #cleanAndShortenText(String)}
   * @return language or {@code null} if language could not be identified
   */
  @Nullable
  @Override
  public Language detectLanguage(String cleanText) {
    DetectedLanguage detectedLanguage = detectLanguage(cleanText, Collections.emptyList(), Collections.emptyList());
    if (detectedLanguage == null) {
      return null;
    } else {
      return detectedLanguage.getDetectedLanguage();
    }
  }

  /**
   * @param cleanText    a cleanText as returned by {@link #cleanAndShortenText(String)}
   * @param noopLangsTmp list of codes that are detected but will lead to the NoopLanguage that has no rules
   * @return language or {@code null} if language could not be identified
   * @since 4.4 (new parameter noopLangs, changed return type to DetectedLanguage)
   */
  @Override
  public DetectedLanguage detectLanguage(String cleanText, List<String> noopLangsTmp, List<String> preferredLangsTmp) {

    String text = cleanText;
    ParsedLanguageLists parsedLanguageLists = prepareDetectLanguage(text, noopLangsTmp, preferredLangsTmp);
    if (parsedLanguageLists == null) {
      return new DetectedLanguage(null, new NoopLanguage());
    }
    List<String> additionalLangs = parsedLanguageLists.getAdditionalLangs();
    List<String> preferredLangs = parsedLanguageLists.getPreferredLangs();

    Map.Entry<String, Double> result = null;
    boolean fasttextFailed = false;
    String source = "";
    if (fastTextDetector != null || ngram != null) {
      try {
        Map<String, Double> scores;
        boolean usingFastText = false;
        if ((text.length() <= SHORT_ALGO_THRESHOLD || fastTextDetector == null) && ngram != null) {
          scores = ngram.detectLanguages(text.trim(), additionalLangs);
          source += "ngram";
        } else {
          usingFastText = true;
          scores = fastTextDetector.runFasttext(text, additionalLangs);
          source += "fasttext";
        }
        result = getHighestScoringResult(scores);
        /*if (result.getValue().floatValue() < THRESHOLD) {
          System.out.println("FastText below threshold: " + result.getValue().floatValue() + " for " + cleanText.length() + " chars");
        } else {
          System.out.println("FastText above threshold: " + result.getValue().floatValue() + " for " + cleanText.length() + " chars");
        }*/
        if ((usingFastText && result.getValue().floatValue() < FASTTEXT_CONFIDENCE_THRESHOLD) || result.getKey().equals("zz")) {
          //System.out.println(cleanText + " ->" + result.getValue().floatValue() + " " + result.getKey());
          Map<Language, Integer> lang2Count = COMMON_WORDS_LANG_IDENTIFIER.getKnownWordsPerLanguage(text);
          Set<String> baseLangAlreadyHandled = new HashSet<>();
          for (Map.Entry<Language, Integer> entry : lang2Count.entrySet()) {
            String langCode = entry.getKey().getShortCode();
            if (baseLangAlreadyHandled.contains(langCode)) {
              // quick hack to fix #5772
              continue;
            }
            baseLangAlreadyHandled.add(langCode);
            if (scores.containsKey(langCode)) {
              // this looks arbitrary, but gave best results with evaluation (LanguageDetectionMinLengthEval):
              scores.put(langCode, scores.get(langCode) + Double.valueOf(entry.getValue()));
            } else {
              scores.put(langCode, Double.valueOf(entry.getValue()));
            }
          }
          source += "+commonwords";
          result = getHighestScoringResult(scores);
        }
        if (preferredLangs.contains("no") && !preferredLangs.contains("da")) {
          // Special case, as Norwegian easily gets detected as Danish (https://github.com/languagetool-org/languagetool/issues/5520).
          scores.keySet().removeIf(k -> k.equals("da"));
          result = getHighestScoringResult(scores);
        }
        if (text.length() <= CONSIDER_ONLY_PREFERRED_THRESHOLD && preferredLangs.size() > 0) {
          //System.out.println("remove? " + preferredLangs + " <-> " + scores);
          scores.keySet().removeIf(k -> !preferredLangs.contains(k));
          //System.out.println("-> " + b + " ==> " + scores);
          result = getHighestScoringResult(scores);
          source += "+prefLang";
        }
        // Calculate a trivial confidence value because fasttext's confidence is often
        // wrong for short cleanText (e.g. 0.99 for a test that's misclassified). Don't
        // use 1.0 because we can never be totally sure...
        double newScore = 0.99 / (30.0 / Math.min(text.length(), 30));
        //System.out.println("fasttext  : " + result);
        //System.out.println("newScore  : " + newScore);
        result = new AbstractMap.SimpleImmutableEntry<>(result.getKey(), newScore);
      } catch (FastTextDetector.FastTextException e) {
        if (e.isDisabled()) {
          fasttextFailed = true;
          reinitFasttextAfterFailure(e);
        } else {
          logger.error("Fasttext failed, fallback used", e);
          fasttextFailed = true;
        }
      } catch (Exception e) {
        fasttextFailed = true;
        reinitFasttextAfterFailure(e);
      }
    }
    if (fastTextDetector == null && ngram == null || fasttextFailed) { // no else, value can change in if clause
      text = textObjectFactory.forText(text).toString();
      source +="+fallback";
      result = detectLanguageCode(text);
      if (additionalLangs.size() > 0) {
        logger.warn("Cannot consider noopLanguages because not in fastText mode: " + additionalLangs);
      }
    }
    if (result != null && result.getKey() != null && LanguageIdentifierService.INSTANCE.canLanguageBeDetected(result.getKey(), additionalLangs)) {
      return new DetectedLanguage(null,
              Languages.getLanguageForShortCode(result.getKey(), additionalLangs),
              result.getValue().floatValue(), source);
    } else {
      if (preferredLangs.size() > 0 && Languages.isLanguageSupported(preferredLangs.get(0))) {
        source += "+fallbackToPrefLang";
        return new DetectedLanguage(null, Languages.getLanguageForShortCode(preferredLangs.get(0)), 0.1f, source);
      }
      return null;
    }
  }

  private void reinitFasttextAfterFailure(Exception e) {
    if (fastTextDetector != null) {
      int newCounter = fasttextInitCounter.incrementAndGet();
      if (newCounter <= 10) {
        try {
          boolean wasRestarted = fastTextDetector.restartProcess();
          if (wasRestarted) {
            logger.warn("{} Fasttext was new initialized after failure. Remaining initializing: {}", Thread.currentThread().getName(), 10 - newCounter);
          } else {
            fasttextInitCounter.decrementAndGet();
          }
        } catch (IOException ex) {
          logger.warn("Restarting fasttext failed. Remaining initializing: {}", 10 - newCounter);
        }
      } else {
        fastTextDetector = null;
        logger.error("Fasttext finally disabled after too many restarts.", e);
      }
    }
  }

  /**
   * @return language or {@code null} if language could not be identified
   */
  @Nullable
  private Map.Entry<String, Double> detectLanguageCode(String text) {
    List<com.optimaize.langdetect.DetectedLanguage> lang = languageDetector.getProbabilities(text);
    // comment in for debugging:
    //System.out.println(languageDetector.getProbabilities(textObject));
    if (lang.size() > 0) {
      String code = lang.get(0).getLocale().getLanguage();
      double prob = lang.get(0).getProbability();
      return new AbstractMap.SimpleImmutableEntry<>(code, prob);
    } else {
      return null;
    }
  }
}
