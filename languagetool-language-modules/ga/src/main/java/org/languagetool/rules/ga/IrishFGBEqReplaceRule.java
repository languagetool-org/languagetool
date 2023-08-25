/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.ga;

import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A rule that matches words which should not be used and suggests
 * correct ones instead.
 *
 * Irish version for uncommon variants, which are included in
 * Foclóir Gaeilge-Béarla
 * relevant words from <code>rules/ga/replace-fgb-eq.txt</code>.
 *
 * @author Jim O'Regan
 */
public class IrishFGBEqReplaceRule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongWords = loadFromPath("/ga/replace-fgb-eq.txt");
  private static final Locale GA_LOCALE = new Locale("GA");

  @Override
  public Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  public IrishFGBEqReplaceRule(final ResourceBundle messages) throws IOException {
    super(messages);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    super.setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("An bhfuil tú <marker>urlamh</marker>?"),
      Example.fixed("An bhfuil tú <marker>ullamh</marker>?"));
    this.setCheckLemmas(false);
  }

  @Override
  public final String getId() {
    return "GA_FGB_EQ_REPLACE";
  }

  @Override
  public String getDescription() {
    return "Ceannfhocal FGB neamhchoitianta, m.sh., \"urlamh\" in áit \"ullamh\"";
  }

  @Override
  public String getShort() {
    return "Neamhchoitianta";
  }

  @Override
  public String getMessage(String tokenStr,List<String> replacements) {
    return "Focal ceart ach tá \""
      + String.join(", ", replacements) + "\" níos coitianta.";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  @Override
  public Locale getLocale() {
    return GA_LOCALE;
  }

}
