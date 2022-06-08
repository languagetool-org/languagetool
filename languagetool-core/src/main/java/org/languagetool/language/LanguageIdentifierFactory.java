package org.languagetool.language;

import com.optimaize.langdetect.text.TextFilter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public enum LanguageIdentifierFactory {

    INSTANCE;

    private LanguageIdentifier defaultIdentifier = null;
    private LanguageIdentifier spellcheckBasedIdentifier = null;

    public synchronized LanguageIdentifier getDefaultLanguageIdentifier(@Nullable Integer maxLength, @Nullable File ngramLangIdentData, @Nullable File fasttextBinary, @Nullable File fasttextModel) {
        if (defaultIdentifier == null) {
            DefaultLanguageIdentifier defaultIdentifier = maxLength != null ? new DefaultLanguageIdentifier(maxLength) : new DefaultLanguageIdentifier();
            defaultIdentifier.enableNgrams(ngramLangIdentData);
            defaultIdentifier.enableFasttext(fasttextBinary, fasttextModel);
            this.defaultIdentifier = defaultIdentifier;
        }
        return this.defaultIdentifier;
    }

    public synchronized LanguageIdentifier getLocalLanguageIdentifier(@Nullable List<String> preferredLangCodes) {
        if (spellcheckBasedIdentifier == null) {
            if (preferredLangCodes == null) {
                this.spellcheckBasedIdentifier = new LocalLanguageIdentifier();
            } else {
                this.spellcheckBasedIdentifier = new LocalLanguageIdentifier(preferredLangCodes);

            }
        }
        return this.spellcheckBasedIdentifier;
    }
}
