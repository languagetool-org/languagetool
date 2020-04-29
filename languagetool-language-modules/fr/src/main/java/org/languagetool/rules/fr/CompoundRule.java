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
package org.languagetool.rules.fr;

import java.io.IOException;
import java.util.ResourceBundle;

import org.languagetool.rules.AbstractCompoundRule;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.Example;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 */
public class CompoundRule extends AbstractCompoundRule {

  private static final CompoundRuleData compoundData = new CompoundRuleData("/fr/compounds.txt");

  public CompoundRule(ResourceBundle messages) throws IOException {
    super(messages,
            "Écrivez avec un trait d’union.",
            "Écrivez avec un mot seul sans espace ni trait d’union.",
            "Écrivez avec un mot seul ou avec trait d’union.",
            "Erreur de trait d’union");
    addExamplePair(Example.wrong("Le <marker>Haut Rhin</marker>."),
                   Example.fixed("Le <marker>Haut-Rhin</marker>."));
  }

  @Override
  public String getId() {
    return "FR_COMPOUNDS";
  }

  @Override
  public String getDescription() {
    return "Mots avec trait d’union";
  }

  @Override
  protected CompoundRuleData getCompoundRuleData() {
    return compoundData;
  }

}
