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
package org.languagetool.rules.de;

import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.ReadabilityRule;
import org.languagetool.rules.Category.Location;

/**
 * A rule that checks the readability of German text (using the Flesch-Reading-Ease Formula)
 * If tooEasyTest == true, the rule tests if paragraph level &gt; level (readability is too easy)
 * If tooEasyTest == false, the rule tests if paragraph level &lt; level (readability is too difficult)
 * @author Fred Kruse
 * @since 4.4
 */
public class GermanReadabilityRule extends ReadabilityRule {
  
  private final boolean tooEasyTest;

  public GermanReadabilityRule(ResourceBundle messages, Language lang, UserConfig userConfig, boolean tooEasyTest) {
    this(messages, lang, userConfig, tooEasyTest, -1, false);
  }
  
  public GermanReadabilityRule(ResourceBundle messages, Language lang, UserConfig userConfig, boolean tooEasyTest, int level) {
    this(messages, lang, userConfig, tooEasyTest, level, false);
  }
  
  public GermanReadabilityRule(ResourceBundle messages, Language lang, UserConfig userConfig, boolean tooEasyTest, boolean defaultOn) {
    this(messages, lang, userConfig, tooEasyTest, -1, defaultOn);
  }
  
  public GermanReadabilityRule(ResourceBundle messages, Language lang, UserConfig userConfig, 
      boolean tooEasyTest, int level, boolean defaultOn) {
    super(messages, lang, userConfig, tooEasyTest, level, defaultOn);
    super.setCategory(new Category(new CategoryId("CREATIVE_WRITING"), messages.getString("category_creative_writing"), Location.INTERNAL, false));
    this.tooEasyTest = tooEasyTest;
  }
  
  @Override
  public String getId() {
    return getId(tooEasyTest);
  }

  @Override
  public String getId(boolean tooEasyTest) {
    if (tooEasyTest) {
      return "READABILITY_RULE_SIMPLE_DE";
    } else {
      return "READABILITY_RULE_DIFFICULT_DE";
    }
  }

  @Override
  public String getDescription() {
    if (tooEasyTest) {
      return "Lesbarkeit: Zu einfacher Text";
    } else {
      return "Lesbarkeit: Zu schwieriger Text";
    }
  }

  private static String printMessageLevel(int level) {
    String sLevel = null;
    if (level == 0) {
      sLevel = "Sehr schwer";
    } else if (level == 1) {
      sLevel = "Schwer";
    } else if (level == 2) {
      sLevel = "Mittelschwer";
    } else if (level == 3) {
      sLevel = "Mittel";
    } else if (level == 4) {
      sLevel = "Mittelleicht";
    } else if (level == 5) {
      sLevel = "Leicht";
    } else if (level == 6) {
      sLevel = "Sehr leicht";
    }
    if (sLevel != null) {
      return " {Grad " + level + ": " + sLevel + "}";
    }
    return "";
  }
  
  @Override
  protected String getMessage(int level, int fre, int als, int asw) {
    String simple;
    String few;
    if (tooEasyTest) {
      simple = "einfach";
      few = "wenige";
    } else {
      simple = "schwierig";
      few = "viele";
    }
    return "Lesbarkeit: Der Text dieses Absatzes ist zu " + simple + printMessageLevel(level) + ". Zu "
        + few + " Wörter pro Satz und zu " + few + " Silben pro Wort.";
  }

  @Override
  public String getConfigureText() {
    return "Grad der Lesbarkeit 0 (sehr schwierig) bis 6 (sehr einfach):";
  }

  @Override
  protected double getFleschReadingEase(double asl, double asw) {
    return 180 - asl - ( 58.5 * asw );  //  German
  }
  
  private static boolean isVowel(char c) {
    return (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' || c == 'y' ||
        c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U' || c == 'Y' ||
        c == 'ä' || c == 'ö' || c == 'ü' || c == 'Ä' || c == 'Ö' || c == 'Ü');
  }
  
  @Override
  protected int simpleSyllablesCount(String word) {
    if (word.length() == 0) {
      return 0;
    }
    int nSyllables = 0;
    if (isVowel(word.charAt(0))) {
      nSyllables++;
    }
    boolean lastDouble = false;
    for (int i = 1; i < word.length(); i++) {
      char c = word.charAt(i);
      if (isVowel(c)) {
        char cl = word.charAt(i - 1);
        if (lastDouble) {
          nSyllables++;
          lastDouble = false;
        } else if (((c == 'i' || c == 'y') && (cl == 'a' || cl == 'e' || cl == 'A' || cl == 'E')) ||
            (c == 'u' && (cl == 'a' || cl == 'e' || cl == 'o' || cl == 'A' || cl == 'E' || cl == 'O')) ||
            (c == 'e' && (cl == 'e' || cl == 'i' || cl == 'E' || cl == 'I')) ||
            (c == 'a' && (cl == 'a' || cl == 'A'))) {
          lastDouble = true;
        } else {
          nSyllables++;
          lastDouble = false;
        }
      } else {
        lastDouble = false;
      }
    }
    return nSyllables == 0 ? 1 : nSyllables;
  }
  
}
