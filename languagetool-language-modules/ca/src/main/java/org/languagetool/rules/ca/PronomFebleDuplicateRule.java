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
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

public class PronomFebleDuplicateRule extends Rule {

  // tots els pronoms febles
  private static final Pattern PRONOM_FEBLE = Pattern.compile("P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP3CP000|PP3CSD00");
  private static final Pattern KEEP_CHECKING = Pattern
      .compile("V.*|SPS00|P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP3CP000|PP3CSD00");
  private static final Pattern INFINITIU = Pattern.compile("V.N.*");
  private static final Pattern GERUNDI = Pattern.compile("V.G.*");
  private static final String[] VERBS_CONTINUAR = new String[] { "continuar", "seguir", "prosseguir" };
  private static final String[] VERBS_IMPERSONAL = new String[] { "ordenar", "recomanar" };

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
    boolean isPrevInfinitive = false;
    for (int i = 1; i < tokens.length; i++) { // ignoring token 0, i.e., SENT_START
      if (PFLemma.isEmpty()) {
        PFLemma = getLemmaOfPronomFeble(tokens[i]);
        // exception: Es recomana, S'ordena
        if (i + 1 < tokens.length && PFLemma.equalsIgnoreCase("es") && tokens[i + 1].hasAnyLemma(VERBS_IMPERSONAL)) {
          PFLemma = "";
          initPos = -1;
          continue;
        }
        if (!PFLemma.isEmpty()) {
          initPos = i;
          continue;
        }
      } else {
        String PFLemma2 = getLemmaOfPronomFeble(tokens[i]); 
        if (!tokens[i].isWhitespaceBefore() && PFLemma2.equals(PFLemma) && isPrevInfinitive) {
          // Rule matches!
          final RuleMatch ruleMatch = new RuleMatch(this, sentence, tokens[initPos].getStartPos(),
              tokens[i].getEndPos(), "Pronom feble duplicat. Elimineu-ne un.", "Pronom feble duplicat");
          // first suggestion
          StringBuilder suggestion = new StringBuilder();
          for (int j = initPos + 1; j <= i; j++) {
            if (j > initPos + 1 && tokens[j].isWhitespaceBefore()) {
              suggestion.append(" ");
            }
            String strToAdd = tokens[j].getToken();
            if (j==initPos+1 && StringTools.isCapitalizedWord(tokens[initPos].getToken())) {
              strToAdd = StringTools.uppercaseFirstChar(strToAdd);
            }  
            suggestion.append(strToAdd);
          }
          ruleMatch.addSuggestedReplacement(suggestion.toString());
          // second suggestion
          suggestion = new StringBuilder();
          for (int j = initPos; j <= i - 1; j++) {
            if (j > initPos && tokens[j].isWhitespaceBefore()) {
              suggestion.append(" ");
            }
            suggestion.append(tokens[j].getToken());
          }
          ruleMatch.addSuggestedReplacement(suggestion.toString());
          ruleMatches.add(ruleMatch);
        } else if (!tokens[i].isWhitespaceBefore() && isPrevInfinitive &&
            (PFLemma.equals("en") && PFLemma2.equals("hi") 
            || PFLemma.equals("hi") && PFLemma2.equals("en"))) 
        {
          final RuleMatch ruleMatch = new RuleMatch(this, sentence, tokens[initPos].getStartPos(),
              tokens[i].getEndPos(), "Combinació de pronoms febles probablement incorrecta", "Pronoms febles incorrectes");
          ruleMatches.add(ruleMatch);
        }
        else {
          // check whether to keep checking
          isPrevInfinitive = matchPostagRegexp(tokens[i], INFINITIU)
              || (matchPostagRegexp(tokens[i], GERUNDI) && tokens[i - 1].hasAnyLemma(VERBS_CONTINUAR));
          if (!matchPostagRegexp(tokens[i], KEEP_CHECKING)) {
            PFLemma = "";
            initPos = -1;

          }
        }
      }

    }
    return toRuleMatchArray(ruleMatches);
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

  private boolean matchPostagRegexp(AnalyzedTokenReadings aToken, Pattern pattern) {
    boolean matches = false;
    for (AnalyzedToken analyzedToken : aToken) {
      String posTag = analyzedToken.getPOSTag();
      if (posTag == null) {
        posTag = "UNKNOWN";
      }
      final Matcher m = pattern.matcher(posTag);
      if (m.matches()) {
        matches = true;
        break;
      }
    }
    return matches;
  }

}
