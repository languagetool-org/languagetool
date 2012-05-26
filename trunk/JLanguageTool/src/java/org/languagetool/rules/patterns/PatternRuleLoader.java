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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

import org.languagetool.JLanguageTool;

/**
 * Loads {@link PatternRule}s from an XML file.
 * 
 * @author Daniel Naber
 */
public class PatternRuleLoader extends DefaultHandler {

  /**
   * @param is stream with the XML rules
   * @param filename used only for verbose exception message
   */
  public final List<PatternRule> getRules(final InputStream is,
      final String filename) throws IOException {
    try {
      final PatternRuleHandler handler = new PatternRuleHandler();
      final SAXParserFactory factory = SAXParserFactory.newInstance();
      final SAXParser saxParser = factory.newSAXParser();
      saxParser.getXMLReader().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      saxParser.parse(is, handler);
      return handler.getRules();
    } catch (final Exception e) {
      throw new IOException("Cannot load or parse '" + filename + "'", e);
    }
  }

  /** Testing only. */
  public final void main(final String[] args) throws IOException {
    final PatternRuleLoader prg = new PatternRuleLoader();
    final String name = "/de/grammar.xml";
    final List<PatternRule> l = prg.getRules(JLanguageTool.getDataBroker().getFromRulesDirAsStream(name), name);
    System.out.println(l);
  }

}

