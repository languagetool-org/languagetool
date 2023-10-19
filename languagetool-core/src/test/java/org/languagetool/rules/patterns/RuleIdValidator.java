/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.rules.Rule;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.*;

public class RuleIdValidator {

  private final Language lang;

  public RuleIdValidator(Language lang) {
    this.lang = lang;
  }
  
  public void validateUniqueness() {
    System.out.println("Check rule id uniqueness for " + lang + "...");
    List<String> fileNames = lang.getRuleFileNames();
    Map<String, String> idsToFile = new HashMap<>();
    List<Rule> allRules = new JLanguageTool(lang).getAllRules();
    for (Rule rule : allRules) {
      if (!(rule instanceof AbstractPatternRule || rule instanceof RepeatedPatternRuleTransformer.RepeatedPatternRule)) {
        idsToFile.put(rule.getId(), "Java (" + rule.getClass().getName() + ")");
      }
    }
    for (String fileName : fileNames) {
      try (InputStream is = JLanguageTool.getDataBroker().getAsStream(fileName)) {
        if (is == null) {
          System.out.println("Skipping " + fileName + " - not found");  // e.g. nl/grammar-test-1.xml
          continue;
        }
        XmlIdHandler handler = new XmlIdHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(is, handler);
        for (String id : handler.ids) {
          if (idsToFile.containsKey(id)) {
            throw new RuntimeException("id '" + id + "' found at least twice. Found in " + fileName + " and " + idsToFile.get(id));
          }
          idsToFile.put(id, fileName);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    //System.out.println("id uniqueness for " + lang + " finished");
  }
  
  private static class XmlIdHandler extends DefaultHandler {

    private final Set<String> ids = new HashSet<>();
    
    @Override
    public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) {
      if (qName.equals("rule") || qName.equals("rulegroup")) {
        String id = attrs.getValue("id");
        if (id != null) {
          ids.add(id);
        }
      }
    }

  }
  
}
