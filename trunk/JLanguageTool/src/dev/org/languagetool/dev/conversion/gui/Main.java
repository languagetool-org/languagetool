package de.danielnaber.languagetool.dev.conversion.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import net.boplicity.xmleditor.*;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.dev.conversion.CgRuleConverter;
import de.danielnaber.languagetool.dev.conversion.AtdRuleConverter;
import de.danielnaber.languagetool.dev.conversion.RuleConverter;
import de.danielnaber.languagetool.dev.conversion.RuleCoverage;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.patterns.PatternRule;

public final class Main implements ActionListener {

	// Display elements
	private JFrame frame; // main frame

	private JComboBox rulesBox;
	private XmlTextPane resultArea;

	private JComboBox ruleTypeBox;
	private JComboBox specificRuleTypeBox;

	private JTextPane coveredByPane;
	private JTextPane warningPane;

	private JButton convert;
	private JButton saveEditedRule;
	private JButton deleteCurrentRule;
	private JButton toggleDefaultOff;
	private JButton makeRuleExclusive;
	private JButton toggleExtraTokens;
	private JButton checkRulesCoveredButton;
	private JButton recheckCurrentRuleCoverage;
	private JButton writeRulesToFileButton;

	private JCheckBox regularRules;
	private JCheckBox disambigRules;
	private JCheckBox noWarningRules;
	private JCheckBox warningRules;
	private JCheckBox coveredRules;
	private JCheckBox notCoveredRules;

	private JTextPane mainRuleFilePane;
	private JTextPane outFilePane;
	private JTextPane disambigOutFilePane;

	private JCheckBox writeCoveredRules;
	private JCheckBox editBeforeWriting;

	private JTextPane numRulesPane;
	private JTextPane displayedNumRulesPane;

	// Lists containing the rules
	private List<? extends Object> ruleObjects;
	private ArrayList<List<String>> allRulesList;
	private ArrayList<String> ruleStrings;
	private ArrayList<String> originalRuleStrings;
	private ArrayList<String[]> warnings;
	private boolean[] disambigRuleIndices;
	private ArrayList<String[]> coveredByList; // only applies to regular LT rules

	// Rule coverage
	private RuleCoverage checker;

	private String filename = "";
	private String outfilename = "";
	private String disambigOutFile = "";
	
	private boolean defaultOff = false;
	private boolean extraTokens = true;

	private int numberOfRules;

	// constants
	private static String cgString = "Constraint Grammar";
	private static String atdString = "After the Deadline";

	private static final int WINDOW_WIDTH = 850;
	private static final int WINDOW_HEIGHT = 800;

	private static Pattern ruleHeaderRegex = Pattern.compile("<rule(group)?\\s+id=\".*?\"\\s+name=\".*?\"");
	private static Pattern defaultOffRegex = Pattern.compile(" default=\"off\"");
	private static String defaultOffString = " default=\"off\"";
	private static Pattern postagToken = Pattern.compile("(<token postag=\"(.*?)\"( postag_regexp=\"yes\")?>)(</token>)");

	// main method
	public static void main(final String[] args) {
		try {
			final Main prg = new Main();
			if (args.length > 0) {
				System.out.println("Usage: java -jar RuleConverterGUI.jar");
				System.out.println("  no arguments");
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

	private void createGUI() {
		// main frame
		frame = new JFrame("Language Tool Rule Converter");
		frame.addWindowListener(new CloseListener());
		frame.setJMenuBar(new MainMenuBar(this));
		setLookAndFeel();

		// converted rule area
		resultArea = new XmlTextPane();
		resultArea.requestFocusInWindow();
		JScrollPane scrollPane = new JScrollPane(resultArea);
		scrollPane.setPreferredSize(new Dimension(250, 145));
		scrollPane.setMinimumSize(new Dimension(10, 10));

		// original rule combo box
		rulesBox = new JComboBox();
		rulesBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displaySelectedRule();
				displayCoveredBy();
				displayWarnings();
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
		mainRuleFilePane.setBorder(new LineBorder(Color.BLACK, 1));

		outFilePane = new JTextPane();
		outFilePane.setText(outfilename);
		outFilePane.setBorder(new LineBorder(Color.BLACK, 1));

		disambigOutFilePane = new JTextPane();
		disambigOutFilePane.setText(disambigOutFile);
		disambigOutFilePane.setBorder(new LineBorder(Color.BLACK, 1));

		// convert button
		convert = new JButton("Convert");
		convert.setMnemonic('C');
		convert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				convertRuleFile();
				populateRuleBox();
			}
		});

		deleteCurrentRule = new JButton("Delete current rule");
		deleteCurrentRule.setMnemonic('D');
		deleteCurrentRule.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteCurrentRule();
			}
		});

		toggleDefaultOff = new JButton("Toggle default=\"off\"");
		toggleDefaultOff.setMnemonic('t');
		toggleDefaultOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!defaultOff) {
					setDefaultOff();
					defaultOff = true;
				} else {
					setDefaultOn();
					defaultOff = false;
				}
			}
		});
		
		
		makeRuleExclusive = new JButton("Make rule exclusive");
		makeRuleExclusive.setMnemonic('x');
		makeRuleExclusive.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				makeCurrentRuleExclusive();
			}
		});
		
		toggleExtraTokens = new JButton("Toggle extra tokens");
		toggleExtraTokens.setMnemonic('a');
		toggleExtraTokens.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!extraTokens) {
					addExtraTokens();
					extraTokens = true;
				} else {
					removeExtraTokens();
					extraTokens = false;
				}
				
			}
		});

		// save rule button
		saveEditedRule = new JButton("Save rule");
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
		coveredByPane.setBorder(new LineBorder(Color.BLACK, 1));

		// warnings pane
		warningPane = new JTextPane();
		warningPane.setBorder(new LineBorder(Color.BLACK, 1));

		// put coveredByPane and warningPane together in one JPanel
		JPanel coveredWarningPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cwcons = new GridBagConstraints();
		cwcons.weightx = 1f;
		cwcons.fill = GridBagConstraints.BOTH;
		cwcons.anchor = GridBagConstraints.WEST;
		coveredWarningPanel.add(new JLabel("Covered by:"), cwcons);
		cwcons.gridx = 1;
		coveredWarningPanel.add(new JLabel("Warnings:"), cwcons);
		cwcons.gridy = 1;
		coveredWarningPanel.add(warningPane, cwcons);
		cwcons.gridx = 0;
		coveredWarningPanel.add(coveredByPane, cwcons);

		// display regular and/or disambiguation rules check box
		regularRules = new JCheckBox("Show regular rules", true);
		disambigRules = new JCheckBox("Show disambiguation rules", true);
		warningRules = new JCheckBox("Show rules with warnings", true);
		noWarningRules = new JCheckBox("Show rules without warnings", true);
		coveredRules = new JCheckBox("Show covered rules", true);
		notCoveredRules = new JCheckBox("Show not covered rules", true);
		regularRules.addActionListener(this);
		disambigRules.addActionListener(this);
		warningRules.addActionListener(this);
		noWarningRules.addActionListener(this);
		coveredRules.addActionListener(this);
		notCoveredRules.addActionListener(this);

		// write covered rules to file check box
		writeCoveredRules = new JCheckBox("Write duplicate rules to file");
		editBeforeWriting = new JCheckBox("Edit rules before writing");

		// check if all current rules are covered button
		checkRulesCoveredButton = new JButton("Check rule coverage");
		checkRulesCoveredButton.setMnemonic('E');
		checkRulesCoveredButton.addActionListener(this);

		recheckCurrentRuleCoverage = new JButton(
				"Check displayed rule coverage");
		recheckCurrentRuleCoverage.setMnemonic('R');
		recheckCurrentRuleCoverage.addActionListener(this);

		// number of rules display
		final JLabel numRulesLabel = new JLabel("Total number of rules:");
		numRulesPane = new JTextPane();
		final JLabel numDisplayedRulesLabel = new JLabel(
				"Number of displayed rules:");
		displayedNumRulesPane = new JTextPane();

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

		insidePanel.setBorder(new LineBorder(Color.BLACK, 1));
		buttonCons.fill = GridBagConstraints.BOTH;

		// buttonCons.weightx = 1f;

		// rule type
		JPanel ruleTypePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		ruleTypePanel.add(new JLabel("Rule type:"), c);
		c.gridx = 1;
		ruleTypePanel.add(ruleTypeBox, c);
		c.gridx = 2;
		ruleTypePanel.add(specificRuleTypeBox, c);

		insidePanel.add(ruleTypePanel, buttonCons);

		JPanel displayRulesPanel = new JPanel(new GridBagLayout());
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		displayRulesPanel.add(regularRules, c);
		c.gridx = 1;
		displayRulesPanel.add(disambigRules, c);
		c.gridx = 0;
		c.gridy = 1;
		displayRulesPanel.add(warningRules, c);
		c.gridx = 1;
		displayRulesPanel.add(noWarningRules, c);
		c.gridx = 0;
		c.gridy = 2;
		displayRulesPanel.add(coveredRules, c);
		c.gridx = 1;
		displayRulesPanel.add(notCoveredRules, c);
		c.anchor = GridBagConstraints.CENTER;

		buttonCons.gridy = 1;
		insidePanel.add(displayRulesPanel, buttonCons);

		JPanel coveragePanel = new JPanel(new GridBagLayout());
		c.gridx = 0;
		coveragePanel.add(checkRulesCoveredButton, c);
		c.gridx = 1;
		coveragePanel.add(recheckCurrentRuleCoverage, c);

		buttonCons.gridy = 2;
		buttonCons.fill = GridBagConstraints.NONE;
		insidePanel.add(coveragePanel, buttonCons);
		

		JPanel modifyRulesPanel = new JPanel(new GridBagLayout());
		c.gridx = 0;
		modifyRulesPanel.add(toggleDefaultOff, c);
		c.gridx = 1;
		modifyRulesPanel.add(makeRuleExclusive, c);
		c.gridx = 2;
		modifyRulesPanel.add(toggleExtraTokens, c);
		buttonCons.gridy = 3;
		insidePanel.add(modifyRulesPanel, buttonCons);

		
		JPanel savePanel = new JPanel(new GridBagLayout());
		c.gridx = 0;
		savePanel.add(saveEditedRule, c);
		c.gridx = 1;
		savePanel.add(deleteCurrentRule, c);
		buttonCons.gridy = 4;
		insidePanel.add(savePanel, buttonCons);

		JPanel convertPanel = new JPanel(new GridBagLayout());
		c.gridx = 0;
		convertPanel.add(convert,c);
		c.gridx = 1;
		convertPanel.add(writeRulesToFileButton, c);
		buttonCons.gridy = 5;
		insidePanel.add(convertPanel, buttonCons);

		
		JPanel writeOptionsPanel = new JPanel(new GridBagLayout());
		c.gridx = 0;
		writeOptionsPanel.add(writeCoveredRules, c);
		c.gridx = 1;
		writeOptionsPanel.add(editBeforeWriting, c);
		buttonCons.gridy = 6;
		insidePanel.add(writeOptionsPanel, buttonCons);

		JPanel numRulesPanel = new JPanel(new GridBagLayout());
		c.gridx = 0;
		numRulesPanel.add(numRulesLabel, c);
		c.gridx = 1;
		numRulesPanel.add(numRulesPane, c);
		c.gridx = 2;
		numRulesPanel.add(numDisplayedRulesLabel, c);
		c.gridx = 3;
		numRulesPanel.add(displayedNumRulesPane, c);

		buttonCons.gridy = 7;
		insidePanel.add(numRulesPanel, buttonCons);

		cons.gridx = 0;
		cons.gridy = 0;
		cons.ipadx = 1;
		cons.ipady = 1;
		JLabel ruleLabel = new JLabel("Original rule:");
		contentPane.add(ruleLabel, cons);
		cons.gridx = 0;
		cons.gridy = 2;
		JLabel convertedRuleLabel = new JLabel("Converted rule:");
		contentPane.add(convertedRuleLabel, cons);
		cons.gridx = 0;
		cons.gridy = 1;
		cons.weightx = 10f;
		cons.weighty = 2f;
		contentPane.add(rulesBox, cons);
		cons.gridx = 0;
		cons.gridy = 3;
		cons.weightx = 10f;
		cons.weighty = 10f;
		cons.ipady = 150;
		scrollPane.setMinimumSize(new Dimension(0, 200));
		contentPane.add(scrollPane, cons);
		cons.gridx = 0;
		cons.gridy = 4;
		cons.ipady = 0;
		cons.ipadx = 0;
		cons.weightx = 0;
		cons.weighty = 0;
		cons.anchor = GridBagConstraints.WEST;
		contentPane.add(coveredWarningPanel, cons);
		cons.gridy = 5;
		contentPane.add(insidePanel, cons);
		cons.gridx = 0;
		cons.gridy = 6;
		contentPane.add(new JLabel("Rule file:"), cons);
		cons.gridy = 7;
		contentPane.add(mainRuleFilePane, cons);
		cons.gridy = 8;
		contentPane.add(new JLabel("Out file:"), cons);
		cons.gridy = 9;
		contentPane.add(outFilePane, cons);
		cons.gridy = 10;
		contentPane.add(new JLabel("Disambiguation out file:"), cons);
		cons.gridy = 11;
		contentPane.add(disambigOutFilePane, cons);

		frame.pack();
		frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
	}

	private void setLookAndFeel() {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager
					.getInstalledLookAndFeels()) {
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

	// Display methods

	private void displaySelectedRule() {
		if (rulesBox.getSelectedIndex() == -1) {
			resultArea.setText("");
		} else {
			String selectedRule = (String) rulesBox.getSelectedItem();
			for (int i = 0; i < originalRuleStrings.size(); i++) {
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
				String[] cov = coveredByList.get(index);
				StringBuilder sb = new StringBuilder();
				for (String s : cov) {
					sb.append(s + ", ");
				}
				coveredByPane.setText(sb.toString().trim());
			}
		}
	}

	public void displayCoveringRules() {
		String[] ruleIds = coveredByPane.getText().split(",\\ ?");
		openExistingRuleWindow(ruleIds);
	}

	private void displayWarnings() {
		if (rulesBox.getSelectedIndex() == -1) {
			warningPane.setText("");
		} else {
			if (warnings != null) {
				int index = getCurrentRuleIndex();
				warningPane.setText(listToString(warnings.get(index)));
				warningPane.repaint();
			}
		}
	}

	// displays the existing LT rule that covers the converted rule
	private void openExistingRuleWindow(String[] ruleIds) {
		if (checker == null) {
			return;
		}
		JLanguageTool tool = checker.getLanguageTool();
		String fetchedRuleString = "<pre>";
		List<Rule> rules = tool.getAllRules();
		for (String ruleId : ruleIds) {
			for (Rule rule : rules) {
				if (rule.getId().equals(ruleId)) {
					// can only display pattern rules
					try {
						PatternRule patternRule = (PatternRule) rule;
						String tempRuleString = patternRule.toXML();
						tempRuleString = tempRuleString.replaceAll("\\<",
								"&lt;").replaceAll("\\>", "&gt;");
						fetchedRuleString = fetchedRuleString.concat(
								tempRuleString).concat("<br>");
						break;
					} catch (ClassCastException e) {
						fetchedRuleString += "Can't display Java rules";
						break;
					}
				}
			}
		}
		fetchedRuleString = fetchedRuleString.concat("</pre>");
		showDialog("<html>" + fetchedRuleString + "</html>", "Existing LT Rule");
	}

	// generates outfile names from the input file name
	private void tieOutFileNames() {
		filename = getCurrentFilename();
		outfilename = filename + ".grammar.xml";
		disambigOutFile = filename + ".disambig.xml";
		outFilePane.setText(outfilename);
		disambigOutFilePane.setText(disambigOutFile);
	}

	// removes the currently displayed rule; it won't be written to the outfile
	public void deleteCurrentRule() {
		if (ruleStrings == null || rulesBox.getSelectedItem() == null) {
			return;
		}
		int index = getCurrentRuleIndex();
		
		ruleObjects.remove(index);
		allRulesList.remove(index);
		ruleStrings.remove(index);
		originalRuleStrings.remove(index);
		warnings.remove(index);
		// removing from the disambigRuleIndices array
		disambigRuleIndices = removeIndexFromBooleanArray(disambigRuleIndices,
				index);
		coveredByList.remove(index);
		numberOfRules--;
		numRulesPane.setText(Integer.toString(numberOfRules));
		
		populateRuleBox();
		if (index > 0) {
			rulesBox.setSelectedItem(originalRuleStrings.get(index - 1));
		}
		
	}

	private static boolean[] removeIndexFromBooleanArray(boolean[] array,
			int index) {
		boolean[] n = new boolean[array.length - 1];
		for (int i = 0; i < index; i++) {
			n[i] = array[i];
		}
		for (int i = index + 1; i < array.length; i++) {
			n[i - 1] = array[i];
		}
		return n;
	}

	// some regex stuff to make the pos tags of the current rule exclusive (or "careful")
	public void makeCurrentRuleExclusive() {
		if (ruleStrings == null || rulesBox.getSelectedItem() == null) {
			return;
		}
		int index = getCurrentRuleIndex();
		String rs = ruleStrings.get(index);
		rs = makeTokensExclusive(rs);
		ruleStrings.set(index,rs);
		displaySelectedRule();
	}
	
	public void makeAllRulesExclusive() {
		if (ruleStrings == null || rulesBox.getSelectedItem() == null) {
			return;
		}
		for (int i=0;i<ruleStrings.size();i++) {
			ruleStrings.set(i,makeTokensExclusive(ruleStrings.get(i)));
		}
		populateRuleBox();
	}
	
	
	private String makeTokensExclusive(String ruleString) {
		Matcher m = postagToken.matcher(ruleString);
		while (m.find()) {
			String r = generateExclusiveException(m.group(2),m.group(3) != null);
			ruleString = ruleString.replace(m.group(),m.group(1) + r + m.group(4));
		}
		return ruleString;
	}
	
	private String generateExclusiveException(String postags, boolean regexp) {
		String ex = "<exception postag=\"";
        ex = ex.concat(postags).concat("\"");
        if (regexp) {
            ex = ex.concat(" postag_regexp=\"yes\" negate_pos=\"yes\"/>");
        } else {
            ex = ex.concat(" negate_pos=\"yes\"/>");
        }
        if (postags.equals(getCurrentRuleConverter().getSentStart()) || 
        	postags.equals(getCurrentRuleConverter().getSentEnd())) {
        	return "";
        }
        return ex;
	}
	
	// method to add the empty tokens at the beginning and end of the pattern, to catch extra overlapping rules
	
	// adds a default="off" attribute to the <rule> and <rulegroup> elements
	private void setDefaultOff() {
		if (ruleStrings == null || rulesBox.getSelectedItem() == null) {
			return;
		}
		for (int i = 0; i < ruleStrings.size(); i++) {
			String oldRuleString = ruleStrings.get(i);
			String newRuleString = addOffAttribute(oldRuleString);
			ruleStrings.set(i, newRuleString);
		}
		displaySelectedRule();
	}

	private void setDefaultOn() {
		if (ruleStrings == null || rulesBox.getSelectedItem() == null) {
			return;
		}
		for (int i = 0; i < ruleStrings.size(); i++) {
			String oldRuleString = ruleStrings.get(i);
			String newRuleString = removeOffAttribute(oldRuleString);
			ruleStrings.set(i, newRuleString);
		}
		displaySelectedRule();
	}

	private String removeOffAttribute(String rule) {
		Matcher alreadyOff = defaultOffRegex.matcher(rule);
		if (alreadyOff.find()) {
			String newRule = rule.replace(alreadyOff.group(), "");
			return newRule;
		} else {
			return rule;
		}
	}

	private String addOffAttribute(String rule) {
		Matcher alreadyOff = defaultOffRegex.matcher(rule);
		if (alreadyOff.find()) {
			return rule;
		} else {
			Matcher ruleHeader = ruleHeaderRegex.matcher(rule);
			if (!ruleHeader.find()) {
				System.out.println();
			}

			String newRule = rule.replace(ruleHeader.group(), ruleHeader
					.group().concat(defaultOffString));
			return newRule;
		}
	}

	// the main workhorse
	private void convertRuleFile() {
		RuleConverter rc = getCurrentRuleConverter();
		this.rulesBox.removeAllItems();
		try {
			// the generic rule objects, just used for building the other lists
			rc.parseRuleFile();
			ruleObjects = rc.getRules();
			// lists of strings
			allRulesList = rc.getAllLtRules();
			// rules in LT format, as strings
			ruleStrings = new ArrayList<String>();
			// original rule strings
			originalRuleStrings = rc.getOriginalRuleStrings();
			// warnings
			warnings = rc.getWarnings();
			// populating the string lists
			for (int i = 0; i < allRulesList.size(); i++) {
				List<String> ruleList = allRulesList.get(i);
				String ruleString = RuleConverter
						.getRuleStringFromList(ruleList);
				ruleStrings.add(ruleString);
			}
			// take out exact copies of rules (this messes up navigating through
			// the rules with the arrow keys)
			removeDuplicateRules();

			disambigRuleIndices = new boolean[allRulesList.size()];
			// list of existing LT rules that cover the new rules
			coveredByList = new ArrayList<String[]>();
			numberOfRules = allRulesList.size();
			numRulesPane.setText(Integer.toString(numberOfRules));

			for (int i = 0; i < ruleObjects.size(); i++) {
				Object ruleObject = ruleObjects.get(i);
				if (rc.isDisambiguationRule(ruleObject)) {
					disambigRuleIndices[i] = true;
				} else {
					disambigRuleIndices[i] = false;
				}
				coveredByList.add(new String[0]);
			}

			tieOutFileNames();

		} catch (IOException e) {
			showDialog("IOException while loading/parsing file " + filename,
					null);
		}
	}

	private void removeDuplicateRules() {
		boolean notdone = true;
		while (notdone) {
			for (int i = 0; i < originalRuleStrings.size(); i++) {
				String originalRuleString = originalRuleStrings.get(i);
				if (originalRuleStrings.subList(i + 1,
						originalRuleStrings.size())
						.contains(originalRuleString)) {
					originalRuleStrings.remove(i);
					ruleStrings.remove(i);
					allRulesList.remove(i);
					ruleObjects.remove(i);
					warnings.remove(i);
					break;
				} else {
					if (i == originalRuleStrings.size() - 1)
						notdone = false;
				}
			}
		}
	}

	private void populateRuleBox() {
		rulesBox.removeAllItems();
		boolean showRegularRules = regularRules.isSelected();
		boolean showDisambigRules = disambigRules.isSelected();
		boolean showWarningRules = warningRules.isSelected();
		boolean showNoWarningRules = noWarningRules.isSelected();
		boolean showCoveredRules = coveredRules.isSelected();
		boolean showNotCoveredRules = notCoveredRules.isSelected();
		int numDisplayed = 0;
		if (originalRuleStrings != null) {
			for (int i = 0; i < originalRuleStrings.size(); i++) {
				boolean cov = coveredByList.get(i).length != 0;
				boolean war = !(warnings.get(i).length == 0);
				boolean dis = disambigRuleIndices[i];
				if (dis && cov && war) {
					if (showDisambigRules && showCoveredRules
							&& showWarningRules) {
						rulesBox.addItem(originalRuleStrings.get(i));
						numDisplayed++;
					}
				} else if (dis && cov && !war) {
					if (showDisambigRules && showCoveredRules
							&& showNoWarningRules) {
						rulesBox.addItem(originalRuleStrings.get(i));
						numDisplayed++;
					}
				} else if (dis && !cov && war) {
					if (showDisambigRules && showNotCoveredRules
							&& showWarningRules) {
						rulesBox.addItem(originalRuleStrings.get(i));
						numDisplayed++;
					}
				} else if (dis && !cov && !war) {
					if (showDisambigRules && showNotCoveredRules
							&& showNoWarningRules) {
						rulesBox.addItem(originalRuleStrings.get(i));
						numDisplayed++;
					}
				} else if (!dis && cov && war) {
					if (showRegularRules && showCoveredRules
							&& showWarningRules) {
						rulesBox.addItem(originalRuleStrings.get(i));
						numDisplayed++;
					}
				} else if (!dis && cov && !war) {
					if (showRegularRules && showCoveredRules
							&& showNoWarningRules) {
						rulesBox.addItem(originalRuleStrings.get(i));
						numDisplayed++;
					}
				} else if (!dis && !cov && war) {
					if (showRegularRules && showNotCoveredRules
							&& showWarningRules) {
						rulesBox.addItem(originalRuleStrings.get(i));
						numDisplayed++;
					}
				} else if (!dis && !cov && !war) {
					if (showRegularRules && showNotCoveredRules
							&& showNoWarningRules) {
						rulesBox.addItem(originalRuleStrings.get(i));
						numDisplayed++;
					}
				}
			}
			displayedNumRulesPane.setText(Integer.toString(numDisplayed));
		}
	}

	private void populateSpecificRuleType() {
		String[] ft = getCurrentRuleConverter().getAcceptableFileTypes();
		specificRuleTypeBox.removeAllItems();
		for (String s : ft) {
			specificRuleTypeBox.addItem(s);
		}
	}

	// Methods to get properties of the gui object

	private RuleConverter getCurrentRuleConverter() {
		RuleConverter rc = null;
		String type = (String) ruleTypeBox.getSelectedItem();
		String specificType = (String) specificRuleTypeBox.getSelectedItem();
		if (specificType == null) {
			specificType = "default";
		}

		if (type.equals(atdString)) {
			rc = new AtdRuleConverter(getCurrentFilename(), null, specificType);
		} else if (type.equals(cgString)) {
			rc = new CgRuleConverter(getCurrentFilename(), null, specificType);
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

	private int getCurrentRuleIndex() {
		String selectedRule = (String) rulesBox.getSelectedItem();
		int index;
		for (index = 0; index < originalRuleStrings.size(); index++) {
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
			if (!outFilePane.getText().equals(outfilename)) {
				outfilename = outFilePane.getText();
			}
			return outfilename;
		}
	}

	private String getCurrentDisambigFile() {
		if (disambigOutFile.equals("")) {
			disambigOutFile = filename + ".disambig.xml";
			return disambigOutFile;
		} else {
			if (!disambigOutFilePane.getText().equals(disambigOutFile)) {
				disambigOutFile = disambigOutFilePane.getText();
			}
			return disambigOutFile;
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
		} else if (e.getSource() == recheckCurrentRuleCoverage) {
			checkDisplayedRuleCoverage();
		} else if (e.getSource() == disambigRules
				|| e.getSource() == regularRules
				|| e.getSource() == warningRules
				|| e.getSource() == noWarningRules
				|| e.getSource() == coveredRules
				|| e.getSource() == notCoveredRules) {
			populateRuleBox();
		}
	}

	// checks if the currently displayed rule is covered by an existing language
	// tool rule
	public void checkDisplayedRuleCoverage() {
		if (ruleStrings == null || rulesBox.getSelectedItem() == null) {
			return;
		}
		try {
			if (checker == null) {
				checker = new RuleCoverage();
				// checker = new
				// RuleCoverage("/home/mbryant/languagetool/JLanguageTool/src/resource/en/english.dict");
			}
			// if the currently displayed rule hasn't been saved yet
			int index = getCurrentRuleIndex();
			if (!ruleStrings.get(index).equals(resultArea.getText())) {
				showDialog("Current rule not yet saved", null);
			} else {
				List<PatternRule> patternRules = checker
						.parsePatternRule(resultArea.getText());
				ArrayList<String[]> allCoveringRules = checker
						.isCoveredBy(patternRules);
				ArrayList<String> coveringRules = new ArrayList<String>();
				for (String[] s : allCoveringRules) {
					for (String ss : s) {
						coveringRules.add(ss);
					}
				}
				coveredByList.set(index, coveringRules
						.toArray(new String[coveringRules.size()]));
				displayCoveredBy();
			}
		} catch (IOException e) {
			showDialog(
					"Couldn't parse or check the rule's coverage for some reason",
					null);
		}
	}
	
	private void addExtraTokens() {
		if (ruleStrings == null || rulesBox.getSelectedItem() == null) {
			return;
		}
		int index = getCurrentRuleIndex();
		String rs = ruleStrings.get(index);
		rs = rs.replace("<pattern>\n","<pattern>\n<token/>\n");
		rs = rs.replace("</pattern>\n","<token/>\n</pattern>\n");
		ruleStrings.set(index,rs);
		displaySelectedRule();
	}
	
	private void removeExtraTokens() {
		if (ruleStrings == null || rulesBox.getSelectedItem() == null) {
			return;
		}
		int index = getCurrentRuleIndex();
		String rs = ruleStrings.get(index);
		rs = rs.replace("<pattern>\n<token/>\n","<pattern>\n");
		rs = rs.replace("<token/>\n</pattern>\n","</pattern>\n");
		ruleStrings.set(index,rs);
		displaySelectedRule();
	}
	
	

	// checks if all the loaded rules (individually) are covered by existing LT
	// rules
	public void checkIfAllCurrentRulesCovered() {
		if (ruleStrings == null || rulesBox.getSelectedItem() == null) {
			return;
		}
		try {
			checker = new RuleCoverage();
			// checker = new
			// RuleCoverage("/home/mbryant/languagetool/JLanguageTool/src/resource/en/english.dict");
			for (int i = 0; i < ruleStrings.size(); i++) {
				if (disambigRuleIndices[i]) {
					continue; // don't check disambiguation (or immunized) rules
				}
				List<PatternRule> patternRules = checker
						.parsePatternRule(ruleStrings.get(i));
				ArrayList<String[]> allCoveringRules = checker
						.isCoveredBy(patternRules);
				ArrayList<String> coveringRules = new ArrayList<String>();
				for (String[] s : allCoveringRules) {
					for (String ss : s) {
						coveringRules.add(ss);
					}
				}
				coveredByList.set(i, coveringRules
						.toArray(new String[coveringRules.size()]));
				displayCoveredBy();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// saves the changes made in the result area to the current rule.
	// modifies what gets written to file
	private void saveEditedVisibleRule() {
		if (originalRuleStrings != null && ruleStrings != null && rulesBox.getSelectedItem() != null) {
			String newRule = resultArea.getText();
			String selectedRule = (String) rulesBox.getSelectedItem();
			int index;
			for (index = 0; index < originalRuleStrings.size(); index++) {
				if (selectedRule.equals(originalRuleStrings.get(index))) {
					break;
				}
			}
			ruleStrings.set(index, newRule);
		}
	}
	
	public void clickSaveButton() {
		saveEditedRule.doClick();
	}
	
	public void removeCoveringRules() {
		int index = getCurrentRuleIndex();
		coveredByList.set(index, new String[0]);
		displayCoveredBy();
	}
	
	public void removeWarnings() {
		int index = getCurrentRuleIndex();
		warnings.set(index, new String[0]);
		displayWarnings();
	}

	public void writeRulesToFile() throws IOException {
		if (ruleStrings == null || rulesBox.getSelectedItem() == null) {
			return;
		}
		boolean writeCovered = writeCoveredRules.isSelected();
		int numReg = 0;
		int numDis = 0;
		StringBuilder regWriteString = new StringBuilder();
		StringBuilder disWriteString = new StringBuilder();
		
		if (anyRegularRules()) {
			regWriteString.append("<category name=\"Auto-generated rules "
					+ new File(filename).getName() + "\">\n");
			for (int i = 0; i < ruleStrings.size(); i++) {
				if (!disambigRuleIndices[i]
						&& (writeCovered || (!writeCovered && coveredByList
								.get(i).length == 0))) {
					regWriteString.append(ruleStrings.get(i));
					numReg++;
				}
			}
			regWriteString.append("</category>");
		}
		if (anyDisambiguationRules()) {
			disWriteString.append("<category name=\"Auto-generated rules "
					+ new File(filename).getName() + "\">\n");
			for (int i = 0; i < ruleStrings.size(); i++) {
				if (disambigRuleIndices[i]) {
					disWriteString.append(ruleStrings.get(i));
					numDis++;
				}
			}
			disWriteString.append("</category>");
		}

		String disString = disWriteString.toString();
		String regString = regWriteString.toString();

		if (editBeforeWriting.isSelected()) {
			writeWithEditing(disString, regString, numDis, numReg);
		} else {
			writeWithoutEditing(disString, regString, numDis, numReg);
		}

	}

	// displays a TextPane that lets you edit the entire rule file before
	// writing it
	private void writeWithEditing(String dis, String reg, int numDis, int numReg)
			throws IOException {
		XmlDisplay regdisplay = new XmlDisplay(reg, getCurrentOutfile(),
				"Edit regular out file");
		regdisplay.show();

		XmlDisplay disdisplay = new XmlDisplay(dis, getCurrentDisambigFile(),
				"Edit disambiguation file");
		disdisplay.show();
	}

	// just writes the rules to file
	private void writeWithoutEditing(String dis, String reg, int numDis,
			int numReg) throws IOException {
		if (!reg.isEmpty()) {
			PrintWriter w = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(outfilename), "UTF-8"));
			w.write(reg);
			w.close();
		}
		if (!dis.isEmpty()) {
			PrintWriter w = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(disambigOutFile), "UTF-8"));
			w.write(dis);
			w.close();
		}

		String message = "";
		if (numReg > 0) {
			message += Integer.toString(numReg) + " rules written to "
					+ outfilename + "<br>";
		}
		if (numDis > 0) {
			message += Integer.toString(numDis) + " rules written to "
					+ disambigOutFile;
		}
		if (message.equals("")) {
			message = "No rules written";
		}
		showDialog(message, null);
	}

	// returns true if there are any regular rules
	private boolean anyRegularRules() {
		for (boolean b : disambigRuleIndices) {
			if (!b)
				return true;
		}
		return false;
	}

	// returns true if there any disambiguation rules
	private boolean anyDisambiguationRules() {
		for (boolean b : disambigRuleIndices) {
			if (b)
				return true;
		}
		return false;
	}

	// shows a dialog box with the specified text
	private void showDialog(String message, String title) {
		final JDialog writeDialog = new JDialog(this.frame);
		JLabel label = new JLabel("<html>" + message + "</html>");
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(2, 2, 2, 2);
		cons.gridx = 0;
		cons.gridy = 0;
		cons.ipady = 10;
		cons.ipadx = 10;
		writeDialog.setLayout(new GridBagLayout());
		writeDialog.add(label, cons);
		if (title == null) {
			title = "Message";
		}
		writeDialog.setTitle(title);
		writeDialog.setLocation(300, 300);

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

	// shows the load file directory dialog
	private String loadFile(Frame f, String title, String fileType) {
		String fn = getCurrentFilename();
		FileDialog fd = new FileDialog(f, title, FileDialog.LOAD);
		fd.setFile(fileType);
		String path = fn.replaceAll(new File(fn).getName(), ""); // set the default dir to the path of the current file
		fd.setDirectory(path);
		fd.setLocation(50, 50);
		fd.setVisible(true);
		return fd.getDirectory() + fd.getFile();
	}

	public void loadFile() {
		String fn = loadFile(frame, "Load grammar file", null);
		if (!fn.equals("nullnull")) {
			filename = fn;
			mainRuleFilePane.setText(filename);
			String fileString = readFileAsString(filename);
			if (fileString.contains("::")) {
				ruleTypeBox.setSelectedItem("After the Deadline");
			} else if (fileString.contains("REMOVE")
					|| fileString.contains("SELECT")
					|| fileString.contains("LIST")
					|| fileString.contains("SET")) {
				ruleTypeBox.setSelectedItem("Constraint Grammar");
			}
			mainRuleFilePane.setText(filename);
			mainRuleFilePane.repaint();
		}
	}

	// reads in the entire file, so we can check what kind of file it is
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
		rulesDialog.setMinimumSize(new Dimension(500, 500));
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
		if (ruleStrings == null || rulesBox.getSelectedItem() == null) {
			return "";
		}
		for (String r : ruleStrings) {
			sb.append(r);
		}
		return sb.toString();
	}
	
	public void showOriginalRuleFile() {
		if (filename == null) {
			return;
		}
		String f = readFileAsString(filename);
		XmlDisplay regdisplay = new XmlDisplay(f, filename, "Edit existing rules file");
		regdisplay.show();
		
	}

	public void displayAboutDialog() {
		showDialog(
				"RuleConverterGUI:<br>Tool for converting rule files from other modalities to LT format.<br>"
						+ "Currently supported: After the Deadline and Constraint Grammar\n",
				"About");
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

	// not really used very often
	public static void showError(final Exception e) {
		final String msg = de.danielnaber.languagetool.tools.Tools
				.getFullStackTrace(e);
		JOptionPane.showMessageDialog(null, msg, "Error",
				JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
	}

	private static String listToString(String[] list) {
		StringBuilder sb = new StringBuilder();
		for (String s : list) {
			sb.append(s);
			sb.append("\n");
		}
		return sb.toString();
	}

	public void cutSelectedText() {
		resultArea.cut();
	}
	
	public void copySelectedText() {
		resultArea.copy();
	}
	
	public void pasteText() {
		resultArea.paste();
	}
	

	/**
	 * Creates an XML display dialog to show rules with proper syntax highlighting
	 * @author mbryant
	 *
	 */
	class XmlDisplay implements ActionListener {

		private JFrame xmlframe;
		private XmlTextPane pane;
		private JScrollPane scrollpane;
		private JButton done;
		private JButton cancel;
		private String text;
		private String title;
		private String fn;

		public XmlDisplay(String text, String filename, String title) {
			this.text = text;
			this.fn = filename;
			this.title = title;
		}

		public boolean isVisible() {
			return this.xmlframe.isVisible();
		}

		public void show() {
			xmlframe = new JFrame(title);
			final Container contentPane = xmlframe.getContentPane();
			final GridBagLayout gridLayout = new GridBagLayout();
			contentPane.setLayout(gridLayout);
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1f;
			c.weighty = 1f;

			pane = new XmlTextPane();
			pane.setText(text);
			pane.requestFocusInWindow();
			scrollpane = new JScrollPane(pane);
			scrollpane
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			done = new JButton("Done");
			done.addActionListener(this);
			done.setMnemonic('D');
			cancel = new JButton("Cancel");
			cancel.addActionListener(this);
			cancel.setMnemonic('C');

			c.gridwidth = 2;
			contentPane.add(scrollpane, c);
			c.gridy = 1;
			c.weighty = 0;
			c.gridwidth = 1;
			c.fill = GridBagConstraints.NONE;
			contentPane.add(done, c);
			c.gridx = 1;
			contentPane.add(cancel, c);

			final JRootPane rootPane = xmlframe.getRootPane();
			final KeyStroke escStroke = KeyStroke.getKeyStroke(
					KeyEvent.VK_ESCAPE, 0);
			final ActionListener actionListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					xmlframe.setVisible(false);
				}
			};
			rootPane.registerKeyboardAction(actionListener, escStroke,
					JComponent.WHEN_IN_FOCUSED_WINDOW);
			// press ctrl+enter to OK-exit
			final KeyStroke enterStroke = KeyStroke.getKeyStroke(
					KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK);
			final ActionListener actionListener2 = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					done.doClick();
				}
			};
			rootPane.registerKeyboardAction(actionListener2, enterStroke,
					JComponent.WHEN_IN_FOCUSED_WINDOW);

			xmlframe.pack();
			xmlframe.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
			xmlframe.setVisible(true);
		}

		public String getText() {
			return this.text;
		}

		private void write() {
			if (!this.text.isEmpty()) {
				try {
					PrintWriter w = new PrintWriter(new OutputStreamWriter(
							new FileOutputStream(fn), "UTF-8"));
					w.write(this.text);
					w.close();
					showDialog("Written to " + fn, null);
				} catch (IOException e) {
					showError(e);
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == done) {
				this.text = pane.getText();
				this.write();
				xmlframe.setVisible(false);
			} else if (e.getSource() == cancel) {
				xmlframe.setVisible(false);
			}
		}
	}
	
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

}
