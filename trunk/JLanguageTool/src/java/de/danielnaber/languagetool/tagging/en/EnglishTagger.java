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
    AnalyzedToken nextAnalyzedToken = null;
    for (Iterator iter = taggerTokens.iterator(); iter.hasNext();) {
      String posTag = (String) iter.next();
      String token = (String)tokens.get(i);
      String nextToken = null;
      if (i < tokens.size()-1)
        nextToken = (String)tokens.get(i+1);
      // the tagger has problems with contracted forms (not only because we turn "don't" into "don", "t"),
      // so fix it manually:
      if (nextAnalyzedToken != null) {
        analyzedTokens.add(nextAnalyzedToken);
        nextAnalyzedToken = null;
      } else if (token.equals("doesn") && "t".equals(nextToken)) {
        analyzedTokens.add(new AnalyzedToken(token, "VBZ", 0));
        nextAnalyzedToken = new AnalyzedToken(nextToken, "RB", 0);
      } else if (token.equals("don") && "t".equals(nextToken)) {
        analyzedTokens.add(new AnalyzedToken(token, "VBP", 0));
        nextAnalyzedToken = new AnalyzedToken(nextToken, "RB", 0);
      } else if (token.equals("won") && "t".equals(nextToken)) {
        analyzedTokens.add(new AnalyzedToken(token, "MD", 0));
        nextAnalyzedToken = new AnalyzedToken(nextToken, "RB", 0);
      } else if (token.equals("they") && "ll".equals(nextToken)) {
        analyzedTokens.add(new AnalyzedToken(token, "PRP", 0));
        nextAnalyzedToken = new AnalyzedToken(nextToken, "MD", 0);
      } else {
        analyzedTokens.add(new AnalyzedToken(token, posTag, 0));    // startPos will be set in JLanguageTool
      }
      i++;
      pos += token.length();
    }
    return analyzedTokens;
  }

  public Object createNullToken(String token, int startPos) {
    return new AnalyzedToken(token, null, startPos);
  }

  /* testing only:
  public static void main(String[] args) {
    File resourceFile = JLanguageTool.getAbsoluteFile(RESOURCE_FILENAME);
    PosTagger tagger = new PosTagger(resourceFile.getAbsolutePath(), (Dictionary)null);
    String[] l = new String[] {"Then",  "don", "'t",  "take", "us", "there", "."};
    String[] taggerTokens = tagger.tag(l);
    //String taggerTokens = tagger.tag("He will not listen to me. Then don 't say so! We 'll show you there 's no way out. Do not say so.");
    for (int i = 0; i < taggerTokens.length; i++) {
      System.err.println(l[i] + " "+ taggerTokens[i]);
    }
  }
  */
  
}
