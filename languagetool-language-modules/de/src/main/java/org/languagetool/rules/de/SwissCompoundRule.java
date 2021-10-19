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
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.LineExpander;

import java.io.IOException;
import java.util.*;

/**
 * @since 4.9
 */
public class SwissCompoundRule extends GermanCompoundRule {

  private static volatile CompoundRuleData compoundData;
 
  public SwissCompoundRule(ResourceBundle messages, Language lang, UserConfig userConfig) throws IOException {
    super(messages, lang, userConfig);
  }

  @Override
  public String getId() {
    return "DE_CH_COMPOUNDS";
  }

  @Override
  public CompoundRuleData getCompoundRuleData() {
    CompoundRuleData data = compoundData;
    if (data == null) {
      synchronized (SwissCompoundRule.class) {
        data = compoundData;
        if (data == null) {
          compoundData = data = new CompoundRuleData(new SwissExpander(), "/de/compounds.txt", "/de/compound-cities.txt");
        }
      }
    }

    return data;
  }
  
  static class SwissExpander implements LineExpander {
    @Override
    public List<String> expandLine(String line) {
      if (line.contains("ß")) {
        return Arrays.asList(line, line.replaceAll("ß", "ss"));  // accept both, assuming Swiss users sometimes deal with GermanyGerman text
      } else {
        return Arrays.asList(line);
      }
    }
  }
}
