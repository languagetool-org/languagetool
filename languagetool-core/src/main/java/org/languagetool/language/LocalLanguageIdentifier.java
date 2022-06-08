package org.languagetool.language;

import lombok.extern.slf4j.Slf4j;
import org.languagetool.DetectedLanguage;

import java.util.List;
import java.util.Optional;

@Slf4j
public class LocalLanguageIdentifier implements LanguageIdentifier {
    
    
    @Override
    public Optional<DetectedLanguage> detectLanguage(String text, List<String> noopLangsTmp, List<String> preferredLangsTmp) {
        return Optional.empty();
    }
}
