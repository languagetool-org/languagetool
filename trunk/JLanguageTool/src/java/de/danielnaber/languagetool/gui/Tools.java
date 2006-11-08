/* LanguageTool, a natural language style checker 
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
package de.danielnaber.languagetool.gui;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * GUI-related tools.
 * 
 * @author Daniel Naber
 */
public class Tools {

  private static final int DEFAULT_CONTEXT_SIZE = 40;   // characters
  private static final String MARKER_START = "<b><font color=\"red\">";
  private static final String MARKER_END = "</font></b>";

  private Tools() {
    // no constructor
  }
  
  public static String makeTexti18n(ResourceBundle messages, String key, Object[] messageArguments) {
    MessageFormat formatter = new MessageFormat("");
    formatter.applyPattern(messages.getString(key));
    return formatter.format(messageArguments);
  }

  /**
   * Get the default context (40 characters) of the given text range,
   * highlighting the range with HTML.
   */
  public static String getContext(int fromPos, int toPos, String text) {
    return getContext(fromPos, toPos, text, DEFAULT_CONTEXT_SIZE);
  }

  /**
   * Get the context (<code>contextSize</code> characters) of the given text range,
   * highlighting the range with HTML code.
   */
  public static String getContext(int fromPos, int toPos, String fileContents, int contextSize) {
    return getContext(fromPos, toPos, fileContents, contextSize, MARKER_START, MARKER_END);
  }
  
  /**
   * Get the context (<code>contextSize</code> characters) of the given text range,
   * highlighting the range with the given marker strings.
   */
  public static String getContext(int fromPos, int toPos, String fileContents, int contextSize,
      String markerStart, String markerEnd) {
    fileContents = fileContents.replaceAll("\n", " ");
    // calculate context region:
    int startContent = fromPos - contextSize;    
    String prefix = "...";
    String postfix = "...";
    String markerPrefix = "   ";
    if (startContent < 0) {
      prefix = "";
      markerPrefix = "";
      startContent = 0;
    }
    int endContent = toPos + contextSize;
    int fileLen = fileContents.length();
    if (endContent > fileLen ) {
      postfix = "";
      endContent = fileLen;
    }
    // make "^" marker. inefficient but robust implementation:
    StringBuilder marker = new StringBuilder();
    int totalLen = fileLen + prefix.length();
    for (int i = 0; i < totalLen; i++) {
      if (i >= fromPos && i < toPos)
        marker.append("^");
      else
        marker.append(" ");
    }
    // now build context string plus marker:
    StringBuilder sb = new StringBuilder();
    sb.append(prefix);
    sb.append(fileContents.substring(startContent, endContent));
    String markerStr = markerPrefix + marker.substring(startContent, endContent);
    sb.append(postfix);
    int startMark = markerStr.indexOf("^");
    int endMark = markerStr.lastIndexOf("^");
    String result = sb.toString();
    result = result.substring(0, startMark) + markerStart + 
      result.substring(startMark, endMark+1) + markerEnd + result.substring(endMark+1);
    return result;
  }

}
