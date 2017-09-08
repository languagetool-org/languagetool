package org.languagetool.rules.sr;

import org.languagetool.Language;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

import java.io.IOException;
import java.util.ResourceBundle;

public class MorfologikSerbianSpellerRule extends MorfologikSpellerRule {

  private static final String RESOURCE_FILENAME = "/sr/hunspell/sr_RS.dict";
  public static final String RULE_ID = "MORFOLOGIK_RULE_SR_RS";

  public MorfologikSerbianSpellerRule(
          ResourceBundle messages,
          Language language) throws IOException {

    super(messages, language);
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return RULE_ID;
  }
}
