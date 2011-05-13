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
package de.danielnaber.languagetool.rules.ro;

import java.io.IOException;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.rules.AbstractCompoundRule;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 *
 * @author Ionuț Păduraru, based on code by Daniel Naber
 */
public class CompoundRule extends AbstractCompoundRule {

  private static final String FILE_NAME = "/ro/compounds.txt";

  public CompoundRule(final ResourceBundle messages) throws IOException {
    super(messages, FILE_NAME,
            "Cuvântul se scrie cu cratimă.",
            "Cuvântul se scrie legat.",
            "Cuvântul se scrie legat sau cu cratimă.");
    super.setShort("Problemă de scriere (cratimă, spațiu, etc.)");
    // default value (2) is not ok for Romanian
    setMaxUnHyphenatedWordCount(Integer.MAX_VALUE);
    // there are words that should not be written with hyphen but as one word
    setHyphenIgnored(false);
  }

  @Override
  public String getId() {
    return "RO_COMPOUND";
  }

  @Override
  public String getDescription() {
    return "Greșeală de scriere (cuvinte scrise legat sau cu cratimă)";
  }

}
