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

import org.languagetool.language.Portuguese;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.tools.Tools;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import java.net.URL;

/**
 * A rule that matches words which should not be used and suggests correct ones instead. 
 * Romanian implementations. Loads the list of words from
 * <code>/ro/replace.txt</code>.
 *
 * @author Tiago F. Santos 
 * @since 3.6
 */
public class PortugueseClicheRule extends AbstractSimpleReplaceRule2 {

  public static final String PORTUGUESE_CLICHE_RULE = "PT_CLICHE_REPLACE";

  private static final String FILE_NAME = "/pt/cliches.txt";
  private static final Locale PT_LOCALE = new Locale("pt");  // locale used on case-conversion

  @Override
  public final List<String> getFileNames() {
    return Collections.singletonList(FILE_NAME);
  }

  public PortugueseClicheRule(ResourceBundle messages) {
    super(messages, new Portuguese());
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    useSubRuleSpecificIds();
    addExamplePair(Example.wrong("<marker>quente como uma fornalha</marker>"),
                   Example.fixed("<marker>quente</marker>"));
  }

  @Override
  public String getId() {
    return PORTUGUESE_CLICHE_RULE;
  }

  @Override
  public String getDescription() {
    return "Frases-feitas e expressões idiomáticas";
  }

  @Override
  public String getShort() {
    return "Frase-feita";
  }

  @Override
  public String getMessage() {
    return "'$match' é uma frase-feita. É preferível dizer $suggestions.";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " ou ";
  }

  @Override
  public URL getUrl() {
    return Tools.getUrl("https://pt.wikipedia.org/wiki/Clichê");
  }

  @Override
  public Locale getLocale() {
    return PT_LOCALE;
  }

}
