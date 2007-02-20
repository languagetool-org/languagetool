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
package de.danielnaber.languagetool.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.Language;

/** A rule that matches "..", "::", "-," but not "...", "!..", "?!!", ",-" etc.
 * Languages will have to subclass it and override <code>isPunctsJoinOk()</code>
 * and <code>isPuctuation()</code>
 * to provide language-specific checking
 * 
 * @author Andriy Rysin
 */
public abstract class AbstractPunctuationCheckRule extends Rule {

	public AbstractPunctuationCheckRule(final ResourceBundle messages) {
		super(messages);
		super.setCategory(new Category(messages.getString("category_misc")));
	}

	public String getId() {
		return "PUNCTUATION_GENERIC_CHECK";
	}

	public String getDescription() {
		return messages.getString("desc_punctuation_generic_check");
	}

	public abstract Language[] getLanguages();

	protected abstract boolean isPunctsJoinOk(String tkns);

	protected abstract boolean isPunctuation(String token);
	
	public RuleMatch[] match(final AnalyzedSentence text) {
		List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
		AnalyzedTokenReadings[] tokens = text.getTokens();

		String tkns = "";
		for (int i = 0; i < tokens.length; i++) {
			String tokenStr = tokens[i].getToken();
			
			if (isPunctuation(tokenStr)) {
				tkns += tokenStr;
				if (i < tokens.length - 1)
					continue;
			}

			if (tkns.length() >= 2) {
				if (!isPunctsJoinOk(tkns)) {
					String msg = "bad duplication or combination of puctuation signs";
					RuleMatch ruleMatch = new RuleMatch(this, tokens[i].getStartPos()-tkns.length(), tokens[i].getStartPos(), msg);
					ruleMatch.setSuggestedReplacement(tkns.substring(0, 1));
					ruleMatches.add(ruleMatch);
				}
			}
			tkns = "";
		}

		return toRuleMatchArray(ruleMatches);
	}

	public void reset() {
		// nothing
	}

}
