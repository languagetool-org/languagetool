/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Panagiotis Minos
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

import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.languagetool.JLanguageTool;

/**
 * A font chooser dialog
 *
 * @author Panagiotis Minos
 * @since 2.6
 */
class FontChooser extends JDialog implements ActionListener,
        DocumentListener, ListSelectionListener {

  private static final Integer[] fontSizesArray = {
    6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 20, 22, 24, 26, 28, 32
  };
  private static final String ACTION_COMMAND_OK = "OK";
  private static final String ACTION_COMMAND_CANCEL = "CANCEL";  
  private static final String ACTION_COMMAND_RESET = "RESET";  
  private String[] fontStylesArray;

  private final ResourceBundle messages;
  private JTextField fontNameTextField;
  private JTextField fontStyleTextField;
  private JTextField fontSizeTextField;
  private JList<String> fontNameList;
  private JList<String> fontStyleList;
  private JList<Integer> fontSizeList;
  private JTextArea previewArea;

  private Font selectedFont;
  private Font defaultFont;

  /**
   * Creates a new font chooser dialog
   *
   * @param owner the {@code Frame} from which the dialog is displayed
   * @param modal specifies whether dialog blocks user input to other top-level
   * windows when shown.
   */
  FontChooser(Frame owner, boolean modal) {
    super(owner, modal);
    messages = JLanguageTool.getMessageBundle();
    initComponents();
  }

  /**
   * Gets the current font value from the font chooser.
   *
   * @return the current font of the font chooser
   */
  Font getSelectedFont() {
    return selectedFont;
  }

  /**
   * Sets the current font of the font chooser to the specified font.
   *
   * @param font the font to be set in the font chooser
   */
  void setSelectedFont(Font font) {
    this.selectedFont = font;
    fontNameList.setSelectedValue(font.getFamily(), true);
    fontStyleList.setSelectedValue(getStyle(font), true);
    fontSizeList.setSelectedValue(font.getSize(), true);
  }

  private void initComponents() {
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "Hide");
    getRootPane().getActionMap().put("Hide", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectedFont = null;
        setVisible(false);
      }
    });
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        selectedFont = null;
        setVisible(false);
      }
    });
    setTitle(messages.getString("FontChooser.title"));
    fontStylesArray = new String[]{
      messages.getString("FontChooser.style.plain"),
      messages.getString("FontChooser.style.bold"),
      messages.getString("FontChooser.style.italic"),
      messages.getString("FontChooser.style.bold_italic")
    };
    String[] fontNamesArray = GraphicsEnvironment
            .getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    getContentPane()
            .setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(4, 4, 4, 4);

    JPanel fontPanel = new JPanel(new GridBagLayout());

    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    JLabel fontNameLabel
            = new JLabel(messages.getString("FontChooser.label.name"));
    fontPanel.add(fontNameLabel, c);

    c.gridx = 1;
    c.gridy = 0;
    JLabel fontStyleLabel
            = new JLabel(messages.getString("FontChooser.label.style"));
    fontPanel.add(fontStyleLabel, c);

    c.gridx = 2;
    c.gridy = 0;
    JLabel fontSizeLabel
            = new JLabel(messages.getString("FontChooser.label.size"));
    fontPanel.add(fontSizeLabel, c);

    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    fontNameTextField = new JTextField();
    fontNameTextField.setEnabled(false);
    fontNameTextField.getDocument().addDocumentListener(this);
    fontPanel.add(fontNameTextField, c);
    c.weightx = 0.0;

    c.gridx = 1;
    c.gridy = 1;
    fontStyleTextField = new JTextField();
    fontStyleTextField.setEnabled(false);
    fontStyleTextField.getDocument().addDocumentListener(this);
    fontPanel.add(fontStyleTextField, c);

    c.gridx = 2;
    c.gridy = 1;
    fontSizeTextField = new JTextField();
    fontSizeTextField.setColumns(4);
    fontSizeTextField.getDocument().addDocumentListener(this);
    fontPanel.add(fontSizeTextField, c);

    c.gridx = 0;
    c.gridy = 2;
    c.weightx = 1.0;
    c.weighty = 1;
    c.fill = GridBagConstraints.BOTH;
    fontNameList = new JList<>(fontNamesArray);
    fontNameList.addListSelectionListener(this);
    fontNameList.setVisibleRowCount(5);
    fontNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane fontNameListPane = new JScrollPane(fontNameList,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    fontPanel.add(fontNameListPane, c);

    c.gridx = 1;
    c.gridy = 2;
    c.weightx = 0.5;
    fontStyleList = new JList<>(fontStylesArray);
    fontStyleList.addListSelectionListener(this);
    fontStyleList.setVisibleRowCount(5);
    fontStyleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane fontStyleListPane = new JScrollPane(fontStyleList,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    fontPanel.add(fontStyleListPane, c);

    c.gridx = 2;
    c.gridy = 2;
    fontSizeList = new JList<>(fontSizesArray);
    fontSizeList.addListSelectionListener(this);
    fontSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fontSizeList.setVisibleRowCount(5);
    JScrollPane fontSizeListPane = new JScrollPane(fontSizeList,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    fontPanel.add(fontSizeListPane, c);

    c.insets = new Insets(8, 8, 4, 8);
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;
    c.weighty = 0.4;
    getContentPane().add(fontPanel, c);

    c.insets = new Insets(4, 8, 4, 8);
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 1.0;
    c.weighty = 0.6;
    previewArea = new JTextArea(messages.getString("FontChooser.pangram"));
    previewArea.setLineWrap(true);
    previewArea.setRows(4);
    JScrollPane pane = new JScrollPane(previewArea);
    TitledBorder border = BorderFactory.createTitledBorder(
            messages.getString("FontChooser.preview"));
    pane.setBorder(border);
    getContentPane().add(pane, c);

    JPanel buttonPanel = new JPanel(new GridBagLayout());

    c.insets = new Insets(4, 4, 4, 4);

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.weighty = 0.0;
    c.anchor = GridBagConstraints.LINE_START;
    c.fill = GridBagConstraints.NONE;
    JButton resetButton = new JButton(Tools.getLabel(
            messages.getString("FontChooser.reset")));
    resetButton.setMnemonic(Tools.getMnemonic(
            messages.getString("FontChooser.reset")));
    resetButton.setActionCommand(ACTION_COMMAND_RESET);
    resetButton.addActionListener(this);
    buttonPanel.add(resetButton, c);

    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.anchor = GridBagConstraints.LINE_END;
    c.fill = GridBagConstraints.NONE;
    JButton cancelButton = new JButton(Tools.getLabel(
            messages.getString("guiCancelButton")));
    cancelButton.setMnemonic(Tools.getMnemonic(
            messages.getString("guiCancelButton")));
    cancelButton.setActionCommand(ACTION_COMMAND_CANCEL);
    cancelButton.addActionListener(this);
    buttonPanel.add(cancelButton, c);

    c.gridx = 2;
    c.gridy = 0;
    JButton okButton = new JButton(Tools.getLabel(
            messages.getString("guiOKButton")));
    okButton.setMnemonic(Tools.getMnemonic(
            messages.getString("guiOKButton")));
    okButton.setActionCommand(ACTION_COMMAND_OK);
    okButton.addActionListener(this);
    buttonPanel.add(okButton, c);

    c.insets = new Insets(4, 8, 8, 8);
    c.gridx = 0;
    c.gridy = 2;
    c.anchor = GridBagConstraints.LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    getContentPane().add(buttonPanel, c);

    this.defaultFont = previewArea.getFont();

    setDefaultFont();

    getRootPane().setDefaultButton(cancelButton);
    this.applyComponentOrientation(
      ComponentOrientation.getOrientation(Locale.getDefault()));
    pack();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (ACTION_COMMAND_CANCEL.equals(e.getActionCommand())) {
      this.selectedFont = null;
      setVisible(false);
    } else if (ACTION_COMMAND_OK.equals(e.getActionCommand())) {
      setVisible(false);
    } else if (ACTION_COMMAND_RESET.equals(e.getActionCommand())) {
      setDefaultFont();
    }
  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) {
      return;
    }

    if (e.getSource() == this.fontNameList) {
      String fontName = this.fontNameList.getSelectedValue();
      if (fontName != null) {
        this.fontNameTextField.setText(fontName);
      }
    } else if (e.getSource() == this.fontStyleList) {
      String fontStyle = this.fontStyleList.getSelectedValue();
      if (fontStyle != null) {
        this.fontStyleTextField.setText(fontStyle);
      }
    } else if (e.getSource() == this.fontSizeList) {
      Integer fontSize = this.fontSizeList.getSelectedValue();
      if (fontSize != null) {
        this.fontSizeTextField.setText(fontSize.toString());
      }
    }
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    updateFont();
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    updateFont();
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    updateFont();
  }

  private void updateFont() {
    String fontName = this.fontNameTextField.getText();
    String styleName = this.fontStyleTextField.getText();
    Integer fontSize = null;
    try {
      fontSize = Integer.parseInt(this.fontSizeTextField.getText());
    } catch (NumberFormatException ex) {
    }
    int style = Font.PLAIN;
    if (fontStylesArray[1].equals(styleName)) {
      style = Font.BOLD;
    } else if (fontStylesArray[2].equals(styleName)) {
      style = Font.ITALIC;
    } else if (fontStylesArray[3].equals(styleName)) {
      style = Font.BOLD | Font.ITALIC;
    }
    if (fontName != null && fontSize != null) {
      Font newFont = new Font(fontName, style, fontSize);
      this.selectedFont = newFont;
      previewArea.setFont(newFont);
    }
  }

  private String getStyle(Font font) {
    switch (font.getStyle()) {
      case Font.PLAIN:
        return fontStylesArray[0];
      case Font.BOLD:
        return fontStylesArray[1];
      case Font.ITALIC:
        return fontStylesArray[2];
      case Font.BOLD | Font.ITALIC:
        return fontStylesArray[3];
      default:
        return fontStylesArray[0];
    }
  }

  private void setDefaultFont() {
    this.selectedFont = defaultFont;
    fontNameList.setSelectedValue(defaultFont.getFontName(), true);
    fontStyleList.setSelectedValue(getStyle(defaultFont), true);
    fontSizeList.setSelectedValue(defaultFont.getSize(), true);
  }
}
