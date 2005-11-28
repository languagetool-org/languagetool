/*
 * Created on 27.11.2005
 */
package de.danielnaber.languagetool.openoffice;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Tools;
import de.danielnaber.languagetool.rules.RuleMatch;

public class OOoDialog implements ActionListener {

  private final static String CHANGE_TEXT_BUTTON = "Change text";

  private static final String PREV_BUTTON = "<";
  private static final String NEXT_BUTTON = ">";
  
  private JDialog dialog = null;
  private JTextPane resultArea = null;
  private XTextDocument xTextDoc = null;
  private List ruleMatches = null;
  private String text = null;

  private RuleMatch currentRuleMatch = null;
  private int currentRuleMatchPos = 0;

  OOoDialog(XTextDocument xTextDoc, List ruleMatches, String text) {
    this.xTextDoc = xTextDoc;
    this.ruleMatches = ruleMatches;
    this.text = text;
  }
  
  void show() {
    dialog = new JDialog();
    dialog.setTitle("JLanguageTool/OOo ALPHA Version");
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
    cons.gridwidth = 2;
    contentPane.add(new JScrollPane(resultArea), cons);
    cons.gridwidth = 1;
    cons.weightx = 1.0f;
    cons.weighty = 1.0f;
    cons.gridx = 0;
    cons.gridy = 1;
    JButton prevButton = new JButton(PREV_BUTTON);
    prevButton.addActionListener(this);
    contentPane.add(prevButton, cons);

    cons.gridx = 1;
    cons.gridy = 1;
    JButton nextButton = new JButton(NEXT_BUTTON);
    nextButton.addActionListener(this);
    contentPane.add(nextButton, cons);

    cons.gridx = 0;
    cons.gridy = 2;
    cons.gridwidth = 2;
    JButton button = new JButton(CHANGE_TEXT_BUTTON);
    button.addActionListener(this);
    contentPane.add(button, cons);
    
    showError(0);

    dialog.pack();
    dialog.setSize(500, 500);
    dialog.setVisible(true);
  }
  
  private void showError(int i) {
    RuleMatch match = (RuleMatch) ruleMatches.get(i);
    currentRuleMatch = match;
    currentRuleMatchPos = i;
    // partly taken from the other GUI class:
    String msg = match.getMessage();
    msg = msg.replaceAll("<suggestion>", "<b>");
    msg = msg.replaceAll("</suggestion>", "</b>");
    msg = msg.replaceAll("<old>", "<b>");
    msg = msg.replaceAll("</old>", "</b>");
    StringBuffer sb = new StringBuffer();
    sb.append(ruleMatches.size() + " matches<br>\n\n");
    sb.append("<br>\n<b>" +(i+1)+ ".</b> ");
    sb.append("<b>Message:</b> " + msg + "<br>\n");
    sb.append("<b>Context:</b> " + Tools.getContext(match.getFromPos(), match.getToPos(), text));
    sb.append("<br>\n");
    resultArea.setText(sb.toString());
  }

  public void actionPerformed(ActionEvent event) {
    if (event.getActionCommand().equals(CHANGE_TEXT_BUTTON)) {
      XText text = xTextDoc.getText();
      XTextCursor cursor = text.createTextCursor();
      cursor.gotoStart(false);
      cursor.goRight((short)currentRuleMatch.getFromPos(), false);
      // FIXME: what if cast fails?
      short errorLength = (short)(currentRuleMatch.getToPos()-currentRuleMatch.getFromPos());
      cursor.goRight(errorLength, true);
      String msgText = currentRuleMatch.getMessage();
      Pattern pattern = Pattern.compile("<em>(.*?)</em>");
      Matcher matcher = pattern.matcher(msgText);
      if (matcher.find()) {
        cursor.setString(matcher.group(1));
        // FIXME: correct position of replacements for upcoming errors!
        int correction = errorLength - matcher.group(1).length();
        System.err.println("corr=" + correction);
      } else {
        System.err.println("No replacement found in message: "+ msgText);
      }
    } else if (event.getActionCommand().equals(PREV_BUTTON)) {
      currentRuleMatchPos--;
      showError(currentRuleMatchPos);
    } else if (event.getActionCommand().equals(NEXT_BUTTON)) {
      currentRuleMatchPos++;
      showError(currentRuleMatchPos);
    } else {
      System.err.println("Unknown action: " + event);
    }
  }

  /** Testing only.
   */
  public static void main(String[] args) throws IOException {
    JLanguageTool lt = new JLanguageTool(Language.ENGLISH);
    String text = "this is a test";
    List ruleMatches = lt.check(text);
    OOoDialog prg = new OOoDialog(null, ruleMatches, text);
    prg.show();
  }

}
