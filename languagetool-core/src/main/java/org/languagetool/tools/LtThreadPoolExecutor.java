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
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.*;

/**
 * ThreadPoolExecutor with some stopping logic for OOM and metrics tracking
 *
 */
class LtThreadPoolExecutor extends ThreadPoolExecutor {

  private static final Gauge activeThreads = Gauge.build("languagetool_threadpool_active_threads", "Running threads by threadpool")
      .labelNames("pool").register();
  private static final Gauge queueSize = Gauge.build("languagetool_threadpool_queue_size", "Queue size by threadpool").labelNames("pool").register();
  private static final Gauge maxQueueSize = Gauge.build("languagetool_threadpool_max_queue_size", "Queue capacity by threadpool").labelNames("pool").register();

  @Getter
  private final String name;
  private final Queue<Runnable> queue;

  LtThreadPoolExecutor(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, @NotNull ThreadFactory threadFactory, @NotNull RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    this.name = name;
    this.queue = getQueue();
    maxQueueSize.labels(name).set(workQueue.remainingCapacity());
  }

  @Override
  public void execute(@NotNull Runnable command) {
    super.execute(command);
    queueSize.labels(name).set(queue.size());
  }

  @Override
  public boolean remove(Runnable task) {
    boolean status = super.remove(task);
    queueSize.labels(name).set(queue.size());
    return status;
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    super.afterExecute(r, t);
    activeThreads.labels(name).dec();
    queueSize.labels(name).set(queue.size());
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
    activeThreads.labels(name).inc();
    queueSize.labels(name).set(queue.size());
  }
}
