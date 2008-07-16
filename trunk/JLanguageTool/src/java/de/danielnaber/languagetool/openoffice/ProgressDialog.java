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
package de.danielnaber.languagetool.openoffice;

import java.awt.FlowLayout;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

class ProgressDialog extends JDialog implements ProgressInformation {

  private JProgressBar progressBar = null;
  
  ProgressDialog(final ResourceBundle messages) {
    setTitle(messages.getString("guiProgressWindowTitle"));
    final JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    progressBar = new JProgressBar();
    progressPanel.add(progressBar);
    setContentPane(progressPanel);
    pack();
    setSize(400,80);
    OOoDialog.centerDialog(this);
    setVisible(true);
    setModal(true);
    setAlwaysOnTop(true);
  }

  public void setMaxProgress(final int maxVal) {
    progressBar.setMaximum(maxVal);
  }

  public void setProgress(final int progress) {
    progressBar.setValue(progress);
  }

  void close() {
    setVisible(false);
  }
  
}
