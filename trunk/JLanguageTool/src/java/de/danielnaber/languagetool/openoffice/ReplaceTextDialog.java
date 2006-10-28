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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;

class ReplaceTextDialog implements ActionListener {

  private static final String OK_BUTTON = "OK";
  private static final String CANCEL_BUTTON = "Cancel";
  private static final String TITLE = "Replace text";
  
  private JDialog dialog = null;
  private JTextField textField = null;
  private String replaceText = null;
  
  ReplaceTextDialog() {
    dialog = new JDialog();
    dialog.setTitle(TITLE);
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
    JButton okButton = new JButton(OK_BUTTON);
    okButton.addActionListener(this);
    contentPane.add(okButton, cons);

    cons.gridx = 1;
    cons.gridy = 1;
    JButton cancelButton = new JButton(CANCEL_BUTTON);
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
    if (OK_BUTTON.equals(event.getActionCommand()) || 
        event.getActionCommand() == null) {       // returned was pressed
      replaceText = textField.getText();
    } else if (CANCEL_BUTTON.equals(event.getActionCommand())) {
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
