package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.ArrayList;
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
    this.setDefaultTempOff();
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    String PFLemma = "";
    int initPos = -1;
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
        }
      } else if (tokens[i].getChunkTags().contains(new ChunkTag("GV")) && !lemesPronomsAbans.isEmpty()
        && lemesPronomsDespres.isEmpty()) {
        countVerb++;
        inVerbGroup=true;
      } else {
        inVerbGroup=false;
      }

      if (!inVerbGroup || i==tokens.length-1) {
        if (lemesPronomsAbans.size()>0 && lemesPronomsDespres.size()>0) {
          final RuleMatch ruleMatch = new RuleMatch(this, sentence, tokens[initPos].getStartPos(),
            tokens[i-1].getEndPos(), "Pronoms febles duplicats. Deixeu els de davant o els de darrere del verb.", "Pronom feble duplicat");
          List<String> replacements = new ArrayList<>();
          replacements.add(StringTools.preserveCase(getSuggestionFromTo(tokens, initPos+lemesPronomsAbans.size(),
            initPos+lemesPronomsAbans.size()+countVerb+lemesPronomsDespres.size()), tokens[initPos].getToken()));
          replacements.add(StringTools.preserveCase(getSuggestionFromTo(tokens, initPos,
            initPos+lemesPronomsAbans.size()+countVerb), tokens[initPos].getToken()));
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

}
