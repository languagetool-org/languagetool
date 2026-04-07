package org.languagetool.rules.ca;

import org.languagetool.AnalyzedSentence;
import org.languagetool.UserConfig;
import org.languagetool.rules.LongSentenceRule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.DiffsAsMatches;
import org.languagetool.tools.PseudoMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.languagetool.rules.ca.CatalanRemoteRewriteHelper.*;

public class CatalanSplitLongSentenceRule extends LongSentenceRule {

  public CatalanSplitLongSentenceRule(ResourceBundle messages, UserConfig userConfig, int maxWords) {
    super(messages, userConfig, maxWords);
  }

  @Override
  public String getId() {
    return "CA_SPLIT_LONG_SENTENCE";
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    RuleMatch[] originalRuleMatches = super.match(sentences);
    List<RuleMatch> resultRuleMatches = new ArrayList<>();
    for (RuleMatch rm : originalRuleMatches) {
      String underlinedText = rm.getSentence().getText(); // the whole sentence, not the selected match
      String correctedSentence = sendPostRequest(underlinedText, this.getId());
      if (correctedSentence == null || correctedSentence.isEmpty()) {
        continue;
      }
      correctedSentence = correctedSentence.replaceAll("[\r\n\\s]*[\r\n][\r\n\\s]*", " ");
      //System.out.println("CORRECTED_SENTENCE: "+correctedSentence);
      DiffsAsMatches diffsAsMatches = new DiffsAsMatches();
      List<PseudoMatch> pseudoMatches = diffsAsMatches.getPseudoMatches(underlinedText, correctedSentence);
      for (PseudoMatch pm : pseudoMatches) {
        String message = rm.getMessage();
        String shortMessage = rm.getShortMessage();
        if (!pm.getReplacement().contains(".") || pm.getReplacement().endsWith(".")) {
          // no és de la divisió de la frase, no sabem què és. Es poden aprofitar si es filtren bé.
          //message = "Canvi recomanat.";
          //shortMessage = "Canvi recomanat";
          continue;
        }
        RuleMatch ruleMatch = new RuleMatch(rm.getRule(), rm.getSentence(), rm.getFromPos() + pm.getFromPos(),
          rm.getFromPos() + pm.getToPos(), message, shortMessage);
        ruleMatch.setType(rm.getType());
        ruleMatch.setSuggestedReplacement(pm.getReplacements().get(0));
        resultRuleMatches.add(ruleMatch);
      }
    }
    return toRuleMatchArray(resultRuleMatches);
  }
}
