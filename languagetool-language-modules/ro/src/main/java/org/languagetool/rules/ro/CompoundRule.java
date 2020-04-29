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
package org.languagetool.rules.ro;

import java.io.IOException;
import java.util.ResourceBundle;

import org.languagetool.rules.AbstractCompoundRule;
import org.languagetool.rules.CompoundRuleData;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 *
 * @author Ionuț Păduraru, based on code by Daniel Naber
 */
public class CompoundRule extends AbstractCompoundRule {

  private static final CompoundRuleData compoundData = new CompoundRuleData("/ro/compounds.txt");

  public CompoundRule(ResourceBundle messages) throws IOException {
    super(messages,
            "Cuvântul se scrie cu cratimă.",
            "Cuvântul se scrie legat.",
            "Cuvântul se scrie legat sau cu cratimă.",
            "Problemă de scriere (cratimă, spațiu, etc.)");
  }

  @Override
  public boolean isHyphenIgnored() {
    return false;
  }

  @Override
  public String getId() {
    return "RO_COMPOUND";
  }

  @Override
  public String getDescription() {
    return "Greșeală de scriere (cuvinte scrise legat sau cu cratimă)";
  }

  @Override
  protected CompoundRuleData getCompoundRuleData() {
    return compoundData;
  }

}
