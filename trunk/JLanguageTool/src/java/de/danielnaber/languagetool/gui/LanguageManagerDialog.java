/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.language.LanguageBuilder;

public class LanguageManagerDialog implements ActionListener {

  private JDialog dialog = null;
  
  private JList list = null; 
  private JButton addButton = null;
  private JButton removeButton = null;
  private JButton closeButton = null;
  private List<File> ruleFiles = new ArrayList<File>();
  
  private Frame owner = null;
  //private ResourceBundle messages = null;
  
  public LanguageManagerDialog(Frame owner, List<Language> languages) {
    this.owner = owner;
    for (Language lang : languages) {
      ruleFiles.add(new File(lang.getRuleFileName()));
    }
    //messages = JLanguageTool.getMessageBundle();
  }
  
  public void show() {
    dialog = new JDialog(owner, true);
    dialog.setTitle("Language Module Manager");   // FIXME: i18n
    
    // close dialog when user presses Escape key:
    // TODO: taken from ConfigurationDialog, avoid duplication:
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    ActionListener actionListener = new ActionListener() {
      @SuppressWarnings("unused")
      public void actionPerformed(ActionEvent actionEvent) {
        dialog.setVisible(false); 
      }
    };
    JRootPane rootPane = dialog.getRootPane();
    rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    final Container contentPane = dialog.getContentPane();
    contentPane.setLayout(new GridBagLayout());
    
    list = new JList(ruleFiles.toArray(new File[]{}));
    GridBagConstraints cons = new GridBagConstraints();
    cons.insets = new Insets(4, 4, 4, 4);
    cons.gridx = 0;
    cons.gridy = 0;
    cons.fill = GridBagConstraints.BOTH;
    cons.weightx = 2.0f;
    cons.weighty = 2.0f;
    contentPane.add(new JScrollPane(list), cons);
    
    cons = new GridBagConstraints();
    cons.insets = new Insets(4, 4, 4, 4);
    cons.fill = GridBagConstraints.HORIZONTAL;
    
    final JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridBagLayout());
    addButton = new JButton("Add...");    // FIXME: i18n
    addButton.addActionListener(this);
    cons.gridx = 1;
    cons.gridy = 0;
    buttonPanel.add(addButton, cons);

    removeButton = new JButton("Remove");    // FIXME: i18n
    removeButton.addActionListener(this);
    cons.gridx = 1;
    cons.gridy = 1;
    buttonPanel.add(removeButton, cons);

    closeButton = new JButton("Close");    // FIXME: i18n
    closeButton.addActionListener(this);
    cons.gridx = 1;
    cons.gridy = 2;
    buttonPanel.add(closeButton, cons);

    cons.gridx = 1;
    cons.gridy = 0;
    cons = new GridBagConstraints();
    cons.anchor = GridBagConstraints.NORTH;
    contentPane.add(buttonPanel, cons);
    
    dialog.pack();
    dialog.setSize(300, 200);
    // center on screen:
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = dialog.getSize();
    dialog.setLocation(screenSize.width/2 - (frameSize.width/2), screenSize.height/2 - (frameSize.height/2));
    dialog.setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == addButton) {
      File ruleFile = Tools.openFileDialog(null, new XMLFileFilter());
      // TODO: avoid duplicate files!
      ruleFiles.add(ruleFile);
      list.setListData(ruleFiles.toArray(new File[]{}));
    } else if (e.getSource() == removeButton) {
      if (list.getSelectedIndex() != -1) {
        ruleFiles.remove(list.getSelectedIndex());
        list.setListData(ruleFiles.toArray(new File[]{}));
      }
    } else if (e.getSource() == closeButton) {
      dialog.setVisible(false);
    } else {
      throw new IllegalArgumentException("Don't know how to handle " + e);
    }
  }
  
  /**
   * Return all external Languages.
   */
  List<Language> getLanguages() {
    List<Language> langs = new ArrayList<Language>();
    for (File ruleFile : ruleFiles) {
      Language newLanguage = LanguageBuilder.makeLanguage(ruleFile);
      if (newLanguage!=null) {
        langs.add(newLanguage);
      }
    }
    return langs;
  }
  
  static class XMLFileFilter extends FileFilter {
    public boolean accept(final File f) {
      if (f.getName().toLowerCase().endsWith(".xml") || f.isDirectory())
        return true;
      return false;
    }
    public String getDescription() {
      return "*.xml";
    }
  }

}
