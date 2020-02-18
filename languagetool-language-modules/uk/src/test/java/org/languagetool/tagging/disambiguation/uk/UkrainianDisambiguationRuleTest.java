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
package org.languagetool.tagging.disambiguation.uk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.MultiWordChunker2;
import org.languagetool.tagging.disambiguation.uk.SimpleDisambiguator.TokenMatcher;
import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import org.languagetool.tagging.uk.UkrainianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.uk.UkrainianWordTokenizer;

public class UkrainianDisambiguationRuleTest {
  
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
    chunker = new MultiWordChunker2("/uk/multiwords.txt", true);
  }

  @Test
  public void testDisambiguator() throws IOException {

    TestTools.myAssert("Танцювати до впаду", 
      "/[null]SENT_START Танцювати/[танцювати]verb:imperf:inf  /[null]null до/[до впаду]<adv>|до/[до]prep  /[null]null " +
      "впаду/[впасти]verb:perf:futr:s:1:xp2|впаду/[до впаду]<adv>",
      tokenizer, sentenceTokenizer, tagger, disambiguator);
    
    TestTools.myAssert("Прийшла Люба додому.", 
      "/[null]SENT_START Прийшла/[прийти]verb:perf:past:f|Прийшла/[прийшлий]adj:f:v_kly|Прийшла/[прийшлий]adj:f:v_naz  /[null]null Люба/[Люба]noun:anim:f:v_naz:prop:fname|Люба/[любий]adj:f:v_kly:compb|Люба/[любий]adj:f:v_naz:compb  /[null]null додому/[додому]adv ./[null]null",
       tokenizer, sentenceTokenizer, tagger, demoDisambiguator);

    TestTools.myAssert("Прийшла Люба додому.", 
      "/[null]SENT_START Прийшла/[прийти]verb:perf:past:f  /[null]null Люба/[Люба]noun:anim:f:v_naz:prop:fname  /[null]null додому/[додому]adv ./[null]null",
       tokenizer, sentenceTokenizer, tagger, disambiguator);
  }

  @Test
  public void testDisambiguatorForInanimVKly() throws IOException {

    TestTools.myAssert("Поломане крило",
      "/[null]SENT_START Поломане/[поломаний]adj:n:v_kly:&adjp:pasv:perf:coll|Поломане/[поломаний]adj:n:v_naz:&adjp:pasv:perf:coll|Поломане/[поломаний]adj:n:v_zna:&adjp:pasv:perf:coll"
      + "  /[null]null крило/[крило]noun:inanim:n:v_naz|крило/[крило]noun:inanim:n:v_zna|крило/[крити]verb:imperf:past:n",
      tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("до",
      "/[null]SENT_START до/[до]noun:inanim:n:v_dav:nv|до/[до]noun:inanim:n:v_mis:nv|до/[до]noun:inanim:n:v_naz:nv|до/[до]noun:inanim:n:v_oru:nv|до/[до]noun:inanim:n:v_rod:nv"
      +"|до/[до]noun:inanim:n:v_zna:nv|до/[до]noun:inanim:p:v_dav:nv|до/[до]noun:inanim:p:v_mis:nv|до/[до]noun:inanim:p:v_naz:nv|до/[до]noun:inanim:p:v_oru:nv|до/[до]noun:inanim:p:v_rod:nv|до/[до]noun:inanim:p:v_zna:nv|до/[до]prep",
      tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("мій лемківський краю...",
        "/[null]SENT_START мій/[мій]adj:m:v_kly:&pron:pos|мій/[мій]adj:m:v_naz:&pron:pos|мій/[мій]adj:m:v_zna:rinanim:&pron:pos  /[null]null"
        + " лемківський/[лемківський]adj:m:v_kly|лемківський/[лемківський]adj:m:v_naz|лемківський/[лемківський]adj:m:v_zna:rinanim  /[null]null"
        + " краю/[край]noun:inanim:m:v_dav|краю/[край]noun:inanim:m:v_kly|краю/[край]noun:inanim:m:v_mis|краю/[край]noun:inanim:m:v_rod .../[null]null",
      tokenizer, sentenceTokenizer, tagger, disambiguator);

    // still v_kly
    TestTools.myAssert("Ясний місяцю!",
        "/[null]SENT_START Ясний/[ясний]adj:m:v_kly:compb|Ясний/[ясний]adj:m:v_naz:compb|Ясний/[ясний]adj:m:v_zna:rinanim:compb"
        +"  /[null]null місяцю/[місяць]noun:inanim:m:v_dav|місяцю/[місяць]noun:inanim:m:v_kly|місяцю/[місяць]noun:inanim:m:v_mis !/[null]null",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
  }

  @Test
  public void testDisambiguatorForPluralNames() throws IOException {
    TestTools.myAssert("всіляких Василів",
        "/[null]SENT_START всіляких/[всілякий]adj:p:v_mis:&pron:gen|всіляких/[всілякий]adj:p:v_rod:&pron:gen|всіляких/[всілякий]adj:p:v_zna:ranim:&pron:gen"
        + "  /[null]null Василів/[Василь]noun:anim:p:v_rod:prop:fname|Василів/[Василь]noun:anim:p:v_zna:prop:fname|Василів/[Василів]adj:m:v_kly|Василів/[Василів]adj:m:v_naz|Василів/[Василів]adj:m:v_zna:rinanim"
        + "|Василів/[Василів]noun:anim:f:v_dav:nv:prop:lname|Василів/[Василів]noun:anim:f:v_mis:nv:prop:lname|Василів/[Василів]noun:anim:f:v_naz:nv:prop:lname|Василів/[Василів]noun:anim:f:v_oru:nv:prop:lname|Василів/[Василів]noun:anim:f:v_rod:nv:prop:lname|Василів/[Василів]noun:anim:f:v_zna:nv:prop:lname"
        + "|Василів/[Василів]noun:anim:m:v_naz:prop:lname:xp1"
        + "|Василів/[Василів]noun:inanim:m:v_naz:prop:geo:xp2|Василів/[Василів]noun:inanim:m:v_zna:prop:geo:xp2",
//        + "|Василів/[Василів]noun:anim:m:v_naz:prop:lname:xp1|Василів/[Василів]noun:inanim:m:v_naz:prop:xp2|Василів/[Василів]noun:inanim:m:v_zna:prop:xp2",
//        + "|Василів/[Василів]noun:anim:f:v_dav:nv:prop:lname|Василів/[Василів]noun:anim:f:v_kly:nv:prop:lname|Василів/[Василів]noun:anim:f:v_mis:nv:prop:lname|Василів/[Василів]noun:anim:f:v_naz:nv:prop:lname|Василів/[Василів]noun:anim:f:v_oru:nv:prop:lname|Василів/[Василів]noun:anim:f:v_rod:nv:prop:lname|Василів/[Василів]noun:anim:f:v_zna:nv:prop:lname|Василів/[Василів]noun:anim:f:v_dav:nv:prop:lname|Василів/[Василів]noun:anim:f:v_kly:nv:prop:lname|Василів/[Василів]noun:anim:f:v_mis:nv:prop:lname|Василів/[Василів]noun:anim:f:v_naz:nv:prop:lname|Василів/[Василів]noun:anim:f:v_oru:nv:prop:lname|Василів/[Василів]noun:anim:f:v_rod:nv:prop:lname|Василів/[Василів]noun:anim:f:v_zna:nv:prop:lname|Василів/[Василів]noun:anim:m:v_naz:prop:lname:xp1|Василів/[Василів]noun:inanim:m:v_naz:prop:xp2|Василів/[Василів]noun:inanim:m:v_zna:prop:xp2",
//        + "  /[null]null Василів/[Василь]noun:anim:p:v_rod:prop:fname|Василів/[Василь]noun:anim:p:v_zna:prop:fname|Василів/[Василів]adj:m:v_kly|Василів/[Василів]adj:m:v_naz|Василів/[Василів]adj:m:v_zna:rinanim"
//        + "|Василів/[Василів]noun:anim:f:v_dav:nv:prop:lname|Василів/[Василів]noun:anim:f:v_kly:nv:prop:lname|Василів/[Василів]noun:anim:f:v_mis:nv:prop:lname|Василів/[Василів]noun:anim:f:v_naz:nv:prop:lname|Василів/[Василів]noun:anim:f:v_oru:nv:prop:lname|Василів/[Василів]noun:anim:f:v_rod:nv:prop:lname|Василів/[Василів]noun:anim:f:v_zna:nv:prop:lname"
//        + "|Василів/[Василів]noun:anim:m:v_naz:prop:lname:xp1|Василів/[Василів]noun:anim:[m:v_naz:prop:lname:xp1|Василів/[Василів]noun:inanim:m:v_naz:prop:xp2|Василів/[Василів]noun:inanim:m:v_zna:prop:xp2",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("2 Андрії",
        "/[null]SENT_START 2/[2]number"
        + "  /[null]null Андрії/[Андрій]noun:anim:m:v_mis:prop:fname|Андрії/[Андрій]noun:anim:p:v_naz:prop:fname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("Юріїв Луценків",
        "/[null]SENT_START Юріїв/[Юрій]noun:anim:p:v_rod:prop:fname|Юріїв/[Юрій]noun:anim:p:v_zna:prop:fname|Юріїв/[Юріїв]adj:m:v_kly|Юріїв/[Юріїв]adj:m:v_naz|Юріїв/[Юріїв]adj:m:v_zna:rinanim"
        + "  /[null]null Луценків/[Луценки]noun:inanim:p:v_rod:ns:prop:geo|Луценків/[Луценко]noun:anim:p:v_rod:prop:lname|Луценків/[Луценко]noun:anim:p:v_zna:prop:lname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("всіляких Фрейдів",
        "/[null]SENT_START всіляких/[всілякий]adj:p:v_mis:&pron:gen|всіляких/[всілякий]adj:p:v_rod:&pron:gen|всіляких/[всілякий]adj:p:v_zna:ranim:&pron:gen"
        + "  /[null]null Фрейдів/[Фрейд]noun:anim:p:v_rod:prop:lname|Фрейдів/[Фрейд]noun:anim:p:v_zna:prop:lname|Фрейдів/[Фрейдів]adj:m:v_kly|Фрейдів/[Фрейдів]adj:m:v_naz|Фрейдів/[Фрейдів]adj:m:v_zna:rinanim",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    
    // untouched
    TestTools.myAssert("Василів автомобіль",
      "/[null]SENT_START Василів/[Василів]adj:m:v_kly|Василів/[Василів]adj:m:v_naz|Василів/[Василів]adj:m:v_zna:rinanim"
      + "|Василів/[Василів]noun:anim:f:v_dav:nv:prop:lname|Василів/[Василів]noun:anim:f:v_mis:nv:prop:lname|Василів/[Василів]noun:anim:f:v_naz:nv:prop:lname|Василів/[Василів]noun:anim:f:v_oru:nv:prop:lname|Василів/[Василів]noun:anim:f:v_rod:nv:prop:lname|Василів/[Василів]noun:anim:f:v_zna:nv:prop:lname|Василів/[Василів]noun:anim:m:v_naz:prop:lname:xp1|Василів/[Василів]noun:inanim:m:v_naz:prop:geo:xp2|Василів/[Василів]noun:inanim:m:v_zna:prop:geo:xp2"
//      + " |Василів/[Василів]noun:anim:m:v_naz:prop:lname:xp1|Василів/[Василів]noun:inanim:m:v_naz:prop:xp2|Василів/[Василів]noun:inanim:m:v_zna:prop:xp2"
      + "  /[null]null автомобіль/[автомобіль]noun:inanim:m:v_naz|автомобіль/[автомобіль]noun:inanim:m:v_zna",
      tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("на Андрії",
        "/[null]SENT_START на/[на]prep"
        + "  /[null]null Андрії/[Андрій]noun:anim:m:v_mis:prop:fname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("Леонідів Кравчука та Кучму",
        "/[null]SENT_START Леонідів/[Леонід]noun:anim:p:v_rod:prop:fname|Леонідів/[Леонід]noun:anim:p:v_zna:prop:fname|Леонідів/[Леонідів]adj:m:v_kly|Леонідів/[Леонідів]adj:m:v_naz|Леонідів/[Леонідів]adj:m:v_zna:rinanim"
        + "  /[null]null Кравчука/[Кравчук]noun:anim:m:v_rod:prop:lname|Кравчука/[Кравчук]noun:anim:m:v_zna:prop:lname"
        + "  /[null]null та/[та]conj:coord|та/[та]part  /[null]null Кучму/[Кучма]noun:anim:m:v_zna:prop:lname|Кучму/[кучма]noun:inanim:f:v_zna",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
  }
  
  @Test
  public void testDisambiguatorForInitials() throws IOException {
    TestTools.myAssert("Є.Бакуліна",
      "/[null]SENT_START"
        + " Є./[Є.]noun:anim:f:v_naz:prop:fname:abbr|Є./[Є.]noun:anim:m:v_rod:prop:fname:abbr|Є./[Є.]noun:anim:m:v_zna:prop:fname:abbr"
        + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname",
      tokenizer, sentenceTokenizer, tagger, disambiguator);
  
    TestTools.myAssert("Є.  Бакуліна",
        "/[null]SENT_START"
          + " Є./[Є.]noun:anim:f:v_naz:prop:fname:abbr|Є./[Є.]noun:anim:m:v_rod:prop:fname:abbr|Є./[Є.]noun:anim:m:v_zna:prop:fname:abbr"
          + "  /[null]null"
          + "  /[null]null"
          + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    
    TestTools.myAssert(" Є. Бакуліна",
        "/[null]SENT_START"
          + "  /[null]null"
          + " Є./[Є.]noun:anim:f:v_naz:prop:fname:abbr|Є./[Є.]noun:anim:m:v_rod:prop:fname:abbr|Є./[Є.]noun:anim:m:v_zna:prop:fname:abbr"
          + "  /[null]null"
          + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert(" Є.\u00A0Бакуліна",
        "/[null]SENT_START"
          + "  /[null]null"
          + " Є./[Є.]noun:anim:f:v_naz:prop:fname:abbr|Є./[Є.]noun:anim:m:v_rod:prop:fname:abbr|Є./[Є.]noun:anim:m:v_zna:prop:fname:abbr"
          + " \u00A0/[null]null"
          + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("Є.Л.Бакуліна",
      "/[null]SENT_START"
        + " Є./[Є.]noun:anim:f:v_naz:prop:fname:abbr|Є./[Є.]noun:anim:m:v_rod:prop:fname:abbr|Є./[Є.]noun:anim:m:v_zna:prop:fname:abbr"
        + " Л./[Л.]noun:anim:f:v_kly:prop:pname:abbr|Л./[Л.]noun:anim:f:v_naz:prop:pname:abbr|Л./[Л.]noun:anim:m:v_rod:prop:pname:abbr|Л./[Л.]noun:anim:m:v_zna:prop:pname:abbr"
        + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname",
      tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert(" Є. Л. Бакуліна",
        "/[null]SENT_START"
          + "  /[null]null"
          + " Є./[Є.]noun:anim:f:v_naz:prop:fname:abbr|Є./[Є.]noun:anim:m:v_rod:prop:fname:abbr|Є./[Є.]noun:anim:m:v_zna:prop:fname:abbr"
          + "  /[null]null"
          + " Л./[Л.]noun:anim:f:v_kly:prop:pname:abbr|Л./[Л.]noun:anim:f:v_naz:prop:pname:abbr|Л./[Л.]noun:anim:m:v_rod:prop:pname:abbr|Л./[Л.]noun:anim:m:v_zna:prop:pname:abbr"
          + "  /[null]null"
          + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("Бакуліна Є.",
        "/[null]SENT_START"
          + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname"
          + "  /[null]null"
          + " Є./[Є.]noun:anim:f:v_naz:prop:fname:abbr|Є./[Є.]noun:anim:m:v_rod:prop:fname:abbr|Є./[Є.]noun:anim:m:v_zna:prop:fname:abbr",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert(" Бакуліна Є. Л.",
        "/[null]SENT_START"
          + "  /[null]null"
          + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname"
          + "  /[null]null"
          + " Є./[Є.]noun:anim:f:v_naz:prop:fname:abbr|Є./[Є.]noun:anim:m:v_rod:prop:fname:abbr|Є./[Є.]noun:anim:m:v_zna:prop:fname:abbr"
          + "  /[null]null"
          + " Л./[Л.]noun:anim:f:v_kly:prop:pname:abbr|Л./[Л.]noun:anim:f:v_naz:prop:pname:abbr|Л./[Л.]noun:anim:m:v_rod:prop:pname:abbr|Л./[Л.]noun:anim:m:v_zna:prop:pname:abbr",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("Є.Л. Бакуліна",
        "/[null]SENT_START"
          + " Є./[Є.]noun:anim:f:v_naz:prop:fname:abbr|Є./[Є.]noun:anim:m:v_rod:prop:fname:abbr|Є./[Є.]noun:anim:m:v_zna:prop:fname:abbr"
          + " Л./[Л.]noun:anim:f:v_kly:prop:pname:abbr|Л./[Л.]noun:anim:f:v_naz:prop:pname:abbr|Л./[Л.]noun:anim:m:v_rod:prop:pname:abbr|Л./[Л.]noun:anim:m:v_zna:prop:pname:abbr"
          + "  /[null]null"
          + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    
    TestTools.myAssert(" Є. Л. Бакуліна і Г. К. Бакулін",
        "/[null]SENT_START"
          + "  /[null]null"
          + " Є./[Є.]noun:anim:f:v_naz:prop:fname:abbr|Є./[Є.]noun:anim:m:v_rod:prop:fname:abbr|Є./[Є.]noun:anim:m:v_zna:prop:fname:abbr"
          + "  /[null]null"
          + " Л./[Л.]noun:anim:f:v_kly:prop:pname:abbr|Л./[Л.]noun:anim:f:v_naz:prop:pname:abbr|Л./[Л.]noun:anim:m:v_rod:prop:pname:abbr|Л./[Л.]noun:anim:m:v_zna:prop:pname:abbr"
          + "  /[null]null"
          + " Бакуліна/[Бакулін]noun:anim:m:v_rod:prop:lname|Бакуліна/[Бакулін]noun:anim:m:v_zna:prop:lname|Бакуліна/[Бакуліна]noun:anim:f:v_naz:prop:lname"
          + "  /[null]null"
          + " і/[і]conj:coord|і/[і]part"
          + "  /[null]null"
          + " Г./[Г.]noun:anim:m:v_naz:prop:fname:abbr"
          + "  /[null]null"
          + " К./[К.]noun:anim:m:v_naz:prop:pname:abbr"
          + "  /[null]null"
          + " Бакулін/[Бакулін]noun:anim:m:v_naz:prop:lname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("С. Макаров.",
        "/[null]SENT_START"
        	+ " С./[С.]noun:anim:m:v_naz:prop:fname:abbr"
          + "  /[null]null"
          + " Макаров/[Макаров]noun:anim:m:v_naz:prop:lname|Макаров/[Макаров]noun:inanim:m:v_naz:prop:geo:xp2|Макаров/[Макаров]noun:inanim:m:v_zna:prop:geo:xp2"
          + " ./[null]null",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("для О.Волкова Л.Кучма",
        "/[null]SENT_START"
          + " для/[для]prep"
          + "  /[null]null"
          + " О./[О.]noun:anim:f:v_naz:prop:fname:abbr|О./[О.]noun:anim:m:v_rod:prop:fname:abbr|О./[О.]noun:anim:m:v_zna:prop:fname:abbr"
          + " Волкова/[Волков]noun:anim:m:v_rod:prop:lname|Волкова/[Волков]noun:anim:m:v_zna:prop:lname|Волкова/[Волкова]noun:anim:f:v_naz:prop:lname"
          + "  /[null]null"
          + " Л./[Л.]noun:anim:m:v_naz:prop:fname:abbr"
          + " Кучма/[Кучма]noun:anim:m:v_naz:prop:lname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);


    // make sure we don't choke on complex test
    TestTools.myAssert("Комендант, преподобний С. С. Мокітімі, був чудовою людиною.",
      "/[null]SENT_START Комендант/[Комендант]noun:anim:m:v_naz:prop:lname|Комендант/[комендант]noun:anim:m:v_naz ,/[null]null"
      +"  /[null]null"
      +" преподобний/[преподобний]adj:m:v_kly|преподобний/[преподобний]adj:m:v_naz|преподобний/[преподобний]adj:m:v_zna:rinanim"
      +"|преподобний/[преподобний]noun:anim:m:v_kly|преподобний/[преподобний]noun:anim:m:v_naz"
      +"  /[null]null"
      +" С./[null]null"
      +"  /[null]null"
      +" С./[null]null"
      +"  /[null]null"
      +" Мокітімі/[null]null ,/[null]null  /[null]null"
      +" був/[бути]verb:imperf:past:m  /[null]null чудовою/[чудовий]adj:f:v_oru:compb  /[null]null людиною/[людина]noun:anim:f:v_oru ./[null]null",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
  }

  @Test
  public void testDisambiguatorRemove() throws IOException {

    TestTools.myAssert("По кривій", 
      "/[null]SENT_START По/[по]prep  /[null]null" +
      " кривій/[крива]noun:inanim:f:v_dav|кривій/[крива]noun:inanim:f:v_mis|кривій/[кривий]adj:f:v_dav:compb|кривій/[кривий]adj:f:v_mis:compb",
      tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("Попри", 
        "/[null]SENT_START Попри/[попри]prep",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("Орися", 
        "/[null]SENT_START Орися/[Орися]noun:anim:f:v_naz:prop:fname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("Цікавим", 
        "/[null]SENT_START Цікавим/[цікавий]adj:m:v_oru:compb|Цікавим/[цікавий]adj:n:v_oru:compb|Цікавим/[цікавий]adj:p:v_dav:compb",
        tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("Вадим", 
        "/[null]SENT_START Вадим/[Вадим]noun:anim:m:v_naz:prop:fname",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
  }

  @Test
  public void testDisambiguatorForSt() throws IOException {
    TestTools.myAssert("за ст. 208",
      "/[null]SENT_START"
        + " за/[за]prep"
        + "  /[null]null"
        + " ст./[ст.]noun:inanim:f:v_dav:nv:abbr|ст./[ст.]noun:inanim:f:v_mis:nv:abbr|ст./[ст.]noun:inanim:f:v_naz:nv:abbr|ст./[ст.]noun:inanim:f:v_oru:nv:abbr|ст./[ст.]noun:inanim:f:v_rod:nv:abbr|ст./[ст.]noun:inanim:f:v_zna:nv:abbr"
        + "  /[null]null 208/[208]number",
      tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("за ст. ст. 208",
      "/[null]SENT_START"
        + " за/[за]prep"
        + "  /[null]null"
        + " ст./[ст.]noun:inanim:p:v_dav:nv:abbr|ст./[ст.]noun:inanim:p:v_mis:nv:abbr|ст./[ст.]noun:inanim:p:v_naz:nv:abbr|ст./[ст.]noun:inanim:p:v_oru:nv:abbr|ст./[ст.]noun:inanim:p:v_rod:nv:abbr|ст./[ст.]noun:inanim:p:v_zna:nv:abbr"
        + "  /[null]null"
        + " ст./[ст.]noun:inanim:p:v_dav:nv:abbr|ст./[ст.]noun:inanim:p:v_mis:nv:abbr|ст./[ст.]noun:inanim:p:v_naz:nv:abbr|ст./[ст.]noun:inanim:p:v_oru:nv:abbr|ст./[ст.]noun:inanim:p:v_rod:nv:abbr|ст./[ст.]noun:inanim:p:v_zna:nv:abbr"
        + "  /[null]null 208/[208]number",
      tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("у XIX ст.",
      "/[null]SENT_START"
      +  " у/[у]prep"
      + "  /[null]null"
      + " XIX/[XIX]number:latin"
      + "  /[null]null"
      + " ст./[ст.]noun:inanim:n:v_dav:nv:abbr|ст./[ст.]noun:inanim:n:v_mis:nv:abbr|ст./[ст.]noun:inanim:n:v_naz:nv:abbr|ст./[ст.]noun:inanim:n:v_oru:nv:abbr|ст./[ст.]noun:inanim:n:v_rod:nv:abbr|ст./[ст.]noun:inanim:n:v_zna:nv:abbr",
      tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("1 ст. ложка",
      "/[null]SENT_START"
      +  " 1/[1]number"
      + "  /[null]null"
      + " ст./[ст.]adj:f:v_dav:nv:abbr|ст./[ст.]adj:f:v_mis:nv:abbr|ст./[ст.]adj:f:v_naz:nv:abbr|ст./[ст.]adj:f:v_oru:nv:abbr|ст./[ст.]adj:f:v_rod:nv:abbr|ст./[ст.]adj:f:v_zna:nv:abbr|ст./[ст.]adj:p:v_dav:nv:abbr|ст./[ст.]adj:p:v_mis:nv:abbr|ст./[ст.]adj:p:v_naz:nv:abbr|ст./[ст.]adj:p:v_oru:nv:abbr|ст./[ст.]adj:p:v_rod:nv:abbr|ст./[ст.]adj:p:v_zna:nv:abbr"
      + "  /[null]null ложка/[ложка]noun:inanim:f:v_naz",
      tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("1 ст. сержант",
      "/[null]SENT_START"
      +  " 1/[1]number"
      + "  /[null]null"
      + " ст./[ст.]adj:m:v_dav:nv:abbr|ст./[ст.]adj:m:v_mis:nv:abbr|ст./[ст.]adj:m:v_naz:nv:abbr|ст./[ст.]adj:m:v_oru:nv:abbr|ст./[ст.]adj:m:v_rod:nv:abbr|ст./[ст.]adj:m:v_zna:nv:abbr"
      + "  /[null]null сержант/[сержант]noun:anim:m:v_naz",
      tokenizer, sentenceTokenizer, tagger, disambiguator);

    TestTools.myAssert("18 ст.",
      "/[null]SENT_START"
      + " 18/[18]number"
      + "  /[null]null"
      + " ст./[ст.]noun:inanim:f:v_dav:nv:abbr|ст./[ст.]noun:inanim:f:v_mis:nv:abbr|ст./[ст.]noun:inanim:f:v_naz:nv:abbr|ст./[ст.]noun:inanim:f:v_oru:nv:abbr|ст./[ст.]noun:inanim:f:v_rod:nv:abbr|ст./[ст.]noun:inanim:f:v_zna:nv:abbr|ст./[ст.]noun:inanim:n:v_dav:nv:abbr|ст./[ст.]noun:inanim:n:v_mis:nv:abbr|ст./[ст.]noun:inanim:n:v_naz:nv:abbr|ст./[ст.]noun:inanim:n:v_oru:nv:abbr|ст./[ст.]noun:inanim:n:v_rod:nv:abbr|ст./[ст.]noun:inanim:n:v_zna:nv:abbr",
      tokenizer, sentenceTokenizer, tagger, disambiguator);
  }

  @Test
  public void testTaggerUppgerGoodAndLowerBad() throws IOException {
    TestTools.myAssert("Держдепартамент", "/[null]SENT_START Держдепартамент/[Держдепартамент]noun:inanim:m:v_naz:prop|Держдепартамент/[Держдепартамент]noun:inanim:m:v_zna:prop",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
  }


  @Test
  public void testTaggingForUpperCaseAbbreviations() throws IOException {
    TestTools.myAssert("ВНЗ", "/[null]SENT_START ВНЗ/[ВНЗ]noun:inanim:m:v_dav:nv:abbr|ВНЗ/[ВНЗ]noun:inanim:m:v_mis:nv:abbr|ВНЗ/[ВНЗ]noun:inanim:m:v_naz:nv:abbr|ВНЗ/[ВНЗ]noun:inanim:m:v_oru:nv:abbr|ВНЗ/[ВНЗ]noun:inanim:m:v_rod:nv:abbr|ВНЗ/[ВНЗ]noun:inanim:m:v_zna:nv:abbr|ВНЗ/[ВНЗ]noun:inanim:p:v_dav:nv:abbr|ВНЗ/[ВНЗ]noun:inanim:p:v_mis:nv:abbr|ВНЗ/[ВНЗ]noun:inanim:p:v_naz:nv:abbr|ВНЗ/[ВНЗ]noun:inanim:p:v_oru:nv:abbr|ВНЗ/[ВНЗ]noun:inanim:p:v_rod:nv:abbr|ВНЗ/[ВНЗ]noun:inanim:p:v_zna:nv:abbr",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("АТО", "/[null]SENT_START АТО/[АТО]noun:inanim:f:v_dav:nv:abbr|АТО/[АТО]noun:inanim:f:v_mis:nv:abbr|АТО/[АТО]noun:inanim:f:v_naz:nv:abbr|АТО/[АТО]noun:inanim:f:v_oru:nv:abbr|АТО/[АТО]noun:inanim:f:v_rod:nv:abbr|АТО/[АТО]noun:inanim:f:v_zna:nv:abbr",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
  }

  @Test
  public void testSimpleRemove() throws IOException {
    TestTools.myAssert("була", "/[null]SENT_START була/[бути]verb:imperf:past:f",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("була-то", "/[null]SENT_START була-то/[бути]verb:imperf:past:f",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
  }

  @Test
  public void testDisambiguatorRemovePresentInDictionary() throws IOException {
    // make sure our disambiguation lines are valid lines in dictionary
    Map<String, TokenMatcher> map = new SimpleDisambiguator().DISAMBIG_REMOVE_MAP;
    for (Entry<String, TokenMatcher> entry : map.entrySet()) {
      List<AnalyzedTokenReadings> tagged = tagger.tag(Arrays.asList(entry.getKey()));
      AnalyzedTokenReadings taggedToken = tagged.get(0);
      TokenMatcher tokenMatcher = entry.getValue();
      
      assertTrue(String.format("%s not found in dictionary, tags: %s", entry.toString(), tagged.toString()), matches(taggedToken, tokenMatcher));
    }
  }

  private static boolean matches(AnalyzedTokenReadings taggedToken, TokenMatcher tokenMatcher) {
    for(AnalyzedToken analyzedToken: taggedToken.getReadings()) {
      if( tokenMatcher.matches(analyzedToken) )
        return true;
    }
    return false;
  }
  
  @Test
  public void testChunker() throws Exception {
    JLanguageTool lt = new JLanguageTool(new Ukrainian());
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("Для  годиться.");
    AnalyzedSentence disambiguated = chunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();
    
    assertTrue(tokens[1].getReadings().toString().contains("<adv>"));
    assertTrue(tokens[4].getReadings().toString().contains("<adv>"));

    analyzedSentence = lt.getAnalyzedSentence("на його думку");
    disambiguated = chunker.disambiguate(analyzedSentence);
    tokens = disambiguated.getTokens();
    
    assertTrue(tokens[1].getReadings().toString().contains("<insert>"));
    assertTrue(tokens[3].getReadings().toString().contains("<insert>"));
    assertTrue(tokens[5].getReadings().toString().contains("<insert>"));
  }

  
  @Test
  public void testIgnoredCharacters() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Ukrainian());
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("Іва́н Петро́вич.");

    // TODO: fix disambiguator - it should be: Петро́вич[Петрович...
    assertEquals("<S> Іва́н[Іван/noun:anim:m:v_naz:prop:fname,Іва́н/null]"
        + " Петрович[Петрович/noun:anim:f:v_dav:nv:prop:lname,Петрович/noun:anim:f:v_mis:nv:prop:lname,Петрович/noun:anim:f:v_naz:nv:prop:lname,Петрович/noun:anim:f:v_oru:nv:prop:lname,Петрович/noun:anim:f:v_rod:nv:prop:lname,Петрович/noun:anim:f:v_zna:nv:prop:lname,Петрович/noun:anim:m:v_naz:prop:lname:xp2,Петрович/noun:anim:m:v_naz:prop:pname]"
        + ".[</S>]",
        analyzedSentence.toString());

  }

}


