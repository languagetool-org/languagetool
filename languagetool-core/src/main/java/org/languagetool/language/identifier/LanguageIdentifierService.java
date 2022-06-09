package org.languagetool.language.identifier;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.languagetool.Languages;

import java.io.File;
import java.util.List;

@Slf4j
public enum LanguageIdentifierService {

    INSTANCE;

    private LanguageIdentifier defaultIdentifier = null;
    private LanguageIdentifier simpleIdenfifier = null;

    /**
     * @param maxLength          - the maximum of characters that will be considered - can help with performance.
     *                           If 0 the default value of 1000 is used.
     *                           Don't use values between 1-100, as this would decrease accuracy.
     * @param ngramLangIdentData - the ngramLangIdentData file, if {@code null} ngram will not be initialized.
     * @param fasttextBinary     - the fasttext binary file, if {@code null} fasttext will not be initialized.
     * @param fasttextModel      - the fasttext model file, if {@code null} fasttext will not be initialized.
     * @return new {@code LanguageIdentifier} or existing if already initialized.
     * @since 5.8
     */
    public synchronized LanguageIdentifier getDefaultLanguageIdentifier(int maxLength,
                                                                        @Nullable File ngramLangIdentData,
                                                                        @Nullable File fasttextBinary,
                                                                        @Nullable File fasttextModel) {
        if (defaultIdentifier == null) {
            DefaultLanguageIdentifier defaultIdentifier = maxLength > 0 ? new DefaultLanguageIdentifier(maxLength) : new DefaultLanguageIdentifier();
            defaultIdentifier.enableNgrams(ngramLangIdentData);
            defaultIdentifier.enableFasttext(fasttextBinary, fasttextModel);
            this.defaultIdentifier = defaultIdentifier;
        }
        return this.defaultIdentifier;
    }

    /**
     * @param preferredLangCodes - a list of language codes for that a spellchecker will be initialized.
     *                           If {@code null} all spellchecker will be used.
     * @return new {@code LanguageIdentifier} or existing if already initialized.
     * @since 5.8
     */
    public synchronized LanguageIdentifier getSimpleLanguageIdentifier(@Nullable List<String> preferredLangCodes) {
        if (simpleIdenfifier == null) {
            if (preferredLangCodes == null) {
                this.simpleIdenfifier = new SimpleLanguageIdentifier();
            } else {
                this.simpleIdenfifier = new SimpleLanguageIdentifier(preferredLangCodes);

            }
        }
        return this.simpleIdenfifier;
    }

    public boolean canLanguageBeDetected(String langCode, List<String> additionalLanguageCodes) {
        return Languages.isLanguageSupported(langCode) || additionalLanguageCodes.contains(langCode);
    }

    /**
     * @param type - option: "default", "simpel, or both to clear the identifiers
     * @return {@code LanguageIdentifierService} instance
     */
    @TestOnly
    public LanguageIdentifierService clearLanguageIdentifier(String type) {
        switch (type) {
            case "default":
                this.defaultIdentifier = null;
                break;
            case "simple":
                this.simpleIdenfifier = null;
                break;
            case "both":
                this.simpleIdenfifier = null;
                this.defaultIdentifier = null;
                break;
            default:
                break;
        }
        return this;
    }
}
