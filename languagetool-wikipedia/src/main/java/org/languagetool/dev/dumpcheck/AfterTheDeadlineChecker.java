/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.dumpcheck;

import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.tools.StringTools;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * After the Deadline (http://openatd.wordpress.com) uses LanguageTool internally
 * for non-English checks but does some additional filtering on the matches. This class
 * checks Wikipedia and Tatoeba sentences with AtD so one can evaluate changes in the 
 * AtD filtering.
 * 
 * @since 2.6
 */
class AfterTheDeadlineChecker {

  private final String urlPrefix;
  private final int maxSentenceCount;

  AfterTheDeadlineChecker(String urlPrefix, int maxSentenceCount) {
    this.urlPrefix = urlPrefix;
    this.maxSentenceCount = maxSentenceCount;
  }

  private void run(Language lang, List<String> fileNames) throws IOException, XPathExpressionException {
    MixingSentenceSource mixingSource = MixingSentenceSource.create(fileNames, lang);
    int sentenceCount = 0;
    while (mixingSource.hasNext()) {
      Sentence sentence = mixingSource.next();
      String resultXml = queryAtDServer(sentence.getText());
      System.out.println("==========================");
      System.out.println(sentence.getSource() + ": " + sentence.getText());
      List<String> matches = getMatches(resultXml);
      for (String match : matches) {
        System.out.println("  " + match);
      }
      sentenceCount++;
      if (maxSentenceCount > 0 && sentenceCount > maxSentenceCount) {
        System.err.println("Limit reached, stopping at sentence #" + sentenceCount);
        break;
      }
    }
  }

  private String queryAtDServer(String text) {
    try {
      URL url = new URL(urlPrefix + URLEncoder.encode(text, "UTF-8"));
      InputStream contentStream = (InputStream) url.getContent();
      return StringTools.streamToString(contentStream, "UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<String> getMatches(String resultXml) throws XPathExpressionException {
    List<String> matches = new ArrayList<>();
    Document document = getDocument(resultXml);
    XPath xPath = XPathFactory.newInstance().newXPath();
    NodeList errors = (NodeList)xPath.evaluate("//error", document, XPathConstants.NODESET);
    for (int i = 0; i < errors.getLength(); i++) {
      Node error = errors.item(i);
      String string = xPath.evaluate("string", error);
      String description = xPath.evaluate("description", error);
      matches.add(description + ": " + string);
    }
    return matches;
  }

  private Document getDocument(String xml) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource inputSource = new InputSource(new StringReader(xml));
      return builder.parse(inputSource);
    } catch (Exception e) {
      throw new RuntimeException("Could not parse XML: " + xml, e);
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 4) {
      System.out.println("Usage: " + AfterTheDeadlineChecker.class.getSimpleName() + " <langCode> <atdUrlPrefix> <file...>");
      System.out.println("   <langCode>      a language code like 'en' for English");
      System.out.println("   <atdUrlPrefix>  URL prefix of After the Deadline server, like 'http://localhost:1059/checkDocument?data='");
      System.out.println("   <sentenceLimit> Maximum number of sentences to check, or 0 for no limit");
      System.out.println("   <file...>       Wikipedia and/or Tatoeba file(s)");
      System.exit(1);
    }
    Language language = Languages.getLanguageForShortCode(args[0]);
    String urlPrefix = args[1];
    int maxSentenceCount = Integer.parseInt(args[2]);
    List<String> files = Arrays.asList(args).subList(3, args.length);
    AfterTheDeadlineChecker atdChecker = new AfterTheDeadlineChecker(urlPrefix, maxSentenceCount);
    atdChecker.run(language, files);
  }
}
