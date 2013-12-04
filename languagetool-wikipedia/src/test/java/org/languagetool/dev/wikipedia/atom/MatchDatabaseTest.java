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
import org.languagetool.rules.RuleMatch;

import java.sql.SQLException;
import java.util.Date;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MatchDatabaseTest {
  
  @Test
  public void test() throws SQLException, ClassNotFoundException {
    MatchDatabase database = new MatchDatabase("jdbc:derby:languageToolAtomChecks;create=true", "user", "pass");
    //database.drop();  // comment in if database structure has been changed
    database.createTable();
    database.clear();
    assertThat(database.list().size(), is(0));
    RuleMatch ruleMatch = new RuleMatch(new FakeRule(1), 5, 10, "my message");
    WikipediaRuleMatch wikiRuleMatch1 = new WikipediaRuleMatch(ruleMatch, "my context", "my article title", new Date(10000));
    database.add(wikiRuleMatch1);
    assertThat(database.list().size(), is(1));
    assertNull(database.list().get(0).getFixDate());

    RuleMatch ruleMatch2 = new RuleMatch(new FakeRule(1), 9, 11, "my message");  // same ID, different character positions
    WikipediaRuleMatch wikiRuleMatch2 = new WikipediaRuleMatch(ruleMatch2, "my context", "my article title", new Date(9000000000L));
    int affected = database.markedFixed(wikiRuleMatch2);
    assertThat(affected, is(1));
    assertThat(database.list().get(0).getFixDate(), is(new Date(9000000000L)));
  }
  
}
