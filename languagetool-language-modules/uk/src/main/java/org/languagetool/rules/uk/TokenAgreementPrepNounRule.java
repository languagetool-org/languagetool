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
import java.util.HashSet;
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
import org.languagetool.JLanguageTool;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.uk.IPOSTag;
import org.languagetool.tagging.uk.PosTagHelper;

/**
 * A rule that checks if preposition and a noun agree on inflection etc
 * 
 * @author Andriy Rysin
 */
public class TokenAgreementPrepNounRule extends Rule {
  private static final Pattern NOUN_ANIM_V_NAZ_PATTERN = Pattern.compile("noun:anim.*:v_naz.*");
  private static final String VIDMINOK_SUBSTR = ":v_";
  private static final Pattern VIDMINOK_REGEX = Pattern.compile(":(v_[a-z]+)");
  private static final String reqAnimInanimRegex = ":r(?:in)?anim";
  private static final Pattern REQ_ANIM_INANIM_PATTERN = Pattern.compile(reqAnimInanimRegex);

  private final Ukrainian ukrainian = new Ukrainian();

  private static final Set<String> NAMES = new HashSet<>(Arrays.asList(
      "ім'я", "прізвище"
      ));

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
    for (int i = 0; i < tokens.length; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];

      String posTag = tokenReadings.getAnalyzedToken(0).getPOSTag();

      //TODO: skip conj напр. «бодай»

      if (posTag == null
          || posTag.contains(IPOSTag.unknown.getText())
          || posTag.equals(JLanguageTool.SENTENCE_START_TAGNAME) ){
        prepTokenReadings = null;
        continue;
      }

      // first token is always SENT_START
      String thisToken = tokenReadings.getToken();
      if( i > 1 && thisToken.length() == 1 && Character.isUpperCase(thisToken.charAt(0)) 
          && tokenReadings.isWhitespaceBefore() && ! tokens[i-1].getToken().matches("[:—–-]")) {  // часто вживають укр. В замість лат.: гепатит В
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
          }
          
          String mwPosTag = multiwordReqToken.getPOSTag();
          if( ! mwPosTag.contains("adv") && ! mwPosTag.contains("insert") ) {
            prepTokenReadings = null;
          }
//          continue;
        }
        
        continue;
      }
      

      String token = tokenReadings.getAnalyzedToken(0).getToken();
      if( posTag.startsWith(IPOSTag.prep.name()) ) { // && tokenReadings.getReadingsLength() == 1 ) {
        String prep = token;

        // що то була за людина
        if( prep.equals("за") && reverseSearch(tokens, i, "що") ) {
          prepTokenReadings = null;
          continue;
        }

        if( prep.equalsIgnoreCase("понад") )
          continue;

        if( prep.equalsIgnoreCase("шляхом") || prep.equalsIgnoreCase("од") ) {
          prepTokenReadings = null;
          continue;
        }

        if( (prep.equalsIgnoreCase("окрім") || prep.equalsIgnoreCase("крім"))
            && tokens.length > i+1 
            && tokens[i+1].getAnalyzedToken(0).getToken().equalsIgnoreCase("як") ) {
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
      
//      AnalyzedToken multiwordToken = getMultiwordToken(tokenReadings);
//      if( multiwordToken != null ) {
//        reqTokenReadings = null;
//        continue;
//      }

      //TODO: for numerics only v_naz
//      if( prep.equalsIgnoreCase("понад") ) { //&& tokenReadings.getAnalyzedToken(0).getPOSTag().equals(IPOSTag.numr) ) { 
//        posTagsToFind.add("v_naz");
//      }
//      else 
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
      

      for(AnalyzedToken readingToken: tokenReadings) {
        if( IPOSTag.numr.match(readingToken.getPOSTag()) ) {
          posTagsToFind.add("v_naz");  // TODO: only if noun is following?
          break;
        }
      }

      //      System.out.println("For " + tokenReadings + " to match " + posTagsToFind + " of " + reqTokenReadings.getToken());
      if( ! hasVidmPosTag(posTagsToFind, tokenReadings) ) {
        if( isTokenToSkip(tokenReadings) )
          continue;

        //TODO: only for subset: президенти/депутати/мери/гості... or by verb піти/йти/балотуватися/записатися...
        if( prep.equalsIgnoreCase("в") || prep.equalsIgnoreCase("у") || prep.equals("межи") || prep.equals("між") || prep.equals("на") ) {
          if( PosTagHelper.hasPosTag(tokenReadings, "noun:anim.*:p:v_naz[^&]*") ) { // but not &pron:
            prepTokenReadings = null;
            continue;
          }
        }

        if (prep.equalsIgnoreCase("на")) {
          // 1) на (свято) Купала, на (вулиці) Мазепи, на (вулиці) Тюльпанів
          if ((Character.isUpperCase(token.charAt(0)) && posTag.matches("noun.*?:.:v_rod.*"))
                // 2) поміняти ім'я на Захар; поміняв Іван на Петро
                || (posTag.matches(".*[fl]name.*")
                    && ((i > 1 && NAMES.contains(tokens[i-2].getAnalyzedToken(0).getToken()))
                        || (i > 2 && NAMES.contains(tokens[i-3].getAnalyzedToken(0).getLemma()))))) {
            prepTokenReadings = null;
            continue;
          }
          // handled by xml rule
          if( token.equals("манер") ) {
            prepTokenReadings = null;
            continue;
          }
          // на біс (можливо краще tag=intj?)
          if( token.equalsIgnoreCase("біс") ) {
            prepTokenReadings = null;
            continue;
          }
        }

        if( prep.equalsIgnoreCase("з") ) {
          if( token.equals("рана") ) {
            prepTokenReadings = null;
            continue;
          }
        }

        // TODO: temporary until we have better logic - skip
        if( prep.equalsIgnoreCase("при") ) {
          if( token.equals("їх") ) {
            continue;
          }
        }

        if( prep.equalsIgnoreCase("від") ) {
          if( token.equalsIgnoreCase("а") || token.equals("рана") || token.equals("корки") || token.equals("мала") ) {  // корки/мала ловиться іншим правилом
            prepTokenReadings = null;
            continue;
          }
        }
        else if( prep.equalsIgnoreCase("до") ) {
          if( token.equalsIgnoreCase("я") || token.equals("корки") || token.equals("велика") ) {  // корки/велика ловиться іншим правилом
            prepTokenReadings = null;
            continue;
          }
        }

        // exceptions
        if( tokens.length > i+1 ) {
          //      if( tokens.length > i+1 && Character.isUpperCase(tokenReadings.getAnalyzedToken(0).getToken().charAt(0))
          //        && hasRequiredPosTag(Arrays.asList("v_naz"), tokenReadings)
          //        && Character.isUpperCase(tokens[i+1].getAnalyzedToken(0).getToken().charAt(0)) )
          //          continue; // "у Конан Дойла", "у Робін Гуда"

          if( isCapitalized( token ) 
              && LemmaHelper.CITY_AVENU.contains( tokens[i+1].getAnalyzedToken(0).getToken().toLowerCase() ) ) {
            prepTokenReadings = null;
            continue;
          }

          if( PosTagHelper.hasPosTag(tokens[i+1], "num.*")
              && (token.equals("мінус") || token.equals("плюс")
                  || token.equals("мінімум") || token.equals("максимум") ) ) {
            prepTokenReadings = null;
            continue;
          }

          // на мохом стеленому дні - пропускаємо «мохом»
          if( PosTagHelper.hasPosTag(tokenReadings, "noun.*?:.:v_oru.*")
              && tokens[i+1].hasPartialPosTag("adjp") ) {
            continue;
          }
          
          if( (prep.equalsIgnoreCase("через") || prep.equalsIgnoreCase("на"))  // років 10, відсотки 3-4
              && (posTag.startsWith("noun:inanim:p:v_naz") || posTag.startsWith("noun:inanim:p:v_rod")) // token.equals("років") 
              && IPOSTag.isNum(tokens[i+1].getAnalyzedToken(0).getPOSTag()) ) {
            prepTokenReadings = null;
            continue;
          }

          if( (token.equals("вами") || token.equals("тобою") || token.equals("їми"))
              && tokens[i+1].getAnalyzedToken(0).getToken().startsWith("ж") ) {
            continue;
          }
          if( (token.equals("собі") || token.equals("йому") || token.equals("їм"))
              && tokens[i+1].getAnalyzedToken(0).getToken().startsWith("подібн") ) {
            continue;
          }
          if( (token.equals("усім") || token.equals("всім"))
              && tokens[i+1].getAnalyzedToken(0).getToken().startsWith("відом") ) {
            continue;
          }

          if( prep.equalsIgnoreCase("до") && token.equals("схід") 
                && tokens[i+1].getAnalyzedToken(0).getToken().equals("сонця") ) {
            prepTokenReadings = null;
            continue;
          }
          
          if( tokens[i+1].getAnalyzedToken(0).getToken().equals("«") 
              && tokens[i].getAnalyzedToken(0).getPOSTag().contains(":abbr") ) {
            prepTokenReadings = null;
            continue;
          }

          if( tokens.length > i+2 ) {
            // спиралося на місячної давнини рішення
            if (/*prep.equalsIgnoreCase("на") &&*/ PosTagHelper.hasPosTag(tokenReadings, "adj.*:[mfn]:v_rod.*")) {
              String genders = PosTagHelper.getGenders(tokenReadings, "adj.*:[mfn]:v_rod.*");
              
              if ( PosTagHelper.hasPosTag(tokens[i+1], "noun.*:["+genders+"]:v_rod.*")) {
                i += 1;
                continue;
              }
            }

            if ((token.equals("нікому") || token.equals("ніким") || token.equals("нічим") || token.equals("нічому")) 
                && tokens[i+1].getAnalyzedToken(0).getToken().equals("не")) {
              //          reqTokenReadings = null;
              continue;
            }
          }
        }

        RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, prepTokenReadings, posTagsToFind, sentence);
        ruleMatches.add(potentialRuleMatch);
      }

      prepTokenReadings = null;
    }

    return toRuleMatchArray(ruleMatches);
  }

  private static boolean isCapitalized(String token) {
    return token.length() > 1 && Character.isUpperCase(token.charAt(0)) && Character.isLowerCase(token.charAt(1));
  }

  private boolean reverseSearch(AnalyzedTokenReadings[] tokens, int pos, String string) {
    for(int i=pos-1; i >= 0 && i > pos-4; i--) {
      if( tokens[i].getAnalyzedToken(0).getToken().equalsIgnoreCase(string) )
        return true;
    }
    return false;
  }


  private boolean isTokenToSkip(AnalyzedTokenReadings tokenReadings) {
    for(AnalyzedToken token: tokenReadings) {
//      System.out.println("    tag: " + token.getPOSTag() + " for " + token.getToken());
      if( IPOSTag.adv.match(token.getPOSTag())
          || IPOSTag.contains(token.getPOSTag(), "adv>")
          ||  IPOSTag.insert.match(token.getPOSTag()) )
        return true;
    }
    return false;
  }

//  private boolean isTokenToIgnore(AnalyzedTokenReadings tokenReadings) {
//    for(AnalyzedToken token: tokenReadings) {
//      if( token.getPOSTag().contains("abbr") )
//        return true;
//    }
//    return false;
//  }

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
          //          System.out.println("  verifying: " + token + " -> " + posTag + " ~ " + posTagToFind);

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
