package org.languagetool.rules.de;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.LinguServices;
import org.languagetool.UserConfig;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.Tools;

import morfologik.speller.Speller;
import morfologik.stemming.Dictionary;

public class CompoundInfinitivRule extends Rule {
  
  private static final Pattern MARK_REGEX = Pattern.compile("[.?!…:;,()\\[\\]]");
  private final LinguServices linguServices;
  private final Speller speller;
  private final Language lang;

  public CompoundInfinitivRule(ResourceBundle messages, Language lang, UserConfig userConfig) throws IOException {
    super.setCategory(Categories.COMPOUNDING.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("Er überprüfte die Rechnungen noch einmal, um ganz <marker>sicher zu gehen</marker>."),
        Example.fixed("Er überprüfte die Rechnungen noch einmal, um ganz <marker>sicherzugehen</marker>."));
    this.lang = lang;
    if (userConfig != null) {
      linguServices = userConfig.getLinguServices();
    } else {
      linguServices = null;
    }
    if (linguServices == null) {
      Dictionary dict = Dictionary.read(JLanguageTool.getDataBroker().getFromResourceDirAsUrl("/de/hunspell/de_DE.dict"));
      speller = new Speller(dict);
    } else {
      speller = null;
    }
    setUrl(Tools.getUrl("https://www.duden.de/sprachwissen/sprachratgeber/Infinitiv-mit-zu"));
  }

  @Override
  public String getId() {
    return "COMPOUND_INFINITIV_RULE";
  }

  @Override
  public String getDescription() {
    return "Erweiterter Infinitiv mit zu (Zusammenschreibung)";
  }

  private static boolean isInfinitiv(AnalyzedTokenReadings token) {
    return token.matchesPosTagRegex("VER:INF:.*");
  }
  
  private boolean isMisspelled(String word) {
    if (linguServices == null && speller != null) {
      return speller.isMisspelled(word);
    } else if (linguServices != null) {
      return !linguServices.isCorrectSpell(word, lang);
    }
    return false;
  }
  
  private boolean isRelevant(AnalyzedTokenReadings token) {
    return token.matchesPosTagRegex("ZUS.*") && !"um".equals(token.getToken().toLowerCase());
  }
  
  private boolean isException(AnalyzedTokenReadings[] tokens, int n) {
    if(tokens[n - 2].matchesPosTagRegex("VER.*")) {
      return true;
    }
    if("sagen".equals(tokens[n + 1].getToken()) &&
        ("weiter".equals(tokens[n - 1].getToken()) || "dazu".equals(tokens[n - 1].getToken()))) {
      return true;
    }
    if(("tragen".equals(tokens[n + 1].getToken()) || "machen".equals(tokens[n + 1].getToken()))
        && "davon".equals(tokens[n - 1].getToken())) {
      return true;
    }
    if("geben".equals(tokens[n + 1].getToken()) && "daran".equals(tokens[n - 1].getToken())) {
      return true;
    }
    if("gehen".equals(tokens[n + 1].getToken()) && "ab".equals(tokens[n - 1].getToken())) {
      return true;
    }
    for(int i = n - 2; i > 0 && !MARK_REGEX.matcher(tokens[i].getToken()).matches(); i--) {
      if(tokens[i].matchesPosTagRegex("VER.*") || "Fang".equals(tokens[i].getToken())) {
        if(!isMisspelled(tokens[n - 1].getToken() + tokens[i].getToken().toLowerCase())) {
          return true;
        } else {
          break;
        }
      }
    }
    if("aus".equals(tokens[n - 1].getToken()) || "an".equals(tokens[n - 1].getToken())) {
      for(int i = n - 2; i > 0 && !MARK_REGEX.matcher(tokens[i].getToken()).matches(); i--) {
        if("von".equals(tokens[i].getToken()) || "vom".equals(tokens[i].getToken())) {
          return true;
        }
      }
    }
    if("her".equals(tokens[n - 1].getToken())) {
      for(int i = n - 2; i > 0 && !MARK_REGEX.matcher(tokens[i].getToken()).matches(); i--) {
        if("vor".equals(tokens[i].getToken())) {
          return true;
        }
      }
    }
    
    return false;
  }
  
  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    for (int i = 2; i < tokens.length - 1; i++) {
      if("zu".equals(tokens[i].getToken()) && isInfinitiv(tokens[i+1])) {
        if (isRelevant(tokens[i - 1]) && !isException(tokens, i) && !isMisspelled(tokens[i - 1].getToken() + tokens[i + 1].getToken())) {
          String msg = "Wenn der erweiterte Infinitv von dem Verb '" + tokens[i - 1].getToken() + tokens[i + 1].getToken()
              + "' abgeleitet ist, muss er zusammengeschrieben werden";
          RuleMatch ruleMatch = new RuleMatch(this, tokens[i - 1].getStartPos(), tokens[i + 1].getEndPos(), msg);
          List<String> suggestions = new ArrayList<String>();
          suggestions.add(tokens[i - 1].getToken() + tokens[i].getToken() + tokens[i + 1].getToken());
          ruleMatch.setSuggestedReplacements(suggestions);
          ruleMatches.add(ruleMatch);
        }
      }
    }
    
    return toRuleMatchArray(ruleMatches);
  }

}
