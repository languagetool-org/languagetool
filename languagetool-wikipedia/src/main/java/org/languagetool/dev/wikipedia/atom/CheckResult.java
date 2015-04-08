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
package org.languagetool.dev.wikipedia.atom;

import java.util.List;
import java.util.Objects;

/**
 * @since 2.4
 */
class CheckResult {

  private final List<ChangeAnalysis> checkResults;
  private final long latestDiffId;

  CheckResult(List<ChangeAnalysis> checkResults, long latestDiffId) {
    this.checkResults = Objects.requireNonNull(checkResults);
    if (latestDiffId < 0) {
      throw new IllegalArgumentException("latestDiffId must be >= 0: " + latestDiffId);
    }
    this.latestDiffId = latestDiffId;
  }

  List<ChangeAnalysis> getCheckResults() {
    return checkResults;
  }

  long getLatestDiffId() {
    return latestDiffId;
  }
}
