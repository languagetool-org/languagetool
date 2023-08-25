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
 * A rule that matches redundant expression. 
 * Portuguese implementations. Loads the list of words from
 * <code>/pt/redundancies.txt</code>.
 *
 * @author Tiago F. Santos 
 * @since 3.8
 */
public class PortugueseRedundancyRule extends AbstractSimpleReplaceRule2 {

  public static final String PT_REDUNDANCY_REPLACE = "PT_REDUNDANCY_REPLACE";

  private static final Locale PT_LOCALE = new Locale("pt");  // locale used on case-conversion

  private final String path;

  @Override
  public List<String> getFileNames() {
    return Collections.singletonList(path);
  }

  public PortugueseRedundancyRule(ResourceBundle messages, String path) {
    super(messages, new Portuguese());
    this.path = Objects.requireNonNull(path);
    super.setCategory(Categories.REDUNDANCY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    useSubRuleSpecificIds();
    addExamplePair(Example.wrong("<marker>duna de areia</marker>"),
                   Example.fixed("<marker>duna</marker>"));
  }

  @Override
  public String getId() {
    return PT_REDUNDANCY_REPLACE;
  }

  @Override
  public String getDescription() {
    return "1. Pleonasmos e redundâncias";
  }

  @Override
  public String getShort() {
    return "Pleonasmo";
  }

  @Override
  public String getMessage() {
    return "\"$match\" é um pleonasmo. É preferível dizer $suggestions";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " ou ";
  }

  @Override
  public URL getUrl() {
    return Tools.getUrl("https://pt.wikipedia.org/wiki/Pleonasmo");
  }

  @Override
  public Locale getLocale() {
    return PT_LOCALE;
  }

}
