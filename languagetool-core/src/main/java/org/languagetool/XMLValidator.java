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
package org.languagetool;

import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.io.*;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validate XML files with a given DTD or XML Schema (XSD).
 * 
 * @author Daniel Naber
 */
public final class XMLValidator {

  public XMLValidator() {
    Tools.setPasswordAuthenticator();
  }

  /**
   * Check some limits of our simplified XML output.
   */
  public void checkSimpleXMLString(String xml) throws IOException {
    Pattern pattern = Pattern.compile("(<error.*?/>)", Pattern.DOTALL|Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(xml);
    int pos = 0;
    while (matcher.find(pos)) {
      String errorElement = matcher.group();
      pos = matcher.end();
      if (errorElement.contains("\n") || errorElement.contains("\r")) {
        throw new IOException("<error ...> may not contain line breaks");
      }
      char beforeError = xml.charAt(matcher.start()-1);
      if (beforeError != '\n' && beforeError != '\r') {
        throw new IOException("Each <error ...> must start on a new line");
      }
    }
  }

  /**
   * Validate XML with the given DTD. Throws exception on error. 
   */
  public void validateXMLString(String xml, String dtdFile, String docType) throws SAXException, IOException, ParserConfigurationException {
    validateInternal(xml, dtdFile, docType);
  }

  /**
   * Validate XML file in classpath with the given DTD. Throws exception on error.
   */
  public void validateWithDtd(String filename, String dtdPath, String docType) throws IOException {
    try (InputStream xmlStream = JLanguageTool.getDataBroker().getAsStream(filename)) {
      if (xmlStream == null) {
        throw new IOException("Not found in classpath: " + filename);
      }
      try {
        String xml = StringTools.readStream(xmlStream, "utf-8");
        validateInternal(xml, dtdPath, docType);
      } catch (Exception e) {
        throw new IOException("Cannot load or parse '" + filename + "'", e);
      }
    }
  }

  /**
   * Validate XML file using the given XSD. Throws an exception on error.
   * @param filename File in classpath to validate
   * @param xmlSchemaPath XML schema file in classpath
   */
  public void validateWithXmlSchema(String filename, String xmlSchemaPath) throws IOException {
    try (InputStream xmlStream = JLanguageTool.getDataBroker().getAsStream(filename)) {
      if (xmlStream == null) {
        throw new IOException("File not found in classpath: " + filename);
      }
      URL schemaUrl = this.getClass().getResource(xmlSchemaPath);
      if (schemaUrl == null) {
        throw new IOException("XML schema not found in classpath: " + xmlSchemaPath);
      }
      validateInternal(new StreamSource(xmlStream), schemaUrl);
    } catch (Exception e) {
      throw new IOException("Cannot load or parse '" + filename + "'", e);
    }
  }

  /**
   * Validate XML file using the given XSD. Throws an exception on error.
   * @param baseFilename File to prepend common parts (unification) from before validating main file
   * @param filename File in classpath to validate
   * @param xmlSchemaPath XML schema file in classpath
   */
  public void validateWithXmlSchema(String baseFilename, String filename, String xmlSchemaPath) throws IOException {
    try (InputStream xmlStream = JLanguageTool.getDataBroker().getAsStream(filename);
         InputStream baseXmlStream = JLanguageTool.getDataBroker().getAsStream(baseFilename)) {
      if (xmlStream == null || baseXmlStream == null ) {
        throw new IOException("Files not found in classpath: " + filename + ", " + baseFilename);
      }
      URL schemaUrl = this.getClass().getResource(xmlSchemaPath);
      if (schemaUrl == null) {
        throw new IOException("XML schema not found in classpath: " + xmlSchemaPath);
      }
      validateInternal(mergeIntoSource(baseXmlStream, xmlStream), schemaUrl);
    } catch (Exception e) {
      throw new IOException("Cannot load or parse '" + filename + "'", e);
    }
  }

  private static Source mergeIntoSource(InputStream baseXmlStream, InputStream xmlStream) throws Exception {
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    domFactory.setIgnoringComments(true);
    domFactory.setValidating(false);
    domFactory.setNamespaceAware(true);

    DocumentBuilder builder = domFactory.newDocumentBuilder();
    Document baseDoc = builder.parse(baseXmlStream);
    Document ruleDoc = builder.parse(xmlStream);

    // Shall this be more generic, i.e. reuse not just unification ???
    NodeList unificationNodes = baseDoc.getElementsByTagName("unification");
    Node ruleNode = ruleDoc.getElementsByTagName("rules").item(0);
    Node firstChildRuleNode = ruleNode.getChildNodes().item(1);

    for (int i = 0; i < unificationNodes.getLength(); i++) {
      Node unificationNode = ruleDoc.importNode(unificationNodes.item(i), true);
      ruleNode.insertBefore(unificationNode, firstChildRuleNode);
    }

    return new DOMSource(ruleDoc);
  }
  
  /**
   * Validate XML file using the given XSD. Throws an exception on error.
   * @param xml the XML string to be validated
   * @param xmlSchemaPath XML schema file in classpath
   * @since 2.3
   */
  public void validateStringWithXmlSchema(String xml, String xmlSchemaPath) throws IOException {
    try {
      URL schemaUrl = this.getClass().getResource(xmlSchemaPath);
      if (schemaUrl == null) {
        throw new IOException("XML schema not found in classpath: " + xmlSchemaPath);
      }
      try (ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes("utf-8"))) {
        validateInternal(new StreamSource(stream), schemaUrl);
      }
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }

  private void validateInternal(String xml, String dtdPath, String docType) throws SAXException, IOException, ParserConfigurationException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(true);
    SAXParser saxParser = factory.newSAXParser();
    //used for removing existing DOCTYPE from grammar.xml files
    String cleanXml = xml.replaceAll("<!DOCTYPE.+>", "");
    String decl = "<?xml version=\"1.0\"";
    String endDecl = "?>";
    URL dtdUrl = this.getClass().getResource(dtdPath);
    if (dtdUrl == null) {
      throw new RuntimeException("DTD not found in classpath: " + dtdPath);
    }
    String dtd = "<!DOCTYPE " + docType + " PUBLIC \"-//W3C//DTD Rules 0.1//EN\" \"" + dtdUrl + "\">";
    int pos = cleanXml.indexOf(decl);
    int endPos = cleanXml.indexOf(endDecl);
    if (pos == -1) {
      throw new IOException("No XML declaration found in '" + cleanXml.substring(0, Math.min(100, cleanXml.length())) + "...'");
    }
    String newXML = cleanXml.substring(0, endPos+endDecl.length()) + "\r\n" + dtd + cleanXml.substring(endPos+endDecl.length());
    InputSource is = new InputSource(new StringReader(newXML));
    saxParser.parse(is, new ErrorHandler());
  }

  private void validateInternal(Source xmlSrc, URL xmlSchema) throws SAXException, IOException {
    Validator validator = getValidator(xmlSchema);
    validator.validate(xmlSrc);
  }

  private Validator getValidator(URL xmlSchema) throws SAXException {
    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = sf.newSchema(xmlSchema);
    Validator validator = schema.newValidator();
    validator.setErrorHandler(new ErrorHandler());
    return validator;
  }

  /**
   * XML handler that throws exception on error and warning, does nothing otherwise.
   */
  static class ErrorHandler extends DefaultHandler {

    @Override
    public void warning (SAXParseException e) throws SAXException {
      System.err.println(e.getMessage()
              + " Problem found at line " + e.getLineNumber()
              + ", column " + e.getColumnNumber() + ".");
      throw e;
    }

    @Override
    public void error (SAXParseException e) throws SAXException {
      System.err.println(e.getMessage()
              + " Problem found at line " + e.getLineNumber()
              + ", column " + e.getColumnNumber() + ".");
      throw e;
    }

  }

}
