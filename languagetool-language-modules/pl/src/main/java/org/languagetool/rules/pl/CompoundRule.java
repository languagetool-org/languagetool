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
package org.languagetool.rules.pl;

import org.languagetool.rules.*;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 * 
 * @author Marcin Miłkowski, based on code by Daniel Naber
 */
public final class CompoundRule extends AbstractCompoundRule {

  private static volatile CompoundRuleData compoundData;

  public CompoundRule(ResourceBundle messages) throws IOException {
    super(messages,
            "Ten wyraz pisze się z łącznikiem.",
            "Ten wyraz pisze się razem (bez spacji ani łącznika).",
            "Ten wyraz pisze się z łącznikiem lub bez niego.",
            "Brak łącznika lub zbędny łącznik");
    addExamplePair(Example.wrong("Witamy w <marker>Rabce Zdroju</marker>."),
                   Example.fixed("Witamy w <marker>Rabce-Zdroju</marker>."));
  }
  
  @Override
  public String getId() {
    return "PL_COMPOUNDS";
  }

  @Override
  public String getDescription() {
    return "Sprawdza wyrazy z łącznikiem, np. „łapu capu” zamiast „łapu-capu”";
  }

  @Override
  protected CompoundRuleData getCompoundRuleData() {
    CompoundRuleData data = compoundData;
    if (data == null) {
      synchronized (CompoundRule.class) {
        data = compoundData;
        if (data == null) {
          compoundData = data = new CompoundRuleData("/pl/compounds.txt");
        }
      }
    }

    return data;
  }

}
