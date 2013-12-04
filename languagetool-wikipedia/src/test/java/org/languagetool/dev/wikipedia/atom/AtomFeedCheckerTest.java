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

import org.junit.Test;
import org.languagetool.language.German;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AtomFeedCheckerTest {
  
  @Test
  public void testCheck() throws IOException {
    AtomFeedChecker atomFeedChecker = new AtomFeedChecker(new German());
    InputStream xmlStream = Tools.getStream("/org/languagetool/dev/wikipedia/atom/feed1.xml");
    CheckResult checkResult = atomFeedChecker.checkChanges(xmlStream, 0L);
    List<ChangeAnalysis> changeAnalysis = checkResult.getCheckResults();
    assertThat(changeAnalysis.size(), is(3));

    assertThat(changeAnalysis.get(0).getAddedMatches().size(), is(1));
    assertThat(changeAnalysis.get(0).getAddedMatches().get(0).getRule().getId(), is("DE_AGREEMENT"));
    assertTrue(changeAnalysis.get(0).getAddedMatches().get(0).getErrorContext().contains("Fehler: <err>der Haus</err>"));
    assertThat(changeAnalysis.get(0).getRemovedMatches().size(), is(0));

    assertThat(changeAnalysis.get(1).getAddedMatches().size(), is(0));
    assertThat(changeAnalysis.get(1).getRemovedMatches().size(), is(0));

    assertThat(changeAnalysis.get(2).getAddedMatches().size(), is(0));
    assertThat(changeAnalysis.get(2).getRemovedMatches().size(), is(0));
  }
  
}
