/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Stefan Lotties
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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

public abstract class AbstractLanguageConcurrencyTest {

  protected abstract Language createLanguage();
  protected abstract String createSampleText();

  @Test
  public void testSpellCheckerFailure() throws Exception {
    final String txt = createSampleText();
    final Language language = createLanguage();

    final Object syncLock = new Object();
    int threadCount = Runtime.getRuntime().availableProcessors() * 10;

    List<Thread> threads = new ArrayList<>();
    synchronized (syncLock) {
      for (int i = 0; i < threadCount; i++) {
        Runnable r = new Runnable() {
          @Override
          public void run() {
            // TODO: can this be removed?
            synchronized (syncLock) {
              syncLock.notifyAll();
            }
            for (int i = 0; i < 100; i++) {
              try {
                JLanguageTool tool = new JLanguageTool(language);
                // TODO: why only test spell checking?:
                //tool.activateDefaultPatternRules();
                // TODO: also check false friend rules?
                Assert.assertNotNull(tool.check(txt));
              } catch (Exception e) {
                // TODO: this only prints to stderr but doesn't make the test fail
                throw new RuntimeException(e);
              }
            }
          }
        };
        Thread t = new Thread(r);
        t.start();
        threads.add(t);
      }
    }
    for (Thread t : threads) {
      t.join();
    }
  }

}
