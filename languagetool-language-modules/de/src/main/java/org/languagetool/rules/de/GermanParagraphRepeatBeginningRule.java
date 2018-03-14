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

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Example;
import org.languagetool.rules.ParagraphRepeatBeginningRule;

/**
 * Check if to paragraphs begin with the same word.
 * If the first word is an article it checks if the first two words are identical
 * 
 * @author Fred Kruse
 * @since 4.1
 */
public class GermanParagraphRepeatBeginningRule extends ParagraphRepeatBeginningRule {

  public GermanParagraphRepeatBeginningRule(ResourceBundle messages) {
    super(messages);
//    addExamplePair(Example.wrong("<marker>Der Hund</marker> spazierte über die Straße.\n<marker>Der Hund</marker> ignorierte den Verkehr."),
//                   Example.fixed("<marker>Der Hund</marker> spazierte über die Straße.\n<marker>Der Vogel</marker> ignorierte den Verkehr."));
  }

  @Override
  public String getId() {
    return "GERMAN_PARAGRAPH_REPEAT_BEGINNING_RULE";
  }
  
  public boolean isArticle(AnalyzedTokenReadings token) {
    return token.hasPosTagStartingWith("ART");
  }

}
