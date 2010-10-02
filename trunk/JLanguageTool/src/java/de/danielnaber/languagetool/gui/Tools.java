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

import java.awt.Frame;
import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import de.danielnaber.languagetool.tools.StringTools;

/**
 * GUI-related tools.
 * 
 * @author Daniel Naber
 */
public class Tools {

  private static final int DEFAULT_CONTEXT_SIZE = 40; // characters
  private static final String MARKER_START = "<b><font color=\"red\">";
  private static final String MARKER_END = "</font></b>";

  private Tools() {
    // no constructor
  }

  public static String makeTexti18n(final ResourceBundle messages, final String key,
      final Object[] messageArguments) {
    final MessageFormat formatter = new MessageFormat("");
    formatter.applyPattern(messages.getString(key));
    return formatter.format(messageArguments);
  }

  /**
   * Get the default context (40 characters) of the given text range,
   * highlighting the range with HTML.
   */
  public static String getContext(final int fromPos, final int toPos, final String text) {
    return getContext(fromPos, toPos, text, DEFAULT_CONTEXT_SIZE);
  }

  /**
   * Get the context (<code>contextSize</code> characters) of the given text
   * range, highlighting the range with HTML code.
   */
  public static String getContext(final int fromPos, final int toPos, final String fileContents,
      int contextSize) {
    return getContext(fromPos, toPos, fileContents, contextSize, MARKER_START,
        MARKER_END, true);
  }

  /**
   * Get the context (<code>contextSize</code> characters) of the given text
   * range, highlighting the range with the given marker strings, not escaping
   * HTML.
   */
  public static String getContext(final int fromPos, final int toPos,
      final String fileContents, final int contextSize,
      final String markerStart, final String markerEnd) {
    return getContext(fromPos, toPos, fileContents, contextSize, markerStart,
        markerEnd, false);
  }
  /**
   * Get the context (<code>contextSize</code> characters) of the given text
   * range, highlighting the range with the given marker strings.
   * 
   * @param fromPos
   *          the start position of the error in characters
   * @param toPos
   *          the end position of the error in characters
   * @param text
   *          the text from which the context should be taken
   * @param contextSize
   *          the size of the context in characters
   * @param markerStart
   *          the string used to mark the beginning of the error
   * @param markerEnd
   *          the string used to mark the end of the error
   * @param escapeHTML
   *          whether HTML/XML characters should be escaped
   */
  public static String getContext(final int fromPos, final int toPos,
      String text, final int contextSize, final String markerStart,
      final String markerEnd, final boolean escapeHTML) {
    text = text.replace('\n', ' ');
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
    final int fileLen = text.length();
    if (endContent > fileLen) {
      postfix = "";
      endContent = fileLen;
    }
    // make "^" marker. inefficient but robust implementation:
    final StringBuilder marker = new StringBuilder();
    final int totalLen = fileLen + prefix.length();
    for (int i = 0; i < totalLen; i++) {
      if (i >= fromPos && i < toPos) {
        marker.append('^');
      } else {
        marker.append(' ');
      }
    }
    // now build context string plus marker:
    final StringBuilder sb = new StringBuilder();
    sb.append(prefix);
    sb.append(text.substring(startContent, endContent));
    final String markerStr = markerPrefix
        + marker.substring(startContent, endContent);
    sb.append(postfix);
    final int startMark = markerStr.indexOf('^');
    final int endMark = markerStr.lastIndexOf('^');
    String result = sb.toString();
    if (escapeHTML) {
      result = StringTools.escapeHTML(result.substring(0, startMark))
          + markerStart
          + StringTools.escapeHTML(result.substring(startMark, endMark + 1))
          + markerEnd + StringTools.escapeHTML(result.substring(endMark + 1));
    } else {
      result = result.substring(0, startMark) + markerStart
          + result.substring(startMark, endMark + 1) + markerEnd
          + result.substring(endMark + 1);
    }
    return result;
  }

  /**
   * Show a file chooser dialog and return the file selected by the user or
   * <code>null</code>.
   */
  static File openFileDialog(final Frame frame, final FileFilter fileFilter) {
    final JFileChooser jfc = new JFileChooser();
    jfc.setFileFilter(fileFilter);
    jfc.showOpenDialog(frame);
    final File file = jfc.getSelectedFile();
    if (file == null) {
      return null;
    }
    return file;
  }

  /**
   * Show the exception (with stacktrace) in a dialog and print it to STDERR.
   */
  static void showError(final Exception e) {
    final String msg = de.danielnaber.languagetool.tools.Tools
        .getFullStackTrace(e);
    JOptionPane
        .showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    e.printStackTrace();
  }

  /**
   * Show the exception (message without stacktrace) in a dialog and print it to
   * STDERR.
   */
  static void showErrorMessage(final Exception e) {
    final String msg = e.getMessage();
    JOptionPane
        .showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    e.printStackTrace();
  }

}
