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
import java.util.*;

import de.danielnaber.jwordsplitter.EmbeddedGermanDictionary;
import de.danielnaber.jwordsplitter.GermanWordSplitter;
import de.danielnaber.jwordsplitter.InputTooLongException;
import org.languagetool.tokenizers.Tokenizer;

import static java.util.Arrays.*;

/**
 * Split German nouns using the jWordSplitter library.
 * 
 * @author Daniel Naber
 */
public class GermanCompoundTokenizer implements Tokenizer {

  private final ExtendedGermanWordSplitter wordSplitter;
  
  public GermanCompoundTokenizer() throws IOException {
    this(true);
  }
  
  static class ExtendedGermanWordSplitter extends GermanWordSplitter {
    ExtendedGermanWordSplitter(boolean hideInterfixCharacters) throws IOException {
      super(hideInterfixCharacters, extendedList());
    }
    static Set<String> extendedList() {
      Set<String> words = new HashSet<>(EmbeddedGermanDictionary.getWords());
      // add compound parts here so we don't need to update JWordSplitter for every missing word we find:
      words.add("online");
      words.add("affin");
      words.add("affine");
      words.add("affiner");
      words.add("affines");
      words.add("affinen");
      words.add("affinem");
      words.add("trockner");
      words.add("thermostat");
      words.add("thermostats");
      words.add("fehl");
      words.add("circus");
      words.add("schi");
      words.add("codex");
      words.add("crème");
      words.add("crèmes");
      words.add("sauce");
      words.add("account");
      words.add("accounts");
      words.add("photograph");
      words.add("oxyd");
      words.add("playback");
      words.add("playbacks");
      words.add("blog");
      words.add("durchsuchung");
      words.add("durchsuchungen");
      words.add("kritisch");
      words.add("kritische");
      words.add("kritisches");
      words.add("kritischer");
      words.add("kritischen");
      words.add("kritischem");
      words.add("fortbewegung");
      words.add("freundlich");
      words.add("freundliche");
      words.add("freundliches");
      words.add("freundlicher");
      words.add("freundlichen");
      words.add("freundlichem");
      words.add("gelegenen");  // e.g. höchstgelegenen
      words.add("gelegenes");
      words.add("gelegene");
      words.add("gelegen");
      words.add("neurogen");
      words.add("neurogene");
      words.add("neurogenes");
      words.add("neurogenen");
      words.add("autogene");
      words.add("autogenes");
      words.add("autogenen");
      words.add("biogenetisch");
      words.add("biogenetische");
      words.add("biogenetischen");
      words.add("biogenetisches");
      words.add("fotogene");
      words.add("fotogenes");
      words.add("fotogenen");
      words.add("erlegene");
      words.add("erlegenes");
      words.add("erlegenen");
      words.add("genesenden");
      words.add("genesender");
      words.add("geneset");
      words.add("telegene");
      words.add("telegenen");
      words.add("telegenes");
      words.add("genetisch");
      words.add("genetische");
      words.add("genetischen");
      words.add("genetisches");
      return words;
    }
  }
  
  public GermanCompoundTokenizer(boolean strictMode) throws IOException {
    wordSplitter = new ExtendedGermanWordSplitter(false);
    // add exceptions here so we don't need to update JWordSplitter for every exception we find:  
    wordSplitter.addException("Maskerade", Collections.singletonList("Maskerade"));
    wordSplitter.addException("Sportshorts", asList("Sport", "shorts")); 
    wordSplitter.addException("Bermudashorts", asList("Bermuda", "shorts"));
    wordSplitter.addException("Laufshorts", asList("Lauf", "shorts"));
    wordSplitter.addException("Badeshorts", asList("Bade", "shorts"));
    wordSplitter.addException("Buchungstrick", asList("Buchungs", "trick"));
    wordSplitter.addException("Buchungstricks", asList("Buchungs", "tricks"));
    wordSplitter.addException("Rückzugsorte", asList("Rückzugs", "orte"));
    wordSplitter.addException("Malerarbeiten", asList("Maler", "arbeiten"));
    wordSplitter.addException("Siebengestirn", asList("Sieben", "gestirn"));
    wordSplitter.addException("Siebengestirne", asList("Sieben", "gestirne"));
    wordSplitter.addException("Siebengestirnen", asList("Sieben", "gestirnen"));
    wordSplitter.addException("Siebengestirns", asList("Sieben", "gestirns"));
    wordSplitter.addException("Siebengestirnes", asList("Sieben", "gestirnes"));
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

