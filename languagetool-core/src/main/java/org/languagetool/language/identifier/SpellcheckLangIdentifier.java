package org.languagetool.language.identifier;

import lombok.extern.slf4j.Slf4j;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.*;

@Slf4j
public class SpellcheckLangIdentifier implements LangIdentifier {

    private List<String> nonLatinCharsLanguages = Arrays.asList("ar", "fa", "ru", "uk", "be", "zh", "ja", "km", "ta", "el", "hi", "mr", "th", "he", "ko");

    private Map<String, SpellingCheckRule> spellingCheckRules = new HashMap<>();

    public SpellcheckLangIdentifier() {
        List<Language> languages = Languages.get();
        for (Language lang : languages) {
            if (lang.isVariant() || lang.getShortCode().equals("zz")) {
                continue;
            }
            boolean hasVariant = lang.hasVariant();
            Language spellingRuleLang = null;
            if (hasVariant) {
                spellingRuleLang = lang.getDefaultLanguageVariant();
            }
            if (spellingRuleLang == null) {
                spellingRuleLang = lang;
            }
            SpellingCheckRule defaultSpellingRule = spellingRuleLang.getDefaultSpellingRule(
                    JLanguageTool
                            .getDataBroker()
                            .getResourceBundle(JLanguageTool.MESSAGE_BUNDLE, new Locale(lang.getShortCodeWithCountryAndVariant())));

            if (defaultSpellingRule != null) {
                spellingCheckRules.put(lang.getShortCode(), defaultSpellingRule);
            } else {
                System.out.println("could not found default speller rule for " + lang.getShortCode());
            }
        }
    }

    public SpellcheckLangIdentifier(List<String> preferredLangCodes) {
        log.info("Init SpellcheckLangIdentifier with {}", preferredLangCodes);
        preferredLangCodes.forEach(langCode -> {
            Language language = Languages.getLanguageForShortCode(langCode);
            SpellingCheckRule defaultSpellingRule = language.getDefaultSpellingRule(
                    JLanguageTool
                            .getDataBroker()
                            .getResourceBundle(JLanguageTool.MESSAGE_BUNDLE, new Locale(langCode)));
            spellingCheckRules.put(language.getShortCode(), defaultSpellingRule);
        });
    }

    @Override
    public Map<String, Double> detectLanguages(String text, List<String> userPreferredLanguages, List<String> additionalLanguageCodes) throws IOException {
        log.info("Try to detect language of \"{}\"", text);
        String[] words = text.split(" ");
        List<String> dominantLangCodes = UnicodeCounter.INSTANCE.getDominantLangCodes(text);
        Map<String, Double> results = new HashMap<>();
        if (!spellingCheckRules.isEmpty()) {
            for (Map.Entry<String, SpellingCheckRule> scp : spellingCheckRules.entrySet()) {
                if (dominantLangCodes.contains(scp.getKey()) ^ (dominantLangCodes.isEmpty() && !nonLatinCharsLanguages.contains(scp.getKey()))) {
                    double errors = 0;
                    for (String word : words) {
                        errors += scp.getValue().isMisspelled(word) ? 1 : 0;
                    }
                    double errorRate = errors / (double) words.length;
                    double calc = 1 - errorRate;
                    log.info("Found {} errors for {} words with {} spellchecke this results in an error rate of {}", errors, words.length, scp.getKey(), errorRate);
                    results.put(scp.getKey(), 1.0 - errorRate);
                }
            }
        }
        if (results.isEmpty()) {
            results.put("zz", 1.0);
        }
        log.info("Got results: {}", results);
        return results;
    }
}
