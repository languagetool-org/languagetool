/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.junit.Test;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.English;
import org.languagetool.language.German;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JLanguageToolTest {

  @Test
  public void testGetMessageBundle() throws Exception {
    final ResourceBundle bundle1 = JLanguageTool.getMessageBundle(new German());
    assertThat(bundle1.getString("de"), is("Deutsch"));

    final ResourceBundle bundle2 = JLanguageTool.getMessageBundle(new English());
    assertThat(bundle2.getString("de"), is("German"));

    final ResourceBundle bundle3 = JLanguageTool.getMessageBundle(new AmericanEnglish());
    assertThat(bundle3.getString("de"), is("German"));
  }

  @Test
  public void testCountLines() {
    assertEquals(0, JLanguageTool.countLineBreaks(""));
    assertEquals(1, JLanguageTool.countLineBreaks("Hallo,\nnächste Zeile"));
    assertEquals(2, JLanguageTool.countLineBreaks("\nZweite\nDritte"));
    assertEquals(4, JLanguageTool.countLineBreaks("\nZweite\nDritte\n\n"));
  }

  @Test
  public void testSentenceTokenize() throws IOException {
    JLanguageTool languageTool = new JLanguageTool(new English());
    List<String> sentences = languageTool.sentenceTokenize("This is a sentence! This is another one.");
    assertEquals(2, sentences.size());
    assertEquals("This is a sentence! ", sentences.get(0));
    assertEquals("This is another one.", sentences.get(1));
  }

  @Test
  public void testAnnotateTextCheck() throws IOException {
    JLanguageTool languageTool = new JLanguageTool(new English());
    AnnotatedText annotatedText = new AnnotatedTextBuilder()
            .addMarkup("<b>")
            .addText("here")
            .addMarkup("</b>")
            .addText(" is an error")
            .build();
    List<RuleMatch> matches = languageTool.check(annotatedText);
    assertThat(matches.size(), is(1));
    assertThat(matches.get(0).getFromPos(), is(3));
    assertThat(matches.get(0).getToPos(), is(7));
  }

  @Test
  public void testAnnotateTextCheckMultipleSentences() throws IOException {
    JLanguageTool languageTool = new JLanguageTool(new English());
    AnnotatedText annotatedText = new AnnotatedTextBuilder()
            .addMarkup("<b>")
            .addText("here")
            .addMarkup("</b>")
            .addText(" is an error. And ")
            .addMarkup("<i attr='foo'>")
            .addText("here is also")
            .addMarkup("</i>")
            .addText(" a error.")
            .build();
    List<RuleMatch> matches = languageTool.check(annotatedText);
    assertThat(matches.size(), is(2));
    assertThat(matches.get(0).getFromPos(), is(3));
    assertThat(matches.get(0).getToPos(), is(7));
    assertThat(matches.get(1).getFromPos(), is(60));
    assertThat(matches.get(1).getToPos(), is(61));
  }

  @Test
  public void testAnnotateTextCheckMultipleSentences2() throws IOException {
    JLanguageTool languageTool = new JLanguageTool(new English());
    AnnotatedText annotatedText = new AnnotatedTextBuilder()
            .addText("here")
            .addText(" is an error. And ")
            .addMarkup("<i attr='foo'/>")
            .addText("here is also ")
            .addMarkup("<i>")
            .addText("a")
            .addMarkup("</i>")
            .addText(" error.")
            .build();
    List<RuleMatch> matches = languageTool.check(annotatedText);
    assertThat(matches.size(), is(2));
    assertThat(matches.get(0).getFromPos(), is(0));
    assertThat(matches.get(0).getToPos(), is(4));
    assertThat(matches.get(1).getFromPos(), is(53));
    assertThat(matches.get(1).getToPos(), is(54));
  }

  @Test
  public void testAnnotateTextCheckPlainText() throws IOException {
    JLanguageTool languageTool = new JLanguageTool(new English());
    AnnotatedText annotatedText = new AnnotatedTextBuilder()
            .addText("A good sentence. But here's a error.").build();
    List<RuleMatch> matches = languageTool.check(annotatedText);
    assertThat(matches.size(), is(1));
    assertThat(matches.get(0).getFromPos(), is(28));
    assertThat(matches.get(0).getToPos(), is(29));
  }

}
