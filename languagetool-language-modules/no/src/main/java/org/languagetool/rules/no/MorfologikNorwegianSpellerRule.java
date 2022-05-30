package org.languagetool.rules.no;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public final class MorfologikNorwegianSpellerRule extends MorfologikSpellerRule {

  public MorfologikNorwegianSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig) throws IOException {
    this(messages, language, userConfig, Collections.emptyList());
  }

  public MorfologikNorwegianSpellerRule(ResourceBundle messages, Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
  }

  @Override
  public String getFileName() {
    return String.format("/no/%s_NO.dict", language.getShortCode());
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_NO";
  }

  @Override
  protected String getIgnoreFileName() {
    return "/no/ignore.txt";
  }

  @Override
  public String getSpellingFileName() {
    return "/nl/spelling.txt";
  }

  @Override
  protected String getProhibitFileName() {
    return "/nl/prohibit.txt";
  }

}
