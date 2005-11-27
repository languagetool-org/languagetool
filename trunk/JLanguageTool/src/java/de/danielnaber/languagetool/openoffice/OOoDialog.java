/*
 * Created on 27.11.2005
 */
package de.danielnaber.languagetool.openoffice;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Tools;
import de.danielnaber.languagetool.rules.RuleMatch;

public class OOoDialog {

  private JDialog dialog = null;
  private JTextPane resultArea = null;
  
  OOoDialog() {
  }
  
  void show(List ruleMatches, String text) {
    dialog = new JDialog();
    dialog.setTitle("JLanguageTool Output");
    Container contentPane = dialog.getContentPane();
    contentPane.setLayout(new GridBagLayout());

    resultArea = new JTextPane();
    resultArea.setContentType("text/html");

    GridBagConstraints cons = new GridBagConstraints();
    cons.fill = GridBagConstraints.BOTH;
    cons.weightx = 2.0f;
    cons.weighty = 2.0f;
    cons.gridx = 0;
    cons.gridy = 0;
    contentPane.add(new JScrollPane(resultArea), cons);

    StringBuffer sb = new StringBuffer();
    System.err.println(ruleMatches);
    sb.append(ruleMatches.size() + " matches<br>\n\n");
    int i = 0;
    for (Iterator iter = ruleMatches.iterator(); iter.hasNext();) {
      // partly taken from the other GUI class:
      RuleMatch match = (RuleMatch) iter.next();
      String msg = match.getMessage();
      msg = msg.replaceAll("<suggestion>", "<b>");
      msg = msg.replaceAll("</suggestion>", "</b>");
      msg = msg.replaceAll("<old>", "<b>");
      msg = msg.replaceAll("</old>", "</b>");
      sb.append("<br>\n<b>" +(i+1)+ ".</b> ");
      sb.append("<b>Message:</b> " + msg + "<br>\n");
      sb.append("<b>Context:</b> " + Tools.getContext(match.getFromPos(), match.getToPos(), text));
      sb.append("<br>\n");
      i++;
    }
    resultArea.setText(sb.toString());

    dialog.pack();
    dialog.setSize(500, 500);
    dialog.setVisible(true);
  }
  
  /** Testing only.
   */
  public static void main(String[] args) throws IOException {
    OOoDialog prg = new OOoDialog();
    JLanguageTool lt = new JLanguageTool(Language.ENGLISH);
    String text = "this is a test";
    List ruleMatches = lt.check(text);
    prg.show(ruleMatches, text);
  }

}
