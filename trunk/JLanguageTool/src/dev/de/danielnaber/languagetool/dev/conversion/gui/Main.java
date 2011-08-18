package de.danielnaber.languagetool.dev.conversion.gui;

import java.awt.Color;
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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import de.danielnaber.languagetool.dev.conversion.CgRuleConverter;
import de.danielnaber.languagetool.dev.conversion.AtdRuleConverter;
import de.danielnaber.languagetool.dev.conversion.RuleConverter;
import de.danielnaber.languagetool.dev.conversion.RuleCoverage;
import de.danielnaber.languagetool.rules.patterns.PatternRule;

public final class Main implements ActionListener {

	private JFrame frame;
	private JTextArea resultArea;
	private JComboBox ruleTypeBox;
	private JComboBox specificRuleTypeBox;
	private JComboBox rulesBox;
	private JButton convert;
	private JButton saveEditedRule;
	private JTextPane coveredByPane;
	private JCheckBox writeCoveredRules;
	private JTextPane mainRuleFilePane;
	private JCheckBox regularRules;
	private JCheckBox disambigRules;
	private JTextPane numRulesPane;
	private JButton checkRulesCoveredButton;
	private JButton writeRulesToFileButton;
	
	private RuleCoverage checker;
	
	private List<? extends Object> ruleObjects;
	private ArrayList<List<String>> allRulesList;
	private ArrayList<String> ruleStrings;
	private ArrayList<String> originalRuleStrings;
	private boolean[] disambigRuleIndices;
	private String[] coveredByList;	// only applies to regular LT rules
	
	private String filename = "";
	private String outfilename = "";
	private String disambigOutFile = "";
	
	private String specificFileType;
	private int numberOfRules = 0;

	private static String cgString = "Constraint Grammar";
	private static String atdString = "After the Deadline";
	
	private static final int WINDOW_WIDTH = 800;
	private static final int WINDOW_HEIGHT = 750;
	
	private void createGUI() {
		// main frame
		frame = new JFrame("Language Tool Rule Converter");
		frame.addWindowListener(new CloseListener());
		frame.setJMenuBar(new MainMenuBar(this));
		setLookAndFeel();
		
		// converted rule area
		resultArea = new JTextArea();
		resultArea.setLineWrap(true);
		resultArea.setWrapStyleWord(true);
		resultArea.requestFocusInWindow();
		JScrollPane scrollPane = new JScrollPane(resultArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(250,145));
		scrollPane.setMinimumSize(new Dimension(10,10));
		
		// original rule combo box
		rulesBox = new JComboBox();
		rulesBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displaySelectedRule();
				displayCoveredBy();
			}
		});
		
		// rule type combo box
		ruleTypeBox = new JComboBox();
		ruleTypeBox.addItem(atdString);
		ruleTypeBox.addItem(cgString);
		ruleTypeBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				populateSpecificRuleType();
			}
		});
		// specific rule type
		specificRuleTypeBox = new JComboBox();
		populateSpecificRuleType();
		
		// rule file pane
		mainRuleFilePane = new JTextPane();
		mainRuleFilePane.setText(filename);
		mainRuleFilePane.setBorder(new LineBorder(Color.BLACK,1));
		
		// convert button
		convert = new JButton("Convert");
		convert.setMnemonic('C');
		convert.addActionListener( new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            convertRuleFile();
	            populateRuleBox();
	        }
	    });
		
		// save rule button
		saveEditedRule =  new JButton("Save rule");
		saveEditedRule.setMnemonic('S');
		saveEditedRule.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveEditedVisibleRule();
			}
		});
		
		// save all rules
		writeRulesToFileButton = new JButton("Write rules to file");
		writeRulesToFileButton.setMnemonic('W');
		writeRulesToFileButton.addActionListener(this);
		
		
		// covered by existing rule pane
		coveredByPane = new JTextPane();
		coveredByPane.setBorder(new LineBorder(Color.BLACK,1));
		
		// display regular and/or disambiguation rules check box
		regularRules = new JCheckBox("Show regular rules",true);
		disambigRules = new JCheckBox("Show disambiguation rules",true);
		regularRules.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				populateRuleBox();
			}
		});
		disambigRules.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				populateRuleBox();
			}
		});
		
		// write covered rules to file check box
		writeCoveredRules = new JCheckBox("Write duplicate rules to file");
		
		// check if all current rules are covered button
		checkRulesCoveredButton = new JButton("Check rule coverage");
		checkRulesCoveredButton.setMnemonic('E');
		checkRulesCoveredButton.addActionListener(this);
		
		// number of rules display
		final JLabel numRulesLabel = new JLabel("Number of rules:");
		numRulesPane = new JTextPane();
		numRulesPane.setText(Integer.toString(numberOfRules));
		
		// add everything into the frame
		final Container contentPane = frame.getContentPane();
		final GridBagLayout gridLayout = new GridBagLayout();
	    contentPane.setLayout(gridLayout);
	    final GridBagConstraints cons = new GridBagConstraints();
	    cons.fill = GridBagConstraints.BOTH;
	    

	    // inside panel to hold all the buttons
	    final GridBagConstraints buttonCons = new GridBagConstraints();
	    final JPanel insidePanel = new JPanel();
	    insidePanel.setOpaque(true);
	    insidePanel.setLayout(new GridBagLayout());
	    
	    insidePanel.setBorder(new LineBorder(Color.BLACK,1));
	    buttonCons.fill = GridBagConstraints.BOTH;
	    
//	    buttonCons.weightx = 1f;
	    
	    // rule type
	    JPanel ruleTypePanel = new JPanel(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
	    ruleTypePanel.add(new JLabel("Rule type:"),c);
	    c.gridx = 1;
	    ruleTypePanel.add(ruleTypeBox,c);
	    c.gridx = 2;
	    ruleTypePanel.add(specificRuleTypeBox,c);

	    insidePanel.add(ruleTypePanel,buttonCons);
	    
	    JPanel displayRulesPanel = new JPanel(new GridBagLayout());
	    c.gridx = 0;
	    displayRulesPanel.add(regularRules,c);
	    c.gridx = 1;
	    displayRulesPanel.add(disambigRules,c);
	    
	    buttonCons.gridy = 1;
	    insidePanel.add(displayRulesPanel,buttonCons);
	    
	    JPanel convertPanel = new JPanel(new GridBagLayout());
	    c.gridx = 0;
	    convertPanel.add(checkRulesCoveredButton,c);
	    c.gridx = 1;
	    convertPanel.add(convert,c);
	    
	    buttonCons.gridy = 2;
	    buttonCons.fill = GridBagConstraints.NONE;
	    insidePanel.add(convertPanel,buttonCons);
	    
	    JPanel savePanel = new JPanel(new GridBagLayout());
	    c.gridx = 0;
	    savePanel.add(saveEditedRule,c);
	    c.gridx = 1;
	    savePanel.add(writeRulesToFileButton,c);
	    
	    buttonCons.gridy = 3;
	    insidePanel.add(savePanel,buttonCons);
	    
	    buttonCons.gridy = 4;
	    insidePanel.add(writeCoveredRules,buttonCons);
	    
	    JPanel numRulesPanel = new JPanel(new GridBagLayout());
	    c.gridx = 0;
	    numRulesPanel.add(numRulesLabel,c);
	    c.gridx = 1;
	    numRulesPanel.add(numRulesPane,c);
	    
	    buttonCons.gridy = 5;
	    insidePanel.add(numRulesPanel,buttonCons);
	    
	    cons.gridx = 0;
	    cons.gridy = 0;
	    cons.ipadx = 1;
	    cons.ipady = 1;
	    JLabel ruleLabel = new JLabel("Original rule:");
	    contentPane.add(ruleLabel,cons);
	    cons.gridx = 0;
	    cons.gridy = 2;
	    JLabel convertedRuleLabel = new JLabel("Converted rule:");
	    contentPane.add(convertedRuleLabel,cons);
	    cons.gridx = 0;
	    cons.gridy = 1;
	    cons.weightx = 10f;
	    cons.weighty = 2f;
	    contentPane.add(rulesBox,cons);
	    cons.gridx = 0;
	    cons.gridy = 3;
	    cons.weightx = 10f;
	    cons.weighty = 10f;
	    cons.ipady = 150;
	    scrollPane.setMinimumSize(new Dimension(0,200));
	    contentPane.add(scrollPane,cons);
	    cons.gridx = 0;
	    cons.gridy = 4;
	    cons.ipady = 0;
	    cons.ipadx = 0;
	    cons.weightx = 0;
	    cons.weighty = 0;
	    cons.anchor = GridBagConstraints.WEST;
	    contentPane.add(new JLabel("Covered by:"),cons);
	    cons.gridy = 5;
	    contentPane.add(coveredByPane,cons);
	    cons.gridy = 6;
	    contentPane.add(insidePanel,cons);
	    cons.gridx = 0;
	    cons.gridy = 7;
	    contentPane.add(new JLabel("Rule file:"),cons);
	    cons.gridy = 8;
	    contentPane.add(mainRuleFilePane,cons);
	    
	    frame.pack();
	    frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
	}
	
	private void setLookAndFeel() {
	    try {
	      for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
	        if ("Nimbus".equals(info.getName())) {
	          UIManager.setLookAndFeel(info.getClassName());
	          break;
	        }
	      }
	    } catch (Exception ignored) {
	      // Well, what can we do...
	    }
	}
	
	private void showGUI() {
	    frame.setVisible(true);
	  }
	
	void quit() {
	    frame.setVisible(false);
	    System.exit(0);
	}
	
	public static void main(final String[] args) {
	    try {
	      final Main prg = new Main();
	      if (args.length > 0) {
	        System.out
	            .println("Usage: java -jar RuleConverterGUI.jar");
	        System.out
	            .println("  no arguments");
	      } else {
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	          @Override
	          public void run() {
	            try {
	              prg.createGUI();
	              prg.showGUI();
	            } catch (final Exception e) {
	              showError(e);
	            }
	          }
	        });
	      }
	    } catch (final Exception e) {
	      showError(e);
	    }
	}
	
	private void displaySelectedRule() {
		if (rulesBox.getSelectedIndex() == -1) {
			resultArea.setText("");
		} else {
			String selectedRule = (String)rulesBox.getSelectedItem();
			for (int i=0;i<originalRuleStrings.size();i++) {
				if (selectedRule.equals(originalRuleStrings.get(i))) {
					resultArea.setText(ruleStrings.get(i));
					break;
				}
			}
			resultArea.repaint();
		}
	}
	
	private void displayCoveredBy() {
		if (rulesBox.getSelectedIndex() == -1) {
			coveredByPane.setText("");
		} else {
			if (coveredByList != null) {
				int index = getCurrentRuleIndex();
				if (!coveredByList[index].isEmpty()) {
					coveredByPane.setText(coveredByList[index]);
					coveredByPane.repaint();
				} else {
					coveredByPane.setText("");
					coveredByPane.repaint();
				}
				
			}
		}
	}
	
	private void convertRuleFile() {
		RuleConverter rc = getCurrentRuleConverter();
		this.rulesBox.removeAllItems();
		try {
			// the generic rule objects, just used for building the other lists
			ruleObjects = rc.getRules();	
			// lists of strings
			allRulesList = rc.getAllLtRules(ruleObjects);
			// rules in LT format, as strings
			ruleStrings = new ArrayList<String>();
			// original rule strings
			originalRuleStrings = new ArrayList<String>();
			// populating the string lists
			for (int i=0;i<ruleObjects.size();i++) {
				Object ruleObject = ruleObjects.get(i);
				String originalString = rc.getRuleAsString(ruleObject);
				originalRuleStrings.add(originalString);
				List<String> ruleList = allRulesList.get(i);
				String ruleString = RuleConverter.getRuleStringFromList(ruleList);
				ruleStrings.add(ruleString);
			}
			// take out exact copies of rules (this messes up navigating through the rules with the arrow keys)
			removeDuplicateRules();
//				if (rc.isDisambiguationRule(ruleObject)) {
//					disambigRuleIndices[i] = true;
//				} else {
//					disambigRuleIndices[i] = false;
//				}
//				coveredByList[i] = "";
			disambigRuleIndices = new boolean[allRulesList.size()];
			// list of existing LT rules that cover the new rules
			coveredByList = new String[allRulesList.size()];
			numberOfRules = allRulesList.size();
			numRulesPane.setText(Integer.toString(numberOfRules));
			
			for (int i=0;i<ruleObjects.size();i++) {
				Object ruleObject = ruleObjects.get(i);
				if (rc.isDisambiguationRule(ruleObject)) {
					disambigRuleIndices[i] = true;
				} else {
					disambigRuleIndices[i] = false;
				}
				coveredByList[i] = "";
			}
			
		} catch (IOException e) {
			showDialog("IOException while loading/parsing file " + filename);
		}
	}
	
	private void removeDuplicateRules() {
		boolean notdone = true;
		while (notdone) {
			for (int i=0;i<originalRuleStrings.size();i++) {
				String originalRuleString = originalRuleStrings.get(i);
				if (originalRuleStrings.subList(i+1, originalRuleStrings.size()).contains(originalRuleString)) {
					originalRuleStrings.remove(i);
					ruleStrings.remove(i);
					allRulesList.remove(i);
					ruleObjects.remove(i);
					break;
				} else {
					if (i == originalRuleStrings.size() - 1) notdone = false;
				}
			}
		}
	}
	
	private void populateRuleBox() {
		rulesBox.removeAllItems();
		if (originalRuleStrings != null) {
			if (regularRules.isSelected() && disambigRules.isSelected()) {
				// add all rules to the rulesBox
				for (String r : originalRuleStrings) {
					rulesBox.addItem(r);
				}
			} else if (regularRules.isSelected()) {
				// add only non-disambiguation rules
				for (int i=0;i<originalRuleStrings.size();i++) {
					if (!disambigRuleIndices[i]) {
						rulesBox.addItem(originalRuleStrings.get(i));
					}
				}
			} else if (disambigRules.isSelected()) {
				// add only disambiguation rules
				for (int i=0;i<originalRuleStrings.size();i++) {
					if (disambigRuleIndices[i]) {
						rulesBox.addItem(originalRuleStrings.get(i));
					}
				}
			} else {
				// nothing
			}
		}
	}
	
	
	private RuleConverter getCurrentRuleConverter() {
		RuleConverter rc = null;
		String type = (String)ruleTypeBox.getSelectedItem();
		
		if (type.equals(atdString)) {
			rc = new AtdRuleConverter(getCurrentFilename(), null, "default");
		} else if (type.equals(cgString)) {
			rc = new CgRuleConverter(getCurrentFilename(), null, "default");
		}
		return rc;
	}
	
	private String getCurrentFilename() {
		try {
			String fn = mainRuleFilePane.getText();
			filename = fn;
			return mainRuleFilePane.getText();
		} catch (NullPointerException e) {
			return "";
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == checkRulesCoveredButton) {
			checkIfAllCurrentRulesCovered();
		} else if (e.getSource() == writeRulesToFileButton) {
			try {
				writeRulesToFile();
			} catch (IOException ex) {
				showError(ex);
			}
		}
	}
	
	public void checkIfAllCurrentRulesCovered() {
		try {
			checker = new RuleCoverage();
			for (int i=0;i<ruleStrings.size();i++) {
				PatternRule patternRule = checker.parsePatternRule(ruleStrings.get(i)).get(0);
				String coveringRule = checker.isCoveredBy(patternRule);
				coveredByList[i] = coveringRule;
				displayCoveredBy();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveEditedVisibleRule() {
		if (originalRuleStrings != null && ruleStrings != null) {
			String newRule = resultArea.getText();
			String selectedRule = (String) rulesBox.getSelectedItem();
			int index;
			for (index=0;index<originalRuleStrings.size();index++) {
				if (selectedRule.equals(originalRuleStrings.get(index))) {
					break;
				}
			}
			ruleStrings.set(index, newRule);
		}
	}
	
	private int getCurrentRuleIndex() {
		String selectedRule = (String)rulesBox.getSelectedItem();
		int index;
		for (index = 0;index<originalRuleStrings.size();index++	) {
			if (selectedRule.equals(originalRuleStrings.get(index))) {
				break;
			}
		}
		return index;
	}
	
	private String getCurrentOutfile() {
		if (outfilename.equals("")) {
			outfilename = filename + ".grammar.xml";
			return outfilename;
		} else {
			return outfilename;
		}
	}
	
	private String getCurrentDisambigFile() {
		if (disambigOutFile.equals("")) {
			disambigOutFile = filename + ".disambig.xml";
			return disambigOutFile;
		} else {
			return disambigOutFile;
		}
	}
	
	public void writeRulesToFile() throws IOException {
		boolean writeCovered = writeCoveredRules.isSelected();
		// write regular rules
		if (anyRegularRules()) {
			outfilename = getCurrentOutfile();
			PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outfilename),"UTF-8"));
	        w.write("<rules>\n");
	        w.write("<category name=\"Auto-generated rules\">\n");
	        for (int i=0;i<ruleStrings.size();i++) {
	        	if (!disambigRuleIndices[i] && (writeCovered || (!writeCovered && coveredByList[i].equals("")))) {
	        		w.write(ruleStrings.get(i));
	        	}
	        }
	        w.write("</category>\n");
	        w.write("</rules>");
	        w.close();
	        showDialog("Rules written to " + outfilename);
		}
		// write disambiguation rules
		if (anyDisambiguationRules()) {
			disambigOutFile = getCurrentDisambigFile();
			PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(disambigOutFile),"UTF-8"));
	        w.write("<rules>\n");
	        for (int i=0;i<ruleStrings.size();i++) {
	        	if (disambigRuleIndices[i]) {
	        		w.write(ruleStrings.get(i));
	        	}
	        }
	        w.write("</rules>");
	        w.close();
	        showDialog("Rules written to " + disambigOutFile);
		}
	}
	
	private void populateSpecificRuleType() {
		String[] ft = getCurrentRuleConverter().getAcceptableFileTypes();
		specificRuleTypeBox.removeAllItems();
		for (String s : ft) {
			specificRuleTypeBox.addItem(s);
		}
	}
	
	private boolean anyRegularRules() {
		for (boolean b : disambigRuleIndices) {
			if (!b) return true;
		}
		return false;
	}
	
	private boolean anyDisambiguationRules() {
		for (boolean b : disambigRuleIndices) {
			if (b) return true;
		}
		return false;
	}
	
	private void showDialog(String fn) {
		final JDialog writeDialog = new JDialog(this.frame);
        JLabel label = new JLabel(fn);
        GridBagConstraints cons = new GridBagConstraints();
        cons.insets = new Insets(2,2,2,2);
        cons.gridx = 0;
        cons.gridy = 0;
        cons.ipady = 10;
        cons.ipadx = 10;
        writeDialog.setLayout(new GridBagLayout());
        writeDialog.add(label,cons);
        writeDialog.setTitle("Message");
        writeDialog.setLocationRelativeTo(this.frame);	// this stuff doesn't really work as far as setting the location goes
        writeDialog.setLocation(50, 50);
        // close dialog window if ESC pressed
        final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        final ActionListener actionListener = new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent actionEvent) {
            writeDialog.setVisible(false);
          }
        };
        final JRootPane rootPane = writeDialog.getRootPane();
        rootPane.registerKeyboardAction(actionListener, stroke,
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        writeDialog.pack();
        writeDialog.setVisible(true);
	}
	
	
	
	public void showOptions() {
		ConfigDialog configDialog = new ConfigDialog(this.frame);
		configDialog.show();
	}
	
	private String loadFile(Frame f, String title, String fileType) {
		String fn = getCurrentFilename();
		FileDialog fd = new FileDialog(f, title, FileDialog.LOAD);
		fd.setFile(fileType);
		String path = fn.replaceAll(new File(fn).getName(),"");	// set the default dir to the path of the current file
		fd.setDirectory(path);
		fd.setLocation(50, 50);
		fd.setVisible(true);
		// TODO: should fix this to account for the nulls
		return fd.getDirectory() + fd.getFile();
	}
	
	public void loadFile() {
		String fn =	loadFile(frame, "Load grammar file", null); 
		if (!fn.equals("nullnull")) {
			filename = fn;
			String fileString = readFileAsString(filename);
			if (fileString.contains("::")) {
				ruleTypeBox.setSelectedItem("After the Deadline");
			} else if (fileString.contains("REMOVE") || fileString.contains("SELECT") || 
					fileString.contains("LIST") || fileString.contains("SET")) {
				ruleTypeBox.setSelectedItem("Constraint Grammar");
			}
			mainRuleFilePane.setText(filename);
		}
	}
	
	private String readFileAsString(String filename) {
		String line = null;
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
		} catch (IOException e) {
			// do nothing if you can't get at the file for some reason 
		}
		return sb.toString();
	}
	
	// doesn't let you edit the rules
	public void showAllRules() {
		final JDialog rulesDialog = new JDialog(this.frame);
		rulesDialog.setMinimumSize(new Dimension(500,500));
		JTextPane rulesPane = new JTextPane();
		String allRules = getAllRules();
		rulesPane.setText(allRules);
		JScrollPane scrollPane = new JScrollPane(rulesPane);
		// close dialog window if ESC pressed
        final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        final ActionListener actionListener = new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent actionEvent) {
            rulesDialog.setVisible(false);
          }
        };
        final JRootPane rootPane = rulesDialog.getRootPane();
        rootPane.registerKeyboardAction(actionListener, stroke,
            JComponent.WHEN_IN_FOCUSED_WINDOW);
		rulesDialog.add(scrollPane);
		rulesDialog.setVisible(true);
	}
	
	private String getAllRules() {
		StringBuilder sb = new StringBuilder();
		if (ruleStrings == null) {
			return "";
		}
		for (String r : ruleStrings) {
			sb.append(r);
		}
		return sb.toString();
	}

	// navigate through the rules of the file
	public void nextRule() {
		if (rulesBox != null) {
			try {
				rulesBox.setSelectedIndex(rulesBox.getSelectedIndex() + 1);
			} catch (IndexOutOfBoundsException e) {
				// nothing
			} catch (IllegalArgumentException e) {
				// nothing
			}
		}
	}

	public void prevRule() {
		if (rulesBox != null) {
			try {
				rulesBox.setSelectedIndex(rulesBox.getSelectedIndex() - 1);
			} catch (IndexOutOfBoundsException e) {
				// nothing
			} catch (IllegalArgumentException e) {
				// nothing
			}
		}
	}
	
	
	static void showError(final Exception e) {
	    final String msg = de.danielnaber.languagetool.tools.Tools
	        .getFullStackTrace(e);
	    JOptionPane
	        .showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();
	  }
	
	// so it knows to quit when you close the window
	class CloseListener implements WindowListener {

	    @Override
	    public void windowClosing(WindowEvent e) {
	      quit();
	    }

	    @Override
	    public void windowActivated(WindowEvent e) {}
	    @Override
	    public void windowClosed(WindowEvent e) {}
	    @Override
	    public void windowDeactivated(WindowEvent e) {}
	    @Override
	    public void windowDeiconified(WindowEvent e) {}
	    @Override
	    public void windowIconified(WindowEvent e) {}
	    @Override
	    public void windowOpened(WindowEvent e) {}

	}
	
	// TODO: still working on this
	class ConfigDialog implements ActionListener {

		private JDialog dialog;
		
		private JComboBox optionsRuleType;
		private JTextPane ruleFilePane;
		private JButton changeRuleFile;
		private JComboBox specificRuleTypeBox;
		private JTextPane outFilePane;
		private JTextPane disambigOutFilePane;
		private JButton okButton;
		private JButton cancelButton;
		private JButton tieFilenamesButton;
		
		private Frame owner;
		
		public ConfigDialog(Frame owner) {
			this.owner = owner;
		}
		
		public void show() {
			dialog = new JDialog(owner,true);
			dialog.setTitle("Rule Converter Options");
			// close window on escape
			final KeyStroke escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		    final ActionListener actionListener = new ActionListener() {
		      @Override
		      public void actionPerformed(ActionEvent actionEvent) {
		        dialog.setVisible(false);
		      }
		    };
		    final JRootPane rootPane = dialog.getRootPane();
		    rootPane.registerKeyboardAction(actionListener, escStroke,
		        JComponent.WHEN_IN_FOCUSED_WINDOW);
		    // press ctrl+enter to OK-exit
		    final KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,KeyEvent.CTRL_MASK);
		    final ActionListener actionListener2 = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					okButton.doClick();
				}
			};
			rootPane.registerKeyboardAction(actionListener2, enterStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		    
		    // initialize the objects
		    ruleFilePane = new JTextPane();
		    ruleFilePane.setText(new File(getCurrentFilename()).getAbsolutePath());
		    final JLabel rfpLabel = new JLabel("Current rule file:");
		    changeRuleFile = new JButton("Browse");
		    changeRuleFile.addActionListener(this);
		    
		    optionsRuleType = new JComboBox();
		    optionsRuleType.addItem(cgString);
			optionsRuleType.addItem(atdString);
			optionsRuleType.setSelectedItem(ruleTypeBox.getSelectedItem());
			optionsRuleType.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					populateSpecificRuleType();
				}
			});
		    final JLabel ortLabel = new JLabel("Current rule type:");
		    
		    specificRuleTypeBox = new JComboBox();
		    populateSpecificRuleType();
		    try {
		    	specificRuleTypeBox.setSelectedItem(specificFileType);
		    } catch (Exception e) {
		    	// nothing
		    }
		    final JLabel srtLabel = new JLabel("Specific rule type");
		    
		    
		    outFilePane = new JTextPane();
		    outFilePane.setText(new File(outfilename).getAbsolutePath());
		    final JLabel ofpLabel = new JLabel("Current output grammar file:");
		    disambigOutFilePane = new JTextPane();
		    disambigOutFilePane.setText(new File(disambigOutFile).getAbsolutePath());
		    final JLabel dofpLabel = new JLabel("Current disambiguation file:");
		    
		    tieFilenamesButton = new JButton("Tie output filenames to input files");
		    tieFilenamesButton.setMnemonic('t');
		    tieFilenamesButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == tieFilenamesButton) {
						String fn = ruleFilePane.getText();
						outfilename = fn + ".grammar.xml";
						disambigOutFile = fn + ".disambig.xml";
						outFilePane.setText(outfilename);
						disambigOutFilePane.setText(disambigOutFile);
					}
				}
			});
		    
		    okButton = new JButton("OK");
		    okButton.addActionListener(this);
		    okButton.setMnemonic('o');
		    cancelButton = new JButton("Cancel");
		    cancelButton.addActionListener(this);
		    
		    // place the objects
		    final Container contentPane = dialog.getContentPane();
		    GridBagLayout layout = new GridBagLayout();
		    
		    layout.rowWeights = new double[]{10f,10f,10f,10f,10f,10f,10f};
		    layout.rowHeights = new int[]{110,90,90,110,110,110,110};
		    contentPane.setLayout(layout);
		    GridBagConstraints cons = new GridBagConstraints();
		    cons.fill = GridBagConstraints.BOTH;
		    
		    cons.gridx = 0;
		    cons.gridy = 0;
		    cons.anchor = GridBagConstraints.EAST;
		    contentPane.add(rfpLabel,cons);
		    cons.gridx = 1;
		    cons.gridy = 0;
		    cons.anchor = GridBagConstraints.WEST;
		    contentPane.add(ruleFilePane,cons);
		    cons.gridx = 2;
		    cons.gridy = 0;
		    cons.anchor = GridBagConstraints.CENTER;
		    contentPane.add(changeRuleFile,cons);
		    cons.gridx = 0;
		    cons.gridy = 1;
		    cons.anchor = GridBagConstraints.EAST;
		    contentPane.add(ortLabel,cons);
		    cons.gridx = 1;
		    cons.gridy = 1;
		    cons.anchor = GridBagConstraints.WEST;
		    contentPane.add(optionsRuleType,cons);
		    cons.gridx = 0;
		    cons.gridy = 2;
		    cons.anchor = GridBagConstraints.EAST;
		    contentPane.add(srtLabel,cons);
		    cons.gridx = 1;
		    cons.gridy = 2;
		    cons.anchor = GridBagConstraints.WEST;
		    contentPane.add(specificRuleTypeBox,cons);
		    cons.gridx = 0;
		    cons.gridy = 3;
		    cons.anchor = GridBagConstraints.EAST;
		    contentPane.add(ofpLabel,cons);
		    cons.gridx = 1;
		    cons.gridy = 3; 
		    cons.gridwidth = 2;
		    cons.anchor = GridBagConstraints.WEST;
		    contentPane.add(outFilePane,cons);
		    cons.gridx = 0;
		    cons.gridy = 4;
		    cons.gridwidth = 2;
		    cons.anchor = GridBagConstraints.EAST;
		    contentPane.add(dofpLabel,cons);
		    cons.gridx = 1;
		    cons.gridy = 4;
		    cons.gridwidth = 2;
		    cons.anchor = GridBagConstraints.WEST;
		    contentPane.add(disambigOutFilePane,cons);
		    cons.gridx = 0;
		    cons.gridy = 5;
		    cons.gridwidth = 2;
		    contentPane.add(tieFilenamesButton,cons);
		    
		    
		    JPanel panel = new JPanel();
		    panel.setLayout(new GridBagLayout());
		    GridBagConstraints panelCons = new GridBagConstraints();
		    panelCons.gridx = 0;
		    panelCons.gridy = 0;
		    panelCons.anchor = GridBagConstraints.SOUTHWEST;
		    panel.add(cancelButton,panelCons);
		    panelCons.gridx = 1;
		    panelCons.anchor = GridBagConstraints.SOUTHEAST;
		    panel.add(okButton,panelCons);
		    
		    cons.gridx = 0;
		    cons.gridy = 6;
		    cons.gridwidth = 3;
		    cons.ipady = 300;
		    contentPane.add(panel,cons);
		    
		    
		    dialog.pack();
		    dialog.setSize(500, 500);
		    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		    final Dimension frameSize = dialog.getSize();
		    dialog.setLocation(screenSize.width / 2 - frameSize.width / 2,
		        screenSize.height / 2 - frameSize.height / 2);
		    dialog.setVisible(true);
		}
		
		private void populateSpecificRuleType() {
			String[] ft = getCurrentRuleConverter().getAcceptableFileTypes();
			specificRuleTypeBox.removeAllItems();
			for (String s : ft) {
				specificRuleTypeBox.addItem(s);
			}
		}
		
		private RuleConverter getCurrentRuleConverter() {
			RuleConverter rc = null;
			String type = (String)optionsRuleType.getSelectedItem();
			
			if (type.equals(atdString)) {
				rc = new AtdRuleConverter(getCurrentFilename(), null, "default");
			} else if (type.equals(cgString)) {
				rc = new CgRuleConverter(getCurrentFilename(), null, "default");
			}
			return rc;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				ruleTypeBox.setSelectedItem(optionsRuleType.getSelectedItem());
				filename = ruleFilePane.getText();
				mainRuleFilePane.setText(filename);
				outfilename = outFilePane.getText();
				disambigOutFile = disambigOutFilePane.getText();
				specificFileType = (String)specificRuleTypeBox.getSelectedItem();
				dialog.setVisible(false);
			} else if (e.getSource() == cancelButton) {
				dialog.setVisible(false);
			} else if (e.getSource() == changeRuleFile) {
				String optionsFilename = loadFile(this.owner, "Load rule file", null);
				ruleFilePane.setText(optionsFilename);
			}
			
		}
		
	}

}
