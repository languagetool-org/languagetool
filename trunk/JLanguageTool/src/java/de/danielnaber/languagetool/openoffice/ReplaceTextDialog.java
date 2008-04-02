/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.openoffice;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;

import de.danielnaber.languagetool.tools.StringTools;

class ReplaceTextDialog implements ActionListener {

  private JDialog dialog = null;
  private JTextField textField = null;
  private String replaceText = null;
  private JButton okButton = null;
  private JButton cancelButton = null;
  
  ReplaceTextDialog(ResourceBundle messages) {    
    dialog = new JDialog();
    dialog.setTitle(messages.getString("guiReplaceWindowTitle"));
    Container contentPane = dialog.getContentPane();
    contentPane.setLayout(new GridBagLayout());

    GridBagConstraints cons = new GridBagConstraints();
    cons.anchor = GridBagConstraints.NORTHWEST;
    cons.insets = new Insets(4, 4, 4, 4);

    cons.fill = GridBagConstraints.BOTH;
    cons.gridwidth = 2;
    cons.gridx = 0;
    cons.gridy = 0;
    textField = new JTextField();
    contentPane.add(textField, cons);
    
    cons.gridwidth = 1;
    cons.gridx = 0;
    cons.gridy = 1;
    cons.anchor = GridBagConstraints.SOUTH;
    okButton = new JButton(StringTools.getLabel(
        messages.getString("guiOKButton")));
    okButton.setMnemonic(StringTools.getMnemonic(
        messages.getString("guiOKButton")));
    okButton.addActionListener(this);
    contentPane.add(okButton, cons);

    cons.gridx = 1;
    cons.gridy = 1;
    cancelButton = new JButton(StringTools.getLabel(
        messages.getString("guiCancelButton")));
    cancelButton.setMnemonic(StringTools.getMnemonic(
        messages.getString("guiCancelButton")));
    cancelButton.addActionListener(this);
    contentPane.add(cancelButton, cons);

    dialog.pack();
    OOoDialog.bindKey(dialog, KeyEvent.VK_ESCAPE, new EscapeActionListener(dialog));
    OOoDialog.bindKey(dialog, KeyEvent.VK_ENTER, this);
    OOoDialog.centerDialog(dialog);
    dialog.setModal(true);
    dialog.setVisible(true);
  }

  public void actionPerformed(ActionEvent event) {
    if (event.getSource() == okButton || 
        event.getActionCommand() == null) {       // returned was pressed
      replaceText = textField.getText();
    } else if (event.getSource() == cancelButton) {
      // nothing
    } else {
      System.err.println("Unknown action: " + event);
    }
    dialog.setVisible(false);
  }

  String getText() {
    return replaceText;
  }

}
