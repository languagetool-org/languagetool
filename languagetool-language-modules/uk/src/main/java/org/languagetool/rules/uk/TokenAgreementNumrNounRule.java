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
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.uk.InflectionHelper.Inflection;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.uk.PosTagHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rule that checks if adjective and following noun agree on gender and inflection
 * 
 * @author Andriy Rysin
 */
public class TokenAgreementNumrNounRule extends Rule {
  private static final Logger logger = LoggerFactory.getLogger(TokenAgreementNumrNounRule.class);

  private static final Pattern NOUN_IGNORE_PATTERN = Pattern.compile(".*(prop|noun.*pron|v_oru).*");
  private static final Pattern NUMR_PATTERN = Pattern.compile("numr(?!.*abbr).*");
  private static final Pattern NOUN_NUMR_ALL_PATTERN = Pattern.compile("noun:inanim:([mf]:v_naz|p:v_(naz|rod)):&numr.*|numr.*abbr.*|number");
  static final Pattern DVA_3_4_PATTERN = Pattern.compile("оби(два|дві)|(.+-)?((два|дві)|три|чотири)");
  private static final Pattern DVA_PATTERN = Pattern.compile("(оби)?два|.+-два", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern DVI_PATTERN = Pattern.compile("(оби)?дві|.+-дві", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  private static final Pattern _1_5 = Pattern.compile("([0-9]+[–-])?1,5");
  private static final Pattern _2_5 = Pattern.compile(".*(?<!1)[234],5");
  private static final Pattern _5_5 = Pattern.compile("([0-9]+[–-])?([0-9\\h]*[05-9]|[0-9\\h]*1[1-4]),5");
  private static final Pattern _FRA = Pattern.compile(".*,[1-9]+");
  private static final Pattern _2to4 = Pattern.compile("([0-9]+[–-])?[^,]*(?<!1)[234]");
  private static final Pattern _5to9 = Pattern.compile("[0-9\\h]*([5-90]|1[2-4])");
  private static final Pattern _5to9_ALPHA = Pattern.compile("(.+-)?(п.ять|шість|сім|вісім|(три)?дев.ять|.*дцять|сорок|.*десять?|дев.яносто|сто|двісті|триста|чотириста|півтораста|.+сот)|(де)?кілька|кількох|аніскільки");
  private static final Pattern NOUN_FORCE_PATTERN = Pattern.compile("чоловік|солдат|тон|(нано|мікро|мілі|дека|кіло|мега|гіга|тера|пета)?(герц|байт|біт|бар|бер|ват|вольт|децибел|рентген|моль|мікрон|грам|аршин|лат|карат)");

  private final Synthesizer synthesizer;

  public TokenAgreementNumrNounRule(ResourceBundle messages, Language ukrainian) throws IOException {
    super.setCategory(Categories.MISC.getCategory(messages));
    this.synthesizer = ukrainian.getSynthesizer();
  }

  @Override
  public final String getId() {
    return "UK_NUMR_NOUN_INFLECTION_AGREEMENT";
  }

  @Override
  public String getDescription() {
    return "Узгодження відмінків, роду і числа числівника та іменника";
  }

  public String getShort() {
    return "Узгодження числівника та іменника";
  }

  static class State {
    boolean number;
    int numrPos;
    int nounPos;
    List<AnalyzedToken> numrTokenReadings = new ArrayList<>(); 
    AnalyzedTokenReadings numrAnalyzedTokenReadings = null;
    
    public boolean isEmpty() {
      return numrTokenReadings.isEmpty();
    }
    public void reset() {
      number = false;
      numrTokenReadings.clear();
      numrAnalyzedTokenReadings = null;
    }
  }
  
  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    State state = new State();

    for (int i = 1; i < tokens.length; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];

      String posTag0 = tokenReadings.getAnalyzedToken(0).getPOSTag();
      String cleanToken = tokenReadings.getCleanToken();

      if( posTag0 == null || cleanToken == null ) {
        state.reset();
        continue;
      }
      

      if( state.isEmpty() ) {
        // no need to start checking on last token or if no noun
        if( i == tokens.length - 1 )
          continue;
      }

      String cleanTokenLower = cleanToken.toLowerCase();

      // grab initial numr inflections

      if( PosTagHelper.hasPosTag(tokens[i], NOUN_NUMR_ALL_PATTERN) ) {
        if( i < tokens.length-1
            && NOUN_FORCE_PATTERN.matcher(tokens[i+1].getCleanToken().toLowerCase()).matches() ) {
          state.reset();
          state.numrPos = i;
          state.numrTokenReadings.add(tokenReadings.getAnalyzedToken(0));
          state.numrAnalyzedTokenReadings = tokenReadings;
          state.number = PosTagHelper.hasPosTagStart(tokens[i], "number");
          continue;
        }
        
        if( i < tokens.length-2
            && PosTagHelper.hasPosTag(tokens[i+1], Pattern.compile("adj:p:v_rod.*"))
            && NOUN_FORCE_PATTERN.matcher(tokens[i+2].getCleanToken().toLowerCase()).matches()
            ) {
          state.reset();
          state.numrPos = i;
          state.numrTokenReadings.add(tokenReadings.getAnalyzedToken(0));
          state.numrAnalyzedTokenReadings = tokenReadings;
          state.number = PosTagHelper.hasPosTagStart(tokens[i], "number");
          i++;
          continue;
        }
      }

      if( PosTagHelper.hasPosTag(tokens[i], NUMR_PATTERN) ) {
        state.reset();
        
        // 57-ма вулиця
        if( cleanToken.matches(".*[0-9]-[а-яіїєґ].*") )
          continue;

        if( LemmaHelper.hasLemma(tokenReadings, Arrays.asList("мати"), "verb") ) {
          state.reset();
          continue;
        }
        // один з одним
        if( LemmaHelper.hasLemma(tokenReadings, Arrays.asList("один")) ) {
          state.reset();
          continue;
        }

        for (AnalyzedToken token: tokenReadings) {
          String adjPosTag = token.getPOSTag();

          // null can happen for words with \u0301 or \u00AD
          if( adjPosTag != null 
              && (adjPosTag.startsWith("numr") || NOUN_NUMR_ALL_PATTERN.matcher(adjPosTag).matches()) ) {
            state.numrPos = i;
            state.numrTokenReadings.add(token);
            state.numrAnalyzedTokenReadings = tokenReadings;
          }
        }

        continue;
      }
      else if( PosTagHelper.hasPosTag(tokens[i], "number") ) {
        state.numrPos = i;
        state.numrTokenReadings.addAll(tokens[i].getReadings());
        state.numrAnalyzedTokenReadings = tokenReadings;
        state.number = true;
        continue;
      }

      if( state.isEmpty() )
        continue;
      

      
      // skip for: два з половиною
      if( i < tokens.length - 2 
          && cleanTokenLower.matches("з|із|зі") 
            && tokens[i+1].getCleanToken().toLowerCase().matches("половиною|третиною|чвертю") ) {
          
        i += 1;
        continue;
      }

      if( i < tokens.length - 1
          && (_2to4.matcher(state.numrAnalyzedTokenReadings.getCleanToken().toLowerCase()).matches()
            || DVA_3_4_PATTERN.matcher(state.numrAnalyzedTokenReadings.getCleanToken().toLowerCase()).matches()) 
          && PosTagHelper.hasPosTag(tokens[i], Pattern.compile("adj:p:v_(rod|naz).*"))
          && PosTagHelper.hasPosTagAndToken(tokens[i+1], Pattern.compile(".*:m:v_rod.*"), Pattern.compile(".*[ая]")) ) {
            // skip adj for: 4 маленьких єнота
            continue;
      }

      String numrCleanToken = state.numrAnalyzedTokenReadings.getCleanToken();
      String numrToken = numrCleanToken.toLowerCase();

      if( numrToken.matches("(один-|одне-)?півтора") || _FRA.matcher(numrToken).matches() ) {
        if( cleanTokenLower.matches("раз|рази|разу|разів") ) {
          String msg = "Після десяткового дробу або «півтора» треба вживати «раза»";
          String url = "http://www.kulturamovy.org.ua/KM/pdfs/mix/61-12-65-26.pdf";
          RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, state.numrAnalyzedTokenReadings.getStartPos(), tokenReadings.getEndPos(), msg, getShort());
          potentialRuleMatch.addSuggestedReplacement(state.numrAnalyzedTokenReadings.getToken() + " раза");
          potentialRuleMatch.setUrl(new URL(url));
          ruleMatches.add(potentialRuleMatch);
          state.reset();
          continue;
        }
      }


      List<AnalyzedToken> nounTokenReadings = new ArrayList<>();

      for (AnalyzedToken token: tokenReadings) {
        String nounPosTag = token.getPOSTag();

        if( nounPosTag == null ) { // can happen for words with \u0301 or \u00AD
          continue;
        }

        if( PosTagHelper.hasPosTag(token, NOUN_IGNORE_PATTERN) ) {
          nounTokenReadings.clear();
          break;
        }

        if( nounPosTag.startsWith("noun") || nounPosTag.startsWith("adj") ) {
          nounTokenReadings.add(token);
        }
        else if ( nounPosTag.equals(JLanguageTool.SENTENCE_END_TAGNAME)
            || nounPosTag.equals(JLanguageTool.PARAGRAPH_END_TAGNAME) ) {
          continue;
        }
        else if( ! PosTagHelper.isPredictOrInsert(token) ) {
          nounTokenReadings.clear();
          break;
        }
      }
      
      // limit багато with m:v_rod - багато білку
      if( state.numrAnalyzedTokenReadings.getCleanToken().toLowerCase().endsWith("багато") ) {
        if( ! (PosTagHelper.hasMaleUA(tokenReadings) 
            || NOUN_FORCE_PATTERN.matcher(cleanTokenLower).matches() ) 
            ) {
          state.reset();
          continue;
        }
      }
      
      // no noun token - restart

      if( nounTokenReadings.isEmpty() ) {
        state.reset();
        continue;
      }

      state.nounPos = i;
      
      if( cleanTokenLower.equals("тон") ) {
        String msg = "Ви мали на увазі: «тонн»?";
        RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, tokenReadings.getStartPos(), tokenReadings.getEndPos(), msg, getShort());
        String repl = "тонн";
        potentialRuleMatch.addSuggestedReplacement(repl);
        ruleMatches.add(potentialRuleMatch);
        state.reset();
        continue;
      }

      
      logger.debug("=== Checking:\n\t{}\n\t{}", state.numrTokenReadings, nounTokenReadings);

      // perform the check

      String genderOfPluralNotFound = null;
      List<InflectionHelper.Inflection> masterInflections = new ArrayList<>();

      // чотири десятих відсотка
      if( state.numrPos == i - 2 
          && Arrays.asList("десятих", "сотих", "тисячних", "третіх", "четвертих").contains(tokens[i-1].getCleanToken().toLowerCase()) ) {
        masterInflections.clear();
        masterInflections.add(new Inflection("m", "v_rod", null));
        masterInflections.add(new Inflection("f", "v_rod", null));
        masterInflections.add(new Inflection("n", "v_rod", null));
      }
      else if( state.number ) {
        if( _5_5.matcher(numrCleanToken).matches() ) {
          masterInflections.add(new Inflection("p", "v_rod", null));
          masterInflections.add(new Inflection("m", "v_rod", null));
          masterInflections.add(new Inflection("f", "v_rod", null));
          masterInflections.add(new Inflection("n", "v_rod", null));
        }
        else if( _2_5.matcher(numrCleanToken).matches() ) {
          masterInflections.add(new Inflection("p", "v_naz", null));
          masterInflections.add(new Inflection("p", "v_zna", "inanim"));
          masterInflections.add(new Inflection("m", "v_rod", null));
          masterInflections.add(new Inflection("f", "v_rod", null));
          masterInflections.add(new Inflection("n", "v_rod", null));
        }
        else if( _1_5.matcher(numrCleanToken).matches() ) {
          masterInflections.add(new Inflection("m", "v_rod", null));
          masterInflections.add(new Inflection("f", "v_rod", null));
          masterInflections.add(new Inflection("n", "v_rod", null));
        }
        else if( _FRA.matcher(numrCleanToken).matches() ) {
          masterInflections.add(new Inflection("m", "v_rod", null));
          masterInflections.add(new Inflection("f", "v_rod", null));
          masterInflections.add(new Inflection("n", "v_rod", null));
        }
        else if( _2to4.matcher(numrCleanToken).matches()
            // limited scope: otherwise too many positives
            && PosTagHelper.hasPosTagAndToken(tokens[i], Pattern.compile(".*:m:v_rod.*"), Pattern.compile(".*[ая]")) ) {
//            || PosTagHelper.hasPosTagAndToken(tokens[i], Pattern.compile(".*:p:v_naz.*"), Pattern.compile(".*[и]"))) ) {
//              n1 = true;
//              state.nounPos = i+1;
              
          masterInflections.clear();
          masterInflections.add(new Inflection("p", "v_naz", null));
          masterInflections.add(new Inflection("p", "v_zna", null));
        }
        // 5-9/0 is very limited in xml rules
        else if( _5to9.matcher(numrCleanToken).matches()
            && NOUN_FORCE_PATTERN.matcher(cleanTokenLower).matches() ) {
//            && (LemmaHelper.hasLemma(tokens[i], Pattern.compile("(нано|мікро|мілі|дека|кіло|мега|гіга|тера|пета)?(герц|байт|біт|бар|бер|ват|вольт|децибел|рентген|моль|мікрон|грам|аршин|лат|карат|солдат|чоловік|тон)"), Pattern.compile("noun:(in)?anim:m:v_naz.*")) )){
//            || tokens[i].getCleanToken().equals("чоловік")) ) {
          masterInflections.add(new Inflection("p", "v_rod", null));
        }
        else {
          state.reset();
          continue;
        }
      }
      else {
        masterInflections = PosTagHelper.hasPosTag(state.numrTokenReadings, NUMR_PATTERN)
            ? InflectionHelper.getNumrInflections(state.numrTokenReadings)
                : Arrays.asList(new Inflection("p", "v_rod", null));
        
        List<Inflection> pVnazZna = masterInflections.stream()
            .filter(inf -> inf.gender.equals("p") && (inf._case.equals("v_naz") || inf._case.equals("v_zna")))
            .collect(Collectors.toList());
        
        if( pVnazZna.size() > 0 ) {

          if( _5to9_ALPHA.matcher(numrToken).matches() ) {
            masterInflections.removeAll(pVnazZna);
            masterInflections.add(new Inflection("p", "v_rod", null));
          }
          else if( numrToken.matches("((.+-)?(двоє|двох|троє|.+еро|.+ьох))|обидвоє|обидвох|обоє|обох") ) {
            masterInflections.removeAll(pVnazZna);
            masterInflections.add(new Inflection("p", "v_rod", null));
          }
          else if( numrToken.matches("(не)?багато|(не|чи)?мало|с[тк]ільки(-то|сь)?|.+-скільки|кілько") ) {
            masterInflections.removeAll(pVnazZna);
            masterInflections.add(new Inflection("p", "v_rod", null));
            masterInflections.add(new Inflection("m", "v_rod", null));
            masterInflections.add(new Inflection("n", "v_rod", null));
            masterInflections.add(new Inflection("f", "v_rod", null));
          }
          else if( numrToken.matches("пів") ) {
            masterInflections.clear();
//            masterInflections.removeAll(pVnazZna);
            masterInflections.add(new Inflection("m", "v_rod", null));
            masterInflections.add(new Inflection("f", "v_rod", null));
            masterInflections.add(new Inflection("n", "v_rod", null));
          }
          // на три дерева
          else if( DVA_3_4_PATTERN.matcher(numrToken).matches() ) {
            masterInflections.removeAll(pVnazZna);
            masterInflections.add(new Inflection("p", "v_naz", null));
            if( PosTagHelper.hasPosTag(nounTokenReadings, Pattern.compile("(noun:inanim:p:v_zna).*")) ) {
              masterInflections.add(new Inflection("p", "v_zna", null));
            }
            // три цікавих міста, but not два додаткових років
            else if( i < tokens.length - 1
                && PosTagHelper.hasPosTag(tokens[i], Pattern.compile("(adj:p:v_zna).*"))
                && PosTagHelper.hasPosTag(tokens[i+1], Pattern.compile("(noun:inanim:p:v_zna).*")) ) {
              masterInflections.add(new Inflection("p", "v_zna", null));
            }
            
            if( DVI_PATTERN.matcher(numrToken).matches() ) {
              String vidm = masterInflections.size() == 2 ? "(naz|zna)" : "naz";
              Pattern pattern = masterInflections.size() == 2 ? Pattern.compile("noun.*:p:v_" + vidm + "(?!:ns).*")
                  : Pattern.compile("noun.*:p:v_" + vidm + ".*");
              if (PosTagHelper.hasPosTag(nounTokenReadings, pattern)
                  && !PosTagHelper.hasPosTag(nounTokenReadings, Pattern.compile("adj:p:v_" + vidm + ".*"))) {
                HashSet<String> found = findSingulars(nounTokenReadings, pattern, ":f:");
                if (found != null && found.isEmpty()) {
                  genderOfPluralNotFound = "f";
                }
              }
            } else if (DVA_PATTERN.matcher(numrToken).matches()) {
              String vidm = masterInflections.size() == 2 ? "(naz|zna)" : "naz";
              Pattern pattern = masterInflections.size() == 2 ? Pattern.compile("noun.*:p:v_" + vidm + "(?!:ns).*")
                  : Pattern.compile("noun.*:p:v_" + vidm + ".*");
              if (PosTagHelper.hasPosTag(nounTokenReadings, pattern)
                  && !PosTagHelper.hasPosTag(nounTokenReadings, Pattern.compile("adj:p:v_" + vidm + ".*"))) {
                HashSet<String> found = findSingulars(nounTokenReadings, pattern, ":[mn]:");
                if (found != null && found.isEmpty()) {
                  genderOfPluralNotFound = "mn";
                }
              }
            }
          }
        }
        else {
          if( numrToken.matches("(один-|одне-)?півтора") ) {
              // TODO: force only direct inflections for півтора
              masterInflections.clear();
//              masterInflections.removeAll(pVnazZna);
            masterInflections.add(new Inflection("m", "v_rod", null));
            masterInflections.add(new Inflection("n", "v_rod", null));
          }
          else if( numrToken.matches("(одн.+-)?півтори") ) {
            masterInflections.clear();
//          masterInflections.removeAll(pVnazZna);
            masterInflections.add(new Inflection("f", "v_rod", null));
          }
        }
      }

      List<InflectionHelper.Inflection> nounInflections = InflectionHelper.getNounInflections(nounTokenReadings);
      List<InflectionHelper.Inflection> adjInflections = InflectionHelper.getAdjInflections(nounTokenReadings);
      nounInflections.addAll(adjInflections);
      // remove dups
      nounInflections = new ArrayList<>(new LinkedHashSet<>(nounInflections));

      boolean disjoint = Collections.disjoint(masterInflections, nounInflections);
      if( genderOfPluralNotFound != null || disjoint ) {

        if( TokenAgreementNumrNounExceptionHelper.isException(tokens, state, masterInflections, nounInflections, nounTokenReadings) ) {
          state.reset();
          continue;
        }

        if( logger.isDebugEnabled()) {
          logger.debug(MessageFormat.format("=== Found:\n\t{0}\n\t",
              state.numrAnalyzedTokenReadings.getToken() + ": " + masterInflections + " // " + state.numrAnalyzedTokenReadings,
            nounTokenReadings.get(0).getToken() + ": " + nounInflections+ " // " + nounTokenReadings));
        }

        String msg = String.format("Потенційна помилка: числівник не узгоджений з іменником: \"%s\" вимагає: [%s], а далі йде \"%s\": [%s]", 
            state.numrTokenReadings.get(0).getToken(), TokenAgreementAdjNounRule.formatInflections(masterInflections, true),
            nounTokenReadings.get(0).getToken(), TokenAgreementAdjNounRule.formatInflections(nounInflections, false));

        if( _1_5.matcher(numrCleanToken).matches() ) {
          msg = "Після «1,5» треба вживати родовий відмінок однини";
        }
        else if( _2_5.matcher(numrCleanToken).matches() ) {
          msg = "Після числівника, що закінчується на 2-4 і потім «,5», іменник має стояти в називному відмінку множини (якщо вимовляємо «з половиною»)";
          msg += ", або в родовом відмінку однини (якщо вимовляємо «і п'ять десятих»)";
        }
        else if( numrCleanToken.endsWith(",5") ) {
          msg = "Після числівника, що закінчується на 5-9 і потім «,5», іменник має стояти в родовому відмінку множини (якщо вимовляємо «з половиною»)";
          msg += ", або в родовом відмінку однини (якщо вимовляємо «і п'ять десятих»)";
        }
        else if( numrCleanToken.equalsIgnoreCase("півтора") ) {
          msg = "Існує правило, що після «півтора» треба вживати родовий відмінок ч. або с.р., однак у текстах в багатьох випадках вживають і форму множини, надто коли перед іменником іде прикметник";
        }
        else if( numrCleanToken.equalsIgnoreCase("півтори") ) {
          msg = "Існує правило, що після «півтора» треба вживати родовий відмінок ж.р., однак у текстах в багатьох випадках вживають і форму множини, надто коли перед іменником іде прикметник";
        }
        else if( masterInflections.contains(new Inflection("m", "v_rod", null))
            && tokens[i].getToken().matches(".*[ую]")
            && PosTagHelper.hasPosTag(nounTokenReadings, "noun.*?:m:v_dav.*") ) {
          msg += CaseGovernmentHelper.USED_U_INSTEAD_OF_A_MSG;
        }
        else if( ! PosTagHelper.hasPosTag(state.numrTokenReadings, "adj.*?v_mis.*")
            && PosTagHelper.hasPosTag(nounTokenReadings, "noun.*?v_mis.*") ) {
          msg += ". Можливо, пропущено прийменник на/в/у...?";
        }

        if( ! disjoint && genderOfPluralNotFound != null ) {
          msg += ". Можливо, не збігається рід однини для множинної форми?";
        }

        RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, state.numrAnalyzedTokenReadings.getStartPos(), tokenReadings.getEndPos(), msg, getShort());

        List<String> suggestions = new ArrayList<>();

        if( ! disjoint && genderOfPluralNotFound != null ) {
//          msg += ". Можливо, не збігається рід однини для множинної форми?";
          String sugg1 = "f".equals(genderOfPluralNotFound)
              ? numrCleanToken.replaceFirst("і$", "а") // два -> дві
                  : numrCleanToken.replaceFirst("а$", "і"); // дві -> два
          suggestions = Arrays.asList(sugg1 + " " + tokens[state.nounPos].getToken());
        }
        else {
        for (Inflection numrInflection : masterInflections) {
          String genderTag = ":"+numrInflection.gender+":";
          String vidmTag = numrInflection._case;


          for(AnalyzedToken nounToken: nounTokenReadings) {

            if( numrInflection.animMatters() ) {
              String animTag = nounToken.getPOSTag().startsWith("noun") 
                  ? ":" + numrInflection.animTag
                      : ":r" + numrInflection.animTag;
              if( ! nounToken.getPOSTag().contains(animTag) )
                continue;
            }
            String newNounPosTag = nounToken.getPOSTag().replaceFirst(":.:v_...", genderTag + vidmTag);

            try {
              String[] synthesized = synthesizer.synthesize(nounToken, newNounPosTag, false);

              for (String s : synthesized) {

                if( numrCleanToken.equalsIgnoreCase("півтора")
                    && nounToken.getLemma().equals("раз") && ! s.equals("раза") )
                  continue;

                String suggestion = state.numrAnalyzedTokenReadings.getToken();
                for(int j=state.numrPos+1; j<state.nounPos; j++ ) {
                  suggestion += " " + tokens[j].getToken(); // add middle adj
                }
                suggestion += " " + s;
                
                if( ! suggestions.contains(suggestion) ) {
                  suggestions.add(suggestion);
                }
              }
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        }
        }

        if( suggestions.size() > 0 ) {
            potentialRuleMatch.setSuggestedReplacements(suggestions);
        }

        ruleMatches.add(potentialRuleMatch);
      }

      state.reset();
    }

    return toRuleMatchArray(ruleMatches);
  }

  private HashSet<String> findSingulars(List<AnalyzedToken> nounTokenReadings, Pattern pattern, String lookFor) throws IOException {
    HashSet<String> found = new HashSet<>();
    for(AnalyzedToken tr: nounTokenReadings) {
      if( PosTagHelper.hasPosTag(tr, pattern) ) {
        String[] synthTokens0 = synthesizer.synthesize(tr, tr.getPOSTag(), false);
        if (synthTokens0.length == 0) // dynamicly tagged: // наглядачки-африканерки
          return null;

        if( ! found.contains(tr.getLemma()) ) {
          // два ока - noun:inanim:p:v_naz:var
          String singularTag = tr.getPOSTag().replace(":p:", lookFor).replaceAll(":(var|bad|arch)", ".*");
          String[] synthTokens = synthesizer.synthesize(tr, singularTag, true);
          found.addAll(Arrays.asList(synthTokens));
        }
      }
    }
    return found;
  }

}
