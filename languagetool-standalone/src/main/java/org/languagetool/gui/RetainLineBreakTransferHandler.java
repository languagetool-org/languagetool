/**
 * Copyright (C) 2011 Steve McLeod (http://stackoverflow.com/users/2959/steve-mcleod)
 *
 * Source:
 * http://stackoverflow.com/questions/7745087
 *
 * License: http://creativecommons.org/licenses/by-sa/3.0/
 */
package org.languagetool.gui;

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Keep line breaks when copying from JTextPane.
 */
class RetainLineBreakTransferHandler extends TransferHandler {

  @Override
  protected Transferable createTransferable(JComponent c) {
    JEditorPane pane = (JEditorPane) c;
    String htmlText = pane.getText();
    String plainText = extractText(new StringReader(htmlText));
    return new MyTransferable(plainText, htmlText);
  }

  private String extractText(Reader reader) {
    StringBuilder result = new StringBuilder();
    HTMLEditorKit.ParserCallback parserCallback = new HTMLEditorKit.ParserCallback() {
      @Override
      public void handleText(char[] data, int pos) {
        result.append(data);
      }
      @Override
      public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet a, int pos) {
        if (tag.equals(HTML.Tag.BR)) {
          result.append('\n');
        }
      }
    };
    try {
      new ParserDelegator().parse(reader, parserCallback, true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result.toString();
  }


  @Override
  public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
    if (action == COPY) {
      clip.setContents(this.createTransferable(comp), null);
    }
  }

  @Override
  public int getSourceActions(JComponent c) {
    return COPY;
  }

  static class MyTransferable implements Transferable {

    private static final DataFlavor[] supportedFlavors;

    static {
      try {
        supportedFlavors = new DataFlavor[]{
                new DataFlavor("text/html;class=java.lang.String"),
                new DataFlavor("text/plain;class=java.lang.String")
        };
      } catch (ClassNotFoundException e) {
        throw new ExceptionInInitializerError(e);
      }
    }

    private final String plainData;
    private final String htmlData;

    MyTransferable(String plainData, String htmlData) {
      this.plainData = plainData;
      this.htmlData = htmlData;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
      return supportedFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      for (DataFlavor supportedFlavor : supportedFlavors) {
        if (supportedFlavor == flavor) {
          return true;
        }
      }
      return false;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
      if (flavor.equals(supportedFlavors[0])) {
        return htmlData;
      }
      if (flavor.equals(supportedFlavors[1])) {
        return plainData;
      }
      throw new UnsupportedFlavorException(flavor);
    }
  }

}
