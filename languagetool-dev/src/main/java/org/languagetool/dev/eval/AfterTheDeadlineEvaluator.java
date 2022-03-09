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

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.ExampleSentence;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;
import org.languagetool.tools.StringTools;
import org.w3c.dom.Document;
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
import java.util.List;

/**
 * Runs incorrect example sentences from grammar.xml against an
 * After the Deadline instance reachable via http.
 * @since 2.6
 */
class AfterTheDeadlineEvaluator {

  private static final int WAIT_TIME_MILLIS = 1000;

  private final String urlPrefix;

  AfterTheDeadlineEvaluator(String urlPrefix) {
    this.urlPrefix = urlPrefix;
  }

  private void run(Language lang) throws IOException, InterruptedException {
    List<Rule> rules = getRules(lang);
    int sentenceCount = 0;
    int errorFoundCount = 0;
    System.out.println("Starting test for " + lang.getName() + " on " + urlPrefix);
    System.out.println("Wait time between HTTP requests: " + WAIT_TIME_MILLIS + "ms");
    System.out.println("Starting test on " + rules.size() + " rules");
    for (Rule rule : rules) {
      if (rule.isDefaultOff()) {
        System.out.println("Skipping rule that is off by default: " + rule.getId());
        continue;
      }
      List<IncorrectExample> incorrectExamples = rule.getIncorrectExamples();
      System.out.println("\n" + rule.getId() + ":");
      if (incorrectExamples.isEmpty()) {
        System.out.println(" (no examples)");
        continue;
      }
      for (IncorrectExample example : incorrectExamples) {
        boolean match = queryAtDServer(example);
        sentenceCount++;
        if (match) {
          errorFoundCount++;
        }
        String marker = match ? "+" : "-";
        System.out.println("  [" + marker + "] " + example.getExample().replace("<marker>", "<m>").replace("</marker>", "</m>"));
        Thread.sleep(WAIT_TIME_MILLIS);
      }
      //use this to stop: if (sentenceCount > 100) { break; }
    }
    System.out.println("\nDone.");
    System.out.println("Sentence count: " + sentenceCount);
    float percentage = (float)errorFoundCount / sentenceCount * 100;
    System.out.printf("Expected errors found: " + errorFoundCount + " (%.2f%%)\n", percentage);
  }

  private List<Rule> getRules(Language lang) throws IOException {
    JLanguageTool lt = new JLanguageTool(lang);
    return lt.getAllActiveRules();
  }

  private boolean queryAtDServer(IncorrectExample example) {
    String sentence = ExampleSentence.cleanMarkersInExample(example.getExample());
    try {
      URL url = new URL(urlPrefix + URLEncoder.encode(sentence, "UTF-8"));
      String result = getContent(url);
      if (isExpectedErrorFound(example, result)) {
        return true;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return false;
  }

  private String getContent(URL url) throws IOException {
    final InputStream contentStream = (InputStream) url.getContent();
    return StringTools.streamToString(contentStream, "UTF-8");
  }

  boolean isExpectedErrorFound(IncorrectExample incorrectExample, String resultXml) throws XPathExpressionException {
    String example = incorrectExample.getExample();
    Document document = getDocument(resultXml);
    XPath xPath = XPathFactory.newInstance().newXPath();
    NodeList errorStrings = (NodeList)xPath.evaluate("//string/text()", document, XPathConstants.NODESET);
    for (int i = 0; i < errorStrings.getLength(); i++) {
      String errorStr = errorStrings.item(i).getNodeValue();
      if (errorStr.isEmpty()) {
        continue;
      }
      List<Integer> errorStartPosList = getStartPositions(incorrectExample, errorStr);
      List<String> mismatches = new ArrayList<>();
      for (Integer errorStartPos : errorStartPosList) {
        int errorEndPos = errorStartPos + errorStr.length();
        int expectedErrorStartPos = example.indexOf("<marker>");
        int expectedErrorEndPos = errorStartPos + errorStr.length();
        if (errorStartPos == expectedErrorStartPos && errorEndPos == expectedErrorEndPos) {
          return true;
        } else {
          mismatches.add("Position mismatch: " + errorStartPos + "-" + errorEndPos + " != " + expectedErrorStartPos + "-" + expectedErrorEndPos);
        }
      }
      for (String mismatch : mismatches) {
        System.out.println("  " + mismatch);
      }
    }
    return false;
  }

  private List<Integer> getStartPositions(IncorrectExample example, String searchStr) {
    List<Integer> posList = new ArrayList<>();
    int pos = 0;
    String sentence = ExampleSentence.cleanMarkersInExample(example.getExample());
    while ((pos = sentence.indexOf(searchStr, pos)) != -1) {
      posList.add(pos);
      pos++;
    }
    return posList;
  }

  private Document getDocument(String xml) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource inputSource = new InputSource(new StringReader(xml));
      return builder.parse(inputSource);
    } catch (Exception e) {
      throw new RuntimeException("Could not parse XML: " + xml);
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: " + AfterTheDeadlineEvaluator.class.getSimpleName() + " <langCode> <urlPrefix>");
      System.err.println("  <urlPrefix> After the Deadline instance, e.g. 'http://de.service.afterthedeadline.com/checkDocument?key=test&data='");
      System.exit(1);
    }
    AfterTheDeadlineEvaluator evaluator = new AfterTheDeadlineEvaluator(args[1]);
    evaluator.run(Languages.getLanguageForShortCode(args[0]));
  }
}
