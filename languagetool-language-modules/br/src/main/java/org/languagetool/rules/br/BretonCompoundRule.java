/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Tiago Santos (tiagofsantos@sapo.pt)
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

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * @author Tiago F. Santos
 */

package org.languagetool.rules.br;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 * @since 4.9
 */
public class BretonCompoundRule extends AbstractCompoundRule {

  private static volatile CompoundRuleData compoundData;

  public BretonCompoundRule(ResourceBundle messages, Language lang, UserConfig userConfig) throws IOException {    
    super(messages, lang, userConfig,
            "Skrivet e vez ar ger-mañ boaz gant ur varrennig-stagañ.",
            "Ar ger-mañ a zo skrivet boaz evel unan hepken.",
            "An droienn-mañ a zo skrivet evel ur ger hepken pe gant ur varrennig-stagañ.",
            "Kudenn barrennig-stagañ");
    super.setCategory(Categories.COMPOUNDING.getCategory(messages));
    addExamplePair(Example.wrong("Gwelet em eus un <marker>alc'hweder gwez</marker> e-kerzh an dibenn-sizhun-mañ."),
                   Example.fixed("Gwelet em eus un <marker>alc'hweder-gwez</marker> e-kerzh an dibenn-sizhun-mañ."));
    setLocQualityIssueType(ITSIssueType.Grammar);
  }

  @Override
  public String getId() {
    return "BR_COMPOUNDS";
  }

  @Override
  public String getDescription() {
    return "Mots composés";
  }

/*
  @Override
  public URL getUrl() {
    return Tools.getUrl("https://pt.wikipedia.org/wiki/Lista_das_alterações_previstas_pelo_acordo_ortográfico_de_1990");
  }
*/

  @Override
  public CompoundRuleData getCompoundRuleData() {
    CompoundRuleData data = compoundData;
    if (data == null) {
      synchronized (BretonCompoundRule.class) {
        data = compoundData;
        if (data == null) {
          compoundData = data = new CompoundRuleData("/br/compounds.txt");
        }
      }
    }

    return data;
  }
}
