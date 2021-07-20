/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.ner;

import opennlp.tools.util.Span;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class EnglishNamedEntityDetectorTest {
  
  @Test
  public void testFindCharPos() {
    EnglishNamedEntityDetector ner = new EnglishNamedEntityDetector();
    String s = "Foo bar (1432–81) blah";
    String[] tokens = ner.tokenize(s);
    //System.out.println("-> " + Arrays.toString(tokens));
    assertThat(ner.findCharPos(0, tokens, s), is(0));
    assertThat(ner.findCharPos(1, tokens, s), is(4));   // "bar"
    assertThat(ner.findCharPos(5, tokens, s), is(18));  // "blah"
  }

  @Test
  public void testNamedEntities() {
    EnglishNamedEntityDetector ner = new EnglishNamedEntityDetector();
    //ner.findCharPos(0, new String[]{"This", "is", "an", "test", "."}, "This is an test.");
    //String[] tokens = ner.tokenize("This is a test with Peter Smith.");
    //String[] tokens = ner.tokenize("This is a test with Elmar Reimann.");  // close to "Elmer"
    //String[] tokens = ner.tokenize("It was the pseudonym used by American author Pul Myron Anthony Linebarger");  // close to "Paul"
    //String s = "He was the son of Mehmed II (1432–81) and Valide Sultan Gülbahar Hatun, who died in 1492.";
    String s = "Dear Tarsem Singh, how are you?";  // 'Tarsem' not close to anything
    List<Span> namedEntities = ner.findNamedEntities(s);
    /*System.out.println(namedEntities);
    for (Span span : namedEntities) {
      System.out.println("NE: " + s.substring(span.getStart(), span.getEnd()));
    }*/
    assertThat(s.substring(namedEntities.get(0).getStart(), namedEntities.get(0).getEnd()), is("Tarsem Singh"));
  }

  @Test
  @Ignore("interactive use only")
  public void test() throws IOException {
    EnglishNamedEntityDetector ner = new EnglishNamedEntityDetector();
    //String[] tokens = ner.tokenize("This is a test with Peter Smith.");
    //String[] tokens = ner.tokenize("This is a test with Elmar Reimann.");  // close to "Elmer"
    //String[] tokens = ner.tokenize("It was the pseudonym used by American author Pul Myron Anthony Linebarger");  // close to "Paul"
    String[] tokens = ner.tokenize("He was born as the son of Mehmed II (1432–81) and Valide Sultan Gülbahar Hatun, who died in 1492.");  // "Gülbahar" not close to anything
    Span[] ners = ner.findNamedEntities(tokens);
    System.out.println(Arrays.toString(ners));
    List<String> tokenList = Arrays.asList(tokens);
    for (Span span : ners) {
      System.out.println(tokenList.subList(span.getStart(), span.getEnd()) + " -> " + span.getType());
    }
  }

  @Test
  @Ignore("interactive use only")
  public void testFile() throws IOException {
    EnglishNamedEntityDetector ner = new EnglishNamedEntityDetector();
    //List<String> lines = Files.readAllLines(Paths.get("/home/dnaber/data/corpus/tatoeba/20191014/sentences-en-20191014-top1000.txt"));
    List<String> lines = Files.readAllLines(Paths.get("/home/dnaber/data/corpus/wikipedia/en/sentences1000.txt"));
    for (String line : lines) {
      long t1 = System.currentTimeMillis();
      String[] tokens = ner.tokenize(line);
      Span[] ners = ner.findNamedEntities(tokens);
      long t2 = System.currentTimeMillis();
      long runtime = t2- t1;
      System.out.println("-- " + line + " (" + runtime + "ms)");
      //System.out.println(Arrays.toString(ners));
      List<String> tokenList = Arrays.asList(tokens);
      if (ners.length > 0) {
        for (Span span : ners) {
          System.out.println("    " + tokenList.subList(span.getStart(), span.getEnd()) + " -> " + span.getType());
        }
      }
    }
  }

  @Test
  @Ignore("interactive use only")
  public void testSpeller() throws IOException {
    EnglishNamedEntityDetector ner = new EnglishNamedEntityDetector();
    JLanguageTool lt = getLt();
    //String file = "/home/dnaber/data/corpus/tatoeba/20191014/sentences-en-20191014-top1000.txt";
    String file = "/home/dnaber/data/corpus/wikipedia/en/sentences1000-speller.txt";
    List<String> lines = Files.readAllLines(Paths.get(file));
    int ok = 0;
    int notOk = 0;
    int spellMatches = 0;
    int spellMatchesPrevented = 0;
    for (String line : lines) {
      List<RuleMatch> matches = lt.check(line);
      if (matches.size() > 0) {
        spellMatches += matches.size();
        //System.out.println("   " + matches.size() + " matches");
        notOk++;
        String[] tokens = ner.tokenize(line);
        Span[] ners = ner.findNamedEntities(tokens);
        //System.out.println("-- " + line + " " + Arrays.toString(ners));
        if (ners.length > 0) {
          List<String> tokenList = Arrays.asList(tokens);
          for (Span span : ners) {
            String markedBySpan = String.join(" ", tokenList.subList(span.getStart(), span.getEnd()));
            if (isSpellerMatch(line, markedBySpan, matches)) {
              if (StringTools.startsWithUppercase(markedBySpan)) {
                System.out.println("   marked by speller: " + markedBySpan);
                // TODO: only prevent if not close to existing name
                spellMatchesPrevented++;
              }
            } else {
              //System.out.println("   not marked by speller: " + markedBySpan);
            }
          }
        }
      } else {
        //System.out.println("-- " + line);
        //System.out.println("   OK");
        ok++;
      }
    }
    System.out.println("no matches: " + ok);
    System.out.println("with matches: " + notOk);
    System.out.println("speller matches: " + spellMatches);
    System.out.println("speller matches prevented: " + spellMatchesPrevented);
    // Wikipedia:
    // no matches: 639
    // with matches: 361
    // speller matches: 579
    // speller matches prevented: 55
    //
    // Tatoeba:
    // no matches: 982
    // with matches: 18
    // speller matches: 18
    // speller matches prevented: 0
  }

  @NotNull
  private JLanguageTool getLt() {
    JLanguageTool lt = new JLanguageTool(new AmericanEnglish());
    for (Rule rule : lt.getAllActiveRules()) {
      if (!rule.isDictionaryBasedSpellingRule()) {
        lt.disableRule(rule.getId());
      }
    }
    return lt;
  }

  private boolean isSpellerMatch(String line, String markedBySpan, List<RuleMatch> matches) {
    int markedBySpanStart = line.indexOf(markedBySpan);
    int markedBySpanEnd = markedBySpanStart + markedBySpan.length();
    for (RuleMatch match : matches) {
      if (markedBySpanStart <= match.getFromPos() && markedBySpanEnd >= match.getToPos()) {
        return true;
      }
    }
    return false;
  }

}
