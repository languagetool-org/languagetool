/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà i Font
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
package org.languagetool.synthesis.ca;

import org.languagetool.AnalyzedToken;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Catalan word form synthesizer.
 * 
 * There are special additions:
 * "DT" tag adds "el, la, l', els, les" according to the gender  
 * and the number of the word and the Catalan rules for apostrophation (l').
 * "DTa" adds "al, a la, a l', als, a les"
 * "DTde" adds "del, de la, de l', dels, de les"
 * "DTper" adds "pel, per la, per l', pels, per les"
 * "DTca" adds "cal, ca la, ca l', cals, ca les"
 *
 * @author Jaume Ortolà i Font
 */
public class CatalanSynthesizer extends BaseSynthesizer {
  
  /* A special tag to add determiner (el, la, l', els, les). **/
  // private static final String ADD_DETERMINER = "DT";
  
  public static final String centralVerbTags = "[0CXY12]";
  public static final String valencianVerbTags = "[0VXZ13567]";
  public static final String balearicVerbTags = "[0BYZ1247]";
  
  private String verbTags;
  
  /* Exceptions */
  public static final List<String> LemmasToIgnore =  Arrays.asList("enterar", "sentar", "conseguir", "alcançar");

  /** Patterns for number and gender **/
  private static final Pattern pMS = Pattern.compile("(N|A.).[MC][SN].*|V.P.*SM.?");
  private static final Pattern pFS = Pattern.compile("(N|A.).[FC][SN].*|V.P.*SF.?");
  private static final Pattern pMP = Pattern.compile("(N|A.).[MC][PN].*|V.P.*PM.?");
  private static final Pattern pFP = Pattern.compile("(N|A.).[FC][PN].*|V.P.*PF.?");

  /** Pattern for previous preposition passed in the postag **/
  private static final Pattern pPrep = Pattern.compile("(DT)(.*)");

  /** Patterns for apostrophation **/
  private static final Pattern pMascYes = Pattern.compile("h?[aeiouàèéíòóú].*",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern pMascNo = Pattern.compile("h?[ui][aeioàèéóò].+",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern pFemYes = Pattern.compile("h?[aeoàèéíòóú].*|h?[ui][^aeiouàèéíòóúüï]+[aeiou][ns]?|urbs",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern pFemNo = Pattern.compile("host|ira|inxa",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  
  /** Patterns verb **/
  private static final Pattern pVerb = Pattern.compile("V.*[CVBXYZ0123456]");
  
  private static final Pattern pLemmaSpace = Pattern.compile("([^ ]+) (.+)");

  public static final CatalanSynthesizer INSTANCE = new CatalanSynthesizer(centralVerbTags);
  public static final CatalanSynthesizer INSTANCE_VAL = new CatalanSynthesizer(valencianVerbTags);
  public static final CatalanSynthesizer INSTANCE_BAL = new CatalanSynthesizer(balearicVerbTags);
  
//  /** @deprecated use {@link #INSTANCE} */
//  public CatalanSynthesizer(Language lang) {
//    this();
//  }

  protected CatalanSynthesizer(String verbTags) {
    super("/ca/ca.sor", "/ca/ca-ES-valencia_synth.dict", "/ca/ca-ES-valencia_tags.txt", "ca");
    this.verbTags = verbTags;
  }

  @Override
  public String[] synthesize(AnalyzedToken token, String posTag) throws IOException {    
    if (posTag.startsWith(SPELLNUMBER_TAG)) {
      return super.synthesize(token, posTag);
    }
    String lemma = token.getLemma();
    String toAddAfter = "";
    // verbs with noun
    if (posTag.startsWith("V")) {
      Matcher mLemmaSpace = pLemmaSpace.matcher(lemma);
      if (mLemmaSpace.matches()) {
        lemma = mLemmaSpace.group(1);
        toAddAfter = mLemmaSpace.group(2);
      }
    }
    initPossibleTags();
    Pattern p;
    boolean addDt = false; 
    String prep = ""; 
    Matcher mPrep = pPrep.matcher(posTag);
    if (mPrep.matches()) {
      addDt = true; // add definite article before token
      if (mPrep.groupCount() > 1) {
        prep = mPrep.group(2); // add preposition before article
      }
    }
    if (addDt) {
      p = Pattern.compile("N.*|A.*|V.P.*|PX.");
    } else {
      p = Pattern.compile(posTag);
    }
    List<String> results = new ArrayList<>();
    
    for (String tag : possibleTags) {
      Matcher m = p.matcher(tag);
      if (m.matches()) {
        if (addDt) {
          List<String> wordForms = lookup(lemma, tag);
          for (String word : wordForms) {
            word = StringTools.preserveCase(word, token.getToken());
            results.addAll(addPrepositionAndDeterminer(word, tag, prep));
          }
        } else {
          results.addAll(lookup(lemma, tag));
        }
      }
    }
    // if found nothing with determiner, add the expected determiner for the original word form
    if (addDt && results.isEmpty()) {
      results.addAll(addPrepositionAndDeterminer(token.getToken(), token.getPOSTag(), prep));
    }
    // if not found, try verbs from a regional variant
    if (results.isEmpty() && posTag.startsWith("V")) {
      return synthesize(token, posTag.substring(0, posTag.length() - 1).concat(verbTags), true);
    }
    return addWordsAfter(results, toAddAfter).toArray(new String[0]);
  }
  
  @Override
  public String[] synthesize(AnalyzedToken token, String posTag, boolean posTagRegExp) throws IOException {
    if (posTag.startsWith(SPELLNUMBER_TAG)) {
      return synthesize(token, posTag);
    }
    if (posTagRegExp) {
      String lemma = token.getLemma();
      if (LemmasToIgnore.contains(lemma)) {
        return new String[0];
      }
      String toAddAfter = "";
      // verbs with noun
      if (posTag.startsWith("V")) {
        Matcher mLemmaSpace = pLemmaSpace.matcher(lemma);
        if (mLemmaSpace.matches()) {
          lemma = mLemmaSpace.group(1);
          toAddAfter = mLemmaSpace.group(2);
        }
      }
      initPossibleTags();
      Pattern p;
      try {
        p = Pattern.compile(posTag);
      } catch (PatternSyntaxException e) {
        System.err.println("WARNING: Error trying to synthesize POS tag "
            + posTag + " from token " + token.getToken() + ": " + e.getMessage());
        return null;
      }
      List<String> results = new ArrayList<>();
      for (String tag : possibleTags) {
        Matcher m = p.matcher(tag);
        if (m.matches()) {
          results.addAll(lookup(lemma, tag));
        }
      }
      // if not found, try verbs from the active regional variant
      if (results.isEmpty()) {
        Matcher mVerb = pVerb.matcher(posTag);
        if (mVerb.matches()) {
          p = Pattern.compile(posTag.substring(0, posTag.length() - 1).concat(verbTags));
          for (String tag : possibleTags) {
            Matcher m = p.matcher(tag);
            if (m.matches()) {
              results.addAll(lookup(lemma, tag));
            }
          }
        }
      }
      return addWordsAfter(results, toAddAfter).toArray(new String[0]);
    }
    return synthesize(token, posTag);
  }
  
  /*
   * Add the appropriate forms of preposition + article + noun / adj
   */
  private List<String> addPrepositionAndDeterminer(String word, String posTag, String prep) {
    List<String> results = new ArrayList<>();
    Matcher mMS = pMS.matcher(posTag);
    Matcher mFS = pFS.matcher(posTag);
    Matcher mMP = pMP.matcher(posTag);
    Matcher mFP = pFP.matcher(posTag);
    if (mMS.matches()) {
      Matcher mMascYes = pMascYes.matcher(word);
      Matcher mMascNo = pMascNo.matcher(word);
      if (prep.equals("per")) {  if (mMascYes.matches() && !mMascNo.matches()) {  results.add("per l'" + word);  }  else {results.add("pel " + word);  } }
      else if (prep.isEmpty()) {  if (mMascYes.matches() && !mMascNo.matches()) {  results.add("l'" + word);  }  else {results.add("el " + word);  } }
      else {  if (mMascYes.matches() && !mMascNo.matches()) {  results.add(prep+" l'" + word);  }  else {results.add(prep+"l " + word);  } }
    }
    if (mFS.matches()) {
      Matcher mFemYes = pFemYes.matcher(word);
      Matcher mFemNo = pFemNo.matcher(word);
      if (prep.equals("per")) {  if (mFemYes.matches() && !mFemNo.matches()) {  results.add("per l'" + word);  }  else {results.add("per la " + word);} }
      else if (prep.isEmpty()) {  if (mFemYes.matches() && !mFemNo.matches()) {  results.add("l'" + word);  }  else {results.add("la " + word);} }
      else {  if (mFemYes.matches() && !mFemNo.matches()) {  results.add(prep+" l'" + word);  }  else {results.add(prep+" la " + word);} }
    }
    if (mMP.matches()) {    
      if (prep.equals("per")) { results.add("pels " + word); }
      else if (prep.isEmpty()) { results.add("els " + word); }
      else { results.add(prep+"ls " + word); }
    }
    if (mFP.matches()) { 
      if (prep.isEmpty()) { results.add("les " + word);  } else {results.add(prep+" les " + word);  }
    }
    return results;
  } 
  
  private List<String> addWordsAfter(List<String> results, String toAddAfter) {
    if (!toAddAfter.isEmpty()) {
      List<String> output = new ArrayList<>();
      for (String result : results) {
        output.add(result + " " + toAddAfter);
      }
      return output;
    }
    return results;
  }
  
  @Override
  public String getTargetPosTag(List<String> posTags, String targetPosTag) {
    if (posTags.isEmpty()) {
      return targetPosTag;
    }
    PostagComparator postagComparator = new PostagComparator();
    posTags.sort(postagComparator);
    // return the last one to keep the previous results
    return posTags.get(posTags.size() - 1);
  }
  
  private class PostagComparator implements Comparator<String> {

    @Override
    public int compare(String arg0, String arg1) {
      // give priority 3 person > 1 person, Indicative > Subjunctive
      int len0 = arg0.length();
      int len1 = arg1.length();
      if (len0 > 4 && len1 > 4) {
        if (arg0.equals("VMIS3S00") && arg1.equals("VMIS1S00")) {
          return 150;
        }
        if (arg0.equals("VMIS1S00") && arg1.equals("VMIS3S00")) {
          return -150;
        }
        if (arg0.equals("VMIP2P00") && arg1.equals("VMIS3S00")) {
          //feu (present/passat)
          return 150;
        }
        if (arg1.equals("VMIP2P00") && arg0.equals("VMIS3S00")) {
          return -150;
        }
        if (arg0.charAt(2) == 'I' && arg1.charAt(2) != 'I') {
          return 100;
        }
        if (arg1.charAt(2) == 'I' && arg0.charAt(2) != 'I') {
          return -100;
        }
        if (arg0.charAt(4) == '3' && arg1.charAt(4) == '1') {
          return 50;
        }
        if (arg1.charAt(4) == '1' && arg0.charAt(4) == '3') {
          return -50;
        }
      }
      return 0;
    }
  }
  
}

