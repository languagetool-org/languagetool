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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Catalan word form synthesizer.
 * @author Jaume Ortolà i Font
 */
public class CatalanSynthesizer extends BaseSynthesizer {

  private static final Map<String, String> verbTags = new HashMap<>();
  static {
    verbTags.put("ca-ES", "[0CXY12]");
    verbTags.put("ca-ES-valencia", "[0VXZ13567]");
    verbTags.put("ca-ES-balear", "[0BYZ1247]");
  }
  
  /* Exceptions */
  public static final List<String> LemmasToIgnore =  Arrays.asList("enterar", "sentar", "conseguir", "alcançar");

  /** Patterns verb **/
  private static final Pattern pVerb = Pattern.compile("V.*[CVBXYZ0123456]");
  
  private static final Pattern pLemmaSpace = Pattern.compile("([^ ]+) (.+)");

  public static final CatalanSynthesizer INSTANCE = new CatalanSynthesizer();
  
//  /** @deprecated use {@link #INSTANCE} */
//  public CatalanSynthesizer(Language lang) {
//    this();
//  }

  protected CatalanSynthesizer() {
    super("/ca/ca.sor", "/ca/ca-ES-valencia_synth.dict", "/ca/ca-ES-valencia_tags.txt", "ca");
  }

  @Override
  public String[] synthesize(AnalyzedToken token, String posTag) throws IOException {    
    return synthesize(token, posTag, "ca-ES");
  }
  
  public String[] synthesize(AnalyzedToken token, String posTag, String langVariantCode) throws IOException {    
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
    Pattern p = Pattern.compile(posTag);
    List<String> results = new ArrayList<>();
    for (String tag : possibleTags) {
      Matcher m = p.matcher(tag);
      if (m.matches()) {
        results.addAll(lookup(lemma, tag));
      }
    }
    // if not found, try verbs from a regional variant
    if (results.isEmpty() && posTag.startsWith("V")) {
      return synthesize(token, posTag.substring(0, posTag.length() - 1).concat(verbTags.get(langVariantCode)), true);
    }
    return addWordsAfter(results, toAddAfter).toArray(new String[0]);
  }
  
  
  @Override
  public String[] synthesize(AnalyzedToken token, String posTag, boolean posTagRegExp) throws IOException {
    return synthesize(token, posTag, posTagRegExp, "ca-ES");
  }
    
  public String[] synthesize(AnalyzedToken token, String posTag, boolean posTagRegExp, String langVariantCode) throws IOException {
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
            + posTag + " from token " + token + ": " + e.getMessage() + " StackTrace: " + e.getStackTrace());
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
          p = Pattern.compile(posTag.substring(0, posTag.length() - 1).concat(verbTags.get(langVariantCode)));
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
  
  private static class PostagComparator implements Comparator<String> {

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

  public List<String> getPossibleTags() throws IOException {
    initPossibleTags();
    return possibleTags;
  }
  
}

