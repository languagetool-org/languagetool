/* JLanguageTool, a natural language style checker 
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
package de.danielnaber.languagetool.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * A simple GUI to check texts with.
 * 
 * @author Daniel Naber
 */
class Main implements ActionListener {

  private static final int CONTEXT_SIZE = 25;
  
  private static final String OPTIONS_BUTTON = "Options...";
  
  private JTextArea textArea = null;
  private JTextPane resultArea = null;
  private JComboBox langBox = null;
  
  private Map configDialogs = new HashMap();       // Language -> ConfigurationDialog

  private Main() {
  }

  private void createAndShowGUI() {
    JFrame frame = new JFrame("JLanguageTool Demo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    textArea = new JTextArea("This is a example input to to show you how JLanguageTool works. " +
        "Note, however, that it does not include a spell checka.");
    // TODO: wrong line number is displayed for lines that are wrapped automatically:
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    resultArea = new JTextPane();
    resultArea.setContentType("text/html");
    resultArea.setText("Results will appear here");
    JLabel label = new JLabel("Please type or paste text to check in the top area");
    JButton button = new JButton("Check text");
    button.setMnemonic('c'); 
    button.addActionListener(this);

    JButton configButton = new JButton(OPTIONS_BUTTON);
    configButton.setMnemonic('o'); 
    configButton.addActionListener(this);

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints buttonCons = new GridBagConstraints();
    buttonCons.gridx = 0;
    buttonCons.gridy = 0;
    panel.add(button, buttonCons);
    buttonCons.gridx = 1;
    buttonCons.gridy = 0;
    panel.add(new JLabel(" in: "), buttonCons);
    buttonCons.gridx = 2;
    buttonCons.gridy = 0;
    langBox = new JComboBox(Language.LANGUAGES);
    panel.add(langBox, buttonCons);
    buttonCons.gridx = 3;
    buttonCons.gridy = 0;
    buttonCons.insets = new Insets(0, 10, 0, 0);
    panel.add(configButton, buttonCons);

    Container contentPane = frame.getContentPane();
    GridBagLayout gridLayout = new GridBagLayout();
    contentPane.setLayout(gridLayout);
    GridBagConstraints cons = new GridBagConstraints();
    cons.fill = GridBagConstraints.BOTH;
    cons.weightx = 10.0f;
    cons.weighty = 10.0f;
    cons.gridx = 0;
    cons.gridy = 0;
    contentPane.add(new JScrollPane(textArea), cons);
    cons.gridy = 1;
    cons.weighty = 5.0f;
    contentPane.add(new JScrollPane(resultArea), cons);

    cons.fill = GridBagConstraints.NONE;
    cons.gridx = 0;
    cons.gridy = 2;
    cons.weighty = 0.0f;
    cons.insets = new Insets(3,3,3,3);
    //cons.fill = GridBagConstraints.NONE;
    contentPane.add(label, cons);
    cons.gridy = 3;
    contentPane.add(panel, cons);
    
    frame.pack();
    frame.setSize(600, 600);
    frame.setVisible(true);
  }
  
  public void actionPerformed(ActionEvent e) {
    String langName = langBox.getSelectedItem().toString();
    Language language = Language.getLanguageforName(langName);
    ConfigurationDialog configDialog = null;
    if (configDialogs.containsKey(language)) {
      configDialog = (ConfigurationDialog)configDialogs.get(language);
    } else {
      configDialog = new ConfigurationDialog();
      configDialogs.put(language, configDialog);
    }
    JLanguageTool langTool;
    try {
      langTool = new JLanguageTool(language);
      langTool.activateDefaultPatternRules();
      Set disabledRules = configDialog.getdisabledRuleIds();
      if (disabledRules != null) {
        for (Iterator iter = disabledRules.iterator(); iter.hasNext();) {
          String ruleId = (String) iter.next();
          langTool.disableRule(ruleId);
        }
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } catch (ParserConfigurationException ex) {
      throw new RuntimeException(ex);
    } catch (SAXException ex) {
      throw new RuntimeException(ex);
    }
    if (e.getActionCommand().equals(OPTIONS_BUTTON)) {
      List rules = langTool.getAllRules();
      configDialog.show(rules);
    } else {
      if (textArea.getText().trim().equals("")) {
        textArea.setText("Please insert text to check here");
      } else {
        StringBuffer sb = new StringBuffer();
        resultArea.setText("Starting check...<br>\n");
        resultArea.repaint(); // FIXME: why doesn't this work?
        //TODO: resultArea.setCursor(new Cursor(Cursor.WAIT_CURSOR)); 
        sb.append("Starting check in " +langName+ "...<br>\n");
        int matches = 0;
        try {
          matches = checkText(langTool, language, textArea.getText(), sb);
        } catch (Exception ex) {
          sb.append("<br><br><b><font color=\"red\">" + ex.toString() + "<br>");
          StackTraceElement[] elements = ex.getStackTrace();
          for (int i = 0; i < elements.length; i++) {
            sb.append(elements[i] + "<br>");
          }
          sb.append("</font></b><br>");
          ex.printStackTrace();
        }
        sb.append("Check done. " +matches+ " potential problems found<br>\n");
        resultArea.setText(sb.toString());
        resultArea.setCaretPosition(0);
      }
    }
  }
  
  private int checkText(JLanguageTool langTool, Language language, String text, StringBuffer sb) throws IOException,
      ParserConfigurationException, SAXException {
    long startTime = System.currentTimeMillis();
    File defaultPatternFile = new File(JLanguageTool.RULES_DIR + File.separator
        + language.getShortName() + File.separator + JLanguageTool.PATTERN_FILE);
    List patternRules = new ArrayList();
    if (defaultPatternFile.exists()) {
      patternRules = langTool.loadPatternRules(defaultPatternFile.getAbsolutePath());
    } else {
      sb.append("Pattern file " + defaultPatternFile.getAbsolutePath() + " not found<br>\n");
    }
    for (Iterator iter = patternRules.iterator(); iter.hasNext();) {
      Rule rule = (Rule) iter.next();
      langTool.addRule(rule);
    }
    List ruleMatches = langTool.check(text);
    long startTimeMatching = System.currentTimeMillis();
    int i = 0;
    for (Iterator iter = ruleMatches.iterator(); iter.hasNext();) {
      RuleMatch match = (RuleMatch) iter.next();
      sb.append("<br>\n<b>" +(i+1)+ ". Line " + (match.getLine() + 1) + ", column " + match.getColumn()
          + "</b><br>\n");
      String msg = match.getMessage();
      msg = msg.replaceAll("<suggestion>", "<b>");
      msg = msg.replaceAll("</suggestion>", "</b>");
      msg = msg.replaceAll("<old>", "<b>");
      msg = msg.replaceAll("</old>", "</b>");
      sb.append("<b>Message:</b> " + msg + "<br>\n");
      sb.append("<b>Context:</b> " + getContext(match.getFromPos(), match.getToPos(), text));
      sb.append("<br>\n");
      i++;
    }
    long endTime = System.currentTimeMillis();
    sb.append("<br>\nTime: " + (endTime - startTime) + "ms (including "
        + (endTime - startTimeMatching) + "ms for rule matching)<br>\n");
    return ruleMatches.size();
  }

  private static String getContext(int fromPos, int toPos, String fileContents) {
    fileContents = fileContents.replaceAll("\n", " ");
    // calculate context region:
    int startContent = fromPos - CONTEXT_SIZE;
    String prefix = "...";
    String postfix = "...";
    String markerPrefix = "   ";
    if (startContent < 0) {
      prefix = "";
      markerPrefix = "";
      startContent = 0;
    }
    int endContent = toPos + CONTEXT_SIZE;
    if (endContent > fileContents.length()) {
      postfix = "";
      endContent = fileContents.length();
    }
    // make "^" marker. inefficient but robust implementation:
    StringBuffer marker = new StringBuffer();
    for (int i = 0; i < fileContents.length() + prefix.length(); i++) {
      if (i >= fromPos && i < toPos)
        marker.append("^");
      else
        marker.append(" ");
    }
    // now build context string plus marker:
    StringBuffer sb = new StringBuffer();
    sb.append(prefix);
    sb.append(fileContents.substring(startContent, endContent));
    sb.append(postfix);
    String markerStr = markerPrefix + marker.substring(startContent, endContent);
    int startMark = markerStr.indexOf("^");
    int endMark = markerStr.lastIndexOf("^");
    String result = sb.toString();
    result = result.substring(0, startMark) + "<b><font color=\"red\">" + 
      result.substring(startMark, endMark+1) + "</font></b>" + result.substring(endMark+1);
    //String result = sb.s
    return result;
  }

  public static void main(String[] args) {
    final Main prg = new Main();
    prg.createAndShowGUI();
  }
  
}
