package org.languagetool.language;

import org.languagetool.DetectedLanguage;
import org.languagetool.language.identifier.LanguageIdentifier;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;

public abstract class LanguageIdentifierTest {
    
      
  protected void langAssert(String expectedLangCode, String text, LanguageIdentifier id) {
    langAssert(expectedLangCode, text, id, Collections.emptyList(), Collections.emptyList());
  }
  
  protected void langAssert(String expectedLangCode, String text, LanguageIdentifier id, List<String> noopLangCodes,
                          List<String> preferredLangCodes) {
    DetectedLanguage detectedLang = id.detectLanguage(text, noopLangCodes, preferredLangCodes);
    String detectedLangCode = detectedLang != null ?
      detectedLang.getDetectedLanguage() != null ? detectedLang.getDetectedLanguage().getShortCode() : null
      : null;
    if (expectedLangCode == null) {
      if (detectedLangCode != null) {
        fail("Got '" + detectedLangCode + "', expected null for '" + text + "'");
      }
    } else {
      if (!expectedLangCode.equals(detectedLangCode)) {
        if (detectedLang != null) {
          fail("Got '" + detectedLangCode + "', expected '" + expectedLangCode + "' for '" + text + "' from '" + detectedLang.getDetectionSource() + "'");
        } else {
          fail("Got null, expected '" + expectedLangCode + "' for '" + text + "'");
        }
      }
    }
  }
}
