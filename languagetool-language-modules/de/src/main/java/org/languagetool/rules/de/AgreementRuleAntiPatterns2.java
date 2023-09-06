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

import java.util.List;

import static java.util.Arrays.asList;
import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.*;

class AgreementRuleAntiPatterns2 {

  static final List<List<PatternToken>> ANTI_PATTERNS = asList(
    asList(
      token("Eurovision"),
      token("Song"),
      token("Contest")
    ),
    asList(
      token("Account"),
      tokenRegex("Managers?")
    ),
    asList(
      token("Wohnungsbau"),
      token("Aalen")
    ),
    asList(
      token("Display"),
      tokenRegex("Ads?|Advertising")
    ),
    asList(
      token("Private"),
      tokenRegex("Equitys?|Clouds?")
    ),
    asList(
      token("Personal"),
      tokenRegex("Agents?|Computers?|Data|Firewalls?")
    ),
    asList(
      token("Junge"),
      tokenRegex("Union|Freiheit|Welt|Europäische|Alternative|Volkspartei|Akademie")
    ),
    asList( // "Das Holocaust Memorial Museum."
      posRegex("ART:.+"),
      posRegex("SUB:.+"),
      pos("UNKNOWN")
    ),
    asList( // "Er fragte, ob das Spaß macht."
      csToken(","),
      posRegex("KON:UNT|ADV:INR"),
      csToken("das"),
      posRegex("SUB:.+"),
      posRegex("VER:3:SIN.*")
    ),
    asList( // "Es gibt viele solcher Bilder"
      tokenRegex("viele|etliche|wenige|einige|mehrere"),
      csToken("solcher"),
      posRegex("SUB:GEN:PLU:.*")
    ),
    asList( // "der französischen First Lady"
      tokenRegex("[dD](ie|er)"),
      csToken("First"),
      csToken("Lady")
    ),
    asList( // "der französischen First Lady"
      tokenRegex("[dD](ie|er)"),
      posRegex("ADJ:.*"),
      csToken("First"),
      csToken("Lady")
    ),
    asList( // "der Super Nintendo"
      tokenRegex("[dD](ie|er)"),
      csToken("Super"),
      csToken("Nintendo")
    ),
    asList( // Firmenname
      csToken("Pizza"),
      csToken("Hut")
    ),
    asList( // Texas und New Mexico, beides spanische Kolonien, sind
      csToken(","),
      csToken("beides"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:PLU.+").min(0).build(),
      posRegex("SUB:NOM:PLU.+"),
      pos("PKT")
    ),
    asList(    // Beides Teenager mit verrückten Ideen
      pos("SENT_START"),
      csToken("Beides"),
      posRegex("SUB:NOM:PLU.+")
    ),
    asList(
      tokenRegex("[dD]e[rn]"),
      csToken("Gold"),
      csToken("Cup")
    ),
    asList(
      token("das"),
      tokenRegex("viele|wenige"),
      posRegex("SUB:.*")
    ),
    asList(
      token("das"),
      posRegex("SUB:.+"),
      new PatternTokenBuilder().csToken("dauern").matchInflectedForms().build()
    ),
    asList( // "Er verspricht allen/niemandem/jedem hohe Gewinne."
      tokenRegex("allen|(nieman|je(man)?)dem"),
      posRegex("ADJ:AKK:PLU:.*"),
      posRegex("SUB:AKK:PLU:.*")
    ),
    asList( // "Er verspricht allen/niemandem/jedem Gewinne von über 15 Prozent."
      tokenRegex("allen|(nieman|je(man)?)dem"),
      posRegex("SUB:AKK:PLU:.*")
    ),
    asList( // "Für ihn ist das Alltag." / "Für die Religiösen ist das Blasphemie und führt zu Aufständen."
      new PatternTokenBuilder().posRegex("PRP:.+|ADV:MOD").setSkip(2).build(),
      new PatternTokenBuilder().token("sein").matchInflectedForms().build(),
      csToken("das"),
      posRegex("SUB:NOM:.*"),
      posRegex("PKT|SENT_END|KON.*")
    ),
    asList( // "Meistens sind das faktenfreie Behauptungen."
      regex("sind|waren|wären"),
      csToken("das"),
      posRegex("ADJ:NOM:PLU.*"),
      posRegex("SUB:NOM:PLU.*"),
      posRegex("PKT|KON.*")
    ),
    asList( // "Meistens ist das reine Formsache."
      regex("ist|war|wär"),
      csToken("das"),
      posRegex("ADJ:NOM:SIN.*"),
      posRegex("SUB:NOM:SIN.*"),
      posRegex("PKT|KON.*")
    ),
    asList( // "Aber ansonsten ist das erste Sahne"
      new PatternTokenBuilder().token("sein").matchInflectedForms().build(),
      csToken("das"),
      csToken("erste"),
      csToken("Sahne")
    ),
    asList( // "Sie sagte, dass das Rache bedeuten würden", "Sie werden merken, dass das echte Nutzer sind."
      pos("KON:UNT"),
      csToken("das"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:.+"),
      new PatternTokenBuilder().tokenRegex("bedeuten|sein").matchInflectedForms().build()
    ),
    asList( // "Sie fragte, ob das wirklich Rache bedeuten würde"
      pos("KON:UNT"),
      csToken("das"),
      pos("ADV:MOD"),
      posRegex("SUB:.+"),
      new PatternTokenBuilder().tokenRegex("bedeuten|sein").matchInflectedForms().build()
    ),
    asList( // "Karl sagte, dass sie niemandem Bescheid gegeben habe."
      new PatternTokenBuilder().token("niemand").matchInflectedForms().build(),
      posRegex("SUB:.+")
    ),
    asList(
      token("alles"),
      csToken("Walzer")
    ),
    asList( // "ei der Daus"
      csToken("der"),
      csToken("Daus")
    ),
    asList( // "Das Orange ist meine Lieblingsfarbe"
      posRegex("PRO:...:...:SIN:NEU.*"),
      csToken("Orange")
    ),
    asList( // "Dieses rötliche Orange gefällt mir am besten"
      posRegex("PRO:...:...:SIN:NEU.*"),
      posRegex("ADJ:.+"),
      csToken("Orange")
    ),
    asList(
      csToken("dem"),
      new PatternTokenBuilder().csToken("Achtung").setSkip(1).build(),
      new PatternTokenBuilder().csToken("schenken").matchInflectedForms().build()
    ),
    asList(
      new PatternTokenBuilder().csToken("schenken").matchInflectedForms().build(),
      csToken("dem"),
      csToken("Achtung")
    ),
    asList(
      csToken("dem"),
      new PatternTokenBuilder().csToken("Rechnung").setSkip(1).build(),
      new PatternTokenBuilder().csToken("tragen").matchInflectedForms().build()
    ),
    asList(
      new PatternTokenBuilder().csToken("tragen").matchInflectedForms().build(),
      csToken("dem"),
      csToken("Rechnung")
    ),
    asList(
      csToken("zum"),
      csToken("einen"),
      posRegex("(ADJ|PA[12]):.+")
    ),
    asList(
      token("auf"),
      csToken("die"),
      csToken("Lauer")
    ),
    asList(
      tokenRegex("(eben)?dieser"),
      csToken("eine"),
      pos("SUB:NOM:SIN:MAS")
    ),
    asList(
      token("das"),
      posRegex("SUB:DAT:.+"),
      token("vorbehalten")
    ),
    asList( // Wenn hier einer Geld hat, dann ich.
      new PatternTokenBuilder().token("wenn").setSkip(1).build(),
      csToken("einer"),
      posRegex("SUB:AKK:.+"),
      posRegex("VER:(MOD:)?3:SIN:.+"),
      csToken(",")
    ),
    asList( // Es ist nicht eines jeden Bestimmung
      tokenRegex("eine[rs]"),
      tokenRegex("jed(wed)?en")
    ),
    asList( // Ich vertraue auf die Meinen.
      token("die"),
      tokenRegex("[MDS]einen")
    ),
    asList( // Sie ist über die Maßen schön.
      csToken("über"),
      csToken("die"),
      tokenRegex("Ma(ß|ss)en")
    ),
    asList( // Was nützt einem Gesundheit, wenn man sonst ein Idiot ist?
      token("was"),
      new PatternTokenBuilder().csToken("nützen").matchInflectedForms().build(),
      csToken("einem"),
      posRegex("SUB:NOM:.+")
    ),
    asList( // Auch das hat sein Gutes.
      new PatternTokenBuilder().csToken("haben").matchInflectedForms().build(),
      csToken("sein"),
      csToken("Gutes")
    ),
    asList( // Auch wenn es sein Gutes hatte.
      csToken("Gutes"),
      new PatternTokenBuilder().tokenRegex("haben|tun").matchInflectedForms().build()
    ),
    asList(
      tokenRegex("(eben)?dieser"),
      csToken("einen"),
      pos("SUB:DAT:SIN:FEM")
    ),
    asList(
      csToken("Rede"),
      csToken("und"),
      csToken("Antwort")
    ),
    asList(
      posRegex("ABK:.+:SUB")
    ),
    asList(
      tokenRegex("(all|je(d|glich))en"),
      csToken("Reiz")
    ),
    asList(
      tokenRegex("wieso|ob|warum|w[ae]nn"),
      token("das"),
      tokenRegex("sinn|mehrwert"),
      tokenRegex("macht|ergibt|stiftet|bringt")
    ),
    asList(
      tokenRegex("hat|hätte|kann|wird|dürfte|muss|soll(te)?|könnte|müsste|würde"),
      token("das"),
      token("Konsequenzen")
    ),
    asList(
      new PatternTokenBuilder().posRegex("VER:.*[1-3]:.+").setSkip(1).build(),
      csToken("vermehrt")
    ),
    asList( // In den Ruhr Nachrichten
      csToken("Ruhr"),
      csToken("Nachrichten")
    ),
    asList(
      csToken("Joint"),
      tokenRegex("Ventures?|Cares?")
    ),
    asList(
      csToken("Premier"),
      csToken("League")
    ),
    asList(
      // Common job title
      csToken("Software"),
      tokenRegex("Engineers?|Developer[sn]?|(Back|Front)end")
    ),
    asList(
      csToken("Mark"),
      posRegex("EIG:.*")
    ),
    asList(
      csToken("Sales"),
      tokenRegex("Agent")
    ),
    asList(
      csToken("Total"),
      tokenRegex("Tankstellen?")
    ),
    asList(
      posRegex("ART:.*"),
      csToken("Bund"),
      csToken("Naturschutz")
    ),
    asList(
      csToken("Real"),
      tokenRegex("Madrid|Valladolid|Mallorca")
    ),
    asList( // Eng.
      csToken("Real"),
      pos("UNKNOWN")
    ),
    asList(
      csToken("Hammer"),
      tokenRegex("Stra(ß|ss)e")
    ),
    asList( // https://www.duden.de/rechtschreibung/Personal_Trainer
      csToken("Personal"),
      tokenRegex("Trainers?")
    ),
    asList( // Ich wollte erstmal allen Hallo sagen.
      token("Hallo"),
      new PatternTokenBuilder().csToken("sagen").matchInflectedForms().build()
    ),
    asList( // "ob die Deutsch sprechen"
      token("die"),
      tokenRegex("Deutsch|Englisch|Spanisch|Französisch|Russisch|Polnisch|Holländisch|Niederländisch|Portugiesisch"),
      new PatternTokenBuilder().csToken("sprechen").matchInflectedForms().build()
    ),
    asList( // "Ein Trainer, der zum einen Fußballspiele sehr gut lesen und analysieren kann"
      token("zum"),
      token("einen"),
      posRegex("SUB:.*")
    ),
    asList( // https://www.duden.de/suchen/dudenonline/Fake%20News
      csToken("Fake"),
      posRegex("News")
    ),
    asList(
      tokenRegex("Steinberg|Park"),
      csToken("Apotheke")
    ),
    asList(
      csToken("IT"),
      csToken("Finanzmagazin")
    ),
    asList(
      csToken("Golden"),
      csToken("Gate")
    ),
    asList( // Vielen Dank fürs Bescheid geben
      token("fürs"),
      token("Bescheid"),
      tokenRegex("geben|sagen")
    ),
    asList( // "Los" ist ein deutsches Substantiv
      token("Los"),
      tokenRegex("Angeles|Zetas")
    ),
    asList( // https://www.autozeitung.de/
      csToken("Auto"),
      csToken("Zeitung")
    ),
    asList( // "Das letzte Mal war das Ende der..."
      csToken("Mal"),
      new PatternTokenBuilder().token("sein").matchInflectedForms().build(),
      csToken("das"),
      posRegex("SUB:NOM:.*")
    ),
    asList(
      csToken("FC"), // Die FC Bayern München Hymne (Vorschlag macht keinen Sinn "FC-Bayern")
      csToken("Bayern")
    ),
    asList(
      csToken("Super"),
      csToken("Mario")
    ),
    asList(
      tokenRegex(".+"),
      tokenRegex(".+"),
      csToken("Toyota"),
      csToken("Motor"),
      tokenRegex("Corp(oration)?|Company")
    ),
    asList(
      tokenRegex(".+"),
      tokenRegex(".+"),
      csToken("Metropolitan"),
      tokenRegex("Police|Community|City|Books")
    ),
    asList(
      tokenRegex("Office|Microsoft"),
      csToken("365")
    ),
    asList(
      csToken("Prinz"),
      tokenRegex("Charles|William")
    ),
    asList(
      token(":"),
      csToken("D")
    ),
    asList(
      tokenRegex("ist|war(en)?|sind|wird|werden"),
      csToken("das"),
      csToken("reine"),
      posRegex("SUB:NOM:.*")
    ),
    asList( // Eine Android Watch
      csToken("Android"),
      tokenRegex("Wear|Watch(es)?|Smartwatch(es)?|OS")
    ),
    asList( // "Bitte öffnen Sie die CAD.pdf"
      tokenRegex("\\w+"),
      new PatternTokenBuilder().token(".").setIsWhiteSpaceBefore(false).build(),
      new PatternTokenBuilder().tokenRegex("pdf|zip|jpe?g|gif|png|rar|mp[34]|mpe?g|avi|docx?|xlsx?|pptx?|html?").setIsWhiteSpaceBefore(false).build()
    ),
    asList( // "Ich mache eine Ausbildung zur Junior Digital Marketing Managerin"
      new PatternTokenBuilder().tokenRegex("Junior|Senior|Account").setSkip(3).build(),
      tokenRegex("Manager[ns]?|Managerin(nen)?|Developer(in)?")
    ),
    asList(
      new PatternTokenBuilder().tokenRegex("Junior|Senior").build(),
      token("Software"),
      tokenRegex("Engineers?|Architects?|Managers?|Directors?")
    ),
    asList(
      new PatternTokenBuilder().tokenRegex("Junior|Senior").build(),
      token("Engineering"),
      tokenRegex("Manager[ns]?|Directors?")
    ),
    asList( // "Angel" is tagged like the "Die Angel" for fishing
      csToken("Business"),
      tokenRegex("Angel[ns]?|Cases?")
    ),
    asList( // "des Manager Magazins"
      csToken("Manager"),
      tokenRegex("Magazins?")
    ),
    asList(
      csToken("Junior"),
      tokenRegex("Suite[sn]?")
    ),
    asList( // Deine Abt.
      tokenRegex("die|eine|unsere|meine|ihre|eure|(eben)?diese|seine|deine"),
      csToken("Abt"),
      token("."),
      tokenRegex(".+")
    ),
    asList(
      tokenRegex(".+"),
      tokenRegex(".+"),
      tokenRegex("Customer|User"),
      tokenRegex("Journeys?|Service")
    ),
    asList(
      tokenRegex(".+"),
      tokenRegex(".+"),
      token("Hall"),
      token("of"),
      token("Fame")
    ),
    asList( // Wir trinken ein kühles Blondes
      token("kühles"),
      token("Blondes")
    ),
    asList(
      tokenRegex("Vitamin|Buchstabe"),
      tokenRegex("D|B[1-9]?|B12")
    ),
    asList( // "Bei uns im Krankenhaus betrifft das Operationssäle."
      new PatternTokenBuilder().token("betreffen").matchInflectedForms().build(),
      csToken("das"),
      posRegex("SUB:AKK:PLU:.*")
    ),
    asList( // "Was für ein Narr"
      token("was"),
      csToken("für"),
      csToken("ein"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:(MAS|NEU).*").min(0).build(),
      posRegex("SUB:NOM:SIN:(MAS|NEU)")
    ),
    asList( // "Was für ein liebe Frau"
      token("was"),
      csToken("für"),
      csToken("eine"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:FEM.*").min(0).build(),
      pos("SUB:NOM:SIN:FEM")
    ),
    asList( // "Was war ich für ein Narr"
      token("was"),
      new PatternTokenBuilder().token("sein").matchInflectedForms().setSkip(2).build(),
      csToken("für"),
      csToken("ein"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:(MAS|NEU).*").min(0).build(),
      posRegex("SUB:NOM:SIN:(MAS|NEU)")
    ),
    asList( // "Was war sie nur für eine dumme Person"
      token("was"),
      new PatternTokenBuilder().token("sein").matchInflectedForms().setSkip(2).build(),
      csToken("für"),
      csToken("eine"),
      new PatternTokenBuilder().posRegex("ADJ:NOM:SIN:FEM.*").min(0).build(),
      pos("SUB:NOM:SIN:FEM")
    ),
    asList( // "Wie viele Paar Schuhe braucht er?"
      csRegex("vielen?|etliche|wenigen?|einigen?"),
      csToken("Paar"),
      posRegex("SUB:NOM:PLU:...")
    ),
    asList( // Dann macht das Sinn.
      csRegex("machte?|ergibt|ergab|stiftete?"),
      csToken("das"),
      csToken("Sinn")
    ),
    asList( // Mir machte das Spaß
      csRegex("machte?"),
      csToken("das"),
      csRegex("Spa(ß|ss)|Freude")
    ),
    asList( // Das sind beides Lichtschalter; Wasser und Luft sind beides Fluide.
      csRegex("sind|waren"),
      csToken("beides"),
      new PatternTokenBuilder().posRegex("ADJ:.*").min(0).build(),
      posRegex("SUB:NOM:PLU:.*")
    ),
    asList( // Heinrich von der Haar (https://de.wikipedia.org/wiki/Heinrich_von_der_Haar)
      token("Heinrich"),
      token("von"),
      token("der"),
      csRegex("Haars?")
    ),
    asList(
      token("Präsident"),
      token("Xi")
    ),
    asList(
      token("Porsche"),
      token("Museum")
    ),
    asList(
      token("Auto"),
      token("Club"),
      token("Europa")
    ),
    asList(
      token("Queen"),
      posRegex("EIG:.*")
    ),
    asList(
      token("King"),
      posRegex("EIG:.*")
    ),
    asList( // des Sturm und Drangs
      token("des"),
      token("Sturm"),
      token("und"),
      csRegex("Drangs?")
    ),
    asList( // die Funke Mediengruppe
      token("die"),
      token("Funke"),
      token("Mediengruppe")
    ),
    asList(
      new PatternTokenBuilder().csToken("meinen").matchInflectedForms().setSkip(3).build(),
      csRegex("das|(eben)?dies(es)?"),
      new PatternTokenBuilder().token("wirklich").min(0).build(),
      token("Ernst")
    ),
    asList(
      new PatternTokenBuilder().csToken("nehmen").matchInflectedForms().setSkip(3).build(),
      csRegex("das|(eben)?dies(es)?"),
      new PatternTokenBuilder().token("wirklich").min(0).build(),
      token("Ernst")
    ),
    asList(
      // ... dann spart das Zeit und Geld.
      new PatternTokenBuilder().csToken("sparen").matchInflectedForms().setSkip(3).build(),
      csRegex("das|(eben)?dies(es)?"),
      token("Zeit"),
      token("und"),
      csRegex("Geld|Nerven")
    ),
    asList(
      csRegex("das|es|(eben)?dies"),
      csRegex("bedeutete?"),
      csRegex("Krieg|Ärger")
    ),
    asList(
      // In der aktuellen Niedrigzinsphase bedeutet das sehr geringe Zinsen, die aber deutlich ansteigen können.
      csRegex("bedeutete?"),
      csRegex("das|(eben)?dies")
    ),
    asList(
      csRegex("das|es|(eben)?dies"),
      csRegex("weite"),
      token("Teile")
    ),
    asList(
      token("hat"),
      token("das"),
      csRegex("Einfluss|Auswirkungen"),
      csRegex("auf|darauf")
    ),
    asList( // ein Auto, das schnell fährt und in **das Menschen** gerne einsteigen
      posRegex("VER.*[123].*"),
      tokenRegex("und|oder|aber"),
      new PatternTokenBuilder().posRegex("PRP.*").min(0).build(),
      tokenRegex("der|die|das|dem|den")  // nicht 'des' weil sonst nicht gefunden: "Wir haben das Abo beendet und des Betrag erstattet."
    ),
    asList( // weil man oft bei **anderen schreckliches Essen** vorgesetzt bekommt
      tokenRegex("bei|zum"),
      token("anderen"),
      posRegex("ADJ.*"),
      posRegex("SUB.*")
    ),
    asList( // dass jeder LanguageTool benutzen sollte
      token("jeder"),
      posRegex("SUB:(AKK|DAT).*"),
      posRegex("VER.*")
    ),
    asList( // der fließend Französisch sprechende Trudeau
      posRegex("(ART|PRO:DEM).*"),
      posRegex("ADV.*"),
      posRegex("SUB.*"),
      posRegex("PA[12].*")
    ),
    asList( // Spricht dieser fließend Französisch
      posRegex("VER.*[123].*"),
      posRegex("(ART|PRO:DEM).*"),
      posRegex("ADV.*"),
      posRegex("SUB.*")
    ),
    asList( // Der Deutsch Langhaar ist ein mittelgroßer Jagdhund
      token("Deutsch"),
      token("Langhaar")
    ),
    asList( // Einige nennen das
      new PatternTokenBuilder().csToken("nennen").matchInflectedForms().build(),
      token("das"),
      posRegex("SUB:NOM:SIN:(FEM|MAS)")
    ),
    asList( // Das ist bestimmt kein Made in Germany
      csToken("Made"),
      csToken("in"),
      csToken("Germany")
    ),
    asList( // Des Plan de XXX
      csRegex("[A-Z].+"),
      csRegex("del?"),
      csRegex("[A-Z].+")
    ),
    asList( // Des Plan de XXX
      csRegex("[A-Z].+"),
      csToken("de"),
      regex("l[ao]s?"),
      csRegex("[A-Z].+")
    )
  );

}
