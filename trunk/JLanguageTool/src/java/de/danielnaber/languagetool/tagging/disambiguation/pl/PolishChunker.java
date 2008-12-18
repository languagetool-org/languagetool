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

package de.danielnaber.languagetool.tagging.disambiguation.pl;

import java.util.HashMap;
import java.util.Map;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;

/**
 * Multiword tagger-chunker for Polish. 
 * 
 * @author Marcin Miłkowski
 */
  public class PolishChunker implements Disambiguator {
    
    private Map <String, String> mStartSpace;
    private Map <String, String> mStartNoSpace;
    private Map <String, String> mFull;

    /**
     * Simple formatted static string with multiword tokens.
     * Can be moved to a local file in the future. 
     */

      private static final String TOKEN_DEFINITIONS = 
        "...|ELLIPSIS\n" 
        + "bez mała|ADV\n"
        + "to znaczy|TO_ZNACZY\nTo znaczy|TO_ZNACZY\n" 
        + "to jest|TO_JEST\nTo jest|TO_JEST\n" 
        + "z uwagi na|PREP:ACC\n" 
        + "ze względu na|PREP:ACC\n" 
        + "bez względu na|PREP:ACC\n"
        + "w oparciu o|PREP:ACC\n"
        + "w związku z|PREP:INST\n"
        + "w zgodzie z|PREP:INST\n"
        + "zgodnie z|PREP:INST\n"
        + "w porównaniu z|PREP:INST\n"
        + "odnośnie do|PREP:GEN\n"
        + "za pomocą|PREP:GEN\n"      
        + "na mocy|PREP:GEN\n"
        + "na podstawie|PREP:GEN\n"
        + "w braku|PREP:GEN\n"
        + "w razie|PREP:GEN\n"
        + "w odniesieniu do|PREP:GEN\n"
        + "w stosunku do|PREP:GEN\n"
        + "w relacji do|PREP:GEN\n"
        + "a także|CONJ\n"
        + "na co dzień|ADV\n"
        + "co najmniej|ADV\n"
        + "co najwyżej|ADV\n"
        + "co nieco|ADV\n"
        + "mimo wszystko|ADV\nMimo wszystko|ADV\n"
        + "do czysta|ADV\n"
        + "do cna|ADV\n"
        + "do naga|ADV\n"
        + "do niedawna|ADV\nDo niedawna|ADV\n"
        + "do równa|ADV\n"
        + "do syta|ADV\n"
        + "do żywa|ADV\n" 
        + "od nowa|ADV\nOd nowa|ADV\n";
      
    /* Lazy init, thanks to Artur Trzewik
     */
    private void lazyInit() {
      
      if (mStartSpace != null) return;

      mStartSpace = new HashMap <String, String>();
      mStartNoSpace = new HashMap <String, String>();
      mFull = new HashMap <String, String>();      

      final String[] posTokens = TOKEN_DEFINITIONS.split("\n");
      for (String posToken : posTokens) {
        final String[] tokenAndTag = posToken.split("\\|");
        final boolean containsSpace = tokenAndTag[0].indexOf(' ') > 0;
        String firstToken = "";
        String[] firstTokens;
        if (!containsSpace) {
          firstTokens = new String[tokenAndTag[0].length()];
          firstToken = tokenAndTag[0].substring(0, 1);
          for (int i = 1; i < tokenAndTag[0].length(); i++) {
            firstTokens [i] = tokenAndTag[0].substring(0 + (i - 1), i);
          }
          if (mStartNoSpace.containsKey(firstToken)) {
            if (Integer.parseInt(mStartNoSpace.get(firstToken)) 
                < firstTokens.length) {
              mStartNoSpace.put(firstToken, 
                  Integer.toString(firstTokens.length));
            }         
          } else {
            mStartNoSpace.put(firstToken, 
                Integer.toString(firstTokens.length));
          }
        } else {            
          firstTokens = tokenAndTag[0].split(" ");
          firstToken = firstTokens[0];

          if (mStartSpace.containsKey(firstToken)) {
            if (Integer.parseInt(mStartSpace.get(firstToken)) 
                < firstTokens.length) {
              mStartSpace.put(firstToken, 
                  Integer.toString(firstTokens.length));
            }          
          } else {
            mStartSpace.put(firstToken, Integer.toString(firstTokens.length));
          }  
        }
        mFull.put(tokenAndTag[0], tokenAndTag[1]);
      }
    }
    /**
     * Implements multiword POS tags, e.g., 
     * &lt;ELLIPSIS&gt; for ellipsis (...) start, 
     * and &lt;/ELLIPSIS&gt; for ellipsis end.
     * @param input The tokens to be chunked.
     * @return AnalyzedSentence with additional markers.
     */
    public final AnalyzedSentence disambiguate(final AnalyzedSentence input) {

      lazyInit();

      final AnalyzedTokenReadings[] anTokens = input.getTokens();
      AnalyzedTokenReadings[] output = 
        new AnalyzedTokenReadings[anTokens.length];

      output = anTokens;        

      for (int i = 0; i < anTokens.length; i++) {
        final String tok = output[i].getToken();          
        final StringBuffer tokens = new StringBuffer();

        int finalLen = 0;
        if (mStartSpace.containsKey(tok)) {
          final int len = Integer.parseInt(mStartSpace.get(tok)); 
          int j = i;
          int lenCounter = 0;
          while (j < anTokens.length) {
            if (!anTokens[j].isWhitespace()) {
              tokens.append(anTokens[j].getToken());
              if (mFull.containsKey(tokens.toString())) {            
                final AnalyzedToken tokenStart = 
                  new AnalyzedToken(tok, 
                      "<" + mFull.get(tokens.toString()) + ">",
                      tokens.toString());               
                output[i].addReading(tokenStart);
                final AnalyzedToken tokenEnd = 
                  new AnalyzedToken(anTokens[finalLen].getToken(), 
                      "</" + mFull.get(tokens.toString()) + ">",
                      tokens.toString());
                output[finalLen].addReading(tokenEnd);
              }
              lenCounter++;
              if (lenCounter == len) {
                break;
              } else {
                tokens.append(' ');
              }
            }
            j++;
            finalLen = j;
          }
        }

        if (mStartNoSpace.containsKey(tok)) {
          final int len = Integer.parseInt(mStartNoSpace.get(tok)); 
          if (i + len < anTokens.length) {            
            for (int j = i; j < i + len; j++) {            
              tokens.append(anTokens[j].getToken());            
              if (mFull.containsKey(tokens.toString())) {            
                final AnalyzedToken tokenStart = 
                  new AnalyzedToken(tok, 
                      "<" + mFull.get(tokens.toString()) + ">",
                      tokens.toString());
                output[i].addReading(tokenStart);
                final AnalyzedToken tokenEnd = 
                  new AnalyzedToken(anTokens[i + len - 1].getToken(),
                      "</" + mFull.get(tokens.toString()) + ">",
                      tokens.toString());
                output[i + len  - 1].addReading(tokenEnd);
              }
            }
          }          

        }
      }

      return new AnalyzedSentence(output);
    }

  }