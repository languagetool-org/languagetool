package org.languagetool.rules.pt;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

public class MorfologikPortugueseSpellerRule extends MorfologikSpellerRule {

  private String dictFilename;
  private Language language;

  @Override
  public String getFileName() {
    return dictFilename;
  }

  @Override
  public String getId() {
    return "HUNSPELL_RULE";
    /*return "MORFOLOGIK_RULE_"
      + language.getShortCodeWithCountryAndVariant().replace("-", "_").toUpperCase();*/
  }

  public MorfologikPortugueseSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig,
                                      List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    this.setIgnoreTaggedWords();
    if (language.getShortCodeWithCountryAndVariant().equals("pt")) {
      language = language.getDefaultLanguageVariant();
    }
    this.language = language;
    this.dictFilename = "/pt/spelling/" + language.getShortCodeWithCountryAndVariant() + JLanguageTool.DICTIONARY_FILENAME_EXTENSION;
  }
}
