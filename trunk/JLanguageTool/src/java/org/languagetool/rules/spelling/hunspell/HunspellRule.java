/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.spelling.hunspell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tools.StringTools;

/**
 * A hunspell-based spellchecking-rule.
 * 
 * @author Marcin Miłkowski
 * 
 */
public class HunspellRule extends SpellingCheckRule {

	/**
	 * The dictionary file
	 */
	Hunspell.Dictionary dictionary = null;

	public HunspellRule(final ResourceBundle messages, final Language language)
			throws UnsatisfiedLinkError, UnsupportedOperationException, IOException {
		super(messages, language);
		super.setCategory(new Category(messages.getString("category_typo")));

		// TODO: currently, the default dictionary is now
		// set to the first country variant on the list - so the order
		// in the Language class declaration is important!
		// we might support country variants in the near future

		final String langCountry = language.getShortName()
				+ "_" 
				+ language.getCountryVariants()[0]; 
		
		final String shortDicPath = "/"
				+ language.getShortName()
				+ "/hunspell/"
				+ langCountry
				+ ".dic";

		//set dictionary only if there are dictionary files
		if (JLanguageTool.getDataBroker().resourceExists(shortDicPath)) {

			dictionary = Hunspell.getInstance().
					getDictionary(getDictionaryPath(langCountry, shortDicPath));
			
		}
	}
	
	private final String getDictionaryPath(final String dicName, 
			final String originalPath) throws IOException {
		
		URL dictURL = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(
				originalPath); 
		
		String dictionaryPath = dictURL.getPath();
		
		//in the webstart version, we need to copy the files outside the jar
		//to the local temporary directory
		if ("jar".equals(dictURL.getProtocol())) {
			File tempDir = new File(System.getProperty("java.io.tmpdir"));
			File temporaryFile = new File(tempDir, dicName + ".dic");
			JLanguageTool.addTemporaryFile(temporaryFile);
			fileCopy(JLanguageTool.getDataBroker().
					getFromResourceDirAsStream(originalPath), temporaryFile);
			temporaryFile = new File(tempDir, dicName + ".aff");
			JLanguageTool.addTemporaryFile(temporaryFile);
			fileCopy(JLanguageTool.getDataBroker().
					getFromResourceDirAsStream(originalPath.
							replaceFirst(".dic$", ".aff")), temporaryFile);					 			  			
			
			dictionaryPath = tempDir.getAbsolutePath() + "/" + dicName;
		} else {		
			dictionaryPath = dictionaryPath.substring(0, dictionaryPath.length() - 4);
		}		
		return dictionaryPath;
	}
	
	private void fileCopy(final InputStream in, final File targetFile) throws IOException {
		OutputStream out = new FileOutputStream(targetFile);
		byte[] buf = new byte[1024];
		  int len;
		  while ((len = in.read(buf)) > 0){
			  out.write(buf, 0, len);
		  }
		  in.close();
		  out.close();
	}
	
	@Override
	public String getId() {
		return "HUNSPELL_RULE";
	}

	@Override
	public String getDescription() {
		return messages.getString("desc_spelling");
	}

	@Override
	public RuleMatch[] match(AnalyzedSentence text) throws IOException {
		final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
		final AnalyzedTokenReadings[] tokens = text
				.getTokensWithoutWhitespace();

		// some languages might not have a dictionary, be silent about it
		if (dictionary == null)
			return toRuleMatchArray(ruleMatches);

		// starting with the first token to skip the zero-length START_SENT
		for (int i = 1; i < tokens.length; i++) {
			final String word = tokens[i].getToken();
			boolean isAlphabetic = true;
			if (word.length() == 1) { // hunspell dicts usually do not contain punctuation
				isAlphabetic =
						StringTools.isAlphabetic(word.charAt(0));
			}
			if (isAlphabetic && dictionary.misspelled(word)) {				
				final RuleMatch ruleMatch = new RuleMatch(this,
						tokens[i].getStartPos(), tokens[i].getStartPos() + word.length(),
						messages.getString("spelling"),
						messages.getString("desc_spelling_short"));
				List<String> suggestions = dictionary.suggest(word);
				if (suggestions != null) {
					ruleMatch.setSuggestedReplacements(suggestions);
				}
				ruleMatches.add(ruleMatch);
			}
		}

		return toRuleMatchArray(ruleMatches);
	}

}
