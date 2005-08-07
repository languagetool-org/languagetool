/* JLanguageTool, a natural language style checker 
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
package de.danielnaber.languagetool.rules.patterns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * A Rule that drescribes a language error as a simple pattern of words or their
 * part-of-speech tags.
 * 
 * @author Daniel Naber
 */
public class PatternRule extends Rule {

  private String id;
  private Language language;
  private String pattern;
  private String description;

  private Element[] patternElements;

  PatternRule(String id, Language language, String pattern, String description) {
    this.id = id;
    this.language = language;
    this.pattern = pattern;
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public Language getLanguage() {
    return language;
  }

  public String toString() {
    return id + ":" + pattern + ":" + description;
  }

  //FIXME
  public RuleMatch[] match(AnalyzedSentence text) {
    List ruleMatches = new ArrayList(); 
    List tokens = text.getTokens();
    if (patternElements == null) {
      patternElements = getPatternElements(pattern); 
    }
    int tokenPos = 0;
    for (Iterator iter = tokens.iterator(); iter.hasNext();) {
      String token = (String) iter.next();
      if (token.trim().equals("")) {
        // ignore
      } else {
        boolean allElementsMatch = true;
        for (int i = 0; i < patternElements.length; i++) {
          Element elem = patternElements[i];
          int nextPos = getNextTokenPosition(tokens, tokenPos+i);       // jump over whitespace
          if (nextPos == -1) {
            allElementsMatch = false;
            break;
          }
          String matchToken = (String)tokens.get(nextPos);
          System.err.println(elem + " matches? " +matchToken);
          if (!elem.match(matchToken)) {
            allElementsMatch = false;
            System.err.println("NOMATCH: " + this);
            break;
          } else {
            System.err.println("MATCH: " + this);
          }
        }
        if (allElementsMatch) {
          System.out.println("------>Match: " + this);
          int startPos = 0;
          int endPos = 0;
          RuleMatch ruleMatch = new RuleMatch(this, startPos, endPos, description);
          ruleMatches.add(ruleMatch);
        }
      }
      tokenPos++;
    }      
    return (RuleMatch[])ruleMatches.toArray(new RuleMatch[0]);
  }

  private int getNextTokenPosition(List tokens, int startPos) {
    int pos = startPos;
    String token = "";
    do {
      if (pos >= tokens.size()) {
        return -1;
      }
      token = (String)tokens.get(pos);
      pos++;
    } while (token.trim().equals(""));
    return pos-1;
  }

  //FIXME
  /*public RuleMatch[] matchOLD(AnalyzedSentence text) {
    List ruleMatches = new ArrayList(); 
    String xml = text.getXML();
    try {
      // use XPath to match the text and the rule:
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder;
      builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(xml));
      Node doc = builder.parse(is);
      String xPath = patternToXPathExpression(pattern);
      XPath expression = new org.jaxen.dom.DOMXPath(xPath);
      //Navigator navigator = expression.getNavigator();
      List results = expression.selectNodes(doc);
      Iterator iterator = results.iterator();
      while (iterator.hasNext()) {
        // the XPath expression matches the text:
        Node result = (Node) iterator.next();
        String startPosStr = result.getAttributes().getNamedItem("startpos").getNodeValue();
        int startPos = Integer.parseInt(startPosStr);
        String endPosStr = result.getAttributes().getNamedItem("endpos").getNodeValue();
        int endPos = Integer.parseInt(endPosStr);
        //System.out.println("result=" + result.getAttributes().getNamedItem("startpos").getNodeValue());
        // FIXME: use a real message, not just description:
        RuleMatch ruleMatch = new RuleMatch(this, startPos, endPos, description);
        ruleMatches.add(ruleMatch);
        //String value = StringFunction.evaluate(result, navigator);
      }
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (JaxenException e) {
      throw new RuntimeException(e);
    }
    return (RuleMatch[])ruleMatches.toArray(new RuleMatch[0]);
  }*/
  
  /*private String patternToXPathExpression(String pattern) {
    StringBuffer xpath = new StringBuffer();
    Element[] parts = getPatternElements(pattern);
    xpath.append("/s/");
    for (int i = 0; i < parts.length; i++) {
      Element elem = parts[i];
      String expr = elem.getXPathExpression();
      if (i == 0)
        expr = expr.replaceAll("<POS>", "");
      else
        expr = expr.replaceAll("<POS>", "[1]");
      xpath.append(expr);
      if (i < parts.length-1) {
        xpath.append("/following-sibling::");
      }
    }
    System.err.println("### XPATH="+xpath.toString());
    return xpath.toString();
  }*/

  private Element[] getPatternElements(String pattern) {
    List elements = new ArrayList();
    pattern = pattern.replaceAll("[\\(\\)]", "");
    String[] parts = pattern.split("\\s+");
    for (int i = 0; i < parts.length; i++) {
      String element = parts[i];
      if (element.startsWith("\"") && !element.endsWith("\"") || element.endsWith("\"") && !element.startsWith("\"")) {
        throw new IllegalArgumentException("Invalid pattern '" + pattern + "': unbalanced quote");
      }
      if (element.startsWith("\"") && element.endsWith("\"")) {         // cut off quotes
        element = element.substring(1, element.length()-1);
        String tokenParts[] = element.split("\\|");
        // TODO: make case sensitiviy optional:
        StringElement stringElement = new StringElement(tokenParts, false); 
        elements.add(stringElement);
      } else {
        throw new IllegalArgumentException("Unknown type in pattern: " + pattern);
      }
    }
    return (Element[])elements.toArray(new Element[0]);
  }
  
}
