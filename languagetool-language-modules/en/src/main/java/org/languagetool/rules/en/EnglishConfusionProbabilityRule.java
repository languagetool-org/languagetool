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
      "your slack profile",
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
      "in a while",
      "confirm you own",
      "oh, god",
      "honey nut",
      "not now",
      "he proofs",
      "he needs",
      "1 thing",
      "way easier",
      "way faster",
      "way quicker",
      "way more",
      "way less",
      "way outside",
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
      "way too early", // vs was
      "way too soon", // vs was
      "way too quick", // vs was
      "then,", // vs than
      "then?", // vs than
      "no it doesn", // vs know
      "no it isn",
      "no it wasn",
      "no it hasn",
      "no it can't",
      "no it won't",
      "no it wouldn",
      "no it couldn",
      "no it shouldn",
      "no that's not", // vs know
      "provided my country",
      "no i don't",
      "no i can't",
      "no i won't",
      "no i wasn",
      "no i haven",
      "no i wouldn",
      "no i couldn",
      "no i shouldn",
      "no you don't",
      "no you can't",
      "no you won't",
      "no you weren",
      "no you haven",
      "no you wouldn",
      "no you couldn",
      "no you shouldn",
      "no they were not",
      "no we were not",
      "for your recharge", // vs you
      "all you kids", // vs your
      "thanks for the patience", // vs patients
      "what to text", // vs do
      "is he famous for", // vs the
      "was he famous for", // the
      "really quiet at", // vs quit/quite
      "he programs", // vs the
      "scene 1", // vs seen
      "scene 2",
      "scene 3",
      "scene 4",
      "scene 5",
      "scene 6",
      "scene 7",
      "scene 8",
      "scene 9",
      "scene 10",
      "scene 11",
      "scene 12",
      "scene 13",
      "scene 14",
      "scene 15",
      "make a hire",
      "on the news",
      "brown plane",
      "news politics",
      "organic reach",
      "out bid",
      "message us in",
      "I picture us",
      "your and our", // vs you
      "house and pool",
      "your set up is",
      "your set up was",
      "because your pay is",
      "but your pay is",
      "the while block", // dev speech
      "updated my edge", // vs by
      "he haven", // vs the
      "is he naked", // vs the
      "these news sound", // vs new
      "those news sound", // vs new
      "(t)he", // vs the
      "[t]he", // vs the
      "the role at", // vs add
      "same false alarm", // vs some
      "why is he relevant", // vs the
      "then that would", // vs than
      "was he part of", // vs the
      "is he right now", // vs the
      "news site",
      "news website",
      "news organization",
      "news organisation",
      "scene in a movie",
      "mr.bean", // vs been
      "mr. bean", // vs been
      "your push notification", // vs you
      "check our page about",
      "have a think", // vs thing (see https://www.lexico.com/definition/think)
      "had a think",
      "live support", // vs life
      "your troll", // vs you
      "waiste type", // vs waste
      "(wait)",
      "now ahead of schedule", // vs know
      "of you being her" // vs your
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
