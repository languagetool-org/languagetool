/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.de;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.GermanyGerman;

public class GermanDisambiguationTest {

  private JLanguageTool lt;
  private Language language;
//  private Tokenizer tokenizer;
//  private SentenceTokenizer sentenceTokenizer;
//  private GermanRuleDisambiguator disambiguator;
//  private GermanTagger tagger;

  @Before
  public void setUp() throws IOException {
    language = new GermanyGerman();
//    tagger = new GermanTagger();
//    tokenizer = language.getWordTokenizer();
//    sentenceTokenizer = new SRXSentenceTokenizer(new GermanyGerman());
//    disambiguator = new GermanRuleDisambiguator();
    lt = new JLanguageTool(language);
  }

  @Test
  public void testChunker() throws IOException {
    List<AnalyzedSentence> tokens;

    tokens = lt.analyzeText("für Ihrer Sicherheit.");
    assertEquals(
        "[<S> für[für/PRP:TMP+MOD+CAU:AKK,für/PRP:TMP+MOD+CAU:AKK,PP] Ihrer[Ihr/PRO:POS:DAT:SIN:FEM:BEG,Ihr/PRO:POS:DAT:SIN:FEM:STV,Ihr/PRO:POS:GEN:SIN:FEM:BEG,Ihr/PRO:POS:GEN:SIN:FEM:STV,B-NP|NPS|PP] Sicherheit[Sicherheit/SUB:DAT:SIN:FEM,Sicherheit/SUB:GEN:SIN:FEM,I-NP|NPS|PP].[</S>./PKT,<P/>,O]]",
        tokens.toString());

    // FIXME: missing tags for "Ihrer"
    tokens = lt.analyzeText("Wir entwickeln ein Konzept für Ihrer Sicherheit.");
    assertEquals(
        "[<S> Wir[ich/PRO:PER:NOM:PLU:ALG,O] entwickeln[entwickeln/VER:1:PLU:KJ1:SFT,entwickeln/VER:1:PLU:PRÄ:SFT,entwickeln/VER:3:PLU:KJ1:SFT,entwickeln/VER:3:PLU:PRÄ:SFT,entwickeln/VER:INF:SFT,O] ein[ein/ART:IND:AKK:SIN:NEU,ein/ART:IND:NOM:SIN:NEU,B-NP|NPS] Konzept[Konzept/SUB:AKK:SIN:NEU,Konzept/SUB:NOM:SIN:NEU,I-NP|NPS] für[für/PRP:TMP+MOD+CAU:AKK,PP] Ihrer[Ihr/PRO:POS:DAT:SIN:FEM:BEG,Ihr/PRO:POS:DAT:SIN:FEM:STV,Ihr/PRO:POS:GEN:SIN:FEM:BEG,Ihr/PRO:POS:GEN:SIN:FEM:STV,B-NP|NPS|PP] Sicherheit[Sicherheit/SUB:DAT:SIN:FEM,Sicherheit/SUB:GEN:SIN:FEM,I-NP|NPS|PP].[</S>./PKT,<P/>,O]]",
        tokens.toString());

  }
}
