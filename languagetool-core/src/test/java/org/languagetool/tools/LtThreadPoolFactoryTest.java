/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2021 Stefan Viol (https://stevio.de)
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

package org.languagetool.tools;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

public class LtThreadPoolFactoryTest {
  
  @Test
  public void cachedThreadPoolTest() {
    ThreadPoolExecutor myThreadPool = LtThreadPoolFactory.createFixedThreadPoolExecutor(
      "Test-Pool-cached",
      10,
      20,
      false,
      (thread, throwable) -> {
        System.out.println(throwable.getClass());
      },
      true);
    assertEquals(myThreadPool, LtThreadPoolFactory.getFixedThreadPoolExecutor("Test-Pool-cached").get());
  }

  @Test
  public void notcachedThreadPoolTest() {
    ThreadPoolExecutor myThreadPool = LtThreadPoolFactory.createFixedThreadPoolExecutor(
      "Test-Pool-notCached",
      10,
      20,
      false,
      (thread, throwable) -> {
        System.out.println(throwable.getClass());
      },
      false);
    assertEquals(LtThreadPoolFactory.defaultPool, LtThreadPoolFactory.getFixedThreadPoolExecutor("Test-Pool-notCached").get());
  }

  @Test
  @Ignore //Could fail if CI is to slow and will slow down the CI build; test local
  public void stressedQueueTest() {
    ThreadPoolExecutor myThreadPool = LtThreadPoolFactory.createFixedThreadPoolExecutor(
      "Test-Pool-stressed",
      5,
      10,
      false,
      (thread, throwable) -> {
        System.out.println(throwable.getClass());
      },
      true);

    for (int i = 0; i < 15; i++) {
      myThreadPool.submit(() -> {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });
    }
    assertThrows(Exception.class, () -> myThreadPool.submit(() -> {
      System.out.println("Should fail.");
    }));
    for (int i = 0; i < 100; i++) {
      await().until(() -> myThreadPool.getQueue().size() < 9);
      myThreadPool.submit(() -> {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });
    }
    long startTime = System.currentTimeMillis();
    await().forever().until(() -> myThreadPool.getActiveCount() == 0);
    long endTime = System.currentTimeMillis();
    //should be something between 0.1s and 0.2s
    assertThat(endTime - startTime, allOf(greaterThan(100l), lessThan(120l)));

  }

  @Test
  @Ignore //Could fail if CI is to slow and will slow down the CI build; test local
  public void normalUsageThreadPoolTest() {
    ThreadPoolExecutor myThreadPool = LtThreadPoolFactory.createFixedThreadPoolExecutor(
      "Test-Pool-snt",
      10,
      20,
      false,
      (thread, throwable) -> {
        System.out.println(throwable.getClass());
      },
      true);
    Optional<ThreadPoolExecutor> fixedThreadPoolExecutor = LtThreadPoolFactory.getFixedThreadPoolExecutor("Test-Pool-snt");
    assertTrue(fixedThreadPoolExecutor.isPresent());
    ThreadPoolExecutor sameAsMyThreadPool = fixedThreadPoolExecutor.get();
    assertEquals(sameAsMyThreadPool, myThreadPool);
    for (int i = 0; i < 30; i++) {
      sameAsMyThreadPool.submit(() -> {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException ex) {
          System.out.println(ex.getMessage());
        }
      });
    }
    assertEquals(10, myThreadPool.getActiveCount());
    assertFalse(myThreadPool.getQueue().isEmpty());
    long startTime = System.currentTimeMillis();
    await().until(() -> myThreadPool.getActiveCount() == 0);
    long endTime = System.currentTimeMillis();
    //should be something between 9s and 10s
    assertThat(endTime - startTime, allOf(greaterThan(6000l), lessThan(7000l)));
    assertEquals(0, myThreadPool.getActiveCount());
    assertTrue(myThreadPool.getQueue().isEmpty());
  }

  @Test
  @Ignore //Could fail if CI is to slow and will slow down the CI build; test local
  public void normalMultiUsageThreadPoolTest() {
    ThreadPoolExecutor myThreadPool = LtThreadPoolFactory.createFixedThreadPoolExecutor(
      "Test-Pool-mnp",
      10,
      20,
      false,
      (thread, throwable) -> {
        System.out.println(throwable.getClass());
      },
      true);
    for (int i = 0; i < 30; i++) {
      new Thread(() -> {
        Optional<ThreadPoolExecutor> fixedThreadPoolExecutor = LtThreadPoolFactory.getFixedThreadPoolExecutor("Test-Pool-mnp");
        assertTrue(fixedThreadPoolExecutor.isPresent());
        ThreadPoolExecutor multiThreadedAccessOnMyThreadPool = fixedThreadPoolExecutor.get();
        assertEquals(multiThreadedAccessOnMyThreadPool, myThreadPool);
        multiThreadedAccessOnMyThreadPool.submit(() -> {
          try {
            Thread.sleep(2000);
          } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
          }
        });
      }).start();
    }
    assertFalse(myThreadPool.getQueue().isEmpty());
    long startTime = System.currentTimeMillis();
    await().until(() -> myThreadPool.getActiveCount() == 0);
    long endTime = System.currentTimeMillis();
    //should be something between 9s and 10s
    assertThat(endTime - startTime, allOf(greaterThan(6000l), lessThan(7000l)));
    assertEquals(0, myThreadPool.getActiveCount());
    assertTrue(myThreadPool.getQueue().isEmpty());
  }

  @Test
  @Ignore //Could fail if CI is to slow and will slow down the CI build; test local
  public void overloadingUsageThreadPoolTest() {
    ThreadPoolExecutor myThreadPool = LtThreadPoolFactory.createFixedThreadPoolExecutor(
      "Test-Pool-osp",
      10,
      20,
      false,
      (thread, throwable) -> {
        System.out.println(throwable.getClass());
      },
      true);
    Optional<ThreadPoolExecutor> fixedThreadPoolExecutor = LtThreadPoolFactory.getFixedThreadPoolExecutor("Test-Pool-osp");
    assertTrue(fixedThreadPoolExecutor.isPresent());
    ThreadPoolExecutor sameAsMyThreadPool = fixedThreadPoolExecutor.get();
    assertEquals(sameAsMyThreadPool, myThreadPool);

    for (int i = 0; i < 31; i++) {
      if (i == 30) {
        assertThrows(RejectedExecutionException.class, () -> {
          sameAsMyThreadPool.submit(() -> {
          });
        });
        break;
      }
      sameAsMyThreadPool.submit(() -> {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException ex) {
          System.out.println(ex.getMessage());
        }
      });
    }
    assertFalse(myThreadPool.getQueue().isEmpty());
    long startTime = System.currentTimeMillis();
    await().until(() -> myThreadPool.getActiveCount() == 0);
    long endTime = System.currentTimeMillis();
    //should be something between 9s and 10s
    assertThat(endTime - startTime, allOf(greaterThan(6000l), lessThan(7000l)));
    assertEquals(0, myThreadPool.getActiveCount());
    assertTrue(myThreadPool.getQueue().isEmpty());
  }

  @Test
  @Ignore //Could fail if CI is to slow and will slow down the CI build; test local
  public void overloadedMultiUsageThreadPoolTest() {
    ThreadPoolExecutor myThreadPool = LtThreadPoolFactory.createFixedThreadPoolExecutor(
      "Test-Pool-omp",
      10,
      20,
      false,
      (thread, throwable) -> {
        System.out.println(throwable.getClass());
      },
      true);
    for (int i = 0; i < 31; i++) {
      int finalI = i;
      new Thread(() -> {
        Optional<ThreadPoolExecutor> fixedThreadPoolExecutor = LtThreadPoolFactory.getFixedThreadPoolExecutor("Test-Pool-omp");
        assertTrue(fixedThreadPoolExecutor.isPresent());
        ThreadPoolExecutor multiThreadedAccessOnMyThreadPool = fixedThreadPoolExecutor.get();
        assertEquals(multiThreadedAccessOnMyThreadPool, myThreadPool);
        if (finalI == 30) {
          assertThrows(RejectedExecutionException.class, () -> {
            multiThreadedAccessOnMyThreadPool.submit(() -> {
            });
          });
        } else {
          multiThreadedAccessOnMyThreadPool.submit(() -> {
            try {
              Thread.sleep(2000);
            } catch (InterruptedException ex) {
              System.out.println(ex.getMessage());
            }
          });
        }
      }).start();
    }
    assertFalse(myThreadPool.getQueue().isEmpty());
    long startTime = System.currentTimeMillis();
    await().until(() -> myThreadPool.getActiveCount() == 0);
    long endTime = System.currentTimeMillis();
    //should be something between 9s and 10s
    assertThat(endTime - startTime, allOf(greaterThan(6000l), lessThan(7000l)));
    assertEquals(0, myThreadPool.getActiveCount());
    assertTrue(myThreadPool.getQueue().isEmpty());
  }
}
