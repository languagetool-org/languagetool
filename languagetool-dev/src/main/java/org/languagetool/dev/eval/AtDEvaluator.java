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
package org.languagetool.dev.eval;

import org.languagetool.AnalyzedSentence;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
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
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Check text against AtD via HTTP. Comment in in {@link RealWordCorpusEvaluator}.
 * @since 2.7
 * @deprecated deprecated since 4.0
 */
class AtDEvaluator implements Evaluator {

  private static final int WAIT_MILLIS = 1500;
  
  private final String urlPrefix;

  /**
   * @param urlPrefix e.g. {@code http://de.service.afterthedeadline.com/checkDocument?key=test&data=}
   */
  AtDEvaluator(String urlPrefix) {
    this.urlPrefix = urlPrefix;
  }

  @Override
  public List<RuleMatch> check(AnnotatedText annotatedText) {
    try {
      String text = annotatedText.getPlainText();
      String xml = queryAtDServer(text);
      return getRuleMatches(xml, annotatedText);
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
  }

  private String queryAtDServer(String text) {
    try {
      System.out.println("Sleeping " + WAIT_MILLIS + " before connecting " + urlPrefix + "...");
      Thread.sleep(WAIT_MILLIS);
      URL url = new URL(urlPrefix + URLEncoder.encode(text, "UTF-8"));
      URLConnection conn = url.openConnection();
      String atSign = "@";
      conn.setRequestProperty("User-Agent", "AtDEvalChecker, contact daniel.naber " + atSign + " languagetool.org");
      InputStream contentStream = (InputStream) conn.getContent();
      return StringTools.streamToString(contentStream, "UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<RuleMatch> getRuleMatches(String resultXml, AnnotatedText text) throws XPathExpressionException {
    List<RuleMatch> matches = new ArrayList<>();
    Document document = getDocument(resultXml);
    XPath xPath = XPathFactory.newInstance().newXPath();
    NodeList errors = (NodeList)xPath.evaluate("//error", document, XPathConstants.NODESET);
    for (int i = 0; i < errors.getLength(); i++) {
      Node error = errors.item(i);
      String string = xPath.evaluate("string", error);
      String description = xPath.evaluate("description", error);
      String preContext = xPath.evaluate("precontext", error);
      String errorText = preContext + " " + string;
      int fromPos = text.getPlainText().indexOf(errorText) + preContext.length() + 1;
      int toPos = fromPos + string.length();
      NodeList suggestions = (NodeList)xPath.evaluate("suggestions", error, XPathConstants.NODESET);
      RuleMatch ruleMatch = new RuleMatch(new AtdRule(), null, 
              text.getOriginalTextPositionFor(fromPos), text.getOriginalTextPositionFor(toPos), description);
      for (int j = 0; j < suggestions.getLength(); j++) {
        Node option = suggestions.item(j);
        String optionStr = xPath.evaluate("option", option);
        ruleMatch.setSuggestedReplacement(optionStr);
      }
      matches.add(ruleMatch);
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

  class AtdRule extends Rule {

    @Override
    public String getId() {
      return "ATD_RULE";
    }

    @Override
    public String getDescription() {
      return "Result from remote After The Deadline server";
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      throw new RuntimeException("not implemented");
    }

  }

}
