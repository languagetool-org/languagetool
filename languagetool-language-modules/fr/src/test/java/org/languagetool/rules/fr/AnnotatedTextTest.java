/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fr;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.French;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;

public class AnnotatedTextTest {

  private final JLanguageTool lt = new JLanguageTool(new French());

  @Ignore("activate when #2118 is fixed")
  @Test
  public void test1() throws IOException {
    //"échapatoire" should be "échappatoire" with two 'p'
    String textToCheck = "Une &eacute;chapatoire est possible.";

    AnnotatedTextBuilder builder = new AnnotatedTextBuilder()
      .addText("Une ")
      .addMarkup("&eacute;", "é")
      .addText("chapatoire est possible.");
    RuleMatch match = lt.check(builder.build()).get(0);
    
    String markedWord = textToCheck.substring(match.getFromPos(), match.getToPos());
    String wordThanShouldBeHighlighted = "&eacute;chapatoire";
    assertThat(markedWord, is(wordThanShouldBeHighlighted));
  }

  @Ignore("activate when #2118 is fixed")
  @Test
  public void test2() throws IOException {
    //"trouuvé" should be "trouvé"
    String textToCheck = "J'ai trouuv&eacute; le livre.";

    AnnotatedTextBuilder builder = new AnnotatedTextBuilder()
      .addText("J'ai trouuv")
      .addMarkup("&eacute;", "é")
      .addText(" le livre.");
    RuleMatch match = lt.check(builder.build()).get(0);
    
    String markedWord = textToCheck.substring(match.getFromPos(), match.getToPos());
    String wordThanShouldBeHighlighted = "trouuv&eacute;";
    assertThat(markedWord, is(wordThanShouldBeHighlighted));
  }
  
}
