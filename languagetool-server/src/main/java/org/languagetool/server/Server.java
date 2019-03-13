/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.languagetool.server.HTTPServerConfig.DEFAULT_PORT;

/**
 * Super class for HTTP and HTTPS server.
 *
 * @since 2.0
 */
abstract class Server {

  protected abstract String getProtocol();

  protected static final Set<String> DEFAULT_ALLOWED_IPS = new HashSet<>(Arrays.asList(
            "0:0:0:0:0:0:0:1",     // Suse Linux IPv6 stuff
            "0:0:0:0:0:0:0:1%0",   // some(?) Mac OS X
            "127.0.0.1"
    ));

  protected int port;
  protected String host;
  protected HttpServer server;
  protected LanguageToolHttpHandler httpHandler;

  private boolean isRunning;

  /**
   * Start the server.
   */
  public void run() {
    String hostName = host != null ? host : "localhost";
    ServerTools.print("Starting LanguageTool " + JLanguageTool.VERSION +
            " (build date: " + JLanguageTool.BUILD_DATE + ", " + JLanguageTool.GIT_SHORT_ID + ") server on " + getProtocol() + "://" + hostName + ":" + port  + "...");
    server.start();
    isRunning = true;
    ServerTools.print("Server started");
  }

  /**
   * Stop the server. Once stopped, a server cannot be used again.
   */
  public void stop() {
    if (httpHandler != null) {
      httpHandler.shutdown();
    }
    if (server != null) {
      ServerTools.print("Stopping server...");
      server.stop(5);
      isRunning = false;
      ServerTools.print("Server stopped");
    }
  }

  /**
   * @return whether the server is running
   * @since 2.0
   */
  public boolean isRunning() {
    return isRunning;
  }

  @Nullable
  protected RequestLimiter getRequestLimiterOrNull(HTTPServerConfig config) {
    int requestLimit = config.getRequestLimit();
    int requestLimitInBytes = config.getRequestLimitInBytes();
    int requestLimitPeriodInSeconds = config.getRequestLimitPeriodInSeconds();
    int ipFingerprintFactor = config.getIpFingerprintFactor();
    if ((requestLimit > 0 || requestLimitInBytes > 0) && requestLimitPeriodInSeconds > 0 && ipFingerprintFactor >= 1) {
      return new RequestLimiter(requestLimit, requestLimitInBytes, requestLimitPeriodInSeconds, ipFingerprintFactor);
    }
    return null;
  }

  @Nullable
  protected ErrorRequestLimiter getErrorRequestLimiterOrNull(HTTPServerConfig config) {
    int requestLimit = config.getTimeoutRequestLimit();
    int requestLimitPeriodInSeconds = config.getRequestLimitPeriodInSeconds();
    if (requestLimit > 0 && requestLimitPeriodInSeconds > 0) {
      return new ErrorRequestLimiter(requestLimit, requestLimitPeriodInSeconds);
    }
    return null;
  }

  protected static boolean usageRequested(String[] args) {
    return args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"));
  }

  protected static void printCommonConfigFileOptions() {
    System.out.println("                 'mode' - 'LanguageTool' or 'AfterTheDeadline' (DEPRECATED) for emulation of After the Deadline output (optional)");
    System.out.println("                 'afterTheDeadlineLanguage' - language code like 'en' or 'en-GB' (required if mode is 'AfterTheDeadline') - DEPRECATED");
    System.out.println("                 'maxTextLength' - maximum text length, longer texts will cause an error (optional)");
    System.out.println("                 'maxTextHardLength' - maximum text length, applies even to users with a special secret 'token' parameter (optional)");
    System.out.println("                 'secretTokenKey' - secret JWT token key, if set by user and valid, maxTextLength can be increased by the user (optional)");
    System.out.println("                 'maxCheckTimeMillis' - maximum time in milliseconds allowed per check (optional)");
    System.out.println("                 'maxErrorsPerWordRate' - checking will stop with error if there are more rules matches per word (optional)");
    System.out.println("                 'maxSpellingSuggestions' - only this many spelling errors will have suggestions for performance reasons (optional,\n" +
                       "                                            affects Hunspell-based languages only)");
    System.out.println("                 'maxCheckThreads' - maximum number of threads working in parallel (optional)");
    System.out.println("                 'cacheSize' - size of internal cache in number of sentences (optional, default: 0)");
    System.out.println("                 'requestLimit' - maximum number of requests per requestLimitPeriodInSeconds (optional)");
    System.out.println("                 'requestLimitInBytes' - maximum aggregated size of requests per requestLimitPeriodInSeconds (optional)");
    System.out.println("                 'timeoutRequestLimit' - maximum number of timeout request (optional)");
    System.out.println("                 'requestLimitPeriodInSeconds' - time period to which requestLimit and timeoutRequestLimit applies (optional)");
    System.out.println("                 'languageModel' - a directory with '1grams', '2grams', '3grams' sub directories which contain a Lucene index");
    System.out.println("                  each with ngram occurrence counts; activates the confusion rule if supported (optional)");
    System.out.println("                 'word2vecModel' - a directory with word2vec data (optional), see");
    System.out.println("                  https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/CHANGES.md#word2vec");
    System.out.println("                 'fasttextModel' - a model file for better language detection (optional), see");
    System.out.println("                  https://fasttext.cc/docs/en/language-identification.html");
    System.out.println("                 'fasttextBinary' - compiled fasttext executable for language detection (optional), see");
    System.out.println("                  https://fasttext.cc/docs/en/support.html");
    System.out.println("                 'maxWorkQueueSize' - reject request if request queue gets larger than this (optional)");
    System.out.println("                 'rulesFile' - a file containing rules configuration, such as .langugagetool.cfg (optional)");
    System.out.println("                 'warmUp' - set to 'true' to warm up server at start, i.e. run a short check with all languages (optional)");
    System.out.println("                 'blockedReferrers' - a comma-separated list of HTTP referrers (and 'Origin' headers) that are blocked and will not be served (optional)");
  }

  protected static void printCommonOptions() {
    System.out.println("  --port, -p PRT   port to bind to, defaults to " + DEFAULT_PORT + " if not specified");
    System.out.println("  --public         allow this server process to be connected from anywhere; if not set,");
    System.out.println("                   it can only be connected from the computer it was started on");
    System.out.println("  --allow-origin   ORIGIN  set the Access-Control-Allow-Origin header in the HTTP response,");
    System.out.println("                         used for direct (non-proxy) JavaScript-based access from browsers;");
    System.out.println("                         example: --allow-origin \"*\"");
    System.out.println("  --verbose, -v    in case of exceptions, log the input text (up to 500 characters)");
    System.out.println("  --languageModel  a directory with '1grams', '2grams', '3grams' sub directories (per language)");
    System.out.println("                         which contain a Lucene index (optional, overwrites 'languageModel'");
    System.out.println("                         parameter in properties files)");
    System.out.println("  --word2vecModel  a directory with word2vec data (optional), see");
    System.out.println("                   https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/CHANGES.md#word2vec");
  }

  protected static void checkForNonRootUser() {
    if ("root".equals(System.getProperty("user.name"))) {
      ServerTools.print("****************************************************************************************************");
      ServerTools.print("*** WARNING: this process is running as root - please do not run it as root for security reasons ***");
      ServerTools.print("****************************************************************************************************");
    }
  }
  
  protected ThreadPoolExecutor getExecutorService(LinkedBlockingQueue<Runnable> workQueue, HTTPServerConfig config) {
    int threadPoolSize = config.getMaxCheckThreads();
    ServerTools.print("Setting up thread pool with " + threadPoolSize + " threads");
    return new StoppingThreadPoolExecutor(threadPoolSize, workQueue);
  }

  static class StoppingThreadPoolExecutor extends ThreadPoolExecutor {
  
    StoppingThreadPoolExecutor(int threadPoolSize, LinkedBlockingQueue<Runnable> workQueue) {
      super(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, workQueue,
            new ThreadFactoryBuilder().setNameFormat("lt-server-thread-%d").build());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
      super.afterExecute(r, t);
      if (t != null && t instanceof OutOfMemoryError) {
        // we prefer to stop instead of being in an unstable state:
        //noinspection CallToPrintStackTrace
        t.printStackTrace();
        System.exit(1);
      }
    }
  }

}
