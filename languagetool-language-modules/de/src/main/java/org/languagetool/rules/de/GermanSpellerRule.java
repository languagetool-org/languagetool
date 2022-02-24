/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import de.danielnaber.jwordsplitter.GermanWordSplitter;
import de.danielnaber.jwordsplitter.InputTooLongException;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.language.German;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.SuggestedReplacement;
import org.languagetool.rules.ngrams.Probability;
import org.languagetool.rules.patterns.StringMatcher;
import org.languagetool.rules.spelling.CommonFileTypes;
import org.languagetool.rules.spelling.hunspell.CompoundAwareHunspellRule;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;
import org.languagetool.tools.StringTools;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.languagetool.rules.SuggestedReplacement.topMatch;
import static org.languagetool.tools.StringTools.startsWithUppercase;
import static org.languagetool.tools.StringTools.uppercaseFirstChar;

public class GermanSpellerRule extends CompoundAwareHunspellRule {

  public static final String RULE_ID = "GERMAN_SPELLER_RULE";

  private static final int MAX_EDIT_DISTANCE = 2;

  private static final String adjSuffix = "(basiert|konform|widrig|fähig|haltig|bedingt|gerecht|würdig|relevant|" +
    "übergreifend|tauglich|artig|bezogen|orientiert|berechtigt|fremd|liebend|bildend|hemmend|abhängig|" +
    "förmig|mäßig|pflichtig|ähnlich|spezifisch|technisch|typisch|frei|arm|freundlicher|gemäß)";
  private static final Pattern missingAdjPattern =
    Pattern.compile("[a-zöäüß]{3,25}" + adjSuffix + "(er|es|en|em|e)?");

  private final static Set<String> lcDoNotSuggestWords = new HashSet<>(Arrays.asList(
    // some of these are taken fom hunspell's dictionary where non-suggested words use tag "/n":
    "verjuden", "verjudet", "verjudeter", "verjudetes", "verjudeter", "verjudeten", "verjudetem",
    "entjuden", "entjudet", "entjudete", "entjudetes", "entjudeter", "entjudeten", "entjudetem",
    "auschwitzmythos",
    "judensippe", "judensippen",
    "judensippschaft", "judensippschaften",
    "nigger", "niggern", "niggers",
    "neger", "negern", "negers",
    "negerin", "negerinnen",
    "rassejude", "rassejuden", "rassejüdin", "rassejüdinnen",
    "möse", "mösen",
    "judenfrei", "judenfreie", "judenfreier", "judenfreies", "judenfreien", "judenfreiem",
    "judenrein", "judenreine", "judenreiner", "judenreines", "judenreinen", "judenreinem"
  ));
  
  // some exceptions for changes to the spelling in 2017 - just a workaround so we don't have to touch the binary dict:
  private static final Pattern PREVENT_SUGGESTION = Pattern.compile(
          ".*(Majonäse|Bravur|Anschovis|Belkanto|Campagne|Frotté|Grisli|Jockei|Joga|Kalvinismus|Kanossa|Kargo|Ketschup|" +
          "Kollier|Kommunikee|Masurka|Negligee|Nessessär|Poulard|Varietee|Wandalismus|kalvinist).*");
  
  private static final int MAX_TOKEN_LENGTH = 200;

  private final Set<String> wordsToBeIgnoredInCompounds = new HashSet<>();
  private final Set<String> wordStartsToBeProhibited    = new HashSet<>();
  private final Set<String> wordEndingsToBeProhibited   = new HashSet<>();
  private static final Map<StringMatcher, Function<String,List<String>>> ADDITIONAL_SUGGESTIONS = new HashMap<>();
  static {
    put("lieder", w -> Arrays.asList("leider", "Lieder"));
    put("frägst", "fragst");
    put("Impflicht", "Impfpflicht");
    put("Wandererin", "Wanderin");
    put("daß", "dass");
    put("eien", "eine");
    put("wiederrum", "wiederum");
    put("ne", "eine");
    put("ner", "einer");
    put("Frauenhofer", "Fraunhofer");
    put("Vieleicht", "Vielleicht");
    put("inbetracht", "in Betracht");
    put("überwhatsapp", "über WhatsApp");
    put("überzoom", "über Zoom");
    put("überweißt", "überweist");
    put("übergoogle", "über Google");
    put("einlogen", "einloggen");
    put("Kruks", "Krux");
    put("Filterbubble", "Filterblase");
    put("Filterbubbles", "Filterblasen");
    putRepl("Analgen.*", "Analgen", "Anlagen");
    putRepl("wiedersteh(en|st|t)", "wieder", "wider");
    putRepl("wiederstan(d|den|dest)", "wieder", "wider");
    putRepl("wiedersprech(e|t|en)?", "wieder", "wider");
    putRepl("wiedersprich(st|t)?", "wieder", "wider");
    putRepl("wiedersprach(st|t|en)?", "wieder", "wider");
    putRepl("wiederruf(e|st|t|en)?", "wieder", "wider");
    putRepl("wiederrief(st|t|en)?", "wieder", "wider");
    putRepl("wiederleg(e|st|t|en|te|ten)?", "wieder", "wider");
    putRepl("wiederhall(e|st|t|en|te|ten)?", "wieder", "wider");
    putRepl("wiedersetz(e|t|en|te|ten)?", "wieder", "wider");
    putRepl("wiederstreb(e|st|t|en|te|ten)?", "wieder", "wider");
    put("gesynct", "synchronisiert");
    put("gesynced", "synchronisiert");
    put("gesyncht", "synchronisiert");
    put("gesyngt", "synchronisiert");
    put("synce", "synchronisiere");
    put("synche", "synchronisiere");
    put("syncen", "synchronisieren");
    put("synchen", "synchronisieren");
    put("wiederspiegelten", "widerspiegelten");
    put("wiedererwarten", "wider Erwarten");
    put("widerholen", "wiederholen");
    put("wiederhohlen", "wiederholen");
    put("herrunterladen", "herunterladen");
    put("dastellen", "darstellen");
    put("zuviel", "zu viel");
    put("abgekatertes", "abgekartetes");
    put("wiederspiegelt", "widerspiegelt");
    put("Komplexheit", "Komplexität");
    put("unterschiedet", "unterscheidet");
    put("einzigst", "einzig");
    put("Einzigst", "Einzig");
    put("geschumpfen", "geschimpft");
    put("Geschumpfen", "Geschimpft");
    put("Oke", "Okay");
    put("Mü", "My");
    put("abschiednehmen", "Abschied nehmen");
    put("wars", w -> Arrays.asList("war's", "war es", "warst"));
    put("[aA]wa", w -> Arrays.asList("AWA", "ach was", "aber"));
    put("[aA]lsallerersten?s", w -> Arrays.asList(w.replaceFirst("lsallerersten?s", "ls allererstes"), w.replaceFirst("lsallerersten?s", "ls Allererstes")));
    putRepl("(an|auf|ein|zu)gehangen(e[mnrs]?)?$", "hangen", "hängt");
    putRepl("[oO]key", "ey$", "ay");
    put("packet", "Paket");
    put("Thanks", "Danke");
    put("Ghanesen?", "Ghanaer");
    put("Thumberg", "Thunberg");
    put("Allalei", "Allerlei");
    put("geupdate[dt]$", "upgedatet");
    //put("gefaked", "gefakt");  -- don't suggest
    put("[pP]roblemhaft(e[nmrs]?)?", w -> Arrays.asList(w.replaceFirst("haft", "behaftet"), w.replaceFirst("haft", "atisch")));
    put("rosane[mnrs]?$", w -> Arrays.asList("rosa", w.replaceFirst("^rosan", "rosafarben")));
    put("Erbung", w -> Arrays.asList("Vererbung", "Erbschaft"));
    put("Energiesparung", w -> Arrays.asList("Energieeinsparung", "Energieersparnis"));
    put("Abbrechung", "Abbruch");
    put("Abbrechungen", w -> Arrays.asList("Abbrüche", "Abbrüchen"));
    put("Urteilung", w -> Arrays.asList("Urteil", "Verurteilung"));
    put("allmöglichen?", w -> Arrays.asList("alle möglichen", "alle mögliche"));
    put("Krankenhausen", w -> Arrays.asList("Krankenhäusern", "Krankenhäuser"));
    put("vorr?auss?etzlich", w -> Arrays.asList("voraussichtlich", "vorausgesetzt"));
    put("nichtmals", w -> Arrays.asList("nicht mal", "nicht einmal"));
    put("eingepeilt", "angepeilt");
    put("gekukt", "geguckt");
    put("nem", "einem");
    put("nen", "einen");
    put("geb", "gebe");
    put("überhaut", "überhaupt");
    put("nacher", "nachher");
    put("jeztz", "jetzt");
    put("les", "lese");
    put("wr", "wir");
    put("bezweifel", "bezweifle");
    put("verzweifel", "verzweifle");
    put("zweifel", "zweifle");
    put("[wW]ah?rscheindlichkeit", "Wahrscheinlichkeit");
    put("Hijab", "Hidschāb");
    put("[lL]eerequiment", "Leerequipment");
    put("unauslässlich", w -> Arrays.asList("unerlässlich", "unablässig", "unauslöschlich"));
    put("Registration", "Registrierung");
    put("Registrationen", "Registrierungen");
    put("Spinnenweben", "Spinnweben");
    putRepl("[Ww]ar ne", "ne", "eine");
    putRepl("[Ää]nliche[rnms]?", "nlich", "hnlich");
    putRepl("[Gg]arnix", "nix", "nichts");
    putRepl("[Ww]i", "i", "ie");
    putRepl("[uU]nauslässlich(e[mnrs]?)?", "aus", "er");
    putRepl("[vV]erewiglicht(e[mnrs]?)?", "lich", "");
    putRepl("[zZ]eritifiert(e[mnrs]?)?", "eritifiert", "ertifiziert");
    putRepl("gerähten?", "geräht", "Gerät");
    putRepl("leptops?", "lep", "Lap");
    putRepl("[pP]ie?rsings?", "[pP]ie?rsing", "Piercing");
    putRepl("for?melar(en?)?", "for?me", "Formu");
    putRepl("näste[mnrs]?$", "^näs", "nächs");
    putRepl("Erdogans?$", "^Erdogan", "Erdoğan");
    put("Germanistiker[ns]", "Germanisten");
    putRepl("Germanistikerin(nen)?", "Germanistiker", "Germanist");
    putRepl("[iI]ns?z[ie]nie?rung(en)?", "[iI]ns?z[ie]nie?", "Inszenie");
    putRepl("[eE]rhöherung(en)?", "[eE]rhöherung", "Erhöhung");
    putRepl("[vV]erspäterung(en)?", "später", "spät");
    putRepl("[vV]orallendingen", "orallendingen", "or allen Dingen");
    putRepl("[aA]ufjede[nm]fall", "jede[nm]fall$", " jeden Fall");
    putRepl("[aA]us[vf]ersehen[dt]lich", "[vf]ersehen[dt]lich", " Versehen");
    putRepl("^funk?z[ou]nier.+", "funk?z[ou]nier", "funktionier");
    putRepl("[wW]öruber", "öru", "orü");
    putRepl("[lL]einensamens?", "[lL]einen", "Lein");
    putRepl("Feinleiner[ns]?", "Feinlei", "Fineli");
    putRepl("[hH]eilei[td]s?", "[hH]eilei[td]", "Highlight");
    putRepl("Oldheimer[ns]?", "he", "t");
    putRepl("[tT]räner[ns]?", "[tT]rä", "Trai");
    putRepl("[tT]eimings?", "[tT]e", "T");
    putRepl("unternehmensl[uü]stig(e[mnrs]?)?", "mensl[uü]st", "mungslust"); // "unternehmenslüstig" -> "unternehmungslustig"
    putRepl("proff?ess?ional(e[mnrs]?)?", "ff?ess?ional", "fessionell");
    putRepl("zuverlässlich(e[mnrs]?)?", "lich", "ig");
    putRepl("fluoreszenzierend(e[mnrs]?)?", "zen", "");
    putRepl("revalierend(e[mnrs]?)?", "^reval", "rivalis");
    putRepl("verhäuft(e[mnrs]?)?", "^ver", "ge");
    putRepl("stürmig(e[mnrs]?)?", "mig", "misch");
    putRepl("größeste[mnrs]?", "ßes", "ß");
    putRepl("n[aä]heste[mnrs]?", "n[aä]he", "näch");
    putRepl("gesundlich(e[mnrs]?)?", "lich", "heitlich");
    putRepl("eckel(e|t(en?)?|st)?", "^eck", "ek");
    putRepl("unhervorgesehen(e[mnrs]?)?", "hervor", "vorher");
    putRepl("entt?euscht(e[mnrs]?)?", "entt?eusch", "enttäusch");
    putRepl("Phählen?", "^Ph", "Pf");
    putRepl("Kattermesser[ns]?", "Ka", "Cu");
    putRepl("gehe?rr?t(e[mnrs]?)?", "he?rr?", "ehr"); // "geherte" -> "geehrte"
    putRepl("gehrter?", "^ge", "gee");
    putRepl("[nN]amenhaft(e[mnrs]?)?", "amen", "am");
    putRepl("hom(o?e|ö)ophatisch(e[mnrs]?)?", "hom(o?e|ö)ophat", "homöopath");
    putRepl("Geschwindlichkeit(en)?", "lich", "ig");
    putRepl("Jänners?", "Jänner", "Januar");
    putRepl("[äÄ]hlich(e[mnrs]?)?", "lich", "nlich");
    putRepl("entf[ai]ngen?", "ent", "emp");
    putRepl("entf[äi]ngs?t", "ent", "emp");
    putRepl("[Bb]ehilfreich(e[rnms]?)", "reich", "lich");
    putRepl("[Bb]zgl", "zgl", "zgl.");
    putRepl("kaltnass(e[rnms]?)", "kaltnass", "nasskalt");
    putRepl("Kaltnass(e[rnms]?)", "Kaltnass", "Nasskalt");
    put("check", "checke");
    put("Rückrad", "Rückgrat");
    put("ala", "à la");
    put("Ala", "À la");
    put("Reinfolge", "Reihenfolge");
    put("Schloß", "Schloss");
    put("Investion", "Investition");
    put("Beleidung", "Beleidigung");
    put("Bole", "Bowle");
    put("letzens", "letztens");
    put("Pakur", w -> Arrays.asList("Parcours", "Parkuhr"));
    put("Erstsemesterin", w -> Arrays.asList("Erstsemester", "Erstsemesters", "Erstsemesterstudentin"));
    put("Erstsemesterinnen", w -> Arrays.asList("Erstsemesterstudentinnen", "Erstsemester", "Erstsemestern"));
    put("kreativlos(e[nmrs]?)?", w -> Arrays.asList(w.replaceFirst("kreativ", "fantasie"), w.replaceFirst("kreativ", "einfalls"), w.replaceFirst("kreativlos", "unkreativ"), w.replaceFirst("kreativlos", "uninspiriert")));
    put("Kreativlosigkeit", "Unkreativität");
    put("hinund?her", "hin und her");
    put("[lL]ymph?trie?nasche", "Lymphdrainage");
    put("Interdeterminismus", "Indeterminismus");
    put("elektrität", "Elektrizität");
    put("ausgeboten", "ausgebootet");
    put("nocheinmall", "noch einmal");
    put("aüßerst", "äußerst");
    put("Grrösse", "Größe");
    put("misverständniss", "Missverständnis");
    put("warheit", "Wahrheit");
    put("[pP]okemon", "Pokémon");
    put("kreigt", "kriegt");
    put("Fritöse", "Fritteuse");
    put("unerkennlich", "unkenntlich");
    put("rückg[äe]nglich", "rückgängig");
    put("em?men[sz]", "immens");
    put("verhing", "verhängte");
    put("verhingen", "verhängten");
    put("fangte", "fing");
    put("fangten", "fingen");
    put("schlie[sß]te", "schloss");
    put("schlie[sß]ten", "schlossen");
    put("past", "passt");
    put("eingetragt", "eingetragen");
    put("getrunkt", "getrunken");
    put("veräht", "verrät");
    put("helfte", "half");
    put("helften", "halfen");
    put("lad", "lade");
    put("befehlte", "befahl");
    put("befehlten", "befahlen");
    put("angelügt", "angelogen");
    put("lügte", "log");
    put("lügten", "logen");
    put("bratete", "briet");
    put("brateten", "brieten");
    put("gefahl", "gefiel");
    put("Komplexibilität", "Komplexität");
    put("abbonement", "Abonnement");
    put("zugegebenerweise", "zugegebenermaßen");
    put("perse", "per se");
    put("Schwitch", "Switch");
    put("[aA]nwesenzeiten", "Anwesenheitszeiten");
    put("[gG]eizigkeit", "Geiz");
    put("[fF]leißigkeit", "Fleiß");
    put("[bB]equemheit", "Bequemlichkeit");
    put("[mM]issionarie?sie?rung", "Missionierung");
    put("[sS]chee?selonge?", "Chaiselongue");
    put("Re[kc]amiere", "Récamière");
    put("Singel", "Single");
    put("legen[td]lich", "lediglich");
    put("ein[ua]ndhalb", "eineinhalb");
    put("[mM]illion(en)?mal", w -> singletonList(uppercaseFirstChar(w.replaceFirst("mal", " Mal"))));
    put("Mysql", "MySQL");
    put("MWST", "MwSt");
    put("Mwst", "MwSt");
    put("Opelarena", "Opel Arena");
    put("Toll-Collect", "Toll Collect");
    put("[pP][qQ]-Formel", "p-q-Formel");
    put("desweitere?[nm]", "des Weiteren");
    put("handzuhaben", "zu handhaben");
    put("nachvollzuziehe?n", "nachzuvollziehen");
    put("Porto?folien", "Portfolios");
    put("[sS]chwie?ri?chkeiten", "Schwierigkeiten");
    put("[üÜ]bergrifflichkeiten", "Übergriffigkeiten");
    put("[aA]r?th?rie?th?is", "Arthritis");
    put("zugesand", "zugesandt");
    put("weibt", "weißt");
    put("fress", "friss");
    put("Mamma", "Mama");
    put("Präse", "Präsentation");
    put("Präsen", "Präsentationen");
    put("Orga", "Organisation");
    put("Orgas", "Organisationen");
    put("Reorga", "Reorganisation");
    put("Reorgas", "Reorganisationen");
    put("instande?zusetzen", "instand zu setzen");
    put("Lia(si|is)onen", "Liaisons");
    put("[cC]asemana?ge?ment", "Case Management");
    put("[aA]nn?[ou]ll?ie?rung", "Annullierung");
    put("[sS]charm", "Charme");
    put("[zZ]auberlich(e[mnrs]?)?", w -> Arrays.asList(w.replaceFirst("lich", "isch"), w.replaceFirst("lich", "haft")));
    put("[eE]rledung", "Erledigung");
    put("erledigigung", "Erledigung");
    put("woltest", "wolltest");
    put("[iI]ntranzparentheit", "Intransparenz");
    put("dunkellilane[mnrs]?", "dunkellila");
    put("helllilane[mnrs]?", "helllila");
    put("Behauptungsthese", "Behauptung");
    put("genzut", "genutzt");
    put("[eEäÄ]klerung", "Erklärung");
    put("[wW]eh?wechen", "Wehwehchen");
    put("nocheinmals", "noch einmal");
    put("unverantwortungs?los(e[mnrs]?)?", w -> Arrays.asList(w.replaceFirst("unverantwortungs?", "verantwortungs"), w.replaceFirst("ungs?los", "lich")));
    putRepl("[eE]rhaltbar(e[mnrs]?)?", "haltbar", "hältlich");
    putRepl("[aA]ufkeinenfall?", "keinenfall?", " keinen Fall");
    putRepl("[Dd]rumrum", "rum$", "herum");
    putRepl("([uU]n)?proff?esionn?ell?(e[mnrs]?)?", "proff?esionn?ell?", "professionell");
    putRepl("[kK]inderlich(e[mnrs]?)?", "inder", "ind");
    putRepl("[wW]iedersprichs?t", "ieder", "ider");
    putRepl("[wW]hite-?[Ll]abels", "[wW]hite-?[Ll]abel", "White Label");
    putRepl("[wW]iederstand", "ieder", "ider");
    putRepl("[kK]önntes", "es$", "est");
    putRepl("[aA]ssess?oare?s?", "[aA]ssess?oare?", "Accessoire");
    putRepl("indifiziert(e[mnrs]?)?", "ind", "ident");
    putRepl("dreite[mnrs]?", "dreit", "dritt");
    putRepl("verblüte[mnrs]?", "blü", "blüh");
    putRepl("Einzigste[mnrs]?", "zigst", "zig");
    putRepl("Invests?", "Invest", "Investment");
    putRepl("(aller)?einzie?gste[mnrs]?", "(aller)?einzie?gst", "einzig");
    putRepl("[iI]nterkurell(e[nmrs]?)?", "ku", "kultu");
    putRepl("[iI]ntersannt(e[mnrs]?)?", "sannt", "essant");
    putRepl("ubera(g|sch)end(e[nmrs]?)?", "uber", "überr");
    putRepl("[Hh]ello", "ello", "allo");
    putRepl("[Gg]etagged", "gged", "ggt");
    putRepl("[wW]olt$", "lt", "llt");
    putRepl("[zZ]uende", "ue", "u E");
    putRepl("[iI]nbälde", "nb", "n B");
    putRepl("[lL]etztenendes", "ene", "en E");
    putRepl("[nN]achwievor", "wievor", " wie vor");
    putRepl("[zZ]umbeispiel", "beispiel", " Beispiel");
    putRepl("[gG]ottseidank", "[gG]ottseidank", "Gott sei Dank");
    putRepl("[gG]rundauf", "[gG]rundauf", "Grund auf");
    putRepl("[aA]nsichtnach", "[aA]nsicht", "Ansicht ");
    putRepl("[uU]n[sz]war", "[sz]war", "d zwar");
    putRepl("[wW]aschte(s?t)?", "aschte", "usch");
    putRepl("[wW]aschten", "ascht", "usch");
    putRepl("Probiren?", "ir", "ier");
    putRepl("[gG]esetztreu(e[nmrs]?)?", "tz", "tzes");
    putRepl("[wW]ikich(e[nmrs]?)?", "k", "rkl");
    putRepl("[uU]naufbesichtigt(e[nmrs]?)?", "aufbe", "beauf");
    putRepl("[nN]utzvoll(e[nmrs]?)?", "utzvoll", "ützlich");
    putRepl("Lezte[mnrs]?", "Lez", "Letz");
    putRepl("Letze[mnrs]?", "Letz", "Letzt");
    putRepl("[nN]i[vw]os?", "[nN]i[vw]o", "Niveau");
    putRepl("[dD]illetant(en)?", "[dD]ille", "Dilet");
    putRepl("Frauenhofer-(Institut|Gesellschaft)", "Frauen", "Fraun");
    putRepl("Add-?Ons?", "Add-?On", "Add-on");
    putRepl("Addons?", "on", "-on");
    putRepl("Internetkaffees?", "kaffee", "café");
    putRepl("[gG]ehorsamkeitsverweigerung(en)?", "[gG]ehorsamkeit", "Gehorsam");
    putRepl("[wW]ochende[ns]?", "[wW]ochend", "Wochenend");
    putRepl("[kK]ongratulier(en?|t(en?)?|st)", "[kK]on", "");
    putRepl("[wWkKdD]an$", "n$", "nn");
    putRepl("geh?neh?m[ie]gung(en)?", "geh?neh?m[ie]gung", "Genehmigung");
    putRepl("Korrigierung(en)?", "igierung", "ektur");
    putRepl("[kK]orregierung(en)?", "[kK]orregierung", "Korrektur");
    putRepl("[kK]orrie?girung(en)?", "[kK]orrie?girung", "Korrektur");
    putRepl("[nN]ocheimal", "eimal", " einmal");
    putRepl("[aA]benzu", "enzu", " und zu");
    putRepl("[kK]onflikation(en)?", "[kK]onfli", "Kompli");
    putRepl("[mM]itanader", "ana", "einan");
    putRepl("[mM]itenand", "enand", "einander");
    putRepl("Gelangenheitsbestätigung(en)?", "heit", "");
    putRepl("[jJ]edwillige[mnrs]?", "willig", "wed");
    putRepl("[qQ]ualitäts?bewußt(e[mnrs]?)?", "ts?bewußt", "tsbewusst");
    putRepl("[vV]oraussichtig(e[nmrs]?)?", "sichtig", "sichtlich");
    putRepl("[gG]leichrechtig(e[nmrs]?)?", "rechtig", "berechtigt");
    putRepl("[uU]nnützlich(e[nmrs]?)?", "nützlich", "nütz");
    putRepl("[uU]nzerbrechbar(e[nmrs]?)?", "bar", "lich");
    putRepl("kolegen?", "ko", "Kol");
    putRepl("tableten?", "tablet", "Tablett");
    putRepl("verswinde(n|s?t)", "^vers", "versch");
    putRepl("unverantwortungsvoll(e[nmrs]?)?", "unverantwortungsvoll", "verantwortungslos");
    putRepl("[gG]erechtlichkeit", "[gG]erechtlich", "Gerechtig");
    putRepl("[zZ]uverlässlichkeit", "lich", "ig");
    putRepl("[uU]nverzeilig(e[mnrs]?)?", "zeilig", "zeihlich");
    putRepl("[zZ]uk(ue?|ü)nftlich(e[mnrs]?)?", "uk(ue?|ü)nftlich", "ukünftig");
    putRepl("[rR]eligiösisch(e[nmrs]?)?", "isch", "");
    putRepl("[fF]olklorisch(e[nmrs]?)?", "isch", "istisch");
    putRepl("[eE]infühlsvoll(e[nmrs]?)?", "voll", "am");
    putRepl("Unstimmlichkeit(en)?", "lich", "ig");
    putRepl("Strebergartens?", "Stre", "Schre");
    putRepl("[hH]ähern(e[mnrs]?)?", "ähern", "ären");
    putRepl("todesbedroh(end|lich)(e[nmrs]?)?", "todes", "lebens");
    putRepl("^[uU]nabsichtig(e[nmrs]?)?", "ig", "lich");
    putRepl("[aA]ntisemitistisch(e[mnrs]?)?", "tist", "t");
    putRepl("[uU]nvorsehbar(e[mnrs]?)?", "vor", "vorher");
    putRepl("([eE]r|[bB]e|unter|[aA]uf)?hälst", "hälst", "hältst");
    put("[wW]ohlfühlseins?", w -> Arrays.asList("Wellness", w.replaceFirst("[wW]ohlfühlsein", "Wohlbefinden"), w.replaceFirst("[wW]ohlfühlsein", "Wohlfühlen")));
    putRepl("[sS]chmett?e?rling(s|en?)?", "[sS]chmett?e?rling", "Schmetterling");
    putRepl("^[eE]inlamie?nie?r(st|en?|(t(e[nmrs]?)?))?", "^einlamie?nie?r", "laminier");
    putRepl("[bB]ravurös(e[nrms]?)?", "vur", "vour");
    putRepl("[aA]ss?ecoires?", "[aA]ss?ec", "Access");
    putRepl("[aA]ufwechse?lungsreich(er|st)?(e[nmrs]?)?", "ufwechse?lung", "bwechslung");
    putRepl("[iI]nordnung", "ordnung", " Ordnung");
    putRepl("[iI]mmoment", "moment", " Moment");
    putRepl("[hH]euteabend", "abend", " Abend");
    putRepl("[wW]ienerschnitzel[ns]?", "[wW]ieners", "Wiener S");
    putRepl("[sS]chwarzwälderkirschtorten?", "[sS]chwarzwälderk", "Schwarzwälder K");
    putRepl("[kK]oxial(e[nmrs]?)?", "x", "ax");
    putRepl("([üÜ]ber|[uU]unter)?[dD]urs?chnitt?lich(e[nmrs]?)?", "s?chnitt?", "chschnitt");
    putRepl("[dD]urs?chnitts?", "s?chnitt", "chschnitt");
    putRepl("[sS]triktlich(e[mnrs]?)?", "lich", "");
    putRepl("[hH]öchstwahrlich(e[mnrs]?)?", "wahr", "wahrschein");
    putRepl("[oO]rganisativ(e[nmrs]?)?", "tiv", "torisch");
    putRepl("[kK]ontaktfreundlich(e[nmrs]?)?", "ndlich", "dig");
    putRepl("Helfer?s-Helfer[ns]?", "Helfer?s-H", "Helfersh");
    putRepl("[iI]ntell?igentsbestien?", "[iI]ntell?igents", "Intelligenz");
    putRepl("[aA]vantgardisch(e[mnrs]?)?", "gard", "gardist");
    putRepl("[gG]ewohnheitsbedürftig(e[mnrs]?)?", "wohnheit", "wöhnung");
    putRepl("[eE]infühlungsvoll(e[mnrs]?)?", "fühlungsvoll", "fühlsam");
    putRepl("[vV]erwant(e[mnrs]?)?", "want", "wandt");
    putRepl("[bB]eanstandigung(en)?", "ig", "");
    putRepl("[eE]inba(hn|nd)frei(e[mnrs]?)?", "ba(hn|nd)", "wand");
    putRepl("[äÄaAeE]rtzten?", "[äÄaAeE]rt", "Är");
    putRepl("pdf-Datei(en)?", "pdf", "PDF");
    putRepl("rumänern?", "rumäner", "Rumäne");
    putRepl("[cCKk]o?usengs?", "[cCKk]o?useng", "Cousin");
    putRepl("Influenzer(in(nen)?|[ns])?", "zer", "cer");
    putRepl("[vV]ersantdienstleister[ns]?", "[vV]ersant", "Versand");
    putRepl("[pP]atrolier(s?t|t?en?)", "atrolier", "atrouillier");
    putRepl("[pP]ropagandiert(e[mnrs]?)?", "and", "");
    putRepl("[pP]ropagandier(en|st)", "and", "");
    putRepl("[kK]app?erzität(en)?", "^[kK]app?er", "Kapa");
    putRepl("känzel(n|s?t)", "känzel", "cancel");
    put("gekänzelt", "gecancelt");
    putRepl("[üÜ]berstreitung(en)?", "[üÜ]berst", "Übersch");
    putRepl("anschliess?lich(e(mnrs)?)?", "anschliess?lich", "anschließend");
    putRepl("[rR]ethorisch(e(mnrs)?)?", "eth", "het");
    putRepl("änlich(e(mnrs)?)?", "än", "ähn");
    putRepl("spätmöglichste(mnrs)?", "spätmöglichst", "spätestmöglich");
    put("mogen", "morgen");
    put("[fF]uss?ill?ien", "Fossilien");
    put("übrings", "übrigens");
    put("[rR]evü", "Revue");
    put("eingänglich", "eingangs");
    put("geerthe", "geehrte");
    put("interrese", "Interesse");
    put("[rR]eschärschen", "Recherchen");
    put("[rR]eschärsche", "Recherche");
    put("ic", "ich");
    put("w[eä]hret", "wäret");
    put("mahte", "Mathe");
    put("letzdenendes", "letzten Endes");
    put("aufgesteht", "aufgestanden");
    put("ganichts", "gar nichts");
    put("gesich", "Gesicht");
    put("glass", "Glas");
    put("muter", "Mutter");
    put("[pP]appa", "Papa");
    put("dier", "dir");
    put("Referenz-Nr", "Referenz-Nr.");
    put("Matrikelnr.", "Matrikel-Nr.");
    put("Rekrutings?prozess", "Recruitingprozess");
    put("sumarum", "summarum");
    put("schein", "scheine");
    put("Innzahlung", w -> Arrays.asList("In Zahlung", "in Zahlung"));
    put("änderen", w -> Arrays.asList("ändern", "anderen"));
    put("wanderen", w -> Arrays.asList("wandern", "Wanderern"));
    put("Dutzen", w -> Arrays.asList("Duzen", "Dutzend"));
    put("patien", w -> Arrays.asList("Partien", "Patient"));
    put("Teammitgliederinnen", w -> Arrays.asList("Teammitgliedern", "Teammitglieder"));
    put("beidige[mnrs]?", w -> Arrays.asList(w.replaceFirst("ig", ""), w.replaceFirst("beid", "beiderseit"), "beeidigen")); //beide, beiderseitige, beeidigen
    put("Wissbegierigkeit", w -> Arrays.asList("Wissbegier", "Wissbegierde"));
    put("Nabend", "'n Abend");
    put("gie?bts", "gibt's");
    put("vs", "vs.");
    put("[kK]affeeteria", "Cafeteria");
    put("[kK]affeeterien", "Cafeterien");
    put("berücksicht", "berücksichtigt");
    put("must", "musst");
    put("kaffe", "Kaffee");
    put("zetel", "Zettel");
    put("wie?daholung", "Wiederholung");
    put("vie?d(er|a)sehen", "wiedersehen");
    put("pr[eä]ventiert", "verhindert");
    put("pr[eä]ventieren", "verhindern");
    put("zur?verfügung", "zur Verfügung");
    put("Verwahrlosigkeit", "Verwahrlosung");
    put("[oO]r?ganisazion", "Organisation");
    put("[oO]rganisative", "Organisation");
    put("Emall?iearbeit", "Emaillearbeit");
    put("[aA]petitt", "Appetit");
    put("bezuggenommen", "Bezug genommen");
    put("mägt", "mögt");
    put("frug", "fragte");
    put("gesäht", "gesät");
    put("verennt", "verrennt");
    put("überrant", "überrannt");
    put("Gallop", "Galopp");
    put("Stop", "Stopp");
    put("Schertz", "Scherz");
    put("geschied", "geschieht");
    put("Aku", "Akku");
    put("Migrationspackt", "Migrationspakt");
    put("[zZ]ulaufror", "Zulaufrohr");
    put("[gG]ebrauchss?puhren", "Gebrauchsspuren");
    put("[pP]reisnachlassung", "Preisnachlass");
    put("[mM]edikamentation", "Medikation");
    put("[nN][ei]gliche", "Negligé");
    put("palletten?", w -> Arrays.asList(w.replaceFirst("pall", "Pal"), w.replaceFirst("pa", "Pai")));
    put("[pP]allete", "Palette");
    put("Geräuch", w -> Arrays.asList("Geräusch", "Gesträuch"));
    put("[sS]chull?igung", "Entschuldigung");
    put("Geerte", "geehrte");
    put("versichen", "versichern");
    put("hobb?ies", "Hobbys");
    put("Begierigkeiten", "Begehrlichkeiten");
    put("selblosigkeit", "Selbstlosigkeit");
    put("gestyled", "gestylt");
    put("umstimigkeiten", "Unstimmigkeiten");
    put("unann?äh?ml?ichkeiten", "Unannehmlichkeiten");
    put("unn?ann?ehmichkeiten", "Unannehmlichkeiten");
    put("übertr[äa]gte", "übertrug");
    put("übertr[äa]gten", "übertrugen");
    put("NodeJS", "Node.js");
    put("Express", "Express.js");
    put("erlas", "Erlass");
    put("schlagte", "schlug");
    put("schlagten", "schlugen");
    put("überwissen", "überwiesen");
    put("einpar", "ein paar");
    put("sreiben", "schreiben");
    put("routiene", "Routine");
    put("ect", "etc");
    put("giept", "gibt");
    put("Pann?acott?a", "Panna cotta");
    put("Fußgängerunterwegs?", "Fußgängerunterführung");
    put("angeschriehen", "angeschrien");
    put("vieviel", "wie viel");
    put("entäscht", "enttäuscht");
    put("Rämchen", "Rähmchen");
    put("Seminarbeit", "Seminararbeit");
    put("Seminarbeiten", "Seminararbeiten");
    put("[eE]ngangment", "Engagement");
    put("[lL]eichtah?tleh?t", "Leichtathlet");
    put("[pP]fane", "Pfanne");
    put("[iI]ngini?eue?r", "Ingenieur");
    put("[aA]nligen", "Anliegen");
    put("Tankungen", w -> Arrays.asList("Betankungen", "Tankvorgänge"));
    put("Ärcker", w -> Arrays.asList("Erker", "Ärger"));
    put("überlasstet", w -> Arrays.asList("überlastet", "überließt"));
    put("zeren", w -> Arrays.asList("zerren", "zehren"));
    put("Hänchen", w -> Arrays.asList("Hähnchen", "Hänschen"));
    put("[sS]itwazion", "Situation");
    put("geschriehen", "geschrien");
    put("beratete", "beriet");
    put("Hälst", "Hältst");
    put("[kK]aos", "Chaos");
    put("[pP]upatät", "Pubertät");
    put("überwendet", "überwindet");
    put("[bB]esichtung", "Besichtigung");
    put("[hH]ell?owi[eh]?n", "Halloween");
    put("geschmelt?zt", "geschmolzen");
    put("gewunschen", "gewünscht");
    put("bittete", "bat");
    put("nehm", "nimm");
    put("möchst", "möchtest");
    put("Win", "Windows");
    put("anschein[dt]", "anscheinend");
    put("Subvestitionen", "Subventionen");
    put("angeschaffen", "angeschafft");
    put("Rechtspruch", "Rechtsspruch");
    put("Second-Hand", "Secondhand");
    put("[jJ]ahundert", "Jahrhundert");
    put("Gesochse", "Gesocks");
    put("Vorraus", "Voraus");
    put("[vV]orgensweise", "Vorgehensweise");
    put("[kK]autsch", "Couch");
    put("guterletzt", "guter Letzt");
    put("Seminares", "Seminars");
    put("Mousepad", "Mauspad");
    put("Mousepads", "Mauspads");
    put("Wi[Ff]i-Router", "Wi-Fi-Router");
    putRepl("[Ll]ilane[srm]?", "ilane[srm]?", "ila");
    putRepl("[zZ]uguterletzt", "guterletzt", " guter Letzt");
    putRepl("Nootbooks?", "Noot", "Note");
    putRepl("[vV]ersendlich(e[mnrs]?)?", "send", "sehent");
    putRepl("[uU]nfäh?r(e[mnrs]?)?", "fäh?r", "fair");
    putRepl("[mM]edikatös(e[mnrs]?)?", "ka", "kamen");
    putRepl("(ein|zwei|drei|vier|fünf|sechs|sieben|acht|neun|zehn|elf|zwölf)undhalb", "und", "ein");
    putRepl("[gG]roßzüge[mnrs]?", "züg", "zügig");
    putRepl("[äÄ]rtlich(e[mnrs]?)?", "rt", "rzt");
    putRepl("[sS]chnelligkeitsfehler[ns]?", "[sS]chnell", "Flücht");
    putRepl("[sS]chweinerosane[mnrs]?", "weinerosane[mnrs]?", "weinchenrosa");
    putRepl("[aA]nstecklich(e[mnrs]?)?", "lich", "end");
    putRepl("[gG]eflechtet(e[mnrs]?)?", "flechtet", "flochten");
    putRepl("[gG]enrealistisch(e[mnrs]?)?", "re", "er");
    putRepl("überträgt(e[mnrs]?)?", "^überträgt", "übertragen");
    putRepl("[iI]nterresent(e[mnrs]?)?", "rresent", "ressant");
    putRepl("Simkartenleser[ns]?", "^Simkartenl", "SIM-Karten-L");
    putRepl("Hilfstmittel[ns]?", "^Hilfst", "Hilfs");
    putRepl("trationell(e[mnrs]?)?", "^tra", "tradi");
    putRepl("[bB]erreichs?", "^[bB]er", "Be");
    putRepl("[fF]uscher[ns]?", "^[fF]u", "Pfu");
    putRepl("[uU]nausweichbar(e[mnrs]?)?", "bar", "lich");
    putRepl("[uU]nabdinglich(e[mnrs]?)?", "lich", "bar");
    putRepl("[eE]ingänglich(e[mnrs]?)?", "lich", "ig");
    putRepl("ausgewöh?nlich(e[mnrs]?)?", "^ausgewöh?n", "außergewöhn");
    putRepl("achsial(e[mnrs]?)?", "^achs", "ax");
    putRepl("famielen?", "^famiel", "Famili");
    putRepl("miter[ns]?", "^mi", "Mie");
    putRepl("besig(t(e[mnrs]?)?|en?)", "sig", "sieg");
    putRepl("[vV]erziehr(t(e[mnrs]?)?|en?)", "ieh", "ie");
    putRepl("^[pP]iek(s?t|en?)", "iek", "ik");
    putRepl("[mM]atschscheiben?", "[mM]atschsch", "Mattsch");
    put("schafen?", w -> Arrays.asList(w.replaceFirst("sch", "schl"), w.replaceFirst("af", "arf"), w.replaceFirst("af", "aff")));
    put("zuschafen", "zu schaffen");
    putRepl("[hH]ofen?", "of", "off");
    putRepl("[sS]ommerverien?", "[sS]ommerverien?", "Sommerferien");
    putRepl("[rR]ecourcen?", "[rR]ec", "Ress");
    putRepl("[fF]amm?ill?i?[aä]risch(e[mnrs]?)?", "amm?ill?i?[aä]risch", "amiliär");
    putRepl("Sim-Karten?", "^Sim", "SIM");
    putRepl("Spax-Schrauben?", "^Spax", "SPAX");
    putRepl("[aA]leine", "l", "ll");
    putRepl("Kaput", "t", "tt");
    putRepl("[fF]estell(s?t|en?)", "est", "estst");
    putRepl("[Ee]igtl", "igtl", "igtl.");
    putRepl("(Baden-)?Würtenbergs?", "Würten", "Württem");
    putRepl("Betriebsratzimmer[ns]?", "rat", "rats");
    putRepl("Rechts?schreibungsfehler[ns]?", "Rechts?schreibungs", "Rechtschreib");
    putRepl("Open[aA]ir-Konzert(en?)?", "Open[aA]ir", "Open-Air");
    putRepl("Jugenschuhen?", "Jug", "Jung");
    putRepl("TODO-Listen?", "TODO", "To-do");
    putRepl("ausiehs?t", "aus", "auss");
    putRepl("unterbemittel(nd|t)(e[nmrs]?)?", "unterbemittel(nd|t)", "minderbemittelt");
    putRepl("[xX]te[mnrs]?", "te", "-te");
    putRepl("verheielt(e[mnrs]?)?", "heiel", "heil");
    putRepl("[rR]evolutionie?sier(s?t|en?)", "ie?s", "");
    putRepl("Kohleaustiegs?", "aus", "auss");
    putRepl("[jJ]urististisch(e[mnrs]?)?", "istist", "ist");
    putRepl("gehäckelt(e[nmrs]?)?", "ck", "k");
    putRepl("deutsprachig(e[nmrs]?)?", "deut", "deutsch");
    putRepl("angesehend(st)?e[nmrs]?", "end", "en");
    putRepl("[iI]slamophobisch(e[mnrs]?)?", "isch", "");
    putRepl("[vV]erharkt(e[mnrs]?)?", "ar", "a");
    putRepl("[dD]esöfterer?[nm]", "öfterer?[nm]", " Öfteren");
    putRepl("[dD]eswei[dt]ere?[mn]", "wei[dt]ere?[mn]", " Weiteren");
    putRepl("Einkaufstachen?", "ch", "sch");
    putRepl("Bortmesser[ns]?", "Bor", "Bro");
    putRepl("Makeupstylist(in(nen)?|en)?", "Makeups", "Make-up-S");
    putRepl("Fee?dbäcks?", "Fee?dbäck", "Feedback");
    putRepl("weirete[nmrs]?", "ret", "ter");
    putRepl("Ni[vw]oschalter[ns]?", "Ni[vw]o", "Niveau");
    putRepl("[eE]xhibitionisch(e[nmrs]?)?", "isch", "istisch");
    putRepl("(ein|aus)?[gG]eschalten(e[nmrs]?)?", "ten", "tet");
    putRepl("[uU]nterschiebene[nmrs]?", "sch", "schr");
    putRepl("[uU]nbequemlich(st)?e[nmrs]?", "lich", "");
    putRepl("[uU][nm]bekweh?m(e[nmrs]?)?", "[nm]bekweh?m", "nbequem");
    putRepl("[dD]esatör(s|en?)?", "satör", "serteur");
    put("Panelen?", w -> Arrays.asList(w.replaceFirst("Panel", "Paneel"), "Panels"));
    put("D[eèé]ja-?[vV]o?ue?", "Déjà-vu");
    put("Cr[eèé]me-?fra[iî]che", "Crème fraîche");
    put("[aA]rr?an?gemont", "Arrangement");
    put("[aA]ngagemon", "Engagement");
    put("Phyrr?ussieg", "Pyrrhussieg");
    put("Mio", "Mio.");
    put("Datein", "Dateien");
    put("[pP]u(zz|ss)el", "Puzzle");
    put("Smilies", "Smileys");
    put("[dD]iseing?", "Design");
    put("[lL]ieradd?ress?e", "Lieferadresse");
    put("[bB]o[yi]kutierung", "Boykottierung");
    put("Mouseclick", "Mausklick");
    put("[aA]ktuelli?esie?rung", "Aktualisierung");
    put("Händy", "Handy");
    put("gewertschätzt", "wertgeschätzt");
    put("tieger", "Tiger");
    put("Rollade", w -> Arrays.asList("Rollladen", "Roulade"));
    put("garnichtmehr", "gar nicht mehr");
    put("vileich", "vielleicht");
    put("vll?t", "vielleicht");
    put("aufgewägt", "aufgewogen");
    put("[rR]eflektion", "Reflexion");
    put("momentmal", "Moment mal");
    put("satzt", "Satz");
    put("Büff?(ee|é)", w -> Arrays.asList("Buffet", "Büfett"));
    put("[fF]rühstücksb[uü]ff?(é|ee)", "Frühstücksbuffet");
    put("[aA]lterego", "Alter Ego");
    put("Copyride", "Copyright");
    put("Analysierung", "Analyse");
    put("Exel", "Excel");
    put("Glücklichkeit", "Glück");
    put("Begierigkeit", "Begierde");
    put("voralem", "vor allem");
    put("Unorganisation", w -> Arrays.asList("Desorganisation", "Unorganisiertheit"));
    put("Cand(el|le)lightdinner", "Candle-Light-Dinner");
    put("wertgelegt", "Wert gelegt");
    put("Deluxe", "de luxe");
    put("antuhen", "antun");
    put("komen", "kommen");
    put("genißen", "genießen");
    put("Stationskrankenpflegerin", "Stationsschwester");
    put("[iIüÜuU]b[ea]w[ae]isung", "Überweisung");
    put("[bB]oxhorn", "Bockshorn");
    put("[zZ]oolophie", "Zoophilie");
    put("Makieren", "Markieren");
    put("Altersheimer", "Alzheimer");
    put("gesen", "gesehen");
    put("Neugierigkeit", w -> Arrays.asList("Neugier", "Neugierde"));
    put("[kK]onn?ekt?schen", "Connection");
    put("E-Maul", "E-Mail");
    put("E-Mauls", "E-Mails");
    put("E-Mal", "E-Mail");
    put("E-Mals", "E-Mails");
    put("[nN]ah?richt", "Nachricht");
    put("[nN]ah?richten", "Nachrichten");
    put("Getrixe", "Getrickse");
    put("Ausage", "Aussage");
    put("gelessen", "gelesen");
    put("Kanst", "Kannst");
    put("Unwohlbefinden", "Unwohlsein");
    put("leiwagen", "Leihwagen");
    put("krahn", "Kran");
    put("[hH]ifi", "Hi-Fi");
    put("chouch", "Couch");
    put("eh?rgeit?z", "Ehrgeiz");
    put("solltes", "solltest");
    put("geklabt", "geklappt");
    put("angefangt", "angefangen");
    put("beinhält", "beinhaltet");
    put("beinhielt", "beinhaltete");
    put("beinhielten", "beinhalteten");
    put("einhaltest", "einhältst");
    put("angeruft", "angerufen");
    put("erhaltete", "erhielt");
    put("übersäht", "übersät");
    put("staats?angehoe?rigkeit", "Staatsangehörigkeit");
    put("[uU]nangeneh?mheiten", "Unannehmlichkeiten");
    put("Humuspaste", "Hummuspaste");
    put("afarung", "Erfahrung");
    put("bescheid?t", "Bescheid");
    put("[mM]iteillung", "Mitteilung");
    put("Revisionierung", "Revision");
    put("[eE]infühlvermögen", "Einfühlungsvermögen");
    put("[sS]peziellisierung", "Spezialisierung");
    put("[cC]hangse", "Chance");
    put("untergangen", "untergegangen");
    put("geliegt", "gelegen");
    put("BluRay", "Blu-ray");
    put("Freiwilligerin", "Freiwillige");
    put("Mitgliederinnen", w -> Arrays.asList("Mitglieder", "Mitgliedern"));
    put("Hautreinheiten", "Hautunreinheiten");
    put("Durfüh?rung", "Durchführung");
    put("tuhen", "tun");
    put("tuhe", "tue");
    put("tip", "Tipp");
    put("ccm", "cm³");
    put("Kilimand?jaro", "Kilimandscharo");
    put("[hH]erausfor?dung", "Herausforderung");
    put("[bB]erücksichtung", "Berücksichtigung");
    put("artzt?", "Arzt");
    put("[tT]h?elepath?ie", "Telepathie");
    put("Wi-?Fi-Dire[ck]t", "Wi-Fi Direct");
    put("gans", "ganz");
    put("Pearl-Harbou?r", "Pearl Harbor");
    put("[aA]utonomität", "Autonomie");
    put("[fF]r[uü]h?st[uü]c?k", "Frühstück");
    putRepl("(ge)?fr[uü]h?st[uü](c?k|g)t", "fr[uü]h?st[uü](c?k|g)t", "frühstückt");
    put("zucc?h?inis?", "Zucchini");
    put("[mM]itag", "Mittag");
    put("Lexion", "Lexikon");
    put("[mM]otorisation", "Motorisierung");
    put("[fF]ormalisation", "Formalisierung");
    put("ausprache", "Aussprache");
    put("[mM]enegment", "Management");
    put("[gG]ebrauspuren", "Gebrauchsspuren");
    put("viedeo", "Video");
    put("[hH]erstammung", "Abstammung");
    put("[iI]nstall?atör", "Installateur");
    put("maletriert", "malträtiert");
    put("abgeschaffen", "abgeschafft");
    put("Verschiden", "Verschieden");
    put("Anschovis", "Anchovis");
    put("Bravur", "Bravour");
    put("Grisli", "Grizzly");
    put("Grislibär", "Grizzlybär");
    put("Grislibären", "Grizzlybären");
    put("Frotté", "Frottee");
    put("Joga", "Yoga");
    put("Kalvinismus", "Calvinismus");
    put("Kollier", "Collier");
    put("Kolliers", "Colliers");
    put("Ketschup", "Ketchup");
    put("Kommunikee", "Kommuniqué");
    put("Negligee", "Negligé");
    put("Nessessär", "Necessaire");
    put("passee", "passé");
    put("Varietee", "Varieté");
    put("Varietees", "Varietés");
    put("Wandalismus", "Vandalismus");
    put("Campagne", "Kampagne");
    put("Campagnen", "Kampagnen");
    put("Jockei", "Jockey");
    put("Roulett", "Roulette");
    put("Bestellungsdaten", "Bestelldaten");
    put("Package", "Paket");
    put("Mo-Di", "Mo.–Di.");
    put("Mo-Mi", "Mo.–Mi.");
    put("Mo-Do", "Mo.–Do.");
    put("Mo-Fr", "Mo.–Fr.");
    put("Mo-Sa", "Mo.–Sa.");
    put("Mo-So", "Mo.–So.");
    put("Di-Mi", "Di.–Mi.");
    put("Di-Do", "Di.–Do.");
    put("Di-Fr", "Di.–Fr.");
    put("Di-Sa", "Di.–Sa.");
    put("Di-So", "Di.–So.");
    put("Mi-Do", "Mi.–Do.");
    put("Mi-Fr", "Mi.–Fr.");
    put("Mi-Sa", "Mi.–Sa.");
    put("Mi-So", "Mi.–So.");
    put("Do-Fr", "Do.–Fr.");
    put("Do-Sa", "Do.–Sa.");
    put("Do-So", "Do.–So.");
    put("Fr-Sa", "Fr.–Sa.");
    put("Fr-So", "Fr.–So.");
    put("Sa-So", "Sa.–So.");
    put("E-mail", "E-Mail");
    put("geleased", "geleast");
    put("released", "releast");
    putRepl("Saudiarabiens?", "Saudiarabien", "Saudi-Arabien");
    putRepl("eMail-Adressen?", "eMail-", "E-Mail-");
    putRepl("[hH]ats", "ats", "at es");
    putRepl("[Ww]ieviele?", "ieviel", "ie viel");
    putRepl("[Aa]dhoc", "dhoc", "d hoc");
    put("As", "Ass");
    put("[bB]i[sß](s?[ij]|ch)en", "bisschen");
    putRepl("Todos?", "Todo", "To-do");
    put("Kovult", "Konvolut");
    putRepl("blog(t?en?|t(es?t)?)$", "g", "gg");
    put("Zombiefizierungen", "Zombifizierungen");
    put("Hühne", w -> Arrays.asList("Bühne", "Hüne", "Hühner"));
    put("Hühnen", w -> Arrays.asList("Bühnen", "Hünen", "Hühnern"));
    put("tiptop", "tiptopp");
    put("Briese", "Brise");
    put("Rechtsschreibreformen", "Rechtschreibreformen");
    putRepl("gewertschätzte(([mnrs]|re[mnrs]?)?)$", "gewertschätzt", "wertgeschätzt");
    putRepl("knapps(t?en?|t(es?t)?)$", "pp", "p");
    put("geknappst", "geknapst");
    putRepl("gepiekste[mnrs]?$", "ie", "i");
    putRepl("Yings?", "ng", "n");
    put("Wiederstandes", "Widerstandes");
    putRepl("veganisch(e?[mnrs]?)$", "isch", "");
    putRepl("totlangweiligste[mnrs]?$", "tot", "tod");
    putRepl("tottraurigste[mnrs]?$", "tot", "tod");
    putRepl("kreir(n|e?nd)(e[mnrs]?)?$", "ire?n", "ieren");
    putRepl("Pepps?", "pp", "p");
    putRepl("Pariahs?", "h", "");
    putRepl("Oeuvres?", "Oe", "Œ");
    put("Margarite", "Margerite");
    put("Kücken", w -> Arrays.asList("Rücken", "Küken"));
    put("Kompanten", w -> Arrays.asList("Kompasse", "Kompassen"));
    put("Kandarren", "Kandaren");
    put("kniehen", "knien");
    putRepl("infisziertes?t$", "fisz", "fiz");
    putRepl("Imbusse(n|s)?$", "m", "n");
    put("Hollundern", "Holundern");
    putRepl("handgehabt(e?[mnrs]?)?$", "handgehabt", "gehandhabt");
    put("Funieres", "Furniers");
    put("Frohndiensts", "Frondiensts");
    put("fithälst", "fit hältst");
    putRepl("fitzuhalten(de?[mnrs]?)?$", "fitzuhalten", "fit zu halten");
    putRepl("(essen|schlafen|schwimmen|spazieren)zugehen$", "zugehen", " zu gehen");
    put("dilettant", w -> Arrays.asList("Dilettant", "dilettantisch"));
    putRepl("dilettante[mnrs]?$", "te", "tische");
    put("Disastern", "Desastern");
    putRepl("Brandwein(en?|s)$", "d", "nt");
    putRepl("Böhen?$", "h", "");
    putRepl("Aufständige[mnr]?$", "ig", "isch");
    putRepl("aufständig(e[mnrs]?)?$", "ig", "isch");
    putRepl("duzend(e[mnrs]?)?$", "uzend", "utzend");
    putRepl("unrelevant(e[mnrs]?)?$", "un", "ir");
    putRepl("Unrelevant(e[mnrs]?)?$", "Un", "Ir");
    put("aufgrundedessen", "aufgrund dessen");
    put("Amalgane", "Amalgame");
    put("Kafe", w -> Arrays.asList("Kaffee", "Café"));
    put("Dammbock", w -> Arrays.asList("Dambock", "Rammbock"));
    put("Dammhirsch", "Damhirsch");
    put("Fairnis", "Fairness");
    put("auschluss", w -> Arrays.asList("Ausschluss", "Ausschuss"));
    put("derikter", w -> Arrays.asList("direkter", "Direktor"));
    put("[iI]dentifierung", "Identifikation");
    put("[eE]mphatie", "Empathie");
    put("[eE]iskrem", "Eiscreme");
    put("[fF]lüchtung", "Flucht");
    put("einamen", "Einnahmen");
    put("[eE]inbu(ss|ß)ung", "Einbuße");
    put("[eE]inbu(ss|ß)ungen", "Einbußen");
    put("nachichten", "Nachrichten");
    put("gegehen", "gegangen");
    put("Ethnocid", "Ethnozid");
    put("Exikose", "Exsikkose");
    put("Schonvermögengrenze", "Schonvermögensgrenze");
    put("kontest", "konntest");
    put("pitza", "Pizza");
    put("Tütü", "Tutu");
    put("gebittet", "gebeten");
    put("gekricht", "gekriegt");
    put("Krankenheit", "Krankheit");
    put("Krankenheiten", "Krankheiten");
    put("[hH]udd[yi]", "Hoodie");
    put("Treibel", "Tribal");
    put("vorort", "vor Ort");
    put("Brotwürfelcro[uû]tons", "Croûtons");
    put("bess?tetigung", "Bestätigung");
    put("[mM]ayonaisse", "Mayonnaise");
    put("misverstaendnis", "Missverständnis");
    put("[vV]erlu(ss|ß)t", "Verlust");
    put("glückigerweise", "glücklicherweise");
    put("[sS]tandtart", "Standard");
    put("Mainzerstrasse", "Mainzer Straße");
    put("Genehmigerablauf", "Genehmigungsablauf");
    put("Bestellerurkunde", "Bestellungsurkunde");
    put("Selbstmitleidigkeit", "Selbstmitleid");
    put("[iI]ntuion", "Intuition");
    put("[cCkK]ontener", "Container");
    put("Barcadi", "Bacardi");
    put("Unnanehmigkeit", "Unannehmlichkeit");
    put("[wW]ischmöppen?", "Wischmopps");
    putRepl("[oO]rdnungswiedrichkeit(en)?", "[oO]rdnungswiedrich", "Ordnungswidrig");
    putRepl("Mauntenbiker[ns]?", "^Maunten", "Mountain");
    putRepl("Mauntenbikes?", "Maunten", "Mountain");
    putRepl("[nN]euhichkeit(en)?", "[nN]euhich", "Neuig");
    putRepl("Prokopfverbrauchs?", "Prokopfv", "Pro-Kopf-V"); // Duden
    putRepl("[Gg]ilst", "ilst", "iltst");
    putRepl("[vV]ollrichtung(en)?", "[vV]oll", "Ver");
    putRepl("[vV]ollrichtest", "oll", "er");
    putRepl("[vV]ollrichten?", "oll", "er");
    putRepl("[vV]ollrichtet(e([mnrs])?)?", "oll", "er");
    putRepl("[bB]edingslos(e([mnrs])?)?", "ding", "dingung");
    putRepl("[eE]insichtbar(e[mnrs]?)?", "sicht", "seh");
    putRepl("asymetrisch(ere|ste)[mnrs]?$", "ym", "ymm");
    putRepl("alterwürdig(ere|ste)[mnrs]?$", "lter", "ltehr");
    putRepl("aufständig(ere|ste)[mnrs]?$", "ig", "isch");
    putRepl("blutdurstig(ere|ste)[mnrs]?$", "ur", "ür");
    putRepl("dilettant(ere|este)[mnrs]?$", "nt", "ntisch");
    putRepl("eliptisch(ere|ste)[mnrs]?$", "l", "ll");
    putRepl("angegröhlt(e([mnrs])?)?$", "öh", "ö");
    putRepl("gothisch(ere|ste)[mnrs]?$", "th", "t");
    putRepl("kollossal(ere|ste)[mnrs]?$", "ll", "l");
    putRepl("paralel(lere|lste)[mnrs]?$", "paralel", "paralle");
    putRepl("symetrischste[mnrs]?$", "ym", "ymm");
    putRepl("rethorisch(ere|ste)[mnrs]?$", "rethor", "rhetor");
    putRepl("repetativ(ere|ste)[mnrs]?$", "repetat", "repetit");
    putRepl("voluptös(e|ere|este)?[mnrs]?$", "tös", "tuös");
    putRepl("[pP]flanzig(e[mnrs]?)?", "ig", "lich");
    putRepl("geblogt(e[mnrs]?)?$", "gt", "ggt");
    putRepl("herraus.*", "herraus", "heraus");
    putRepl("[aA]bbonier(en?|s?t|te[mnrst]?)", "bbo", "bon");
    putRepl("[aA]pelier(en?|s?t|te[nt]?)", "pel", "ppell");
    putRepl("[vV]oltie?schier(en?|s?t|te[nt]?)", "ie?sch", "ig");
    putRepl("[mM]eistverkaufteste[mnrs]?", "teste", "te");
    putRepl("[uU]nleshaft(e[mnrs]?)?", "haft", "erlich");
    putRepl("[gG]laubenswürdig(e[mnrs]?)?", "ens", "");
    putRepl("[nN]i[vw]ovoll(e[mnrs]?)?", "[vw]ovoll", "veauvoll");
    putRepl("[nN]otgezwungend?(e[mnrs]?)?", "zwungend?", "drungen");
    putRepl("[mM]isstraurig(e[mnrs]?)?", "rig", "isch");
    putRepl("[iI]nflagrantie?", "flagrantie?", " flagranti");
    putRepl("Aux-Anschl(uss(es)?|üssen?)", "Aux", "AUX");
    putRepl("desinfektiert(e[mnrs]?)?", "fekt", "fiz");
    putRepl("desinfektierend(e[mnrs]?)?", "fekt", "fiz");
    putRepl("desinfektieren?", "fekt", "fiz");
    putRepl("[dD]esinfektionier(en?|t(e[mnrs]?)?|st)", "fektionier", "fizier");
    putRepl("[dD]esinfektionierend(e[mnrs]?)?", "fektionier", "fizier");
    putRepl("[kK]ompensionier(en?|t(e[mnrs]?)?|st)", "ion", "");
    putRepl("neuliche[mnrs]?", "neu", "neuer");
    putRepl("ausbüchsen?", "chs", "x");
    putRepl("aus(ge)?büchst(en?)?", "chs", "x");
    putRepl("innoff?iziell?(e[mnrs]?)?", "innoff?iziell?", "inoffiziell");
    putRepl("[gG]roesste[mnrs]?", "oess", "öß");
    putRepl("[tT]efonisch(e[mnrs]?)?", "efon", "elefon");
    putRepl("[oO]ptimalisiert", "alis", "");
    putRepl("[iI]ntrovertisch(e[mnrs]?)?", "isch", "iert");
    putRepl("[aA]miert(e[mnrs]?)?", "mi", "rmi");
    putRepl("[vV]ersiehrt(e[mnrs]?)?", "h", "");
    putRepl("[dD]urchsichtbar(e[mnrs]?)?", "bar", "ig");
    putRepl("[oO]ffensichtig(e[mnrs]?)?", "ig", "lich");
    putRepl("[zZ]urverfühgung", "verfühgung", " Verfügung");
    putRepl("[vV]erständlichkeitsfragen?", "lichkeits", "nis");
    putRepl("[sS]pendeangebot(e[ns]?)?", "[sS]pende", "Spenden");
    putRepl("gahrnichts?", "gahr", "gar ");
    putRepl("[aA]ugensichtlich(e[mnrs]?)?", "sicht", "schein");
    putRepl("[lL]eidensvoll(e[mnrs]?)?", "ens", "");
    putRepl("[bB]ewusstlich(e[mnrs]?)?", "lich", "");
    putRepl("[vV]erschmerzlich(e[mnrs]?)?", "lich", "bar");
    putRepl("Krankenbruders?", "bruder", "pfleger");
    putRepl("Krankenbrüdern?", "brüder", "pfleger");
    putRepl("Lan-(Kabel[ns]?|Verbindung)", "Lan", "LAN");
    putRepl("[sS]epalastschriftmandat(s|en?)?", "[sS]epal", "SEPA-L");
    putRepl("Pinn?eingaben?", "Pinn?e", "PIN-E");
    putRepl("[sS]imkarten?", "[sS]imk", "SIM-K");
    putRepl("[vV]orsich(geht|gehen|ging(en)?|gegangen)", "sich", " sich ");
    putRepl("mitsich(bringt|bringen|brachten?|gebracht)", "sich", " sich ");
    putRepl("[ck]arnivorisch(e[mnrs]?)?", "[ck]arnivorisch", "karnivor");
    putRepl("[pP]erfektest(e[mnrs]?)?", "est", "");
    putRepl("[gG]leichtig(e[mnrs]?)?", "tig", "zeitig");
    putRepl("[uU]n(her)?vorgesehen(e[mnrs]?)?", "(her)?vor", "vorher");
    putRepl("([cC]orona|[gG]rippe)viruss?es", "viruss?es", "virus");
    putRepl("Zaubererin(nen)?", "er", "");
    putRepl("Second-Hand-L[äa]dens?", "Second-Hand-L", "Secondhandl");
    putRepl("Second-Hand-Shops?", "Second-Hand-S", "Secondhands");
    putRepl("[mM]editerranisch(e[mnrs]?)?", "isch", "");
    putRepl("interplementier(s?t|en?)", "inter", "im");
    putRepl("[hH]ochalterlich(e[mnrs]?)?", "alter", "mittelalter");
    putRepl("posiniert(e[mnrs]?)?", "si", "sitio");
    putRepl("[rR]ussophobisch(e[mnrs]?)?", "isch", "");
    putRepl("[uU]nsachmä(ß|ss?)ig(e[mnrs]?)?", "mä(ß|ss?)ig", "gemäß");
    putRepl("[mM]odernisch(e[mnrs]?)?", "isch", "");
    putRepl("intapretation(en)?", "inta", "Inter");
    putRepl("[rR]ethorikkurs(e[ns]?)?", "eth", "het");
    putRepl("[uU]nterschreibungsfähig(e[mnrs]?)?", "schreibung", "schrift");
    putRepl("[eE]rrorier(en?|t(e[mnrs]?)?|st)", "ror", "u");
    putRepl("malediert(e[mnrs]?)?", "malediert", "malträtiert");
    putRepl("maletriert(e[mnrs]?)?", "maletriert", "malträtiert");
    putRepl("Ausbildereignerprüfung(en)?", "eigner", "eignungs");
    putRepl("abtrakt(e[mnrs]?)?", "ab", "abs");
    putRepl("unerfolgreich(e[mnrs]?)?", "unerfolgreich", "erfolglos");
    putRepl("[bB]attalion(en?|s)?", "[bB]attalion", "Bataillon");
    putRepl("[bB]esuchungsverbot(e[ns]?)?", "ung", "");
    putRepl("spätrig(e[mnrs]?)?", "rig", "er");
    putRepl("angehangene[mnrs]?", "hangen", "hängt");
    putRepl("[ck]amel[ie]onhaft(e[mnrs]?)?", "[ck]am[ie]lion", "chamäleon");
    putRepl("[wW]idersprüchig(e[mnrs]?)?", "ig", "lich");
    putRepl("[fF]austig(e[mnrs]?)?", "austig", "austdick");
    putRepl("Belastungsekgs?", "ekg", "-EKG");
    putRepl("Flektion(en)?", "Flektion", "Flexion");
    put("Bingerloch", "Binger Loch");
    put("[nN]or[dt]rh?einwest(f|ph)alen", "Nordrhein-Westfalen");
    put("abzusolvieren", "zu absolvieren");
    put("Schutzfließ", "Schutzvlies");
    put("Simlock", "SIM-Lock");
    put("fäschungen", "Fälschungen");
    put("Weinverköstigung", "Weinverkostung");
    put("vertag", "Vertrag");
    put("geauessert", "geäußert");
    put("gestriffen", "gestreift");
    put("gefäh?ten", "Gefährten");
    put("gefäh?te", "Gefährte");
    put("immenoch", "immer noch");
    put("sevice", "Service");
    put("verhälst", "verhältst");
    put("[sS]äusche", "Seuche");
    put("Schalottenburg", "Charlottenburg");
    put("senora", "Señora");
    put("widerrum", "wiederum");
    put("[dD]epp?risonen", "Depressionen");
    put("Defribilator", "Defibrillator");
    put("Defribilatoren", "Defibrillatoren");
    put("SwatchGroup", "Swatch Group");
    put("achtungslo[ßs]", "achtlos");
    put("Boomerang", "Bumerang");
    put("Boomerangs", "Bumerangs");
    put("Lg", w -> Arrays.asList("LG", "Liebe Grüße"));
    put("gildet", "gilt");
    put("gleitete", "glitt");
    put("gleiteten", "glitten");
    put("Standbay", "Stand-by");
    put("[vV]ollkommnung", "Vervollkommnung");
    put("femist", "vermisst");
    put("stantepede", "stante pede");
    put("[kK]ostarika", "Costa Rica");
    put("[kK]ostarikas", "Costa Ricas");
    put("[aA]uthenzität", "Authentizität");
    put("anlässig", "anlässlich");
    put("[sS]tieft", "Stift");
    put("[Ii]nspruchnahme", "Inanspruchnahme");
    put("höstwah?rsch[ea]inlich", "höchstwahrscheinlich");
    put("[aA]lterschbeschränkung", "Altersbeschränkung");
    put("[kK]unstoff", "Kunststoff");
    put("[iI]nstergramm?", "Instagram");
    put("fleicht", "vielleicht");
    put("[eE]rartens", "Erachtens");
    put("laufte", "lief");
    put("lauften", "liefen");
    put("malzeit", "Mahlzeit");
    put("[wW]ahts?app", "WhatsApp");
    put("[wW]elan", w -> Arrays.asList("WLAN", "W-LAN"));
    put("Pinn", w -> Arrays.asList("Pin", "PIN"));
    put("Geldmachung", w -> Arrays.asList("Geltendmachung", "Geldmacherei"));
    put("[uU]nstimm?ichkeiten", "Unstimmigkeiten");
    put("Teilnehmung", "Teilnahme");
    put("Teilnehmungen", "Teilnahmen");
    put("waser", "Wasser");
    put("Bekennung", "Bekenntnis");
    put("[hH]irar?chie", "Hierarchie");
    put("Chr", "Chr.");
    put("Tiefbaumt", "Tiefbauamt");
    put("getäucht", "getäuscht");
    put("[hH]ähme", "Häme");
    put("Wochendruhezeiten", "Wochenendruhezeiten");
    put("Studiumplatzt?", "Studienplatz");
    put("Permanent-Make-Up", "Permanent-Make-up");
    put("woltet", "wolltet");
    put("Bäckei", "Bäckerei");
    put("Bäckeien", "Bäckereien");
    put("warmweis", "warmweiß");
    put("kaltweis", "kaltweiß");
    put("jez", "jetzt");
    put("hendis", "Handys");
    put("wie?derwarten", "wider Erwarten");
    put("[eE]ntercott?e", "Entrecôte");
    put("[eE]rwachtung", "Erwartung");
    put("[aA]nung", "Ahnung");
    put("[uU]nreimlichkeiten", "Ungereimtheiten");
    put("[uU]nangeneh?mlichkeiten", "Unannehmlichkeiten");
    put("Messy", "Messie");
    put("Polover", "Pullover");
    put("heilwegs", "halbwegs");
    put("undsoweiter", "und so weiter");
    put("Gladbeckerstrasse", "Gladbecker Straße");
    put("Bonnerstra(ß|ss)e", "Bonner Straße");
    put("[bB]range", "Branche");
    put("Gewebtrauma", "Gewebetrauma");
    put("aufgehangen", "aufgehängt");
    put("Ehrenamtpauschale", "Ehrenamtspauschale");
    put("Essenzubereitung", "Essenszubereitung");
    put("[gG]eborgsamkeit", "Geborgenheit");
    put("gekommt", "gekommen");
    put("hinweißen", "hinweisen");
    put("Importation", "Import");
    put("lädest", "lädst");
    put("Themabereich", "Themenbereich");
    put("Werksresett", "Werksreset");
    put("wiederfahren", "widerfahren");
    put("wiederspiegelten", "widerspiegelten");
    put("weicheinlich", "wahrscheinlich");
    put("schnäpchen", "Schnäppchen");
    put("Hinduist", "Hindu");
    put("Hinduisten", "Hindus");
    put("Konzeptierung", "Konzipierung");
    put("Phyton", "Python");
    put("nochnichtmals?", "noch nicht einmal");
    put("Refelektion", "Reflexion");
    put("Refelektionen", "Reflexionen");
    put("[sS]chanse", "Chance");
    put("nich", "nicht");
    put("wat", "was");
    put("[Ee][Ss]ports", "E-Sports");
    put("gerelaunch(ed|t)", "relauncht");
    put("Gerelaunch(ed|t)", "Relauncht");
    put("Bowl", "Bowle");
    put("Dark[Ww]eb", "Darknet");
    put("Sachs?en-Anhal?t", "Sachsen-Anhalt");
    put("[Ss]chalgen", "schlagen");
    put("[Ss]chalge", "schlage");
    put("[dD]eutsche?sprache", "deutsche Sprache");
    put("eigl", "eigtl");
    put("ma", "mal");
    put("leidete", "litt");
    put("leidetest", "littest");
    put("leideten", "litten");
    put("Hoody", "Hoodie");
    put("Hoodys", "Hoodies");
    put("Staatsexam", "Staatsexamen");
    put("Staatsexams", "Staatsexamens");
    put("Exam", "Examen");
    put("Exams", "Examens");
    put("[Rr]eviewing", "Review");
    put("[Bb]aldmöglich", "baldmöglichst");
    put("[Bb]rudi", "Bruder");
    put("ih", w -> Arrays.asList("ich", "in", "im", "ah"));
    put("Ih", w -> Arrays.asList("Ich", "In", "Im", "Ah"));
    put("[qQ]uicky", "Quickie");
    put("[qQ]uickys", "Quickies");
    put("bissl", w -> Arrays.asList("bissel", "bisserl"));
    put("Keywort", w -> Arrays.asList("Keyword", "Stichwort"));
    put("Keyworts", w -> Arrays.asList("Keywords", "Stichworts"));
    put("Keywörter", w -> Arrays.asList("Keywords", "Stichwörter"));
    put("strang", w -> Arrays.asList("Strang", "strengte"));
    put("Gym", w -> Arrays.asList("Fitnessstudio", "Gymnasium"));
    put("Gyms", w -> Arrays.asList("Fitnessstudios", "Gymnasiums"));
    put("gäng", w -> Arrays.asList("ging", "gang"));
    put("di", w -> Arrays.asList("du", "die", "Di.", "der", "den"));
    put("Di", w -> Arrays.asList("Du", "Die", "Di.", "Der", "Den"));
    put("mußt", "musst");
    put("müßtest", "müsstest");
    put("müßten", "müssten");
    put("Bs", "Bis");
    put("Biß", "Biss");
    put("bs", "bis");
    put("sehn", "sehen");
    put("zutun", "zu tun");
    put("Müllhalte", "Müllhalde");
    put("Entäuschung", "Enttäuschung");
    put("Entäuschungen", "Enttäuschungen");
    put("kanns", "kann es");
    put("funktionierts", "funktioniert es");
    put("hbat", "habt");
    put("ichs", "ich es");
    put("folgendermassen", "folgendermaßen");
    put("Adon", "Add-on");
    put("Adons", "Add-ons");
    put("vertaggt", w -> Arrays.asList("vertagt", "getaggt"));
    put("keinsten", w -> Arrays.asList("keinen", "kleinsten"));
    put("Angehensweise", "Vorgehensweise");
    put("Angehensweisen", "Vorgehensweisen");
    put("Neudefinierung", "Neudefinition");
    put("Definierung", "Definition");
    put("Definierungen", "Definitionen");
    putRepl("[Üü]bergrifflich(e[mnrs]?)?", "lich", "ig");
  }

  private static void putRepl(String wordPattern, String pattern, String replacement) {
    ADDITIONAL_SUGGESTIONS.put(StringMatcher.regexp(wordPattern), w -> singletonList(w.replaceFirst(pattern, replacement)));
  }

  private static void put(String pattern, String replacement) {
    ADDITIONAL_SUGGESTIONS.put(StringMatcher.regexp(pattern), w -> singletonList(replacement));
  }

  private static void put(String pattern, Function<String, List<String>> f) {
    ADDITIONAL_SUGGESTIONS.put(StringMatcher.regexp(pattern), f);
  }

  private static final GermanWordSplitter splitter = getSplitter();
  private static GermanWordSplitter getSplitter() {
    try {
      return new GermanWordSplitter(false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final LineExpander lineExpander = new LineExpander();
  private final GermanCompoundTokenizer compoundTokenizer;
  private final Synthesizer synthesizer;
  private final Tagger tagger;

  public GermanSpellerRule(ResourceBundle messages, German language) {
    this(messages, language, null, null);
  }

  /**
   * @since 4.2
   */
  public GermanSpellerRule(ResourceBundle messages, German language, UserConfig userConfig, String languageVariantPlainTextDict) {
    this(messages, language, userConfig, languageVariantPlainTextDict, Collections.emptyList(), null);
  }

  /**
   * @since 4.3
   */
  public GermanSpellerRule(ResourceBundle messages, German language, UserConfig userConfig, String languageVariantPlainTextDict, List<Language> altLanguages, LanguageModel languageModel) {
    super(messages, language, language.getNonStrictCompoundSplitter(), getSpeller(language, userConfig, languageVariantPlainTextDict), userConfig, altLanguages, languageModel);
    addExamplePair(Example.wrong("LanguageTool kann mehr als eine <marker>nromale</marker> Rechtschreibprüfung."),
                   Example.fixed("LanguageTool kann mehr als eine <marker>normale</marker> Rechtschreibprüfung."));
    compoundTokenizer = language.getStrictCompoundTokenizer();
    tagger = language.getTagger();
    synthesizer = language.getSynthesizer();
  }

  @Override
  protected synchronized void init() throws IOException {
    super.init();
    super.ignoreWordsWithLength = 1;
    String pattern = "(" + nonWordPattern.pattern() + "|(?<=[\\d°])-|-(?=\\d+))";
    nonWordPattern = Pattern.compile(pattern);
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  @Override
  protected boolean isIgnoredNoCase(String word) {
    return wordsToBeIgnored.contains(word) ||
      // words from spelling.txt also accepted in uppercase (e.g. sentence start, bullet list items):
      (word.matches("[A-ZÖÄÜ][a-zöäüß-]+") && wordsToBeIgnored.contains(word.toLowerCase(language.getLocale()))) ||
      (ignoreWordsWithLength > 0 && word.length() <= ignoreWordsWithLength);
  }

  @Override
  public List<String> getCandidates(String word) {
    List<List<String>> partList;
    try {
      partList = splitter.getAllSplits(word);
    } catch (InputTooLongException e) {
      partList = new ArrayList<>();
    }
    List<String> candidates = new ArrayList<>();
    for (List<String> parts : partList) {
      List<String> tmp = super.getCandidates(parts);
      tmp = tmp.stream().filter(k -> !k.matches("[A-ZÖÄÜ][a-zöäüß]+-[\\-\\s]?[a-zöäüß]+") &&
                                     !k.matches("[a-zöäüß]+-[\\-\\s][A-ZÖÄÜa-zöäüß]+")).collect(Collectors.toList());  // avoid e.g. "Direkt-weg"
      tmp = tmp.stream().filter(k -> !k.contains("-s-")).collect(Collectors.toList());  // avoid e.g. "Geheimnis-s-voll"
      if (!word.endsWith("-")) {
        tmp = tmp.stream().filter(k -> !k.endsWith("-")).collect(Collectors.toList());  // avoid "xyz-" unless the input word ends in "-"
      }
      candidates.addAll(tmp);
      if (parts.size() == 2) {
        // e.g. "inneremedizin" -> "innere Medizin", "gleichgroß" -> "gleich groß"
        candidates.add(parts.get(0) + " " + parts.get(1));
        if (isNounOrProperNoun(uppercaseFirstChar(parts.get(1)))) {
          candidates.add(parts.get(0) + " " + uppercaseFirstChar(parts.get(1)));
        }
      }
      if (parts.size() == 2 && !parts.get(0).endsWith("s")) {
        // so we get e.g. Einzahlungschein -> Einzahlungsschein
        candidates.add(parts.get(0) + "s" + parts.get(1));
      }
      if (parts.size() == 2 && parts.get(1).startsWith("s")) {
        // so we get e.g. Ordnungshütter -> Ordnungshüter (Ordnungshütter is split as Ordnung + shütter)
        String firstPart = parts.get(0);
        String secondPart = parts.get(1);
        candidates.addAll(super.getCandidates(Arrays.asList(firstPart + "s", secondPart.substring(1))));
      }
    }
    return candidates;
  }

  @Override
  protected boolean isProhibited(String word) {
    return super.isProhibited(word) ||
      wordStartsToBeProhibited.stream().anyMatch(w -> word.startsWith(w)) ||
      wordEndingsToBeProhibited.stream().anyMatch(w -> word.endsWith(w));
  }

  @Override
  protected void addIgnoreWords(String origLine) {
    // hack: Swiss German doesn't use "ß" but always "ss" - replace this, otherwise
    // misspellings (from Swiss point-of-view) like "äußere" wouldn't be found:
    String line = language.getShortCodeWithCountryAndVariant().equals("de-CH") ? origLine.replace("ß", "ss") : origLine;
    if (origLine.endsWith("-*")) {
      // words whose line ends with "-*" are only allowed in hyphenated compounds
      wordsToBeIgnoredInCompounds.add(line.substring(0, line.length() - 2));
      return;
    }
    List<String> words = expandLine(line);
    for (String word : words) {
      super.addIgnoreWords(word);
    }
  }

  @Override
  protected List<String> expandLine(String line) {
    return lineExpander.expandLine(line);
  }

  @Override
  protected RuleMatch createWrongSplitMatch(AnalyzedSentence sentence, List<RuleMatch> ruleMatchesSoFar, int pos, String coveredWord, String suggestion1, String suggestion2, int prevPos) {
    if (suggestion2.matches("[a-zöäü]-.+")) {
      // avoid confusing matches for e.g. "haben -sehr" (was: "habe n-sehr")
      return null;
    }
    return super.createWrongSplitMatch(sentence, ruleMatchesSoFar, pos, coveredWord, suggestion1, suggestion2, prevPos);
  }

  /*
   * @since 3.6
   */
  @Override
  public List<String> getSuggestions(String word) throws IOException {
    /* Do not just comment in because of https://github.com/languagetool-org/languagetool/issues/3757
    if (word.length() < 18 && word.matches("[a-zA-Zöäüß-]+.?")) {
      for (String prefix : VerbPrefixes.get()) {
        if (word.startsWith(prefix)) {
          String lastPart = word.substring(prefix.length());
          if (lastPart.length() > 3 && !isMisspelled(lastPart)) {
            // as these are only single words and both the first part and the last part are spelled correctly
            // (but the combination is not), it's okay to log the words from a privacy perspective:
            logger.info("UNKNOWN: {}", word);
          }
        }
      }
    }*/
    List<String> suggestions = super.getSuggestions(word);
    suggestions = suggestions.stream().filter(this::acceptSuggestion).collect(Collectors.toList());
    if (word.endsWith(".")) {
      // To avoid losing the "." of "word" if it is at the end of a sentence.
      suggestions.replaceAll(s -> s.endsWith(".") ? s : s + ".");
    }
    suggestions = suggestions.stream().filter(k ->
      !k.equals(word) &&
      (!k.endsWith("-") || word.endsWith("-")) &&  // no "-" at end (#2450)
      !k.matches("\\p{L} \\p{L}+")  // single chars like in "ü berstenden" (#2610)
    ).collect(Collectors.toList());
    return suggestions;
  }

  @Override
  protected boolean acceptSuggestion(String s) {
      return !PREVENT_SUGGESTION.matcher(s).matches()
        && !s.matches(".+[*_:]in")  // only suggested when using "..._in" in spelling.txt, so rather never offer this suggestion
        && !s.matches(".+[*_:]innen")
        && !s.contains("--")
        && !s.endsWith("roulett")
        && !s.matches(".+\\szigste[srnm]") // do not suggest "ein zigste" for "einzigste"
        && !s.matches("[\\wöäüÖÄÜß]+ [a-zöäüß]-[\\wöäüÖÄÜß]+")   // e.g. "Mediation s-Background"
        && !s.matches("[\\wöäüÖÄÜß]+- [\\wöäüÖÄÜß]+")   // e.g. "Pseudo- Rebellentum"
        && !s.matches("[A-ZÄÖÜ][a-zäöüß]+-[a-zäöüß]+-[a-zäöüß]+")   // e.g. "Kapuze-over-teil"
        && !s.matches("[A-ZÄÖÜ][a-zäöüß]+- [a-zäöüßA-ZÄÖÜ\\-]+")   // e.g. "Tuchs-N-Harmonie"
        && !s.matches("[\\wöäüÖÄÜß]+ -[\\wöäüÖÄÜß]+")   // e.g. "ALT -TARIF"
        && !s.endsWith("-s")   // https://github.com/languagetool-org/languagetool/issues/4042
        && !s.endsWith(" de")   // https://github.com/languagetool-org/languagetool/issues/4042
        && !s.matches("[A-ZÖÄÜa-zöäüß] .+") // z.B. nicht "I Tand" für "IT and Services"
        && !s.matches(".+ [a-zöäüßA-ZÖÄÜ]");  // z.B. nicht "rauchen e" für "rauche ne" vorschlagen
  }

  @NotNull
  protected static List<String> getSpellingFilePaths(String langCode) {
    List<String> paths = new ArrayList<>(CompoundAwareHunspellRule.getSpellingFilePaths(langCode));
    paths.add( "/" + langCode + "/hunspell/spelling_recommendation.txt");
    return paths;
  }

  @Nullable
  protected static MorfologikMultiSpeller getSpeller(Language language, UserConfig userConfig, String languageVariantPlainTextDict) {
    try {
      String langCode = language.getShortCode();
      String morfoFile = "/" + langCode + "/hunspell/" + langCode + "_" + language.getCountries()[0] + JLanguageTool.DICTIONARY_FILENAME_EXTENSION;
      if (JLanguageTool.getDataBroker().resourceExists(morfoFile)) {  // spell data will not exist in LibreOffice/OpenOffice context
        List<String> paths = new ArrayList<>(getSpellingFilePaths(langCode));
        if (languageVariantPlainTextDict != null) {
          paths.add(languageVariantPlainTextDict);
        }
        List<InputStream> streams = getStreams(paths);
        try (BufferedReader br = new BufferedReader(
          new InputStreamReader(new SequenceInputStream(Collections.enumeration(streams)), UTF_8))) {
          BufferedReader variantReader = getVariantReader(languageVariantPlainTextDict);
          return new MorfologikMultiSpeller(morfoFile, new ExpandingReader(br), paths,
            variantReader, languageVariantPlainTextDict, userConfig, MAX_EDIT_DISTANCE);
        }
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not set up morfologik spell checker", e);
    }
  }

  @Nullable
  private static BufferedReader getVariantReader(String languageVariantPlainTextDict) {
    BufferedReader variantReader = null;
    if (languageVariantPlainTextDict != null && !languageVariantPlainTextDict.isEmpty()) {
      InputStream variantStream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(languageVariantPlainTextDict);
      variantReader = new ExpandingReader(new BufferedReader(new InputStreamReader(variantStream, UTF_8)));
    }
    return variantReader;
  }

  @Override
  protected void filterForLanguage(List<String> suggestions) {
    if (language.getShortCodeWithCountryAndVariant().equals("de-CH")) {
      for (int i = 0; i < suggestions.size(); i++) {
        String s = suggestions.get(i);
        suggestions.set(i, s.replace("ß", "ss"));
      }
    }
    // Remove suggestions like "Mafiosi s" and "Mafiosi s.":
    suggestions.removeIf(s -> Arrays.stream(s.split(" ")).anyMatch(k -> k.matches("\\w\\p{Punct}?")));
    // This is not quite correct as it might remove valid suggestions that start with "-",
    // but without this we get too many strange suggestions that start with "-" for no apparent reason
    // (e.g. for "Gratifikationskrisem" -> "-Gratifikationskrisen"):
    suggestions.removeIf(s -> s.length() > 1 && s.startsWith("-"));
  }

  @Override
  protected List<String> sortSuggestionByQuality(String misspelling, List<String> suggestions) {
    List<String> result = new ArrayList<>();
    List<String> topSuggestions = new ArrayList<>(); // candidates from suggestions that get boosted to the top

    for (String suggestion : suggestions) {
      if (misspelling.equalsIgnoreCase(suggestion)) { // this should be preferred - only case differs
        topSuggestions.add(suggestion);
      } else if (suggestion.contains(" ")) { // this should be preferred - prefer e.g. "vor allem":
        // suggestions at the sentence end include a period sometimes, clean up for ngram lookup
        String[] words = suggestion.replaceFirst("\\.$", "").split(" ", 2);
        if (languageModel != null && words.length == 2) {
          // language model available, test if split word occurs at all / more frequently than alternative
          Probability nonSplit = languageModel.getPseudoProbability(singletonList(words[0] + words[1]));
          Probability split = languageModel.getPseudoProbability(Arrays.asList(words));
          //System.out.printf("Probability - %s vs %s: %.12f (%d) vs %.12f (%d)%n",
          //  words[0] + words[1], suggestion,
          if (nonSplit.getProb() > split.getProb() || split.getProb() == 0) {
            result.add(suggestion);
          } else {
            topSuggestions.add(suggestion);
          }
        } else {
          topSuggestions.add(suggestion);
        }
      } else {
        result.add(suggestion);
      }
    }
    result.addAll(0, topSuggestions);

    return result;
  }

  @Override
  protected List<String> getFilteredSuggestions(List<String> wordsOrPhrases) {
    List<String> result = new ArrayList<>();
    for (String wordOrPhrase : wordsOrPhrases) {
      String[] words = tokenizeText(wordOrPhrase);
      if (words.length >= 2 && isAdjOrNounOrUnknown(words[0]) && isNounOrUnknown(words[1]) &&
              startsWithUppercase(words[0]) && startsWithUppercase(words[1])) {
        // ignore, seems to be in the form "Release Prozess" which is *probably* wrong
      } else {
        result.add(wordOrPhrase);
      }
    }
    return result;
  }

  private boolean isNounOrUnknown(String word) {
    try {
      List<AnalyzedTokenReadings> readings = tagger.tag(singletonList(word));
      return readings.stream().anyMatch(reading -> reading.hasPosTagStartingWith("SUB") || reading.isPosTagUnknown());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isOnlyNoun(String word) {
    try {
      List<AnalyzedTokenReadings> readings = tagger.tag(singletonList(word));
      for (AnalyzedTokenReadings reading : readings) {
        boolean accept = reading.getReadings().stream().allMatch(k -> k.getPOSTag() != null && k.getPOSTag().startsWith("SUB:"));
        if (!accept) {
          return false;
        }
      }
      return readings.stream().allMatch(reading -> reading.matchesPosTagRegex("SUB:.*"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isAdjOrNounOrUnknown(String word) {
    try {
      List<AnalyzedTokenReadings> readings = tagger.tag(singletonList(word));
      return readings.stream().anyMatch(reading -> reading.hasPosTagStartingWith("SUB") || reading.hasPosTagStartingWith("ADJ") || reading.isPosTagUnknown());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isNounOrProperNoun(String word) {
    try {
      List<AnalyzedTokenReadings> readings = tagger.tag(singletonList(word));
      return readings.stream().anyMatch(reading -> reading.hasPosTagStartingWith("SUB") || reading.hasPosTagStartingWith("EIG"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean ignoreElative(String word) {
    if (StringUtils.startsWithAny(word, "bitter", "dunkel", "erz", "extra", "früh",
        "gemein", "hyper", "lau", "mega", "minder", "stock", "super", "tod", "ultra", "ur")) {
      String lastPart = RegExUtils.removePattern(word, "^(bitter|dunkel|erz|extra|früh|gemein|grund|hyper|lau|mega|minder|stock|super|tod|ultra|ur|voll)");
      return lastPart.length() >= 3 && !isMisspelled(lastPart);
    }
    return false;
  }

  @Override
  public boolean isMisspelled(String word) {
    if (word.startsWith("Spielzug") && !word.matches("Spielzugs?|Spielzugangs?|Spielzuganges|Spielzugbuchs?|Spielzugbüchern?|Spielzuges|Spielzugverluste?|Spielzugverluste[ns]")) {
      return true;
    }
    if (word.startsWith("Standart") && !word.equals("Standarte") && !word.equals("Standarten") && !word.startsWith("Standartenträger") && !word.startsWith("Standartenführer")) {
      return true;
    }
    if (word.endsWith("schafte") && word.matches("[A-ZÖÄÜ][a-zöäß-]+schafte")) {
      return true;
    }
    return super.isMisspelled(word);
  }

  @Override
  protected boolean ignoreWord(List<String> words, int idx) throws IOException {
    String word = words.get(idx);
    if (word.length() > MAX_TOKEN_LENGTH) {
      return true;
    }
    boolean ignore = super.ignoreWord(words, idx);
    boolean ignoreUncapitalizedWord = !ignore && idx == 0 && super.ignoreWord(StringUtils.uncapitalize(words.get(0)));
    boolean ignoreByHyphen = false;
    boolean ignoreBulletPointCase = false;
    if (!ignoreUncapitalizedWord) {
      // happens e.g. with list items in Google Docs, which introduce \uFEFF, which here appears as
      // an empty token:
      ignoreBulletPointCase = !ignore && idx == 1 && words.get(0).isEmpty() 
        && startsWithUppercase(word)
        && isMisspelled(word)
        && !isMisspelled(word.toLowerCase());
    }
    boolean ignoreHyphenatedCompound = false;
    if (!ignore && !ignoreUncapitalizedWord) {
      if (word.contains("-")) {
        if (idx > 0 && "".equals(words.get(idx-1)) && StringUtils.startsWithAny(word, "stel-", "tel-") ) {
          // accept compounds such as '100stel-Millimeter' or '5tel-Gramm'
          return !isMisspelled(StringUtils.substringAfter(word, "-"));
        } else {
          ignoreByHyphen = word.endsWith("-") && ignoreByHangingHyphen(words, idx);
        }
      }
      ignoreHyphenatedCompound = !ignoreByHyphen && ignoreCompoundWithIgnoredWord(word);
    }
    if (CommonFileTypes.getSuffixPattern().matcher(word).matches()) {
      return true;
    }
    if (missingAdjPattern.matcher(word).matches()) {
      String firstPart = StringTools.uppercaseFirstChar(word.replaceFirst(adjSuffix + "(er|es|en|em|e)?", ""));
      // We append "test" to see if the word plus "test" is accepted as a compound. This way, we get the
      // infix 's" handled properly (e.g. "arbeitsartig" is okay, "arbeitartig" is not). It does not accept
      // all compounds, though, as hunspell's compound detection is limited ("Zwiebacktest"):
      // TODO: see isNeedingFugenS()
      // https://www.sekada.de/korrespondenz/rechtschreibung/artikel/grammatik-in-diesen-faellen-steht-das-fugen-s/
      /*if (!isMisspelled(firstPart) && !isMisspelled(firstPart + "test")) {
        System.out.println("accept1: " + word + " [" + !isMisspelled(word) + "]");
        //return true;
      } else if (firstPart.endsWith("s") && !isMisspelled(firstPart.replaceFirst("s$", "")) && !isMisspelled(firstPart + "test")) { // "handlungsartig"
        System.out.println("accept2: " + word + " [" + !isMisspelled(word) + "]");
        //return true;
      }*/
      if (isMisspelled(word)) {
        if (!isMisspelled(firstPart) &&
            !firstPart.matches(".{3,25}(tum|ing|ling|heit|keit|schaft|ung|ion|tät|at|um)") &&
            isOnlyNoun(firstPart) &&
            !isMisspelled(firstPart + "test")) {  // does hunspell accept this? takes infex-s into account automatically
          //System.out.println("will accept: " + word);
          return true;
        } else if (!isMisspelled(firstPart) &&
                   !firstPart.matches(".{3,25}(tum|ing|ling|heit|keit|schaft|ung|ion|tät|at|um)")) {
                   //System.out.println("will not accept: " + word);
        } else if (firstPart.endsWith("s") && !isMisspelled(firstPart.replaceFirst("s$", "")) &&
                   firstPart.matches(".{3,25}(tum|ing|ling|heit|keit|schaft|ung|ion|tät|at|um)s") &&   // "handlungsartig"
                   isOnlyNoun(firstPart.replaceFirst("s$", "")) &&
                   !isMisspelled(firstPart + "test")) {  // does hunspell accept this? takes infex-s into account automatically
          //System.out.println("will accept: " + word);
          return true;
        } else if (firstPart.endsWith("s") && !isMisspelled(firstPart.replaceFirst("s$", "")) &&
                   firstPart.matches(".{3,25}(tum|ing|ling|heit|keit|schaft|ung|ion|tät|at|um)s")) {
          //System.out.println("will not accept: " + word);
        }
      }
    }
    if ((idx+1 < words.size() && (word.endsWith(".mp") || word.endsWith(".woff")) && words.get(idx+1).equals("")) ||
        (idx > 0 && "".equals(words.get(idx-1)) && StringUtils.equalsAny(word, "sat", "stel", "tel", "stels", "tels") )) {
      // e.g. ".mp3", "3sat", "100stel", "5tel" - the check for the empty string is because digits were removed during
      // hunspell-style tokenization before
      return true;
    }
    return ignore || ignoreUncapitalizedWord || ignoreBulletPointCase || ignoreByHyphen || ignoreHyphenatedCompound || ignoreElative(word);
  }

  @Override
  protected List<SuggestedReplacement> getAdditionalTopSuggestions(List<SuggestedReplacement> suggestions, String word) throws IOException {
    List<String> suggestionsList = suggestions.stream()
      .map(SuggestedReplacement::getReplacement).collect(Collectors.toList());
    return SuggestedReplacement.convert(getAdditionalTopSuggestionsString(suggestionsList, word));
  }

  private List<String> getAdditionalTopSuggestionsString(List<String> suggestions, String word) throws IOException {
    String suggestion;
    if ("WIFI".equalsIgnoreCase(word)) {
      return singletonList("Wi-Fi");
    } else if ("W-Lan".equalsIgnoreCase(word)) {
      return singletonList("WLAN");
    } else if ("genomen".equals(word)) {
      return singletonList("genommen");
    } else if ("Preis-Leistungsverhältnis".equals(word)) {
      return singletonList("Preis-Leistungs-Verhältnis");
    } else if ("ausversehen".equals(word)) {
      return singletonList("aus Versehen");
    } else if ("getz".equals(word)) {
      return Arrays.asList("jetzt", "geht's");
    } else if ("Trons".equals(word)) {
      return singletonList("Trance");
    } else if ("ei".equals(word)) {
      return singletonList("ein");
    } else if ("jo".equals(word) || "jepp".equals(word) || "jopp".equals(word)) {
      return singletonList("ja");
    } else if ("Jo".equals(word) || "Jepp".equals(word) || "Jopp".equals(word)) {
      return singletonList("Ja");
    } else if ("Ne".equals(word)) {
      // "Ne einfach Frage!"
      // "Ne, das musst du machen!"
      return Arrays.asList("Nein", "Eine");
    } else if ("ne".equals(word)) {
      // "Das warst du, ne?"
      // "Das ist ne einfache Aufgabe!"
      // "Ne das würde ich anders machen."
      return Arrays.asList("nein", "eine", "oder");
    } else if ("is".equals(word)) {
      return singletonList("ist");
    } else if ("Is".equals(word)) {
      return singletonList("Ist");
    } else if ("un".equals(word)) {
      return singletonList("und");
    } else if ("Un".equals(word)) {
      return singletonList("Und");
    } else if ("Std".equals(word)) {
      return singletonList("Std.");
    } else if (word.matches(".*ibel[hk]eit$")) {
      suggestion = word.replaceFirst("el[hk]eit$", "ilität");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.endsWith("aquise")) {
      suggestion = word.replaceFirst("aquise$", "akquise");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.endsWith("standart")) {
      suggestion = word.replaceFirst("standart$", "standard");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.endsWith("standarts")) {
      suggestion = word.replaceFirst("standarts$", "standards");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.endsWith("tips")) {
      suggestion = word.replaceFirst("tips$", "tipps");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.endsWith("tip")) {
      suggestion = word + "p";
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.endsWith("entfehlung")) {
      suggestion = word.replaceFirst("ent", "emp");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.endsWith("oullie")) {
      suggestion = word.replaceFirst("oullie$", "ouille");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.startsWith("[dD]urschnitt")) {
      suggestion = word.replaceFirst("^urschnitt", "urchschnitt");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.startsWith("Bundstift")) {
      suggestion = word.replaceFirst("^Bundstift", "Buntstift");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.matches("[aA]llmähll?i(g|ch)(e[mnrs]?)?")) {
      suggestion = word.replaceFirst("llmähll?i(g|ch)", "llmählich");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.matches(".*[mM]a[jy]onn?[äe]se.*")) {
      suggestion = word.replaceFirst("a[jy]onn?[äe]se", "ayonnaise");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.matches(".*[rR]es(a|er)[vw]i[he]?rung(en)?")) {
      suggestion = word.replaceFirst("es(a|er)[vw]i[he]?rung", "eservierung");
      if (hunspell.spell(suggestion)) { // suggest e.g. 'Ticketreservierung', but not 'Blödsinnsquatschreservierung'
        return singletonList(suggestion);
      }
    } else if (word.matches("[rR]eschaschier.+")) {
      suggestion = word.replaceFirst("schaschier", "cherchier");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.matches(".*[lL]aborants$")) {
      suggestion = word.replaceFirst("ts$", "ten");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.matches("[pP]roff?ess?ion([äe])h?ll?(e[mnrs]?)?")) {
      suggestion = word.replaceFirst("roff?ess?ion([äe])h?l{1,2}", "rofessionell");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.matches("[vV]erstehendniss?(es?)?")) {
      suggestion = word.replaceFirst("[vV]erstehendnis", "Verständnis");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.startsWith("koregier")) {
      suggestion = word.replace("reg", "rrig");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.matches("diagno[sz]ier.*")) {
      suggestion = word.replaceAll("gno[sz]ier", "gnostizier");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.contains("eiss")) {
      suggestion = word.replace("eiss", "eiß");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.contains("uess")) {
      suggestion = word.replace("uess", "üß");
      if (hunspell.spell(suggestion)) {
        return singletonList(suggestion);
      }
    } else if (word.equals("gin")) {
      return singletonList("ging");
    } else if (word.equals("dh") || word.equals("dh.")) {
      return singletonList("d.\u202fh.");
    } else if (word.equals("ua") || word.equals("ua.")) {
      return singletonList("u.\u202fa.");
    } else if (word.matches("z[bB]") || word.matches("z[bB].")) {
      return singletonList("z.\u202fB.");
    } else if (word.equals("uvm") || word.equals("uvm.")) {
      return singletonList("u.\u202fv.\u202fm.");
    } else if (word.equals("udgl") || word.equals("udgl.")) {
      return singletonList("u.\u202fdgl.");
    } else if (word.equals("Ruhigkeit")) {
      return singletonList("Ruhe");
    } else if (word.equals("angepreist")) {
      return singletonList("angepriesen");
    } else if (word.equals("halo")) {
      return singletonList("hallo");
    } else if (word.equalsIgnoreCase("zumindestens")) {
      return singletonList(word.replace("ens", ""));
    } else if (word.equals("ca")) {
      return singletonList("ca.");
    } else if (word.equals("Jezt")) {
      return singletonList("Jetzt");
    } else if (word.equals("Wollst")) {
      return singletonList("Wolltest");
    } else if (word.equals("wollst")) {
      return singletonList("wolltest");
    } else if (word.equals("Rolladen")) {
      return singletonList("Rollladen");
    } else if (word.equals("Maßname")) {
      return singletonList("Maßnahme");
    } else if (word.equals("Maßnamen")) {
      return singletonList("Maßnahmen");
    } else if (word.equals("nanten")) {
      return singletonList("nannten");
    } else if (word.endsWith("ies")) {
      if (word.equals("Stories")) {
        return singletonList("Storys");
      } else if (word.equals("Lobbies")) {
        return singletonList("Lobbys");
      } else if (word.equals("Hobbies")) {
        return singletonList("Hobbys");
      } else if (word.equals("Parties")) {
        return singletonList("Partys");
      } else if (word.equals("Babies")) {
        return singletonList("Babys");
      } else if (word.equals("Ladies")) {
        return singletonList("Ladys");
      } else if (word.endsWith("derbies")) {
        suggestion = word.replaceFirst("derbies$", "derbys");
        if (hunspell.spell(suggestion)) {
          return singletonList(suggestion);
        }
      } else if (word.endsWith("stories")) {
        suggestion = word.replaceFirst("stories$", "storys");
        if (hunspell.spell(suggestion)) {
          return singletonList(suggestion);
        }
      } else if (word.endsWith("parties")) {
        suggestion = word.replaceFirst("parties$", "partys");
        if (hunspell.spell(suggestion)) {
          return singletonList(suggestion);
        }
      }
    } else if (word.equals("Hallochen")) {
      return Arrays.asList("Hallöchen", "hallöchen");
    } else if (word.equals("hallochen")) {
      return singletonList("hallöchen");
    } else if (word.equals("ok")) {
      return Arrays.asList("okay", "O.\u202fK."); // Duden-like suggestion with no-break space
    } else if (word.equals("gesuchen")) {
      return Arrays.asList("gesuchten", "gesucht");
    } else if (word.equals("Germanistiker")) {
      return Arrays.asList("Germanist", "Germanisten");
    } else if (word.equals("Abschlepper")) {
      return Arrays.asList("Abschleppdienst", "Abschleppwagen");
    } else if (word.equals("par")) {
      return singletonList("paar");
    } else if (word.equals("vllt")) {
      return singletonList("vielleicht");
    } else if (word.equals("iwie")) {
      return singletonList("irgendwie");
    } else if (word.equals("bzgl")) {
      return singletonList("bzgl.");
    } else if (word.equals("bau")) {
      return singletonList("baue");
    } else if (word.equals("sry")) {
      return singletonList("sorry");
    } else if (word.equals("Sry")) {
      return singletonList("Sorry");
    } else if (word.equals("thx")) {
      return singletonList("danke");
    } else if (word.equals("Thx")) {
      return singletonList("Danke");
    } else if (word.equals("Zynik")) {
      return singletonList("Zynismus");
    } else if (word.equalsIgnoreCase("email")) {
      return singletonList("E-Mail");
    } else if (word.length() > 9 && word.startsWith("Email")) {
      String suffix = word.substring(5);
      if (!hunspell.spell(suffix)) {
        List<String> suffixSuggestions = hunspell.suggest(uppercaseFirstChar(suffix));
        suffix = suffixSuggestions.isEmpty() ? suffix : suffixSuggestions.get(0);
      }
      return singletonList("E-Mail-"+Character.toUpperCase(suffix.charAt(0))+suffix.substring(1));
    } else if (word.equals("wiederspiegeln")) {
      return singletonList("widerspiegeln");
    } else if (word.equals("ch")) {
      return singletonList("ich");
    } else {
      for (Map.Entry<StringMatcher, Function<String, List<String>>> entry : ADDITIONAL_SUGGESTIONS.entrySet()) {
        if (entry.getKey().matches(word)) {
          return entry.getValue().apply(word);
        }
      }
    }
    if (!startsWithUppercase(word)) {
      String ucWord = uppercaseFirstChar(word);
      if (!suggestions.contains(ucWord) && hunspell.spell(ucWord) && !ucWord.endsWith(".")) {
        // Hunspell doesn't always automatically offer the most obvious suggestion for compounds:
        return singletonList(ucWord);
      }
    }
    String verbSuggestion = getPastTenseVerbSuggestion(word);
    if (verbSuggestion != null) {
      return singletonList(verbSuggestion);
    }
    String participleSuggestion = getParticipleSuggestion(word);
    if (participleSuggestion != null) {
      return singletonList(participleSuggestion);
    }
    String abbreviationSuggestion = getAbbreviationSuggestion(word);
    if (abbreviationSuggestion != null) {
      return singletonList(abbreviationSuggestion);
    }
    // hyphenated compounds words (e.g., "Netflix-Flm")
    if (suggestions.isEmpty() && word.contains("-")) {
      String[] words = word.split("-");
      if (words.length > 1) {
        List<List<String>> suggestionLists = new ArrayList<>(words.length);
        int startAt = 0;
        int stopAt = words.length;
        String partialWord = words[0] + "-" + words[1];
        if (super.ignoreWord(partialWord) || wordsToBeIgnoredInCompounds.contains(partialWord)) { // "Au-pair-Agentr"
          startAt = 2;
          suggestionLists.add(singletonList(words[0] + "-" + words[1]));
        }
        partialWord = words[words.length-2] + "-" + words[words.length-1];
        if (super.ignoreWord(partialWord) || wordsToBeIgnoredInCompounds.contains(partialWord)) { // "Seniren-Au-pair"
          stopAt = words.length-2;
        }
        for (int idx = startAt; idx < stopAt; idx++) {
          if (!hunspell.spell(words[idx])) {
            List<String> list = sortSuggestionByQuality(words[idx], super.getSuggestions(words[idx]));
            suggestionLists.add(list);
          } else {
            suggestionLists.add(singletonList(words[idx]));
          }
        }
        if (stopAt < words.length-1) {
          suggestionLists.add(singletonList(partialWord));
        }
        if (suggestionLists.size() <= 3) {  // avoid OutOfMemory on words like "free-and-open-source-and-cross-platform"
          List<String> additionalSuggestions = suggestionLists.get(0);
          for (int idx = 1; idx < suggestionLists.size(); idx++) {
            List<String> suggestionList = suggestionLists.get(idx);
            List<String> newList = new ArrayList<>(additionalSuggestions.size() * suggestionList.size());
            for (String additionalSuggestion : additionalSuggestions) {
              for (String aSuggestionList : suggestionList) {
                newList.add(additionalSuggestion + "-" + aSuggestionList);
              }
            }
            additionalSuggestions = newList;
          }
          // avoid overly long lists of suggestions (we just take the first results, although we don't know whether they are better):
          return additionalSuggestions.subList(0, Math.min(5, additionalSuggestions.size()));
        }
      }
    }
    return Collections.emptyList();
  }

  // Get a correct suggestion for invalid words like greifte, denkte, gehte: useful for
  // non-native speakers and cannot be found by just looking for similar words.
  @Nullable
  private String getPastTenseVerbSuggestion(String word) {
    if (word.endsWith("e")) {
      // strip trailing "e"
      String wordStem = word.substring(0, word.length()-1);
      try {
        String lemma = baseForThirdPersonSingularVerb(wordStem);
        if (lemma != null) {
          AnalyzedToken token = new AnalyzedToken(lemma, null, lemma);
          String[] forms = synthesizer.synthesize(token, "VER:3:SIN:PRT:.*", true);
          if (forms.length > 0) {
            return forms[0];
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  @Nullable
  private String baseForThirdPersonSingularVerb(String word) throws IOException {
    List<AnalyzedTokenReadings> readings = tagger.tag(singletonList(word));
    for (AnalyzedTokenReadings reading : readings) {
      if (reading.hasPosTagStartingWith("VER:3:SIN")) {
        return reading.getReadings().get(0).getLemma();
      }
    }
    return null;
  }

  // Get a correct suggestion for invalid words like geschwimmt, geruft: useful for
  // non-native speakers and cannot be found by just looking for similar words.
  @Nullable
  private String getParticipleSuggestion(String word) {
    if (word.startsWith("ge") && word.endsWith("t")) {
      // strip leading "ge" and replace trailing "t" with "en":
      String baseform = word.substring(2, word.length()-1) + "en";
      try {
        String participle = getParticipleForBaseform(baseform);
        if (participle != null) {
          return participle;
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  @Nullable
  private String getParticipleForBaseform(String baseform) throws IOException {
    AnalyzedToken token = new AnalyzedToken(baseform, null, baseform);
    String[] forms = synthesizer.synthesize(token, "VER:PA2:.*", true);
    if (forms.length > 0 && hunspell.spell(forms[0])) {
      return forms[0];
    }
    return null;
  }

  private String getAbbreviationSuggestion(String word) throws IOException {
    if (word.length() < 5) {
      List<AnalyzedTokenReadings> readings = tagger.tag(singletonList(word));
      for (AnalyzedTokenReadings reading : readings) {
        if (reading.hasPosTagStartingWith("ABK")) {
          return word+".";
        }
      }
    }
    return null;
  }

  private boolean ignoreByHangingHyphen(List<String> words, int idx) throws IOException {
    String word = words.get(idx);
    String nextWord = getWordAfterEnumerationOrNull(words, idx+1);
    nextWord = removeEnd(nextWord, ".");
    boolean isCompound = nextWord != null &&
      (compoundTokenizer.tokenize(nextWord).size() > 1 ||
       nextWord.indexOf('-') > 0 ||
       nextWord.matches("[A-ZÖÄÜ][a-zöäüß]{2,}(ei|öl)$"));  // compound tokenizer will only split compounds where each part is >= 3 characters...
    if (isCompound) {
      word = removeEnd(word, "-");
      boolean isMisspelled = !hunspell.spell(word);  // "Stil- und Grammatikprüfung" or "Stil-, Text- und Grammatikprüfung"
      if (isMisspelled && (super.ignoreWord(word) || wordsToBeIgnoredInCompounds.contains(word))) {
        isMisspelled = false;
      } else if (isMisspelled && word.endsWith("s") && isNeedingFugenS(removeEnd(word, "s"))) {
        // Vertuschungs- und Bespitzelungsmaßnahmen: remove trailing "s" before checking "Vertuschungs" so that the spell checker finds it
        isMisspelled = !hunspell.spell(removeEnd(word, "s"));
      }
      return !isMisspelled;
    }
    return false;
  }

  private boolean isNeedingFugenS(String word) {
    // according to http://www.spiegel.de/kultur/zwiebelfisch/zwiebelfisch-der-gebrauch-des-fugen-s-im-ueberblick-a-293195.html
    return StringUtils.endsWithAny(word, "tum", "ling", "ion", "tät", "keit", "schaft", "sicht", "ung", "en");
  }

  // for "Stil- und Grammatikprüfung", get "Grammatikprüfung" when at position of "Stil-"
  @Nullable
  private String getWordAfterEnumerationOrNull(List<String> words, int idx) {
    for (int i = idx; i < words.size(); i++) {
      String word = words.get(i);
      if (!(word.endsWith("-") || StringUtils.equalsAny(word, ",", "und", "oder", "sowie") || word.trim().isEmpty())) {
        return word;
      }
    }
    return null;
  }

  // check whether a <code>word<code> is a valid compound (e.g., "Feynmandiagramm" or "Feynman-Diagramm")
  // that contains an ignored word from spelling.txt (e.g., "Feynman")
  private boolean ignoreCompoundWithIgnoredWord(String word) throws IOException {
    if (!startsWithUppercase(word) && !StringUtils.startsWithAny(word, "nord", "west", "ost", "süd")) {
      // otherwise stuff like "rumfangreichen" gets accepted
      return false;
    }
    String[] words = word.split("-");
    if (words.length < 2) {
      // non-hyphenated compound (e.g., "Feynmandiagramm"):
      // only search for compounds that start(!) with a word from spelling.txt
      int end = super.startsWithIgnoredWord(word, true);
      if (end < 3) {
        // support for geographical adjectives - although "süd/ost/west/nord" are not in spelling.txt
        // to accept sentences such as
        // "Der westperuanische Ferienort, das ostargentinische Städtchen, das südukrainische Brauchtum, der nordägyptische Staudamm."
        if (word.startsWith("ost") || word.startsWith("süd")) {
          end = 3;
        } else if (word.startsWith("west") || word.startsWith("nord")) {
          end = 4;
        } else {
          return false;
        }
      }
      String ignoredWord = word.substring(0, end);
      String partialWord = word.substring(end);
      partialWord = partialWord.endsWith(".") ? partialWord.substring(0, partialWord.length()-1) : partialWord;
      boolean isCandidateForNonHyphenatedCompound = !StringUtils.isAllUpperCase(ignoredWord) && (StringUtils.isAllLowerCase(partialWord) || ignoredWord.endsWith("-"));
      boolean needFugenS = isNeedingFugenS(ignoredWord);
      if (isCandidateForNonHyphenatedCompound && !needFugenS && partialWord.length() > 2) {
        return hunspell.spell(partialWord) || hunspell.spell(StringUtils.capitalize(partialWord));
      } else if (isCandidateForNonHyphenatedCompound && needFugenS && partialWord.length() > 2) {
        partialWord = partialWord.startsWith("s") ? partialWord.substring(1) : partialWord;
        return hunspell.spell(partialWord) || hunspell.spell(StringUtils.capitalize(partialWord));
      }
      return false;
    }
    // hyphenated compound (e.g., "Feynman-Diagramm"):
    boolean hasIgnoredWord = false;
    List<String> toSpellCheck = new ArrayList<>(3);
    String stripFirst = word.substring(words[0].length()+1); // everything after the first "-"
    String stripLast  = word.substring(0, word.length()-words[words.length-1].length()-1); // everything up to the last "-"

    if (super.ignoreWord(stripFirst) || wordsToBeIgnoredInCompounds.contains(stripFirst)) { // e.g., "Senioren-Au-pair"
      hasIgnoredWord = true;
      if (!super.ignoreWord(words[0])) {
        toSpellCheck.add(words[0]);
      }
    } else if (super.ignoreWord(stripLast) || wordsToBeIgnoredInCompounds.contains(stripLast)) { // e.g., "Au-pair-Agentur"
      hasIgnoredWord = true;
      if (!super.ignoreWord(words[words.length-1])){
        toSpellCheck.add(words[words.length-1]);
      }
    } else {
      for (String word1 : words) {
        if (super.ignoreWord(word1) || wordsToBeIgnoredInCompounds.contains(word1)) {
          hasIgnoredWord = true;
        } else {
          toSpellCheck.add(word1);
        }
      }
    }

    if (hasIgnoredWord) {
      for (String w : toSpellCheck) {
        if (!hunspell.spell(w)) {
          return false;
        }
      }
    }
    return hasIgnoredWord;
  }

  static class ExpandingReader extends BufferedReader {

    private final List<String> buffer = new ArrayList<>();
    private final LineExpander lineExpander = new LineExpander();

    ExpandingReader(Reader in) {
      super(in);
    }

    @Override
    public String readLine() throws IOException {
      if (buffer.isEmpty()) {
        String line = super.readLine();
        if (line == null) {
          return null;
        }
        buffer.addAll(lineExpander.expandLine(line));
      }
      return buffer.remove(0);
    }
  }

  @Override
  protected boolean isQuotedCompound(AnalyzedSentence analyzedSentence, int idx, String token) {
    if (idx > 3 && token.startsWith("-")) {
      return StringUtils.equalsAny(analyzedSentence.getTokens()[idx-1].getToken(), "“", "\"") &&
          StringUtils.equalsAny(analyzedSentence.getTokens()[idx-3].getToken(), "„", "\"");
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.languagetool.rules.spelling.SpellingCheckRule#addProhibitedWords(java.util.List)
   */
  @Override
  protected void addProhibitedWords(List<String> words) {
    if (words.size() == 1 && words.get(0).endsWith(".*")) {
      wordStartsToBeProhibited.add(words.get(0).substring(0, words.get(0).length()-2));
    } else if (words.get(0).startsWith(".*")) {
      words.stream().forEach(word -> wordEndingsToBeProhibited.add(word.substring(2)));
    } else {
      super.addProhibitedWords(words);
    }
  }

  @Override
  protected List<SuggestedReplacement> filterNoSuggestWords(List<SuggestedReplacement> l) {
    return l.stream().filter(k -> !lcDoNotSuggestWords.contains(k.getReplacement().toLowerCase())).collect(Collectors.toList());
  }

  @Override
  protected List<SuggestedReplacement> getOnlySuggestions(String word) {
    if (word.matches("[Aa]utentisch(e[nmsr]?|ste[nmsr]?|ere[nmsr]?)?")) {
      return topMatch(word.replaceFirst("utent", "uthent"));
    }
    switch (word) {
      case "daß": return topMatch("dass");
      case "mußt": return topMatch("musst");
      case "mußten": return topMatch("mussten");
      case "mußte": return topMatch("musste");
      case "mußtest": return topMatch("musstest");
      case "müßtest": return topMatch("müsstest");
      case "müßen": return topMatch("müssen");
      case "müßten": return topMatch("müssten");
      case "müßte": return topMatch("müsste");
      case "Daß": return topMatch("Dass");
      case "bescheid": return topMatch("Bescheid");
      case "ausversehen": return topMatch("aus Versehen");
      case "Luxenburg": return topMatch("Luxemburg");
      case "Luxenburgs": return topMatch("Luxemburgs");
      case "Lichtenstein": return topMatch("Liechtenstein");
      case "Lichtensteins": return topMatch("Liechtensteins");
      case "immernoch": return topMatch("immer noch");
      case "Rechtshcreibfehler": return topMatch("Rechtschreibfehler"); // for demo text on home page
      case "markirt": return topMatch("markiert"); // for demo text on home page
      case "Johannesbeere": return topMatch("Johannisbeere");
      case "Johannesbeeren": return topMatch("Johannisbeeren");
      case "Endgeld": return topMatch("Entgeld");
      case "Entäuschung": return topMatch("Enttäuschung");
      case "Entäuschungen": return topMatch("Enttäuschungen");
      case "Triologie": return topMatch("Trilogie", "Werk (z.B. Film), das aus drei Teilen besteht");
    }
    return Collections.emptyList();
  }

}
