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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.languagetool.AnalyzedToken;
import org.languagetool.Language;
import org.languagetool.synthesis.BaseSynthesizer;

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

  public CatalanSynthesizer(Language lang) {
    super("/ca/ca.sor", "/ca/ca-ES-valencia_synth.dict", "/ca/ca-ES-valencia_tags.txt", lang);
  }

  @Override
  public String[] synthesize(AnalyzedToken token, String posTag) throws IOException {
    if (posTag.startsWith(SPELLNUMBER_TAG)) {
      String[] tag = posTag.split(":");
      String strToSpell = token.getToken();
      if (tag.length > 1 && tag[1].equals("feminine")) {
        strToSpell = "feminine " + strToSpell;
      }
      return new String[] { getSpelledNumber(strToSpell) };
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
          lookupWithEl(lemma, tag, prep, results);
        } else {
          results.addAll(lookup(lemma, tag));
        }
      }
    }       
    
    // if not found, try verbs from any regional variant
    if (results.isEmpty() && posTag.startsWith("V")) {
      if (posTag.endsWith("V") || posTag.endsWith("B")) {
        results.addAll(lookup(lemma, posTag.substring(0, posTag.length() - 1).concat("Z")));
      }
      if (results.isEmpty() && !posTag.endsWith("0")) {
        results.addAll(lookup(lemma, posTag.substring(0, posTag.length() - 1).concat("0")));
      }
      if (results.isEmpty()) { // another try
        return synthesize(token, posTag.substring(0, posTag.length() - 1).concat("."), true);
      }
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
      // if not found, try verbs from any regional variant
      if (results.isEmpty()) {
        Matcher mVerb = pVerb.matcher(posTag);
        if (mVerb.matches()) {
          if (!posTag.endsWith("0")) {
            p = Pattern.compile(posTag.substring(0, posTag.length() - 1)
                .concat("0"));
            for (String tag : possibleTags) {
              Matcher m = p.matcher(tag);
              if (m.matches()) {
                results.addAll(lookup(lemma, tag));
              }
            }
          }
          if (results.isEmpty()) { // another try
            p = Pattern.compile(posTag.substring(0, posTag.length() - 1)
                .concat("."));
            for (String tag : possibleTags) {
              Matcher m = p.matcher(tag);
              if (m.matches()) {
                results.addAll(lookup(lemma, tag));
              }
            }
          }
        }
      }
      return addWordsAfter(results, toAddAfter).toArray(new String[0]);
    }
    return synthesize(token, posTag);
  }

  /**
   * Lookup the inflected forms of a lemma defined by a part-of-speech tag.
   * Adds determiner "el" properly inflected and preposition
   * (prep. +) det. + noun. / adj.
   * @param lemma the lemma to be inflected.
   * @param posTag the desired part-of-speech tag.
   * @param results the list to collect the inflected forms.
   */
  private void lookupWithEl(String lemma, String posTag, String prep, List<String> results) {
    List<String> wordForms = lookup(lemma, posTag);
    Matcher mMS = pMS.matcher(posTag);
    Matcher mFS = pFS.matcher(posTag);
    Matcher mMP = pMP.matcher(posTag);
    Matcher mFP = pFP.matcher(posTag);
    for (String word : wordForms) {
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
    }

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
}
