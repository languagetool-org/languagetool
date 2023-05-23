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
package org.languagetool.rules.pt;

import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.ReadabilityRule;
import org.languagetool.rules.Category.Location;

/**
 * A rule that checks the readability of Portuguese text (using the Flesch-Reading-Ease Formula)
 * If tooEasyTest == true, the rule tests if paragraph level &gt; level (readability is too easy)
 * If tooEasyTest == false, the rule tests if paragraph level &lt; level (readability is too difficult)
 * @author Fred Kruse
 * @since 4.4
 */
public class PortugueseReadabilityRule extends ReadabilityRule {
  
  private final boolean tooEasyTest;

  public PortugueseReadabilityRule(ResourceBundle messages, Language lang, UserConfig userConfig, boolean tooEasyTest) {
    this (messages, lang, userConfig, tooEasyTest, -1, false);
  }
  
  public PortugueseReadabilityRule(ResourceBundle messages, Language lang, UserConfig userConfig, boolean tooEasyTest, int level) {
    this (messages, lang, userConfig, tooEasyTest, level, false);
  }
  
  public PortugueseReadabilityRule(ResourceBundle messages, Language lang, UserConfig userConfig, boolean tooEasyTest, boolean defaultOn) {
    this (messages, lang, userConfig, tooEasyTest, -1, defaultOn);
  }
  
  public PortugueseReadabilityRule(ResourceBundle messages, Language lang, UserConfig userConfig, 
      boolean tooEasyTest, int level, boolean defaultOn) {
    super(messages, lang, userConfig, tooEasyTest, level, defaultOn);
    super.setCategory(new Category(new CategoryId("TEXT_ANALYSIS"), "Análise de Texto", Location.INTERNAL, false));
    this.tooEasyTest = tooEasyTest;
  }
  
  @Override
  public String getId() {
    return getId(tooEasyTest);
  }

  @Override
  public String getId(boolean tooEasyTest) {
    if (tooEasyTest) {
      return "READABILITY_RULE_SIMPLE_PT";
    } else {
      return "READABILITY_RULE_DIFFICULT_PT";
    }
  }

  @Override
  public String getDescription() {
    if (tooEasyTest) {
      return "Legibilidade: texto demasiado simples";
    } else {
      return "Legibilidade: texto demasiado complexo";
    }
  }

  @Override
  public String printMessageLevel(int level) {
    String sLevel = null;
    if (level == 0) {
      sLevel = "Muito complexo";
    } else if (level == 1) {
      sLevel = "Complexo";
    } else if (level == 2) {
      sLevel = "Moderadamente complexo";
    } else if (level == 3) {
      sLevel = "Meio-termo";
    } else if (level == 4) {
      sLevel = "Moderadamente simples";
    } else if (level == 5) {
      sLevel = "Simples";
    } else if (level == 6) {
      sLevel = "Muito simples";
    }
    if (sLevel != null) {
      return " {Nível " + level + ": " + sLevel + "}";
    }
    return "";
  }
  
  @Override
  protected String getMessage(int level, int fre, int asl, int asw) {
    String simple;
    String few;
    if (tooEasyTest) {
      simple = "fácil";
      few = "poucas";
    } else {
      simple = "difícil";
      few = "muitas";
    }
    return "Legibilidade {FRE: " + fre +", ASL: " + asl + ", ASW: " + asw + "}: O texto deste parágrafo é " + simple + printMessageLevel(level) + ". Tem "
        + few + " palavras por frase e " + few + " sílabas por palavra.";
  }

  @Override
  public String getConfigureText() {
    return "Nível de legibilidade 0 (muito difícil) a 6 (muito fácil):";
  }

  /* Equation for readability
   * FRE = Flesch-Reading-Ease
   * ASL = Average Sentence Length
   * ASW = Average Number of Syllables per Word
   * English:    FRE= 206,835 - ( 1,015 * ASL ) - ( 84,6 * ASW )
   * Portuguese: FRE= 206,840 - ( 1.020 * ASL ) - ( 60.0 * ASW )
   * http://ridi.ibict.br/bitstream/123456789/273/1/EnyIS2007.pdf
   */
  @Override
  public double getFleschReadingEase(double asl, double asw) {
    return 206.84 - (1.02 * asl) - ( 60.0 * asw);  //  Portuguese
  }
  
  private static boolean isVowel(char c) {
    return (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' || c == 'y' ||
        c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U' || c == 'Y' ||
        c == 'á' || c == 'é' || c == 'í' || c == 'ó' || c == 'ú' || c == 'à' || 
        c == 'Á' || c == 'É' || c == 'Í' || c == 'Ó' || c == 'Ú' || c == 'À' || 
        c == 'â' || c == 'ê' || c == 'ô' || 
        c == 'Â' || c == 'Ê' || c == 'Ô' || 
        c == 'Ü');
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
        } else if (((c == 'ã' || c == 'õ') && (cl == 'e' || cl == 'o')) || 
            (c == 'a' && (cl == 'e' || cl == 'i' || cl == 'í' || cl == 'o' || cl == 'u' || cl == 'ú')) ||
            (c == 'e' && (cl == 'e' || cl == 'i' || cl == 'í' || cl == 'o' || cl == 'a' || cl == 'u')) ||
            (c == 'i' && (cl == 'a' || cl == 'e' || cl == 'o' || cl == 'u' || cl == 'á' || cl == 'é')) ||
            (c == 'í' && (cl == 'a' || cl == 'e')) ||
            (c == 'o' && (cl == 'a' || cl == 'á' || cl == 'e' || cl == 'é' || cl == 'i' || cl == 'í' || cl == 'u')) ||
            (c == 'u' && (cl == 'a' || cl == 'á' || cl == 'e' || cl == 'é' || cl == 'i' || cl == 'o')) ||
            (c == 'ú' && (cl == 'a' || cl == 'e' || cl == 'o'))) {
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
