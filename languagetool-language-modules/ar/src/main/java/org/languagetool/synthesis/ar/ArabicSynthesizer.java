/* LanguageTool, a natural language style checker
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
package org.languagetool.synthesis.ar;

import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;
import org.languagetool.AnalyzedToken;
import org.languagetool.Language;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.tagging.ar.ArabicTagManager;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

/**
 * Arabic word form synthesizer.
 * Based on part-of-speech lists in Public Domain. See readme.txt for details,
 * the POS tagset is described in arabic_tags_description.txt.
 * <p>
 * There are two special additions:
 * <ol>
 *    <li>+GF - tag that adds  feminine gender to word</li>
 *    <li>+GM - a tag that adds masculine gender to word</li>
 * </ol>
 *
 * @author Taha Zerrouki
 * @since 4.9
 */
public class ArabicSynthesizer extends BaseSynthesizer {

  private static final String RESOURCE_FILENAME = "/ar/arabic_synth.dict";
  private static final String TAGS_FILE_NAME = "/ar/arabic_tags.txt";

  // A special tag to remove pronouns properly
  private static final String REMOVE_PRONOUN = "(\\+RP)?";
  private ArabicTagManager tagmanager = new ArabicTagManager();

  public ArabicSynthesizer(Language lang) {
    super(RESOURCE_FILENAME, TAGS_FILE_NAME, lang);
  }

  /**
   * Get a form of a given AnalyzedToken, where the form is defined by a
   * part-of-speech tag.
   *
   * @param token  AnalyzedToken to be inflected.
   * @param posTag A desired part-of-speech tag.
   * @return String value - inflected word.
   */
  @Override
  public String[] synthesize(AnalyzedToken token, String posTag) {
    IStemmer synthesizer = createStemmer();
    List<WordData> wordData = synthesizer.lookup(token.getLemma() + "|" + posTag);
    List<String> wordForms = new ArrayList<>();
    String stem;
    for (WordData wd : wordData) {
      // ajust some stems
      stem = correctStem(wd.getStem().toString(), posTag);
      wordForms.add(stem);
    }
    return wordForms.toArray(new String[0]);
  }
  /**
   * Special English regexp based synthesizer that allows adding articles
   * when the regexp-based tag ends with a special signature {@code \\+INDT} or {@code \\+DT}.
   *
   * @since 2.5
   */
  @Override
  public String[] synthesize(AnalyzedToken token, String posTag,
                             boolean posTagRegExp) throws IOException {

    if (posTag != null && posTagRegExp) {
      String myPosTag = posTag;
      initPossibleTags();
//       System.out.println("synthesis 1:"+token.toString() +"|"+myPosTag);
     myPosTag = correctTag(myPosTag);
//      System.out.println("synthesis 2:"+token.toString() +"|"+myPosTag+"****"+posTag);

      Pattern p = Pattern.compile(myPosTag);
      List<String> results = new ArrayList<>();
      String stem;
      for (String tag : possibleTags) {
        Matcher m = p.matcher(tag);
        if (m.matches() && token.getLemma() != null) {
          // local result
          List<String> result_one = new ArrayList<>();
          lookup(token.getLemma(), tag, result_one);
          for (String  wd : result_one) {
            // ajust some stems according to original postag
//             System.out.println("correctStem input: "+wd+"["+tag+"] "+posTag);       
            stem = correctStem(wd, posTag);
//             System.out.println("correctStem output: "+stem+"["+tag+"] "+posTag);            
            results.add(stem);
          }
        }
      }
      return results.toArray(new String[0]);
    }

    return synthesize(token, posTag);
  }



  /* correct tags  */

  public String correctTag(String postag) {
    String mypostag = postag;
    if (postag == null) return null;
   // remove attached pronouns

   // remove conjuction if 
//    System.out.println("CorrectTag 1:"+mypostag);
    mypostag = tagmanager.setConjunction(mypostag, "-");
    // remove Alef Lam definite article
    mypostag = tagmanager.setDefinite(mypostag, "-");
//    System.out.println("CorrectTag 2:"+mypostag+"##"+postag);    

   // remove Lam Jar
//     mypostag = tagmanager.setJar(mypostag, "-");  
//    System.out.println("CorrectTag 3:"+mypostag+"##"+postag);    
    return mypostag;
  }


  /* correct stem to generate stems to be attached with pronouns  */
  public String correctStem(String stem, String postag) {

    if (postag == null) return stem;
    if(tagmanager.isAttached(postag))
    {
      stem = stem.replaceAll("ه$","");
    }

    if(tagmanager.isDefinite(postag))
    {
      String prefix = tagmanager.getDefinitePrefix(postag);// can handle ال & لل
      stem = prefix + stem;
    }    
    if(tagmanager.hasJar(postag))
    {
      String prefix = tagmanager.getJarPrefix(postag);
      stem = prefix + stem;
    }
    if(tagmanager.hasConjunction(postag))
    {
      String  prefix = tagmanager.getConjunctionPrefix(postag);
//       System.out.println("stem 1:"+prefix);
//       System.out.println("stem 2:"+prefix+stem);
      stem = prefix + stem;
      
    }
    return stem;
  }

  
  
  //  @Override
//  public String[] synthesize(AnalyzedToken token, String posTag) {
//    IStemmer synthesizer = createStemmer();
//    String myPosTag = posTag;
//    // a flag to correct special case of posTag
//    String correctionFlag = "";
//    // extract special signature if exists
//    correctionFlag = extractSignature(myPosTag);
//    myPosTag = removeSignature(myPosTag);
//    // correct postag according to special signature if exists
//    myPosTag = correctTag(myPosTag, correctionFlag);
//    List<WordData> wordData = synthesizer.lookup(token.getLemma() + "|" + myPosTag);
//    List<String> wordForms = new ArrayList<>();
//    for (WordData wd : wordData) {
//      wordForms.add(wd.getStem().toString());
//    }
//    return wordForms.toArray(new String[0]);
//  }


//  /* Extract  */
//  public String extractSignature(String postag) {
//    String tmp = postag;
//    String correctionFlag = "";
//    if (tmp.endsWith(REMOVE_PRONOUN)) {
//      correctionFlag += "+RP";
//    }
//    return correctionFlag;
//  }
//
//  /* Extract  */
//  public String removeSignature(String postag) {
//    String tmp = postag;
//    if (tmp.endsWith(REMOVE_PRONOUN)) {
//      // remove the code
//      tmp = tmp.substring(0, tmp.indexOf(REMOVE_PRONOUN));
//    }
//    return tmp;
//  }

  /* remove the flag to an encoded tag */
//  public String removeTag(String postag, String flag) {
//    StringBuilder tmp = new StringBuilder(postag);
//    if (tmp != null) {
//      if (flag.equals("H") && tmp.charAt(tmp.length() - 1) == 'H') {
//
//        tmp.setCharAt(tmp.length() - 1, '-');
//      }
//    }
//    return tmp.toString();
//  }

//  /* correct tags  */
//  public String correctTag(String postag, String correctionFlag) {
//    if (postag == null) return null;
//    String tmp = postag;
//    // remove attached pronouns
//    if (correctionFlag.equals("+RP")) {
//      tmp = removeTag(tmp, "H");
//    }
//    return tmp;
//  }
  }




