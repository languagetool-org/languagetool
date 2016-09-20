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
package org.languagetool.tagging.disambiguation.rules.uk;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.MultiWordChunker;
import org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest;
import org.languagetool.tagging.disambiguation.uk.UkrainianHybridDisambiguator;
import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import org.languagetool.tagging.uk.UkrainianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.uk.UkrainianWordTokenizer;

import static org.junit.Assert.assertTrue;

public class UkrainianDisambiguationRuleTest extends DisambiguationRuleTest {
  
  private UkrainianTagger tagger;
  private UkrainianWordTokenizer tokenizer;
  private SRXSentenceTokenizer sentenceTokenizer;
  private UkrainianHybridDisambiguator disambiguator;
  private DemoDisambiguator demoDisambiguator;
  private Disambiguator chunker;

  @Before
  public void setUp() {
    tagger = new UkrainianTagger();
    tokenizer = new UkrainianWordTokenizer();
    sentenceTokenizer = new SRXSentenceTokenizer(new Ukrainian());
    disambiguator = new UkrainianHybridDisambiguator();
    demoDisambiguator = new DemoDisambiguator();
    chunker = new MultiWordChunker("/uk/multiwords.txt", true);
  }

  @Test
  public void testDisambiguator() throws IOException {

    TestTools.myAssert("Танцювати до впаду", 
      "/[null]SENT_START Танцювати/[танцювати]verb:imperf:inf  /[null]null до/[до впаду]<adv>|до/[до]prep:rv_rod  /[null]null " +
      "впаду/[впасти]verb:perf:futr:s:1:xp2|впаду/[до впаду]</adv>",
      tokenizer, sentenceTokenizer, tagger, disambiguator);
    
    TestTools.myAssert("Прийшла Люба додому.", 
      "/[null]SENT_START Прийшла/[прийти]verb:perf:past:f|Прийшла/[прийшлий]adj:f:v_kly|Прийшла/[прийшлий]adj:f:v_naz  /[null]null Люба/[Люба]noun:anim:f:v_naz:prop:fname|Люба/[любий]adj:f:v_kly|Люба/[любий]adj:f:v_naz  /[null]null додому/[додому]adv ./[null]null",
       tokenizer, sentenceTokenizer, tagger, demoDisambiguator);

    TestTools.myAssert("Прийшла Люба додому.", 
      "/[null]SENT_START Прийшла/[прийти]verb:perf:past:f|Прийшла/[прийшлий]adj:f:v_kly|Прийшла/[прийшлий]adj:f:v_naz  /[null]null Люба/[Люба]noun:anim:f:v_naz:prop:fname  /[null]null додому/[додому]adv ./[null]null",
       tokenizer, sentenceTokenizer, tagger, disambiguator);
  }

  @Test
  public void testDisambiguatorForInitials() throws IOException {
    TestTools.myAssert("Є.Бакуліна",
      "/[null]SENT_START"
        + " Є/[Є]noun:anim:f:v_naz:prop:fname:abbr|Є/[Є]noun:anim:m:v_rod:prop:fname:abbr|Є/[Є]noun:anim:m:v_zna:prop:fname:abbr"
        + " ./[null]null"
        + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname",
      tokenizer, sentenceTokenizer, tagger, disambiguator);
  
    TestTools.myAssert(" Є. Бакуліна",
        "/[null]SENT_START"
          + "  /[null]null"
          + " Є/[Є]noun:anim:f:v_naz:prop:fname:abbr|Є/[Є]noun:anim:m:v_rod:prop:fname:abbr|Є/[Є]noun:anim:m:v_zna:prop:fname:abbr"
          + " ./[null]null"
          + "  /[null]null"
          + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert(" Є.\u00A0Бакуліна",
        "/[null]SENT_START"
          + "  /[null]null"
          + " Є/[Є]noun:anim:f:v_naz:prop:fname:abbr|Є/[Є]noun:anim:m:v_rod:prop:fname:abbr|Є/[Є]noun:anim:m:v_zna:prop:fname:abbr"
          + " ./[null]null"
          + " \u00A0/[null]null"
          + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("Є.Л.Бакуліна",
      "/[null]SENT_START"
        + " Є/[Є]noun:anim:f:v_naz:prop:fname:abbr|Є/[Є]noun:anim:m:v_rod:prop:fname:abbr|Є/[Є]noun:anim:m:v_zna:prop:fname:abbr"
        + " ./[null]null"
        + " Л/[Л]noun:anim:f:v_naz:prop:patr:abbr|Л/[Л]noun:anim:m:v_rod:prop:patr:abbr|Л/[Л]noun:anim:m:v_zna:prop:patr:abbr"
        + " ./[null]null"
        + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname",
      tokenizer, sentenceTokenizer, tagger, disambiguator);
    
    TestTools.myAssert(" Є. Л. Бакуліна",
        "/[null]SENT_START"
          + "  /[null]null"
          + " Є/[Є]noun:anim:f:v_naz:prop:fname:abbr|Є/[Є]noun:anim:m:v_rod:prop:fname:abbr|Є/[Є]noun:anim:m:v_zna:prop:fname:abbr"
          + " ./[null]null"
          + "  /[null]null"
          + " Л/[Л]noun:anim:f:v_naz:prop:patr:abbr|Л/[Л]noun:anim:m:v_rod:prop:patr:abbr|Л/[Л]noun:anim:m:v_zna:prop:patr:abbr"
          + " ./[null]null"
          + "  /[null]null"
          + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    
    TestTools.myAssert(" Є. Л. Бакуліна і Г. К. Бакулін",
        "/[null]SENT_START"
          + "  /[null]null"
          + " Є/[Є]noun:anim:f:v_naz:prop:fname:abbr|Є/[Є]noun:anim:m:v_rod:prop:fname:abbr|Є/[Є]noun:anim:m:v_zna:prop:fname:abbr"
          + " ./[null]null"
          + "  /[null]null"
          + " Л/[Л]noun:anim:f:v_naz:prop:patr:abbr|Л/[Л]noun:anim:m:v_rod:prop:patr:abbr|Л/[Л]noun:anim:m:v_zna:prop:patr:abbr"
          + " ./[null]null"
          + "  /[null]null"
          + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname"
          + "  /[null]null"
          + " і/[і]conj:coord|і/[і]part"
          + "  /[null]null"
          + " Г/[Г]noun:anim:m:v_naz:prop:fname:abbr"
          + " ./[null]null"
          + "  /[null]null"
          + " К/[К]noun:anim:m:v_naz:prop:patr:abbr"
          + " ./[null]null"
          + "  /[null]null"
          + " Бакулін/[Бакулін]noun:anim:m:v_naz:prop:lname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    // make sure we don't choke on complex test
    TestTools.myAssert("Комендант, преподобний С. С. Мокітімі, був чудовою людиною.",
      "/[null]SENT_START Комендант/[Комендант]noun:anim:m:v_naz:prop:lname|Комендант/[комендант]noun:anim:m:v_naz ,/[null]null"
      +"  /[null]null преподобний/[преподобний]adj:m:v_kly|преподобний/[преподобний]adj:m:v_naz|преподобний/[преподобний]adj:m:v_zna:rinanim"
      +"|преподобний/[преподобний]noun:anim:m:v_kly|преподобний/[преподобний]noun:anim:m:v_naz"
      +"  /[null]null С/[null]null ./[null]null  /[null]null С/[null]null ./[null]null  /[null]null"
      +" Мокітімі/[null]null ,/[null]null  /[null]null"
      +" був/[бути]verb:imperf:past:m  /[null]null чудовою/[чудовий]adj:f:v_oru:compb  /[null]null людиною/[людина]noun:anim:f:v_oru ./[null]null",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
  }

  @Test
  public void testChunker() throws Exception {
    JLanguageTool lt = new JLanguageTool(new Ukrainian());
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("Для  годиться.");
    AnalyzedSentence disambiguated = chunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();
    
    assertTrue(tokens[1].getReadings().toString().contains("<adv>"));
    assertTrue(tokens[4].getReadings().toString().contains("</adv>"));
  }
  

}


