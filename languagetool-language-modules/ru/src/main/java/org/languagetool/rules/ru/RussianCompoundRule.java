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
package org.languagetool.rules.ru;

import org.languagetool.rules.*;

import java.io.IOException;
import java.util.ResourceBundle;


/**
 * Checks that compounds (if in the list) are not written as separate words.
 * Russian compounds rule.
 * @author Yakov Reztsov
 * 
 * Based on German compounds rule.
 * @author Daniel Naber
 *
 */
public class RussianCompoundRule extends AbstractCompoundRule {

  private static volatile CompoundRuleData compoundData;

  public RussianCompoundRule(ResourceBundle messages) throws IOException {
    super(messages,
            "Эти слова должны быть написаны через дефис.",
            "Эти слова должны быть написаны слитно.",
            "Эти слова могут быть написаны через дефис или слитно.");
    addExamplePair(Example.wrong("Собрание состоится в <marker>конференц зале</marker>."),
                   Example.fixed("Собрание состоится в <marker>конференц-зале</marker>."));
   super.sentenceStartsWithUpperCase = true;
  }

  
  @Override
  public String getId() {
    return "RU_COMPOUNDS";
  }

  @Override
  public String getDescription() {
    return "Правописание через дефис";
  }

  @Override
  protected CompoundRuleData getCompoundRuleData() {
    CompoundRuleData data = compoundData;
    if (data == null) {
      synchronized (RussianCompoundRule.class) {
        data = compoundData;
        if (data == null) {
          compoundData = data = new CompoundRuleData("/ru/compounds.txt");
        }
      }
    }

    return data;
  }

}
