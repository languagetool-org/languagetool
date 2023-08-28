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

import org.languagetool.language.Catalan;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A rule that matches words which should not be used and suggests correct ones
 * instead.
 * 
 * Loads the relevant words from <code>rules/ca/replace_anglicism.txt</code>.
 * 
 * @author Jaume Ortolà
 */
public class SimpleReplaceMultiwordsRule extends AbstractSimpleReplaceRule2 {

  private static final String FILE_NAME = "/ca/replace_multiwords.txt";
  private static final Locale CA_LOCALE = new Locale("ca");

  public SimpleReplaceMultiwordsRule(final ResourceBundle messages) throws IOException {
    super(messages, new Catalan());
    super.setCategory(Categories.GRAMMAR.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Grammar);
    super.useSubRuleSpecificIds();
  }

  @Override
  public final String getId() {
    return "CA_SIMPLE_REPLACE_MULTIWORDS";
  }

  @Override
  public String getDescription() {
    return "Expressions inadequades: $match";
  }

  @Override
  public String getShort() {
    return "Expressió inadequada";
  }

  @Override
  public Locale getLocale() {
    return CA_LOCALE;
  }

  @Override
  public List<String> getFileNames() {
    return Arrays.asList(FILE_NAME);
  }

  @Override
  public String getMessage() {
    return "Expressió incorrecta.";
  }

  @Override
  public URL getUrl() {
    return null;
  }

}