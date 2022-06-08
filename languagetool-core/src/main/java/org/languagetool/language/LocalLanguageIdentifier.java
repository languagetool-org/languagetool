package org.languagetool.language;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.languagetool.DetectedLanguage;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.noop.NoopLanguage;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.*;

@Slf4j
public class LocalLanguageIdentifier extends LanguageIdentifier {

    private final Map<String, SpellingCheckRule> spellingCheckRules = new HashMap<>();

    public LocalLanguageIdentifier() {
        List<Language> languages = Languages.get();
        for (Language language : languages) {
            if (language.isVariant() || language.getShortCode().equals("zz")) {
                continue;
            }
            boolean hasVariant = language.hasVariant();
            Language spellingRuleLanguage = null;
            if (hasVariant) {
                spellingRuleLanguage = language.getDefaultLanguageVariant();
            }
            if (spellingRuleLanguage == null) {
                spellingRuleLanguage = language;
            }
            SpellingCheckRule defaultSpellingCheckRule = spellingRuleLanguage.getDefaultSpellingRule(JLanguageTool.getDataBroker().getResourceBundle(JLanguageTool.MESSAGE_BUNDLE, new Locale(spellingRuleLanguage.getShortCodeWithCountryAndVariant())));
            if (defaultSpellingCheckRule != null) {
                spellingCheckRules.put(language.getShortCode(), defaultSpellingCheckRule);
            } else {
                log.warn("Could not found default speller rule for {}", language.getShortCode());
            }
        }
    }

    public LocalLanguageIdentifier(List<String> preferredLangCodes) {
        log.info("Init SpellcheckLangIdentifier with {}", preferredLangCodes);
        preferredLangCodes.forEach(langCode -> {
            Language language = Languages.getLanguageForShortCode(langCode);
            SpellingCheckRule defaultSpellingRule = language.getDefaultSpellingRule(JLanguageTool.getDataBroker().getResourceBundle(JLanguageTool.MESSAGE_BUNDLE, new Locale(langCode)));
            if (defaultSpellingRule != null) {
                spellingCheckRules.put(language.getShortCode(), defaultSpellingRule);
            }
        });
    }

    @Nullable
    @Override
    public DetectedLanguage detectLanguage(String cleanText, List<String> noopLangsTmp, List<String> preferredLangsTmp) {
        Pair<List<String>, List<String>> langPair = prepareDetectLanguage(cleanText, noopLangsTmp, preferredLangsTmp);
        if (langPair == null) {
            return new DetectedLanguage(null, new NoopLanguage());
        }
        List<String> additionalLangs = langPair.getLeft();
        List<String> preferredLangs = langPair.getRight();

        String[] words = cleanText.split(" ");
        List<String> dominantLangCodes = UNICODE_BASED_LANG_IDENTIFIER.getDominantLangCodes(cleanText);
        Map<String, Double> scores = new HashMap<>();
        String detectionSource = "spellchecker";
        for (Map.Entry<String, SpellingCheckRule> scp : spellingCheckRules.entrySet()) {
            if (dominantLangCodes.contains(scp.getKey()) ^ (dominantLangCodes.isEmpty() && !NON_LATIN_CHARS_LANGUAGES.contains(scp.getKey()))) {
                double errors = 0;
                for (String word : words) {
                    try {
                        errors += scp.getValue().isMisspelled(word) ? 1 : 0;
                    } catch (IOException ex) {
                        log.error("Got {}", ex.getMessage());
                    }
                }
                double errorRate = errors / (double) words.length;
                log.info("Found {} errors for {} words with {} spellchecker this scores in an error rate of {}", errors, words.length, scp.getKey(), errorRate);
                scores.put(scp.getKey(), 1.0 - errorRate);
            }
        }
        if (scores.isEmpty()) {
            scores.put("zz", 1.0);
        }
        log.info("Got scores: {}", scores);
        //Check if languages have the same highest score
        double maxValue = 0;
        int countFullScore = 0;
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            if (entry.getValue() > maxValue) {
                countFullScore = 1;
                maxValue = entry.getValue();
            } else if (entry.getValue() == maxValue) {
                countFullScore++;
            }
        }
        Map.Entry<String, Double> highestScoringResult = getHighestScoringResult(scores);
        if (highestScoringResult.getValue() < SCORE_THRESHOLD || highestScoringResult.getKey().equals("zz") || countFullScore > 1) {
            Map<Language, Integer> lang2Count = COMMON_WORDS_LANG_IDENTIFIER.getKnownWordsPerLanguage(cleanText);
            Set<String> baseLangAlreadyHandled = new HashSet<>();
            for (Map.Entry<Language, Integer> entry : lang2Count.entrySet()) {
                String langCode = entry.getKey().getShortCode();
                if (baseLangAlreadyHandled.contains(langCode)) {
                    continue;
                }
                baseLangAlreadyHandled.add(langCode);
                if (scores.containsKey(langCode)) {
                    // this looks arbitrary, but gave best scores with evaluation (LanguageDetectionMinLengthEval):
                    scores.put(langCode, scores.get(langCode) + Double.valueOf(entry.getValue()));
                } else {
                    scores.put(langCode, Double.valueOf(entry.getValue()));
                }
            }
            detectionSource += "+commonwords";
            highestScoringResult = getHighestScoringResult(scores);
        }
        if (preferredLangs.contains("no") && !preferredLangs.contains("da")) {
            // Special case, as Norwegian easily gets detected as Danish (https://github.com/languagetool-org/languagetool/issues/5520).
            scores.keySet().removeIf(k -> k.equals("da"));
            highestScoringResult = getHighestScoringResult(scores);
        }
        if (cleanText.length() < CONSIDER_ONLY_PREFERRED_THRESHOLD && preferredLangs.size() > 0) {
            //System.out.println("remove? " + preferredLangs + " <-> " + scores);
            scores.keySet().removeIf(k -> !preferredLangs.contains(k));
            //System.out.println("-> " + b + " ==> " + scores);
            highestScoringResult = getHighestScoringResult(scores);
            detectionSource += "+prefLang";
        }
        if (highestScoringResult.getKey() != null && canLanguageBeDetected(highestScoringResult.getKey(), additionalLangs)) {
            return new DetectedLanguage(null,
                    Languages.getLanguageForShortCode(highestScoringResult.getKey(), additionalLangs),
                    highestScoringResult.getValue().floatValue(), detectionSource);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Language detectLanguage(String cleanText) {
        return null;
    }
}
