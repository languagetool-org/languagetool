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
package org.languagetool.rules.nl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.language.Dutch;
import org.languagetool.rules.*;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * Dutch implementations. Loads the list of words from
 * <code>rules/nl/replace.txt</code>.
 * 
 * @since 2.7
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule2 {

  public static final String DUTCH_SIMPLE_REPLACE_RULE = "NL_SIMPLE_REPLACE";
  
  private static final Locale NL_LOCALE = new Locale("nl");

  public SimpleReplaceRule(ResourceBundle messages) throws IOException {
    super(messages, new Dutch());
    useSubRuleSpecificIds();
    setLocQualityIssueType(ITSIssueType.Misspelling);
    setCategory(new Category(new CategoryId("VERGISSINGEN"), "Vergissingen"));
    addExamplePair(Example.wrong("<marker>klaa</marker>."),
                   Example.fixed("<marker>klaar</marker>."));
  }

  @Override
  public CaseSensitivy getCaseSensitivy() {
    // edit by R. Baars 19-11-2022 to make routine case sensitive
    return CaseSensitivy.CS;
  }

  @Override
  public List<String> getFileNames() {
    return Collections.singletonList("/nl/replace.txt");
  }

  @Override
  public final String getId() {
    return DUTCH_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "Snelle correctie van veel voorkomende vergissingen";
  }

  @Override
  public String getDescription(String details) {
    if (details != null) {
      return "Snelle correctie van veel voorkomende vergissingen: " + details;
    } else {
      return getDescription();
    }
  }

  @Override
  public String getShort() {
    return "Vergissing?";
  }

  @Override
  public String getMessage() {
    return "'$match' zou fout kunnen zijn. Misschien bedoelt u: $suggestions";
  }

  @Override
  public String getSuggestionsSeparator() {
    return ", ";
  }

  @Override
  public Locale getLocale() {
    return NL_LOCALE;
  }

}
