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
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.tagging.ar.ArabicTagManager;
import org.languagetool.tagging.ar.ArabicTagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.languagetool.tools.ArabicConstants.*;

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

  private final ArabicTagManager tagmanager = new ArabicTagManager();
  private final ArabicTagger tagger = new ArabicTagger();

  public static final ArabicSynthesizer INSTANCE = new ArabicSynthesizer();

  /** @deprecated use {@link #INSTANCE} */
  public ArabicSynthesizer(Language lang) {
    this();
  }
  private ArabicSynthesizer() {
    super(RESOURCE_FILENAME, TAGS_FILE_NAME, "ar");
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
      // adjust some stems
      stem = correctStem(wd.getStem().toString(), posTag);
      wordForms.add(stem);
    }
    return wordForms.toArray(new String[0]);
  }


  @Override
  public String[] synthesize(AnalyzedToken token, String posTag,
                             boolean posTagRegExp) throws IOException {

    if (posTag != null && posTagRegExp) {
      String myPosTag = posTag;
      initPossibleTags();
      myPosTag = correctTag(myPosTag);

      Pattern p = Pattern.compile(myPosTag);
      List<String> results = new ArrayList<>();
      String stem;
      for (String tag : possibleTags) {
        Matcher m = p.matcher(tag);
        if (m.matches() && token.getLemma() != null) {
          // local result
          List<String> resultOne = lookup(token.getLemma(), tag);
          for (String wd : resultOne) {
            // adjust some stems according to original postag
            stem = correctStem(wd, posTag);
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
    if (postag == null) {
      return null;
    }
    // remove attached pronouns
    mypostag = tagmanager.setConjunction(mypostag, "-");

    // remove Alef Lam definite article
    mypostag = tagmanager.setDefinite(mypostag, "-");

    // change all pronouns to one kind
    mypostag = tagmanager.unifyPronounTag(mypostag);

    return mypostag;
  }

  @Override
  public String getPosTagCorrection(String posTag) {
    return correctTag(posTag);
  }


  /* correct stem to generate stems to be attached with pronouns  */
  public String correctStem(String stem, String postag) {
    String correctStem = stem;
    if (postag == null) return stem;
    if (tagmanager.isAttached(postag)) {
      correctStem = correctStem.replaceAll("ه$", "");
    }

    if (tagmanager.isDefinite(postag)) {
      String prefix = tagmanager.getDefinitePrefix(postag);// can handle ال & لل
      correctStem = prefix + correctStem;
    }
    if (tagmanager.hasJar(postag)) {
      String prefix = tagmanager.getJarPrefix(postag);
      correctStem = prefix + correctStem;
    }
    if (tagmanager.hasConjunction(postag)) {
      String prefix = tagmanager.getConjunctionPrefix(postag);
      correctStem = prefix + correctStem;

    }
    return correctStem;
  }

  /**
   * @return set a new enclitic for the given word,
   */
  public String setEnclitic(AnalyzedToken token, String suffix) {
    List<String> wordlist = setEncliticMultiple(token, suffix);
    return wordlist.get(0);
  }

  public List<String> setEncliticMultiple(AnalyzedToken token, String suffix) {
    // if the suffix is not empty
    // save procletic
    // ajust postag to get synthesed words
    // set enclitic flag
    // synthesis => lookup for stems with similar postag and has enclitic flag
    // Add procletic and enclitic to stem
    // return new word list
    String postag = token.getPOSTag();
    String word = token.getToken();

    List<String> defaultWordlist = new ArrayList<String>();
    defaultWordlist.add("(" + word + ")");
    if (postag.isEmpty()) {
      return defaultWordlist;
    }
    List<String> wordlist = new ArrayList<String>();
    /* The flag is by defaul equal to '-' , if suffix => "H" */
    char flag = (suffix.isEmpty()) ? '-' : 'H';
    // save procletic
    String procletic = tagger.getProclitic(token);
    // set enclitic flag
    String newposTag = tagmanager.setFlag(postag, "PRONOUN", flag);
    //adjust procletics
    newposTag = tagmanager.setProcleticFlags(newposTag);
    // synthesis => lookup for stems with similar postag and has enclitic flag
    String lemma = token.getLemma();
    AnalyzedToken newToken = new AnalyzedToken(lemma, newposTag, lemma);
    String[] newwordList = synthesize(newToken, newposTag);

    String stem = "";
    if (newwordList.length != 0) {
      String newWord = "";
      for (int i = 0; i < newwordList.length; i++) {
        stem = newwordList[i];
        // We replace the Heh, because the Tag dictionary represent only Heh Pronouns, for reason of compressin
        // the other pronoun suffix are added by tagger or synthesizer
        if (tagmanager.hasPronoun(newposTag) && flag == 'H') {
          if (stem.endsWith("ي")) {
            // if the stem is ended by Yeh for 1st person pronoun
            // if suffix is Yeh pronoun, ignore suffix
            // else ignore stem
            if (suffix.equals("ي"))
              newWord = procletic + stem;
            else
              newWord = "";
          } else if (stem.endsWith("ه")) {
            stem = stem.replaceAll("ه$", "");
            newWord = procletic + stem + suffix;
          } else {
            newWord = procletic + stem + suffix;
          }
        } else
          newWord = procletic + stem;

        if (!newWord.isEmpty())
          wordlist.add(newWord);
      }
    } else // no word generated
    {
      stem = "(" + word + ")";
      wordlist.add(stem);
    }

    if (wordlist.isEmpty()) {
      return defaultWordlist;
    }
    return wordlist;
  }

  /**
   * @return set a new procletic for the given word,
   */
  public String setJarProcletic(AnalyzedToken token, String prefix) {
    // if the preffix is not empty
    // save enclitic
    // ajust postag to get synthesed words
    // set procletic flags
    // synthesis => lookup for stems with similar postag
    // Add procletic and enclitic to stem
    // return new word
    String postag = token.getPOSTag();
    String word = token.getToken();
    if (postag.isEmpty())
      return word;
    // case of definate word:
    // إضافة الجار إلى أل التعريف
    if (tagmanager.isDefinite(postag)) {
      if (prefix.equals("ل")) {
        prefix += "ل";
      } else {
        //  if(prefprefix.equals("ب")||prefix.equals("ك"))
        // case of Beh Jar, Kaf Jar, empty Jar
        prefix += "ال";
      }
    }
    return setProcletic(token, prefix);
  }

  /**
   * @return set a new procletic for the given word,
   */
  public String setProcletic(AnalyzedToken token, String prefix) {
    // if the preffix is not empty
    // save enclitic
    // ajust postag to get synthesed words
    // set procletic flags
    // synthesis => lookup for stems with similar postag
    // Add procletic and enclitic to stem
    // return new word
    String postag = token.getPOSTag();
    String word = token.getToken();
    if (postag.isEmpty()) {
      return word;
    }
    // save enclitic
    String enclitic = tagger.getEnclitic(token);
    String newposTag = postag;
    //remove procletics
    newposTag = tagmanager.setProcleticFlags(newposTag);

    // synthesis => lookup for stems with similar postag and has enclitic flag
    String lemma = token.getLemma();
    AnalyzedToken newToken = new AnalyzedToken(lemma, newposTag, lemma);
    String[] newwordList = synthesize(newToken, newposTag);

    String stem = "";
    if (newwordList.length != 0) {
      stem = newwordList[0];
      if (tagmanager.hasPronoun(newposTag)) {
        stem = stem.replaceAll("ه$", "");
      }
    } else {
      // no word generated
      stem = "(" + word + ")";
    }
    String newWord = prefix + stem + enclitic;
    return newWord;
  }

  /* generate a new form according to a specific postag, this form is Attached*/
  public List<String> inflectLemmaLike(String targetLemma, AnalyzedToken sourcetoken) {
    // make a token with the lemma
    AnalyzedTokenReadings tokenReadList = tagger.tag(targetLemma);
    List<String> wordlist = new ArrayList<String>();

    if (!tokenReadList.hasLemma(targetLemma)) {
      wordlist.add("[" + targetLemma + "]");
      return wordlist;
    }
    String sourcePostag = sourcetoken.getPOSTag();
    // get affixes
    String prefix = tagger.getProclitic(sourcetoken);
    String suffix = tagger.getEnclitic(sourcetoken);

    List<AnalyzedToken> tokenListFiltred = new ArrayList<AnalyzedToken>();

    // if the lemma is not equals to given one, continue
    // how can a lemma not the same,
    // if we tag a diacritized verb, the tagger remove diacritics and can generate other cases
    for (AnalyzedToken currentToken : tokenReadList.getReadings()) {
      if (targetLemma.equals(currentToken.getLemma())) {
        tokenListFiltred.add(currentToken);
      }
    }

    for (AnalyzedToken currentToken : tokenListFiltred) {
      // if the lemma is not equals to given one, continue
      // how can a lemma not the same,
      // if we tag a diacritized verb, the tagger remove diacritics and can generate other cases
      // merge postag
      String postagLemma = currentToken.getPOSTag();
      String mergedPostag = tagmanager.mergePosTag(sourcePostag, postagLemma);

      // construct word
      String word = prefix + targetLemma;
      AnalyzedToken token = new AnalyzedToken(word, mergedPostag, targetLemma);
      List<String> wordlist2 = setEncliticMultiple(token, suffix);
      wordlist.addAll(wordlist2);
    }
    // remove dupplicates
    List<String> resultWordlist = new ArrayList<String>(new HashSet<>(wordlist));
    return resultWordlist;
  }


  /* genarate Mafoul Mutlaq from masdar */
  public static String inflectMafoulMutlq(String word) {
    if (word == null) {
      return word;
    }
    String newword = word;
    if (word.endsWith(Character.toString(TEH_MARBUTA))) {
      newword += FATHATAN;
    } else {
      newword += FATHATAN + "" + ALEF;
    }
    return newword;
  }

  /* genarate Mafoul Mutlaq from masdar */
  public static String inflectAdjectiveTanwinNasb(String word, boolean feminin) {
    if (word == null) {
      return word;
    }
    String newword = word;
    if (feminin) {
      if (word.endsWith(Character.toString(TEH_MARBUTA))) {
        newword += FATHATAN;
      } else {
        newword += Character.toString(TEH_MARBUTA) + FATHATAN;
      }
    } else { // if masculine, remove teh marbuta
      if (word.endsWith(Character.toString(TEH_MARBUTA))) {
        newword = word.replaceAll(Character.toString(TEH_MARBUTA), "");
      } else {
        newword += Character.toString(FATHATAN) + ALEF;
      }
    }
    return newword;
  }
}


