/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Loads {@link PatternRule}s from a false friends XML file.
 * 
 * @author Daniel Naber
 */
public class FalseFriendRuleLoader extends DefaultHandler {

  private final String falseFriendHint;
  private final String falseFriendSugg;

  public FalseFriendRuleLoader(Language motherTongue) {
    ResourceBundle messages = ResourceBundle.getBundle(JLanguageTool.MESSAGE_BUNDLE, motherTongue.getLocale());
    this.falseFriendHint =  messages.getString("false_friend_hint");
    this.falseFriendSugg =  messages.getString("false_friend_suggestion");
  }

  public FalseFriendRuleLoader(String falseFriendHint, String falseFriendSugg) {
    this.falseFriendHint = Objects.requireNonNull(falseFriendHint);
    this.falseFriendSugg = Objects.requireNonNull(falseFriendSugg);
  }

  /**
   * @param file XML file with false friend rules
   * @since 2.3
   */
  public final List<AbstractPatternRule> getRules(File file, Language language, Language motherTongue) throws IOException {
    try (InputStream inputStream = new FileInputStream(file)) {
      return getRules(inputStream, language, motherTongue);
    } catch (ParserConfigurationException | SAXException e) {
      throw new IOException("Could not load false friend rules from " + file, e);
    }
  }

  public final List<AbstractPatternRule> getRules(InputStream stream,
      Language textLanguage, Language motherTongue)
      throws ParserConfigurationException, SAXException, IOException {
    FalseFriendRuleHandler handler = new FalseFriendRuleHandler(
        textLanguage, motherTongue, falseFriendHint);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    saxParser.getXMLReader().setFeature(
            "http://apache.org/xml/features/nonvalidating/load-external-dtd",
            false);
    saxParser.parse(stream, handler);
    List<AbstractPatternRule> rules = handler.getRules();
    // Add suggestions to each rule:
    MessageFormat msgFormat = new MessageFormat(falseFriendSugg);
    for (AbstractPatternRule rule : rules) {
      List<String> suggestions = handler.getSuggestionMap().get(rule.getId());
      if (suggestions != null) {
        String[] msg = { formatSuggestions(suggestions) };
        rule.setMessage(rule.getMessage() + " " + msgFormat.format(msg));
      }
    }
    return rules;
  }

  private String formatSuggestions(List<String> l) {
    return l.stream().map(o -> "<suggestion>" + o + "</suggestion>").collect(Collectors.joining(", "));
  }

}
