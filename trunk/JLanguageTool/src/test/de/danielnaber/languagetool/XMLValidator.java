/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.tools.StringTools;

/**
 * Validate XML files with a given DTD.
 * 
 * @author Daniel Naber
 */
public class XMLValidator {

  public XMLValidator() {
  }

  /**
   * Check some limits of our simplified XML output.  
   */
  public void checkSimpleXMLString(String xml) throws IOException {
    Pattern p = Pattern.compile("(<error.*?/>)", Pattern.DOTALL|Pattern.MULTILINE);
    Matcher matcher = p.matcher(xml);
    int pos = 0;
    while (matcher.find(pos)) {
      String errorElement = matcher.group();
      pos = matcher.end();
      if (errorElement.contains("\n") || errorElement.contains("\r"))
        throw new IOException("<error ...> may not contain line breaks");
      char beforeError = xml.charAt(matcher.start()-1);
      if (beforeError != '\n' && beforeError != '\r')
        throw new IOException("Each <error ...> must start on a new line");
    }
  }

  /**
   * Validate XML with the given DTD. Throws exception on error. 
   */
  public void validateXMLString(String xml, String dtdFile, String docType) throws SAXException, IOException, ParserConfigurationException {
    validateInternal(xml, dtdFile, docType);
  }
  
  /**
   * Validate XML file with the given DTD. Throws exception on error. 
   */
  public final void validate(String filename, String dtdFile, String docType) throws SAXException, IOException, ParserConfigurationException {
    String xml = StringTools.readFile(this.getClass().getResourceAsStream(filename));
    validateInternal(xml, dtdFile, docType);
  }

  private void validateInternal(String xml, String dtdFile, String doctype) throws SAXException, IOException, ParserConfigurationException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(true);
    SAXParser saxParser = factory.newSAXParser();
    final String decl = "<?xml version=\"1.0\"";
    final String endDecl = "?>";
    final String dtd = "<!DOCTYPE "+doctype+" PUBLIC \"-//W3C//DTD Rules 0.1//EN\" \"" +this.getClass().getResource(dtdFile)+ "\">";
    int pos = xml.indexOf(decl);
    int endPos = xml.indexOf(endDecl);
    if (pos == -1)
      throw new IOException("No XML declaration found in '" + xml.substring(0, Math.min(100, xml.length())) + "...'");
    String newXML = xml.substring(0, endPos+endDecl.length()) + "\r\n" + dtd + xml.substring(endPos+endDecl.length());
    //System.err.println(newXML);
    InputSource is = new InputSource(new StringReader(newXML));
    saxParser.parse(is, new ErrorHandler());
  }

}

/**
 * XML handler that throws exception on error and warning, does nothing otherwise.
 */
class ErrorHandler extends DefaultHandler {
  
  public void warning (SAXParseException e) throws SAXException {
    throw e;
  }
  
  public void error (SAXParseException e) throws SAXException {
    throw e;
  }

}
