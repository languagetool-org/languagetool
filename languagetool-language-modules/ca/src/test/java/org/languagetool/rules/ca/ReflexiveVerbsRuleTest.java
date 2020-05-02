/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà
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

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Jaume Ortolà
 */
public class ReflexiveVerbsRuleTest {

  private ReflexiveVerbsRule rule;
  private JLanguageTool langTool;

  @Before
  public void setUp() throws IOException {
    rule = new ReflexiveVerbsRule(TestTools.getEnglishMessages());
    langTool = new JLanguageTool(new Catalan());
  }

  @Test
  public void testRule() throws IOException { 

    //TODO: se'n vola / s'envola
    // correct sentences:
    //TODO: assertCorrect("Mai se m'hauria ocorregut vanagloriar-me");
    assertCorrect("s’està esforçant");
    assertCorrect("m'he estat queixant");
    assertCorrect("se'n va començar a ressentir");
    assertCorrect("Me’n pensava endur més de mil");
    assertCorrect("ja se'n comença a adonar");
    assertCorrect("s'acaba d'adonar que ha escrit poemes");
    assertCorrect("se’ls acaben d’emportar un familiar");
    assertCorrect("i ens l'acabem empassant.");
    assertCorrect("se’l va acabar enduent");
    assertCorrect("Ella se sol endur les garrotades.");
    assertCorrect("No t’acabes d’atrevir.");
    assertCorrect("Es va intentar suïcidar.");
    assertCorrect("L'ha a baixar desde."); //nonsense sentence with spelling error!
    assertCorrect("El tauler es pot construir baixant amb cartes.");
    assertCorrect("El botó del ratolí es configura per anar endavant i endarrere.");
    assertCorrect("la festa de Rams es commemora anant a l'església a beneir el palmó");
    assertCorrect("les circumstàncies m'obliguen a gloriar-me"); 
    //assertCorrect("es van agenollar i prosternar");
    //assertCorrect("Una equivocació tan gran no es pot callar.");
    //assertCorrect(" és del tot necessari si no es vol caure en una religió alienant");
    assertCorrect("Li agrada queixar-se.");
    assertCorrect("Els agrada queixar-se.");
    assertCorrect("Ens agrada queixar-se.");
    assertCorrect("Els animem a queixar-se.");
    assertCorrect("L'animo a continuar esforçant-se.");
    assertCorrect("Ens agrada queixar-nos.");
    assertCorrect("Us agrada queixar-vos.");
    assertIncorrect("Ens agrada queixar-vos.");
    
    assertCorrect("Lorella va a venir"); // used to cause NPE - see #1787
    assertCorrect("Ens animava a queixar-nos.");
    assertCorrect("L'animem a queixar-se.");
    assertCorrect("Us animem a endur-vos a casa.");
    assertCorrect("T'animem a queixar-te.");
    assertCorrect("T'enduràs això.");
    assertCorrect("Alguns ens adonàrem que era veritat");
    assertCorrect("M'he baixat moltes imatges");
    assertCorrect("baixeu-vos l'Aspell des de http://aspell.net/win32/");
    assertCorrect("els fitxers de traducció es baixaran automàticament");
    assertCorrect("Baixeu-vos el programa de l'enllaç");
    assertCorrect("No em plantejo anar a un altre partit");
    assertCorrect("-Deixa't caure al canal i prou");
    assertCorrect("Deixa't caure al canal i prou");
    assertCorrect("Durant el 2010 s'ha crescut molt");
    assertCorrect("de què tant ens queixem");
    assertCorrect("cada zona més meridional esdevingué adient per als éssers àrtics");
    assertCorrect("cereals, garrofers, vinya i olivar.");
    assertCorrect("m'aniria bé probablement posar els quilos");
    assertCorrect("tot m'aniria bé");
    assertCorrect("tot m'havia anat bé");
    assertCorrect("tot m'havia anat molt bé fins que m'ha passat");
    assertCorrect("el cor m'anava a cent per hora.");
    assertIncorrect("Jo m'anava a cent per hora.");
    assertIncorrect("M'anava a casa a cent per hora.");
    assertCorrect("Sempre li havia anat bé");
    assertCorrect("Em va bé");
    assertIncorrect("Sempre t'havies anat bé");
    assertCorrect("Sempre m'havia vingut bé");
    assertCorrect("Sempre m'havia anat bé");
    assertCorrect("T'agraeixo molt que m'hagis deixat robar-te una mica del teu temp");
    assertCorrect("sense haver-s'hi d'esforçar gaire");
    assertCorrect("cosa que li permetia, sense haver-s'hi d'esforçar gaire, seguir entre classe i classe");
    assertCorrect("fins que no em vingui la inspiració");
    assertCorrect("Si no ho trobes bé, vés-te a queixar al director");
    
    assertCorrect("potser em vindria de gust fer un mossec");
    assertCorrect("li ho va fer empassar de cop");
    //assertCorrect("del lloc on m'havia caigut"); correcte o incorrecte segons quin sigui el subjecte
    assertCorrect("i matar-se caient de més de vuitanta peus d'altura");
    assertCorrect("Deixa de portar-me la contra.");
    //assertIncorrect("No deixis de portar-te el menjar.");
    assertCorrect("quan ja es tornava a envolar li va caure aquest");
    assertCorrect("Van fer agenollar els presos");
    assertCorrect("Deixa'm dir-t'ho a l'orella");
    assertCorrect("Em deixes demanar-te una cosa?");
    assertCorrect("havien fet desbocar un cavall sense brida");
    assertCorrect("quan el vent ja m'hauria portat les rondalles");
    assertCorrect("Llavors m'oloro les mans"); 
    assertCorrect("Hem de poder-nos queixar");
    assertCorrect("Ens hem de poder queixar");
    assertCorrect("Després d'acomiadar-nos vam pujar a la nau");
    assertCorrect("li havia impedit defensar-se.");
    assertCorrect("L'instant que havia trigat a fer-lo li havia impedit defensar-se.");
    assertCorrect("quan ja s’olorava en l’aire la primavera");
    assertCorrect("que la vergonya em pugés a les galtes");
    //assertCorrect("per on volia portar-me el mestre");
    //assertCorrect("¿De què m’havia d’haver adonat?");
    //assertCorrect("i et costa empassar-te la saliva.");
    //assertCorrect("la recança que em feia haver-me d’allunyar d’ella");
    assertCorrect("En Feliu em fa dir-te que el dispensis");
    assertCorrect("i que ja em vindria la son");
    assertCorrect("La mort del pare m’havia portat la imatge d’aquests morts");
    assertCorrect("Una onada de foc em pujava del pit a la cara.");

    assertIncorrect("Ens vàrem marxar amb tren.");
    //donar-se compte
    assertCorrect("D'aquest Decret se n'ha donat compte al Ple de l'Ajuntament");
    assertCorrect("Encara em cal donar compte d'un altre recull");
    assertCorrect("Michael Kirby ens dóna compte a Anàlisi estructural");
    //assertCorrect("tractant-se de cas d'urgència i donant-se compte al Ple de l'Ajuntament");
    assertIncorrect("Ell es va donar compte de l'error");
    //assertIncorrect("Es va donar compte de l'error"); //cas dubtós
    assertIncorrect("Joan es va donar compte de l'error");
    assertIncorrect("Algú se n'hauria de donar compte.");
    assertIncorrect("Vas donar-te compte de l'error");
    assertIncorrect("llavors comenten discretament l'afer i es callen, tanmateix, els noms");
    //
    assertCorrect("el qui amb mi delira està lliure");
    assertCorrect("per venir-vos a veure ");
    assertCorrect("No li ho ensenyis, que el faràs delir.");
    assertCorrect("per a portar-te aigua");
    assertCorrect("que no em costi d'anar al llit");
    assertCorrect("el senyor Colomines s'anà progressivament reposant");
    assertCorrect("en sentir els plors s'encongeix automàticament,");
    assertCorrect("La penya de l'Ateneu es va de mica en mica reconstruint");
    assertCorrect("no m'he pogut endur l'espasa");
    assertCorrect("un llop es podria haver endut la criatura");
    assertCorrect("Quan se'l van haver endut a casa");
    assertCorrect("Ja et deus haver adonat que no");
    assertCorrect("fins que se'n va haver desempallegat");
    assertCorrect("per haver-se deixat endur per l'orgull");
    assertCorrect("i de venir-vos a trobar");
    assertCorrect("el sol s'havia post, li anaven portant tots els malalts");
    assertCorrect("que no em caigui la casa");
    assertCorrect("que no em caigui al damunt res");
    assertCorrect("Em queia bé.");
    assertCorrect("Els qui s'havien dispersat van anar pertot arreu");
    assertCorrect("Els qui volen enriquir-se cauen en temptacions");
    assertCorrect("Després d'acomiadar-nos, vam pujar a la nau");
    assertCorrect("que em vingui a ajudar");
    assertCorrect("fins i tot us vendríeu un amic");
    assertCorrect("ens hem esforçat molt per venir-vos a veure");
    assertCorrect("Un altre dia s'anava a l'Ermita i un tercer dia se solia anar a altres indrets de caràcter comarcal.");
    assertCorrect("La nit de sant Joan es baixaven falles de la muntanya."); //solucions: marcar "la nit..." com a CC o comprovar la concordança subj/verb
    assertCorrect("que no pertanyen a ells mateixos es cau en una contradicció.");
    assertCorrect("Els salts els fan impulsant-se amb les cames");
    assertCorrect("Zheng, adonant-se que gairebé totes les forces singaleses");
    assertCorrect("que s'havien anat instal·lant");
    assertCorrect("gràcies a la presència del Riu Set s'hi alberga una gran arboreda amb taules");
    assertCorrect("no fa gaires anys també s'hi portaven alguns animals");
    assertCorrect("el sòlid es va \"descomponent\".");
    assertCorrect("la divisió s'ha d'anar amb cura per evitar ambigüitats");
    assertCorrect("la senyera s'ha de baixar");
    assertCorrect("Es van témer assalts a altres edificis de la CNT ");
    assertCorrect("que Joan em dugués el mocador");
    //assertCorrect("que Joan es dugués el mocador"); // dubtós
    assertCorrect("em duràs un mocador de seda del teu color");
    assertCorrect("El va deixar per a dedicar-se a la música");
    assertCorrect("Hermes s'encarregava de dur les ànimes que acabaven de morir a l'Inframón");
    assertCorrect("aquest nom és poc adequat ja que es poden portar les propostes de l'escalada clàssica");
    //assertCorrect("totes les comissions dels països vencedors en les guerres napoleòniques es van portar els seus propis cuiners");
    assertCorrect("en fer-lo girar se'n podia observar el moviment");
    assertCorrect("el segon dia es duien a terme les carreres individuals");
    assertCorrect("Normalment no es duu un registre oficial extern");
    assertCorrect("Ens portem força bé");
    assertCorrect("Hem de portar-nos bé");
    assertCorrect("Ells es porten tres anys");
    assertCorrect("Fan que em malfiï.");
    assertCorrect("Em fan malfiar.");
    assertCorrect("El fan agenollar.");
    assertCorrect("ens anem a aferrissar");
    assertCorrect("anem a aferrissar-nos");
    assertCorrect("ens preparem per a anar");
    assertCorrect("comencen queixant-se");
    assertCorrect("comenceu a queixar-vos");
    assertCorrect("no em podia pas queixar");
    assertCorrect("em puc queixar");
    assertCorrect("en teniu prou amb queixar-vos");
    assertCorrect("ens en podem queixar");
    assertCorrect("es queixa");
    assertCorrect("es va queixant");
    assertCorrect("es va queixar");
    assertCorrect("has d'emportar-t'hi");
    assertCorrect("has de poder-te queixar");
    assertCorrect("t'has de poder queixar");
    assertCorrect("havent-se queixat");
    assertCorrect("haver-se queixat");
    assertCorrect("no es va poder emportar");
    assertCorrect("no has de poder-te queixar");
    assertCorrect("no has de queixar-te");
    assertCorrect("no podeu deixar de queixar-vos");
    assertCorrect("no t'has de queixar");
    assertCorrect("no us podeu deixar de queixar");
    assertCorrect("pareu de queixar-vos");
    assertCorrect("podent abstenir-se");
    assertCorrect("poder-se queixar");
    assertCorrect("podeu queixar-vos");
    assertCorrect("queixa't");
    assertCorrect("queixant-vos");
    assertCorrect("queixar-se");
    assertCorrect("queixeu-vos");
    assertCorrect("s'ha queixat");
    assertCorrect("se li ha queixat");
    assertCorrect("se li queixa");
    assertCorrect("se li va queixar");
    assertCorrect("va decidir suïcidar-se");
    assertCorrect("va queixant-se");
    assertCorrect("va queixar-se");
    assertCorrect("va queixar-se-li");
    assertCorrect("Se'n pujà al cel");
    assertCorrect("Se li'n va anar la mà");
    assertCorrect("El nen pot callar");
    assertCorrect("es va desfent");
    assertCorrect("s'ha anat configurant");  
    assertCorrect("s'han anat fabricant amb materials");
    assertCorrect("la matèria que cau s'accelera");
    assertCorrect("Altres muntanyes foren pujades per pastors, caçadors o aventurers.");
    assertCorrect("mai assolí èxit social");
    assertCorrect("Aquests polímers són lineals i no ramificats.");
    assertCorrect("tornaven a assolar la Vall de l'Ebre.");
    assertCorrect("està previst que s'acabin per a anar directament a la zona");
    assertCorrect("es deixaven caure");
    assertCorrect("es van deixar caure");
    assertCorrect("van deixar-se caure");
    assertCorrect("et deixaves pujar");
    assertCorrect("Els animals es feien témer amb cops secs de ferro");
    assertCorrect("es veié obligat a marxar el 1512.");
    assertCorrect("Francesc III es va anar a asseure sobre el tron");
    assertCorrect("Va anar a dutxar-se");
    assertCorrect("es va anar a dutxar");
    assertCorrect("es van deixar anar molts empresonats.");
    assertCorrect("A Joan se li'n va anar la mà");
    assertCorrect("se'ns en va anar la mà");
    assertCorrect("ja que si l'arròs se sega molt verd");
    assertCorrect("s'hi afegeixen bolets abans d'enfundar-la en l'intestí");
    assertCorrect("Joan ha anat a fer-se la prova.");
    //assertCorrect("Joan s'ha anat a fer la prova."); -->dubtós
    //assertCorrect("Cada grup s'ha anat a fer la prova."); -->dubtós
    assertCorrect("Cada grup s'ha anat a dutxar.");
    assertCorrect("Joan ha anat a dutxar-se.");
    assertCorrect("Joan s'ha anat a dutxar.");
    assertCorrect("amb els Confederats intentant burlar el bloqueig a Maryland.");
    //IMPERSONALS
    assertCorrect("l'altre es duu la mà al llavi inferior"); 
    assertCorrect("l'altre s'olora les mans"); 
    //assertCorrect("la impressió que es va endavant");
    assertCorrect("Es pot baixar la darrera versió.");
    assertCorrect("Se'l va fer callar.");
    assertCorrect("Se li va fer callar."); //incorrecta per una altra qüestió
    assertCorrect("Se'ns va fer callar.");
    assertCorrect("També es canta quan es va a pasturar als animals");    
    assertCorrect("Quan es baixa a l'ordinador de l'usuari,");
    assertCorrect("sinó que es baixa per parts a l'atzar.");
    assertCorrect("Es tem que la radioactivitat afecti la població local");
    assertCorrect("Després de tot això es va témer la possibilitat");
    assertCorrect("probablement es vagi a destil·lar l'etanol");
    assertCorrect(", es podia anar a Madrid per aconseguir en Celebi");
    assertCorrect("Els soldats es preparen per a marxar a la guerra.");
    assertCorrect("Tu et prepares per marxar a la guerra.");
    assertCorrect("i que es temia que s'aconseguís el nombre previst.");
    assertCorrect("Des del principi es temia el pitjor");
    assertCorrect("La primera muntanya que es va pujar per motius purament esportius,");
    assertCorrect("Quan el so era via fora, s'anava a guerrejar fora de la terra.");
    assertCorrect("els algorismes, de manera que s'evita caure");
    assertCorrect("En acabar l'assalt, és comú que es pugi un banc");
    assertCorrect("Es va caure en la provocació.");
    assertCorrect("Abans d'això ja s'havien pujat muntanyes,");
    assertCorrect("a una representació de La Passió no només s'hi va a veure un espectacle sumptuós");
    assertCorrect("A escola no s'hi va a plorar.");
    assertCorrect("A escola no es va a jugar.");
    assertCorrect("A escola no es va a plorar.");
    assertCorrect("Al nostre pis de la Torre es pujava per aquella llarga escala");
    assertCorrect("Joan no es va a jugar la feina.");  
    assertCorrect("I aquella flaire que em pujava al cap");
    assertCorrect("el que no s'olora, el que no es tasta");
       
    // errors:
    assertIncorrect("Ells s'han crescut molt");
    assertIncorrect("Em vaig créixer davant les dificultats");
    assertIncorrect("Joan s'ha crescut molt");
    assertIncorrect("Joana s'ha crescut molt");
    assertIncorrect("Ada Martínez s'ha crescut molt");
    assertIncorrect("Ada Colau s'ha crescut molt");
    assertIncorrect("Ha arribat l'hora de saltar-se la legalitat.");
    assertIncorrect("Delia per menjar-ne.");
    assertIncorrect("Ells es volen dur les ànimes a l'Inframón");
    assertIncorrect("Joan es va portar el carretó");
    assertIncorrect("en aquesta vida ens portem moltes sorpreses");
    assertIncorrect("Ens hem portat massa material al campament");
    assertIncorrect("Hem de dur-nos tot això.");
    assertIncorrect("L'has fet tornar-se vermell.");
    assertIncorrect("El fan agenollar-se.");
    assertIncorrect("Fes-lo agenollar-se.");
    assertIncorrect("Deixa'm agenollar-me.");
    assertIncorrect("l'havia fet ufanejar-se obertament");
    assertIncorrect("un dels pocs moviments que poden fer és intentar pujar-se al carro de la indignació.");
    assertIncorrect("és intentar pujar-se al carro de la indignació.");
    assertIncorrect("Pujar-se al carro de la indignació.");
    assertIncorrect("Pujar-vos al carro de la indignació.");
    assertIncorrect("se li va caure la cara de vergonya");
    assertIncorrect("se'ns va caure la cara de vergonya");
    assertIncorrect("A mi se'm va caure la cara de vergonya");
    assertIncorrect("Joan no es va a l'escola");
    assertIncorrect("que el procés no se'ns vagi de les mans");
    assertIncorrect("Ho volen per a anar-se de la zona"); 
    assertIncorrect("Ho volen per anar-se de la zona"); 
    assertIncorrect("Ho desitgen per anar-se de la zona"); 
    assertIncorrect("els grups que es van caure del cartell");
    assertIncorrect("el nen que es va caure al pou");//--> Es pot tractar diferent: caure / anar
    assertCorrect("el dia que es va anar a la ciutat");
    //assertIncorrect("el dia que es va anar a la ciutat");
    assertIncorrect("tot l'auditori es callà");
    assertIncorrect("les gotes que es van caure fora"); 
    assertIncorrect("Ells s'han baixat del tren.");
    assertIncorrect("Ximo Puig i Rubalcaba no s'han baixat del cotxe oficial des del 79.");
    assertIncorrect("Se'ns va callar.");
    assertIncorrect("Tothom es va callar.");
    assertIncorrect("Els nens van poder-se caure");  
    assertIncorrect("Aleshores ell es va anar a estudiar a Barcelona"); //-->va anar a fer introspecció :-)
    assertIncorrect("Joan es va anar a estudiar a Barcelona.");
    assertIncorrect("se'ns va anar la mà");
    assertIncorrect("A Joan se li va anar la mà");  
    assertIncorrect("Al pare se li va anar la mà");  
    assertIncorrect("Escriu que quan era mosso «se li anaven els ulls»");
    
    assertIncorrect("Es van caure en la trampa.");
    assertIncorrect("Aleshores es van anar a la ciutat a presentar una queixa.");
    assertIncorrect("Va entrar l'avi que pujava del taller i es va seure.");
    //assertIncorrect("Aleshores es va anar a la ciutat a presentar una queixa.");
    //assertIncorrect("quan es pugen, permeten canviar de feina.");
    assertIncorrect("havent queixat");
    assertIncorrect("haver queixat");
    assertIncorrect("les membranes s'han anat fabricat amb materials sintètics"); 
    assertIncorrect("s'han anat fabricat amb materials sintètics");
    assertIncorrect("Holmes i Watson s'han anat d'acampada");
    assertIncorrect("L'independentisme s'ha anat a Brussel·les!");
    assertIncorrect("El seu marit s'ha anat a la Xina per negocios");
    assertIncorrect("L'home es marxà de seguida");
    assertIncorrect("L'home s'anà de seguida");
    assertIncorrect("A Joan se li va caure la cara de vergonya");
    assertIncorrect("El nen es cau");
    assertIncorrect("El nen se li cau");
    assertIncorrect("A la nena se li caigueren les arracades");
    assertIncorrect("El nen s'ha de caure");
    assertIncorrect("El nen pot caure's");
    assertIncorrect("Calleu-vos");
    //assertIncorrect("Es pujà al cel"); ->indecidible
    assertIncorrect("El berenar es pujà al cel");
    assertIncorrect("Va baixar-se del cotxe en marxa.");
    
    assertIncorrect("comencen queixant");
    assertIncorrect("comenceu a queixar-nos");
    assertIncorrect("et puc queixar");
    assertIncorrect("en teniu prou amb queixar");
    assertIncorrect("en podem queixar");
    assertIncorrect("et queixa");
    assertIncorrect("em va queixant");
    assertIncorrect("li va queixar");
    assertIncorrect("hem d'emportar-t'hi");
    assertIncorrect("heu de poder-te queixar");
    assertIncorrect("m'has de poder queixar");
    assertIncorrect("havent queixat");
    assertIncorrect("haver queixat");
    assertIncorrect("no es vam poder emportar");
    assertIncorrect("no has de poder-vos queixar");
    assertIncorrect("no has de queixar-ne");
    assertIncorrect("no podeu deixar de queixar-ne");
    assertIncorrect("no li has de queixar");
    assertIncorrect("no em podeu queixar");
    assertIncorrect("pareu de queixar-se'n");
    assertIncorrect("podent abstenir");
    assertIncorrect("poder queixar");
    assertIncorrect("podeu queixar");
    assertIncorrect("queixa'n");
    assertIncorrect("queixant");
    assertIncorrect("queixar");
    assertIncorrect("queixeu-se'n");
    assertIncorrect("de n'ha queixat");
    assertIncorrect("me li ha queixat");
    assertIncorrect("te li queixa");
    assertIncorrect("us li va queixar");
    assertIncorrect("va decidir suïcidar-me");
    assertIncorrect("va queixant");
    assertIncorrect("va queixar");
    assertIncorrect("va queixar-li");
    assertIncorrect("anem a aferrissar");
  }

  private void assertCorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(sentence));
    assertEquals(0, matches.length);
  }

  private void assertIncorrect(String sentence) throws IOException {
    final RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(sentence));
    assertEquals(1, matches.length);
  }

}
