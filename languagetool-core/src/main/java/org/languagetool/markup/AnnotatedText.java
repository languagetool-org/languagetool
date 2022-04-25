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

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * A text with markup and with a mapping so error positions will refer to the original
 * position that includes the markup, even though only the plain text parts are checked.
 * Use {@link AnnotatedTextBuilder} to create objects of this type.
 * @since 2.3
 */
public class AnnotatedText {

  /**
   * @since 3.9
   */
  public enum MetaDataKey {
    DocumentTitle,
    EmailToAddress,
    FullName,
    EmailNumberOfAttachments
  }

  private final List<TextPart> parts;
  private final Map<Integer, MappingValue> mapping;  // plain text position to original text (with markup) position
  private final Map<MetaDataKey, String> metaData;
  private final Map<String, String> customMetaData;

  AnnotatedText(List<TextPart> parts, Map<Integer, MappingValue> mapping, Map<MetaDataKey, String> metaData, Map<String, String> customMetaData) {
    this.parts = Objects.requireNonNull(parts);
    this.mapping = Objects.requireNonNull(mapping);
    this.metaData = Objects.requireNonNull(metaData);
    this.customMetaData = Objects.requireNonNull(customMetaData);
  }

  /**
   * @since 5.4
   */
  public List<TextPart> getParts() {
    return parts;
  }

  /**
   * Get the plain text, without markup and content from {@code interpretAs}.
   * @since 4.3
   */
  public String getOriginalText() {
    StringBuilder sb = new StringBuilder();
    for (TextPart part : parts) {
      if (part.getType() == TextPart.Type.TEXT) {
        sb.append(part.getPart());
      }
    }
    return sb.toString();
  }

  /**
   * Get the plain text, without markup but with content from {@code interpretAs}.
   */
  public String getPlainText() {
    StringBuilder sb = new StringBuilder();
    for (TextPart part : parts) {
      if (part.getType() == TextPart.Type.TEXT || part.getType() == TextPart.Type.FAKE_CONTENT) {
        sb.append(part.getPart());
      }
    }
    return sb.toString();
  }

  /**
   * @since 4.3
   */
  public String getTextWithMarkup() {
    StringBuilder sb = new StringBuilder();
    for (TextPart part : parts) {
      if (part.getType() != TextPart.Type.FAKE_CONTENT) {
        sb.append(part.getPart());
      }
    }
    return sb.toString();
  }

  /**
   * Internally used by LanguageTool to adjust error positions to point to the
   * original location with markup, even though markup was ignored during text checking.
   * @param plainTextPosition the position in the plain text (no markup) that was checked
   * @param isToPos the from/to position needed
   * @return an adjusted position of the same location in the text with markup
   */
  public int getOriginalTextPositionFor(int plainTextPosition, boolean isToPos) {
    if (plainTextPosition < 0) {
      throw new IllegalArgumentException("plainTextPosition must be >= 0: " + plainTextPosition);
    }
    if (mapping.isEmpty()) {
      return 0;
    }
    int minDiff = Integer.MAX_VALUE;
    MappingValue bestMatch = null;
    // algorithm: find the closest lower position
    for (Map.Entry<Integer, MappingValue> entry : mapping.entrySet()) {
      int maybeClosePosition = entry.getKey();
      if (plainTextPosition < maybeClosePosition) {
        int diff = maybeClosePosition - plainTextPosition;
        if (diff > 0 && diff < minDiff) {
          bestMatch = entry.getValue();
          minDiff = diff;
        }
      }
    }
    if (bestMatch == null) {
      String msg = "mappings: " + (mapping.size() < 5 ? mapping : mapping.size());
      throw new RuntimeException("Could not map " + plainTextPosition + " to original position. isToPos: " + isToPos + ", " + msg);
    }
    // we remove markup total length if usage of fake markup and need from position
    if (!isToPos && bestMatch.getFakeMarkupLength() > 0) {
      minDiff = bestMatch.getFakeMarkupLength();
    }
    // We assume that when we have found the closest match there's a one-to-one mapping
    // in this region, thus we can subtract 'minDiff' to get the exact position.
    // If the bestMatch is a fakeMarkup, subtract it:
    return bestMatch.getTotalPosition() - minDiff;
  }
  
  /**
   * @since 3.9
   */
  public String getGlobalMetaData(String key, String defaultValue) {
    return customMetaData.getOrDefault(key, defaultValue);
  }

  /**
   * @since 3.9
   */
  public String getGlobalMetaData(MetaDataKey key, String defaultValue) {
    return metaData.getOrDefault(key, defaultValue);
  }

  /** @since 5.4 */
  public Map<MetaDataKey, String> getGlobalMetaData() {
    return metaData;
  }

  /** @since 5.4 */
  public Map<String, String> getCustomMetaData() {
    return customMetaData;
  }

  @Override
  public String toString() {
    return StringUtils.join(parts, "");
  }

}
