/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.sk;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.AbstractCompoundRule;
import org.languagetool.rules.CompoundRuleData;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 * 
 * @author Zdenko Podobný based on code by Marcin Miłkowski, Daniel Naber
 */
public final class CompoundRule extends AbstractCompoundRule {

  private static volatile CompoundRuleData compoundData;

  public CompoundRule(ResourceBundle messages, Language lang, UserConfig userConfig) throws IOException {
    super(messages, lang, userConfig,
            "Toto slovo sa zvyčajne píše so spojovníkom.",
            "Toto slovo sa obvykle píše bez spojovníka.",
            "Tento výraz sa bežne píše s alebo bez spojovníka.",
            "Problém spájania slov");
  }

  @Override
  public String getId() {
    return "SK_COMPOUNDS";
  }

  @Override
  public String getDescription() {
    return "Slová so spojovníkom napr. použite „česko-slovenský” namiesto „česko slovenský”";
  }

  @Override
  public CompoundRuleData getCompoundRuleData() {
    CompoundRuleData data = compoundData;
    if (data == null) {
      synchronized (CompoundRule.class) {
        data = compoundData;
        if (data == null) {
          compoundData = data = new CompoundRuleData("/sk/compounds.txt");
        }
      }
    }

    return data;
  }

}
