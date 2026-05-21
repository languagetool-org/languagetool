/* LanguageTool, a natural language style checker 
 * Copyright (C) 2025 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.patterns.FalseFriendRuleLoader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

import static org.languagetool.JLanguageTool.getDataBroker;

public class ValidateFalseFriendsSyntaxTest {

  @Test
  public void testFalseFriendsXML() throws IOException, ParserConfigurationException, SAXException {
    System.out.println("Validating false-friends.xml syntax...");
    InputStream is = JLanguageTool.getDataBroker().getAsStream(getDataBroker().getRulesDir() + "/false-friends.xml");
    Language langDE = Languages.getLanguageForShortCode("de");
    FalseFriendRuleLoader loader = new FalseFriendRuleLoader(langDE, true);
    loader.getRules(is, langDE, Languages.getLanguageForShortCode("en"));
    System.out.println("Validation successfully finished.");
  }

}
