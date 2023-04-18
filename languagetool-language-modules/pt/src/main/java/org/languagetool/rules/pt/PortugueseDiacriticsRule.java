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
 * A rule that matches words which require specific diacritics (e.g, {@code a la} instead of {@code à la}).
 *
 * @author Tiago F. Santos 
 * @since 4.7
 */
public class PortugueseDiacriticsRule extends AbstractSimpleReplaceRule2 {

  public static final String PT_DIACRITICS_REPLACE = "PT_DIACRITICS_REPLACE";

  private static final String FILE_NAME = "/pt/diacritics.txt";
  private static final Locale PT_LOCALE = new Locale("pt");  // locale used on case-conversion

  @Override
  public List<String> getFileNames() {
    return Collections.singletonList(FILE_NAME);
  }

  public PortugueseDiacriticsRule(ResourceBundle messages) {
    super(messages, new Portuguese());
    setDefaultOff();
    super.setCategory(Categories.TYPOS.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    useSubRuleSpecificIds();
    addExamplePair(Example.wrong("<marker>coupe</marker>"),
                   Example.fixed("<marker>coupé</marker>"));
  }

  @Override
  public String getId() {
    return PT_DIACRITICS_REPLACE;
  }

  @Override
  public String getDescription() {
    return "Palavras estrangeiras com diacríticos";
  }

  @Override
  public String getShort() {
    return "A palavra estrangeira original tem diacrítico";
  }

  @Override
  public String getMessage() {
    return "'$match' é uma expressão estrangeira importada cuja grafia tem diacríticos. É preferível escrever $suggestions";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " ou ";
  }

  @Override
  public URL getUrl() {
    return Tools.getUrl("http://escreverbem.com.br/o-frances-no-portugues-2/");
  }

  @Override
  public Locale getLocale() {
    return PT_LOCALE;
  }

}
