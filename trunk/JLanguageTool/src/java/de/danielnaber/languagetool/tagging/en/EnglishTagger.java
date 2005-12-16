/* JLanguageTool, a natural language style checker 
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
package de.danielnaber.languagetool.tagging.en;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import opennlp.tools.lang.english.PosTagger;
import opennlp.tools.ngram.Dictionary;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tagging.Tagger;

/**
 * Encapsulate the OpenNLP POS tagger for English.
 * 
 * @author Daniel Naber
 */
public class EnglishTagger implements Tagger {

  private static final String RESOURCE_FILENAME = "resource" +File.separator+ "en" +File.separator+
    "EnglishPOS.bin.gz";
  
  private PosTagger tagger = null;

  public EnglishTagger() {
  }
  
  public List tag(List tokens) {
    // lazy init to save startup time if the English tagger isn't used:
    if (tagger == null) {
      File resourceFile = JLanguageTool.getAbsoluteFile(RESOURCE_FILENAME);
      tagger = new PosTagger(resourceFile.getAbsolutePath(), (Dictionary)null);
    }
    List taggerTokens = tagger.tag(tokens);
    List analyzedTokens = new ArrayList();
    int i = 0;
    int pos = 0;
    for (Iterator iter = taggerTokens.iterator(); iter.hasNext();) {
      String posTag = (String) iter.next();
      String token = (String)tokens.get(i);
      // startPos will be set in JLanguageTool:
      analyzedTokens.add(new AnalyzedToken(token, posTag, 0));
      i++;
      pos += token.length();
    }
    return analyzedTokens;
  }

}
