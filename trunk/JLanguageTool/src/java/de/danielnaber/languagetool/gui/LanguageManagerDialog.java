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

/**
 * Dialog for managing externally loaded rules.
 * 
 * @author Daniel Naber
 */
public class LanguageManagerDialog implements ActionListener {

  private JDialog dialog;
  
  private JList list; 
  private JButton addButton;
  private JButton removeButton;
  private JButton closeButton;
  private final List<File> ruleFiles = new ArrayList<File>();
  
  private final Frame owner;
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
    dialog.setTitle(Messages.getString("guiLanguageManagerDialog"));   // FIXME: i18n //$NON-NLS-1$
    
    // close dialog when user presses Escape key:
    // TODO: taken from ConfigurationDialog, avoid duplication:
    final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    final ActionListener actionListener = new ActionListener() {
      @Override
      @SuppressWarnings("unused")
      public void actionPerformed(ActionEvent actionEvent) {
        dialog.setVisible(false); 
      }
    };
    final JRootPane rootPane = dialog.getRootPane();
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
    addButton = new JButton(Messages.getString("guiAddButton"));
    addButton.addActionListener(this);
    cons.gridx = 1;
    cons.gridy = 0;
    buttonPanel.add(addButton, cons);

    removeButton = new JButton(Messages.getString("guiRemoveButton"));
    removeButton.addActionListener(this);
    cons.gridx = 1;
    cons.gridy = 1;
    buttonPanel.add(removeButton, cons);

    closeButton = new JButton(Messages.getString("guiCloseButton"));
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
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final Dimension frameSize = dialog.getSize();
    dialog.setLocation(screenSize.width/2 - (frameSize.width/2), screenSize.height/2 - (frameSize.height/2));
    dialog.setVisible(true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == addButton) {
      final File ruleFile = Tools.openFileDialog(null, new XMLFileFilter());
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
      throw new IllegalArgumentException("Don't know how to handle " + e); //$NON-NLS-1$
    }
  }
  
  /**
   * Return all external Languages.
   */
  List<Language> getLanguages() {
    final List<Language> languages = new ArrayList<Language>();
    for (File ruleFile : ruleFiles) {
      if (ruleFile != null) {
        final Language newLanguage = LanguageBuilder.makeAdditionalLanguage(ruleFile);
        languages.add(newLanguage);
      }
    }
    return languages;
  }
  
  static class XMLFileFilter extends FileFilter {
    @Override
    public boolean accept(final File f) {
      if (f.getName().toLowerCase().endsWith(".xml") || f.isDirectory()) { //$NON-NLS-1$
        return true;
      }
      return false;
    }
    @Override
    public String getDescription() {
      return "*.xml"; //$NON-NLS-1$
    }
  }

}
