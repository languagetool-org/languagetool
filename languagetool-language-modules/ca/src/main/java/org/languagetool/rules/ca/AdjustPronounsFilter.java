/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.AnalyzedToken;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tools.StringTools;

/*
 * Add the pronoun "en" in the required place with all the necessary transformations, 
 * including moving the <marker> positions.
 */

public class AdjustPronounsFilter extends RuleFilter {

  Pattern pApostropheNeeded = Pattern.compile("h?[aeiouàèéíòóú].*", Pattern.CASE_INSENSITIVE);

  static private CatalanSynthesizer synth = CatalanSynthesizer.INSTANCE;

  private static Map<String, String> addEnApostrophe = new HashMap<>();
  static {
    addEnApostrophe.put("m'", "me n'");
    addEnApostrophe.put("t'", "te n'");
    addEnApostrophe.put("s'", "se n'");
    addEnApostrophe.put("ens", "ens n'");
    addEnApostrophe.put("us", "us n'");
    addEnApostrophe.put("vos", "vos n'");
    addEnApostrophe.put("li", "li n'");
    addEnApostrophe.put("els", "els n'");
    addEnApostrophe.put("se m'", "se me n'");
    addEnApostrophe.put("se t'", "se te n'");
    addEnApostrophe.put("se li", "se li n'");
    addEnApostrophe.put("se'ns", "se'ns n'");
    addEnApostrophe.put("se us", "se us n'");
    addEnApostrophe.put("se vos", "se vos n'");
    addEnApostrophe.put("se'ls", "se'ls n'");
    addEnApostrophe.put("hi", "n'hi ");
    addEnApostrophe.put("", "n'");
  }

  private static Map<String, String> addEn = new HashMap<>();
  static {
    addEn.put("em", "me'n");
    addEn.put("et", "te'n");
    addEn.put("es", "se'n");
    addEn.put("se", "se'n");
    addEn.put("ens", "ens en");
    addEn.put("us", "us en");
    addEn.put("li", "li'n");
    addEn.put("els", "els en");
    addEn.put("se'm", "se me'n");
    addEn.put("se't", "se te'n");
    addEn.put("se li", "se li'n");
    addEn.put("se'ns", "se'ns en");
    addEn.put("se us", "se us en");
    addEn.put("se vos", "se vos en");
    addEn.put("se'ls", "se'ls en");
    addEn.put("hi", "n'hi");
    addEn.put("", "en");
  }

  private static Map<String, String> removeReflexive = new HashMap<>();
  static {
    removeReflexive.put("em", "");
    removeReflexive.put("me", "");
    removeReflexive.put("m'", "");
    removeReflexive.put("et", "");
    removeReflexive.put("te", "");
    removeReflexive.put("t'", "");
    removeReflexive.put("es", "");
    removeReflexive.put("se", "");
    removeReflexive.put("s'", "");
    removeReflexive.put("ens", "");
    removeReflexive.put("us", "");
    removeReflexive.put("vos", "");
    removeReflexive.put("se'm", "em");
    removeReflexive.put("se m'", "m'");
    removeReflexive.put("se't", "et");
    removeReflexive.put("se t'", "t'");
    removeReflexive.put("se li", "li");
    removeReflexive.put("se'ns", "ens");
    removeReflexive.put("se us", "us");
    removeReflexive.put("se'ls", "els");
  }

  private static Map<String, String> addReflexiveVowel = new HashMap<>();
  static {
    addReflexiveVowel.put("1S", "m'");
    addReflexiveVowel.put("2S", "t'");
    addReflexiveVowel.put("3S", "s'");
    addReflexiveVowel.put("1P", "ens ");
    addReflexiveVowel.put("2P", "us ");
    addReflexiveVowel.put("3P", "s'");
  }

  private static Map<String, String> addReflexiveConsonant = new HashMap<>();
  static {
    addReflexiveConsonant.put("1S", "em ");
    addReflexiveConsonant.put("2S", "et ");
    addReflexiveConsonant.put("3S", "es ");
    addReflexiveConsonant.put("1P", "ens ");
    addReflexiveConsonant.put("2P", "us ");
    addReflexiveConsonant.put("3P", "es ");
  }

  private static Map<String, String> addReflexiveImperative = new HashMap<>();
  static {
    addReflexiveImperative.put("2S", "'t");
    addReflexiveImperative.put("3S", "'s");
    addReflexiveImperative.put("1P", "-nos");
    addReflexiveImperative.put("2P", "-vos");
    addReflexiveImperative.put("3P", "-se");
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {

    List<String> replacements = new ArrayList<>();
    List<String> actions = Arrays.asList(getRequired("actions", arguments).split(","));
    String newLemma = getOptional("newLemma", arguments);
    int posWord = 0;
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    while (posWord < tokens.length
        && (tokens[posWord].getStartPos() < match.getFromPos() || tokens[posWord].isSentenceStart())) {
      posWord++;
    }
    int toLeft = 0;
    boolean done = false;
    String firstVerb = "";
    String firstVerbPersonaNumber = "";
    String firstVerbPersonaNumberImperative = "";
    String replacementVerb = "";
    int firstVerbPos = 0;
    boolean inPronouns = false;
    boolean firstVerbValid = false;
    while (!done && posWord - toLeft > 0) {
      AnalyzedTokenReadings currentTkn = tokens[posWord - toLeft];
      String currentTknStr = currentTkn.getToken();
      // change lemma if asked
      if (toLeft == 0 && newLemma != null) {
        List<String> postags = new ArrayList<>();
        for (AnalyzedToken reading : currentTkn) {
          if (reading.getPOSTag() != null && reading.getPOSTag().startsWith("V")) {
            postags.add(reading.getPOSTag());
          }
        }
        String targetPostag = synth.getTargetPosTag(postags, "");
        if (!targetPostag.isEmpty()) {
          AnalyzedToken at = new AnalyzedToken(currentTknStr, targetPostag, newLemma);
          String[] synthForms = synth.synthesize(at, targetPostag);
          if (synthForms != null && synthForms.length > 0) {
            replacementVerb = synthForms[0];
          }
        }
      }
      boolean isVerb = currentTkn.hasPosTagStartingWith("V");
      boolean isPronoun = currentTkn.matchesPosTagRegex("P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP3CP000|PP3CSD00");
      if (isPronoun) {
        inPronouns = true;
      }
      if (isPronoun || (isVerb && !inPronouns && !firstVerbValid) || currentTknStr.equalsIgnoreCase("de")
          || currentTknStr.equalsIgnoreCase("d'")) {
        if (isVerb) {
          firstVerb = currentTknStr;
          firstVerbPos = toLeft;
          firstVerbValid = currentTkn.matchesPosTagRegex("V.[SI].*");
          if (firstVerbValid) {
            firstVerbPersonaNumber = currentTkn.readingWithTagRegex("V.[SI].*").getPOSTag().substring(4, 6);
          }
          if (currentTkn.matchesPosTagRegex("V.M.*")) {
            firstVerbPersonaNumberImperative = currentTkn.readingWithTagRegex("V.M.*").getPOSTag().substring(4, 6);
          }
        }
        toLeft++;
      } else {
        done = true;
        if (toLeft > 0) {
          toLeft--;
        }
      }
    }
    if (!firstVerbValid) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = posWord - toLeft; i < posWord - firstVerbPos; i++) {
      sb.append(tokens[i].getToken());
      if (tokens[i + 1].isWhitespaceBefore()) {
        sb.append(" ");
      }
    }
    String pronounsStr = sb.toString().trim();
    sb = new StringBuilder();
    for (int i = posWord - firstVerbPos; i <= posWord; i++) {
      if (i == posWord && !replacementVerb.isEmpty()) {
        sb.append(replacementVerb);
      } else {
        sb.append(tokens[i].getToken());
      }
      if (i + 1 < tokens.length && tokens[i + 1].isWhitespaceBefore()) {
        sb.append(" ");
      }
    }
    String verbStr = sb.toString().trim();
    for (String action : actions) {
      String replacement = "";
      switch (action) {
      case "addPronounEn":
        replacement = doAddPronounEn(firstVerb, pronounsStr, verbStr);
        break;
      case "removePronounReflexive":
        replacement = doRemovePronounReflexive(firstVerb, pronounsStr, verbStr);
        break;
      case "replaceEmEn":
        replacement = doReplaceEmEn(firstVerb, pronounsStr, verbStr);
        break;
      case "addPronounReflexive":
        replacement = doAddPronounReflexive(firstVerb, pronounsStr, verbStr, firstVerbPersonaNumber);
        break;
      case "addPronounReflexiveImperative":
        replacement = doAddPronounReflexiveImperative(firstVerb, pronounsStr, verbStr,
            firstVerbPersonaNumberImperative);
        break;
      }
      if (!replacement.isEmpty()) {
        replacements.add(replacement);
      }
    }
    if (replacements.isEmpty()) {
      return null;
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[posWord - toLeft].getStartPos(),
        match.getToPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacements(replacements);
    return ruleMatch;
  }

  private String doAddPronounEn(String firstVerb, String pronounsStr, String verbStr) {
    Map<String, String> transform;
    String replacement = "";
    String between = "";
    if (pApostropheNeeded.matcher(firstVerb).matches()) {
      transform = addEnApostrophe;
    } else {
      transform = addEn;
      between = " ";
    }
    String pronounsReplacement = transform.get(pronounsStr.toLowerCase());
    if (pronounsReplacement != null) {
      replacement = StringTools.preserveCase(pronounsReplacement, (pronounsStr + between + verbStr).trim()) + between
          + verbStr.toLowerCase();
    }
    return replacement;
  }

  private String doRemovePronounReflexive(String firstVerb, String pronounsStr, String verbStr) {
    String replacement = "";
    String between = " ";
    String pronounsReplacement = removeReflexive.get(pronounsStr.toLowerCase());
    if (pronounsReplacement != null) {
      replacement = StringTools.preserveCase(pronounsReplacement + between + verbStr, pronounsStr).trim()
          .replaceAll("' ", "'");
    }
    return replacement;
  }

  private String doAddPronounReflexive(String firstVerb, String pronounsStr, String verbStr,
      String firstVerbPersonaNumber) {
    String pronounToAdd = "";
    String replacement = "";
    if (pronounsStr.isEmpty()) {
      if (pApostropheNeeded.matcher(verbStr).matches()) {
        pronounToAdd = addReflexiveVowel.get(firstVerbPersonaNumber);
      } else {
        pronounToAdd = addReflexiveConsonant.get(firstVerbPersonaNumber);
      }
      if (pronounToAdd != null) {
        replacement = StringTools.preserveCase(pronounToAdd + verbStr, verbStr).trim().replaceAll("' ", "'");
      }
    }
    return replacement;
  }

  private String doAddPronounReflexiveImperative(String firstVerb, String pronounsStr, String verbStr,
      String firstVerbPersonaNumber) {
    String pronounToAdd = "";
    String replacement = "";
    if (pronounsStr.isEmpty()) {
      pronounToAdd = addReflexiveImperative.get(firstVerbPersonaNumber);
      if (pronounToAdd != null) {
        replacement = StringTools.preserveCase(verbStr + pronounToAdd, verbStr).trim();
      }
    }
    return replacement;
  }

  private String doReplaceEmEn(String firstVerb, String pronounsStr, String verbStr) {
    String replacement = "";
    if (pronounsStr.equalsIgnoreCase("em")) {
      replacement = StringTools.preserveCase("en", pronounsStr) + " " + verbStr;
    }
    if (pronounsStr.equalsIgnoreCase("m'")) {
      replacement = StringTools.preserveCase("n'", pronounsStr) + verbStr;
    }
    if (pronounsStr.equalsIgnoreCase("m'hi")) {
      replacement = StringTools.preserveCase("n'hi ", pronounsStr) + verbStr;
    }
    return replacement;
  }

}
