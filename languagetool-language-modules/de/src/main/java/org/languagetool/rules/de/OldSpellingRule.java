/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.*;

import java.io.IOException;
import java.util.*;

/**
 * Finds spellings that were only correct in the pre-reform orthography.
 * @since 3.8
 */
public class OldSpellingRule extends Rule {

  private static final String DESC = "Findet Schreibweisen, die nur in der alten Rechtschreibung gültig waren";
  private static final String FILE_PATH = "/de/alt_neu.csv";
  private static final String MESSAGE = "Diese Schreibweise war nur in der alten Rechtschreibung korrekt.";
  private static final String SHORT_MESSAGE = "alte Rechtschreibung";
  private static final String RULE_INTERNAL = "OLD_SPELLING_INTERNAL";
  private static final ITSIssueType ISSUE_TYPE = ITSIssueType.Misspelling;
  private static final SpellingData DATA = new SpellingData(DESC, FILE_PATH, MESSAGE, SHORT_MESSAGE, RULE_INTERNAL, ISSUE_TYPE);

  public OldSpellingRule(ResourceBundle messages) {
    super.setCategory(Categories.TYPOS.getCategory(messages));
    setLocQualityIssueType(ISSUE_TYPE);
    addExamplePair(Example.wrong("Der <marker>Abfluß</marker> ist schon wieder verstopft."),
                   Example.fixed("Der <marker>Abfluss</marker> ist schon wieder verstopft."));
  }

  @Override
  public String getId() {
    return "OLD_SPELLING";
  }

  @Override
  public String getDescription() {
    return DESC;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    String[] exceptions = {"Schloß Holte"};
    return toRuleMatchArray(SpellingRuleWithSuggestions.computeMatches(sentence, DATA, exceptions));
  }
  
}
