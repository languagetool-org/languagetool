package org.languagetool.language.identifier;

import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.ResourceBundle;

public class NoopSpellCheckerRule extends SpellingCheckRule {

    public NoopSpellCheckerRule(ResourceBundle messages, Language language) {
        super(messages, language, null);
    }

    @Override
    public String getId() {
        return "NoopSpellCheckerRule";
    }

    @Override
    public String getDescription() {
        return "NoopSpellCheckerRule";
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        return new RuleMatch[0];
    }

    @Override
    public boolean isMisspelled(String word) throws IOException {
        return true;
    }
}
