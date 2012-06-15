package org.languagetool.rules.spelling.morfologik.pl;

import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

public final class MorfologikPolishSpellerRule extends MorfologikSpellerRule {

    //simplistic!
    private static final String RESOURCE_FILENAME = "/pl/hunspell/pl_PL.dict";
    
    public MorfologikPolishSpellerRule(ResourceBundle messages,
            Language language) {
        super(messages, language);
    }

    @Override
    public String getFileName() {
        return RESOURCE_FILENAME;
    }

}
