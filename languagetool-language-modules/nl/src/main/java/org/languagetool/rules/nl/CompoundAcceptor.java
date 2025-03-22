/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Mark Baas
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
package org.languagetool.rules.nl;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.languagetool.*;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.CachingWordListLoader;
import org.languagetool.tagging.nl.DutchTagger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Accept Dutch compounds that are not accepted by the speller. This code
 * is supposed to accept more words in order to extend the speller, but it's not meant
 * to accept all valid compounds.
 * It works on 2-part compounds only for now.
 */
public class CompoundAcceptor {

  private static final Pattern acronymPattern = Pattern.compile("[A-Z]{2,4}-");
  private static final Pattern specialAcronymPattern = Pattern.compile("[A-Za-z]{2,4}-");
  private static final Pattern normalCasePattern = Pattern.compile("[A-Za-z][a-zé]*");
  private static final int MAX_WORD_SIZE = 35;
  public static final CompoundAcceptor INSTANCE = new CompoundAcceptor();
  protected final CachingWordListLoader wordListLoader = new CachingWordListLoader();
  protected final Set<String> noS = new ObjectOpenHashSet<>(), 
                              needsS = new ObjectOpenHashSet<>(),
                              geographicalDirections = new ObjectOpenHashSet<>(),
                              alwaysNeedsS = new ObjectOpenHashSet<>(),
                              alwaysNeedsHyphen = new ObjectOpenHashSet<>(),
                              part1Exceptions = new ObjectOpenHashSet<>(),
                              part2Exceptions = new ObjectOpenHashSet<>(),
                              acronymExceptions = new ObjectOpenHashSet<>();
  private static final String COMPOUND_NO_S_FILE = "nl/compound_acceptor/no_s.txt", 
                              COMPOUND_NEEDS_S_FILE = "nl/compound_acceptor/needs_s.txt",
                              COMPOUND_DIRECTIONS_FILE = "nl/compound_acceptor/directions.txt",
                              COMPOUND_ALWAYS_NEEDS_S_FILE = "nl/compound_acceptor/always_needs_s.txt",
                              COMPOUND_ALWAYS_NEEDS_HYPHEN_FILE = "nl/compound_acceptor/always_needs_hyphen.txt",
                              COMPOUND_PART1_EXCEPTIONS_FILE = "nl/compound_acceptor/part1_exceptions.txt",
                              COMPOUND_PART2_EXCEPTIONS_FILE = "nl/compound_acceptor/part2_exceptions.txt",
                              COMPOUND_ACRONYM_EXCEPTIONS_FILE = "nl/compound_acceptor/acronym_exceptions.txt";

  // Make sure we don't allow compound words where part 1 ends with a specific vowel and part2 starts with one, for words like "politieeenheid".
  private final Set<String> collidingVowels = ImmutableSet.of(
    "aa", "ae", "ai", "au", "ee", "ée", "ei", "éi", "eu", "éu", "ie", "ii", "ij", "oe", "oi", "oo", "ou", "ui", "uu"
  );

  private static final MorfologikDutchSpellerRule speller;
  static {
    try {
      speller = new MorfologikDutchSpellerRule(JLanguageTool.getMessageBundle(), Languages.getLanguageForShortCode("nl"), null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final DutchTagger dutchTagger = DutchTagger.INSTANCE;

  public CompoundAcceptor() {
    noS.addAll(wordListLoader.loadWords(COMPOUND_NO_S_FILE));
    needsS.addAll(wordListLoader.loadWords(COMPOUND_NEEDS_S_FILE));
    geographicalDirections.addAll(wordListLoader.loadWords(COMPOUND_DIRECTIONS_FILE));
    alwaysNeedsS.addAll(wordListLoader.loadWords(COMPOUND_ALWAYS_NEEDS_S_FILE));
    alwaysNeedsHyphen.addAll(wordListLoader.loadWords(COMPOUND_ALWAYS_NEEDS_HYPHEN_FILE));
    part1Exceptions.addAll(wordListLoader.loadWords(COMPOUND_PART1_EXCEPTIONS_FILE));
    part2Exceptions.addAll(wordListLoader.loadWords(COMPOUND_PART2_EXCEPTIONS_FILE));
    acronymExceptions.addAll(wordListLoader.loadWords(COMPOUND_ACRONYM_EXCEPTIONS_FILE));
  }

  boolean acceptCompound(String word) {
    if (word.length() > MAX_WORD_SIZE) {  // prevent long runtime
      return false;
    }
    for (int i = 3; i < word.length() - 3; i++) {
      String part1 = word.substring(0, i);
      String part2 = word.substring(i);
      if (!part1.equals(part2) && acceptCompound(part1, part2)) {
        //System.out.println(part1+part2 + " -> accepted");
        return true;
      }
    }
    return false;
  }

  public List<String> getParts(String word) {
    if (word.length() > MAX_WORD_SIZE) {  // prevent long runtime
      return Collections.emptyList();
    }
    for (int i = 3; i < word.length() - 3; i++) {
      String part1 = word.substring(0, i);
      String part2 = word.substring(i);
      if (!part1.equals(part2) && acceptCompound(part1, part2)) {
        return Arrays.asList(part1, part2);
      }
    }
    return Collections.emptyList();
  }

  boolean acceptCompound(String part1, String part2) {
    try {
      String part1lc = part1.toLowerCase();
      // reject if it's in the exceptions list or if a wildcard is the entirety of part1
      if (part1.endsWith("s") && !part1Exceptions.contains(part1.substring(0, part1.length() -1)) && !alwaysNeedsS.contains(part1) && !noS.contains(part1) && !part1.contains("-")) {
        for (String suffix : alwaysNeedsS) {
          if (part1lc.endsWith(suffix)) {
            return isNoun(part2) && isExistingWord(part1lc.substring(0, part1lc.length() - 1)) && spellingOk(part2);
          }
        }
        return needsS.contains(part1lc) && isNoun(part2) && spellingOk(part1.substring(0, part1.length() - 1)) && spellingOk(part2);
      } else if (geographicalDirections.contains(part1)){
        return isGeographicalCompound(part2); // directions
      } else if (part1.endsWith("-")) { // abbreviations
        return (acronymOk(part1) || alwaysNeedsHyphen.contains(part1lc)) && spellingOk(part2);
      } else if (part2.startsWith("-")) { // vowel collision
        part2 = part2.substring(1);
        return noS.contains(part1lc) && isNoun(part2) && spellingOk(part1) && spellingOk(part2) && hasCollidingVowels(part1, part2);
      } else {
        return (noS.contains(part1lc) || part1Exceptions.contains(part1lc)) && isNoun(part2) && spellingOk(part1) && !hasCollidingVowels(part1, part2);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isNoun(String word) throws IOException {
    return dutchTagger.getPostags(word).stream().anyMatch(k -> {
      assert k.getPOSTag() != null;
      return k.getPOSTag().startsWith("ZNW") && !part2Exceptions.contains(word);
    });
  }

  private boolean isExistingWord(String word) throws IOException {
    return dutchTagger.getPostags(word).stream().anyMatch(k -> k.getPOSTag() != null);
  }

  private boolean isGeographicalCompound(String word) throws IOException {
    return dutchTagger.getPostags(word).stream().anyMatch(k -> {
      assert k.getPOSTag() != null;
      return k.getPOSTag().startsWith("ENM:LOC");
    });
  }

  private boolean hasCollidingVowels(String part1, String part2) {
    char char1 = part1.charAt(part1.length() - 1);
    char char2 = part2.charAt(0);
    String vowels = String.valueOf(char1) + char2;
    return collidingVowels.contains(vowels.toLowerCase());
  }

  private boolean acronymOk(String nonCompound) {
    // for compound words like IRA-akkoord, MIDI-bestanden, WK-finalisten
    if ( acronymPattern.matcher(nonCompound).matches() ){
      return acronymExceptions.stream().noneMatch(exception -> exception.toUpperCase().equals(nonCompound.substring(0, nonCompound.length() -1)));
    } else if ( specialAcronymPattern.matcher(nonCompound).matches() ) {
      // special case acronyms that are accepted only with specific casing
      return acronymExceptions.contains(nonCompound.substring(0, nonCompound.length() -1));
    } else {
      return false;
    }
  }

  private boolean spellingOk(String nonCompound) throws IOException {
    if (!normalCasePattern.matcher(nonCompound).matches()) {
      return false;   // e.g. kinderenHet -> split as kinder,enHet
    }
    AnalyzedSentence as = new AnalyzedSentence(new AnalyzedTokenReadings[] {
      new AnalyzedTokenReadings(new AnalyzedToken(nonCompound.toLowerCase(), "FAKE_POS", "fakeLemma"))
    });
    RuleMatch[] matches = speller.match(as);
    return matches.length == 0;
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + CompoundAcceptor.class.getName() + " <file>");
      System.exit(1);
    }
    CompoundAcceptor acceptor = new CompoundAcceptor();
    List<String> words = Files.readAllLines(Paths.get(args[0]));
    for (String word : words) {
      boolean accepted = acceptor.acceptCompound(word);
      System.out.println(accepted + " " + word);
    }
  }

}
