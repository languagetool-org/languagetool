package org.languagetool.rules.es;

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

import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A rule that matches words which should not be used and suggests
 * correct ones instead. 
 * 
 * Loads the relevant words from <code>rules/es/replace.txt</code>.
 * 
 * @author Jaume Ortolà
 */
public class SimpleReplaceAnglicismRule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongWords = loadFromPath("/es/replace_anglicism.txt");
  private static final Locale ES_LOCALE = new Locale("ES");

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }
  
  public SimpleReplaceAnglicismRule(final ResourceBundle messages) throws IOException {
    super(messages);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    super.setLocQualityIssueType(ITSIssueType.Misspelling);
    //this.setIgnoreTaggedWords();
    this.setCheckLemmas(false);
  }  

  @Override
  public final String getId() {
    return "ES_SIMPLE_REPLACE_ANGLICISM";
  }

 @Override
  public String getDescription() {
    return "Anglicismos incesarios";
  }

  @Override
  public String getShort() {
    return "Anglicismo innecesario";
  }
  
  @Override
  public String getMessage(String tokenStr,List<String> replacements) {
    return "Anglicismo innecesario.";
  }
  
  @Override
  public boolean isCaseSensitive() {
    return false;
  }
  
  @Override
  public Locale getLocale() {
    return ES_LOCALE;
  }

}
