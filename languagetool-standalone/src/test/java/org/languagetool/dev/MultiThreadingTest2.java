/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.fail;


/**
 * Test for a single language with multiple threads.
 */
public class MultiThreadingTest2 {

  private static final Language LANG = new GermanyGerman();
  private static final int THREADS = 5;
  private static final int RUNS = 50;
  private static final List<String> sentences = Arrays.asList(
    "oder nutzen Sie diesen Text als Beispiel für ein Paar Fehler , die LanguageTool erkennen kann.",
    "Ihm wurde Angst und bange, als er davon hörte. ( Eine Rechtschreibprüfun findet findet übrigens auch statt.",
    "Eine Rechtschreibprüfun findet findet übrigens auch statt.",
    "Eine Rechtschreibprüfung findet findet übrigens auch statt.",
    "Eine rechtschreibprüfung findet übrigends auch statt.",
    "Eine rechtschreibprüfung findet übrigens auch auch statt.",
    "Ein ökonomischer Gottesdienst."
  );

  private final Random rnd = new Random(1234);
  private final Map<String,String> expectedResults = new HashMap<>();  // input sentence to result mapping

  @Test
  @Ignore("for interactive use only")
  public void test() throws Exception {
    initExpectedResults();
    ExecutorService executor = Executors.newFixedThreadPool(THREADS);
    for (int i = 0; i < RUNS; i++) {
      System.out.println("Run #" + i);
      Collections.shuffle(sentences, rnd);
      List<Future> futures = new ArrayList<>();
      for (String sentence : sentences) {
        futures.add(executor.submit(new Handler(LANG, sentence)));
      }
      for (Future future : futures) {
        future.get();  // wait for all results or exception
      }
    }
  }

  private void initExpectedResults() throws IOException {
    JLanguageTool lt = new JLanguageTool(LANG);
    for (String sentence : sentences) {
      List<RuleMatch> matches = lt.check(sentence);
      expectedResults.put(sentence, matches.toString());
    }
  }

  class Handler implements Runnable {

    private final Language lang;
    private final String sentence;

    Handler(Language lang, String sentence) {
      this.lang = lang;
      this.sentence = sentence;
    }

    @Override
    public void run() {
      try {
        JLanguageTool lt = new JLanguageTool(lang);
        List<RuleMatch> matches = lt.check(sentence);
        //System.out.println("=>" + matches);
        String expected = expectedResults.get(sentence);
        String real = matches.toString();
        if (!expectedResults.get(sentence).equals(real)) {
          fail("Got '" + real + "', expected '" + expected + "' for input: " + sentence);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
