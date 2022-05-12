package org.languagetool.language.identifier;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SimpleLangIdentifier implements LangIdentifier {

    private final LangIdentifier spellcheckLangIdentifier;

    public SimpleLangIdentifier() {
        this.spellcheckLangIdentifier = new SpellcheckLangIdentifier();
    }

    public SimpleLangIdentifier(List<String> preferredLangCodes) {
        this.spellcheckLangIdentifier = new SpellcheckLangIdentifier(preferredLangCodes);
    }

    @Override
    public Map<String, Double> detectLanguages(String text, List<String> userPreferredLanguages, List<String> additionalLanguageCodes) throws IOException {
        return this.spellcheckLangIdentifier.detectLanguages(text, userPreferredLanguages, additionalLanguageCodes);
    }
}
