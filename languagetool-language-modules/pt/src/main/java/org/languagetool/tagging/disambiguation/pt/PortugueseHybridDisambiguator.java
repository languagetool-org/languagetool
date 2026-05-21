/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.tagging.disambiguation.pt;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.MultiWordChunker;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;

import java.io.IOException;

/**
 * Hybrid chunker-disambiguator for Portuguese.
 * 
 */
public class PortugueseHybridDisambiguator extends AbstractDisambiguator {

  private final MultiWordChunker chunker = MultiWordChunker.getInstance("/pt/multiwords.txt", true, true, true);
  private final MultiWordChunker chunkerGlobal = MultiWordChunker.getInstance("/spelling_global.txt", false, true, true, "NPCN000");
  private final Disambiguator disambiguator;

  public PortugueseHybridDisambiguator(Language lang) {
    chunkerGlobal.setIgnoreSpelling(true);
    disambiguator = new XmlRuleDisambiguator(lang, true);
    chunker.setRemovePreviousTags(true);
    chunker.setIgnoreSpelling(true);
  }

  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException {
    return disambiguate(input, null);
  }
  /**
   * Calls two disambiguator classes: (1) a chunker; (2) a rule-based
   * disambiguator.
   * 
   * Put the results of the MultiWordChunker in a more appropriate and useful way.
   *   &lt;NN&gt;&lt;/NN&gt; becomes NN NN
   *   The individual original tags are removed. 
   *   Add spell ignore
   */
  @Override
  public final AnalyzedSentence disambiguate(AnalyzedSentence input, @Nullable JLanguageTool.CheckCancelledCallback checkCanceled)
      throws IOException {
    return disambiguator.disambiguate(chunker.disambiguate(chunkerGlobal.disambiguate(input, checkCanceled), checkCanceled), checkCanceled);
  }

}
