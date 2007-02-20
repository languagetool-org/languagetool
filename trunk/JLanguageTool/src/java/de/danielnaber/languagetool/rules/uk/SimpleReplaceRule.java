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
package de.danielnaber.languagetool.rules.uk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * A rule that matches words or phrases which should not be used 
 * and suggests correct ones instead. Currently only implemented for Ukrainian.
 * Loads the relevant word from <code>rules/uk/replace.txt</code>.
 * 
 * @author Andriy Rysin
 */
public class SimpleReplaceRule extends Rule {

	private static final String FILE_NAME = "rules" +File.separator+ "uk" +File.separator+ "replace.txt";
	private static final String FILE_ENCODING = "utf-8";

	private Map<String, String> wrongWords;        // e.g. "Đ˛Ń€ĐµŃ�Ń‚Ń– Ń€ĐµŃ�Ń‚" -> "Đ·Ń€ĐµŃ�Ń‚ĐľŃŽ"

	public SimpleReplaceRule(ResourceBundle messages) throws IOException {
		if (messages != null)
			super.setCategory(new Category(messages.getString("category_misc")));
		wrongWords = loadWords(JLanguageTool.getAbsoluteFile(FILE_NAME)); 
	}

	public String getId() {
		return "UK_SIMPLE_REPLACE";
	}

	public String getDescription() {
		return "Checks for wrong words/phrases";
	}

	public RuleMatch[] match(AnalyzedSentence text) {
		List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
		AnalyzedTokenReadings[] tokens = text.getTokens();
		int pos = 0;

		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i].getToken();

			if (token.trim().equals("")) {
				// ignore
			} else {
				String origToken = token;
				if (wrongWords.containsKey(token)) {
					String replacement = wrongWords.get(token);
					String msg = token + " is not valid, use " + replacement;
					RuleMatch potentialRuleMatch = new RuleMatch(this, pos, pos+origToken.length(), msg);
					potentialRuleMatch.setSuggestedReplacement(replacement);
//					shouldNotAppearWord.put(shouldNotAppear, potentialRuleMatch);
					ruleMatches.add(potentialRuleMatch);
				}
			}
			pos += tokens[i].getToken().length();
		}
		return toRuleMatchArray(ruleMatches);
	}

	@Override
	public void reset() {
	}

	@Override
	public Language[] getLanguages() {
		return new Language[] { Language.UKRAINIAN };
	}

	private Map<String, String> loadWords(File file) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis, FILE_ENCODING);
			br = new BufferedReader(isr);
			String line;
			
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#") || line.length() == 0 )       // ignore comments
					continue;
				String[] parts = line.split("=");
				if (parts.length != 2) {
					throw new IOException("Format error in file " +file.getAbsolutePath()+ ", line: " + line);
				}
				map.put(parts[0], parts[1]);
			}
			
		} finally {
			if (br != null) br.close();
			if (isr != null) isr.close();
			if (fis != null) fis.close();
		}
		return map;
	}

}
