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
public class LongSentenceRule extends org.languagetool.rules.LongSentenceRule {

    private static final int DEFAULT_MAX_WORDS = 40;
    private static final boolean DEFAULT_INACTIVE = true;

    /**
     * @param defaultActive allows default granularity
     * @since 3.7
     */
    public LongSentenceRule(ResourceBundle messages, int maxSentenceLength, boolean defaultActive) {
      super(messages);
      super.setCategory(Categories.STYLE.getCategory(messages));
      setLocQualityIssueType(ITSIssueType.Style);
      addExamplePair(Example.wrong("<marker>Dies ist ein Bandwurmsatz, der immer weiter geht, obwohl das kein guter Stil ist, den man eigentlich berücksichtigen sollte, obwohl es auch andere Meinungen gibt, die aber in der Minderzahl sind, weil die meisten Autoren sich doch an die Stilvorgaben halten, wenn auch nicht alle, was aber letztendlich wiederum eine Sache des Geschmacks ist.</marker>"),
                Example.fixed("<marker>Dies ist ein kurzer Satz.</marker>"));
      if (defaultActive) {
          setDefaultOn();
        }
      maxWords = maxSentenceLength;
    }

    /**
     * @param maxSentenceLength the maximum sentence length that does not yet trigger a match
     * @since 2.4
     */
    public LongSentenceRule(ResourceBundle messages, int maxSentenceLength) {
      this(messages, maxSentenceLength, DEFAULT_INACTIVE);
    }

    /**
     * Creates a rule with the default maximum sentence length (40 words).
     */
    public LongSentenceRule(ResourceBundle messages) {
      this(messages, DEFAULT_MAX_WORDS, DEFAULT_INACTIVE);
      setDefaultOn();
    }


  @Override
    public String getId() {
      return "DE_TOO_LONG_SENTENCE_" + maxWords;
    }

    @Override
    public String getDescription() {
      return "Sehr langer Satz (mehr als " + maxWords + " Worte)";
    }

    @Override
    public String getMessage() {
        return "Dieser Satz ist sehr lang (mehr als " + maxWords + " Worte).";
    }

}
