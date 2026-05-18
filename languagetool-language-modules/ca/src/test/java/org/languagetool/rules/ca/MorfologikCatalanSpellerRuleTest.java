/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

import org.junit.Test;
import org.languagetool.*;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MorfologikCatalanSpellerRuleTest {

  private JLanguageTool lt;
  private MorfologikCatalanSpellerRule rule;
  private int maxSuggestions = 5;

  private void assertSuggestionsTest(String sentenceStr, String suggestions, int numMatches) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(sentenceStr));
    assertEquals(numMatches, matches.length);
    if (numMatches > 0) {
      List<String> suggestedReplacements = matches[0].getSuggestedReplacements();
      assertEquals(suggestions, suggestedReplacements.subList(0, Math.min(maxSuggestions, suggestedReplacements.size())).toString());
    }
  }

  @Test
  public void testMorfologikSpeller() throws IOException {
    lt = new JLanguageTool(Catalan.getInstance());
    rule = new MorfologikCatalanSpellerRule(TestTools.getMessages("ca"), Catalan.getInstance(),
      null, Collections.emptyList());
    //buggy!
    assertSuggestionsTest("pissara", "[passarà, passara, passera, pàssera, passar]", 1);
    assertSuggestionsTest("Tornaràn", "[Tornaran]", 1);
    // prefixes and suffixes.
    assertSuggestionsTest("S'autodefineixin com a populars.", "", 0);
    assertSuggestionsTest("Redibuixen el futur.", "", 0);
    assertSuggestionsTest("L'exdirigent del partit.", "", 0);
    assertSuggestionsTest("S'autoprenia.", "", 0);
    assertSuggestionsTest("S'autocanta.", "", 0);
    assertSuggestionsTest("CatalanoAmericans.", "[Catalanoamericans]", 1);
    assertSuggestionsTest("lAjuntament", "[l'Ajuntament, l'ajuntament, ajuntament]", 1);
    // word not well-formed with prefix
    assertSuggestionsTest("S'autopren.", "[estupren]", 1);
    assertSuggestionsTest("Any2010", "[Any 2010]", 1);
    // correct sentences:
    assertSuggestionsTest("Abacallanada", "", 0);
    assertSuggestionsTest("Abatre-les-en", "", 0);
    assertSuggestionsTest("Allò que més l'interessa.", "", 0);
    // checks that "WORDCHARS ·-'" is added to Hunspell .aff file
    assertSuggestionsTest("Porta'n quatre al col·legi.", "", 0);
    assertSuggestionsTest("Has de portar-me'n moltes.", "", 0);
    assertSuggestionsTest(",", "", 0);
    // Spellcheck dictionary contains Valencian and general accentuation
    assertSuggestionsTest("Francès i francés.", "", 0);
    // checks abbreviations
    assertSuggestionsTest("Viu al núm. 23 del carrer Nou.", "", 0);
    assertSuggestionsTest("N'hi ha de color vermell, blau, verd, etc.", "", 0);
    // Test for Multiwords.
    assertSuggestionsTest("Era vox populi.", "", 0);
    assertSuggestionsTest("Aquell era l'statu quo.", "", 0);
    assertSuggestionsTest("Va ser la XIV edició.", "", 0);
    //test for "LanguageTool":
    assertSuggestionsTest("LanguageTool!", "", 0);
    //test for numbers, punctuation
    assertSuggestionsTest(",", "", 0);
    assertSuggestionsTest("123454", "", 0);
    assertSuggestionsTest("1234,54", "", 0);
    assertSuggestionsTest("1.234,54", "", 0);
    assertSuggestionsTest("1 234,54", "", 0);
    assertSuggestionsTest("-1 234,54", "", 0);
    assertSuggestionsTest("Fa una temperatura de 30°C", "", 0);
    assertSuggestionsTest("Fa una temperatura de 30 °C", "", 0);
    assertSuggestionsTest("−0,4 %, −0,4%.", "", 0); // minus sign
    //tests for mixed case words
    assertSuggestionsTest("pH", "", 0);
    assertSuggestionsTest("McDonald", "", 0);
    assertSuggestionsTest("AixòÉsUnError", "[Això És Un Error]", 1);
    //incorrect words:
    assertSuggestionsTest("Bordoy", "[Bordó, Bordoi, Bordo, Borðoy, Burdur]", 1);
    assertSuggestionsTest("Mal'aysia", "[Malàisia, Malvasia]", 1);
    assertSuggestionsTest("Mala’ysia", "[Malàisia, Melanèsia]", 1);
    assertSuggestionsTest("Malaysia", "[Malàisia, Malay sia]", 1);
    assertSuggestionsTest("Mal'aysia", "[Malàisia, Malvasia]", 1);
    assertSuggestionsTest("quna", "[que, una, quan, bona, dona]", 1); //millor: quan
    //capitalized suggestion
    assertSuggestionsTest("Video", "[Vídeo]", 1);
    assertSuggestionsTest("bànner", "[Banner, bàner, Bonner, baner, vanar]", 1);
    assertSuggestionsTest("especialisats", "[especialitzats]", 1);
    assertSuggestionsTest("colaborassió", "[col·laboració]", 1);
    assertSuggestionsTest("colaboració", "[col·laboració]", 1);
    assertSuggestionsTest("sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss", "[]", 1);
    assertSuggestionsTest("plassa", "[plaça, Plessa, plaçà, passa, classe]", 1);
    assertSuggestionsTest("Deú", "[Deu, Déu, Dau, De, Del]", 1);
    assertSuggestionsTest("joan", "[Joan]", 1);
    assertSuggestionsTest("abatusats", "[abatussats]", 1);
    // incomplete multiword
    assertSuggestionsTest("L'statu", "[tato, Satto, Steno, tatú, sta tu]", 1);
    assertSuggestionsTest("argüit", "[arguït]", 1);
    assertSuggestionsTest("ángel", "[àngel, àngels, anual, angle, anhel]", 1);
    assertSuggestionsTest("caçessim", "[cacéssim, cassàssim, casséssim, casàssim, caséssim]", 1);
    assertSuggestionsTest("coche", "[cotxe, cuixa, coixa, cuixé, cotxa]", 1);
    assertSuggestionsTest("cantaríà", "[cantaria]", 1);
    //best suggestion first
    assertSuggestionsTest("poguem", "[puguem]", 1);
    //incorrect mixed case words
    assertSuggestionsTest("PH", "[pH, PP, H, PA, AH]", 1);
    assertSuggestionsTest("Ph", "[pH, PP, H, Pa, Ah]", 1);
    assertSuggestionsTest("MCDonald", "[McDonald]", 1);
    assertSuggestionsTest("tAula", "[taula, taulà, taüla, taule, taules]", 1);
    assertSuggestionsTest("TAula", "[Taula, Taulà, Taüla, Taule, Taules]", 1);
    assertSuggestionsTest("col·Labora", "[col·labora, col·laborà, col·labore]", 1);
    assertSuggestionsTest("col·laborÀ", "[col·labora, col·laborà]", 1);
    assertSuggestionsTest("después", "[després, desprès, despès, dèspotes, descoes]", 1);
    assertSuggestionsTest("dessinstalasio", "[desinstal·làssiu]", 1); //millorable!

    // done by simple replace verb
    //assertSuggestionsTest("Buco", "[Boço, Boca, Bou, Coco, Botó]", 1);
    //assertSuggestionsTest("matitzàrem", "[matisarem, matisàrem, emetitzarem, emetitzàrem, matisara]", 1);
    //assertSuggestionsTest("tamitzéssim", "[tamisàssim, tamiséssim, tamisassin, tamisassis, tamisessin]", 1);
    assertSuggestionsTest("No vull posarmi ara.", "[posar-m'hi]", 1);
    assertSuggestionsTest("Podria oposarmi.", "[oposar-m'hi]", 1);
    assertSuggestionsTest("Volem acostarsi.", "[acostar-s'hi, acostar si]", 1);
    assertSuggestionsTest("Volem acostarlosi.", "[acostar-los-hi, acostar-los]", 1);
    assertSuggestionsTest("Volem acostarnosi.", "[acostar-nos-hi, acostar-nos]", 1);
    assertSuggestionsTest("adquireixquen", "[adquirisquen, adquiresquen]", 1);
    assertSuggestionsTest("calificació", "[qualificació]", 1);
    assertSuggestionsTest("desconte", "[descompte, descompta]", 1);
    assertSuggestionsTest("transtors", "[transcurs]", 1); //millorable
    assertSuggestionsTest("atentats", "[atemptats]", 1);
    assertSuggestionsTest("síntomes", "[símptomes]", 1);
    assertSuggestionsTest("atentats", "[atemptats]", 1);
    assertSuggestionsTest("contable", "[comptable]", 1);
    assertSuggestionsTest("desició", "[decisió]", 1);
    assertSuggestionsTest("España", "[Espanya, Espanye, Espanyà, Espenya, Espenye]", 1);
    assertSuggestionsTest("concenciosament", "[conscienciosament]", 1);
    assertSuggestionsTest("conscienciosament", "", 0);
    assertSuggestionsTest("excelent", "[excel·lent]", 1);
    assertSuggestionsTest("exceleixquen", "[excel·lisquen, excel·lesquen]", 1);
    assertSuggestionsTest("caligrafia", "[cal·ligrafia, cal·ligrafie, cal·ligrafià]", 1);
    assertSuggestionsTest("calificador", "[qualificador]", 1);
    assertSuggestionsTest("Excelentissim", "[Excel·lentíssim]", 1);
    assertSuggestionsTest("IlustRISIM", "[Il·lustríssim]", 1);
    assertSuggestionsTest("Xicago", "[Xicano, Xi cago]", 1);
    assertSuggestionsTest("Chile", "[Xile]", 1);
    assertSuggestionsTest("transment", "[transmet, transient, trenament]", 1);
    assertSuggestionsTest("nordment", "[normant, nord ment]", 1);
    assertSuggestionsTest("milisegons", "[mil·lisegons]", 1);
    /*  change in Speller necessary: words of length = 4*/
    assertSuggestionsTest("nula", "[nul·la, Nola, Nole, Nule, nova]", 1);
    assertSuggestionsTest("En la Pecra", "[Para, Pare, Pere, Pedra, Pacte]", 1);
    assertSuggestionsTest("IVa", "[Iva, IVA, Va, Ve, Viva]", 1);
    assertSuggestionsTest("Dvd", "[DVD]", 1);
    assertSuggestionsTest("aõh", "[AOH, ah, eh, oh, AAH]", 1);
    assertSuggestionsTest("Windows10", "[Windows 10]", 1);
    assertSuggestionsTest("windows10", "[Windows 10]", 1);
    // deprecated characters of "ela geminada"
    assertSuggestionsTest("S'hi havien instaŀlat.", "", 0);
    assertSuggestionsTest("S'HI HAVIEN INSTAĿLAT.", "", 0);
    assertSuggestionsTest("a", "", 0);
    // pronoms febles
    assertSuggestionsTest("ferse", "[fer-se, farsa]", 1);
    assertSuggestionsTest("Magradaria", "[M'agradaria, Agradaria, Degradaria, Magrejaria, Magra daria]", 1);
    assertSuggestionsTest("tenvio", "[t'envio, teniu, tensió, canvio, envio]", 1);
    assertSuggestionsTest("consultins", "[consulti'ns, consultius, consultis, consulti's, consultin]", 1);
    assertSuggestionsTest("portarvos", "[portar-vos, portar vos]", 1);
    assertSuggestionsTest("portemne", "[portem-ne, Portainé]", 1);
    assertSuggestionsTest("dacontentar", "[d'acontentar, acontentar, descontentar]", 1);
    assertSuggestionsTest("devidents", "[de vidents, d'evidents, evidents]", 1);
    assertSuggestionsTest("lacomplexat", "[la complexat, l'acomplexat, acomplexat]", 1);
    assertSuggestionsTest("dacomplexats", "[d'acomplexats, acomplexats, da complexats]", 1);
    assertSuggestionsTest("lacomplexats", "[la complexats, acomplexats]", 1);
    assertSuggestionsTest("veurehi", "[veure-hi, beure-hi, veure hi]", 1);
    assertSuggestionsTest("veureles", "[veure-les, beure-les, vorells, barrales, beurades]", 1);
    assertSuggestionsTest("lilla", "[Lilla, l'Illa, l'illa, Lille, filla]", 1);
    assertSuggestionsTest("portas", "[portàs, portes, porta, portar, poetes]", 1);
    assertSuggestionsTest("mantenir'me", "[mantenir-me]", 1);
    assertSuggestionsTest("elcap", "[el cap, alçar, alça, alçat, alcem]", 1);
    assertSuggestionsTest("almeu", "[al meu, allau, lleu, Dalmau, alceu]", 1);
    assertSuggestionsTest("delteu", "[del teu, delta, allau, Dalmau, falteu]", 1);
    assertSuggestionsTest("unshomes", "[uns homes]", 1);
    assertSuggestionsTest("pelsseus", "[pels seus, passos, paísseu, païsses]", 1);
    assertSuggestionsTest("daquesta", "[d'aquesta, aquesta, requesta, Tequesta, requeste]", 1);
    assertSuggestionsTest("daquelles", "[d'aquelles, aquelles]", 1);
    assertSuggestionsTest("lah", "[la, les, las, ah, eh]", 1);
    assertSuggestionsTest("dela", "[de la, Dala, d'ela, Dale, del]", 1);
    assertSuggestionsTest("sha", "[s'ha, xe, xa, ha, he]", 1);
    assertSuggestionsTest("Sha", "[S'ha, Ha, He, Se, Sa]", 1);
    assertSuggestionsTest("avegades", "[a vegades, vegades, amagades, apagades, avalades]", 1);
    assertSuggestionsTest("Encanvi", "[En canvi, Encabí, Encalbí, Encani, Encanti]", 1);
    assertSuggestionsTest("Nosé", "[No sé, Nos, Nus, Nosa, Pose]", 1);
    assertSuggestionsTest("air", "[aïr, dir, ahir, ai, aire]", 1);
    assertSuggestionsTest("Misiones", "[Missiones, Missionés]", 1); // millorable
    assertSuggestionsTest("quedan", "[queden, queda'n]", 1);
    assertSuggestionsTest("portan", "[porten, porta'n]", 1);
    assertSuggestionsTest("portans", "[porta'ns, portes, porten, portant, portals]", 1);
    assertSuggestionsTest("porta'nshi", "[porta'ns-hi, porta'ns hi]", 1);
    assertSuggestionsTest("porto'nz", "[porta'ns, portons, Portorož, porta'n, porte'ns]", 1);
    assertSuggestionsTest("portalhi", "[porta-l'hi, porta-hi, portal hi]", 1);
    //assertEquals("porta-l'hi", matches[0].getSuggestedReplacements().get(2));
    assertSuggestionsTest("veurels", "[veure'ls, veure's, verals, barrals, beures]", 1);
    assertSuggestionsTest("veuret", "[veure, veure't, veurem, beure, veureu]", 1);
    assertSuggestionsTest("veures", "[veure's, beures, veure, veurem, beure]", 1);
    assertSuggestionsTest("serlo", "[ser-lo, parlo, saló, sarau, sereu]", 1);
    assertSuggestionsTest("verlo", "[parlo, baró, Abelló, vareu, baló]", 1);
    assertSuggestionsTest("relajarme", "[relaxar-me, relaxara, relaxarem, relaxaria, relaxaré]", 1);
    assertSuggestionsTest("aborrirnos", "[avorrir-nos]", 1);
    assertSuggestionsTest("aburrirnos", "[avorrir-nos]", 1);
    assertSuggestionsTest("aborirnos", "[abolir-nos, avorrir-nos, borinos]", 1);
    assertSuggestionsTest("sescontaminarla", "[descontaminar-la, descontaminara, descontaminaria, descontaminarà]", 1);
    assertSuggestionsTest("anarsen", "[anar-se'n, anaren, Andersen, anassen, anessen]", 1);
    assertSuggestionsTest("danarsen", "[d'anar-se'n, denerven]", 1);
    assertSuggestionsTest("enviartela", "[enviar-te-la, enviar tela]", 1);
    assertSuggestionsTest("enviartel", "[enviar-te'l, Innviertel, enviar tel]", 1);
    assertSuggestionsTest("dirtho", "[dir-t'ho, dir-ho, Dirshu]", 1);
    assertSuggestionsTest("sentimen", "[sentiment, sentien, sentiran, sentiren, sentim en]", 1);
    assertSuggestionsTest("fesmho", "[fes-m'ho, fes-ho, fes mho]", 1);
    assertSuggestionsTest("prenten", "[pren-te'n, pretén, prenen, prenien, presten]", 1);
    assertSuggestionsTest("daconseguirlos", "[d'aconseguir-los, aconseguir-los]", 1);
    assertSuggestionsTest("laconseguirlos", "[l'aconseguir-los, aconseguir-los]", 1);
    assertSuggestionsTest("portarinhi", "[portar-hi, portar-n'hi]", 1);
    assertSuggestionsTest("Vull dirlis això.", "[dir-los, birlis, dialitz]", 1);
    assertSuggestionsTest("Portemlis el sopar més tard.", "[Portem-los, Portells]", 1);

    assertSuggestionsTest("inflacció", "[infecció, inflació, infracció, inflicció]", 1);
    assertSuggestionsTest("norueg", "[noruega, noruec, nurag]", 1);
    assertSuggestionsTest("prenense", "[prenent-se, pretensa, pretense, prenen se]", 1);
    assertSuggestionsTest("cual", "[qual]", 1);
    assertSuggestionsTest("m0entretinc", "[m'entretinc]", 1);
    assertSuggestionsTest("m9entretinc", "[m'entretinc]", 1);
    assertSuggestionsTest("lajuntamnet", "[l'ajuntament, ajuntament, rejuntament]", 1);
    assertSuggestionsTest("lajuntament", "[la juntament, l'ajuntament, ajuntament, rejuntament]", 1);
    assertSuggestionsTest("lajust", "[la just, l'ajust, ajust]", 1);
    assertSuggestionsTest("©L'Institut", "[© L'Institut]", 1);
    assertSuggestionsTest("18l'Institut", "[18 l'Institut]", 1);
    //Ela geminada
    assertSuggestionsTest("La sol•licitud", "[sol·licitud]", 1);
    assertSuggestionsTest("La sol-licitud", "[sol·licitud]", 1);
    assertSuggestionsTest("La sol⋅licitud", "[sol·licitud]", 1);
    assertSuggestionsTest("La sol∙licitud", "[sol·licitud]", 1);
    assertSuggestionsTest("un estat sindical-laborista", "", 0);
    assertSuggestionsTest("en un estat sindical.La classe obrera", "", 0);
    assertSuggestionsTest("al-Ladjdjun", "[Ladijin, Ladon, Ladson, Ladytron, Langdon]", 1);
    // "ela geminada" error + another spelling error
    assertSuggestionsTest("La sol•licitut", "[sol·licitud, sol·licitat, sol·licito]", 1);
    assertSuggestionsTest("Il•lustran", "[Il·lustren]", 1);
    assertSuggestionsTest("bél.lica", "[bèl·lica, vèlica]", 1);
    assertSuggestionsTest("mercar", "[marcar, marcer, mercer, mercat, marca]", 1);
    //majúscules
    assertSuggestionsTest("De PH 4", "[pH, PP, H, PA, AH]", 1);
    assertSuggestionsTest("De l'any 156 Ac a l'any 2000.", "[aC, A, Al, Ací, Ah]", 1);
    //split words
    assertSuggestionsTest("unaa juda", "[Una ajuda]", 1);
    assertSuggestionsTest("elsi nteressos", "[Els interessos]", 1);
    assertSuggestionsTest("el sinteressos", "[interessos, sintèresis]", 1);
    assertSuggestionsTest("ell ustre", "[el lustre, ell ostra, ell nostra, ell nostre, ell mostra]", 1);
    assertSuggestionsTest("unah ora", "[Una hora, Un ah ora, Una ora, Unes ora, Ones ora]", 1);
    assertSuggestionsTest("benv inguts", "[Ben vinguts, Benvinguts]", 1);
    assertSuggestionsTest("benam at", "[Bena mat, Benamat]", 1);
    assertSuggestionsTest("estimade s", "[Estimada]", 1); //millorable
    assertSuggestionsTest("estimad es", "[Estimar, Estimat, Estima, Estimada, Estimem]", 1); // millorable
    assertSuggestionsTest("co nstel·lació", "[Constel·lació]", 1);
    assertSuggestionsTest("a sotaveu", "[sota veu, botàveu, cotàveu, dotàveu, mutàveu]", 1);
    assertSuggestionsTest("ambun", "[Ambon, embon]", 1); // millorable
    assertSuggestionsTest("directamente", "[directament]", 1);
    //diacritics
    assertSuggestionsTest("literaria", "[literària, l'iteraria]", 1);
    // different speller dictionaries Cat/Val
    //assertSuggestionsTest("ingeniaria", "", 1);
    //assertEquals(1, matches.length);
    assertSuggestionsTest("l'unic", "[únic]", 1);
    assertSuggestionsTest("\uD83E\uDDE1\uD83E\uDDE1\uD83E\uDDE1l'unic", "[únic]", 1);
    assertSuggestionsTest("🧡 Bacances", "[Vacances, Balances, Recances, Barcasses, Balancés]", 1);
    assertSuggestionsTest("- Bacances", "[Vacances, Balances, Recances, Barcasses, Balancés]", 1);
    //Sol Picó (🐌+🐚)
    assertSuggestionsTest("Sol Picó (\uD83D\uDC0C+\uD83D\uDC1A)", "", 0);
    assertSuggestionsTest("rà dio", "[Ràdio]", 1);
    assertSuggestionsTest("GranElefant", "[Gran Elefant]", 1);
    //don't split prefixes
    assertSuggestionsTest("multiindisciplina", "[]", 1);
    //Casing
    assertSuggestionsTest("SOL.LICITUD", "[SOL·LICITUD]", 1);
    assertSuggestionsTest("PROBATURA", "[PROVATURA]", 1);
    //special chars
    assertSuggestionsTest("33° 5′ 40″ N i 32° 59′ 0″ E.", "", 0);
    assertSuggestionsTest("33°5′40″N i 32°59′0″E.", "", 0);
    assertSuggestionsTest("Fa 5·10-³ metres.", "", 0);
    // mentions, hashtags, domain names
    assertSuggestionsTest("Parlem del #temagran amb @algugros en algunacosa.cat.", "", 0);
    assertSuggestionsTest("En el domini .org hi ha fitxers d'extensió .txt.", "", 0);
    assertSuggestionsTest("En el domini .live hi ha fitxers d'extensió .7z.", "", 0);
    assertSuggestionsTest("En1993", "[En 1993]", 1);
    assertSuggestionsTest("✅Compto amb el títol", "", 0);
    //assertEquals("✅ Compto", matches[0].getSuggestedReplacements().get(0));
    assertSuggestionsTest("✅Conpto amb el títol", "[Compto]", 1);
    assertSuggestionsTest("·Compto amb el títol", "[· Compto]", 1);
    assertSuggestionsTest("105.3FM", "[105.3 FM]", 1);
    //invisible characters at start
    //assertSuggestionsTest("\u0003consagrada al turisme", "", 1);
    //assertEquals(1, matches.length);
    //assertEquals("[Consagrada]", matches[0].getSuggestedReplacements().toString());
    //assertSuggestionsTest("Volen \u0018Modificar la situació.", "", 1);
    //assertEquals(1, matches.length);
    //assertEquals("[modificar]", matches[0].getSuggestedReplacements().toString());
    // camel case
    assertSuggestionsTest("polÃtiques", "[polítiques, plàtiques, polàbiques]", 1);
    assertSuggestionsTest("SegleXXI", "[Segle XXI]", 1);
    assertSuggestionsTest("segleXIX", "[segle XIX]", 1);
    assertSuggestionsTest("PolíticaInternacionalEuropea", "[Política Internacional Europea]", 1);
    // global spelling
    assertSuggestionsTest("FT", "", 0);
    // combining characters
    assertSuggestionsTest("dema\u0300", "", 0);
    assertSuggestionsTest("demanàren", "[demanaren, demanaran]", 1);
    assertSuggestionsTest("demana\u0300ren", "[demanaren, demanaran]", 1);

    // avoid capitalized replacement
    assertSuggestionsTest("Em va demanar que fés la feina", "[fes, les, és, es, més]", 1);
    assertSuggestionsTest("Ha arribat la caballería", "[cavalleria, cavallaria]", 1);

    // do not suggest forms of "sentar, enterar".
    assertSuggestionsTest("sentences", "[sentències, sentencies, sentenciés, senten ces]", 1);
    assertSuggestionsTest("autonoma", "[autònoma]", 1);
    assertSuggestionsTest("inhalàmbrica", "[sense fils, sense fil, sense cables, autònom]", 1);
    assertSuggestionsTest("inhal·làmbricament", "[sense fils, sense fil, sense cables, autònom]", 1);
    assertSuggestionsTest("innal·làmbricamente", "[sense fils, sense fil, sense cables, autònom]", 1);
    assertSuggestionsTest("empots", "[em pots, embuts, espots, empats, ampits]", 1);
    assertSuggestionsTest("enspodeu", "[ens podeu]", 1);
    //No: A gustin
    assertSuggestionsTest("Agustin", "[Agostin]", 1); // millorable
    // hashtags, url, email
    assertSuggestionsTest("(#sensepastanagues)", "", 0);
    assertSuggestionsTest("C#, F#", "", 0);

    AnalyzedTokenReadings[] atrsArray = new AnalyzedTokenReadings[2];
    AnalyzedTokenReadings atrs0 = new AnalyzedTokenReadings(new AnalyzedToken("", "SENT_START", ""));
    AnalyzedTokenReadings atrs1 = new AnalyzedTokenReadings(new AnalyzedToken("Yuval Noha Hariri", null, null));
    atrsArray[0] = atrs0;
    atrsArray[1] = atrs1;
    AnalyzedSentence sentence = new AnalyzedSentence(atrsArray);
    RuleMatch[] matches = rule.match(sentence);
    assertEquals(1, matches.length);
    assertEquals("Yuval Noah Harari", matches[0].getSuggestedReplacements().get(0));
  }
}
