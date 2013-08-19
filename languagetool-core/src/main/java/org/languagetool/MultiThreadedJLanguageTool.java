/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A variant of {@link JLanguageTool} that uses as many threads as
 * the system has processors. Use this if you want text checking to
 * be fast and do not care about the high load that this might cause.
 */
public class MultiThreadedJLanguageTool extends JLanguageTool {

  public MultiThreadedJLanguageTool(Language language) throws IOException {
    super(language);
  }

  public MultiThreadedJLanguageTool(Language language, Language motherTongue) throws IOException {
    super(language, motherTongue);
  }

  /**
   * @return the number of processors this system has
   */
  protected int getThreadPoolSize() {
    return Runtime.getRuntime().availableProcessors();
  }

  /**
   * @return a fixed size executor with the given number of threads
   */
  protected ExecutorService getExecutorService(int threads) {
    return Executors.newFixedThreadPool(threads);
  }

}
