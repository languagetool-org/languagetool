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
package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Category;
import org.languagetool.rules.ITSIssueType;

/**
 * A rule that matches words which should not be used and suggests
 * correct ones instead. 
 * 
 * Catalan implementations. Loads the
 * relevant words from <code>rules/ca/replace.txt</code>.
 * 
 * @author Jaume Ortolà
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule {

  private static final String FILE_NAME = "/ca/replace.txt";
  // locale used on case-conversion
  private static final Locale CA_LOCALE = new Locale("CA");

  @Override
  public final String getFileName() {
    return FILE_NAME;
  }
  
  public SimpleReplaceRule(final ResourceBundle messages) throws IOException {
    super(messages);
    super.setCategory(new Category("Errors ortogràfics"));
    super.setLocQualityIssueType(ITSIssueType.Misspelling);
    this.setIgnoreTaggedWords();
  }  

  @Override
  public final String getId() {
    return "CA_SIMPLE_REPLACE";
  }

 @Override
  public String getDescription() {
    return "Detecta paraules incorrectes i proposa suggeriments de canvi";
  }

  @Override
  public String getShort() {
    return "Paraula incorrecta";
  }
  
  @Override
  public String getMessage(String tokenStr,List<String> replacements) {
    return "Paraula incorrecta.";
  }
  
  /**
   * use case-insensitive matching.
   */
  @Override
  public boolean isCaseSensitive() {
    return false;
  }
  
  /**
   * locale used on case-conversion
   */
  @Override
  public Locale getLocale() {
    return CA_LOCALE;
  }

}
