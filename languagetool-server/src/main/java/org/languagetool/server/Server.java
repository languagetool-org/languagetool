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

import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.tools.LtThreadPoolFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import static org.languagetool.server.HTTPServerConfig.DEFAULT_PORT;

/**
 * Super class for HTTP and HTTPS server.
 *
 * @since 2.0
 */
@Slf4j
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
    int ipFingerprintFactor = config.getIpFingerprintFactor(); // can be <= 0, means fingerprinting is disabled
    List<String> requestLimitWhitelistUsers = config.getRequestLimitWhitelistUsers();
    int requestLimitWhitelistLimit = config.getRequestLimitWhitelistLimit();
    if ((requestLimit > 0 || requestLimitInBytes > 0) && requestLimitPeriodInSeconds > 0) {
      return new RequestLimiter(requestLimit, requestLimitInBytes, requestLimitPeriodInSeconds, ipFingerprintFactor,
        requestLimitWhitelistUsers, requestLimitWhitelistLimit);
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
    System.out.println("                 'maxTextLength' - maximum text length, longer texts will cause an error (optional)");
    System.out.println("                 'maxTextHardLength' - maximum text length, applies even to users with a special secret 'token' parameter (optional)");
    System.out.println("                 'secretTokenKey' - secret JWT token key, if set by user and valid, maxTextLength can be increased by the user (optional)");
    System.out.println("                 'maxCheckTimeMillis' - maximum time in milliseconds allowed per check (optional)");
    System.out.println("                 'maxErrorsPerWordRate' - checking will stop with error if there are more rules matches per word (optional)");
    System.out.println("                 'maxSpellingSuggestions' - only this many spelling errors will have suggestions for performance reasons (optional,\n" +
                       "                                            affects Hunspell-based languages only)");
    System.out.println("                 'maxCheckThreads' - maximum number of threads working in parallel (optional)");
    System.out.println("                 'cacheSize' - size of internal cache in number of sentences (optional, default: 0)");
    System.out.println("                 'cacheTTLSeconds' - how many seconds sentences are kept in cache (optional, default: 300 if 'cacheSize' is set)");
    System.out.println("                 'requestLimit' - maximum number of requests per requestLimitPeriodInSeconds (optional)");
    System.out.println("                 'requestLimitInBytes' - maximum aggregated size of requests per requestLimitPeriodInSeconds (optional)");
    System.out.println("                 'timeoutRequestLimit' - maximum number of timeout request (optional)");
    System.out.println("                 'requestLimitPeriodInSeconds' - time period to which requestLimit and timeoutRequestLimit applies (optional)");
    System.out.println("                 'languageModel' - a directory with '1grams', '2grams', '3grams' sub directories which contain a Lucene index");
    System.out.println("                                   each with ngram occurrence counts; activates the confusion rule if supported (optional)");
    System.out.println("                 'word2vecModel' - a directory with word2vec data (optional), see");
    System.out.println("                  https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/CHANGES.md#word2vec");
    System.out.println("                 'fasttextModel' - a model file for better language detection (optional), see");
    System.out.println("                                   https://fasttext.cc/docs/en/language-identification.html");
    System.out.println("                 'fasttextBinary' - compiled fasttext executable for language detection (optional), see");
    System.out.println("                                    https://fasttext.cc/docs/en/support.html");
    System.out.println("                 'maxWorkQueueSize' - reject request if request queue gets larger than this (optional)");
    System.out.println("                 'rulesFile' - a file containing rules configuration, such as .langugagetool.cfg (optional)");
    System.out.println("                 'warmUp' - set to 'true' to warm up server at start, i.e. run a short check with all languages (optional)");
    System.out.println("                 'blockedReferrers' - a comma-separated list of HTTP referrers (and 'Origin' headers) that are blocked and will not be served (optional)");
    System.out.println("                 'premiumOnly' - activate only the premium rules (optional)");
    System.out.println("                 'disabledRuleIds' - a comma-separated list of rule ids that are turned off for this server (optional)");
    System.out.println("                 'pipelineCaching' - set to 'true' to enable caching of internal pipelines to improve performance");
    System.out.println("                 'maxPipelinePoolSize' - cache size if 'pipelineCaching' is set");
    System.out.println("                 'pipelineExpireTimeInSeconds' - time after which pipeline cache items expire");
    System.out.println("                 'pipelinePrewarming' - set to 'true' to fill pipeline cache on start (can slow down start a lot)");
    System.out.println("                 Spellcheck-only languages: You can add simple spellcheck-only support for languages that LT doesn't");
    System.out.println("                                            support by defining two optional properties:");
    System.out.println("                   'lang-xx' - set name of the language, use language code instead of 'xx', e.g. lang-tr=Turkish");
    System.out.println("                   'lang-xx-dictPath' - absolute path to the hunspell .dic file, use language code instead of 'xx', e.g.");
    System.out.println("                                        lang-tr-dictPath=/path/to/tr.dic. Note that the same directory also needs to");
    System.out.println("                                        contain a common_words.txt file with the most common 10,000 words (used for better language detection)");
  }

  protected static void printCommonOptions() {
    System.out.println("  --port, -p PRT   port to bind to, defaults to " + DEFAULT_PORT + " if not specified");
    System.out.println("  --public         allow this server process to be connected from anywhere; if not set,");
    System.out.println("                   it can only be connected from the computer it was started on");
    System.out.println("  --allow-origin [ORIGIN] set the Access-Control-Allow-Origin header in the HTTP response,");
    System.out.println("                         used for direct (non-proxy) JavaScript-based access from browsers.");
    System.out.println("                         Example: --allow-origin \"https://my-website.org\"");
    System.out.println("                         Don't set a parameter for `*`, i.e. access from all websites.");
    System.out.println("  --verbose, -v    in case of exceptions, log the input text (up to 500 characters)");
    System.out.println("  --languageModel  a directory with '1grams', '2grams', '3grams' sub directories (per language)");
    System.out.println("                         which contain a Lucene index (optional, overwrites 'languageModel'");
    System.out.println("                         parameter in properties files)");
    System.out.println("  --word2vecModel  a directory with word2vec data (optional), see");
    System.out.println("                   https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/CHANGES.md#word2vec");
    System.out.println("  --premiumAlways  activate the premium rules even when user has no username/password - useful for API servers");
  }

  protected static void checkForNonRootUser() {
    if ("root".equals(System.getProperty("user.name"))) {
      ServerTools.print("****************************************************************************************************");
      ServerTools.print("*** WARNING: this process is running as root - please do not run it as root for security reasons ***");
      ServerTools.print("****************************************************************************************************");
    }
  }
  
  protected ThreadPoolExecutor getExecutorService(HTTPServerConfig config) {
    int threadPoolSize = config.getMaxCheckThreads();
    ServerTools.print("Setting up thread pool with " + threadPoolSize + " threads");

    // reuse = false -> this should only be called once in production, needs to be false for tests
    return LtThreadPoolFactory.createFixedThreadPoolExecutor(LtThreadPoolFactory.SERVER_POOL,
      threadPoolSize, threadPoolSize, 0,0L, false,
      (thread, throwable) -> log.error("Thread: " + thread.getName() + " failed with: " + throwable.getMessage()), false);
  }
}
