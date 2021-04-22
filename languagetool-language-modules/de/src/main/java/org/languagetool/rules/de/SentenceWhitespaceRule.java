/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.rules.*;

import java.util.ResourceBundle;

/**
 * Checks that there's whitespace between sentences etc.
 *
 * @author Daniel Naber
 * @since 2.8
 */
public class SentenceWhitespaceRule extends org.languagetool.rules.SentenceWhitespaceRule {

  public SentenceWhitespaceRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.MISC.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Whitespace);
    addExamplePair(Example.wrong("Hier steht ein Satz.<marker>Das</marker> ist ein weiterer Satz."),
                   Example.fixed("Hier steht ein Satz.<marker> Das</marker> ist ein weiterer Satz."));
  }

  @Override
  public String getId() {
    return "DE_SENTENCE_WHITESPACE";
  }

  @Override
  public String getDescription() {
    return "Fehlendes Leerzeichen zwischen Sätzen oder nach Ordnungszahlen";
  }

  @Override
  public String getMessage(boolean prevSentenceEndsWithNumber) {
    if (prevSentenceEndsWithNumber) {
      return "Fügen Sie nach Ordnungszahlen (1., 2. usw.) ein Leerzeichen ein";
    } else {
      return "Fügen Sie zwischen Sätzen ein Leerzeichen ein";
    }
  }

}
