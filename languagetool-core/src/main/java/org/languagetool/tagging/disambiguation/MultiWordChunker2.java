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

package org.languagetool.tagging.disambiguation;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tools.StringTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Multiword tagger-chunker.
 * Note: currently does not support:
 * <ul>
 *  <li> overlapping tagging (first matching multiword entry wins)
 * </ul>
 * @author Andriy Rysin
 */
public class MultiWordChunker2 extends AbstractDisambiguator {
  private static final String WRAP_TAG = "<%s>";

  private final String filename;
  private final boolean allowFirstCapitalized;
  private boolean removeOtherReadings = false;
  private String tagFormat = WRAP_TAG;
  private Map<String, List<MultiWordEntry>> tokenToPosTagMap;


  /**
   * @param filename file text with multiwords and tags
   */
  public MultiWordChunker2(String filename) {
    this(filename, false);
  }
  
  /**
   * @param filename file text with multiwords and tags
   * @param allowFirstCapitalized if set to {@code true}, first word of the multiword can be capitalized
   */
  public MultiWordChunker2(String filename, boolean allowFirstCapitalized) {
    this.filename = filename;
    this.allowFirstCapitalized = allowFirstCapitalized;
  }

  /**
   * @param removeOtherReadings If true and multiword matches other readings will be removed
   */
  public void setRemoveOtherReadings(boolean removeOtherReadings) {
    this.removeOtherReadings = removeOtherReadings;
  }

  /**
   * @param wrapTag If true the tag will be wrapped with &lt; and &gt;
   */
  public void setWrapTag(boolean wrapTag) {
    tagFormat = wrapTag ? WRAP_TAG : null;
  }
  
  /**
   * Override this method if you want format POS tag differently
   * @param posTag POS tag for the multiword
   * @param position Position of the token in the multiword
   * @return Returns formatted POS tag for the multiword
   */
  protected String formatPosTag(String posTag, int position, int multiwordLength) {
    return tagFormat != null ? String.format(tagFormat, posTag) : posTag;
  }
  
  /*
   * Lazy init, thanks to Artur Trzewik
   */
  private void lazyInit() {

    if (tokenToPosTagMap != null) {
      return;
    }

    Map<String, List<MultiWordEntry>> map = new HashMap<>();

    try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(filename)) {
      List<String> posTokens = loadWords(stream);

      for (String posToken : posTokens) {
        String[] tokenAndTag = posToken.split("\t");
        if (tokenAndTag.length != 2) {
          throw new RuntimeException("Invalid format in " + filename + ": '" + posToken + "', expected two tab-separated parts");
        }
        
        String[] tokens = tokenAndTag[0].split(" ");
        String posTag = tokenAndTag[1];
        
        List<MultiWordEntry> multiwordItems;
        if( map.containsKey(tokens[0]) ) {
          multiwordItems = map.get(tokens[0]);
        }
        else {
          multiwordItems = new ArrayList<>();
          map.put(tokens[0], multiwordItems);
        }
        
        multiwordItems.add(new MultiWordEntry(Arrays.asList(tokens), posTag));
      }
      
      tokenToPosTagMap = map;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Implements multiword POS tags, e.g., &lt;ELLIPSIS&gt; for ellipsis (...)
   * start, and &lt;/ELLIPSIS&gt; for ellipsis end.
   *
   * @param input The tokens to be chunked.
   * @return AnalyzedSentence with additional markers.
   */
  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input) {

    lazyInit();

    AnalyzedTokenReadings[] inputTokens = input.getTokens();
    AnalyzedTokenReadings[] outputTokens = inputTokens;
    
    for (int i = 1; i < inputTokens.length; i++) {
      AnalyzedTokenReadings analyzedToken = inputTokens[i];
      
      String firstToken = analyzedToken.getToken();
      
      List<MultiWordEntry> multiwordItems = tokenToPosTagMap.get(firstToken);
      
      if( multiwordItems == null ) {
        if (allowFirstCapitalized && StringTools.isCapitalizedWord(firstToken) ) {
          multiwordItems = tokenToPosTagMap.get(StringTools.lowercaseFirstChar(firstToken));
        }
        if( multiwordItems == null )
          continue;
      }
      
      MultiWordEntry multiwordEntry = findMultiwordEntry(inputTokens, i, multiwordItems);
      
      if( multiwordEntry == null )
        continue;

      for(int multiwordPos=0, inputTokenPos=i; multiwordPos<multiwordEntry.tokens.size(); inputTokenPos++) {
        AnalyzedTokenReadings currentToken = inputTokens[inputTokenPos];
        
        if( currentToken.isWhitespace() ) {
          continue;
        }
        
        String multiwordTag = formatPosTag(multiwordEntry.tag, multiwordPos, multiwordEntry.tokens.size());

        outputTokens[inputTokenPos] = prepareNewReading(multiwordEntry.getLemma(), currentToken.getToken(), currentToken, multiwordTag);
        ++multiwordPos;
      }
    }
    
    return new AnalyzedSentence(outputTokens);
  }

  private MultiWordEntry findMultiwordEntry(AnalyzedTokenReadings[] inputTokens, int startingPosition, List<MultiWordEntry> multiwordItems) {
    for (MultiWordEntry multiWordEntry : multiwordItems) {
      if( isMatching(inputTokens, startingPosition, multiWordEntry) )
        return multiWordEntry;
    }
    
    return null;
  }

  private boolean isMatching(AnalyzedTokenReadings[] inputTokens, int startingPosition, MultiWordEntry multiWordEntry) {
    int j=1;  // we already matched the first token from multiword
    for(int i=1; j<multiWordEntry.tokens.size() && startingPosition+i<inputTokens.length; i++) {
  
      if( inputTokens[startingPosition+i].isWhitespace() ) {
        continue;
      }
      
      if( ! matches(multiWordEntry.tokens.get(j), inputTokens[startingPosition+i]) )
       return false;
      
      ++j;
    }
    return j == multiWordEntry.tokens.size();
  }
  
  protected boolean matches(String matchText, AnalyzedTokenReadings inputTokens) {
    return matchText.equals(inputTokens.getToken());
  }

  protected AnalyzedTokenReadings prepareNewReading(String tokens, String tok, AnalyzedTokenReadings token, String tag) {
    AnalyzedToken tokenStart = new AnalyzedToken(tok, tag, tokens);
    return setAndAnnotate(token, tokenStart);
  }

  private AnalyzedTokenReadings setAndAnnotate(AnalyzedTokenReadings oldReading, AnalyzedToken newReading) {
    String old = oldReading.toString();
    String prevAnot = oldReading.getHistoricalAnnotations();

    List<AnalyzedToken> initialNewReadings = removeOtherReadings ? Arrays.asList(newReading) : oldReading.getReadings();
    AnalyzedTokenReadings newAtr = new AnalyzedTokenReadings(initialNewReadings, oldReading.getStartPos());
    newAtr.setWhitespaceBefore(oldReading.isWhitespaceBefore());
    if( ! removeOtherReadings ) {
      newAtr.addReading(newReading);
    }
    newAtr.setHistoricalAnnotations(annotateToken(prevAnot, old, newAtr.toString()));
    newAtr.setChunkTags(oldReading.getChunkTags());
    
    return newAtr;
  }
  
  private String annotateToken(String prevAnot, String oldReading, String newReading) {
    return prevAnot + "\nMULTIWORD_CHUNKER: " + oldReading + " -> " + newReading;
  }

  private List<String> loadWords(InputStream stream) {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.charAt(0) == '#') {  // ignore comments
          continue;
        }
        lines.add(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return lines;
  }

  
  private static final class MultiWordEntry {
    List<String> tokens;
    String tag;

    public MultiWordEntry(List<String> tokens, String tag) {
      this.tokens = tokens;
      this.tag = tag;
    }
    
    String getLemma() {
      return StringUtils.join(tokens, " ");
    }

    @Override
    public String toString() {
      return "MultiWordEntry [tokens=" + tokens + ", tag=" + tag + "]";
    }
    
  }

}