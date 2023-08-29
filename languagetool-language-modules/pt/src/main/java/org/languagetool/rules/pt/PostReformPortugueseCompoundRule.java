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
public class PostReformPortugueseCompoundRule extends AbstractCompoundRule {

  private static volatile CompoundRuleData compoundData;

  public PostReformPortugueseCompoundRule(ResourceBundle messages, Language lang, UserConfig userConfig) throws IOException {    
    super(messages, lang, userConfig,
            "Esta palavra é hifenizada.",
            "Esta palavra é composta por justaposição.",
            "Esta palavra pode ser composta por justaposição ou hifenizada.",
            "Este conjunto forma uma palavra composta.");
    super.setCategory(Categories.COMPOUNDING.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Grammar);
    useSubRuleSpecificIds();
  }

  @Override
  public String getId() {
    return "PT_COMPOUNDS_POST_REFORM";
  }

  @Override
  public String getDescription() {
    return "Erro na formação da palavra composta \"$match\"";
  }

  @Override
  public URL getUrl() {
    return Tools.getUrl("https://pt.wikipedia.org/wiki/Lista_das_alterações_previstas_pelo_acordo_ortográfico_de_1990");
  }

  @Override
  public CompoundRuleData getCompoundRuleData() {
    CompoundRuleData data = compoundData;
    if (data == null) {
      synchronized (PostReformPortugueseCompoundRule.class) {
        data = compoundData;
        if (data == null) {
          compoundData = data = new CompoundRuleData("/pt/post-reform-compounds.txt");
        }
      }
    }

    return data;
  }

  // This override is here to account for Portuguese transformations required as per the latest orthography:
  // ultra + som  => ultrassom (with <s> turned into <ss> to keep the sound).
  @Override
  public String mergeCompound(String str, boolean uncapitalizeMidWords) {
    String[] stringParts = str.replaceAll("-", " ").split(" ");
    StringBuilder sb = new StringBuilder();
    for (int k = 0; k < stringParts.length; k++) {
      if (k == 0) {
        sb.append(stringParts[0]);
      } else {
        // if previous element ends in vowel and current one starts with <r> or <s>, we need to double the letter into
        // a digraph that creates the sound we want
        if (stringParts[k-1].matches("(?i).+[aeiou]$") && stringParts[k].matches("(?i)^[rs].+")) {
          stringParts[k] = stringParts[k].charAt(0) + stringParts[k];
        }
        sb.append(uncapitalizeMidWords ? StringUtils.uncapitalize(stringParts[k]) : stringParts[k]);
      }
    }
    return sb.toString();
  }
}
