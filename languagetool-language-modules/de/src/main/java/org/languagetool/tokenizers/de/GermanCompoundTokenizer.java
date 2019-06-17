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
package org.languagetool.tokenizers.de;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.danielnaber.jwordsplitter.GermanWordSplitter;
import de.danielnaber.jwordsplitter.InputTooLongException;
import org.languagetool.tokenizers.Tokenizer;

/**
 * Split German nouns using the jWordSplitter library.
 * 
 * @author Daniel Naber
 */
public class GermanCompoundTokenizer implements Tokenizer {

  private final GermanWordSplitter wordSplitter;
  
  public GermanCompoundTokenizer() throws IOException {
    this(true);
  }
  
  public GermanCompoundTokenizer(boolean strictMode) throws IOException {
    wordSplitter = new GermanWordSplitter(false);
    // add exceptions here so we don't need to update JWordSplitter for every exception we find:  
    wordSplitter.addException("Maskerade", Collections.singletonList("Maskerade"));
    wordSplitter.addException("Sportshorts", Arrays.asList("Sport", "shorts")); 
    wordSplitter.addException("Bermudashorts", Arrays.asList("Bermuda", "shorts"));
    wordSplitter.addException("Laufshorts", Arrays.asList("Lauf", "shorts"));
    wordSplitter.addException("Badeshorts", Arrays.asList("Bade", "shorts"));
    wordSplitter.addException("Buchungstrick", Arrays.asList("Buchungs", "trick"));
    wordSplitter.addException("Buchungstricks", Arrays.asList("Buchungs", "tricks"));
    wordSplitter.setStrictMode(strictMode);
    wordSplitter.setMinimumWordLength(3);
  }

  @Override
  public List<String> tokenize(String word) {
    try {
      return wordSplitter.splitWord(word);
    } catch (InputTooLongException e) {
      return Collections.singletonList(word);
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + GermanCompoundTokenizer.class.getSimpleName() + " <wordToSplit>");
      System.exit(1);
    }
    GermanCompoundTokenizer tokenizer = new GermanCompoundTokenizer();
    System.out.println(tokenizer.tokenize(args[0]));
  }

}

