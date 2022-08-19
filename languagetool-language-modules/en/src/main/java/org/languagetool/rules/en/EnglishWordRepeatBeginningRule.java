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
package org.languagetool.rules.en;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatBeginningRule;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adds a list English adverbs to {@link WordRepeatBeginningRule}.
 *
 * @author Markus Brenneis
 */
public class EnglishWordRepeatBeginningRule extends WordRepeatBeginningRule {
  
  public EnglishWordRepeatBeginningRule(ResourceBundle messages, Language language) {
    super(messages, language);
    addExamplePair(Example.wrong("Moreover, the street is almost entirely residential. <marker>Moreover</marker>, it was named after a poet."),
                   Example.fixed("Moreover, the street is almost entirely residential. <marker>It</marker> was named after a poet."));
  }
  
  @Override
  public String getId() {
    return "ENGLISH_WORD_REPEAT_BEGINNING_RULE";
  }
  
  //==================== ADVERBS ======================
  
	// adverbs used to add to what the previous sentence mentioned
	private static final Set<String> ADD_ADVERBS = new HashSet<>();

	// adverbs used to express contrast to what the previous sentence mentioned
	private static final Set<String> CONTRAST_ADVERBS = new HashSet<>();

	// adverbs used to express emphasis to what the previous sentence mentioned
	private static final Set<String> EMPHASIS_ADVERBS = new HashSet<>();

	// adverbs used to explain what the previous sentence mentioned
	private static final Set<String> EXPLAIN_ADVERBS = new HashSet<>();
	
	//==================== EXPRESSIONS ======================
	// the expressions will be used only as additional suggestions
	
	// linking expressions that can be used instead of the ADD_ADVERBS
	private static final List<String> ADD_EXPRESSIONS = Arrays.asList("In addition", "As well as");
	
	// linking expressions that can be used instead of the CONTRAST_ADVERBS
	private static final List<String> CONTRAST_EXPRESSIONS = Arrays.asList("Even so", "On the other hand");
	
	static {
		// based on https://www.pinterest.com/pin/229542912245527548/
		ADD_ADVERBS.add("Additionally");
		ADD_ADVERBS.add("Besides");
		ADD_ADVERBS.add("Furthermore");
		ADD_ADVERBS.add("Moreover");
		ADD_ADVERBS.add("Also");
		CONTRAST_ADVERBS.add("Nevertheless");
		CONTRAST_ADVERBS.add("Nonetheless");
		CONTRAST_ADVERBS.add("Alternatively");
		EMPHASIS_ADVERBS.add("Undoubtedly");
		EMPHASIS_ADVERBS.add("Indeed");
		EMPHASIS_ADVERBS.add("Obviously");
		EMPHASIS_ADVERBS.add("Clearly");
		EMPHASIS_ADVERBS.add("Importantly");
		EMPHASIS_ADVERBS.add("Absolutely");
		EMPHASIS_ADVERBS.add("Definitely");
		EXPLAIN_ADVERBS.add("Particularly");
		EXPLAIN_ADVERBS.add("Especially");
		EXPLAIN_ADVERBS.add("Specifically");
	}

	@Override
	public boolean isException(String token) {
		return super.isException(token) || token.equals("The") || token.equals("A") || token.equals("An");
	}

	@Override
	protected boolean isAdverb(AnalyzedTokenReadings token) {
		String tok = token.getToken();
		return ADD_ADVERBS.contains(tok) || CONTRAST_ADVERBS.contains(tok) || EMPHASIS_ADVERBS.contains(tok)
				|| EXPLAIN_ADVERBS.contains(tok);
	}

	@Override
	protected List<String> getSuggestions(AnalyzedTokenReadings token) {
		String tok = token.getToken();
		// the repeated word is a personal pronoun
		if (token.hasPosTag("PRP")) {
			String adaptedToken = tok.equals("I") ? tok : tok.toLowerCase();
			return Arrays.asList("Furthermore, " + adaptedToken, "Likewise, " + adaptedToken,
					"Not only that, but " + adaptedToken);
		} else if (ADD_ADVERBS.contains(tok)) {
			List<String> addSuggestions = getDifferentAdverbsOfSameCategory(tok, ADD_ADVERBS);
			addSuggestions.addAll(ADD_EXPRESSIONS);
			return addSuggestions;
		} else if (CONTRAST_ADVERBS.contains(tok)) {
			List<String> contrastSuggestions = getDifferentAdverbsOfSameCategory(tok, CONTRAST_ADVERBS);
			contrastSuggestions.addAll(CONTRAST_EXPRESSIONS);
			return contrastSuggestions;
		} else if (EMPHASIS_ADVERBS.contains(tok)) {
			return getDifferentAdverbsOfSameCategory(tok, EMPHASIS_ADVERBS);
		} else if (EXPLAIN_ADVERBS.contains(tok)) {
			return getDifferentAdverbsOfSameCategory(tok, EXPLAIN_ADVERBS);
		}
		return Collections.emptyList();
	}

	/**
	 * Gives suggestions to replace the given adverb.
	 * 
	 * @param adverb            to get suggestions for
	 * @param adverbsOfCategory the adverbs of the same category as adverb (adverb
	 *                          is <b>required</b> to be contained in the Set)
	 * @return a List of suggested adverbs to replace the given adverb
	 */
	private List<String> getDifferentAdverbsOfSameCategory(String adverb, Set<String> adverbsOfCategory) {
		return adverbsOfCategory.stream().filter(adv -> !adv.equals(adverb)).collect(Collectors.toList());
	}
}
