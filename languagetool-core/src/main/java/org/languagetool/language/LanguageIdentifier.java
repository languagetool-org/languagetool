package org.languagetool.language;

import com.optimaize.langdetect.text.TextFilter;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.languagetool.DetectedLanguage;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class LanguageIdentifier {
    private static final Pattern URL_REGEX = Pattern.compile("https?://[-_.?&~;+=/#%0-9A-Za-z]+");   // '%' has been added
    private static final Pattern MAIL_REGEX = Pattern.compile("[-_.0-9A-Za-z]+@[-_0-9A-Za-z]+[-_.0-9A-Za-z]+");
    private static final Pattern SIGNATURE = Pattern.compile("\n-- \n.*", Pattern.DOTALL);
    private static final Pattern MENTION = Pattern.compile("@[A-Za-z0-9_]+");
    protected static final float SCORE_THRESHOLD = 0.85f;
    protected static final int CONSIDER_ONLY_PREFERRED_THRESHOLD = 50;
    protected static final List<String> NON_LATIN_CHARS_LANGUAGES = Arrays.asList("ar", "fa", "ru", "uk", "be", "zh", "ja", "km", "ta", "el", "hi", "mr", "th", "he", "ko");

    protected static final TextFilter REMOVE_EMAIL_SIGNATURE_FILTER = text -> SIGNATURE.matcher(text).replaceFirst("");
    protected static final TextFilter REMOVE_MENTION_FILTER = text -> MENTION.matcher(text).replaceFirst("");
    protected static final TextFilter REMOVE_NON_BREAKING_SPACES_FILTER = text -> text.toString().replace('\u00A0', ' ');
    protected static final TextFilter REMOVE_URL_FILTER = text -> MAIL_REGEX.matcher(URL_REGEX.matcher(text).replaceAll(" ")).replaceAll(" ");

    protected static final UnicodeBasedLangIdentifier UNICODE_BASED_LANG_IDENTIFIER = new UnicodeBasedLangIdentifier();
    protected static final CommonWordsLangIdentifier COMMON_WORDS_LANG_IDENTIFIER;

    static {
        try {
            COMMON_WORDS_LANG_IDENTIFIER = new CommonWordsLangIdentifier();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param cleanText    a cleanText as returned by {@link #cleanAndShortenText(String)}
     * @param noopLangsTmp list of codes that are detected but will lead to the NoopLanguage that has no rules
     * @return language or {@code null} if language could not be identified
     * @since 4.4 (new parameter noopLangs, changed return type to DetectedLanguage)
     */
    @Nullable
    public abstract DetectedLanguage detectLanguage(String cleanText, List<String> noopLangsTmp, List<String> preferredLangsTmp);

    @Nullable
    public abstract Language detectLanguage(String cleanText);

    /**
     * @since 5.8
     */
    public String cleanAndShortenText(String text, int maxLength) {
        String shortText = text.length() > maxLength ? text.substring(0, maxLength) : text;
        shortText = shortText.replaceAll("\uFEFF+", " ");  // used by the browser add-on to filter HTML etc. (_ignoreText() in validator.js)
        shortText = REMOVE_URL_FILTER.filter(shortText);
        shortText = REMOVE_EMAIL_SIGNATURE_FILTER.filter(shortText);
        shortText = REMOVE_MENTION_FILTER.filter(shortText);
        shortText = REMOVE_NON_BREAKING_SPACES_FILTER.filter(shortText);
        return shortText;
    }

    /**
     * @since 5.4
     */
    public String cleanAndShortenText(String text) {
        return this.cleanAndShortenText(text, 1000);
    }

    protected Pair<List<String>, List<String>> prepareDetectLanguage(String text, List<String> noopLangsTmp, List<String> preferredLangsTmp) {
        Objects.requireNonNull(noopLangsTmp);
        Objects.requireNonNull(preferredLangsTmp);

        // Chrome sends 'nn' (Nynorsk) or 'nb' (Bokmal), but fasttext detects 'no', so we have to map, and 
        // Bokmal seems to be the standard variant:
        List<String> additionalLangs = noopLangsTmp.stream().map(k -> k.equals("nb") ? "no" : k).collect(Collectors.toList());
        List<String> preferredLangs = preferredLangsTmp.stream().map(k -> k.equals("nb") ? "no" : k).collect(Collectors.toCollection(ArrayList::new));
        if (preferredLangs.stream().anyMatch(k -> k.contains("-"))) {
            throw new IllegalArgumentException("preferredLanguages may only contain language codes without variants (e.g. 'en', but not 'en-US'): " + preferredLangs + ". Use 'preferredVariants' to specify variants.");
        }

        List<String> domLangCodes = UNICODE_BASED_LANG_IDENTIFIER.getDominantLangCodes(text);
        String domLangStr = String.join(",", domLangCodes);
        if (domLangStr.equals("th") || domLangStr.equals("he") || domLangStr.equals("ko") || domLangStr.equals("hi,mr")) {
            // more than 50% of characters are ..., so assume we don't support this cleanText:
            return null;
        }
        if (!preferredLangs.contains("ru") && !preferredLangs.contains("uk") && !preferredLangs.contains("be") && !preferredLangs.contains("zh") && !preferredLangs.contains("hi") && !preferredLangs.contains("mr")) {
            // Cyrillic and Chinese are so different from Latin characters that we try to detect it even with preferredLangs not properly set:
            preferredLangs.addAll(domLangCodes);
            additionalLangs.addAll(domLangCodes);
        }
        return Pair.of(additionalLangs, preferredLangs);
    }

    protected static boolean canLanguageBeDetected(String langCode, List<String> additionalLanguageCodes) {
        return Languages.isLanguageSupported(langCode) || additionalLanguageCodes.contains(langCode);
    }

    protected Map.Entry<String, Double> getHighestScoringResult(Map<String, Double> probs) {
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
}
