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

  public EnglishWordRepeatRule(ResourceBundle messages, Language language) {
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
    if (position == 0) {
      return false;
    }
    if (wordRepetitionOf("had", tokens, position) && posIsIn(tokens, position - 2, "PRP", "NN")) {
      return true;   // "If I had had time, I would have gone to see him."
    }
    if (wordRepetitionOf("that", tokens, position) && posIsIn(tokens, position+1, "NN", "PRP$", "JJ", "VBZ", "VBD")) {
      return true;   // "I don't think that that is a problem."
    }
    if (wordRepetitionOf("can", tokens, position) && posIsIn(tokens, position-1, "NN")) {
      return true; // "The can can hold the water."
    }
    if (wordRepetitionOf("blah", tokens, position)) {
      return true;   // "blah blah"
    }
    if (wordRepetitionOf("yadda", tokens, position)) {
      return true;   // "yadda yadda"
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
    //addition by ahsen on 17.03.19 
    if (wordRepetitionOf("tut", tokens, position)) {
        return true;   // "tut tut"
      }
    if (wordRepetitionOf("now", tokens, position)) {
        return true;   // "now now"
      }
    if (wordRepetitionOf("knock", tokens, position)) {
        return true;   // "knock knock"
      }
    if (wordRepetitionOf("chop", tokens, position)) {
        return true;   // "chop chop"
      }
    if (wordRepetitionOf("aye", tokens, position)) {
        return true;   // "aye aye"
      }
    //end of the addition
    //Additions by Lumpus99
    if (wordRepetitionOf("do", tokens, position)) {
      return true;   // "That's not what you do. What you do do is this."
    }
    if (wordRepetitionOf("this", tokens, position)) {
      return true;   // "I received this this morning."
    }
    if (wordRepetitionOf("clap", tokens, position)) {
      return true;   // "Clap clap"
    }
    //End of editions by Lumpus99
    //Additions by Grisworld
    if (wordRepetitionOf("that", tokens, position)) {
      return true;   // "There was a problem with my code. I fixed that that night."
    }
    if (wordRepetitionOf("bang", tokens, position)) {
      return true;   // bang bang
    }
    if (wordRepetitionOf("for", tokens, position)) {
      return true;   // You wrote these for if statements.You must write them all for for loops.
    }
    if (wordRepetitionOf("had", tokens, position)) {
      return true;   // When her father was not at house at that night,she had had one telescope to watch her father coming.
    }

    //End of additions by Grisworld
    if (tokens[position].getToken().endsWith("ay")) {
      if (tokens[position - 1].getToken().equals("may") && tokens[position].getToken().equals("May")) {
        return true;   // "may May"
      }
      if (tokens[position - 1].getToken().equals("May") && tokens[position].getToken().equals("may")) {
        return true;   // "May may"
      }
      if (tokens[1].getToken().equals("May") && tokens[2].getToken().equals("May")) {
        return true;   // "May May" SENT_START
      }
    }
    if (tokens[position].getToken().endsWith("ill")) {
      return (position > 0 && tokens[position - 1].getToken().equals("will") && tokens[position].getToken().equals("Will")) // will Wills
        || (tokens[position - 1].getToken().equals("Will") && tokens[position].getToken().equals("will")) // Will will ...
        || (tokens[1].getToken().equals("Will") && tokens[2].getToken().equals("Will")); // "Will Will" SENT_START
    }
    return false;
  }

  private boolean posIsIn(AnalyzedTokenReadings[] tokens, int position, String... posTags) {
    if (position >= 0 && position < tokens.length) {
      for (String posTag : posTags) {
        if (tokens[position].hasPartialPosTag(posTag)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean wordRepetitionOf(String word, AnalyzedTokenReadings[] tokens, int position) {
    return tokens[position - 1].getToken().equals(word) && tokens[position].getToken().equals(word);
  }

}
