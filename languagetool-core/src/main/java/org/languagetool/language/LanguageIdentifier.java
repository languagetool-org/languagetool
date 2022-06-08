package org.languagetool.language;

import com.optimaize.langdetect.text.TextFilter;
import org.languagetool.DetectedLanguage;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public abstract class LanguageIdentifier {

    private static final Pattern URL_REGEX = Pattern.compile("https?://[-_.?&~;+=/#%0-9A-Za-z]+");   // '%' has been added
    private static final Pattern MAIL_REGEX = Pattern.compile("[-_.0-9A-Za-z]+@[-_0-9A-Za-z]+[-_.0-9A-Za-z]+");
    private static final Pattern SIGNATURE = Pattern.compile("\n-- \n.*", Pattern.DOTALL);
    private static final Pattern MENTION = Pattern.compile("@[A-Za-z0-9_]+");


    protected static TextFilter REMOVE_EMAIL_SIGNATURE_FILTER = text -> SIGNATURE.matcher(text).replaceFirst("");

    protected static TextFilter REMOVE_MENTION_FILTER = text -> MENTION.matcher(text).replaceFirst("");

    protected static TextFilter REMOVE_NON_BREAKING_SPACES_FILTER = text -> text.toString().replace('\u00A0', ' ');

    protected static TextFilter REMOVE_URL_FILTER = text -> MAIL_REGEX.matcher(URL_REGEX.matcher(text).replaceAll(" ")).replaceAll(" ");

    public abstract Optional<DetectedLanguage> detectLanguage(String text, List<String> noopLangsTmp, List<String> preferredLangsTmp);


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
}
