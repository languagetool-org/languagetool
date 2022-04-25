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
package org.languagetool.rules.es;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.tagging.es.SpanishTagger;

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
            "Se escribe con un guion.",
            "Se escribe junto sin espacio ni guion.",
            "Se escribe junto o con un guion.",
            "Error de palabra compuesta");
    addExamplePair(Example.wrong("<marker>Guinea Conakri</marker>."),
                   Example.fixed("<marker>Guinea-Conakri</marker>."));
  }

  @Override
  public String getId() {
    return "ES_COMPOUNDS";
  }

  @Override
  public String getDescription() {
    return "Palabras compuestas con guion";
  }

  @Override
  public CompoundRuleData getCompoundRuleData() {
    CompoundRuleData data = compoundData;
    if (data == null) {
      synchronized (CompoundRule.class) {
        data = compoundData;
        if (data == null) {
          compoundData = data = new CompoundRuleData("/es/compounds.txt");
        }
      }
    }
    return data;
  }
  
  @Override
  public boolean isMisspelled (String word) throws IOException {
    return !SpanishTagger.INSTANCE.tag(Arrays.asList(word)).get(0).isTagged();
  }

}
