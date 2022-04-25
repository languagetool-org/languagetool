/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ca;

import org.languagetool.Tag;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.ITSIssueType;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Suggestions for replacing adverbs -ment 
 * 
 * Loads the relevant word forms from <code>rules/ca/replace_adverbs_ment.txt</code>.
 * 
 * @author Jaume Ortolà
 */
public class SimpleReplaceAdverbsMent extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongWords = loadFromPath("/ca/replace_adverbs_ment.txt");
  private static final Locale CA_LOCALE = new Locale("CA");

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }
  
  public SimpleReplaceAdverbsMent(final ResourceBundle messages) throws IOException {
    super(messages);
    super.setCategory(new Category(new CategoryId("PICKY_STYLE"), "regles d'estil, mode perfeccionaista"));
    super.setLocQualityIssueType(ITSIssueType.Style);
    super.setDefaultOff();
    this.setCheckLemmas(false);
    setTags(Arrays.asList(Tag.picky));
    super.setUrl(new URL("https://esadir.cat/gramatica/sintaxi/advermanera"));
  }  

  @Override
  public final String getId() {
    return "ADVERBIS_MENT";
  }

 @Override
  public String getDescription() {
    return "Alternatives a adverbis acabants en -ment.";
  }

  @Override
  public String getShort() {
    return "Alternatives a adverbis acabants en -ment";
  }
  
  @Override
  public String getMessage(String tokenStr,List<String> replacements) {
    return "A vegades s'abusa dels adverbis acabats en -ment en detriment de formes més àgils.";
  }
  
  @Override
  public boolean isCaseSensitive() {
    return false;
  }
  
  @Override
  public Locale getLocale() {
    return CA_LOCALE;
  }

}
