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
package org.languagetool.rules.pt;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Checks that compounds (if in the list) are not written as separate words.
 * @since 2.6
 */
public class PortugueseColourHyphenationRule extends AbstractCompoundRule {

  private static volatile CompoundRuleData compoundData;

  public PortugueseColourHyphenationRule(ResourceBundle messages, Language lang, UserConfig userConfig) throws IOException {
    super(messages, lang, userConfig,
      "Nomes de cores são palavras compostas e devem ser hifenizados.",
      "Esta palavra é composta por justaposição.",
      "Esta palavra pode ser composta por justaposição ou hifenizada.",
      "Nomes de cores são palavras compostas.");
    super.setCategory(Categories.COMPOUNDING.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Grammar);
    useSubRuleSpecificIds();
  }

  @Override
  public String getId() {
    return "PT_COLOUR_HYPHENATION";
  }

  @Override
  public String getDescription() {
    return "Nomes de cores devem ser hifenizados: \"$match\"";
  }

  @Override
  public URL getUrl() {
    return Tools.getUrl("https://pt.wikipedia.org/wiki/Lista_das_alterações_previstas_pelo_acordo_ortográfico_de_1990");
  }

  @Override
  public CompoundRuleData getCompoundRuleData() {
    CompoundRuleData data = compoundData;
    if (data == null) {
      synchronized (PortugueseColourHyphenationRule.class) {
        data = compoundData;
        if (data == null) {
          compoundData = data = new CompoundRuleData("/pt/compound_colours.txt");
        }
      }
    }

    return data;
  }
}