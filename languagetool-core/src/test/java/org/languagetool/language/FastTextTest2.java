/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Fabian Richter
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
package org.languagetool.language;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.After;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import static org.junit.Assert.assertEquals;

/**
 * In some cases, it appears that the Fasttext integration can get into a state
 * where multiple texts are sent in one batch and the responses then wrongly associated
 * This is a (failed) attempt at reproducing that.
 * Removing buffering from the communication with fasttext is an attempt at fixing it.
 */
public class FastTextTest2 {
  
  private static final List<String> languages = Arrays.asList("en", "es", "de", "fr");
  private static final List<Entry<String, String>> TESTED_ENTRIES =
    Arrays.asList(
                  new SimpleImmutableEntry<>("en", "This is an English text."),
                  new SimpleImmutableEntry<>("de", "Dies ist ein deutscher Text.")
                  );
  private static final int THREAD_COUNT = 11;

  private FastText instance;

  @Before
  public void setUp() throws IOException {
    instance = new FastText(new File("lid.176.bin"),
                            new File("fasttext"));
  }

  @After
  public void tearDown() {
    instance.destroy();
    instance = null;
  }

  private Runnable checkLanguage(String text, String expectedLang) {
    return () -> {
      while (true) {
        try {
          Map<String, Double> detected = instance.runFasttext(text, languages);
          System.out.printf("Detected '%s' as %s%n", text, detected);
          Entry<String, Double> highest = detected.entrySet().stream().max(Entry.comparingByValue()).get();
          assertEquals("Expected language correctly detected", expectedLang, highest.getKey());
          Thread.sleep(1);
        } catch (IOException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  @Test
  @Ignore("unable to reproduce bug")
  public void testConcurrentUse() throws InterruptedException {
    List<Thread> threads = new ArrayList<>(THREAD_COUNT);
    for (int i = 0; i < THREAD_COUNT; i++) {
      Entry<String, String> entry = TESTED_ENTRIES.get(i % TESTED_ENTRIES.size());
      Thread t = new Thread(checkLanguage(entry.getValue(), entry.getKey()), "fasttext-test-" + i);
      threads.add(t);
      System.out.printf("Started thread %d%n", i);
      t.start();
    }
    for (Thread t : threads) {
      t.join();
    }
  }
}
