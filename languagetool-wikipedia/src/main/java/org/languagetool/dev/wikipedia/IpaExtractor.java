/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract IPA information from Wikipedia XML dump. Might be used to
 * find words with difficult spelling. Note that only up to one IPA
 * information is found per article.
 *
 * Example Wikitext that is detected:
 * <pre>'''Trance''' [{{IPA|trɑ̃s}}]</pre>
 * @since 2.9
 */
class IpaExtractor {

  private static final Pattern FULL_IPA_PATTERN = Pattern.compile("'''?(.*?)'''?\\s+\\[?\\{\\{IPA\\|([^}]*)\\}\\}");
  private static final Pattern IPA_PATTERN = Pattern.compile("\\{\\{IPA\\|([^}]*)\\}\\}");

  private int articleCount = 0;
  private int ipaCount = 0;

  public static void main(String[] args) throws XMLStreamException, FileNotFoundException {
    if (args.length == 0) {
      System.out.println("Usage: " + IpaExtractor.class.getSimpleName() + " <xml-dump...>");
      System.exit(1);
    }
    IpaExtractor extractor = new IpaExtractor();
    for (String filename : args) {
      FileInputStream fis = new FileInputStream(filename);
      extractor.run(fis);
    }
    System.err.println("articleCount: " + extractor.articleCount);
    System.err.println("IPA count: " + extractor.ipaCount);
  }

  private void run(FileInputStream fis) throws XMLStreamException {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLEventReader reader = factory.createXMLEventReader(fis);
    String title = null;
    while (reader.hasNext()) {
      XMLEvent event = reader.nextEvent();
      if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
        String elementName = event.asStartElement().getName().getLocalPart();
        switch (elementName) {
          case "title":
            XMLEvent nextEvent = reader.nextEvent();
            title = nextEvent.asCharacters().getData();
            articleCount++;
            break;
          case "text":
            ipaCount += handleTextElement(title, reader);
            break;
        }
      }
    }
  }

  private int handleTextElement(String title, XMLEventReader reader) throws XMLStreamException {
    XMLEvent event = reader.nextEvent();
    StringBuilder sb = new StringBuilder();
    while (event.isCharacters()) {
      sb.append(event.asCharacters().getData());
      event = reader.nextEvent();
    }
    String wikiText = sb.toString();
    int index = wikiText.indexOf("{{IPA");
    if (index != -1) {
      Matcher matcher = FULL_IPA_PATTERN.matcher(wikiText);
      if (matcher.find()) {
        System.out.println(title + ": " + matcher.group(1) + " -> " + matcher.group(2));
        return 1;
      } else {
        Matcher matcher2 = IPA_PATTERN.matcher(wikiText);
        if (matcher2.find()) {
          System.out.println(title + ": " + matcher2.group(1));
          return 1;
        } else {
          System.out.println(title + ": (no pattern found)");
        }
      }
    }
    return 0;
  }

}
