/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ca;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.tagging.ca.CatalanTagger;

import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 */
public class CompoundRule extends AbstractCompoundRule {

  private static volatile CompoundRuleData compoundData;

  public CompoundRule(ResourceBundle messages, Language lang, UserConfig userConfig) throws IOException {
    super(messages, lang, userConfig,
            "S'escriu amb un guionet.",
            "S'escriu junt sense espai ni guionet.",
            "S'escriu junt o amb guionet.",
            "Error de mot compost");
    addExamplePair(Example.wrong("<marker>Ryan-Air</marker>."),
                   Example.fixed("<marker>Ryanair</marker>."));
  }

  @Override
  public String getId() {
    return "CA_COMPOUNDS";
  }

  @Override
  public String getDescription() {
    return "Paraules compostes amb guió";
  }

  @Override
  public CompoundRuleData getCompoundRuleData() {
    CompoundRuleData data = compoundData;
    if (data == null) {
      synchronized (CompoundRule.class) {
        data = compoundData;
        if (data == null) {
          compoundData = data = new CompoundRuleData("/ca/compounds.txt");
        }
      }
    }
    return data;
  }
  
  @Override
  public boolean isMisspelled (String word) throws IOException {
    return !CatalanTagger.INSTANCE_VAL.tag(Arrays.asList(word)).get(0).isTagged();
  }

}
