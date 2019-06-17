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
package org.languagetool.rules.de;

import java.io.IOException;
import java.util.ResourceBundle;

import org.languagetool.rules.AbstractCompoundRule;
import org.languagetool.rules.Categories;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.Example;

/**
 * Checks that compounds are not written as separate words. The supported compounds are loaded
 * from {@code /de/compounds.txt} and {@code /de/compounds-cities.txt} in the resource directory.
 * 
 * @author Daniel Naber
 */
public class CompoundRule extends AbstractCompoundRule {

  private static final CompoundRuleData compoundData = new CompoundRuleData("/de/compounds.txt", "/de/compound-cities.txt");
 
  public CompoundRule(ResourceBundle messages) throws IOException {
    super(messages,
            "Dieses Wort wird mit Bindestrich geschrieben.",
            "Dieses Wort wird zusammengeschrieben.",
            "Diese Wörter werden zusammengeschrieben oder mit Bindestrich getrennt.",
            "Zusammenschreibung von Wörtern");
    super.setCategory(Categories.COMPOUNDING.getCategory(messages));
    addExamplePair(Example.wrong("Wenn es schlimmer wird, solltest Du zum <marker>HNO Arzt</marker> gehen."),
                   Example.fixed("Wenn es schlimmer wird, solltest Du zum <marker>HNO-Arzt</marker> gehen."));
  }

  @Override
  public String getId() {
    return "DE_COMPOUNDS";
  }

  @Override
  public String getDescription() {
    return "Zusammenschreibung von Wörtern, z. B. 'CD-ROM' statt 'CD ROM'";
  }

  @Override
  protected CompoundRuleData getCompoundRuleData() {
    return compoundData;
  }
}
