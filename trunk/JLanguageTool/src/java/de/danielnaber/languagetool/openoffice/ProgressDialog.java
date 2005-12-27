/*
 * Created on 24.12.2005
 */
package de.danielnaber.languagetool.openoffice;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

class ProgressDialog extends JFrame {

  public ProgressDialog() {
    setTitle("-Dmitri v3- Starting LanguageTool...");
    JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JProgressBar progressBar = new JProgressBar(0, 100);
    progressBar.setIndeterminate(true);
    progressPanel.add(progressBar);
    setContentPane(progressPanel);
    pack();
    setSize(400,80);
    // center on screen:
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    setLocation(screenSize.width/2 - (frameSize.width/2), screenSize.height/2 - (frameSize.height/2));
    setVisible(true);
  }
  
  void close() {
    setVisible(false);
  }
  
}
