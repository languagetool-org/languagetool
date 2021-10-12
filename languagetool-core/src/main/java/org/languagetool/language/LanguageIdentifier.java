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

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.*;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.noop.NoopLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
  private static final int SHORT_ALGO_THRESHOLD = 50;
  // texts shorter than this will *only* consider preferred languages (if set):
  private static final int CONSIDER_ONLY_PREFERRED_THRESHOLD = 50;

  // ast and gl often prevent the correct detection of Spanish (as they are quite similar
  // to Spanish, I assume) so we disable them for now. See LanguageDetectionEval.java:
  private static final List<String> ignoreLangCodes = Arrays.asList("ast", "gl");

  // languages that we offer profiles for as they are not yet supported by language-detector:
  private static final List<String> externalLangCodes = Arrays.asList("eo");
  // fall back to checking against list of common words if fasttext probability is lower than this:
  private static final float THRESHOLD = 0.9f;      // 7.656
  //private static final float THRESHOLD = 0.95f;   // 7.39
  //private static final float THRESHOLD = 0.975f;  // 7.228 
  //private static final float THRESHOLD = 1.0f;    // 7.0

  private final LanguageDetector languageDetector;
  private final TextObjectFactory textObjectFactory;
  private final int maxLength;
  private final UnicodeBasedLangIdentifier unicodeIdentifier = new UnicodeBasedLangIdentifier();

  private FastText fastText;
  private NGramLangIdentifier ngram;

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
        // note: keep these in sync with if(fasttextEnabled) in detectLanguage:
        .withTextFilter(ImprovedUrlTextFilter.getInstance())
        .withTextFilter(RemoveMinorityScriptsTextFilter.forThreshold(0.3))
        .withTextFilter(new RemoveEMailSignatureFilter())
        .withTextFilter(new RemoveMentionFilter())
        .withTextFilter(new RemoveNonBreakingSpaces())
        .build();
    } catch (IOException e) {
      throw new RuntimeException("Could not set up language identifier", e);
    }
  }

  public void enableFasttext(File fasttextBinary, File fasttextModel) {
    if (fasttextBinary != null && fasttextModel != null) {
      try {
        fastText = new FastText(fasttextModel, fasttextBinary);
        logger.info("Started fasttext process for language identification: Binary " + fasttextBinary + " with model @ " + fasttextModel);
      } catch (IOException e) {
        throw new RuntimeException("Could not start fasttext process for language identification @ " + fasttextBinary + " with model @ " + fasttextModel, e);
      }
    }
  }

  /** @since 5.2 */
  public boolean isFastTextEnabled() {
    return fastText != null;
  }

  public void enableNgrams(File ngramDir) {
    try {
      logger.info("Loading ngram data for language identification from " + ngramDir + "...");
      ngram = new NGramLangIdentifier(ngramDir, 50);
      logger.info("Loaded ngram data for language identification from " + ngramDir);
    } catch (IOException e) {
      throw new RuntimeException("Could not load ngram data language identification from " + ngramDir, e);
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
   * @since 5.4
   */
  public String cleanAndShortenText(String text) {
    String shortText = text.length() > maxLength ? text.substring(0, maxLength) : text;
    shortText = shortText.replaceAll("\uFEFF+", " ");  // used by the browser add-on to filter HTML etc. (_ignoreText() in validator.js)
    if (fastText != null || ngram != null) {
      // do *not* use TextObjectFactory because of https://github.com/languagetool-org/languagetool/issues/1278
      // (using it for optimaize is okay, assuming the same strong normalization was applied during training):
      shortText = ImprovedUrlTextFilter.getInstance().filter(shortText);
      shortText = new RemoveEMailSignatureFilter().filter(shortText);
      shortText = new RemoveMentionFilter().filter(shortText);
      shortText = new RemoveNonBreakingSpaces().filter(shortText);
    }
    return shortText;
  }

  /**
   * @param cleanText a cleanText as returned by {@link #cleanAndShortenText(String)}
   * @return language or {@code null} if language could not be identified
   */
  @Nullable
  public Language detectLanguage(String cleanText) {
    DetectedLanguage detectedLanguage = detectLanguage(cleanText, Collections.emptyList(), Collections.emptyList());
    if (detectedLanguage == null) {
      return null;
    }
    return detectedLanguage.getDetectedLanguage();
  }

  /**
   * @param cleanText a cleanText as returned by {@link #cleanAndShortenText(String)}
   * @return language or {@code null} if language could not be identified
   */
  @Nullable
  @Experimental
  DetectedLanguage detectLanguageWithDetails(String cleanText) {
    return detectLanguage(cleanText, Collections.emptyList(), Collections.emptyList());
  }
  
  /**
   * @return language or {@code null} if language could not be identified
   * @param cleanText a cleanText as returned by {@link #cleanAndShortenText(String)}
   * @param noopLangsTmp list of codes that are detected but will lead to the NoopLanguage that has no rules
   * @since 4.4 (new parameter noopLangs, changed return type to DetectedLanguage)
   */
  @Nullable
  public DetectedLanguage detectLanguage(String cleanText, List<String> noopLangsTmp, List<String> preferredLangsTmp) {
    Objects.requireNonNull(noopLangsTmp);
    Objects.requireNonNull(preferredLangsTmp);
    // Chrome sends 'nn' (Nynorsk) or 'nb' (Bokmal), but fasttext detects 'no', so we have to map, and 
    // Bokmal seems to be the standard variant:
    List<String> additionalLangs = noopLangsTmp.stream().map(k -> k.equals("nb") ? "no" : k).collect(Collectors.toList());
    List<String> preferredLangs = preferredLangsTmp.stream().map(k -> k.equals("nb") ? "no" : k).collect(Collectors.toCollection(ArrayList::new));
    if (preferredLangs.stream().anyMatch(k -> k.contains("-"))) {
      throw new IllegalArgumentException("preferredLanguages may only contain language codes without variants (e.g. 'en', but not 'en-US'): " +
        preferredLangs + ". Use 'preferredVariants' to specify variants.");
    }
    List<String> domLangCodes = unicodeIdentifier.getDominantLangCodes(cleanText);
    String domLangStr = String.join(",", domLangCodes);
    if (domLangStr.equals("th") || domLangStr.equals("he") || domLangStr.equals("ko") || domLangStr.equals("hi,mr")) {
      // more than 50% of characters are ..., so assume we don't support this cleanText:
      return new DetectedLanguage(null, new NoopLanguage());
    }
    if (!preferredLangs.contains("ru") && !preferredLangs.contains("uk") && !preferredLangs.contains("be") && !preferredLangs.contains("zh") &&
        !preferredLangs.contains("hi") && !preferredLangs.contains("mr")) {
      // Cyrillic and Chinese are so different from Latin characters that we try to detect it even with preferredLangs not properly set:
      preferredLangs.addAll(domLangCodes);
      additionalLangs.addAll(domLangCodes);
    }
    Map.Entry<String,Double> result = null;
    boolean fasttextFailed = false;
    if (fastText != null || ngram != null) {
      try {
        Map<String, Double> scores;
        boolean usingFastText = false;
        if ((cleanText.length() <= SHORT_ALGO_THRESHOLD || fastText == null) && ngram != null) {
          scores = ngram.detectLanguages(cleanText.trim(), additionalLangs);
        } else {
          usingFastText = true;
          scores = fastText.runFasttext(cleanText, additionalLangs);
        }
        result = getHighestScoringResult(scores);
        /*if (result.getValue().floatValue() < THRESHOLD) {
          System.out.println("FastText below threshold: " + result.getValue().floatValue() + " for " + cleanText.length() + " chars");
        } else {
          System.out.println("FastText above threshold: " + result.getValue().floatValue() + " for " + cleanText.length() + " chars");
        }*/
        if ((usingFastText && result.getValue().floatValue() < THRESHOLD) || result.getKey().equals("zz")) {
          //System.out.println(cleanText + " ->" + result.getValue().floatValue() + " " + result.getKey());
          CommonWords commonWords = new CommonWords();
          Map<Language, Integer> lang2Count = commonWords.getKnownWordsPerLanguage(cleanText);
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
          result = getHighestScoringResult(scores);
        }
        if (preferredLangs.contains("no") && !preferredLangs.contains("da")) {
          // Special case, as Norwegian easily gets detected as Danish (https://github.com/languagetool-org/languagetool/issues/5520).
          scores.keySet().removeIf(k -> k.equals("da"));
          result = getHighestScoringResult(scores);
        }
        if (cleanText.length() < CONSIDER_ONLY_PREFERRED_THRESHOLD && preferredLangs.size() > 0) {
          //System.out.println("remove? " + preferredLangs + " <-> " + scores);
          scores.keySet().removeIf(k -> !preferredLangs.contains(k));
          //System.out.println("-> " + b + " ==> " + scores);
          result = getHighestScoringResult(scores);
        }
        // Calculate a trivial confidence value because fasttext's confidence is often
        // wrong for short cleanText (e.g. 0.99 for a test that's misclassified). Don't
        // use 1.0 because we can never be totally sure...
        double newScore = 0.99 / (30.0 / Math.min(cleanText.length(), 30));
        //System.out.println("fasttext  : " + result);
        //System.out.println("newScore  : " + newScore);
        result = new AbstractMap.SimpleImmutableEntry<>(result.getKey(), newScore);
      } catch(FastText.FastTextException e) {
        if (e.isDisabled()) {
          fastText = null;
          logger.error("Fasttext disabled", e);
        } else {
          logger.error("Fasttext failed, fallback used", e);
          fasttextFailed = true;
        }
      } catch (Exception e) {
        //fastText.destroy();
        fastText = null;
        logger.error("Fasttext disabled", e);
      }
    }
    if (fastText == null && ngram == null || fasttextFailed) { // no else, value can change in if clause
      cleanText = textObjectFactory.forText(cleanText).toString();
      result = detectLanguageCode(cleanText);
      if (additionalLangs.size() > 0) {
        logger.warn("Cannot consider noopLanguages because not in fastText mode: " + additionalLangs);
      }
    }
    if (result != null && result.getKey() != null && canLanguageBeDetected(result.getKey(), additionalLangs)) {
      return new DetectedLanguage(null,
        Languages.getLanguageForShortCode(result.getKey(), additionalLangs),
        result.getValue().floatValue());
    } else {
      return null;
    }
  }
  
  static boolean canLanguageBeDetected(String langCode, List<String> additionalLanguageCodes) {
    return Languages.isLanguageSupported(langCode) || additionalLanguageCodes.contains(langCode);
  }

  private Map.Entry<String, Double> getHighestScoringResult(Map<String, Double> probs) {
    String result = null;
    double max = -1;
    for (Map.Entry<String, Double> entry : probs.entrySet()) {
      if (entry.getValue() > max) {
        max = entry.getValue();
        result = entry.getKey();
      }
    }
    return new AbstractMap.SimpleImmutableEntry<>(result, max);
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

  static class RemoveEMailSignatureFilter implements TextFilter {
    private static final Pattern SIGNATURE = Pattern.compile("\n-- \n.*", Pattern.DOTALL);
    @Override
    public String filter(CharSequence text) {
      return SIGNATURE.matcher(text.toString()).replaceFirst("");
    }
  }

  static class RemoveMentionFilter implements TextFilter {
    private static final Pattern MENTION = Pattern.compile("@[A-Za-z0-9_]+");
    @Override
    public String filter(CharSequence text) {
      return MENTION.matcher(text.toString()).replaceFirst("");
    }
  }

  static class RemoveNonBreakingSpaces implements TextFilter {
    @Override
    public String filter(CharSequence text) {
      return text.toString().replace('\u00A0', ' ');
    }
  }

  static class ImprovedUrlTextFilter implements TextFilter {
    private static final Pattern URL_REGEX = Pattern.compile("https?://[-_.?&~;+=/#%0-9A-Za-z]+");   // '%' has been added
    private static final Pattern MAIL_REGEX = Pattern.compile("[-_.0-9A-Za-z]+@[-_0-9A-Za-z]+[-_.0-9A-Za-z]+");
    private static final ImprovedUrlTextFilter INSTANCE = new ImprovedUrlTextFilter();
    static ImprovedUrlTextFilter getInstance() {
      return INSTANCE;
    }
    @Override
    public String filter(CharSequence text) {
      String modified = URL_REGEX.matcher(text).replaceAll(" ");
      return MAIL_REGEX.matcher(modified).replaceAll(" ");
    }
  }

}
