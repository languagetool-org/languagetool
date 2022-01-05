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
import org.languagetool.tools.Tools;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Loads {@link PatternRule}s from an XML file.
 * 
 * @author Daniel Naber
 */
public class PatternRuleLoader extends DefaultHandler {

  private boolean relaxedMode = false;

  /**
   * @param file XML file with pattern rules
   */
  public final List<AbstractPatternRule> getRules(File file) throws IOException {
    try (InputStream inputStream = new FileInputStream(file)) {
      PatternRuleLoader ruleLoader = new PatternRuleLoader();
      return ruleLoader.getRules(inputStream, file.getAbsolutePath());
    }
  }

  /**
   * If set to true, don't throw an exception if id or name is not set.
   * Used for online rule editor.
   * @since 2.1
   */
  public void setRelaxedMode(boolean relaxedMode) {
    this.relaxedMode = relaxedMode;
  }

  /**
   * @param is stream with the XML rules
   * @param filename used only for verbose exception message - should refer to where the stream comes from
   */
  public final List<AbstractPatternRule> getRules(InputStream is, String filename) throws IOException {
    try {
      PatternRuleHandler handler = new PatternRuleHandler(filename);
      handler.setRelaxedMode(relaxedMode);
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();

      if (JLanguageTool.isCustomPasswordAuthenticatorUsed()) {
        Tools.setPasswordAuthenticator();
      }

      saxParser.getXMLReader().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      saxParser.parse(is, handler);
      return handler.getRules();
    } catch (Exception e) {
      throw new IOException("Cannot load or parse input stream of '" + filename + "'", e);
    }
  }

}

