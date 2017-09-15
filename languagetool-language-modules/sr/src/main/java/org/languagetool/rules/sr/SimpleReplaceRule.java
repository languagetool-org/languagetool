package org.languagetool.rules.sr;

import org.languagetool.rules.AbstractSimpleReplaceRule;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A rule that matches words which should not be used and suggests correct ones instead.
 * Romanian implementations. Loads the list of words from
 * <code>/sr/replace.txt</code>.
 *
 * @author Zoltan Csala
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongWords = load("/sr/replace.txt");
  private static final Locale SR_LOCALE = new Locale("sr");  // locale used on case-conversion

  public SimpleReplaceRule(ResourceBundle messages) throws IOException {
    super(messages);
  }

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  @Override
  public final String getId() {
    return "SR_SIMPLE_REPLACE";
  }

  @Override
  public String getDescription() {
    return "Провера погрешних речи или израза";
  }

  @Override
  public String getShort() {
    return "Погрешна реч тј. израз";
  }

  @Override
  public Locale getLocale() {
    return SR_LOCALE;
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return "Уместо израза „" + tokenStr + "“ било би боље да користите: "
            + String.join(", ", replacements) + ".";
  }
}
