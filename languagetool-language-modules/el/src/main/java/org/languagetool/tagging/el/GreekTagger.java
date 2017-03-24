/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.el;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.ioperm.morphology.el.GreekAnalyzer;
import org.ioperm.morphology.el.Lemma;
import org.languagetool.AnalyzedToken;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.WordTagger;

/**
 *
 * @author Panagiotis Minos (pminos@gmail.com)
 */
public class GreekTagger extends BaseTagger {

  private final GreekAnalyzer tagger;

  @Override
  public String getManualAdditionsFileName() {
    return "/el/added.txt";
  }

  public GreekTagger() {
    super("/el/greek.dict",  new Locale("el"));
    tagger = new GreekAnalyzer();
  }

  @Override
  protected List<AnalyzedToken> additionalTags(String word, WordTagger wordTagger) {
    List<AnalyzedToken> tokens = new ArrayList<>();
    List<Lemma> lemma = tagger.getLemma(word, false);
    for(Lemma lm : lemma) {
      AnalyzedToken tk =  new AnalyzedToken(word, lm.getTag(), lm.getLemma());
      tokens.add(tk);
    }
    return tokens;
  }
}
