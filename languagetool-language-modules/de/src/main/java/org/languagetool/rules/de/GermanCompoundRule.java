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

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.*;
import org.languagetool.tagging.de.GermanTagger;

import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * Checks that compounds are not written as separate words. The supported compounds are loaded
 * from {@code /de/compounds.txt} and {@code /de/compounds-cities.txt} in the resource directory.
 * 
 * @author Daniel Naber
 */
public class GermanCompoundRule extends AbstractCompoundRule {

  private static volatile CompoundRuleData compoundData;
  
  private static GermanSpellerRule germanSpellerRule;
 
  public GermanCompoundRule(ResourceBundle messages, Language lang, UserConfig userConfig) throws IOException {
    super(messages, lang, userConfig,
            "Dieses Wort wird mit Bindestrich geschrieben.",
            "Dieses Wort wird zusammengeschrieben.",
            "Diese Wörter werden zusammengeschrieben oder mit Bindestrich getrennt.",
            "Zusammenschreibung von Wörtern");
    super.setCategory(Categories.COMPOUNDING.getCategory(messages));
    addExamplePair(Example.wrong("Wenn es schlimmer wird, solltest Du zum <marker>HNO Arzt</marker> gehen."),
                   Example.fixed("Wenn es schlimmer wird, solltest Du zum <marker>HNO-Arzt</marker> gehen."));
    if (germanSpellerRule == null) {
      germanSpellerRule = new GermanSpellerRule(messages, new GermanyGerman());
    }
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
  public CompoundRuleData getCompoundRuleData() {
    CompoundRuleData data = compoundData;
    if (data == null) {
      synchronized (GermanCompoundRule.class) {
        data = compoundData;
        if (data == null) {
          compoundData = data = new CompoundRuleData("/de/compounds.txt", "/de/compound-cities.txt");
        }
      }
    }

    return data;
  }
  
  @Override
  public boolean isMisspelled(String word) throws IOException {
    //return !GermanTagger.INSTANCE.tag(Arrays.asList(word)).get(0).isTagged();
    return germanSpellerRule.isMisspelled(word);
  }
}
