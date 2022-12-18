/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Arabic;
import org.languagetool.synthesis.ar.ArabicSynthesizer;
import org.languagetool.tagging.ar.ArabicTagManager;
import org.languagetool.tagging.ar.ArabicTagger;
import org.languagetool.tokenizers.ArabicWordTokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class VerbTransRuleTest {

  private ArabicTagger tagger;
  private ArabicTagManager tagmanager;
  private ArabicWordTokenizer tokenizer;
  private ArabicSynthesizer synthesizer;
  private final boolean debug = false;

  @Before
  public void setUp() {
    tagger = new ArabicTagger();
    tokenizer = new ArabicWordTokenizer();
    tagmanager = new ArabicTagManager();
    synthesizer = new ArabicSynthesizer(new Arabic());
  }


  @Test
  public void testDirectTranstoIndirectTrans() throws IOException {

// what to do
    // we have the verb "to need in arabic"
    // احتاج
    // this verb is indirect transitive, it use preposition as transitivity tool
    // احتاج إلى
    // That means any error on adding attached pronoun to the verb is false
    // احتاجه => احتاج إليه
    // iHThajahu => Ihtaja Ilayhi
    // how to proceed
    //* extract verb tag
    // if tag is added has a pronoun
    // generate a new verb word without pronoun
    // generate the preposition form with the pronoun
    // transfert the flag of pronoun from verb to pepostion
//    String verb = "أفاضه";
    String verb = "أفاضوه";
    String preposition = "في";
    // extract verb lemma and postag
    List<String> verbtokens = tokenizer.tokenize(verb);
    List<String> prepositiontokens = tokenizer.tokenize(preposition);
    List<AnalyzedTokenReadings> verbTokenR = tagger.tag(verbtokens);
    List<AnalyzedTokenReadings> prepositionTokenR = tagger.tag(prepositiontokens);
    //// extract preposition tag

    List<AnalyzedToken> verbTokensy = verbTokenR.get(0).getReadings();
    String verbTag = verbTokensy.get(0).getPOSTag();
    String verbLemma = verbTokensy.get(0).getLemma();
    // extract preposition tag
    List<AnalyzedToken> prepositionTokensy = prepositionTokenR.get(0).getReadings();
    String prepositionTag = prepositionTokensy.get(0).getPOSTag();
    String prepositionLemma = prepositionTokensy.get(0).getLemma();
    if (debug) {
      System.out.printf("VerbTransRuleTes: verb : [%s, %s]; preposition [%s %s]\n", verbTag, verbLemma, prepositionTag, prepositionLemma);
    }
    if (tagmanager.isAttached(verbTag)) {
      char pronounFlag = tagmanager.getFlag(verbTag, "PRONOUN");
      String newVerbTag = tagmanager.setFlag(verbTag, "PRONOUN", '-');

      //      // generate new preposition word form
      String newPrepositionTag = tagmanager.setFlag(prepositionTag, "PRONOUN", pronounFlag);
      newPrepositionTag = tagmanager.setFlag(newPrepositionTag, "OPTION", 'D');
      if (debug) {
        System.out.printf("VerbTransRuleTes: Tags verb: %s preposition: %s\n", newVerbTag, newPrepositionTag);
      }
      // String newPreposition = synthesizer.synthesis(verbLemma, newVerbTag)
      AnalyzedToken verbAToken = new AnalyzedToken(verb, newVerbTag, verbLemma);
      String newVerb = Arrays.toString(synthesizer.synthesize(verbTokensy.get(0), newVerbTag));
      AnalyzedToken prepAToken = new AnalyzedToken(preposition, newPrepositionTag, preposition);
      String newPreposition = Arrays.toString(synthesizer.synthesize(prepAToken, newPrepositionTag));
      //      String newVerb = synthesizer.synth(verbLemma, newVerbTag);
      if (debug) {
        System.out.printf("VerbTransRuleTes: suggestions verb: %s preposition: %s\n", newVerb, newPreposition);
      }

    }

  }

  @Test
  public void testdirecttranstoindirecttransAll() throws IOException {

    // what to do
    // we have the verb "to need in arabic"
    // احتاج
    // this verb is indirect transitive, it use preposition as transitivity tool
    // احتاج إلى
    // That means any error on adding attached pronoun to the verb is false
    // احتاجه => احتاج إليه
    // iHThajahu => Ihtaja Ilayhi
    // how to proceed
    //* extract verb tag
    // if tag is added has a pronoun
    // generate a new verb word without pronoun
    // generate the preposition form with the pronoun
    // transfert the flag of pronoun from verb to pepostion
    String verb = "أفاضوه";
    String preposition = "في";
    // extract verb lemma and postag
    List<String> verbtokens = tokenizer.tokenize(verb);
    List<String> prepositiontokens = tokenizer.tokenize(preposition);
    List<AnalyzedTokenReadings> verbTokenR = tagger.tag(verbtokens);
    List<AnalyzedTokenReadings> prepositionTokenR = tagger.tag(prepositiontokens);
    //// extract preposition tag
    for (AnalyzedTokenReadings x : verbTokenR) {
      List<AnalyzedToken> verbTokensy = x.getReadings();
      for (AnalyzedToken V : verbTokensy) {
        String verbTag = V.getPOSTag();
        String verbLemma = V.getLemma();
        // extract preposition tag
        List<AnalyzedToken> prepositionTokensy = prepositionTokenR.get(0).getReadings();
        String prepositionTag = prepositionTokensy.get(0).getPOSTag();
        String prepositionLemma = prepositionTokensy.get(0).getLemma();
        if (debug) {
          System.out.printf("VerbTransRuleTes: verb : [%s, %s]; preposition [%s %s]\n", verbTag, verbLemma, prepositionTag, prepositionLemma);
        }
        if (tagmanager.isAttached(verbTag)) {
          char pronounFlag = tagmanager.getFlag(verbTag, "PRONOUN");

          String newPreposition = generateAttachedNewForm(preposition, prepositionTag, pronounFlag);
          String newVerb = generateUnattachedNewForm(verbLemma, verbTag);
          if (debug) {
            System.out.printf("VerbTransRuleTes: suggestions verb: %s preposition: %s \n", newVerb, newPreposition);
          }
        }
      }  //for
    }  //for

  }

  /* generate a new form according to a specific postag*/
  private String generateNewForm(String word, String posTag, char flag) {
    // generate new from word form
    String newposTag = tagmanager.setFlag(posTag, "PRONOUN", flag);
    if (flag != '-') {
      newposTag = tagmanager.setFlag(newposTag, "OPTION", 'D');
    }
    // generate the new preposition according to modified postag
    AnalyzedToken prepAToken = new AnalyzedToken(word, newposTag, word);
    String newWord = Arrays.toString(synthesizer.synthesize(prepAToken, newposTag));

    return newWord;

  }

  /* generate a new form according to a specific postag, this form is Attached*/
  private String generateAttachedNewForm(String word, String posTag, char flag) {
    return generateNewForm(word, posTag, flag);

  }

  /* generate a new form according to a specific postag, this form is Un-Attached*/
  private String generateUnattachedNewForm(String word, String posTag) {
    return generateNewForm(word, posTag, '-');
  }
}
