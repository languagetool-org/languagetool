package org.languagetool.language.identifier;

import java.util.List;
import java.util.Map;

public interface LangIdentifier {
    Map<String, Double> detectLanguages(String text, List<String> userPreferredLanguages, List<String> additionalLanguageCodes);
    
    int getMaxCharThreshold();
}
