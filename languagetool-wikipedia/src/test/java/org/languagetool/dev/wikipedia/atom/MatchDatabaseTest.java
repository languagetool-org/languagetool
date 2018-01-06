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
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.RuleMatch;

import java.sql.SQLException;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class MatchDatabaseTest {
  
  @Test
  public void test() throws SQLException, ClassNotFoundException {
    Language language = Languages.getLanguageForShortCode("de");
    MatchDatabase database = new MatchDatabase("jdbc:derby:atomFeedChecksDB;create=true", "user", "pass");
    database.dropTables();
    database.createTables();
    assertThat(database.getLatestDate(language), is(new Date(0)));
    assertThat(database.list().size(), is(0));
    assertThat(database.getCheckDates().size(), is(0));
    FakeRule rule1 = new FakeRule(1);
    rule1.setCategory(new Category(new CategoryId("TEST_ID"), "My Category"));
    RuleMatch ruleMatch = new RuleMatch(rule1, null, 5, 10, "my message");
    AtomFeedItem feedItem1 = new AtomFeedItem("//id1?diff=123", "title", "summary1", new Date(10000));
    WikipediaRuleMatch wikiRuleMatch1 = new WikipediaRuleMatch(language, ruleMatch, "my context", feedItem1);
    database.add(wikiRuleMatch1);
    assertThat(database.list().size(), is(1));
    assertThat(database.list().get(0).getRuleId(), is("ID_1"));
    assertThat(database.list().get(0).getRuleDescription(), is("A fake rule"));
    assertThat(database.list().get(0).getRuleMessage(), is("my message"));
    assertThat(database.list().get(0).getTitle(), is("title"));
    assertThat(database.list().get(0).getErrorContext(), is("my context"));
    assertThat(database.list().get(0).getDiffId(), is(123L));
    assertThat(database.list().get(0).getFixDiffId(), is(0L));
    assertThat(database.list().get(0).getEditDate(), is(new Date(10000)));
    assertThat(database.getLatestDate(language), is(new Date(0)));
    assertNull(database.list().get(0).getRuleSubId());
    assertNull(database.list().get(0).getFixDate());
    assertThat(database.getCheckDates().size(), is(0));

    RuleMatch ruleMatch2 = new RuleMatch(new FakeRule(1), null, 9, 11, "my message");  // same ID, different character positions
    AtomFeedItem feedItem2 = new AtomFeedItem("//id2?diff=124", "title", "summary2", new Date(9000000000L));
    WikipediaRuleMatch wikiRuleMatch2 = new WikipediaRuleMatch(language, ruleMatch2, "my context", feedItem2);
    int affected = database.markedFixed(wikiRuleMatch2);
    assertThat(affected, is(1));
    assertThat(database.list().get(0).getFixDate(), is(new Date(9000000000L)));
    assertThat(database.list().get(0).getDiffId(), is(123L));
    assertThat(database.list().get(0).getFixDiffId(), is(124L));
    assertThat(database.getLatestDate(language), is(new Date(0)));
    assertThat(database.getCheckDates().size(), is(0));
  }
  
}
