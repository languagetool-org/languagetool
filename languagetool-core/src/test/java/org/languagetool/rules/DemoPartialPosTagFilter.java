/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DemoPartialPosTagFilter extends PartialPosTagFilter {

  @Override
  protected List<AnalyzedTokenReadings> tag(String token) {
    if ("accurate".equals(token)) {
      AnalyzedToken resultToken = new AnalyzedToken(token, "JJ", "fake");
      List<AnalyzedToken> resultTokens = Collections.singletonList(resultToken);
      List<AnalyzedTokenReadings> result = new ArrayList<>();
      result.add(new AnalyzedTokenReadings(resultTokens, 0));
      return result;
    }
    return null;
  }

}
