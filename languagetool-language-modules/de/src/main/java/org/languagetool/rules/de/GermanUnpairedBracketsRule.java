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
import org.languagetool.rules.GenericUnpairedBracketsRule;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public class GermanUnpairedBracketsRule extends GenericUnpairedBracketsRule {

  private static final List<String> DE_START_SYMBOLS = Arrays.asList("[", "(", "{", "„", "»", "«", "\"");
  private static final List<String> DE_END_SYMBOLS   = Arrays.asList("]", ")", "}", "“", "«", "»", "\"");

  public GermanUnpairedBracketsRule(ResourceBundle messages, Language language) {
    super(messages, DE_START_SYMBOLS, DE_END_SYMBOLS);
    addExamplePair(Example.wrong("Dem Präsidenten des Deutschen Bauernverbands <marker>(</marker>DBV zufolge habe die Dürre einen Schaden von 1,4 Millionen verursacht."),
                   Example.fixed("Dem Präsidenten des Deutschen Bauernverbands <marker>(</marker>DBV) zufolge habe die Dürre einen Schaden von 1,4 Millionen verursacht."));
  }

  @Override
  public String getId() {
    return "UNPAIRED_BRACKETS";
  }  // no "DE_" to be compatible with old versions

  @Override
  protected List<String> getSuggestions(Supplier<String> text, int startPos, int endPos) {
    if (startPos > 0 && endPos <= text.get().length()) {
      String prevCh = text.get().substring(startPos-1, endPos-1);
      String ch = text.get().substring(startPos, endPos);
      if (prevCh.equals(" ") && ch.equals("“")) {
        return Arrays.asList("„");
      }
      if (prevCh.equals("\u00a0") && ch.equals("“")) {
        return Arrays.asList("„");
      }
    }
    if (startPos == 0 && endPos <= text.get().length()) {
      String ch = text.get().substring(startPos, endPos);
      if (ch.equals("“")) {
        return Arrays.asList("„");
      }
    }
    return null;
  }

}
