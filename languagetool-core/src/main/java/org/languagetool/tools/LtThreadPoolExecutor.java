/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2020 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.tools;

import io.prometheus.client.Gauge;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * ThreadPoolExecutor with some stopping logic for OOM and metrics tracking
 */
@Slf4j
class LtThreadPoolExecutor extends ThreadPoolExecutor {

  private static final Gauge maxQueueSize = Gauge.build("languagetool_threadpool_max_queue_size", "Queue capacity by threadpool")
    .labelNames("pool").register();
  private static final Gauge queueSize = Gauge.build("languagetool_threadpool_queue_size", "Queue size by threadpool")
    .labelNames("pool").register();
  private static final Gauge largestPoolSize = Gauge.build("languagetool_threadpool_largest_queue_size", "The largest number of threads that have ever simultaneously been in the pool")
    .labelNames("pool").register();

  @Getter
  private final String name;

  LtThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, @NotNull ThreadFactory threadFactory, @NotNull RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    this.name = name;
    maxQueueSize.labels(name).set(workQueue.remainingCapacity());
  }

  {
    Timer timer = new Timer("LtThreadPoolExecutorMonitor", true);
    TimerTask timedAction = new TimerTask() {
      @Override
      public void run() {
        queueSize.labels(name).set(getQueue().size());
        largestPoolSize.labels(name).set(getLargestPoolSize());
        log.trace("{} queueSize: {}", name, queueSize.labels(name).get());
        log.trace("{} largestPoolSize: {}", name, largestPoolSize.labels(name).get());
      }
    };
    timer.scheduleAtFixedRate(timedAction, 0, 1000);
  }

  @Override
  public void execute(@NotNull Runnable command) {
    super.execute(command);
  }

  @Override
  public boolean remove(Runnable task) {
    return super.remove(task);
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    super.afterExecute(r, t);

    // inherited from removed StoppingThreadPoolExecutor in org.languagetool.server.Server
    if (t != null && t instanceof OutOfMemoryError) {
      // we prefer to stop instead of being in an unstable state:
      //noinspection CallToPrintStackTrace
      t.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  protected void beforeExecute(Thread t, Runnable r) {
    super.beforeExecute(t, r);
  }
}
