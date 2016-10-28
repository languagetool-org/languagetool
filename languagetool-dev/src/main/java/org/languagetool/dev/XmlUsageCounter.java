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
package org.languagetool.dev;

import org.languagetool.Language;
import org.languagetool.Languages;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.*;

/**
 * Internal tool to count XML elements and attributes used in our grammar XML files.
 * @since 2.6
 */
class XmlUsageCounter {

  private final Map<String,Integer> map = new HashMap<>();

  private void countElementsAndAttributes(InputStream in) throws XMLStreamException {
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
    while (eventReader.hasNext()) {
      XMLEvent event = eventReader.nextEvent();
      if (event.isStartElement()) {
        String elementName = event.asStartElement().getName().getLocalPart();
        add(elementName);
        Iterator attributes = event.asStartElement().getAttributes();
        while (attributes.hasNext()) {
          Attribute att = (Attribute) attributes.next();
          add(elementName + "/" + att.getName());
        }
      }
    }
  }

  private void add(String name) {
    if (map.containsKey(name)) {
      int oldCount = map.get(name);
      map.put(name, oldCount+1);
    } else {
      map.put(name, 1);
    }
  }

  private void printResult() {
    List<ElemCount> elemCounts = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : map.entrySet()) {
      elemCounts.add(new ElemCount(entry.getKey(), entry.getValue()));
    }
    Collections.sort(elemCounts, (ec1, ec2) -> ec2.count - ec1.count);
    for (ElemCount elemCount : elemCounts) {
      System.out.println(elemCount.count + " " + elemCount.elem);
    }
  }

  public static void main(String[] args) throws XMLStreamException {
    XmlUsageCounter counter = new XmlUsageCounter();
    Set<String> countedFiles = new HashSet<>();
    for (Language language : Languages.get()) {
      List<String> ruleFileNames = language.getRuleFileNames();
      //comment in this to count disambiguation files instead:
      //List<String> ruleFileNames = Collections.singletonList(ResourceDataBroker.RESOURCE_DIR + "/" +
      //        language.getShortCode() + "/" + "disambiguation.xml");
      for (String ruleFileName : ruleFileNames) {
        if (countedFiles.contains(ruleFileName)) {
          continue;
        }
        System.err.println("Counting elements for " + ruleFileName);
        InputStream ruleStream = XmlUsageCounter.class.getResourceAsStream(ruleFileName);
        if (ruleStream == null) {
          System.err.println("Not found, ignoring: " + ruleFileName);
          continue;
        }
        counter.countElementsAndAttributes(ruleStream);
        countedFiles.add(ruleFileName);
      }
    }
    counter.printResult();
  }

  static class ElemCount {
    String elem;
    Integer count;
    ElemCount(String elem, Integer count) {
      this.elem = elem;
      this.count = count;
    }
  }
}
