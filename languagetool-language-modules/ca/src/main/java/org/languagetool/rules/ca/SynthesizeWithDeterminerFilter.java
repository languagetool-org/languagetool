package org.languagetool.rules.ca;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SynthesizeWithDeterminerFilter extends RuleFilter {

  static private CatalanSynthesizer synth = CatalanSynthesizer.INSTANCE;

  private static Map<String, String> prepDet = new HashMap<>();
  static {
    prepDet.put("MS", "el ");
    prepDet.put("FS", "la ");
    prepDet.put("MP", "els ");
    prepDet.put("FP", "les ");
    prepDet.put("MSapos", "l'");
    prepDet.put("FSapos", "l'");
    prepDet.put("aMS", "al ");
    prepDet.put("aFS", "a la ");
    prepDet.put("aMP", "als ");
    prepDet.put("aFP", "a les ");
    prepDet.put("aMSapos", "a l'");
    prepDet.put("aFSapos", "a l'");
    prepDet.put("dMS", "del ");
    prepDet.put("dFS", "de la ");
    prepDet.put("dMP", "dels ");
    prepDet.put("dFP", "de les ");
    prepDet.put("dMSapos", "de l'");
    prepDet.put("dFSapos", "de l'");
    prepDet.put("pMS", "pel ");
    prepDet.put("pFS", "per la ");
    prepDet.put("pMP", "pels ");
    prepDet.put("pFP", "per les ");
    prepDet.put("pMSapos", "per l'");
    prepDet.put("pFSapos", "per l'");

  }

  private List<String> genderNumberList = Arrays.asList ("MS", "FS", "MP", "FP");

  private static Map<String, Pattern> genderNumberPatterns = new HashMap<>();
  static {
    genderNumberPatterns.put("MS", Pattern.compile("(N|A.).[MC][SN].*|V.P.*SM.") );
    genderNumberPatterns.put("FS", Pattern.compile("(N|A.).[FC][SN].*|V.P.*SF.") );
    genderNumberPatterns.put("MP", Pattern.compile("(N|A.).[MC][PN].*|V.P.*PM.") );
    genderNumberPatterns.put("FP", Pattern.compile("(N|A.).[FC][PN].*|V.P.*PF.") );
  }

  /** Patterns for apostrophation **/
  private static final Pattern pMascYes = Pattern.compile("h?[aeiouàèéíòóú].*",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern pMascNo = Pattern.compile("h?[ui][aeioàèéóò].+",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern pFemYes = Pattern.compile("h?[aeoàèéíòóú].*|h?[ui][^aeiouàèéíòóúüï]+[aeiou][ns]?|urbs",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern pFemNo = Pattern.compile("host|ira|inxa",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    String lemmaFromStr = getRequired("lemmaFrom", arguments);
    String lemmaSelect = getRequired("lemmaSelect", arguments);
    boolean synthAllForms = getOptional("synthAllForms", arguments, "false").equalsIgnoreCase("true")? true: false;
    String prepositionFromStr = getOptional("prepositionFrom", arguments, "");
    int lemmaFrom = getPosition(lemmaFromStr, patternTokens, match);
    String preposition="";
    if (StringUtils.isNumeric(prepositionFromStr)) {
      int prepositionFrom = getPosition(prepositionFromStr, patternTokens, match);
      preposition = patternTokens[prepositionFrom].getToken().substring(0, 1).toLowerCase();
    } else if (!prepositionFromStr.isEmpty()) {
      //a=a d=de per=p
      preposition = prepositionFromStr.substring(0,1);
    }
    List<String> suggestions = new ArrayList<>();
    String originalWord = patternTokens[lemmaFrom].getToken();
    Pattern p = Pattern.compile(lemmaSelect);
    boolean isSentenceStart = isMatchAtSentenceStart(match.getSentence().getTokensWithoutWhitespace(), match);
    List<AnalyzedToken> potentialSuggestions = new ArrayList<>();
    // original word form in the first place
    AnalyzedToken originalAT = patternTokens[lemmaFrom].readingWithTagRegex(lemmaSelect);
    potentialSuggestions.add(originalAT);
    // second-best suggestion
    String secondGenderNumber = "";
    if (lemmaFrom-1 > 0) {
      AnalyzedToken reading = patternTokens[lemmaFrom - 1].readingWithTagRegex("D.*");
      if (reading != null) {
        secondGenderNumber = reading.getPOSTag().substring(3,5);
      }
    }
    for (String tag : synth.getPossibleTags()) {
      Matcher m = p.matcher(tag);
      if (m.matches()) {
        String[] synthForms = synth.synthesize(originalAT, tag);
        for (String synthForm : synthForms) {
          AnalyzedToken at = new AnalyzedToken(synthForm, tag, originalAT.getLemma());
          if (!synthAllForms && !synthForm.equalsIgnoreCase(originalWord)) {
            continue;
          }
          if (!potentialSuggestions.contains(at)) {
            if (tag.contains(secondGenderNumber)
              || tag.contains(secondGenderNumber.substring(1,2)+secondGenderNumber.substring(0,1))) {
              potentialSuggestions.add(1, at);
            } else {
              potentialSuggestions.add(at);
            }
          }
        }
      }
    }
    for (AnalyzedToken potentialSuggestion : potentialSuggestions) {
      String form = potentialSuggestion.getToken();
      for (String genderNumber : genderNumberList) {
        String apos = ""; // s'apostrofa o no
        if (genderNumber.equals("MS")) {
          if (pMascYes.matcher(form).matches() && !pMascNo.matcher(form).matches()) {
            apos = "apos";
          }
        } else if (genderNumber.equals("FS")) {
          if (pFemYes.matcher(form).matches() && !pFemNo.matcher(form).matches()) {
            apos = "apos";
          }
        }
        if (genderNumberPatterns.get(genderNumber).matcher(potentialSuggestion.getPOSTag()).matches()) {
          String suggestion = prepDet.get(preposition + genderNumber + apos) + StringTools.preserveCase(form, originalWord);
          if (isSentenceStart) {
            suggestion = StringTools.uppercaseFirstChar(suggestion);
          }
          if (!suggestions.contains(suggestion)) {
            suggestions.add(suggestion);
          }
        }
      }

    }
    match.addSuggestedReplacements(suggestions);
    return match;
  }

}
