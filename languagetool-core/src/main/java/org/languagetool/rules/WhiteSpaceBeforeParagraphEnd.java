package org.languagetool.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;

/**
 * A rule that checks for a whitespace at the end of a paragraph
 * 
 * @author Fred Kruse
 */

public class WhiteSpaceBeforeParagraphEnd extends TextLevelRule {

  public WhiteSpaceBeforeParagraphEnd(ResourceBundle messages, boolean defaultActive) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));

    if (!defaultActive) {
        setDefaultOff();   //  Default is Off
    }
    
    setLocQualityIssueType(ITSIssueType.Style);
  }

  public WhiteSpaceBeforeParagraphEnd(ResourceBundle messages) {
    this(messages, false);
  }

  @Override
  public String getId() {
    return "WHITESPACE_PARAGRAPH";
  }

  @Override
  public String getDescription() {
    return messages.getString("whitespace_before_parapgraph_end_desc");
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0; 
    for (int n = 0; n < sentences.size(); n++) {
      AnalyzedSentence sentence = sentences.get(n);
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      int i = 2;
//    Whitespace before linebreak (includes last non-whitespace because of problems with OO dialog
      while (i < tokens.length) {
        if(tokens[i].isWhitespace() && !tokens[i].isLinebreak() && !tokens[i - 1].isWhitespace()) {
          int lastNonWhite = i - 1;
          for (i++; i < tokens.length && tokens[i].isWhitespace() && !tokens[i].isLinebreak(); i++);
          if (i < tokens.length && tokens[i].isLinebreak()) { 
            int fromPos = pos + tokens[lastNonWhite].getStartPos();
            int toPos = pos + tokens[i].getStartPos();
            RuleMatch ruleMatch = new RuleMatch(this, fromPos, toPos, messages.getString("whitespace_before_parapgraph_end_msg"));
            ruleMatch.setSuggestedReplacement(tokens[lastNonWhite].getToken());
            ruleMatches.add(ruleMatch);
          }
        }
        i++;
      }
//    Whitespace before end of paragraph
      if(n == sentences.size() - 1) {
        for (i = tokens.length - 1; i > 0 && tokens[i].isWhitespace() && !tokens[i].isLinebreak(); i--);
        if(i < tokens.length - 1) {
          int fromPos = pos + tokens[i + 1].getStartPos();
          int toPos = pos + tokens[tokens.length - 1].getStartPos() + 1;
          RuleMatch ruleMatch = new RuleMatch(this, fromPos, toPos, messages.getString("whitespace_before_parapgraph_end_msg"));
          ruleMatch.setSuggestedReplacement("");
          ruleMatches.add(ruleMatch);
        }
      }
      pos += sentence.getText().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

}
