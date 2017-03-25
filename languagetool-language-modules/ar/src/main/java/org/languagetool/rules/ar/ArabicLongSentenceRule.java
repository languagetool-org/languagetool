/* LanguageTool, a natural language style checker
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
package org.languagetool.rules.ar;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.LongSentenceRule;
import org.languagetool.rules.RuleMatch;

public class ArabicLongSentenceRule extends LongSentenceRule {

	private static final Pattern NON_WORD_REGEX = Pattern.compile("[،؟؛.?!:;,~’'\"„“»«‚‘›‹()\\[\\]-]");
	private final int maxWords;

	public ArabicLongSentenceRule(ResourceBundle messages, int maxSentenceLength) {
		super(messages, maxSentenceLength);
		maxWords = maxSentenceLength;
	}

	@Override
	public final String getId() {
		return "ARABIC_TOO_LONG_SENTENCE";
	}

	@Override
	public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
		List<RuleMatch> ruleMatches = new ArrayList<>();
		AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
		String msg = MessageFormat.format(messages.getString("long_sentence_rule_msg"), maxWords);
		int numWords = 0;
		int pos = 0;
		if (tokens.length < maxWords + 1) { // just a short-circuit
			return toRuleMatchArray(ruleMatches);
		} else {
			for (AnalyzedTokenReadings aToken : tokens) {
				String token = aToken.getToken();
				pos += token.length(); // won't match the whole offending
										// sentence, but much of it
				if (!aToken.isSentenceStart() && !aToken.isSentenceEnd() && !NON_WORD_REGEX.matcher(token).matches()) {
					numWords++;
				}
			}
		}
		if (numWords > maxWords) {
			RuleMatch ruleMatch = new RuleMatch(this, 0, pos, msg);
			ruleMatches.add(ruleMatch);
		}
		return toRuleMatchArray(ruleMatches);
	}

}
