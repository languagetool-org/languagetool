/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà i Font
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
package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Category;
import org.languagetool.rules.RuleMatch;

/**
 * This rule checks if a word without graphical accent and with a verb POS tag should be
 * a noun or an adjective with graphical accent.  
 * It uses two lists of word pairs: verb-noun and verb-adjective.
 *   
 * @author Jaume Ortolà i Font
 */
public class ReflexiveVerbsRule extends CatalanRule {

  /**
   * Patterns
   */
  
  private static final Pattern VERBS_REFLEXIUS = Pattern.compile("abacallanar|abalançar|abaltir|ablenar|aborrallonar|abotifarrar|abrinar|abromar|abstenir|acagallonar|acanyar|acarcanyar|acarnissar|acatarrar|aclivellar|aclocar|aclofar|acorriolar|adelerar|adir|adonar|adormissar|afaenar|afal·lerar|afarrossar|afeccionar|aferrallar|aferrissar|aferrussar|aficionar|afincar|agallinar|agarbir|agarrofar|agemolir|agenollar|aglevar|agomboiar|agotzonar|agrimar|agrumollar|aiguabarrejar|ajocar|allitar|allocar|altivar|alçurar|amatinar|amatonar|amelar|amigar|amistançar|amoixir|amoltonar|amotar|amullerar|amunionar|antullar|anyocar|aparroquianar|aparroquiar|aperduar|apergaminar|apiadar|apirrossar|apomar|aponar|aponentar|apropinquar|apugonar|aquedar|arnar|arrapinyar|arrasir|arravatar|arraïmar|arrepapar|arrepetellar|arrigolar|arrogar|arrossar|arruar|arrumbar|assemblar|atendar|atenir|atorrentar|atrafegar|atrevir|autocompadir|autodestruir|avencar|avenir|avidolar|avinençar|balbar|balcar|baldar|balir|balmar|bequetejar|bescomptar|bifurcar|boirar|botinflar|bromar|cagaferrar|candir|capbaixar|capguardar|capmassar|captenir|cariar|carnificar|carpir|coalitzar|colltrencar|collvinclar|compenetrar|condoldre|condolir|condormir|congraciar|contorçar|contrapuntar|contòrcer|corcorcar|coresforçar|corfondre|cornuar|corruixar|cruspir|decandir|demacrar|desacalorar|desafeccionar|desalenar|desamorar|desaparroquiar|desapassionar|desaplegar|desavenir|desbandar|desbocar|descantar|descarar|descontrolar|descovar|desdejunar|desdubtar|deseixir|desemmurriar|desempallegar|desencaboriar|desencapritxar|desencartonar|desenfebrar|desenfellonir|desengalavernar|desengavatxar|desennuvolar|desenrabiar|desenravenar|desenrojolar|desensonyar|desentaular|desentendre|desentestar|desentossudir|desentrampar|desentusiasmar|desfeinar|desmemoriar|desmenjar|desmorriar|desnodrir|desomplir|despondre|despreocupar|desteixinar|desvagar|desvergonyir|desviure|devanir|dignar|dismutar|embarbollar|embarbussar|embascar|embessonar|embofegar|embordeir|embordonir|emborrascar|emborrossar|embotifarrar|embotzegar|embromallar|embromar|embroquerar|emmainadar|emmalesar|emmalurar|emmalurir|emmarar|emmarranar|emmatar|emmigranyar|emmorronar|emmurriar|empassar|empassolar|empegueir|empenyalar|empescar|empillocar|empinyar|empiocar|empitarrar|emplomissar|emplujar|emportar|enamoriscar|encabotar|encabritar|encalmar|encalostrar|encanallar|encaparrotar|encaterinar|encinglar|encirar|encistar|enclaperar|encolerir|encondolir|encordar|encotxar|encovar|encruar|endarreriar|enderiar|endiumenjar|endoblir|endormiscar|endur|enfarfollar|enfaristolar|enfavar|enfebrar|enfereir|enferestir|enferotgir|enferritjar|enfigar|enfistular|enfugir|enfurrunyar|enfutimar|enfutismar|engalavernar|engallar|engallir|engallofir|engargussar|engarrofar|engelabrir|engolfar|engorgar|engronyar|engrunyonir|enguerxinar|enjullar|enllagrimar|enlleganyar|enlleir|enllitegar|ennavegar|enneguitar|ennigular|ennuvolar|enquistar|enreular|enriallar|enrinxar|enriolar|enrogallar|enrojolar|enrullar|ensamarrar|enseguir|ensobecar|ensobinar|ensonyar|ensotar|ensulsiar|ensulsir|enteranyinar|entercar|entonyinar|entornar|entossar|entossudir|entotsolar|entreabaltir|entrebadar|entrebatre|entrebesar|entrecavalcar|entrecreuar|entredevorar|entreferir|entreforcar|entregirar|entrelligassar|entrematar|entremetre|entremirar|entrenyorar|entresaludar|entreseguir|entresentir|entresoldar|entretocar|entretopar|entretzenar|entrexocar|entrigar|envelar|enventar|envidreir|envolar|enxautar|esbadocar|esbafar|esbafegar|esbatussar|esbellussar|esblaimar|esblamar|esbojarrar|esborneiar|esbravar|esbromar|escabridar|escagaixar|escagarrinar|escagassar|escalaixar|escamnar|escamotar|escanyellar|escanyolir|escanyussar|escapolar|escapolir|escarcanyar|escarramicar|escarranxar|escarrassar|escarxofar|escatifenyar|esclarissar|esconillar|escorporar|escridar|escudejar|escullar|escunçar|esfarinar|esfetgegar|esforçar|esgargamellar|esgatifinyar|esgatinyar|esgolar|esguimbar|esllanguir|esllavissar|esmadeixar|esmarrar|esparpellar|esperesar|esperitar|espeuar|espitellar|espitregar|espitxar|espollinar|espoltrar|esporcellar|espotonar|esprimatxar|espunyir|esquerdissar|esquifir|esquitllar|estilar|estorinar|estrallar|estritllar|esvanir|esvedellar|esventegar|esventrellar|esvorar|etiolar|extralimitar|extravasar|extravenar|fincar|flixar|gamar|gaspar|gatinyar|gaubar|gloriar|grifar|gronxolar|hipertrofiar|immiscir|indigestar|indisciplinar|industriar|insolentar|insurgir|intersecar|inveterar|irèixer|jactar|juramentar|lexicalitzar|llufar|malfiar|malfixar|marfondir|marfondre|migrolar|mofar|morfir|mullerar|neular|neulir|neurotitzar|obstinar|octubrar|olivar|pellobrir|pellpartir|pelltrencar|penedir|penjolar|personar|pollar|prosternar|queixar|querar|querellar|quillar|ramificar|rampellar|realegrar|rebel·lar|rebordeir|rebordonir|refiar|rellentir|repairar|repanxolar|repapar|repenjar|repetellar|resclosir|ressagar|ressentir|ressobinar|revenjar|rifar|ruboritzar|salinar|sangcremar|sangglaçar|sobresanar|solsir|sotaplujar|suïcidar|tallgirar|tinyar|tolir|transvestir|traslluir|traspaperar|traspostar|trufar|vanagloriar|vanagloriejar|vanar|vantar|vergonyar|xautar");
  private static final Pattern NO_VERBS_REFLEXIUS = Pattern.compile("atendre|escollir");
  private static final Pattern VERB_HAVER = Pattern.compile("haver");
//  private static final Pattern VERB_ANAR = Pattern.compile("anar");
  private static final Pattern NO_VERB = Pattern.compile("N.*|A.*|_GN_.*");
  private static final Pattern UPPERCASE = Pattern.compile("\\p{Lu}.*");
  // V[MAS][ISMNGP][PIFSC0][123][SP][MF]
  
//  private static final Pattern VERB= Pattern.compile("V.*");
  private static final Pattern VERB_INDSUBJ = Pattern.compile("V.[SI].*");
  private static final Pattern VERB_INDSUBJIMP = Pattern.compile("V.[MSI].*");
  private static final Pattern VERB_IMP = Pattern.compile("V.M.*");
//  private static final Pattern VERB_INF = Pattern.compile("V.N.*");
  private static final Pattern VERB_INFGER = Pattern.compile("V.[NG].*");
//  private static final Pattern VERB_GER = Pattern.compile("V.G.*");
  private static final Pattern VERB_PART = Pattern.compile("V.P.*");
//  private static final Pattern PREPOSICIO = Pattern.compile("SPS00");
  private static final Pattern PREP_VERB_PRONOM = Pattern.compile("SPS00|V.*|P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP3CP000|PP3CSD00");
    
  private static final Pattern VERB_1S = Pattern.compile("V...1S.");
  private static final Pattern VERB_2S = Pattern.compile("V...2S.");
  private static final Pattern VERB_3S = Pattern.compile("V...3S.");
  private static final Pattern VERB_1P = Pattern.compile("V...1P.");
  private static final Pattern VERB_2P = Pattern.compile("V...2P.");
  private static final Pattern VERB_3P = Pattern.compile("V...3P.");
  
  private static final Pattern PRONOM_FEBLE_1S = Pattern.compile("P010S000");
  private static final Pattern PRONOM_FEBLE_2S = Pattern.compile("P020S000");
  private static final Pattern PRONOM_FEBLE_3S = Pattern.compile("P0300000");
  private static final Pattern PRONOM_FEBLE_1P = Pattern.compile("P010P000");
  private static final Pattern PRONOM_FEBLE_2P = Pattern.compile("P020P000");
  private static final Pattern PRONOM_FEBLE_3P = Pattern.compile("P0300000");
  private static final Pattern PRONOM_FEBLE_13S = Pattern.compile("P010S000|P0300000");
  private static final Pattern PRONOM_FEBLE_23S = Pattern.compile("P020S000|P0300000");
  
  private static final Pattern PRONOM_FEBLE = Pattern.compile("P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP3CP000|PP3CSD00"); // tots els pronoms febles
  private static final Pattern PRONOM_REFLEXIU = Pattern.compile("P0.0.*"); //me te se ens us (i variants)
  
  //private static final Pattern REFLEXIU_POSPOSAT = Pattern.compile("-[mts]|-[mts]e|'[mts]|-nos|'ns|-vos|-us",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  //private static final Pattern REFLEXIU_DAVANT = Pattern.compile("e[mts]|[mts]e|ens|us|-[mts]|-[mts]e|'[mts]|[mts]'|-nos|'ns|-vos|-us",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  
 // <token postag="P0.*|PP.*" postag_regexp="yes"><exception postag="_GN_.*" postag_regexp="yes"/><exception regexp="yes">jo|mi|tu|ella?|nosaltres|vosaltres|elle?s|vost[èé]s?|vós</exception><exception postag="allow_saxon_genitive">'s</exception></token>
  
   
  public ReflexiveVerbsRule(ResourceBundle messages) throws IOException {
	  if (messages != null) {
		  super.setCategory(new Category("Verbs"));
	  }
  }

  
  @Override
  public String getId() {
    return "REFLEXIVE_VERBS";
  }

  @Override
  public String getDescription() {
    return "Verbs reflexisu: comprova que porten el pronom adequat.";
  }

  @Override
  public RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    loop:
    for (int i = 1; i < tokens.length; i++) {   //ignoring token 0, i.e., SENT_START
    	boolean theRuleMatches=true;
    	
    	final String token;
    	if (i == 1) {
    		token = tokens[i].getToken().toLowerCase();
    	} else {
    		token = tokens[i].getToken();
    	}

    	/* Formes possibles amb verb reflexiu:
    	 * 1) es queixa, se li queixa
    	 * 2) queixa't, queixeu-vos
    	 * 3) s'ha queixat, se li ha queixat
    	 * 4) es va queixar, se li va queixar, va queixar-se, va queixar-se-li
    	 *    em puc queixar, ens en podem queixar, podeu queixar-vos
    	 *    es va queixant, va queixant-se, comencen queixant-se
    	 * queixar-se
    	 * 5) no t'has de queixar, no has de queixar-te, pareu de queixar-vos, comenceu a queixar-vos, has d'emportar-t'hi
    	 * 6) no podeu deixar de queixar-vos, no us podeu deixar de queixar, 
    	 *    en teniu prou amb queixar-vos, no has de poder-te queixar
    	 * 
    	 * queixar-se, queixant-vos 
    	 * haver-se/havent-se queixat, poder-se queixar
    	 * poder-se queixar, has de poder-te queixar
    	 */

    	if (!matchLemmaRegexp(tokens[i],VERBS_REFLEXIUS))
    		continue loop;
    	//atengué l'administració
    	if (matchLemmaRegexp(tokens[i],NO_VERBS_REFLEXIUS)) 
    		continue loop;
    	if (matchPostagRegexp(tokens[i],NO_VERB))
    		continue loop;
    	final Matcher mUpperCase=UPPERCASE.matcher(tokens[i].getToken());
    	if (i>1 && mUpperCase.matches())
    		continue loop;

    	Pattern pPronomBuscat=null;
    	// 1) es queixa, se li queixa, se li'n queixa
    	if (matchPostagRegexp(tokens[i],VERB_INDSUBJ)) {
    		pPronomBuscat=pronomPattern(tokens[i]);
    		if (pPronomBuscat!=null) {
        		int j=1;
        		boolean keepCounting=true;
        		while (i-j>0 && j<4 && keepCounting) {
        			if (matchPostagRegexp(tokens[i-j],pPronomBuscat))  
        				continue loop;
        			keepCounting=matchPostagRegexp(tokens[i-j],PRONOM_FEBLE);
        			j++;
        		}
        	}
    	}
    	// 2) queixa't, queixeu-vos-hi
    	if (matchPostagRegexp(tokens[i],VERB_IMP)) {
    		pPronomBuscat=pronomPattern(tokens[i]);
    		if (pPronomBuscat!=null) {
    			if (i < tokens.length-1 && matchPostagRegexp(tokens[i+1],pPronomBuscat))
    				continue loop;
    		}
    	}
    	// 3) s'ha queixat, se li ha queixat, se li n'ha queixat. 
    	if (matchPostagRegexp(tokens[i],VERB_PART) ) {
    		if (matchLemmaRegexp(tokens[i-1],VERB_HAVER) 
    				&& matchPostagRegexp(tokens[i-1],VERB_INDSUBJ)) {
    			pPronomBuscat=pronomPattern(tokens[i-1]);
    			if (pPronomBuscat!=null) {
    				int j=2;
    				boolean keepCounting=true;
    				while (i-j>0 && j<5 && keepCounting) {
            			if (matchPostagRegexp(tokens[i-j],pPronomBuscat))  
            				continue loop;
    					keepCounting=matchPostagRegexp(tokens[i-j],PRONOM_FEBLE);
    					j++;
    				}
    			}
    		}
    		// *havent queixat, *haver queixat
    		else if (!(matchLemmaRegexp(tokens[i-1],VERB_HAVER) 
    				&& matchPostagRegexp(tokens[i-1],VERB_INFGER)))
    			continue loop;
    	}   
    	// 4) em vaig queixar, se li va queixar, se li'n va queixar, vas queixar-te'n, 
    	//    em puc queixar, ens en podem queixar, podeu queixar-vos,
    	//    es va queixant, va queixant-se, comences queixant-te
    	// 5) no t'has de queixar, no has de queixar-te, pareu de queixar-vos, comenceu a queixar-vos
    	//    corre a queixar-se, corre a queixar-te, vés a queixar-te
    	//    no hauria pogut burlar-me
    	// 6) no podeu deixar de queixar-vos, no us podeu deixar de queixar
    	//    en teniu prou amb queixar-vos, comenceu lentament a queixar-vos
    	// 7) no es va poder emportar, va decidir suïcidar-se,
    	// 8) Queixar-se, queixant-vos, podent abstenir-se
    	if (matchPostagRegexp(tokens[i],VERB_INFGER)) {
    		int k=1;
    		boolean keepCounting=true;
    		boolean foundVerb=false;
    		while (i-k>0 && keepCounting && !foundVerb) {
    			foundVerb=matchPostagRegexp(tokens[i-k],VERB_INDSUBJIMP);
				keepCounting=matchPostagRegexp(tokens[i-k],PREP_VERB_PRONOM);
				k++;
    		}
    		if (foundVerb) {
    			pPronomBuscat=pronomPattern(tokens[i-k+1]);
        		if (pPronomBuscat!=null) {
        			if (i < tokens.length-1 && matchPostagRegexp(tokens[i+1],pPronomBuscat))
        				continue loop;
        			int j=1;
        			keepCounting=true;
        			while (i-j>0 && keepCounting) {
            			if (matchPostagRegexp(tokens[i-j],pPronomBuscat))  
            				continue loop;
        				keepCounting=matchPostagRegexp(tokens[i-j],PREP_VERB_PRONOM);
        				j++;
        			}
        		}
    		}
    		else {
    			if (i < tokens.length-1 && matchPostagRegexp(tokens[i+1],PRONOM_REFLEXIU))
        			continue loop;
        		int j=1;
    			keepCounting=true;
    			while (i-j>0 && keepCounting) {
    				if (matchPostagRegexp(tokens[i-j],PRONOM_REFLEXIU))
            			continue loop;
    				keepCounting=matchPostagRegexp(tokens[i-j],PREP_VERB_PRONOM);
    				j++;
    			}
    		}
    	}
    	
    	if (theRuleMatches) {
    		final String msg = "Aquest verb és reflexiu. Falta un pronom.";
    		final RuleMatch ruleMatch = new RuleMatch(this, tokens[i].getStartPos(), tokens[i].getStartPos()+token.length(), msg, "Verb reflexiu: falta un pronom");
    		ruleMatches.add(ruleMatch);
    	}
    }
    return toRuleMatchArray(ruleMatches);
  }

  /**
   * Find appropiate pronoun pattern. (Troba el pronom feble apropiat)
   */ 
  private Pattern pronomPattern(AnalyzedTokenReadings aToken) {
	if (matchPostagRegexp(aToken,VERB_1S) && matchPostagRegexp(aToken,VERB_3S))
		return PRONOM_FEBLE_13S;
	if (matchPostagRegexp(aToken,VERB_2S) && matchPostagRegexp(aToken,VERB_3S))
		return PRONOM_FEBLE_23S;
	else if (matchPostagRegexp(aToken,VERB_1S) )
		return PRONOM_FEBLE_1S;
	else if (matchPostagRegexp(aToken,VERB_2S) )
		return PRONOM_FEBLE_2S;
	else if (matchPostagRegexp(aToken,VERB_3S) )
		return PRONOM_FEBLE_3S;
	else if (matchPostagRegexp(aToken,VERB_1P) )
		return PRONOM_FEBLE_1P;
	else if (matchPostagRegexp(aToken,VERB_2P) )
		return PRONOM_FEBLE_2P;
	else if (matchPostagRegexp(aToken,VERB_3P) )
		return PRONOM_FEBLE_3P;
	else
		return null;
  }
  
  /**
   * Match POS tag with regular expression
   */
  private boolean matchPostagRegexp(AnalyzedTokenReadings aToken, Pattern pattern) {
    boolean matches = false;
    final int readingsLen = aToken.getReadingsLength();
    for (int i = 0; i < readingsLen; i++) {
      final String posTag = aToken.getAnalyzedToken(i).getPOSTag();
      if (posTag != null) {
        final Matcher m = pattern.matcher(posTag);
        if (m.matches()) {
          matches = true;
          break;
        }
      }
    }
    return matches;
  }
  
  /**
   * Match lemma with regular expression
   */
  private boolean matchLemmaRegexp(AnalyzedTokenReadings aToken, Pattern pattern) {
    boolean matches = false;
    final int readingsLen = aToken.getReadingsLength();
    for (int i = 0; i < readingsLen; i++) {
      final String posTag = aToken.getAnalyzedToken(i).getLemma();
      if (posTag != null) {
        final Matcher m = pattern.matcher(posTag);
        if (m.matches()) {
          matches = true;
          break;
        }
      }
    }
    return matches;
  }

  @Override
  public void reset() {
    // nothing
  }

}
