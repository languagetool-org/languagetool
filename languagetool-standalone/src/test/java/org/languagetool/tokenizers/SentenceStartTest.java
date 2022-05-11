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
package org.languagetool.tokenizers;


import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class SentenceStartTest {

  private final JLanguageTool lt =  new JLanguageTool(new BritishEnglish());

  /*
   * Test case for https://github.com/languagetool-org/languagetool/issues/1919
   */


  /**
   * This test case always pass.
   */
  @Test
  public void testSentenceStartWithLineBreak() throws IOException {
    {
      AnnotatedText txt  = new AnnotatedTextBuilder()
        .addText("Hello.\nlong time no see.").build();
      List<RuleMatch> matches = lt.check(txt);
      assertEquals(1, matches.size());
      assertEquals("This sentence does not start with an uppercase letter.",matches.get(0).getMessage());
    }
    {
      AnnotatedText txt  = new AnnotatedTextBuilder()
        .addText("Hello!\nlong time no see.").build();
      List<RuleMatch> matches = lt.check(txt);
      assertEquals(1, matches.size());
      assertEquals("This sentence does not start with an uppercase letter.",matches.get(0).getMessage());
    }
    {
      AnnotatedText txt  = new AnnotatedTextBuilder()
        .addText("Hello?\nlong time no see.").build();
      List<RuleMatch> matches = lt.check(txt);
      assertEquals(1, matches.size());
      assertEquals("This sentence does not start with an uppercase letter.",matches.get(0).getMessage());
    }
  }

  /**
   * This test case would failed before fixing the bug.
   */
  @Test
  public void testSentenceStartWithSpaceAndLineBreak() throws IOException {
    {
      AnnotatedText txt  = new AnnotatedTextBuilder()
        .addText("Hello. \nlong time no see.").build();
      List<RuleMatch> matches = lt.check(txt);
      assertEquals(1, matches.size());
      assertEquals("This sentence does not start with an uppercase letter.",matches.get(0).getMessage());
    }
    {
      AnnotatedText txt  = new AnnotatedTextBuilder()
        .addText("Hello! \nlong time no see.").build();
      List<RuleMatch> matches = lt.check(txt);
      assertEquals(1, matches.size());
      assertEquals("This sentence does not start with an uppercase letter.",matches.get(0).getMessage());
    }
    {
      AnnotatedText txt  = new AnnotatedTextBuilder()
        .addText("Hello? \nlong time no see.").build();
      List<RuleMatch> matches = lt.check(txt);
      assertEquals(1, matches.size());
      assertEquals("This sentence does not start with an uppercase letter.",matches.get(0).getMessage());
    }
  }


  /**
   * This test case would failed before fixing the bug.
   * (Two or more consecutive line breaks need to be considered to start a new sentence)
   */
  @Test
  public void testSentenceStartWithMultipleLineBreak() throws IOException {

    {
      AnnotatedText txt  = new AnnotatedTextBuilder()
        .addText("Hello and welcome, \n\n\nsorry I have to reply in English.").build();
      List<RuleMatch> matches = lt.check(txt);
      assertEquals(1, matches.size());
      assertEquals("This sentence does not start with an uppercase letter.",matches.get(0).getMessage());
    }
    {
      AnnotatedText txt  = new AnnotatedTextBuilder()
        .addText("Hello and welcome,\n\t \n \r\nsorry I have to reply in English.").build();
      List<RuleMatch> matches = lt.check(txt);
      assertEquals(1, matches.size());
      assertEquals("This sentence does not start with an uppercase letter.",matches.get(0).getMessage());

    }
  }


  @Test
  public void testSentenceStartWithTitle() throws IOException {
    {

      AnnotatedText txt  = new AnnotatedTextBuilder()
        .addText("(1) English \nit is good.").build();
      List<RuleMatch> matches = lt.check(txt);
      assertEquals(1, matches.size());
      assertEquals("This sentence does not start with an uppercase letter.",matches.get(0).getMessage());
    }
  }
}


