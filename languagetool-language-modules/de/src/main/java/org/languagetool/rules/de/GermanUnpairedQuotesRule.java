/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://danielnaber.de)
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
package org.languagetool.rules.de;

import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.GenericUnpairedQuotesRule;
import org.languagetool.tools.Tools;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class GermanUnpairedQuotesRule extends GenericUnpairedQuotesRule {

  private static final List<String> DE_START_SYMBOLS = Arrays.asList("„", "»", "«", "\"", "'", "‚", "›", "‹");
  private static final List<String> DE_END_SYMBOLS   = Arrays.asList("“", "«", "»", "\"", "'", "‘", "‹", "›");

  public GermanUnpairedQuotesRule(ResourceBundle messages, Language language) {
    super(messages, DE_START_SYMBOLS, DE_END_SYMBOLS);
    setUrl(Tools.getUrl("https://languagetool.org/insights/de/beitrag/klammern/"));
    addExamplePair(Example.wrong("»Hallo Hans ist das dein <marker>›</marker>neues Auto?«, fragte er."),
                   Example.fixed("»Hallo Hans ist das dein <marker>›</marker>neues‹ Auto?«, fragte er."));
  }

  @Override
  public String getId() {
    return "DE_UNPAIRED_QUOTES";
  }


}
