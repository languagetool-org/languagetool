/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * @author Marcin Miłkowski
 * @author Tiago F. Santos
 * @since 3.6
 */
 
public class PortugalPortugueseReplaceRule extends AbstractSimpleReplaceRule {

  public static final String PORTUGAL_PORTUGUESE_SIMPLE_REPLACE_RULE = "PT_PT_SIMPLE_REPLACE";

  private static final Map<String, List<String>> wrongWords = loadFromPath("/pt/pt-PT/replace.txt");
  private static final Locale PT_PT_LOCALE = new Locale("pt-PT");

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  public PortugalPortugueseReplaceRule(ResourceBundle messages) throws IOException {
    super(messages);
    super.setCategory(Categories.REGIONALISMS.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.LocaleViolation);
    addExamplePair(Example.wrong("Onde está o <marker>banheiro</marker>?"),
                   Example.fixed("Onde está o <marker>toilet</marker>?"));
  }

  @Override
  public final String getId() {
    return PORTUGAL_PORTUGUESE_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "Brasileirismo: 1. palavras confundidas com as de Portugal";
  }

  @Override
  public String getShort() {
    return "Palavra brasileira";
  }
  
  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return tokenStr + " é uma expressão brasileira, em Português de Portugal utiliza-se: "
        + String.join(", ", replacements) + ".";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  @Override
  public Locale getLocale() {
    return PT_PT_LOCALE;
  }

}
