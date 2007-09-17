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
package de.danielnaber.languagetool.tagging.pl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import morfologik.stemmers.Lametyzator;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.BaseTagger;

/**
 * Polish POS tagger based on FSA morphological dictionaries.
 * 
 * @author Marcin Milkowski
 */


public class PolishTagger extends BaseTagger {

	private static final String RESOURCE_FILENAME = "/resource/pl/polish.dict"; 
	private Lametyzator morfologik = null; 

  public void setFileName() {
    System.setProperty(Lametyzator.PROPERTY_NAME_LAMETYZATOR_DICTIONARY, 
        RESOURCE_FILENAME);    
  }

  public final List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) throws IOException {
    String[] taggerTokens;
    
	List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    //caching Lametyzator instance - lazy init
	if (morfologik == null){
     setFileName();
	   morfologik = new Lametyzator();
	}
	
    for (String word : sentenceTokens) {
      List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();      
      String[] lowerTaggerTokens = null;
        taggerTokens = morfologik.stemAndForm(word);
        if (!word.equals(word.toLowerCase())) {
          lowerTaggerTokens = morfologik.stemAndForm(word.toLowerCase());
        }
                
    if (taggerTokens != null) {
       int i = 0;
        while (i < taggerTokens.length) {
            //Lametyzator returns data as String[]
            //first lemma, then annotations
            //use Jozef's idea
            final String lemma = taggerTokens[i];
            final String[] tagsArr = taggerTokens[i + 1].split("\\+");

            for (String currTag : tagsArr) {
              l.add(new AnalyzedToken(word, currTag, lemma));
            }
            i = i + 2;
        } 
      }     
    if (lowerTaggerTokens != null) {
      int i = 0;
       while (i < lowerTaggerTokens.length) {
           //Lametyzator returns data as String[]
           //first lemma, then annotations
           //use Jozef's idea
           final String lemma = lowerTaggerTokens[i];
           final String[] tagsArr = lowerTaggerTokens[i + 1].split("\\+");

           for (String currTag : tagsArr) {
             l.add(new AnalyzedToken(word, currTag, lemma));
           }
           i = i + 2;
       } 
     }        
    
    if (lowerTaggerTokens == null && taggerTokens == null) {
            l.add(new AnalyzedToken(word, null, pos));                       
    }
      pos += word.length();
      tokenReadings.add(new AnalyzedTokenReadings((AnalyzedToken[]) l.toArray(new AnalyzedToken[l.size()])));
   }
    
    return tokenReadings;

  }  

}
