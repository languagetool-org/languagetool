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
package org.languagetool.dev.wikipedia;

import org.languagetool.Language;
import org.languagetool.tokenizers.Tokenizer;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Provides access to the sentences of a Wikipedia XML dump. Note that
 * conversion exceptions are logged to STDERR and are otherwise ignored.
 * @since 2.4
 */
class WikipediaSentenceSource extends SentenceSource {

  private final TextMapFilter textFilter = new SwebleWikipediaTextFilter();
  private final XMLEventReader reader;
  private final Tokenizer sentenceTokenizer;
  private final List<String> sentences;
  
  WikipediaSentenceSource(InputStream xmlInput, Language language) throws XMLStreamException {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    reader = factory.createXMLEventReader(xmlInput);
    sentenceTokenizer = language.getSentenceTokenizer();
    sentences = new ArrayList<>();
  }

  @Override
  public boolean hasNext() {
    try {
      fillSentences();
    } catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }
    return sentences.size() > 0;
  }

  @Override
  public Sentence next() {
    try {
      fillSentences();
      if (sentences.size() == 0) {
        throw new NoSuchElementException();
      }
      return new Sentence(sentences.remove(0), getSource());
    } catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getSource() {
    return "wikipedia";
  }

  private void fillSentences() throws XMLStreamException {
    while (sentences.size() == 0 && reader.hasNext()) {
      XMLEvent event = reader.nextEvent();
      if (event.getEventType() == XMLStreamConstants.START_ELEMENT && event.asStartElement().getName().getLocalPart().equals("text")) {
        event = reader.nextEvent();
        StringBuilder sb = new StringBuilder();
        while (event.isCharacters()) {
          sb.append(event.asCharacters().getData());
          event = reader.nextEvent();
        }
        try {
          String textToCheck = textFilter.filter(sb.toString()).getPlainText();
          for (String sentence : sentenceTokenizer.tokenize(textToCheck)) {
            if (acceptSentence(sentence)) {
              sentences.add(sentence);
            }
          }
        } catch (Exception e) {
          System.err.println("Could not extract text, skipping document: " + e.toString());
        }
      }
    }
  }

}
