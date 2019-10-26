package org.languagetool.rules.ga;
/*
 * Copyright © 2019 Jim O'Regan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AnGramadoirTriailData {
  List<TriailError> errors;
  AnGramadoirTriailData() {
    this.errors = new ArrayList<TriailError>();
  }
  AnGramadoirTriailData(InputStream is) throws IOException {
    this();
    try {
      this.loadXML(is);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }
  public void loadXML(InputStream is) throws Exception {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(is);
    String root = doc.getDocumentElement().getNodeName();
    if (root != "matches") {
      throw new IOException("Expected root node " + root);
    }
    NodeList nl = doc.getDocumentElement().getChildNodes();
    for(int i=0; i < nl.getLength(); i++) {
      Node n = nl.item(i);
      String nodename = n.getNodeName();
      if("#text".equals(nodename)) {
        continue;
      } else if(!"error".equals(nodename)) {
        throw new IOException("Unexpected node " + nodename);
      }
      int fromY = getIntAttribute(n, "fromy");
      int fromX = getIntAttribute(n, "fromx");
      int toY = getIntAttribute(n, "toy");
      int toX = getIntAttribute(n, "tox");
      String ruleID = getStringAttribute(n, "ruleId");
      String msg = getStringAttribute(n, "msg");
      String context = getStringAttribute(n, "context");
      int contextOffset = getIntAttribute(n, "contextoffset");
      int errorLength = getIntAttribute(n, "errorlength");
      errors.add(new TriailError(fromY, fromX, toY, toX, ruleID, msg, context, contextOffset, errorLength));
    }
  }
  private String getStringAttribute(Node n, String attr) {
    return n.getAttributes().getNamedItem(attr).getNodeValue();
  }
  private int getIntAttribute(Node n, String attr) {
    String raw = n.getAttributes().getNamedItem(attr).getNodeValue();
    return Integer.parseInt(raw);
  }
  public List<TriailError> getErrors() {
    return errors;
  }
}
