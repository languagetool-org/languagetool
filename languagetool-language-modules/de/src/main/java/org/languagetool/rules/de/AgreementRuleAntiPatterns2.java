/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;

import java.util.Arrays;
import java.util.List;

import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.*;

class AgreementRuleAntiPatterns2 {

  static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      token("Eurovision"),
      token("Song"),
      token("Contest")
    ),
    Arrays.asList(
      token("Account"),
      tokenRegex("Managers?")
    ),
    Arrays.asList(
      token("Wohnungsbau"),
      token("Aalen")
    ),
    Arrays.asList(
      token("Display"),
      tokenRegex("Ads?|Advertising")
    ),
    Arrays.asList(
      token("Private"),
      tokenRegex("Equitys?|Clouds?")
    ),
    Arrays.asList(
      token("Personal"),
      tokenRegex("Agents?|Computers?|Data|Firewalls?")
    ),
    Arrays.asList(
      token("Junge"),
      tokenRegex("Union|Freiheit|Welt|Europäische|Alternative|Volkspartei|Akademie")
    ),
    Arrays.asList( // "Das Holocaust Memorial Museum."
      posRegex("ART:.+"),
      posRegex("SUB:.+"),
      pos("UNKNOWN")
    ),
    Arrays.asList( // "Er fragte, ob das Spaß macht."
      csToken(","),
      posRegex("KON:UNT|ADV:INR"),
      csToken("das"),
      posRegex("SUB:.+"),
      posRegex("VER:3:SIN.*")
    ),
    Arrays.asList( // "Es gibt viele solcher Bilder"
      tokenRegex("viele|wenige|einige|mehrere"),
      csToken("solcher"),
      posRegex("SUB:GEN:PLU:.*")
    ),
    Arrays.asList( // "der französischen First Lady"
      tokenRegex("[dD](ie|er)"),
      csToken("First"),
      csToken("Lady")
    ),
    Arrays.asList( // "der französischen First Lady"
      tokenRegex("[dD](ie|er)"),
      posRegex("ADJ:.*"),
      csToken("First"),
      csToken("Lady")
    ),
    Arrays.asList( // "der Super Nintendo"
      tokenRegex("[dD](ie|er)"),
      csToken("Super"),
      csToken("Nintendo")
    ),
    Arrays.asList( // Firmenname
      csToken("Pizza"),
      csToken("Hut")
    ),
    Arrays.asList( // Texas und New Mexico, beides spanische Kolonien, sind
      csToken(","),
      csToken("beides"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:PLU.+").min(0).build(),
      posRegex("SUB:NOM:PLU.+"),
      pos("PKT")
    ),
    Arrays.asList(    // Beides Teenager mit verrückten Ideen
      pos("SENT_START"),
      csToken("Beides"),
      posRegex("SUB:NOM:PLU.+")
    ),
    Arrays.asList(
      tokenRegex("[dD]e[rn]"),
      csToken("Gold"),
      csToken("Cup")
    ),
    Arrays.asList(
      token("das"),
      tokenRegex("viele|wenige"),
      posRegex("SUB:.*")
    ),
    Arrays.asList(
      token("das"),
      posRegex("SUB:.+"),
      new PatternTokenBuilder().csToken("dauern").matchInflectedForms().build()
    ),
    Arrays.asList( // "Er verspricht allen/niemandem/jedem hohe Gewinne."
      tokenRegex("allen|(nieman|je(man)?)dem"),
      posRegex("ADJ:AKK:PLU:.*"),
      posRegex("SUB:AKK:PLU:.*")
    ),
    Arrays.asList( // "Er verspricht allen/niemandem/jedem Gewinne von über 15 Prozent."
      tokenRegex("allen|(nieman|je(man)?)dem"),
      posRegex("SUB:AKK:PLU:.*")
    ),
    Arrays.asList( // "Für ihn ist das Alltag." / "Für die Religiösen ist das Blasphemie und führt zu Aufständen."
      new PatternTokenBuilder().posRegex("PRP:.+|ADV:MOD").setSkip(2).build(),
      new PatternTokenBuilder().token("sein").matchInflectedForms().build(),
      csToken("das"),
      posRegex("SUB:NOM:.*"),
      posRegex("PKT|SENT_END|KON.*")
    ),
    Arrays.asList( // "Meistens sind das faktenfreie Behauptungen."
      regex("sind|waren|wären"),
      csToken("das"),
      posRegex("ADJ:NOM:PLU.*"),
      posRegex("SUB:NOM:PLU.*"),
      posRegex("PKT|KON.*")
    ),
    Arrays.asList( // "Meistens ist das reine Formsache."
      regex("ist|war|wär"),
      csToken("das"),
      posRegex("ADJ:NOM:SIN.*"),
      posRegex("SUB:NOM:SIN.*"),
      posRegex("PKT|KON.*")
    ),
    Arrays.asList( // "Aber ansonsten ist das erste Sahne"
      new PatternTokenBuilder().token("sein").matchInflectedForms().build(),
      csToken("das"),
      csToken("erste"),
      csToken("Sahne")
    ),
    Arrays.asList( // "Sie sagte, dass das Rache bedeuten würden", "Sie werden merken, dass das echte Nutzer sind."
      pos("KON:UNT"),
      csToken("das"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:.+"),
      new PatternTokenBuilder().tokenRegex("bedeuten|sein").matchInflectedForms().build()
    ),
    Arrays.asList( // "Sie fragte, ob das wirklich Rache bedeuten würde"
      pos("KON:UNT"),
      csToken("das"),
      pos("ADV:MOD"),
      posRegex("SUB:.+"),
      new PatternTokenBuilder().tokenRegex("bedeuten|sein").matchInflectedForms().build()
    ),
    Arrays.asList( // "Karl sagte, dass sie niemandem Bescheid gegeben habe."
      new PatternTokenBuilder().token("niemand").matchInflectedForms().build(),
      posRegex("SUB:.+")
    ),
    Arrays.asList(
      token("alles"),
      csToken("Walzer")
    ),
    Arrays.asList( // "ei der Daus"
      csToken("der"),
      csToken("Daus")
    ),
    Arrays.asList( // "Das Orange ist meine Lieblingsfarbe"
      posRegex("PRO:...:...:SIN:NEU.*"),
      csToken("Orange")
    ),
    Arrays.asList( // "Dieses rötliche Orange gefällt mir am besten"
      posRegex("PRO:...:...:SIN:NEU.*"),
      posRegex("ADJ:.+"),
      csToken("Orange")
    ),
    Arrays.asList(
      csToken("dem"),
      new PatternTokenBuilder().csToken("Achtung").setSkip(1).build(),
      new PatternTokenBuilder().csToken("schenken").matchInflectedForms().build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("schenken").matchInflectedForms().build(),
      csToken("dem"),
      csToken("Achtung")
    ),
    Arrays.asList(
      csToken("dem"),
      new PatternTokenBuilder().csToken("Rechnung").setSkip(1).build(),
      new PatternTokenBuilder().csToken("tragen").matchInflectedForms().build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("tragen").matchInflectedForms().build(),
      csToken("dem"),
      csToken("Rechnung")
    ),
    Arrays.asList(
      csToken("zum"),
      csToken("einen"),
      posRegex("ADJ:.+")
    ),
    Arrays.asList(
      token("auf"),
      csToken("die"),
      csToken("Lauer")
    ),
    Arrays.asList(
      token("dieser"),
      csToken("eine"),
      pos("SUB:NOM:SIN:MAS")
    ),
    Arrays.asList(
      token("das"),
      posRegex("SUB:DAT:.+"),
      token("vorbehalten")
    ),
    Arrays.asList( // Wenn hier einer Geld hat, dann ich.
      new PatternTokenBuilder().token("wenn").setSkip(1).build(),
      csToken("einer"),
      posRegex("SUB:AKK:.+"),
      posRegex("VER:(MOD:)?3:SIN:.+"),
      csToken(",")
    ),
    Arrays.asList( // Es ist nicht eines jeden Bestimmung
      tokenRegex("eine[rs]"),
      tokenRegex("jed(wed)?en")
    ),
    Arrays.asList( // Ich vertraue auf die Meinen.
      token("die"),
      tokenRegex("[MDS]einen")
    ),
    Arrays.asList( // Sie ist über die Maßen schön.
      csToken("über"),
      csToken("die"),
      csToken("Maßen")
    ),
    Arrays.asList( // Was nützt einem Gesundheit, wenn man sonst ein Idiot ist?
      token("was"),
      new PatternTokenBuilder().csToken("nützen").matchInflectedForms().build(),
      csToken("einem"),
      posRegex("SUB:NOM:.+")
    ),
    Arrays.asList( // Auch das hat sein Gutes.
      new PatternTokenBuilder().csToken("haben").matchInflectedForms().build(),
      csToken("sein"),
      csToken("Gutes")
    ),
    Arrays.asList( // Auch wenn es sein Gutes hatte.
      csToken("Gutes"),
      new PatternTokenBuilder().tokenRegex("haben|tun").matchInflectedForms().build()
    ),
    Arrays.asList(
      csToken("dieser"),
      csToken("einen"),
      pos("SUB:DAT:SIN:FEM")
    ),
    Arrays.asList(
      csToken("Rede"),
      csToken("und"),
      csToken("Antwort")
    ),
    Arrays.asList(
      posRegex("ABK:.+:SUB")
    ),
    Arrays.asList(
      tokenRegex("(all|je(d|glich))en"),
      csToken("Reiz")
    ),
    Arrays.asList(
      tokenRegex("wieso|ob|warum|w[ae]nn"),
      token("das"),
      tokenRegex("sinn|mehrwert"),
      tokenRegex("macht|ergibt|stiftet|bringt")
    ),
    Arrays.asList(
      tokenRegex("hat|hätte|kann|wird|dürfte|muss|soll(te)?|könnte|müsste|würde"),
      token("das"),
      token("Konsequenzen")
    ),
    Arrays.asList(
      new PatternTokenBuilder().posRegex("VER:.*[1-3]:.+").setSkip(1).build(),
      csToken("vermehrt")
    ),
    Arrays.asList( // In den Ruhr Nachrichten
      csToken("Ruhr"),
      csToken("Nachrichten")
    ),
    Arrays.asList(
      csToken("Joint"),
      tokenRegex("Ventures?|Cares?")
    ),
    Arrays.asList(
      csToken("Premier"),
      csToken("League")
    ),
    Arrays.asList(
      // Common job title
      csToken("Software"),
      tokenRegex("Engineers?|Developer[sn]?|(Back|Front)end")
    ),
    Arrays.asList(
      csToken("Mark"),
      posRegex("EIG:.*")
    ),
    Arrays.asList(
      csToken("Sales"),
      tokenRegex("Agent")
    ),
    Arrays.asList(
      csToken("Total"),
      tokenRegex("Tankstellen?")
    ),
    Arrays.asList(
      csToken("Real"),
      tokenRegex("Madrid|Valladolid|Mallorca")
    ),
    Arrays.asList( // Eng.
      csToken("Real"),
      pos("UNKNOWN")
    ),
    Arrays.asList(
      csToken("Hammer"),
      tokenRegex("Stra(ß|ss)e")
    ),
    Arrays.asList( // https://www.duden.de/rechtschreibung/Personal_Trainer
      csToken("Personal"),
      tokenRegex("Trainers?")
    ),
    Arrays.asList( // Ich wollte erstmal allen Hallo sagen.
      token("Hallo"),
      new PatternTokenBuilder().csToken("sagen").matchInflectedForms().build()
    ),
    Arrays.asList( // "ob die Deutsch sprechen"
      token("die"),
      tokenRegex("Deutsch|Englisch|Spanisch|Französisch|Russisch|Polnisch|Holländisch|Niederländisch|Portugiesisch"),
      new PatternTokenBuilder().csToken("sprechen").matchInflectedForms().build()
    ),
    Arrays.asList( // "Ein Trainer, der zum einen Fußballspiele sehr gut lesen und analysieren kann"
      token("zum"),
      token("einen"),
      posRegex("SUB:.*")
    ),
    Arrays.asList( // https://www.duden.de/suchen/dudenonline/Fake%20News
      csToken("Fake"),
      posRegex("News")
    ),
    Arrays.asList(
      tokenRegex("Steinberg|Park"),
      csToken("Apotheke")
    ),
    Arrays.asList(
      csToken("IT"),
      csToken("Finanzmagazin")
    ),
    Arrays.asList(
      csToken("Golden"),
      csToken("Gate")
    ),
    Arrays.asList( // Vielen Dank fürs Bescheid geben
      token("fürs"),
      token("Bescheid"),
      tokenRegex("geben|sagen")
    ),
    Arrays.asList( // "Los" ist ein deutsches Substantiv
      token("Los"),
      tokenRegex("Angeles|Zetas")
    ),
    Arrays.asList( // https://www.autozeitung.de/
      csToken("Auto"),
      csToken("Zeitung")
    ),
    Arrays.asList( // "Das letzte Mal war das Ende der..."
      csToken("Mal"),
      new PatternTokenBuilder().token("sein").matchInflectedForms().build(),
      csToken("das"),
      posRegex("SUB:NOM:.*")
    ),
    Arrays.asList(
      csToken("FC"), // Die FC Bayern München Hymne (Vorschlag macht keinen Sinn "FC-Bayern")
      csToken("Bayern")
    ),
    Arrays.asList(
      csToken("Super"),
      csToken("Mario")
    ),
    Arrays.asList(
      tokenRegex(".+"),
      tokenRegex(".+"),
      csToken("Toyota"),
      csToken("Motor"),
      tokenRegex("Corp(oration)?|Company")
    ),
    Arrays.asList(
      tokenRegex(".+"),
      tokenRegex(".+"),
      csToken("Metropolitan"),
      tokenRegex("Police|Community|City|Books")
    ),
    Arrays.asList(
      tokenRegex("Office|Microsoft"),
      csToken("365")
    ),
    Arrays.asList(
      csToken("Prinz"),
      tokenRegex("Charles|William")
    ),
    Arrays.asList(
      token(":"),
      csToken("D")
    ),
    Arrays.asList(
      tokenRegex("ist|war(en)?|sind|wird|werden"),
      csToken("das"),
      csToken("reine"),
      posRegex("SUB:NOM:.*")
    ),
    Arrays.asList( // Eine Android Watch
      csToken("Android"),
      tokenRegex("Wear|Watch(es)?|Smartwatch(es)?|OS")
    ),
    Arrays.asList( // "Bitte öffnen Sie die CAD.pdf"
      tokenRegex("\\w+"),
      new PatternTokenBuilder().token(".").setIsWhiteSpaceBefore(false).build(),
      new PatternTokenBuilder().tokenRegex("pdf|zip|jpe?g|gif|png|rar|mp[34]|mpe?g|avi|docx?|xlsx?|pptx?|html?").setIsWhiteSpaceBefore(false).build()
    ),
    Arrays.asList( // "Ich mache eine Ausbildung zur Junior Digital Marketing Managerin"
      new PatternTokenBuilder().tokenRegex("Junior|Senior").setSkip(3).build(),
      tokenRegex("Manager[ns]?|Managerin(nen)?|Developer(in)?")
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("Junior|Senior").build(),
      token("Software"),
      tokenRegex("Engineers?|Architects?|Managers?|Directors?")
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("Junior|Senior").build(),
      token("Engineering"),
      tokenRegex("Manager[ns]?|Directors?")
    ),
    Arrays.asList( // "Angel" is tagged like the "Die Angel" for fishing
      csToken("Business"),
      tokenRegex("Angel[ns]?|Cases?")
    ),
    Arrays.asList( // "des Manager Magazins"
      csToken("Manager"),
      tokenRegex("Magazins?")
    ),
    Arrays.asList(
      csToken("Junior"),
      tokenRegex("Suite[sn]?")
    ),
    Arrays.asList( // Deine Abt.
      tokenRegex("die|eine|unsere|meine|ihre|eure|diese|seine|deine"),
      csToken("Abt"),
      token("."),
      tokenRegex(".+")
    ),
    Arrays.asList(
      tokenRegex(".+"),
      tokenRegex(".+"),
      tokenRegex("Customer|User"),
      tokenRegex("Journeys?|Service")
    ),
    Arrays.asList(
      tokenRegex(".+"),
      tokenRegex(".+"),
      token("Hall"),
      token("of"),
      token("Fame")
    ),
    Arrays.asList( // Wir trinken ein kühles Blondes
      token("kühles"),
      token("Blondes")
    ),
    Arrays.asList(
      tokenRegex("Vitamin|Buchstabe"),
      tokenRegex("D|B|B[1-9]|B12")
    ),
    Arrays.asList( // "Bei uns im Krankenhaus betrifft das Operationssäle."
      new PatternTokenBuilder().token("betreffen").matchInflectedForms().build(),
      csToken("das"),
      posRegex("SUB:AKK:PLU:.*")
    ),
    Arrays.asList( // "Was für ein Narr"
      token("was"),
      csToken("für"),
      csToken("ein"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:(MAS|NEU).*").min(0).build(),
      posRegex("SUB:NOM:SIN:(MAS|NEU)")
    ),
    Arrays.asList( // "Was für ein liebe Frau"
      token("was"),
      csToken("für"),
      csToken("eine"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:FEM.*").min(0).build(),
      pos("SUB:NOM:SIN:FEM")
    ),
    Arrays.asList( // "Was war ich für ein Narr"
      token("was"),
      new PatternTokenBuilder().token("sein").matchInflectedForms().setSkip(2).build(),
      csToken("für"),
      csToken("ein"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:(MAS|NEU).*").min(0).build(),
      posRegex("SUB:NOM:SIN:(MAS|NEU)")
    ),
    Arrays.asList( // "Was war sie nur für eine dumme Person"
      token("was"),
      new PatternTokenBuilder().token("sein").matchInflectedForms().setSkip(2).build(),
      csToken("für"),
      csToken("eine"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:FEM.*").min(0).build(),
      pos("SUB:NOM:SIN:FEM")
    ),
    Arrays.asList( // "Wie viele Paar Schuhe braucht er?"
      csRegex("vielen?|wenigen?|einigen?"),
      csToken("Paar"),
      posRegex("SUB:NOM:PLU:...")
    ),
    Arrays.asList( // Dann macht das Sinn.
      csRegex("machte?|ergibt|ergab|stiftete?"),
      csToken("das"),
      csToken("Sinn")
    ),
    Arrays.asList( // Mir machte das Spaß
      csRegex("machte?"),
      csToken("das"),
      csRegex("Spa(ß|ss)|Freude")
    ),
    Arrays.asList( // Das sind beides Lichtschalter; Wasser und Luft sind beides Fluide.
      csRegex("sind|waren"),
      csToken("beides"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:NOM:PLU:.*")
    ),
    Arrays.asList( // Heinrich von der Haar (https://de.wikipedia.org/wiki/Heinrich_von_der_Haar)
      token("Heinrich"),
      token("von"),
      token("der"),
      csRegex("Haars?")
    ),
    Arrays.asList(
      token("Präsident"),
      token("Xi")
    ),
    Arrays.asList(
      token("Porsche"),
      token("Museum")
    ),
    Arrays.asList(
      token("Queen"),
      posRegex("EIG:.*")
    ),
    Arrays.asList(
      token("King"),
      posRegex("EIG:.*")
    ),
    Arrays.asList( // des Sturm und Drangs
      token("des"),
      token("Sturm"),
      token("und"),
      csRegex("Drangs?")
    ),
    Arrays.asList( // die Funke Mediengruppe
      token("die"),
      token("Funke"),
      token("Mediengruppe")
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("meinen").matchInflectedForms().setSkip(3).build(),
      csRegex("das|dies|dieses"),
      new PatternTokenBuilder().token("wirklich").min(0).build(),
      token("Ernst")
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("nehmen").matchInflectedForms().setSkip(3).build(),
      csRegex("das|dies|dieses"),
      new PatternTokenBuilder().token("wirklich").min(0).build(),
      token("Ernst")
    ),
    Arrays.asList(
      // ... dann spart das Zeit und Geld.
      new PatternTokenBuilder().csToken("sparen").matchInflectedForms().setSkip(3).build(),
      csRegex("das|dies|dieses"),
      token("Zeit"),
      token("und"),
      csRegex("Geld|Nerven")
    ),
    Arrays.asList(
      csRegex("das|es|dies"),
      csRegex("bedeutete?"),
      token("Krieg")
    ),
    Arrays.asList(
      csRegex("das|es|dies"),
      csRegex("weite"),
      token("Teile")
    ),
    Arrays.asList(
      token("hat"),
      token("das"),
      csRegex("Einfluss|Auswirkungen"),
      csRegex("auf|darauf")
    ),
    Arrays.asList( // ein Auto, das schnell fährt und in **das Menschen** gerne einsteigen
      posRegex("VER.*[123].*"),
      tokenRegex("und|oder|aber"),
      new PatternTokenBuilder().posRegex("PRP.*").min(0).build(),
      posRegex("ART:DEF.*")
    ),
    Arrays.asList( // weil man oft bei **anderen schreckliches Essen** vorgesetzt bekommt
      tokenRegex("bei|zum"),
      token("anderen"),
      posRegex("ADJ.*"),
      posRegex("SUB.*")
    ),
    Arrays.asList( // dass jeder LanguageTool benutzen sollte
      token("jeder"),
      posRegex("SUB:(AKK|DAT).*"),
      posRegex("VER.*")
    ),
    Arrays.asList( // der fließend Französisch sprechende Trudeau
      posRegex("(ART|PRO:DEM).*"),
      posRegex("ADV.*"),
      posRegex("SUB.*"),
      posRegex("PA[12].*")
    ),
    Arrays.asList( // Spricht dieser fließend Französisch
      posRegex("VER.*[123].*"),
      posRegex("(ART|PRO:DEM).*"),
      posRegex("ADV.*"),
      posRegex("SUB.*")
    ),
    Arrays.asList( // Der Deutsch Langhaar ist ein mittelgroßer Jagdhund
      token("Deutsch"),
      token("Langhaar")
    ),
    Arrays.asList( // Einige nennen das
      new PatternTokenBuilder().csToken("nennen").matchInflectedForms().build(),
      token("das"),
      posRegex("SUB:NOM:SIN:(FEM|MAS)")
    ),
    Arrays.asList( // Das ist bestimmt kein Made in Germany
      csToken("Made"),
      csToken("in"),
      csToken("Germany")
    ),
    Arrays.asList( // Des Plan de XXX
      csRegex("[A-Z].+"),
      csRegex("del?"),
      csRegex("[A-Z].+")
    ),
    Arrays.asList( // Des Plan de XXX
      csRegex("[A-Z].+"),
      csToken("de"),
      regex("l[ao]s?"),
      csRegex("[A-Z].+")
    )
  );

}
