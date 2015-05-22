package org.languagetool.rules.uk;

import java.util.*;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.WordRepeatRule;
import org.languagetool.tagging.uk.IPOSTag;
import org.languagetool.tagging.uk.PosTagHelper;

/**
 * @since 2.9
 */
public class UkrainianWordRepeatRule extends WordRepeatRule {
  private static final HashSet<String> REPEAT_ALLOWED_SET = new HashSet<>(
      Arrays.asList("що", "ні", "одне", "ось")
  );
  private static final HashSet<String> REPEAT_ALLOWED_CAPS_SET = new HashSet<>(
      Arrays.asList("ПРО", "Джей", "Ді")
  );

  public UkrainianWordRepeatRule(ResourceBundle messages, Language language) {
    super(messages, language);
  }

  @Override
  public String getId() {
    return "UKRAINIAN_WORD_REPEAT_RULE";
  }

  @Override
  public boolean ignore(AnalyzedTokenReadings[] tokens, int position) {
    AnalyzedTokenReadings analyzedTokenReadings = tokens[position];
    String token = analyzedTokenReadings.getToken();
    
    // від добра добра не шукають
    if( position > 1 && token.equals("добра")
        && tokens[position-2].getToken().equalsIgnoreCase("від") )
      return true;
    
    if( REPEAT_ALLOWED_SET.contains(token.toLowerCase()) )
      return true;

    if( REPEAT_ALLOWED_CAPS_SET.contains(token) )
      return true;
    
    if( PosTagHelper.hasPosTag(analyzedTokenReadings, "date|time|number") )
      return true;
    
    for(AnalyzedToken analyzedToken: analyzedTokenReadings.getReadings()) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag != null ) {
        if ( ! isInitial(analyzedToken, tokens, position)
//            && ! posTag.equals(JLanguageTool.SENTENCE_START_TAGNAME)
            && ! posTag.equals(JLanguageTool.SENTENCE_END_TAGNAME) )
          return false;
      }
    }
    return true;
  }

  private boolean isInitial(AnalyzedToken analyzedToken, AnalyzedTokenReadings[] tokens, int position) {
    return analyzedToken.getPOSTag().contains(IPOSTag.abbr.getText())
        || (analyzedToken.getToken().length() == 1 
        && Character.isUpperCase(analyzedToken.getToken().charAt(0))
        && position < tokens.length-1 && tokens[position+1].getToken().equals("."));
  }

  @Override
  protected RuleMatch createRuleMatch(String prevToken, String token, int prevPos, int pos, String msg) {
    boolean doubleI = prevToken.equals("І") && token.equals("і");
    if( doubleI ) {
      msg += " або, можливо, перша І має бути латинською.";
    }
    
    RuleMatch ruleMatch = super.createRuleMatch(prevToken, token, prevPos, pos, msg);

    if( doubleI ) {
      List<String> replacements = new ArrayList<>(ruleMatch.getSuggestedReplacements());
      replacements.add("I і");
      ruleMatch.setSuggestedReplacements(replacements);
    }
    return ruleMatch;
  }
}
