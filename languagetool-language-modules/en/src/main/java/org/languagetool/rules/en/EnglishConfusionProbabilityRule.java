/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.ngrams.ConfusionProbabilityRule;
import org.languagetool.rules.Example;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @since 2.7
 */
public class EnglishConfusionProbabilityRule extends ConfusionProbabilityRule {

  private static final List<String> EXCEPTIONS = Arrays.asList(
      // Use all-lowercase, matches will be case-insensitive.
      // See https://github.com/languagetool-org/languagetool/issues/1678
      "host to five",   // "... is host to five classical music orchestras"
      "had I known",
      "is not exactly known",
      "live duet",
      "isn't known",
      "your move makes",
      "your move is",
      "he unchecked the",
      "thank you for the patience",
      "your patience regarding",
      "your fix",  // fix = bug fix
      "your commit",
      "on point",
      "chapter one",
      "usb port",
      // The quote in this case in management means: "Know the competition and your software, and you will win.":
      "know the competition and",
      "know the competition or",
      "know your competition and",
      "know your competition or",
      "G Suite",
      "paste event",
      "need to know",
      "of you not",   // "It would be wiser of you not to see him again."
      "of her element",
      "very grateful of you",
      "your use case",
      "he's", // vs. the's
      "he’s",
      "they're",
      "they’re",
      "your look is", // Really, your look is
      "have you known",// vs. "know"
      "have I known",// vs. "know"
      "had I known",
      "had you known",
      "his fluffy butt",
      "it's now better", // vs. no
      "it’s now better", // vs. no
      "it is now better", // vs. no
      "let us know below",
      "let us know in",
      "your kind of",
      "sneak peek",
      "the 4 of you",
      "your ride",
      "he most likely",
      "good cause",
      "big butt",
      "news debate",
      "verify you own",
      "ensure you own",
      "happy us!",
      "your pick up",
      "no but you",
      "no but we",
      "no but he",
      "no but I",
      "no but they",
      "no but she",
      "no but it",
      "he tracks",
      "which complains about", // vs complaints
      "do your work", // vs "you"
      "one many times", // vs "on"
      "let us know",
      "let me know",
      "this way the",
      "save you money",
      "way better",
      "so your plan",
      "the news man",
      "created us equally",
      ", their,",
      ", your,",
      ", its,",
      "us, humans,",
      "bring you happiness",
      "doing way more",
      "in a while",
      "confirm you own",
      "oh, god",
      "honey nut",
      "not now",
      "he proofs",
      "he needs",
      "1 thing",
      "way easier",
      "way more",
      "I now don't",
      "once your return is",
      "can we text",
      "believe in god",
      "on premise",
      "from poor to rich",
      "my GPU",
      "was your everything",
      "they mustnt", // different error
      "reply my email",
      "things god said",
      "let you text",
      "doubt in god",
      "in the news",
      "(news)",
      "fresh prince of",
      "good day bye",
      "it's us",
      "could be us being", // vs is
      "on twitter", // vs in
      "enjoy us being", // vs is
      "If your use of", // vs you
      "way too much", // vs was
      "then,", // vs than
      "then?", // vs than
      "also no it doesn", // vs know
      "also no it isn",
      "also no it wasn",
      "also no it hasn",
      "also no it can't",
      "also no it won't",
      "also no it wouldn",
      "also no it couldn",
      "also no it shouldn",
      "no that's not", // vs know
      "provided my country",
      "so no i don't",
      "so no i can't",
      "so no i won't",
      "so no i wasn",
      "so no i haven",
      "so no i wouldn",
      "so no i couldn",
      "so no i shouldn",
      "so no you don't",
      "so no you can't",
      "so no you won't",
      "so no you weren",
      "so no you haven",
      "so no you wouldn",
      "so no you couldn",
      "so no you shouldn",
      "for your recharge", // vs you
      "all you kids", // vs your
      "thanks for the patience", // vs patients
      "what to text", // vs do
      "is he famous for", // vs the
      "was he famous for", // the
      "really quiet at", // vs quit/quite
      "he programs" // vs the
    );
    
  public EnglishConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    this(messages, languageModel, language, 3);
  }

  public EnglishConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language, int grams) {
    super(messages, languageModel, language, grams, EXCEPTIONS);
    addExamplePair(Example.wrong("I did not <marker>now</marker> where it came from."),
                   Example.fixed("I did not <marker>know</marker> where it came from."));
  }

  @Override
  protected boolean isException(String sentence, int startPos, int endPos) {
    if (startPos > 3) {
      String covered = sentence.substring(startPos-3, endPos);
      // the Google ngram data expands negated contractions like this: "Negations (n't) are normalized so
      // that >don't< becomes >do not<." (Source: https://books.google.com/ngrams/info)
      // We don't deal with that yet (see GoogleStyleWordTokenizer), so ignore for now:
      if (covered.matches("['’`´‘]t .*")) {
        return true;
      }
    }
    return false;
  }

}
