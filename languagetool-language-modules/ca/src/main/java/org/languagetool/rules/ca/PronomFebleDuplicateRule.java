package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

public class PronomFebleDuplicateRule extends Rule {

  // tots els pronoms febles
  private static final Pattern PRONOM_FEBLE = Pattern.compile("P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP[123]CP000|PP3CSD00");
  private static final List<String> PRONOMS_EN_HI = Arrays.asList("en", "hi");
  private String correctedPronouns = null;
  private final String ruleMessage ="Combinació incorrecta de pronoms febles. Deixeu els de davant o els de darrere del verb.";
  private final String shortMessage ="Combinació incorrecta de pronoms febles.";

  @Override
  public String getId() {
    return "PRONOMS_FEBLES_DUPLICATS";
  }

  @Override
  public String getDescription() {
    return "Pronoms febles duplicats";
  }

  public PronomFebleDuplicateRule(ResourceBundle messages) throws IOException {
    super.setCategory(new Category(new CategoryId("PRONOMS_FEBLES"), "Pronoms febles"));
    setLocQualityIssueType(ITSIssueType.Grammar);
    addExamplePair(Example.wrong("<marker>S'ha de fer-se</marker>."), Example.fixed("<marker>S'ha de fer</marker>."));
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    String PFLemma = "";
    int initPos = -1;
    int lastVerbPos = -1;
    boolean isPrevInfinitive = false;
    List<String> lemesPronomsAbans = new ArrayList<>();
    List<String> lemesPronomsDespres = new ArrayList<>();
    int countVerb = 0;
    boolean inVerbGroup = false;
    for (int i = 1; i < tokens.length; i++) { // ignoring token 0, i.e., SENT_START
      String pfLemma=getLemmaOfPronomFeble(tokens[i]);
      if (!pfLemma.isEmpty()) {
        if (countVerb==0 && (lemesPronomsAbans.size()>0 || tokens[i].isWhitespaceBefore() || tokens[i-1].hasPosTag("SENT_START"))) {
          lemesPronomsAbans.add(pfLemma);
          if (lemesPronomsAbans.size()==1) {
            initPos=i;
          }
          inVerbGroup=true;
        } else if (!tokens[i].isWhitespaceBefore()) {
          lemesPronomsDespres.add(pfLemma);
        } else {
          countVerb=0;
          lemesPronomsAbans.clear();
          lemesPronomsDespres.clear();
          lemesPronomsAbans.add(pfLemma);
          initPos=i;
          inVerbGroup=true;
        }
      } else if (tokens[i].getChunkTags().contains(new ChunkTag("GV")) && !lemesPronomsAbans.isEmpty()
        && lemesPronomsDespres.isEmpty() && !isException(tokens, i)) {
        if (tokens[i].readingWithTagRegex("V.[SI].*") != null && countVerb > 0) {
         inVerbGroup = false;
        } else {
          countVerb++;
          inVerbGroup = true;
          lastVerbPos = i;
        }
      } else {
        inVerbGroup=false;
      }

      if (!inVerbGroup || i==tokens.length-1) {
        if (isThereErrorInLemmas (lemesPronomsAbans, lemesPronomsDespres, tokens, lastVerbPos)) {
          final RuleMatch ruleMatch = new RuleMatch(this, sentence, tokens[initPos].getStartPos(),
            tokens[i-1].getEndPos(), ruleMessage, shortMessage);
          List<String> replacements = new ArrayList<>();
          if (correctedPronouns == null) {
            replacements.add(StringTools.preserveCase(getSuggestionFromTo(tokens, initPos+lemesPronomsAbans.size(),
              initPos+lemesPronomsAbans.size()+countVerb+lemesPronomsDespres.size()), tokens[initPos].getToken()));
            replacements.add(StringTools.preserveCase(getSuggestionFromTo(tokens, initPos,
              initPos+lemesPronomsAbans.size()+countVerb), tokens[initPos].getToken()));
          } else {
            String verbs = getSuggestionFromTo(tokens, initPos+lemesPronomsAbans.size(),
              initPos+lemesPronomsAbans.size()+countVerb);
            replacements.add(StringTools.preserveCase(correctedPronouns + " " + verbs, tokens[initPos].getToken()));
            String pronomsDarrere = PronomsFeblesHelper.transformDarrere(correctedPronouns, verbs);
            replacements.add(StringTools.preserveCase(verbs + pronomsDarrere, tokens[initPos].getToken()));
          }
          ruleMatch.addSuggestedReplacements(replacements);
          ruleMatches.add(ruleMatch);
        }
        countVerb=0;
        lemesPronomsAbans.clear();
        lemesPronomsDespres.clear();
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private String getSuggestionFromTo(AnalyzedTokenReadings[] tokens, int from, int to) {
    StringBuilder sugg = new StringBuilder();
    for (int j=from; j<to; j++) {
      if(tokens[j].isWhitespaceBefore() && sugg.length()>0) {
        sugg.append(" ");
      }
      sugg.append(tokens[j].getToken());
    }
    return sugg.toString();
  }

  private String getLemmaOfPronomFeble(AnalyzedTokenReadings aToken) {
    for (AnalyzedToken analyzedToken : aToken) {
      String posTag = analyzedToken.getPOSTag();
      if (posTag == null) {
        posTag = "UNKNOWN";
      }
      final Matcher m = PRONOM_FEBLE.matcher(posTag);
      if (m.matches()) {
        return analyzedToken.getLemma();
      }
    }
    return "";
  }

  private boolean isException(AnalyzedTokenReadings[] tokens, int i) {
    if (tokens[i].getToken().equals("poder") && tokens[i-1].hasPosTagStartingWith("V")) {
      return true;
    }
    return false;
  }

  private boolean isThereErrorInLemmas (List<String> lemesPronomsAbans, List<String> lemesPronomsDespres,
                                        AnalyzedTokenReadings[] tokens, int lastVerbPos) {
    correctedPronouns = null;
    if (lemesPronomsAbans.size() == 0 || lemesPronomsDespres.size() == 0) {
      return false;
    }
    if (lemesPronomsAbans.size() == 1 && lemesPronomsDespres.size() == 1
      && lemesPronomsAbans.get(0).equals(lemesPronomsDespres.get(0))) {
      return true;
    }
    if (lemesPronomsAbans.size() > 1 && lemesPronomsDespres.size() > 1) {
      return true;
    }
    if ((tokens[lastVerbPos].getToken().equals("haver") || tokens[lastVerbPos].getToken().equals("havent"))
      && PRONOMS_EN_HI.contains(lemesPronomsDespres.get(0))
      && PRONOMS_EN_HI.contains(lemesPronomsAbans.get(0))) {
      correctedPronouns = "n'hi";
      return true;
    }
    return false;
  }

}
