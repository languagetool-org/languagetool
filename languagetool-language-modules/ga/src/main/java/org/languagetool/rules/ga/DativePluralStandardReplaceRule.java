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
import java.util.*;

/**
 * A rule that matches words in the dative plural and suggests
 * the common plural instead.
 *
 * relevant words from <code>rules/ga/dative-plurals.txt</code>.
 *
 * @author Jim O'Regan
 */
public class DativePluralStandardReplaceRule extends AbstractSimpleReplaceRule {
  
  private static final Map<String, String> replacements = DativePluralsData.getSimpleReplacements();
  private static final Map<String, List<String>> wrongWords = listify(replacements);
  private static final Locale GA_LOCALE = new Locale("GA");

  private static Map<String, List<String>> listify(Map<String, String> singles) {
    Map<String, List<String>> out = new HashMap<>();
    for(Map.Entry<String, String> e : singles.entrySet()) {
      List<String> tmp = new ArrayList<>();
      tmp.add(e.getValue());
      out.put(e.getKey(), tmp);
    }
    return out;
  }

  @Override
  public Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  public DativePluralStandardReplaceRule(final ResourceBundle messages) throws IOException {
    super(messages);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    super.setLocQualityIssueType(ITSIssueType.Grammar);
    addExamplePair(Example.wrong("Dá mba thruamhéalach é cás an sclábhaí fir, ba mheasa fós do na <marker>mnáibh</marker> a gcás siúd."),
                   Example.fixed("Dá mba thruamhéalach é cás an sclábhaí fir, ba mheasa fós do na <marker>mná</marker> a gcás siúd."));
    this.setCheckLemmas(false);
  }

  @Override
  public final String getId() {
    return "GA_DATIVE_PLURALS_STD";
  }

  @Override
  public String getDescription() {
    return "Tuiseal tabharthach iolra";
  }

  @Override
  public String getShort() {
    return "Tabharthach iolra";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return "Is litriú réamhchaighdeánach (tabharthach iolra) é \"" + tokenStr + "\"; an \""
      + String.join(", ", replacements) + "\" a bhí i gceist agat?";
  }

}
