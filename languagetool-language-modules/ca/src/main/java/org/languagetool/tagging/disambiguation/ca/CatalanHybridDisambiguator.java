/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.tagging.disambiguation.ca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.language.Catalan;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.MultiWordChunker;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tools.StringTools;

/**
 * Hybrid chunker-disambiguator for Catalan
 * 
 * @author Jaume Ortolà
 */
public class CatalanHybridDisambiguator extends AbstractDisambiguator {

  private final MultiWordChunker chunker = new MultiWordChunker("/ca/multiwords.txt", true, true, false);
  private final MultiWordChunker chunkerGlobal = new MultiWordChunker("/spelling_global.txt", false, true, false,"NPCN000");
  private final Disambiguator disambiguator;

  private final String englishDictFilepath = "/en/hunspell/en_US.dict";
  protected MorfologikSpeller englishSpeller;

  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException {
    return disambiguate(input, null);
  }

  /**
   * Calls two disambiguator classes: (1) a chunker; (2) a rule-based
   * disambiguator.
   */
  
  public CatalanHybridDisambiguator(Language lang) {
    disambiguator = new XmlRuleDisambiguator(lang, true);
    chunker.setRemovePreviousTags(true);
    try {
      englishSpeller = new MorfologikSpeller(englishDictFilepath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public final AnalyzedSentence disambiguate(AnalyzedSentence input,
                                             @Nullable JLanguageTool.CheckCancelledCallback checkCanceled) throws IOException {
    return ignoreEnglishWords(disambiguator.disambiguate(chunker.disambiguate(chunkerGlobal.disambiguate(input,
      checkCanceled), checkCanceled), checkCanceled), checkCanceled);
  }

  private AnalyzedSentence ignoreEnglishWords(AnalyzedSentence input,
                                              @Nullable JLanguageTool.CheckCancelledCallback checkCanceled) {
    AnalyzedTokenReadings[] anTokens = input.getTokens();
    AnalyzedTokenReadings[] output = anTokens;
    boolean prevIsEnglish = false;
    boolean isEnglish;
    Integer skippedTokens = 0;
    for (int i = 0; i < anTokens.length - 1; i++) {
      String word = output[i].getToken();
      if (word.length() < 1 || StringTools.isWhitespace(word) || StringTools.isNotWordString(word)) {
        skippedTokens++;
        continue;
      }
      if (!prevIsEnglish && (output[i].isIgnoredBySpeller() || output[i].isTagged())) {
        prevIsEnglish = output[i].hasPosTag("_english_ignore_");
        skippedTokens = 1;
        continue;
      }
      isEnglish = output[i].hasPosTag("_english_ignore_") || !englishSpeller.isMisspelled(word);
      if (isEnglish && prevIsEnglish) {
        output[i].ignoreSpelling();
        output[i - skippedTokens].ignoreSpelling();
      }
      skippedTokens = 1;
      prevIsEnglish = isEnglish;
    }
    return new AnalyzedSentence(output);
  }



}
