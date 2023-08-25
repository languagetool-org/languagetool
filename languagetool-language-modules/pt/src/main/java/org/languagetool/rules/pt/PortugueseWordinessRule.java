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

import java.util.*;
import java.net.URL;

/**
 * A rule that matches wordy expressions. 
 * Portuguese implementation. Loads the list of words from
 * <code>/pt/wordiness.txt</code>.
 *
 * @author Tiago F. Santos 
 * @since 3.8
 */
public class PortugueseWordinessRule extends AbstractSimpleReplaceRule2 {

  public static final String PT_WORDINESS_REPLACE = "PT_WORDINESS_REPLACE";

  private static final Locale PT_LOCALE = new Locale("pt");  // locale used on case-conversion

  private final String path;

  @Override
  public List<String> getFileNames() {
    return Collections.singletonList(path);
  }

  public PortugueseWordinessRule(ResourceBundle messages, String path) {
    super(messages, new Portuguese());
    this.path = Objects.requireNonNull(path);
    super.setCategory(Categories.REDUNDANCY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    useSubRuleSpecificIds();
    addExamplePair(Example.wrong("<marker>Raramente é o caso em que acontece</marker> isto."),
                   Example.fixed("<marker>Raramente acontece</marker> isto."));
  }

  @Override
  public String getId() {
    return PT_WORDINESS_REPLACE;
  }

  @Override
  public String getDescription() {
    return "2. Expressões prolixas";
  }

  @Override
  public String getShort() {
    return "Expressão prolixa";
  }

  @Override
  public String getMessage() {
    return "\"$match\" é uma expressão prolixa. É preferível dizer $suggestions.";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " ou ";
  }

  @Override
  public URL getUrl() {
    return Tools.getUrl("https://pt.wikipedia.org/wiki/V%C3%ADcio_de_linguagem#Prolixidade_ou_preciosismo");
  }

  @Override
  public Locale getLocale() {
    return PT_LOCALE;
  }

}
