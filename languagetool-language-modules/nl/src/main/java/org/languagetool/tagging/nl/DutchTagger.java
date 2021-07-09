/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.nl;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;

import java.util.*;

/**
 * Dutch tagger.
 * 
 * @author Marcin Milkowski
 */
public class DutchTagger extends BaseTagger {

  public DutchTagger() {
    super("/nl/dutch.dict", new Locale("nl"));
  }
    // custom code to deal with words carrying optional accents
  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) {
    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;

    for (String word : sentenceTokens) {
      boolean ignoreSpelling = false;

      // make treatment of weird apostrophes same as in tokenizer (R. Baars, 2020-11-06)
      String originalWord = word;
      word = word.replace("`","'").replace("’","'").replace("‘","'").replace("´","'");
      
      final List<AnalyzedToken> l = new ArrayList<>();
      final String lowerWord = word.toLowerCase(locale);
      final boolean isLowercase = word.equals(lowerWord);
      final boolean isMixedCase = StringTools.isMixedCase(word);
      final boolean isAllUpper = StringTools.isAllUppercase(word);

      // assign tokens for flattened word to original word
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(word));
      //List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word));
      // normal case:
      addTokens(taggerTokens, l);
      // tag non-lowercase (alluppercase or startuppercase), but not mixedcase
      // word with lowercase word tags:
      if (!isLowercase && !isMixedCase) {
        List<AnalyzedToken> lowerTaggerTokens = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(lowerWord));
        addTokens(lowerTaggerTokens, l);
      }

      // tag all-uppercase proper nouns
      if (l.isEmpty() && isAllUpper) {
        final String firstUpper = StringTools.uppercaseFirstChar(lowerWord);
        List<AnalyzedToken> firstupperTaggerTokens = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(firstUpper));
        addTokens(firstupperTaggerTokens, l);
      }

      if (l.isEmpty()) {
        // there is still no postag found
        //String word2 = lowerWord;
        String word2 = word; // why the lowerword?
        // remove single accented characters
        word2 = word2.replaceAll("([^aeiouáéíóú])(á)([^aeiouáéíóú])", "$1a$3");
        word2 = word2.replaceAll("([^aeiouáéíóú])(é)([^aeiouáéíóú])", "$1e$3");
        word2 = word2.replaceAll("([^aeiouáéíóú])(í)([^aeiouáéíóú])", "$1i$3");
        word2 = word2.replaceAll("([^aeiouáéíóú])(ó)([^aeiouáéíóú])", "$1o$3");
        word2 = word2.replaceAll("([^aeiouáéíóú])(ú)([^aeiouáéíóú])", "$1u$3");

        // remove allowed accented characters
        word2 = word2.replace("áá", "aa");
        word2 = word2.replace("áé", "ae");
        word2 = word2.replace("áí", "ai");
        word2 = word2.replace("áú", "au");
        word2 = word2.replace("éé", "ee");
        word2 = word2.replace("éí", "ei");
        word2 = word2.replace("éú", "eu");
        word2 = word2.replace("íé", "ie");
        word2 = word2.replace("óé", "oe");
        word2 = word2.replace("óí", "oi");
        word2 = word2.replace("óó", "oo");
        word2 = word2.replace("óú", "ou");
        word2 = word2.replace("úí", "ui");
        word2 = word2.replace("úú", "uu");
        word2 = word2.replace("íj", "ij");

        word2 = word2.replaceAll("(^|[^aeiou])á([^aeiou]|$)", "$1a$2");
        word2 = word2.replaceAll("(^|[^aeiou])é([^aeiou]|$)", "$1e$2");
        word2 = word2.replaceAll("(^|[^aeiou])í([^aeiou]|$)", "$1i$2");
        word2 = word2.replaceAll("(^|[^aeiou])ó([^aeiou]|$)", "$1o$2");
        word2 = word2.replaceAll("(^|[^aeiou])ú([^aeiou]|$)", "$1u$2");

        // best would be to check the parts as well (uncompound)
        if (word2.contains("-")) {
          //String part1 = word2.replaceAll("(^.*)-(.*$)", "$1");
          //List<AnalyzedToken> p1 = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(part1));
          String part2 = word2.replaceAll("(^.*)-(.*$)", "$2");
          List<AnalyzedToken> p2 = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(part2));
          //if (!(p1.isEmpty()||p2.isEmpty())) {
          if (!p2.isEmpty()) {
            // word is split on a likely location
            word2 = word2.replaceAll("([a-z])-([a-z])", "$1$2");
          }
        }

        if (!word2.equals(word)) {
          List<AnalyzedToken> l2 = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(word2));
          if (!l2.isEmpty()) {
            // woord bestaat
            addTokens(l2, l);
            ignoreSpelling = true;
          }
        }
        //*************** START OF ADDED UNCOMPOUNDER CODE ****************** //
        // (still too) simple uncompounder
        // it needs check for postags and substring
        // nevertheless, 5 is rather safe
        // TODO :
        // - optimize code
        // - move code to separate file/function/class
        // - add more safe word types
        // wordExceptions TODO: implement this as textfile, it is quite a big list, at least theoretically
        Boolean activateUncompounder=false; // to switch uncompounder code on and off
        if (activateUncompounder && l.isEmpty()) {
          String wordExceptions="translating|voorzittersschap|weerszijden|bijenkomst|stijlwestie";
          // TODO make riskyParts a list or even a file. Or better still, add 2 lists: 1 for trustworthy fronts and one for trustworthy ends
          String riskyParts="vergoding|bijbel|tegens|rood|geel|groen|blauw|paars|oranje|bronzen|stat|westie|westies|barheid|douch|vrouwe|ellen|geluis|beroes|heep|hepen";
          if (!word.matches(wordExceptions)) {
            int size =word.length();
            String trueCollisions=".*(a~[aeéiu]|[eé]~[eéiu]|i~[e]|o~[eiou]|i~j|[A-Z]~[a-z]|[a-z]~[A-Z]|[0-9]~[a-zA-Z]|[a-zA-Z]~[0-9]).*";
            // other false patterns
            trueCollisions+="|(tegen|voor|achter|midden|open)~(s|s-)~.*|(af|aan|uit|op)?(rijd|snijd|glijd)~.*";
            for (int i = 5; i <= size-5; i++) {
              // end is most significant, so check that
              String end=word.substring(i);
              // betther make riskyparts a list
              if (!end.matches(riskyParts)) {
                List<AnalyzedToken> e = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(end));
                String front=word.substring(0,i);
                if (!front.matches(riskyParts)) {
                  if (!e.isEmpty()) {
                    // is a word
                    //System.out.println("front:"+front);
                    // check front
                    List<AnalyzedToken> f = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(front));
                    if (!f.isEmpty()) {
                      // front is a valid word
                      String option=front+"~"+end;
                      if (!option.matches(trueCollisions)) {
                        // there is no character collision
                        // get the tags now, and check the combinations
                        for(int j=0;j<e.size();j++){
                          String eTag=e.get(j).getPOSTag();
                          String eWord=e.get(j).getLemma();
                          for(int k=0;k<f.size();k++){
                            String fTag=f.get(k).getPOSTag();
                            String fWord=f.get(k).getLemma();

                            String tagCombi=(fTag+"~"+eTag);
                            //System.out.println(option+":"+tagCombi);

                            if (tagCombi.matches("^(ZNW:EKV|ZNW:EKV:DE_|ZNW:EKV:HET|ZNW:MRV:VRK:HET|WKW:TGW:1EP)~ZNW:.*$")) {
                              // the end tag determines the total tag
                              l.add(new AnalyzedToken(word, eTag, front+eWord));
                              ignoreSpelling=true;
                              l.add (new AnalyzedToken(word, "LIKELY_SPELLING", word));
                            } else if (tagCombi.matches("^ZNW:MRV:DE_~ZNW:.*$")&&(front.matches(".*en$"))) {
                              // the end tag determines the total tag
                              // compounding with -n when there is also a plural with -s is forbidden\
                              String otherPlural=front.replaceAll("n$","s");
                              System.out.println(otherPlural);
                              List<AnalyzedToken> o = asAnalyzedTokenListForTaggedWords(otherPlural, getWordTagger().tag(otherPlural));
                              if (o.isEmpty()) {
                                // in fact, all tags should be checked to be ZNW:MRV:DE_, if so, then reject
                                AnalyzedToken temp = new AnalyzedToken(word, eTag, front+eWord);
                                l.add( new AnalyzedToken(word, eTag, front+eWord) );
                                l.add (new AnalyzedToken(word, "LIKELY_SPELLING", word));
                                ignoreSpelling=true;
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
                if (word.substring(i-2,i).equals("s-")) {
                  //System.out.println("s-"+front);
                  // front could have an compounding s and dash
                  front=word.substring(0,i-2);
                  if (!front.matches(riskyParts)) {
                    List<AnalyzedToken> f = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(front));
                    if (!f.isEmpty()) {
                      String option=front+"~s-~"+end;
                      for(int j=0;j<e.size();j++){
                        String eTag=e.get(j).getPOSTag();
                        String eWord=e.get(j).getLemma();
                        for(int k=0;k<f.size();k++){
                          String fTag=f.get(k).getPOSTag();
                          String fWord=f.get(k).getLemma();

                          String tagCombi=(fTag+"~s-~"+eTag);
                          //System.out.println(option+":"+tagCombi);
                          if (tagCombi.matches("^(ZNW:EKV|ZNW:EKV:DE_|ZNW:EKV:HET|ZNW:MRV:VRK:HET)~s-~ZNW:.*$")&&(!option.matches(".*e~s-~.*"))) {
                            // the end tag determines the total tag
                            l.add( new AnalyzedToken(word, eTag, front+"s-"+eWord) );
                            l.add (new AnalyzedToken(word, "LIKELY_SPELLING", word));
                            ignoreSpelling=true;
                          }
                        }
                      }
                    }
                  }
                }
                if (word.substring(i-1,i).equals("s")) {
                  //System.out.println("s$");
                  // front could have an compounding s and dash
                  front=word.substring(0,i-1);
                  if (!front.matches(riskyParts)) {
                    List<AnalyzedToken> f = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(front));
                    if (!f.isEmpty()) {
                      String option=front+"~s~"+end;
                      if (!option.matches(".*~s~[A-Z0-9].*")) {
                        for(int j=0;j<e.size();j++){
                          String eTag=e.get(j).getPOSTag();
                          String eWord=e.get(j).getLemma();
                          for(int k=0;k<f.size();k++){
                            String fTag=f.get(k).getPOSTag();
                            String fWord=f.get(k).getLemma();
                            String tagCombi=(fTag+"~s~"+eTag);
                            //System.out.println(option+":"+tagCombi);
                            if (tagCombi.matches("^(ZNW:EKV|ZNW:EKV:DE_|ZNW:EKV:HET|ZNW:MRV:VRK:HET)~s~ZNW:.*$")&&(!option.matches(".*e~s~.*"))) {
                              l.add( new AnalyzedToken(word, eTag, front+"s"+eWord) );
                              l.add (new AnalyzedToken(word, "LIKELY_SPELLING", word));
                              ignoreSpelling=true;
                            }
                          }
                        }
                      }
                    }
                  }
                }
                if (word.substring(i-1,i).equals("-")) {
                  // front could have an compounding s and dash
                  front=word.substring(0,i-1);
                  if (!front.matches(riskyParts)) {
                    List<AnalyzedToken> f = asAnalyzedTokenListForTaggedWords(originalWord, getWordTagger().tag(front));
                    if (!f.isEmpty()) {
                      String option=front+"~-~"+end;
                      for(int j=0;j<e.size();j++){
                        String eTag=e.get(j).getPOSTag();
                        String eWord=e.get(j).getLemma();
                        for(int k=0;k<f.size();k++){
                          String fTag=f.get(k).getPOSTag();
                          String fWord=f.get(k).getLemma();
                          String tagCombi=(fTag+"~-~"+eTag);
                          //System.out.println(option+":"+tagCombi);
                          if (tagCombi.matches("^(ZNW:EKV|ZNW:EKV:DE_|ZNW:EKV:HET|ZNW:MRV:VRK:HET|WKW:TGW:1EP)~-~ZNW:.*$")) {
                            // the end tag determines the total tag
                            // this could be added
                            l.add( new AnalyzedToken(word, eTag, front+"-"+eWord) );
                            l.add (new AnalyzedToken(word, "LIKELY_SPELLING", word));
                            ignoreSpelling=true;
                          } else if (tagCombi.matches("^ZNW:MRV:DE_~ZNW:.*$")&&(front.matches(".*en$"))) {
                            String otherPlural=front.replaceAll("n$","s");
                            //System.out.println(otherPlural);
                            //compounding with -n when there is also a plural with -s is forbidden
                            List<AnalyzedToken> o = asAnalyzedTokenListForTaggedWords(otherPlural, getWordTagger().tag(otherPlural));
                            if (o.isEmpty()) {
                              // in fact, all tags should be checked to be ZNW:MRV:DE_, if so, then reject
                              System.out.println(o.toString());
                              AnalyzedToken temp = new AnalyzedToken(word, eTag, front+eWord);
                              l.add( new AnalyzedToken(word, eTag, front+"-"+eWord) );
                              l.add (new AnalyzedToken(word, "LIKELY_SPELLING", word));
                              ignoreSpelling=true;
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        // ********* END OF UNCOMPOUNDER CODE *************** //
      }

      // set word to original
      word = originalWord;

      if (l.isEmpty()) {
        l.add(new AnalyzedToken(originalWord, null, null));
      }

      AnalyzedTokenReadings atr = new AnalyzedTokenReadings(l, pos);
      if (ignoreSpelling) {
        // it might be a word that needs captials! Check this in dictionary
        if (isLowercase) {
          List<AnalyzedToken> fu = asAnalyzedTokenListForTaggedWords(StringTools.uppercaseFirstChar(originalWord), getWordTagger().tag(StringTools.uppercaseFirstChar(originalWord)));
          if (fu.isEmpty()) {
            // does not exist in dictionary having firstupper
            atr.ignoreSpelling();
          } else {
            // there is an uppercased form in the dictionary; so this one is probably wrong
            //System.out.println("=>"+l.toString());
            // TODO clearing the l list does not work here; the 'LIKELY_SPELLING' tag should be removed! But somehow, this does not work when done here.
            l.clear();
            l.add(new AnalyzedToken(originalWord, null, null));
            //System.out.println("=>"+l.toString());
          }
        } else {
            atr.ignoreSpelling();
        }
      }

      tokenReadings.add(atr);
      
      pos += word.length();
    }
    
    return tokenReadings;
  }

  private void addTokens(final List<AnalyzedToken> taggedTokens, final List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }

}
