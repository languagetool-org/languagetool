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
package org.languagetool.tagging;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Demo;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

public class MorfologikTaggerTest {

  @Test
  public void testTag() {
    URL url = MorfologikTaggerTest.class.getResource("/org/languagetool/tagging/test.dict");
    MorfologikTagger tagger = new MorfologikTagger(url);

    List<TaggedWord> result1 = tagger.tag("lowercase");
    assertThat(result1.size(), is(2));
    assertThat(result1.get(0).getLemma(), is("lclemma"));
    assertThat(result1.get(0).getPosTag(), is("POS1"));
    assertThat(result1.get(1).getLemma(), is("lclemma2"));
    assertThat(result1.get(1).getPosTag(), is("POS1a"));

    List<TaggedWord> result2 = tagger.tag("Lowercase");
    assertThat(result2.size(), is(0));

    List<TaggedWord> result3 = tagger.tag("schön");
    assertThat(result3.size(), is(1));
    assertThat(result3.get(0).getLemma(), is("testlemma"));
    assertThat(result3.get(0).getPosTag(), is("POSTEST"));

    List<TaggedWord> noResult = tagger.tag("noSuchWord");
    assertThat(noResult.size(), is(0));
  }

  @Test
  public void testPositionWithIgnoredChars() throws IOException {
    Demo demoLanguage = new Demo();
    JLanguageTool languageTool = new JLanguageTool(demoLanguage);
    String text = "t\u00ADox te\u00ADstx";
    AnalyzedSentence analyzedSent = languageTool.getRawAnalyzedSentence(text);
    assertNotNull(analyzedSent.getTokens()[3].getToken());
    assertEquals("SENT_END", analyzedSent.getTokens()[3].getAnalyzedToken(0).getPOSTag());
    assertEquals(text.indexOf("te\u00ADst"), analyzedSent.getTokens()[3].getStartPos());
    assertEquals(text.length(), analyzedSent.getTokens()[3].getEndPos());
  }

}
