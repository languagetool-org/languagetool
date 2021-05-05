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
package org.languagetool.rules.el;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatBeginningRule;

import java.util.*;
import java.util.stream.*;

/**
 * Adds a list Greek adverbs to {@link WordRepeatBeginningRule}.
 * 
 * @author Georgios Sideris
 */
public class GreekWordRepeatBeginningRule extends WordRepeatBeginningRule {
  
  public GreekWordRepeatBeginningRule(ResourceBundle messages, Language language) {
    super(messages, language);
    addExamplePair(Example.wrong("Επίσης, παίζω ποδόσφαιρο. <marker>Επίσης</marker>, παίζω μπάσκετ."),
                   Example.fixed("Επίσης, παίζω ποδόσφαιρο. <marker>Επιπλέον</marker>, παίζω μπάσκετ."));
  }
  
  @Override
  public String getId() {
    return "GREEK_WORD_REPEAT_BEGINNING_RULE";
  }
  
  //adverbs used to add to what the previous sentence mentioned (English example: Also)
  private static final Set<String> ADD_ADVERBS = new HashSet<>(); 
  
  //adverbs used to express contrast to what the previous sentence mentioned (English example: Contrarily)
  private static final Set<String> CONTRAST_ADVERBS = new HashSet<>(); 
  
  // adverbs used to explain what the previous sentence mentioned (English example: Specifically)
  private static final Set<String> EXPLAIN_ADVERBS = new HashSet<>();
  
  static {
	ADD_ADVERBS.add("Επίσης");
	ADD_ADVERBS.add("Επιπρόσθετα");
	ADD_ADVERBS.add("Ακόμη");
	ADD_ADVERBS.add("Επιπλέον");
	ADD_ADVERBS.add("Συμπληρωματικά");
	CONTRAST_ADVERBS.add("Αντίθετα");
	CONTRAST_ADVERBS.add("Ωστόσο");
	CONTRAST_ADVERBS.add("Εντούτοις");
	CONTRAST_ADVERBS.add("Εξάλλου");
	EXPLAIN_ADVERBS.add("Δηλαδή");
	EXPLAIN_ADVERBS.add("Ειδικότερα");
	EXPLAIN_ADVERBS.add("Ειδικά");
	EXPLAIN_ADVERBS.add("Συγκεκριμένα");
  }

  @Override
  public boolean isException(String token) {
    return super.isException(token) || token.equals("Ο") || token.equals("Η") || token.equals("Το") ||
    	   token.equals("Οι")|| token.equals("Τα");
  }

  @Override
  protected boolean isAdverb(AnalyzedTokenReadings token) {
    return ADD_ADVERBS.contains(token.getToken()) || 
    	   CONTRAST_ADVERBS.contains(token.getToken()) ||
    	   EXPLAIN_ADVERBS.contains(token.getToken());
  }


  @Override
  protected List<String> getSuggestions(AnalyzedTokenReadings token) {
	  String tok = token.getToken();
	  if (ADD_ADVERBS.contains(tok)) {
		  return ADD_ADVERBS.stream()
				  			.filter(adv -> !adv.equals(tok))
				  			.collect(Collectors.toList());
	  } else if (CONTRAST_ADVERBS.contains(tok)) {
		  return CONTRAST_ADVERBS.stream()
		  			.filter(adv -> !adv.equals(tok))
		  			.collect(Collectors.toList());
	  } else if (EXPLAIN_ADVERBS.contains(tok)) {
		  return EXPLAIN_ADVERBS.stream()
		  			.filter(adv -> !adv.equals(tok))
		  			.collect(Collectors.toList());
	  }
	  return Collections.emptyList();
  }

}
