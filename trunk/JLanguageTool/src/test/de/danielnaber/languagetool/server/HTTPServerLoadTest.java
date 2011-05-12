package de.danielnaber.languagetool.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Test HTTP server access from multiple threads.
 */
public class HTTPServerLoadTest extends HTTPServerTest {

  // we keep these numbers low so "ant test" stays fast - increase them for serious testing:
  private static final int REPEAT_COUNT = 2;
  private static final int THREAD_COUNT = 2;

  @Override
  public void testHTTPServer() throws Exception {
    final long startTime = System.currentTimeMillis();
    final HTTPServer server = new HTTPServer();
    try {
      server.run();
      final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
      final List<Future> futures = new ArrayList<Future>();
      for (int i = 0; i < THREAD_COUNT; i++) {
        final Future<?> future = executorService.submit(new TestRunnable());
        futures.add(future);
      }
      for (Future future : futures) {
        future.get();
      }
    } finally {
      server.stop();
      final long runtime = System.currentTimeMillis() - startTime;
      System.out.println("Running with " + THREAD_COUNT + " threads in " + runtime + "ms");
    }
  }
  
  @Override
  public void testAccessDenied() throws Exception {
    // no need to test this here, tested in super class
  }
  
  private class TestRunnable implements Runnable {
    @Override
    public void run() {
      try {
        for (int i = 0; i < REPEAT_COUNT; i++) {
          runTests();          
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
  
}
