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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

public abstract class AbstractLanguageConcurrencyTest {

  protected abstract Language createLanguage();
  protected abstract String createSampleText();

  volatile int failedTests;
  
  @Ignore("too slow to run every time")
  @Test
  public void testSpellCheckerFailure() throws Exception {
    String sampleText = createSampleText();
    Language language = createLanguage();
    int threadCount = Runtime.getRuntime().availableProcessors() * 10;
    int testRuns = 100;

    ReadWriteLock testWaitLock = new ReentrantReadWriteLock();
    Lock testWriteLock = testWaitLock.writeLock();
    testWriteLock.lock();
    
    failedTests = 0;
    
    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      Thread t = new Thread(new TestRunner(testWaitLock, language, testRuns, sampleText));
      t.start();
      threads.add(t);
    }
    
    // Release the lock and allow all TestRunner threads to do their work.
    testWriteLock.unlock();
    
    for (Thread t : threads) {
      t.join();
    }
    
    Assert.assertEquals(0, failedTests);
  }

  final class TestRunner implements Runnable {
    private final ReadWriteLock waitLock;
    private final Language language;
    private final int testRuns;
    private final String sampleText;
    TestRunner(ReadWriteLock waitLock, Language language, int testRuns, String sampleText) {
      this.waitLock = waitLock;
      this.language = language;
      this.testRuns = testRuns;
      this.sampleText = sampleText;
    }
    
    @Override
    public void run() {
      /* Request a read-lock to force this thread waiting until the main-thread releases the write-lock.
       * This ensures all TestRunner threads will be executed very concurrently and force threading issues to come up,
       * in case the tested code is not thread-safe.
       */
      Lock lock = waitLock.readLock();
      lock.lock();
      lock.unlock();

      for (int i = 0; i < this.testRuns; i++) {
        try {
          JLanguageTool tool = new JLanguageTool(this.language);
          Assert.assertNotNull(tool.check(this.sampleText));
        } catch (Exception e) {          
          failedTests += 1;
          
          // Force a log message and the debugger to pause.
          throw new RuntimeException(e);
        }
      }
    }
  }
}
