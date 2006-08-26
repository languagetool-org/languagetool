/**
 * French Tagger
 */
package de.danielnaber.languagetool.tagging.es;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import com.dawidweiss.stemmers.Lametyzator;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.tagging.Tagger;

/**
 * @author Marcin Milkowski
 *
 */
public class SpanishTagger implements Tagger {

  /* Spanish Tagger
   * 
   * Based on FreeLing tagger dictionary 
   * and Spanish Wikipedia corpus tagged with FreeLing
   * 
   * @author Marcin Milkowski
   */
  private static final String RESOURCE_FILENAME = "resource" +File.separator+ "es" +File.separator+
  "spanish.dict"; 
    private Lametyzator morfologik_spanish = null;
    
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens)
      throws IOException {
    String[] taggerTokens;
    boolean firstWord = true;
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    //caching Lametyzator instance - lazy init
    if (morfologik_spanish == null) {   
       File resourceFile = JLanguageTool.getAbsoluteFile(RESOURCE_FILENAME); 
       //System.setProperty(Lametyzator.PROPERTY_NAME_LAMETYZATOR_DICT, resourceFile.getAbsolutePath());
       morfologik_spanish = new Lametyzator(JLanguageTool.getInputStream(resourceFile.getAbsolutePath()), "iso8859-1", '+');
    }
    
    for (Iterator<String> iter = sentenceTokens.iterator(); iter.hasNext();) {
      String word = iter.next();
      List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
        taggerTokens = morfologik_spanish.stemAndForm(word);
        if (firstWord && taggerTokens == null) {        // e.g. "Das" -> "das" at start of sentence
            taggerTokens = morfologik_spanish.stemAndForm(word.toLowerCase());
        firstWord = false;
        }
    if (taggerTokens !=null) {
        int i = 0;
        while (i<taggerTokens.length)
        {
            //Lametyzator returns data as String[]
            //first lemma, then annotations
            l.add(new AnalyzedToken(word, taggerTokens[i+1], taggerTokens[i]));
            i=i+2;
        }
    }
    else 
        l.add(new AnalyzedToken(word, null, pos));
    pos += word.length();
    tokenReadings.add(new AnalyzedTokenReadings((AnalyzedToken[])l.toArray(new AnalyzedToken[0]))); 
    }
    return tokenReadings;

  }
  
  
  /* (non-Javadoc)
   * @see de.danielnaber.languagetool.tagging.Tagger#createNullToken(java.lang.String, int)
   */
  public Object createNullToken(String token, int startPos) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, startPos));
  }
  
  }
