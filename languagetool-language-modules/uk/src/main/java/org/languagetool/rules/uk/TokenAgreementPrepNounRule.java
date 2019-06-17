/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Andriy Rysin
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
package org.languagetool.rules.uk;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.uk.TokenAgreementPrepNounExceptionHelper.RuleException;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.uk.IPOSTag;
import org.languagetool.tagging.uk.PosTagHelper;

/**
 * A rule that checks if preposition and a noun agree on inflection etc
 * 
 * @author Andriy Rysin
 */
public class TokenAgreementPrepNounRule extends Rule {
  
  private static final Pattern NOUN_ANIM_V_NAZ_PATTERN = Pattern.compile("noun:anim:.:v_naz.*");
  private static final String VIDMINOK_SUBSTR = ":v_";
  private static final Pattern VIDMINOK_REGEX = Pattern.compile(":(v_[a-z]+)");
  private static final String reqAnimInanimRegex = ":r(?:in)?anim";
  private static final Pattern REQ_ANIM_INANIM_PATTERN = Pattern.compile(reqAnimInanimRegex);

  private final Ukrainian ukrainian = new Ukrainian();

  public TokenAgreementPrepNounRule(ResourceBundle messages) throws IOException {
    super.setCategory(Categories.MISC.getCategory(messages));
  }

  @Override
  public final String getId() {
    return "UK_PREP_NOUN_INFLECTION_AGREEMENT";
  }

  @Override
  public String getDescription() {
    return "Узгодження прийменника та іменника у реченні";
  }

  public String getShort() {
    return "Узгодження прийменника та іменника";
  }

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    AnalyzedTokenReadings prepTokenReadings = null;
    for (int i = 1; i < tokens.length; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];

      String posTag = tokenReadings.getAnalyzedToken(0).getPOSTag();

      if (posTag == null
          || posTag.contains(IPOSTag.unknown.getText()) ){
        prepTokenReadings = null;
        continue;
      }

      String thisToken = tokenReadings.getToken();

      // часто вживають укр. В замість лат.: гепатит В
      // first token is always SENT_START
      if( i > 1
          && thisToken.length() == 1 
          && Character.isUpperCase(thisToken.charAt(0)) 
          && tokenReadings.isWhitespaceBefore() 
          && tokens[i-1].getToken().matches(".*[а-яіїєґ0-9]")) {
        prepTokenReadings = null;
        continue;
      }

      AnalyzedToken multiwordReqToken = getMultiwordToken(tokenReadings);
      if( multiwordReqToken != null ) {

        if (tokenReadings.getToken().equals("з") && multiwordReqToken.getLemma().equals("згідно з") ) { // напр. "згідно з"
          posTag = multiwordReqToken.getPOSTag(); // "rv_oru";
          prepTokenReadings = tokenReadings;
          continue;
        }
        else {
          if( posTag.startsWith(IPOSTag.prep.name()) ) {
            prepTokenReadings = null;
            continue;
          }

          String mwPosTag = multiwordReqToken.getPOSTag();
          if( ! mwPosTag.contains("adv") && ! mwPosTag.contains("insert") ) {
            prepTokenReadings = null;
          }
        }

        continue;
      }


      String token = tokenReadings.getAnalyzedToken(0).getToken();
      if( posTag.startsWith(IPOSTag.prep.name()) ) {
        String prep = token.toLowerCase();

        // що то була за людина
        if( prep.equals("за") && LemmaHelper.reverseSearch(tokens, i, 4, Pattern.compile("що"), null) ) {
          prepTokenReadings = null;
          continue;
        }

        if( prep.equals("понад") )
          continue;

        if( prep.equals("шляхом") || prep.equals("од") ) {
          prepTokenReadings = null;
          continue;
        }

        if( (prep.equals("окрім") || prep.equals("крім"))
            && tokens.length > i+1 
            && tokens[i+1].getToken().equalsIgnoreCase("як") ) {
          prepTokenReadings = null;
          continue;
        }

        prepTokenReadings = tokenReadings;
        continue;
      }

      if( prepTokenReadings == null )
        continue;


      // Do actual check


      Set<String> posTagsToFind = new LinkedHashSet<>();
      String prep = prepTokenReadings.getAnalyzedToken(0).getLemma();

      // замість Андрій вибрали Федір
      if( prep.equalsIgnoreCase("замість") ) {
        posTagsToFind.add("v_naz");
      }

      Set<String> expectedCases = CaseGovernmentHelper.getCaseGovernments(prepTokenReadings, IPOSTag.prep.name());
      
      // we want to ignore «залежно» + noun, but we want to catch «незважаючи» без «на»
//      if( expectedCases.isEmpty() ) {
//        prepTokenReadings = null;
//        continue;
//      }

      expectedCases.remove("v_inf"); // we don't care about rv_inf here
      posTagsToFind.addAll(expectedCases);

      RuleException exception = TokenAgreementPrepNounExceptionHelper.getExceptionStrong(tokens, i, prepTokenReadings, posTagsToFind);
      switch( exception.type ) {
      case exception:
        prepTokenReadings = null;
        continue;
      case skip:
        i += exception.skip;
        continue;
      case none:
        break;
      }

      
      if( PosTagHelper.hasPosTagPart(tokenReadings, ":v_") ) {

        if( hasVidmPosTag(posTagsToFind, tokenReadings) ) {
          prepTokenReadings = null;
          continue;
        }

        exception = TokenAgreementPrepNounExceptionHelper.getExceptionNonInfl(tokens, i, prepTokenReadings, posTagsToFind);
        switch( exception.type ) {
        case exception:
          prepTokenReadings = null;
          continue;
        case skip:
          i += exception.skip;
          continue;
        case none:
          break;
        }

        exception = TokenAgreementPrepNounExceptionHelper.getExceptionInfl(tokens, i, prepTokenReadings, posTagsToFind);
        switch( exception.type ) {
        case exception:
          prepTokenReadings = null;
          continue;
        case skip:
          i += exception.skip;
          continue;
        case none:
          break;
        }

        RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, prepTokenReadings, posTagsToFind, sentence);
        ruleMatches.add(potentialRuleMatch);
      }
      else { // no _v found

        exception = TokenAgreementPrepNounExceptionHelper.getExceptionNonInfl(tokens, i, prepTokenReadings, posTagsToFind);
        switch( exception.type ) {
        case exception:
          prepTokenReadings = null;
          continue;
        case skip:
          i += exception.skip;
          continue;
        case none:
          break;
        }

      }
      prepTokenReadings = null;
    }

    return toRuleMatchArray(ruleMatches);
  }


  static boolean hasVidmPosTag(Collection<String> posTagsToFind, AnalyzedTokenReadings tokenReadings) {
    boolean vidminokFound = false;  // because POS dictionary is not complete

    for(AnalyzedToken token: tokenReadings) {
      String posTag = token.getPOSTag();

      if( posTag == null ) {
        if( tokenReadings.getReadingsLength() == 1) 
          return true;
        
        continue;
      }
      
      if( posTag.contains(PosTagHelper.NO_VIDMINOK_SUBSTR) )
        return true;

      if( posTag.contains(VIDMINOK_SUBSTR) ) {
        vidminokFound = true;

        for(String posTagToFind: posTagsToFind) {
          if ( posTag.contains(posTagToFind) )
            return true;
        }
      }
    }

    return ! vidminokFound; //false;
  }

  private RuleMatch createRuleMatch(AnalyzedTokenReadings tokenReadings, AnalyzedTokenReadings reqTokenReadings, Set<String> posTagsToFind, AnalyzedSentence sentence) {
    String tokenString = tokenReadings.getToken();
    
    Synthesizer ukrainianSynthesizer = ukrainian.getSynthesizer();

    List<String> suggestions = new ArrayList<>();
    
    String requiredPostTagsRegEx = ":(" + String.join("|", posTagsToFind) + ")";
    for (AnalyzedToken analyzedToken: tokenReadings.getReadings()) {
    
      String oldPosTag = analyzedToken.getPOSTag();
      
      if( oldPosTag == null )
        continue;
      
      String requiredPostTagsRegExToApply = requiredPostTagsRegEx;

      Matcher matcher = REQ_ANIM_INANIM_PATTERN.matcher(oldPosTag);
      if( matcher.find() ) {
        requiredPostTagsRegExToApply += matcher.group(0);
      }
      else {
        requiredPostTagsRegExToApply += "(?:" + reqAnimInanimRegex + ")?";
      }

      String posTag = oldPosTag.replaceFirst(":v_[a-z]+", requiredPostTagsRegExToApply);

      try {
        String[] synthesized = ukrainianSynthesizer.synthesize(analyzedToken, posTag, true);

        suggestions.addAll( Arrays.asList(synthesized) );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    
    if( suggestions.size() > 0 ) {  // remove duplicates
      suggestions = new ArrayList<>(new LinkedHashSet<>(suggestions));
    }

    List<String> reqVidminkyNames = new ArrayList<>();
    for (String vidm: posTagsToFind) {
      reqVidminkyNames.add(PosTagHelper.VIDMINKY_MAP.get(vidm));
    }

    List<String> foundVidminkyNames = new ArrayList<>();
    for (AnalyzedToken token: tokenReadings) {
      String posTag2 = token.getPOSTag();
      if( posTag2 != null && posTag2.contains(VIDMINOK_SUBSTR) ) {
        String vidmName = PosTagHelper.VIDMINKY_MAP.get(posTag2.replaceFirst("^.*"+VIDMINOK_REGEX+".*$", "$1"));
        if( foundVidminkyNames.contains(vidmName) ) {
          if (posTag2.contains(":p:")) {
            vidmName = vidmName + " (мн.)";
            foundVidminkyNames.add(vidmName);
          }
          // else skip dup
        }
        else {
          foundVidminkyNames.add(vidmName);
        }
      }
    }

    String msg = MessageFormat.format("Прийменник «{0}» вимагає іншого відмінка: {1}, а знайдено: {2}", 
        reqTokenReadings.getToken(), String.join(", ", reqVidminkyNames), String.join(", ", foundVidminkyNames));

    if( tokenString.equals("їх") && requiredPostTagsRegEx != null ) {
      msg += ". Можливо, тут потрібно присвійний займенник «їхній»?";
      try {
        String newYihPostag = "adj:p" + requiredPostTagsRegEx + ".*";
        String[] synthesized = ukrainianSynthesizer.synthesize(new AnalyzedToken("їхній", "adj:m:v_naz:&pron:pos", "їхній"), newYihPostag, true);
        suggestions.addAll( Arrays.asList(synthesized) );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    else if( reqTokenReadings.getToken().equalsIgnoreCase("о") ) {
      for(AnalyzedToken token: tokenReadings.getReadings()) {
        if( PosTagHelper.hasPosTag(token, NOUN_ANIM_V_NAZ_PATTERN) ) {
          msg += ". Можливо, тут «о» — це вигук і потрібно кличний відмінок?";
          try {
            String newPostag = token.getPOSTag().replace("v_naz", "v_kly");
            String[] synthesized = ukrainianSynthesizer.synthesize(token, newPostag, false);
            for (String string : synthesized) {
              if( ! string.equals(token.getToken()) && ! suggestions.contains(string) ) {
                suggestions.add( string );
              }
            }
            break;
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }

    }

    RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, tokenReadings.getStartPos(), tokenReadings.getEndPos(), msg, getShort());

    potentialRuleMatch.setSuggestedReplacements(suggestions);

    return potentialRuleMatch;
  }

  @Nullable
  private static AnalyzedToken getMultiwordToken(AnalyzedTokenReadings analyzedTokenReadings) {
      for(AnalyzedToken analyzedToken: analyzedTokenReadings) {
        String posTag = analyzedToken.getPOSTag();
        if( posTag != null && posTag.startsWith("<") )
          return analyzedToken;
      }
      return null;
  }

}
