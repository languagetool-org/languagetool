package org.languagetool.rules.sr;

import org.languagetool.Language;
import org.languagetool.rules.Example;
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
    addExamplePair(
            Example.wrong("Изгубила све сам <marker>бткие</marker>, ал' још водим рат."),
            Example.fixed("Изгубила све сам <marker>битке</marker>, ал' још водим рат.")
    );
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
