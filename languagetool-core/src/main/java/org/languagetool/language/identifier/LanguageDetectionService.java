package org.languagetool.language.identifier;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.languagetool.DetectedLanguage;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.language.CommonWords;
import org.languagetool.language.FastText;
import org.languagetool.language.LanguageIdentifier;
import org.languagetool.language.UnicodeBasedLangIdentifier;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public enum LanguageDetectionService {

    INSTANCE;

    public static final Pattern TEXT_PATTERN_SIGNATURE = Pattern.compile("\n-- \n.*", Pattern.DOTALL);
    public static final Pattern TEXT_PATTERN_MENTION = Pattern.compile("@[A-Za-z0-9_]+");
    public static final Pattern TEXT_PATTERN_URL = Pattern.compile("https?://[-_.?&~;+=/#%0-9A-Za-z]+");
    public static final Pattern TEXT_PATTERN_MAIL = Pattern.compile("[-_.0-9A-Za-z]+@[-_0-9A-Za-z]+[-_.0-9A-Za-z]+");
    public static final char WHITESPACE = '\u00A0';
    private static final int MAX_LENGTH = 1000;
    private static final int SHORT_ALGO_THRESHOLD = 50;

    private UnicodeBasedLangIdentifier unicodeIdentifier;

    @Setter
    private LangIdentifier languageIdentifier;

    LanguageDetectionService() {
        this.unicodeIdentifier = new UnicodeBasedLangIdentifier();
    }

    public Optional<DetectedLanguage> detectLanguage(String text, List<String> noopLangsTmp, List<String> preferredLangsTmp) {
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

        List<String> domLangCodes = unicodeIdentifier.getDominantLangCodes(text);
        String domLangStr = String.join(",", domLangCodes);
        if (domLangStr.equals("th") || domLangStr.equals("he") || domLangStr.equals("ko") || domLangStr.equals("hi,mr")) {
            // more than 50% of characters are ..., so assume we don't support this cleanText:
            return Optional.empty();
        }
        if (!preferredLangs.contains("ru") && 
                !preferredLangs.contains("uk") &&
                !preferredLangs.contains("be") &&
                !preferredLangs.contains("zh") &&
                !preferredLangs.contains("hi") &&
                !preferredLangs.contains("mr")) {
            // Cyrillic and Chinese are so different from Latin characters that we try to detect it even with preferredLangs not properly set:
            preferredLangs.addAll(domLangCodes);
            additionalLangs.addAll(domLangCodes);
        }
        String cleanText = cleanAndShortenText(text);
        Map<String, Double> scores = this.languageIdentifier.detectLanguages(cleanText, preferredLangs, additionalLangs);
        Map.Entry<String, Double> highestScoringResult = getHighestScoringResult(scores);

        if (highestScoringResult.getKey() != null && canLanguageBeDetected(highestScoringResult.getKey(), additionalLangs)) {
            return Optional.of(new DetectedLanguage(null,
                    Languages.getLanguageForShortCode(highestScoringResult.getKey(), additionalLangs),
                    highestScoringResult.getValue().floatValue()));
        } else {
            return Optional.empty();
        }
    }

    private String cleanAndShortenText(String text) {
        String shortText = text.length() > MAX_LENGTH ? text.substring(0, MAX_LENGTH) : text;
        shortText = shortText.replaceAll("\uFEFF+", " "); // used by the browser add-on to filter HTML etc. (_ignoreText() in validator.js)
        String noUrl = TEXT_PATTERN_URL.matcher(text).replaceAll("");
        String noMail = TEXT_PATTERN_MAIL.matcher(noUrl).replaceAll("");
        String noSignature = TEXT_PATTERN_SIGNATURE.matcher(noMail).replaceFirst("");
        String noMention = TEXT_PATTERN_MENTION.matcher(noSignature).replaceFirst("");
        return noMention.replace(WHITESPACE, ' ').trim();
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

    private boolean canLanguageBeDetected(String langCode, List<String> additionalLanguageCodes) {
        return Languages.isLanguageSupported(langCode) || additionalLanguageCodes.contains(langCode);
    }
}
