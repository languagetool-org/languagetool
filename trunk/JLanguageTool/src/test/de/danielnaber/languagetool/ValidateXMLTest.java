/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.tools.StringTools;

public class ValidateXMLTest extends TestCase {

  public void testPatternFile() throws SAXException, IOException, ParserConfigurationException {
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      Language lang = Language.LANGUAGES[i];
      validate("rules" + File.separator + lang.getShortName() + File.separator + "grammar.xml", "rules/rules.dtd");
    }
  }

  public void testFalseFriendsXML() throws SAXException, IOException, ParserConfigurationException {
    validate("rules" + File.separator + "false-friends.xml", "rules/false-friends.dtd");
  }

  private void validate(String filename, String dtdFile) throws SAXException, IOException, ParserConfigurationException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(true);
    SAXParser saxParser = factory.newSAXParser();
    String xml = StringTools.readFile(filename);
    final String decl = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    final String dtd = "<!DOCTYPE rules PUBLIC \"-//W3C//DTD Rules 0.1//EN\" \"file:" +dtdFile+ "\">";
    int pos = xml.indexOf(decl);
    if (pos == -1)
      fail("No XML declaration found in " + filename);
    String newXML = xml.substring(0, pos+decl.length()) + "\r\n" + dtd + xml.substring(pos+decl.length());
    //System.err.println(newXML);
    InputSource is = new InputSource(new StringReader(newXML));
    saxParser.parse(is, new MyHandler());
  }

}

class MyHandler extends DefaultHandler {
  
  public void warning (SAXParseException e) throws SAXException {
    throw e;
  }
  
  public void error (SAXParseException e) throws SAXException {
    throw e;
  }

}
