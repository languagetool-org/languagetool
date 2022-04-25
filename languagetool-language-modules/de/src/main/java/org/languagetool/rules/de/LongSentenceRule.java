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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Tag;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A rule that warns on long sentences.
 * @since 3.9
 */
public class LongSentenceRule extends org.languagetool.rules.LongSentenceRule {

  public LongSentenceRule(ResourceBundle messages, UserConfig userConfig, int defaultWords) {
    super(messages, userConfig, defaultWords);
    addExamplePair(Example.wrong("<marker>Dies ist ein Bandwurmsatz, der immer weiter geht, obwohl das kein guter Stil ist, den man eigentlich berücksichtigen sollte, obwohl es auch andere Meinungen gibt, die aber in der Minderzahl sind, weil die meisten Autoren sich doch an die Stilvorgaben halten, wenn auch nicht alle, was aber letztendlich wiederum eine Sache des Geschmacks ist</marker>."),
                   Example.fixed("<marker>Dies ist ein kurzer Satz.</marker>"));
  }

  @Override
  public String getDescription() {
    return "Findet lange Sätze";
  }

  @Override
  public String getId() {
    return "TOO_LONG_SENTENCE_DE";
  }

}
