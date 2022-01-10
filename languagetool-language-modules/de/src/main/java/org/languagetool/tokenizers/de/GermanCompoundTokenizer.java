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

import com.google.common.base.Suppliers;
import de.danielnaber.jwordsplitter.EmbeddedGermanDictionary;
import de.danielnaber.jwordsplitter.GermanWordSplitter;
import de.danielnaber.jwordsplitter.InputTooLongException;
import gnu.trove.THashSet;
import org.languagetool.tokenizers.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Arrays.asList;

/**
 * Split German nouns using the jWordSplitter library.
 * 
 * @author Daniel Naber
 */
public class GermanCompoundTokenizer implements Tokenizer {
  private static final Supplier<GermanCompoundTokenizer> strictInstance = Suppliers.memoize(() -> {
    try {
      return new GermanCompoundTokenizer(true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  });
  private static final Supplier<GermanCompoundTokenizer> nonStrictInstance = Suppliers.memoize(() -> {
    try {
      return new GermanCompoundTokenizer(false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  });

  private final ExtendedGermanWordSplitter wordSplitter;
  
  public GermanCompoundTokenizer() throws IOException {
    this(true);
  }
  
  static class ExtendedGermanWordSplitter extends GermanWordSplitter {
    ExtendedGermanWordSplitter(boolean hideInterfixCharacters) throws IOException {
      super(hideInterfixCharacters, extendedList());
    }
    static Set<String> extendedList() {
      THashSet<String> words = new THashSet<>(EmbeddedGermanDictionary.getWords());
      // add compound parts here so we don't need to update JWordSplitter for every missing word we find:
      words.add("synonym");
      words.trimToSize();
      return words;
    }
  }
  
  public GermanCompoundTokenizer(boolean strictMode) throws IOException {
    wordSplitter = new ExtendedGermanWordSplitter(false);
    // add exceptions here so we don't need to update JWordSplitter for every exception we find:  
    //wordSplitter.addException("Maskerade", Collections.singletonList("Maskerade"));
    //wordSplitter.addException("Sportshorts", asList("Sport", "shorts")); 
    wordSplitter.addException("Hallesche", asList("Hallesche"));
    wordSplitter.addException("Halleschen", asList("Halleschen"));
    wordSplitter.addException("Reinigungstab", asList("Reinigungs", "tab"));
    wordSplitter.addException("Reinigungstabs", asList("Reinigungs", "tabs"));
    wordSplitter.addException("Tauschwerte", asList("Tausch", "werte"));
    wordSplitter.addException("Tauschwertes", asList("Tausch", "wertes"));
    wordSplitter.addException("Kinderspielen", asList("Kinder", "spielen"));
    wordSplitter.addException("Buchhaltungstrick", asList("Buchhaltungs", "trick"));
    wordSplitter.addException("Buchhaltungstricks", asList("Buchhaltungs", "tricks"));
    wordSplitter.addException("karamelligen", asList("karamelligen"));  // != Karamel+Ligen
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

  public static GermanCompoundTokenizer getStrictInstance() {
    return strictInstance.get();
  }

  public static GermanCompoundTokenizer getNonStrictInstance() {
    return nonStrictInstance.get();
  }

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.out.println("Usage: " + GermanCompoundTokenizer.class.getSimpleName() + " <wordsToSplit... or file>");
      System.exit(1);
    }
    GermanCompoundTokenizer tokenizer = new GermanCompoundTokenizer();
    if (new File(args[0]).exists()) {
      System.out.println("Working on lines from " + args[0] + ":");
      List<String> lines = Files.readAllLines(Paths.get(args[0]));
      for (String line : lines) {
        System.out.println(tokenizer.tokenize(line));
      }
    } else {
      for (String arg : args) {
        System.out.println(tokenizer.tokenize(arg));
      }
    }
  }

}

