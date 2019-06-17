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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
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
  private static final int K_HIGHEST_SCORES = 5;
  private static final int SHORT_ALGO_THRESHOLD = 50;
  // texts shorter than this will *only* consider preferred languages (if set):
  private static final int CONSIDER_ONLY_PREFERRED_THRESHOLD = 50;
  private static final Pattern SIGNATURE = Pattern.compile("\n-- \n.*", Pattern.DOTALL);

  // ast and gl often prevent the correct detection of Spanish (as the are quite similar
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
    DetectedLanguage detectedLanguage = detectLanguage(text, Collections.emptyList(), Collections.emptyList());
    if (detectedLanguage == null) {
      return null;
    }
    return detectedLanguage.getDetectedLanguage();
  }
  
  /**
   * @return language or {@code null} if language could not be identified
   */
  @Nullable
  @Experimental
  DetectedLanguage detectLanguageWithDetails(String text) {
    DetectedLanguage detectedLanguage = detectLanguage(text, Collections.emptyList(), Collections.emptyList());
    if (detectedLanguage == null) {
      return null;
    }
    return detectedLanguage;
  }
  
  /**
   * @return language or {@code null} if language could not be identified
   * @param noopLangsTmp list of codes that are detected but will lead to the NoopLanguage that has no rules
   * @since 4.4 (new parameter noopLangs, changed return type to DetectedLanguage)
   */
  @Nullable
  public DetectedLanguage detectLanguage(String text, List<String> noopLangsTmp, List<String> preferredLangsTmp) {
    Objects.requireNonNull(noopLangsTmp);
    Objects.requireNonNull(preferredLangsTmp);
    // Chrome sends 'nn' (Nynorsk) or 'nb' (Bokmal), but fasttext detects 'no', so we have to map, and 
    // Bokmal seems to be the standard variant:
    List<String> noopLangs = noopLangsTmp.stream().map(k -> k.equals("nb") ? "no" : k).collect(Collectors.toList());
    List<String> preferredLangs = preferredLangsTmp.stream().map(k -> k.equals("nb") ? "no" : k).collect(Collectors.toList());
    if (preferredLangs.stream().anyMatch(k -> k.contains("-"))) {
      throw new IllegalArgumentException("preferredLanguages may only contain language codes without variants (e.g. 'en', but not 'en-US'): " +
        preferredLangs + ". Use 'preferredVariants' to specify variants");
    }
    String shortText = text.length() > maxLength ? text.substring(0, maxLength) : text;
    shortText = textObjectFactory.forText(shortText).toString();
    shortText = shortText.replaceAll("\uFEFF+", " ");  // used by the browser add-on to filter HTML etc. (_ignoreText() in validator.js)
    Map.Entry<String,Double> result = null;
    if (fasttextEnabled) {
      try {
        Map<String, Double> scores = runFasttext(shortText, noopLangs);
        result = getHighestScoringResult(scores);
        if (result.getValue().floatValue() < THRESHOLD) {
          //System.out.println(text + " ->" + result.getValue().floatValue() + " " + result.getKey());
          CommonWords commonWords = new CommonWords();
          Map<Language, Integer> lang2Count = commonWords.getKnownWordsPerLanguage(text);
          //System.out.println("-> "+ lang2Count);
          for (Map.Entry<Language, Integer> entry : lang2Count.entrySet()) {
            String langCode = entry.getKey().getShortCode();
            if (scores.containsKey(langCode)) {
              // this looks arbitrary, but gave best results with evaluation (LanguageDetectionMinLengthEval):
              scores.put(langCode, scores.get(langCode) + Double.valueOf(entry.getValue()));
            } else {
              scores.put(langCode, Double.valueOf(entry.getValue()));
            }
          }
          result = getHighestScoringResult(scores);
        }
        if (text.length() < CONSIDER_ONLY_PREFERRED_THRESHOLD && preferredLangs.size() > 0) {
          //System.out.println("remove? " + preferredLangs + " <-> " + scores);
          scores.keySet().removeIf(k -> !preferredLangs.contains(k));
          //System.out.println("-> " + b + " ==> " + scores);
          result = getHighestScoringResult(scores);
        }
        // Calculate a trivial confidence value because fasttext's confidence is often
        // wrong for short text (e.g. 0.99 for a test that's misclassified). Don't
        // use 1.0 because we can never be totally sure...
        double newScore = 0.99 / (30.0 / Math.min(text.length(), 30));
        //System.out.println("fasttext  : " + result);
        //System.out.println("newScore  : " + newScore);
        result = new AbstractMap.SimpleImmutableEntry<>(result.getKey(), newScore);
      } catch (Exception e) {
        fasttextEnabled = false;
        RuleLoggerMessage msg = new RuleErrorNotification(this.getClass().getSimpleName(), "-",
          String.format("Fasttext disabled, failed on '%s': %s", text, ExceptionUtils.getStackTrace(e)));
        RuleLoggerManager.getInstance().log(msg, Level.WARNING);
        fasttextProcess.destroy();
      }
    }
    if (!fasttextEnabled) { // no else, value can change in if clause
      result = detectLanguageCode(shortText);
      if (noopLangs.size() > 0) {
        logger.warn("Cannot consider noopLanguages because not in fastText mode: " + noopLangs);
      }
    }
    if (result != null && result.getKey() != null && canLanguageBeDetected(result.getKey(), noopLangs)) {
      return new DetectedLanguage(null,
        Languages.getLanguageForShortCode(result.getKey(), noopLangs),
        result.getValue().floatValue());
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

  class RemoveEMailSignatureFilter implements TextFilter {
    @Override
    public String filter(CharSequence text) {
      return SIGNATURE.matcher(text.toString()).replaceFirst("");
    }
  }
}
