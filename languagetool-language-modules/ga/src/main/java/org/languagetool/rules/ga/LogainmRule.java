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
package org.languagetool.rules.ga;

import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A rule that matches words which should not be used and suggests
 * correct ones instead. 
 * 
 * Irish version for English placenames
 * relevant words from <code>rules/ga/placenames.txt</code>.
 * 
 * @author Jim O'Regan
 */
public class LogainmRule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongWords = loadFromPath("/ga/placenames.txt");
  private static final Locale GA_LOCALE = new Locale("GA");

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }
  
  public LogainmRule(final ResourceBundle messages) throws IOException {
    super(messages);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    super.setLocQualityIssueType(ITSIssueType.Misspelling);
    this.setIgnoreTaggedWords();
    this.setCheckLemmas(false);
  }  

  @Override
  public final String getId() {
    return "GA_LOGAINM";
  }

 @Override
  public String getDescription() {
    return "Logainm Béarla, m.sh., 'Dublin' in áit 'Baile Átha Cliath'.";
  }

  @Override
  public String getShort() {
    return "Logainm Béarla";
  }
 
  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return "Is logainm Béarla é \"" + tokenStr + "\" ar \""
        + String.join(", ", replacements) + "\".";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }
 
  @Override
  public URL getUrl() {
    return Tools.getUrl("https://www.logainm.ie/ga/");
  }

  @Override
  public Locale getLocale() {
    return GA_LOCALE;
  }

}
