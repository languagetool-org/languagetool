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

import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.French;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnnotatedTextTest {

  private final JLanguageTool lt = new JLanguageTool(new French());

  @Test
  public void testInterpretAsBefore() throws IOException {
    //"échapatoire" should be "échappatoire" with two 'p'
    String textToCheck = "Une &eacute;chapatoire est possible.";

    AnnotatedTextBuilder builder = new AnnotatedTextBuilder()
      .addText("Une ")
      .addMarkup("&eacute;", "é")
      .addText("chapatoire est possible.");

    RuleMatch match = lt.check(builder.build()).get(0);
    String markedWord = textToCheck.substring(match.getFromPos(), match.getToPos());
    assertThat(markedWord, is("&eacute;chapatoire"));
  }

  @Test
  public void testInterpretAsAfter() throws IOException {
    //"trouuvé" should be "trouvé"
    String textToCheck = "J'ai trouuv&eacute; le livre.";

    AnnotatedTextBuilder builder = new AnnotatedTextBuilder()
      .addText("J'ai trouuv")
      .addMarkup("&eacute;", "é")
      .addText(" le livre.");
    
    RuleMatch match = lt.check(builder.build()).get(0);
    String markedWord = textToCheck.substring(match.getFromPos(), match.getToPos());
    assertThat(markedWord, is("trouuv&eacute;"));
  }


  @Test
  public void testWithSimpleMarkup() throws IOException {
    //"louper" should be "loupé"
    String textToCheck = "J'ai louper le train.<span> Ce n'était pas dans mes habitudes.</span>";

    AnnotatedTextBuilder builder = new AnnotatedTextBuilder()
      .addText("J'ai louper le train.")
      .addMarkup("<span>")
      .addText(" Ce n'était pas dans mes habitudes.")
      .addMarkup("</span>");

    RuleMatch match = lt.check(builder.build()).get(0);
    String markedWord = textToCheck.substring(match.getFromPos(), match.getToPos());
    assertThat(markedWord, is("louper"));
  }

  @Test
  public void testWithMultipleSimpleMarkup() throws IOException {
    //"louper" should be "loupé"
    String textToCheck = "J'ai louper le train.<span> Ce n'était pas dans mes habitudes.</span>";

    AnnotatedTextBuilder builder = new AnnotatedTextBuilder()
      .addText("J'ai louper le train.")
      .addMarkup("<span>")
      .addText(" Ce n'était pas dans mes habitudes.")
      .addMarkup("</span>")
      .addMarkup("<span>")
      .addMarkup("</span>");

    RuleMatch match = lt.check(builder.build()).get(0);
    String markedWord = textToCheck.substring(match.getFromPos(), match.getToPos());
    assertThat(markedWord, is("louper"));
  }

  @Test
  public void testWithFakeMarkupInSimpleMarkupeMarkup() throws IOException {
    //"échapatoire" should be "échappatoire" with two 'p'
    String textToCheck = "Une <span class='red'>&eacute;chapatoire</span> est possible.";

    AnnotatedTextBuilder builder = new AnnotatedTextBuilder()
      .addText("Une ")
      .addMarkup("<span class='red'>")
      .addMarkup("&eacute;", "é")
      .addText("chapatoire")
      .addMarkup("</span>")
      .addText(" est possible.");

    RuleMatch match = lt.check(builder.build()).get(0);
    String markedWord = textToCheck.substring(match.getFromPos(), match.getToPos());
    assertThat(markedWord, is("&eacute;chapatoire"));
  }


  @Test
  public void testWithBr() throws IOException {
    //"louper" should be "loupé"
    String textToCheck = "J'ai louper le train.<br/> Ce n'était pas dans mes habitudes.";

    AnnotatedTextBuilder builder = new AnnotatedTextBuilder()
      .addText("J'ai louper le train.")
      .addMarkup("<br/>", "\n")
      .addText(" Ce n'était pas dans mes habitudes.");

    RuleMatch match = lt.check(builder.build()).get(0);
    String markedWord = textToCheck.substring(match.getFromPos(), match.getToPos());
    assertThat(markedWord, is("louper"));
  }

}
