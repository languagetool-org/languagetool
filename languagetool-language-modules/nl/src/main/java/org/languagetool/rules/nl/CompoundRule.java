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
package org.languagetool.rules.nl;

import org.languagetool.rules.AbstractCompoundRule;
import org.languagetool.rules.CompoundRuleData;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 */
public class CompoundRule extends AbstractCompoundRule {

  private static final CompoundRuleData compoundData = new CompoundRuleData("/nl/compounds.txt");

  public CompoundRule(ResourceBundle messages) throws IOException {
    super(messages,
            "Dit woord hoort waarschijnlijk aaneengeschreven met een koppelteken.",
            "Dit woord hoort waarschijnlijk aaneengeschreven.",
            "Deze uitdrukking hoort mogelijk aan elkaar, eventueel met een koppelteken.",
            "Koppeltekenprobleem");
    super.sentenceStartsWithUpperCase = true;
  }

  @Override
  public String getId() {
    return "NL_COMPOUNDS";
  }

  @Override
  public String getDescription() {
    return "Woorden die aaneen geschreven horen, bijvoorbeeld 'zee-egel' i.p.v. 'zee egel'";
  }

  @Override
  protected CompoundRuleData getCompoundRuleData() {
    return compoundData;
  }

}
