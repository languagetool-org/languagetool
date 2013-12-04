/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia.atom;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Parse the Atom feed of Wikipedia's latest changes.
 * @since 2.4
 */
class AtomFeedParser {

  List<AtomFeedItem> getAtomFeedItems(InputStream xml) throws XMLStreamException {
    List<AtomFeedItem> items = new ArrayList<>();
    String id = null;
    String title = null;
    Date date = null;
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    XMLEventReader eventReader = inputFactory.createXMLEventReader(xml);
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");  // e.g. 2013-12-03T09:48:29Z
    while (eventReader.hasNext()) {
      XMLEvent event = eventReader.nextEvent();
      if (event.isStartElement()) {
        String localPart = event.asStartElement().getName().getLocalPart();
        switch (localPart) {
          case "id":
            id = getCharacterData(eventReader);
            break;
          case "title":
            title = getCharacterData(eventReader);
            break;
          case "updated":
            String dateString = getCharacterData(eventReader);
            try {
              date = dateFormat.parse(dateString);
            } catch (ParseException e) {
              throw new RuntimeException("Could not parse date string '" + dateString + "'", e);
            }
            break;
          case "summary":
            if (id == null || title == null || date == null) {
              throw new RuntimeException("id, title and/or date is null: id=" + id + ", title=" + title + ", date=" + date);
            }
            items.add(new AtomFeedItem(id, title, getCharacterData(eventReader), date));
            id = null;
            title = null;
            date = null;
            break;
        }
      }
    }
    return items;
  }

  private String getCharacterData(XMLEventReader eventReader) throws XMLStreamException {
    XMLEvent event = eventReader.nextEvent();
    StringBuilder sb = new StringBuilder();
    while (event.isCharacters()) {
      sb.append(event.asCharacters().getData());
      event = eventReader.nextEvent();
    }
    return sb.toString();
  }

}
