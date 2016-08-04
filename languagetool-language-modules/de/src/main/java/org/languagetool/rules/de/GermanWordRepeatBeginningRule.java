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
package org.languagetool.rules.de;

import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatBeginningRule;

/**
 * Extends {@link WordRepeatBeginningRule} by a list of German adverbs ("Konjunktionaladverben")
 * for a more precise error message.
 * 
 * @author Markus Brenneis
 */
public class GermanWordRepeatBeginningRule extends WordRepeatBeginningRule {

  private static final Set<String> ADVERBS = new HashSet<>(Arrays.asList(
          "Auch", "Anschließend", "Außerdem", "Danach", "Ferner",
          "Nebenher", "Nebenbei", "Überdies", "Weiterführend", "Zudem", "Zusätzlich"
  ));

  public GermanWordRepeatBeginningRule(ResourceBundle messages, Language language) {
    super(messages, language);
    addExamplePair(Example.wrong("Dann hatten wir Freizeit. Dann gab es Essen. <marker>Dann</marker> gingen wir schlafen."),
                   Example.fixed("Dann hatten wir Freizeit. Danach gab es Essen. <marker>Schließlich</marker> gingen wir schlafen."));
  }
  
  @Override
  public String getId() {
    return "GERMAN_WORD_REPEAT_BEGINNING_RULE";
  }
  
  @Override
  protected boolean isAdverb(AnalyzedTokenReadings token) {
    return ADVERBS.contains(token.getToken());
  }

}
