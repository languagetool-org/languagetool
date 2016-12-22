package org.languagetool.rules.uk;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.uk.LemmaHelper.Dir;
import org.languagetool.tagging.uk.PosTagHelper;

/**
 * @since 3.6
 */
public final class TokenVerbAgreementExceptionHelper {

  private TokenVerbAgreementExceptionHelper() {
  }

  public static boolean isException(AnalyzedTokenReadings[] tokens, int i,
                                    List<TokenVerbAgreementRule.Inflection> masterInflections, List<TokenVerbAgreementRule.Inflection> slaveInflections,
                                    List<AnalyzedToken> nounTokenReadings, List<AnalyzedToken> verbTokenReadings) {

    if( PosTagHelper.hasPosTag(verbTokenReadings, ".*:p(:.*|$)") ) {

        // моя мама й сестра мешкали
        if( LemmaHelper.tokenSearch(tokens, i-2, null, TokenInflectionExceptionHelper.CONJ_FOR_PLULAR_PATTERN, 
            Pattern.compile("(noun|adj).*"), Dir.REVERSE) != -1 ) {
          logException();
          return true;
        }
        
        // Бразилія, Мексика, Індія збувають
        int pos = LemmaHelper.tokenSearch(tokens, i-2, null, Pattern.compile(","), Pattern.compile("adj.*"), Dir.REVERSE);
        if( pos > 1
            && PosTagHelper.hasPosTagPart(tokens[pos-1], "noun") ) {
          logException();
          return true;
        }
    }

    // не встиг я отямитися
    // Хотів би я подивитися
    int verbPos = LemmaHelper.tokenSearch(tokens, i-2, "verb", null, Pattern.compile("(adv|part).*"), Dir.REVERSE);
    if( verbPos != -1
        && PosTagHelper.hasPosTag(verbTokenReadings, "verb.*:inf.*") 
        && ! Collections.disjoint(TokenVerbAgreementRule.getVerbInflections(tokens[verbPos].getReadings()), masterInflections) ) {
      logException();
      return true;
    }
    
    // чи готові ми сидіти без світла
    if( i > 1
        && PosTagHelper.hasPosTagPart(tokens[i-2], "adj") 
        && PosTagHelper.hasPosTag(verbTokenReadings, "verb.*:inf.*")
        && CaseGovernmentHelper.hasCaseGovernment(tokens[i-2], "v_inf")
        && ! Collections.disjoint(InflectionHelper.getAdjInflections(tokens[i-2].getReadings()), InflectionHelper.getNounInflections(nounTokenReadings))) {
      logException();
      return true;
    }

    // що ми зробити не зможемо
    verbPos = LemmaHelper.tokenSearch(tokens, i+1, "verb", null, Pattern.compile("(adv|part).*"), Dir.FORWARD);
    if( verbPos != -1
        && PosTagHelper.hasPosTag(verbTokenReadings, "verb.*:inf.*") 
        && ! Collections.disjoint(TokenVerbAgreementRule.getVerbInflections(tokens[verbPos].getReadings()), masterInflections) ) {
      logException();
      return true;
    }

    // ми розраховувати не повинні
    int adjPos = LemmaHelper.tokenSearch(tokens, i+1, "adj", null, Pattern.compile("(adv|part).*"), Dir.FORWARD);
    if( adjPos != -1
        && PosTagHelper.hasPosTag(verbTokenReadings, "verb.*:inf.*") 
        && CaseGovernmentHelper.hasCaseGovernment(tokens[adjPos], "v_inf")
        && ! Collections.disjoint(InflectionHelper.getAdjInflections(tokens[adjPos].getReadings()), InflectionHelper.getNounInflections(nounTokenReadings)) ) {
      logException();
      return true;
    }

    // решта забороняються
    if( tokens[i-1].getToken().equalsIgnoreCase("решта") 
        && PosTagHelper.hasPosTag(verbTokenReadings, ".*:p(:.*|$)") ) {
      logException();
      return true;
    } 
    
    // тому, що як австрієць маєте
    if( PosTagHelper.hasPosTag(tokens[i-1], "noun.*:v_naz.*")
        && LemmaHelper.tokenSearch(tokens, i-2, null, Pattern.compile("[Яя]к"), Pattern.compile("adj.*"), Dir.REVERSE) != -1 ) {
      logException();
      return true;
    } 
    
    return false;

  }
  

  private static void logException() {
    if( TokenVerbAgreementRule.DEBUG ) {
      StackTraceElement stackTraceElement = new Exception().getStackTrace()[1];
      System.err.println("exception: " + stackTraceElement.getFileName() + ": " + stackTraceElement.getLineNumber());
    }
  }

}
