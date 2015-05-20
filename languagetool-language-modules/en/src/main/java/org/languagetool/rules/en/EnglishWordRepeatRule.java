/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatRule;

/**
 * Word repeat rule for English, to avoid false alarms in the generic word repetition rule.
 */
public class EnglishWordRepeatRule extends WordRepeatRule {

  public EnglishWordRepeatRule(final ResourceBundle messages, final Language language) {
    super(messages, language);
    addExamplePair(Example.wrong("This <marker>is is</marker> just an example sentence."),
                   Example.fixed("This <marker>is</marker> just an example sentence."));
  }

  @Override
  public String getId() {
    return "ENGLISH_WORD_REPEAT_RULE";
  }

  @Override
  public boolean ignore(AnalyzedTokenReadings[] tokens, int position) {
    if (wordRepetitionOf("had", tokens, position)&& prePOSIsIn(tokens, position, "PRP")) {
      return true;   // "If I had had time, I would have gone to see him."
    }
    if (wordRepetitionOf("that", tokens, position) && nextPOSIsIn(tokens, position, "NN", "PRP$", "JJ", "VBZ", "VBD")) {
      return true;   // "I don't think that that is a problem."
    }
    if (wordRepetitionOf("can", tokens, position)&& POSIsIn(tokens, position, "NN")) {
			return true; // "The can can hold the water."
		}
    if (wordRepetitionOf("Pago", tokens, position)) {
      return true;   // "Pago Pago"
    }
    if (wordRepetitionOf("Wagga", tokens, position)) {
      return true;   // "Wagga Wagga"
    }
    if (wordRepetitionOf("Duran", tokens, position)) {
      return true;   // "Duran Duran"
    }
    if (wordRepetitionOf("sapiens", tokens, position)) {
      return true;   // "Homo sapiens sapiens"
    }
    if (wordRepetitionOf("tse", tokens, position)) {
      return true;   // "tse tse"
    }
    if (wordRepetitionOf("Li", tokens, position)) {
      return true;   // "Li Li", Chinese name
    }
    return false;
  }

  private boolean nextPOSIsIn(AnalyzedTokenReadings[] tokens, int position, String... posTags) {
    if (tokens.length > position + 1) {
      for (String posTag : posTags) {
        if (tokens[position + 1].hasPartialPosTag(posTag)) {
          return true;
        }
      }
    }
    return false;
  }
 /**
	 * @author Mility
	 * @since 2015/5/15
	 * @param tokens
	 * @param position
	 * @param posTags
	 * @return 功能：判断第一“can”的标签是否是“NN”，如果是则返回true，否则返回false
	 */
	private boolean POSIsIn(AnalyzedTokenReadings[] tokens, int position,String... posTags) {
		if (tokens.length > position - 1) {
			for (String posTag : posTags) {
				if (tokens[position - 1].hasPartialPosTag(posTag)) {
					return true;
				}
			}

		}
		return false;
	}

	/**
	 * @author Mility
	 * @since 2015/5/15
	 * @param tokens
	 * @param position
	 * @param posTags
	 * @return 功能：判断第一“had”前是否有“PRP”（人称代词），如果有则返回true，否则返回false
	 */
	private boolean prePOSIsIn(AnalyzedTokenReadings[] tokens, int position,String... posTags) {
		if (tokens.length > position - 2) {
			for (String posTag : posTags) {
				if (tokens[position - 2].hasPartialPosTag(posTag)) {
					return true;
				}
			}

		}
		return false;
	}
  private boolean wordRepetitionOf(String word, AnalyzedTokenReadings[] tokens, int position) {
    return position > 0 && tokens[position - 1].getToken().equals(word) && tokens[position].getToken().equals(word);
  }

}
