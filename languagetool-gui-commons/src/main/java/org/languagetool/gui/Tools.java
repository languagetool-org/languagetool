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
package org.languagetool.gui;

import org.languagetool.tools.ContextTools;

import java.awt.*;
import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * GUI-related tools.
 * 
 * @author Daniel Naber
 */
public class Tools {

  private static final int DEFAULT_CONTEXT_SIZE = 40; // characters
  private static final String MARKER_START = "<b><font bgcolor=\"#ff8b8b\">";
  private static final String MARKER_END = "</font></b>";

  private Tools() {
    // no public constructor
  }

  public static String makeTexti18n(final ResourceBundle messages, final String key,
                                    final Object... messageArguments) {
    final MessageFormat formatter = new MessageFormat("");
    formatter.applyPattern(messages.getString(key).replaceAll("'", "''"));
    return formatter.format(messageArguments);
  }

  /**
   * Get the default context (40 characters) of the given text range,
   * highlighting the range with HTML.
   * @deprecated use {@link ContextTools}
   */
  public static String getContext(final int fromPos, final int toPos, final String text) {
    return getContext(fromPos, toPos, text, DEFAULT_CONTEXT_SIZE);
  }

  /**
   * Get the context (<code>contextSize</code> characters) of the given text
   * range, highlighting the range with HTML code.
   * @deprecated use {@link ContextTools}
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
   * @deprecated use {@link ContextTools}
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
   * @param fromPos the start position of the error in characters
   * @param toPos the end position of the error in characters
   * @param text the text from which the context should be taken
   * @param contextSize the size of the context in characters
   * @param markerStart the string used to mark the beginning of the error
   * @param markerEnd the string used to mark the end of the error
   * @param escapeHTML whether HTML/XML characters should be escaped
   * @deprecated use {@link ContextTools}
   */
  public static String getContext(final int fromPos, final int toPos,
      String text, final int contextSize, final String markerStart,
      final String markerEnd, final boolean escapeHTML) {
    final ContextTools contextTools = new ContextTools();
    contextTools.setContextSize(contextSize);
    contextTools.setEscapeHtml(escapeHTML);
    contextTools.setErrorMarkerStart(markerStart);
    contextTools.setErrorMarkerEnd(markerEnd);
    return contextTools.getContext(fromPos, toPos, text);
  }

  /**
   * Show a file chooser dialog and return the file selected by the user or
   * <code>null</code>.
   */
  static File openFileDialog(final Frame frame, final FileFilter fileFilter) {
    final JFileChooser jfc = new JFileChooser();
    jfc.setFileFilter(fileFilter);
    jfc.showOpenDialog(frame);
    return jfc.getSelectedFile();
  }

  /**
   * Show the exception (with stacktrace) in a dialog and print it to STDERR.
   */
  static void showError(final Exception e) {
    final String msg = org.languagetool.tools.Tools.getFullStackTrace(e);
    JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    e.printStackTrace();
  }

  /**
   * Show the exception (message without stacktrace) in a dialog and print the
   * stacktrace to STDERR.
   */
  static void showErrorMessage(final Exception e, final Component parent) {
    final String msg = e.getMessage();
    JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    e.printStackTrace();
  }

  /**
   * Show the exception (message without stacktrace) in a dialog and print the
   * stacktrace to STDERR.
   */
  static void showErrorMessage(final Exception e) {
    showErrorMessage(e, null);
  }

  /**
   * LibO shortens menu items with more than ~100 characters by dropping text in the middle.
   * That isn't really sensible, so we shorten the text here in order to preserve the important parts.
   */
  public static String shortenComment(String comment) {
    final int maxCommentLength = 100;
    if(comment.length() > maxCommentLength) {
      // if there is text in brackets, drop it (beginning at the end)
      while (comment.lastIndexOf(" [") > 0
              && comment.lastIndexOf(']') > comment.lastIndexOf(" [")
              && comment.length() > maxCommentLength) {
        comment = comment.substring(0,comment.lastIndexOf(" [")) + comment.substring(comment.lastIndexOf(']')+1);
      }
      while (comment.lastIndexOf(" (") > 0
              && comment.lastIndexOf(')') > comment.lastIndexOf(" (")
              && comment.length() > maxCommentLength) {
        comment = comment.substring(0,comment.lastIndexOf(" (")) + comment.substring(comment.lastIndexOf(')')+1);
      }
      // in case it's still not short enough, shorten at the end
      if(comment.length() > maxCommentLength) {
        comment = comment.substring(0,maxCommentLength-1) + "…";
      }
    }
    return comment;
  }

  /**
   * Returns translation of the UI element without the control character {@code &}. To
   * have {@code &} in the UI, use {@code &&}.
   *
   * @param label Label to convert.
   * @return String UI element string without mnemonics.
   */
  public static String getLabel(final String label) {
    return label.replaceAll("&([^&])", "$1").replaceAll("&&", "&");
  }

  /**
   * Returns the UI element string with mnemonics encoded in OpenOffice.org
   * convention (using {@code ~}).
   *
   * @param label Label to convert
   * @return String UI element with {@code ~} replacing {@code &}.
   */
  public static String getOOoLabel(final String label) {
    return label.replaceAll("&([^&])", "~$1").replaceAll("&&", "&");
  }

  /**
   * Returns mnemonic of a UI element.
   *
   * @param label String Label of the UI element
   * @return Mnemonic of the UI element, or {@code \u0000} in case of no mnemonic set.
   */
  public static char getMnemonic(final String label) {
    int mnemonicPos = label.indexOf('&');
    while (mnemonicPos != -1 && mnemonicPos == label.indexOf("&&")
            && mnemonicPos < label.length()) {
      mnemonicPos = label.indexOf('&', mnemonicPos + 2);
    }
    if (mnemonicPos == -1 || mnemonicPos == label.length()) {
      return '\u0000';
    }
    return label.charAt(mnemonicPos + 1);
  }

}
