/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.tagging.ar;

import java.util.List;

/**
 * @author Taha Zerrouki
 * @since 5.0
 */
public class ArabicTagManager {

  // CONSTANT for noun flags position
  private static final int NOUN_TAG_LENGTH = 12;
  private static final int NOUN_FLAG_POS_WORDTYPE = 0;
  private static final int NOUN_FLAG_POS_CATEGORY = 1;

  private static final int NOUN_FLAG_POS_GENDER = 4;
  private static final int NOUN_FLAG_POS_NUMBER = 5;
  private static final int NOUN_FLAG_POS_CASE = 6;
  private static final int NOUN_FLAG_POS_INFLECT_MARK = 7;

  private static final int NOUN_FLAG_POS_CONJ = 9;
  private static final int NOUN_FLAG_POS_JAR = 10;
  private static final int NOUN_FLAG_POS_PRONOUN = 11;

  // CONSTANT for Verb flags position
  private static final int VERB_TAG_LENGTH = 15;
  private static final int VERB_FLAG_POS_WORDTYPE = 0;
  private static final int VERB_FLAG_POS_CATEGORY = 1;
  private static final int VERB_FLAG_POS_TRANS = 2;

  private static final int VERB_FLAG_POS_GENDER = 4;
  private static final int VERB_FLAG_POS_NUMBER = 5;
  private static final int VERB_FLAG_POS_PERSON = 6;
  private static final int VERB_FLAG_POS_INFLECT_MARK = 7;
  private static final int VERB_FLAG_POS_TENSE = 8;
  private static final int VERB_FLAG_POS_VOICE = 9;
  private static final int VERB_FLAG_POS_CASE = 10;

  private static final int VERB_FLAG_POS_CONJ = 12;
  private static final int VERB_FLAG_POS_ISTIQBAL = 13;
  private static final int VERB_FLAG_POS_PRONOUN = 14;

  public ArabicTagManager() {

  }

  public String modifyPosTag(String postag, List<String> tags) {
    // if one of tags are incompatible return null
    for (String tg : tags) {
      postag = addTag(postag, tg);
      if (postag == null)
        return null;
    }
    return postag;
  }

  /* Add the flag to an encoded tag */
  public String addTag(String postag, String flag) {
    StringBuilder tmp = new StringBuilder(postag);
    if (flag.equals("W")) {
      tmp.setCharAt(postag.length() - 3, 'W');
    } else if (flag.equals("K")) {
      if (postag.startsWith("N")) {
        // the noun must be majrour
        if (isMajrour(postag))
          tmp.setCharAt(postag.length() - 2, 'K');
          // a prefix K but non majrour
        else return null;

      } else return null;
    } else if (flag.equals("B")) {
      if (postag.startsWith("N")) {
        // the noun must be majrour
        if (isMajrour(postag))
          tmp.setCharAt(postag.length() - 2, 'B');
          // a prefix B but non majrour
        else return null;

      } else return null;
    } else if (flag.equals("L")) {
      if (isNoun(postag)) {
        // the noun must be majrour
        if (isMajrour(postag))
          tmp.setCharAt(postag.length() - 2, 'L');
          // a prefix Lam but non majrour
        else return null;

      } else {// verb case
        tmp.setCharAt(postag.length() - 2, 'L');
      }
    } else if (flag.equals("D")) {
      // the noun must be not attached
      if (isUnAttachedNoun(postag))
        tmp.setCharAt(postag.length() - 1, 'L');
        // a prefix Lam but non majrour
      else return null;
    } else if (flag.equals("S")) {
      // َAdd S flag
      // if postag contains a future tag, TODO with regex
      if (isFutureTense(postag)) {
        tmp.setCharAt(postag.length() - 2, 'S');
      } else
        // a prefix Seen but non verb or future
        return null;
    }
    return tmp.toString();
  }

  /**
   * @param postag
   * @return true if have flag majrour
   */
  public boolean isMajrour(String postag) {
    return (postag.charAt(6) == 'I') || (postag.charAt(6) == '-');
  }

  /**
   * @param postag
   * @param jar
   * @return add jar flag to noun
   */
  public String setJar(String postag, String jar) {
    StringBuilder tmp = new StringBuilder(postag);
    char myflag = 0;
    if (isMajrour(postag)) {
      if (jar.equals("ب") || jar.equals("B"))
        myflag = 'B';
      else if (jar.equals("ل") || jar.equals("L"))
        myflag = 'L';
      else if (jar.equals("ك") || jar.equals("K"))
        myflag = 'K';
      else if (jar.equals("-") || jar.isEmpty())
        myflag = '-';
      if (myflag != 0)
        tmp.setCharAt(NOUN_FLAG_POS_JAR, myflag);
    }
    return tmp.toString();
  }

  /**
   * @param postag
   * @param flag
   * @return add definite flag to noun
   */
  public String setDefinite(String postag, String flag) {
    StringBuilder tmp = new StringBuilder(postag);
    char myflag = 0;
    if (isNoun(postag) && isUnAttachedNoun(postag)) {
      if (flag.equals("ال")
        || flag.equals("L")
        || flag.equals("لل")
        || flag.equals("D")
      )
        myflag = 'L';
      else if (flag.equals("-") || flag.isEmpty())
        myflag = '-';
      if (myflag != 0)
        tmp.setCharAt(NOUN_FLAG_POS_PRONOUN, myflag);
    }
    return tmp.toString();
  }

  /**
   * @param postag
   * @param flag
   * @return add conjuction flag to noun
   */
  public String setConjunction(String postag, String flag) {
    StringBuilder tmp = new StringBuilder(postag);
    char myflag = 0;
    if (flag.equals("و")
      || flag.equals("W")
      || flag.equals("ف")
      || flag.equals("F")
    )
      myflag = 'W';
    else if (flag.equals("-") || flag.isEmpty())
      myflag = '-';
    if (myflag != 0) {
      if (isNoun(postag)) {
        tmp.setCharAt(NOUN_FLAG_POS_CONJ, myflag);
      } else if (isVerb(postag)) {
        tmp.setCharAt(VERB_FLAG_POS_CONJ, myflag);
      }
    }
    return tmp.toString();
  }

  /**
   * @param postag
   * @param flag
   * @return add conjuction flag to noun
   */
  public String setPronoun(String postag, String flag) {
    StringBuilder tmp = new StringBuilder(postag);
    char myflag = 0;
    if (flag.equals("ه")
      || flag.equals("H")
    )
      myflag = 'H';
    if (myflag != 0) {
      if (isNoun(postag)) {
        tmp.setCharAt(NOUN_FLAG_POS_PRONOUN, myflag);
      } else if (isVerb(postag)) {
        tmp.setCharAt(VERB_FLAG_POS_PRONOUN, myflag);
      }

    }
    return tmp.toString();
  }

  /**
   * @param postag
   * @return true if have flag future
   */
  public boolean isFutureTense(String postag) {
    return postag.startsWith("V") && postag.contains("f");
  }

  /**
   * @param postag
   * @return true if have flag is noun and has attached pronoun
   */
  public boolean isUnAttachedNoun(String postag) {
    return postag.startsWith("N") && !postag.endsWith("H")&& !postag.endsWith("X");
  }

  /**
   * @param postag
   * @return true if have flag is noun/verb and has attached pronoun
   */
  public boolean isAttached(String postag) {
    return (isNoun(postag) && (postag.charAt(NOUN_FLAG_POS_PRONOUN) == 'H'))
      || (isVerb(postag) && (postag.charAt(VERB_FLAG_POS_PRONOUN) == 'H'));
  }

  /**
   * @param postag
   * @return test if word has stopword tagging
   */
  public boolean isStopWord(String postag) {
    return postag.startsWith("P");
  }

  /**
   * @param postag
   * @return true if have flag noun
   */
  public boolean isNoun(String postag) {
    return postag.startsWith("N");
  }

  /**
   * @param postag
   * @return true if have flag verb
   */
  public boolean isVerb(String postag) {
    return postag.startsWith("V");
  }

  /**
   * @param postag
   * @return true if have flag is noun and definite
   */
  public boolean isDefinite(String postag) {

    return isNoun(postag) && (postag.charAt(NOUN_FLAG_POS_PRONOUN) == 'L');
  }

  /**
   * @param postag
   * @return true if the postag has a Jar
   */
  public boolean hasJar(String postag) {

    return isNoun(postag) && (postag.charAt(NOUN_FLAG_POS_JAR) != '-');
  }

  /**
   * @param postag
   * @return true if the postag has a conjuction
   */
  public boolean hasConjunction(String postag) {

    return (isNoun(postag) && (postag.charAt(NOUN_FLAG_POS_CONJ) != '-'))
      || (isVerb(postag) && (postag.charAt(VERB_FLAG_POS_CONJ) != '-'));
  }

  /**
   * @param postag
   * @return if have a flag which is a noun and definite, return the prefix letter for this case
   */
  public String getDefinitePrefix(String postag) {
    if (postag.isEmpty())
      return "";
    if (isNoun(postag) && (postag.charAt(NOUN_FLAG_POS_PRONOUN) == 'L')) {
      if (hasJar(postag) && getJarPrefix(postag).equals("ل"))
        return "ل";
      else return "ال";
    }
    return "";
  }

  /**
   * @param postag
   * @return the Jar prefix letter
   */
  public String getJarPrefix(String postag) {

    if (postag.isEmpty())
      return "";
    if (isNoun(postag)) {
      if (postag.charAt(NOUN_FLAG_POS_JAR) == 'L')
        return "ل";
      else if (postag.charAt(NOUN_FLAG_POS_JAR) == 'K')
        return "ك";
      else if (postag.charAt(NOUN_FLAG_POS_JAR) == 'B')
        return "ب";
    }
    return "";
  }

  /**
   * @param postag
   * @return the Conjunction prefix letter
   */
  public String getConjunctionPrefix(String postag) {
    int pos;
    if (postag.isEmpty())
      return "";
    if (isNoun(postag))
      pos = NOUN_FLAG_POS_CONJ;
    else if (isVerb(postag))
      pos = VERB_FLAG_POS_CONJ;
    else
      return "";

    if (postag.charAt(pos) == 'F')
      return "ف";
    else if (postag.charAt(pos) == 'W')
      return "و";

    return "";
  }

}

