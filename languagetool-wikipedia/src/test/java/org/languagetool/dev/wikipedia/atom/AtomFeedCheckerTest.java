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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.language.English;
import org.languagetool.language.German;
import org.languagetool.tools.Tools;

public class AtomFeedCheckerTest {

  private static final String DB_URL = "jdbc:derby:atomFeedChecksDB;create=true";

  @Ignore("Interactive use only - for testing the 'recent changes' XML we get from the API")
  @Test
  public void testCheckManually() throws IOException {
    AtomFeedChecker atomFeedChecker = new AtomFeedChecker(new English());
    CheckResult checkResult = atomFeedChecker.checkChanges(new FileInputStream("/home/dnaber/wiki.xml"));
    List<ChangeAnalysis> changeAnalysisList = checkResult.getCheckResults();
    for (ChangeAnalysis changeAnalysis : changeAnalysisList) {
      System.out.println(changeAnalysis.getTitle());
      for (WikipediaRuleMatch match : changeAnalysis.getRemovedMatches()) {
        System.out.println(" [-] " + match);
      }
      for (WikipediaRuleMatch match : changeAnalysis.getAddedMatches()) {
        System.out.println(" [+] " + match);
      }
      System.out.println("----------------------");
    }
  }

  @Test
  public void testCheck() throws IOException {
    AtomFeedChecker atomFeedChecker = new AtomFeedChecker(new German());
    CheckResult checkResult = atomFeedChecker.checkChanges(getStream());
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

    CheckResult checkResult2 = atomFeedChecker.checkChanges(getStream());
    List<ChangeAnalysis> changeAnalysis2 = checkResult2.getCheckResults();
    assertThat(changeAnalysis2.size(), is(3));   // not skipped because no database is used
  }

  @Test
  public void testCheckToDatabase() throws IOException, SQLException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
    initDatabase();
    DatabaseConfig databaseConfig = new DatabaseConfig(DB_URL, "user", "pass");
    AtomFeedChecker atomFeedChecker1 = new AtomFeedChecker(new German(), databaseConfig);
    CheckResult checkResult = atomFeedChecker1.runCheck(getStream());
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

    Date latestCheckDate1 = atomFeedChecker1.getDatabase().getCheckDates().get("de");
    assertThat(dateFormat.format(latestCheckDate1), is("2013-12-03 10:48"));

    AtomFeedChecker atomFeedChecker2 = new AtomFeedChecker(new German(), databaseConfig);
    CheckResult checkResult2 = atomFeedChecker2.runCheck(getStream());
    List<ChangeAnalysis> changeAnalysis2 = checkResult2.getCheckResults();
    // All articles could be skipped as they have been checked in the previous run:
    assertThat(changeAnalysis2.size(), is(0));
    assertThat(atomFeedChecker2.getDatabase().getCheckDates().size(), is(1));
    Date latestCheckDate2 = atomFeedChecker2.getDatabase().getCheckDates().get("de");
    assertThat(dateFormat.format(latestCheckDate2), is("2013-12-03 10:48"));
  }

  private void initDatabase() throws SQLException {
    MatchDatabase database = new MatchDatabase(DB_URL, "user", "pass");
    database.dropTables();
    database.createTables();
  }

  private InputStream getStream() throws IOException {
    return Tools.getStream("/org/languagetool/dev/wikipedia/atom/feed1.xml");
  }
  
}
