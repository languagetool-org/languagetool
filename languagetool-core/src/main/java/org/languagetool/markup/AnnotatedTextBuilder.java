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
package org.languagetool.markup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use this builder to create input of text with markup for LanguageTool, so that it
 * can check only the plain text parts and ignore the markup, yet still calculate the
 * positions of errors so that they refer to the complete text, including markup.
 *
 * <p>It's up to you to split the input into parts that are plain text and parts that
 * are markup.
 *
 * <p>For example, text with XML markup like</p>
 *
 * <pre>
 *   Here is &lt;b&gt;some text&lt;/b&gt;
 * </pre>
 *
 * <p>needs to be prepared like this:</p>
 *
 * <pre>
 * new AnnotatedTextBuilder()
 *   .addText("Here is ").addMarkup("&lt;b&gt;").addText("some text").addMarkup("&lt;/b&gt;")
 *   .build()
 * </pre>
 *
 * @since 2.3
 */
public class AnnotatedTextBuilder {

  private final List<TextPart> parts = new ArrayList<>();
  private final Map<AnnotatedText.MetaDataKey, String> metaData = new HashMap<>();
  private final Map<String, String> customMetaData = new HashMap<>();

  public AnnotatedTextBuilder() {
  }

  /**
   * Add global meta data like document title or receiver name (when writing an email).
   * Some rules may use this information.
   * @since 3.9
   */
  public AnnotatedTextBuilder addGlobalMetaData(AnnotatedText.MetaDataKey key, String value) {
    metaData.put(key, value);
    return this;
  }

  /**
   * Add any global meta data about the document to be checked. Some rules may use this information.
   * Unless you're using your own rules for which you know useful keys, you probably want to
   * use {@link #addGlobalMetaData(AnnotatedText.MetaDataKey, String)}.
   * @since 3.9
   */
  public AnnotatedTextBuilder addGlobalMetaData(String key, String value) {
    customMetaData.put(key, value);
    return this;
  }

  /**
   * Add a plain text snippet, to be checked by LanguageTool when using
   * {@link org.languagetool.JLanguageTool#check(AnnotatedText)}.
   */
  public AnnotatedTextBuilder addText(String text) {
    parts.add(new TextPart(text, TextPart.Type.TEXT));
    return this;
  }

  /**
   * Add a markup text snippet like {@code <b attr='something'>} or {@code <div>}. These
   * parts will be ignored by LanguageTool when using {@link org.languagetool.JLanguageTool#check(AnnotatedText)}.
   */
  public AnnotatedTextBuilder addMarkup(String markup) {
    parts.add(new TextPart(markup, TextPart.Type.MARKUP));
    return this;
  }

  /**
   * Add a markup text snippet like {@code <b attr='something'>} or {@code <div>}. These
   * parts will be ignored by LanguageTool when using {@link org.languagetool.JLanguageTool#check(AnnotatedText)}.
   * @param interpretAs A string that will be used by the checker instead of the markup. This is usually
   *                    whitespace, e.g. {@code \n\n} for {@code <p>}
   */
  public AnnotatedTextBuilder addMarkup(String markup, String interpretAs) {
    parts.add(new TextPart(markup, TextPart.Type.MARKUP));
    parts.add(new TextPart(interpretAs, TextPart.Type.FAKE_CONTENT));
    return this;
  }

  /** @since 5.4 */
  public void add(TextPart part) {
    parts.add(part);
  }

  /**
   * Create the annotated text to be passed into {@link org.languagetool.JLanguageTool#check(AnnotatedText)}.
   */
  public AnnotatedText build() {
    int plainTextPosition = 0;
    int totalPosition = 0;
    Map<Integer, MappingValue> mapping = new HashMap<>();
    for (int i = 0; i < parts.size(); i++) {
      TextPart part = parts.get(i);
      if (part.getType() == TextPart.Type.TEXT) {
        plainTextPosition += part.getPart().length();
        totalPosition += part.getPart().length();
        MappingValue mappingValue = new MappingValue(totalPosition);
        mapping.put(plainTextPosition, mappingValue);
      } else if (part.getType() == TextPart.Type.MARKUP) {
        totalPosition += part.getPart().length();
        if (hasFakeContent(i, parts)) {
          plainTextPosition += parts.get(i + 1).getPart().length();
          i++;
          if (mapping.get(plainTextPosition) == null) {
            MappingValue mappingValue = new MappingValue(totalPosition, part.getPart().length());
            mapping.put(plainTextPosition, mappingValue);
          }
        }
      }
    }
    return new AnnotatedText(parts, mapping, metaData, customMetaData);
  }

  private boolean hasFakeContent(int i, List<TextPart> parts) {
    int nextPartIndex = i + 1;
    if (nextPartIndex < parts.size()) {
      if (parts.get(nextPartIndex).getType().equals(TextPart.Type.FAKE_CONTENT)) {
        return true;
      }
    }
    return false;
  }

}

