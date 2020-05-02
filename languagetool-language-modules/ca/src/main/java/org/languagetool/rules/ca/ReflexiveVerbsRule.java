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
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.*;
import org.languagetool.tools.StringTools;

/**
 * This rule checks the use of pronominal/non pronominal verbs.
 *   
 * @author Jaume Ortolà i Font
 */
public class ReflexiveVerbsRule extends Rule {

  /**
   * Patterns
   */
  
  // List of only pronominal verbs from GDLC (eliminats: assolar, enfundar, burlar, traslluir, intersecar, trufar)
  // (afegits: delir, desomplir, encaramallar, rojar)
  // autofinançar??
  // Eliminats: témer
  private static final List<String> verbsPronominals = Arrays.asList("arranxar", "empitimar", "enfillar", "enseriar", "enseriosir", "esgroguissar", "esmaperdre", "ufanar", "apoltronar", "marfondir", "rojar", "personar", "encaramallar", "desomplir", "delir", "fugar", "abacallanar", "abalançar", "ablenar", "aborrallonar", "abotifarrar", "abrinar", "abromar", "abstenir", "acagallonar", "acanyar", "acarcanyar", "acarnissar", "acatarrar", "aciutadanar", "aclocar", "acopar", "acorrufar", "acorriolar", "adir", "adonar", "adormissar", "afal·lerar", "afarrossar", "afeccionar", "aferrallar", "aferrissar", "aferrussar", "agallinar", "agarbir", "agarrofar", "agemolir", "agenollar", "agotzonar", "aiguabarrejar", "allocar", "alçurar", "amatinar", "amelar", "amigar", "amoixir", "amoltonar", "amotar", "amullerar", "amunionar", "antullar", "aparroquianar", "aparroquiar", "aperduar", "apergaminar", "apiadar", "aponentar", "apropinquar", "apugonar", "arguellar", "arrapinyar", "arrasir", "arravatar", "arraïmar", "arrepapar", "arrepenjar", "arrepetellar", "arrigolar", "arrodir", "arrogar", "arrossar", "arruar", "assemblar", "assocarrar", "atendar", "atenir", "atorrentar", "atrafegar", "atrevir", "avencar", "avidolar", "avinençar", "balbar", "balcar", "balir", "balmar", "bescomptar", "boirar", "boixar", "botinflar", "bromar", "cagaferrar", "candir", "capbaixar", "capmassar", "captenir", "cariar", "carnificar", "carpir", "coalitzar", "colltrencar", "collvinclar", "compenetrar", "condoldre", "condolir", "congraciar", "contorçar", "contòrcer", "corcorcar", "coresforçar", "cornuar", "corruixar", "acorruixar", "crisalidar", "desafeccionar", "desalenar", "desamorar", "desaparroquiar", "desapassionar", "desaplegar", "desavenir", "desbocar", "descantar", "descarar", "descontrolar", "descovar", "desdubtar", "desempallegar", "desenrojolar", "desentossudir", "desfeinar", "desmemoriar", "desnodrir", "despondre", "despreocupar", "dessolidaritzar", "desteixinar", "desvagar", "desvergonyir", "desviure", "dignar", "embarbussar", "embascar", "embessonar", "embordeir", "embordir", "emborrascar", "emborrossar", "embotifarrar", "embotzegar", "embromallar", "embromar", "embroquerar", "emmainadar", "emmalurar", "emmalurir", "emmarar", "emmarranar", "emmatar", "emmigranyar", "emmorronar", "emmurriar", "empassar", "empassolar", "empegueir", "empenyalar", "empescar", "empillocar", "empinyar", "empiocar", "empitarrar", "emplomissar", "emplujar", "emportar", "encabotar", "encabritar", "encalmar", "encalostrar", "encelar", "encinglar", "encirar", "encistar", "enclaperar", "encolerir", "encordar", "encruar", "endoblir", "endur", "enfarfollar", "enfaristolar", "enfavar", "enfereir", "enferotgir", "enferritjar", "enfugir", "enfurrunyar", "enfutimar", "enfutismar", "engelabrir", "engolfar", "engorgar", "engripar", "enguerxinar", "enllagrimar", "enlleganyar", "enlleir", "ennavegar", "enneguitar", "enquistar", "enrinxar", "enseriosir", "ensobecar", "entonyinar", "entossudir", "entotsolar", "entreabaltir", "entrebadar", "entrebatre", "entrebesar", "entrecavalcar", "entredevorar", "entreferir", "entreforcar", "entrematar", "entremetre", "entremirar", "entrenyorar", "entresaludar", "entreseguir", "entresoldar", "entretocar", "entretzenar", "entrigar", "envidreir", "envidriar", "envolar", "enxautar", "esbafar", "esbafegar", "esbatussar", "esblamar", "esbojarrar", "esborneiar", "esbromar", "escabridar", "escamotar", "escanyellar", "escanyolir", "escanyussar", "escapolar", "escapolir", "escarcanyar", "escarramicar", "escarrassar", "escarxofar", "escatifenyar", "esconillar", "escorporar", "escullar", "escunçar", "esfarinar", "esfetgegar", "esforçar", "esgargamellar", "esgatinyar", "esgolar", "esguimbar", "esllanguir", "esllavissar", "esperitar", "espitellar", "espitxar", "espollinar", "espoltrar", "esporcellar", "espotonar", "esprimatxar", "esquifir", "esquitllar", "estilar", "estritllar", "esvedellar", "esventegar", "esvomegar", "etiolar", "extralimitar", "extravasar", "extravenar", "gamar", "gaspar", "gatinyar", "gaubar", "gloriar", "grifar", "immiscir", "indigestar", "industriar", "innivar", "insolentar", "insurgir", "inveterar", "irèixer", "jactar", "juramentar", "lateritzar", "llufar", "malfiar", "malfixar", "migrolar", "mofar", "mullerar", "neulir", "obstinar", "octubrar", "olivar", "pellobrir", "pellpartir", "pelltrencar", "penedir", "penjolar", "pollar", "prosternar", "queixar", "querar", "querellar", "quillar", "ramificar", "rancurar", "realegrar", "rebel·lar", "rebordeir", "refiar", "repanxolar", "repapar", "repetellar", "reressagar", "resclosir", "ressagar", "ressentir", "revenjar", "salinar", "suïcidar", "tinyar", "tolir", "transvestir", "traspostar", "vanagloriar", "vanagloriejar", "vanar", "vantar", "vergonyar", "xautar");
  private static final Pattern VERB_AUTO = Pattern.compile("auto.+");
  private static final List<String> excepVerbsPronominals = Arrays.asList("amoixar", "delirar", "atendre", "escollir", "assolir","autografiar","automatitzar","autoritzar");  
  
  private static final List<String> verbsNoPronominals = Arrays.asList("baixar","caure","callar","marxar","albergar","olorar","seure", "saltar", "créixer", "postular"); 
  private static final List<String> verbsNoPronominalsImpersonals = Arrays.asList("caure", "callar", "marxar", "olorar", "créixer");
  private static final List<String> verbsNoPronominalsImpersonals2 = Arrays.asList("témer","albergar","baixar");
  private static final List<String> excepVerbsNoPronominals = Arrays.asList("segar");
  
  private static final List<String> verbsMoviment = Arrays.asList ("anar","pujar","venir");
  private static final List<String> excepVerbsMoviment = Arrays.asList ("vendre");

  private static final List<String> verbsSovintAmbComplement = Arrays.asList ("deixar","fer","veure","costar");
  private static final List<String> verbsDeixarFer= Arrays.asList ("deixar", "fer");
  private static final List<String> verbsPortarDur = Arrays.asList ("portar", "dur");
  private static final List<String> lemesEnPerifrasis = Arrays.asList ("pensar", "intentar", "deixar", "estar", "anar", "acabar", "començar", "soler", "tornar", "haver", "anar", "deure", "poder", "voler");//, "a", "de", "en", "pas");
  
  
  private static final List<String> verbsPotencialmentPronominals = Arrays.asList("abaixar", "abandonar", "abarrocar", "abellir", "abismar", "abissar", "ablamar", "ablanir", "abocar", "aboldronar", "abonançar", "abonar", "abonir", "abonyegar", "abordar", "abraonar", "abraçar", "abrivar", "abroquerar", "abrusar", "absentar", "abstraure", "abstreure", "aburgesar", "acabar", "acalar", "acalorar", "acantonar", "acarrerar", "acastellanar", "acatalanar", "accelerar", "acetificar", "acidificar", "aclarir", "aclimatar", "aclivellar", "aclucar", "acoblar", "acollir", "acollonir", "acomiadar", "acomodar", "acomplexar", "acomplir", "aconductar", "aconsellar", "acontentar", "acopar", "acoquinar", "acordar", "acorruar", "acostar", "acostumar", "acotar", "acotxar", "acovardir", "acreditar", "acréixer", "acubar", "acubillar", "acudir", "acugular", "acuitar", "acular", "acumular", "acusar", "adaptar", "adargar", "adherir", "adjudicar", "adollar", "adolorir", "adondar", "adormir", "adossar", "adotzenar", "adreçar", "adscriure", "adunar", "afalconar", "afanyar", "afartar", "afeblir", "afectar", "afermar", "aferrar", "afigurar", "afilar", "afilerar", "afiliar", "afillar", "afinar", "aflaquir", "afligir", "aflonjar", "afluixar", "afogar", "afollar", "afrancesar", "afrevolir", "afuar", "afusar", "agabellar", "agafar", "agarbar", "agarbonar", "agitar", "aglomerar", "aglutinar", "agombolar", "agostejar", "agradar", "agregar", "agremiar", "agreujar", "agrir", "agrisar", "agrumar", "aguantar", "aguditzar", "aigualir", "airejar", "aixecar", "aixoplugar", "ajaure", "ajaçar", "ajeure", "ajornalar", "ajudar", "ajuntar", "ajupir", "ajustar", "alabar", "alarmar", "alcalinitzar", "alcoholitzar", "alegrar", "alentir", "aliar", "alimentar", "alinear", "allarar", "allargar", "allargassar", "allerar", "alleugerir", "alleujar", "alliberar", "alligar", "allistar", "allitar", "allotjar", "allunyar", "alterar", "alzinar", "alçar", "amagar", "amagrir", "amanerar", "amanir", "amansar", "amansir", "amassar", "ambientar", "americanitzar", "amistançar", "amistar", "amollar", "amorar", "amorosir", "amorrar", "amorriar", "amotinar", "amoïnar", "amuntegar", "anastomitzar", "angoixar", "anguniejar", "animar", "anomenar", "anticipar", "apagar", "apaivagar", "apanyar", "aparellar", "apariar", "apartar", "aparèixer", "apassionar", "apercebre", "apilotar", "apinyar", "apitrar", "aplanar", "aplaçar", "aplicar", "apocar", "apoderar", "aposentar", "apostar", "apostemar", "apregonar", "aprendre", "apressar", "aprimar", "aprofitar", "apropar", "apropiar", "aprovisionar", "aproximar", "apujar", "apuntalar", "aquedar", "aquietar", "aquilotar", "arborar", "arbrar", "arcar", "argollar", "aristocratitzar", "armar", "arquejar", "arraconar", "arramadar", "arrambar", "arramellar", "arranjar", "arrapar", "arraulir", "arrear", "arrecerar", "arredossar", "arreglar", "arrelar", "arremangar", "arremolinar", "arremorar", "arrenglerar", "arreplegar", "arrestar", "arribar", "arrimar", "arriscar", "arrissar", "arrodonir", "arromangar", "arrombollar", "arronsar", "arrossegar", "arrufar", "arrugar", "arruïnar", "articular", "asfixiar", "assabentar", "assaonar", "assecar", "assegurar", "assentar", "assenyalar", "asserenar", "assessorar", "asseure", "assimilar", "associar", "assolar", "assolellar", "assossegar", "assotar", "astorar", "atabalar", "ataconar", "atalaiar", "atandar", "atansar", "atapeir", "atardar", "atavellar", "aterrir", "aterrossar", "atipar", "atiplar", "atonir", "atorrollar", "atracar", "atribolar", "atribuir", "atrinxerar", "atrofiar", "atropellar", "atrotinar", "aturar", "avalotar", "avançar", "avarar", "avariar", "avenir", "aventurar", "avergonyir", "avesar", "aviar", "aviciar", "avidar", "avivar", "avorrir", "aïllar", "aïrar", "badar", "balancejar", "balandrejar", "baldar", "banyar", "barallar", "barrejar", "basar", "basquejar", "bastar", "batre", "befar", "bellugar", "beneficiar", "bleir", "blocar", "bolcar", "bombar", "bonificar", "botir", "brindar", "brossar", "bufar", "buidar", "burocratitzar", "cabrejar", "cabussar", "cagar", "calar", "calmar", "calçar", "campar", "cansar", "cap", "capalçar", "capbussar", "capficar", "capgirar", "captar", "captrencar", "caracteritzar", "caragirar", "carbonar", "carbonatar", "carbonitzar", "cardar", "cargolar", "carregar", "cartejar", "casar", "cascar", "cenyir", "cerciorar", "cicatritzar", "circumscriure", "clamar", "classificar", "clavar", "clivellar", "cloure", "coagular", "cobrir", "colar", "colgar", "colltorçar", "colltòrcer", "colrar", "coltellejar", "col·lapsar", "col·legiar", "col·locar", "comanar", "combinar", "compadir", "compaginar", "compatir", "compensar", "complementar", "complexificar", "complicar", "complir", "complànyer", "compondre", "comportar", "comprendre", "comprimir", "comprometre", "compungir", "comunicar", "concentrar", "concertar", "conciliar", "concordar", "concretar", "condemnar", "condensar", "conduir", "confabular", "confederar", "confessar", "confinar", "confirmar", "confitar", "conformar", "congelar", "congestionar", "conglomerar", "conglutinar", "congratular", "congregar", "congriar", "conhortar", "conjuminar", "conjunyir", "conjurar", "connaturalitzar", "consagrar", "conscienciar", "consentir", "conservar", "consolar", "consolidar", "constipar", "consumir", "contagiar", "contaminar", "contemperar", "contenir", "contorbar", "contornar", "contradir", "contraposar", "contreure", "controlar", "convertir", "convèncer", "corbar", "corcar", "cordar", "coronar", "corporificar", "corregir", "correspondre", "corrompre", "corsecar", "cotitzar", "covar", "crebantar", "cremar", "creure", "criar", "crispar", "cucar", "cuidar", "cuixatrencar", "curar", "curullar", "damnar", "debatre", "decantar", "decidir", "declarar", "decuplicar", "decurvar", "dedicar", "defendre", "defensar", "definir", "deformar", "defugir", "degradar", "deixar", "deixatar", "deixondar", "deixondir", "deixuplinar", "delectar", "delir", "delitar", "denudar", "departir", "depauperar", "depilar", "deportar", "depositar", "depravar", "deprimir", "depurar", "derivar", "desabillar", "desabonar", "desabrigar", "desacalorar", "desacoblar", "desaconductar", "desaconduir", "desacordar", "desacostumar", "desacreditar", "desadherir", "desaferrar", "desafinar", "desagafar", "desagermanar", "desagradar", "desagregar", "desajustar", "desalinear", "desamarrar", "desamigar", "desamistançar", "desamorrar", "desanar", "desanimar", "desaparellar", "desapariar", "desaparroquianar", "desaplicar", "desapropiar", "desar", "desarborar", "desarmar", "desarramadar", "desarrambar", "desarranjar", "desarrapar", "desarreglar", "desarregussar", "desarrelar", "desarrengar", "desarrenglar", "desarrenglerar", "desarrimar", "desarrissar", "desarromangar", "desarrufar", "desarrugar", "desarticular", "desassossegar", "desatansar", "desatapeir", "desatendar", "desavesar", "desaveïnar", "desballestar", "desbaratar", "desbarbar", "desbarrar", "desbordar", "desbrancar", "desbraonar", "descabalar", "descabdellar", "descabellar", "descalcificar", "descalçar", "descaminar", "descantellar", "descarbonatar", "descarbonitzar", "descarburar", "descargolar", "descarnar", "descarregar", "descarrerar", "descartar", "descastellanitzar", "descatalanitzar", "descelerar", "descentrar", "descenyir", "desclassar", "desclavar", "descloure", "descoagular", "descobrir", "descolgar", "descollar", "descolorar", "descolorir", "descol·locar", "descompassar", "descompensar", "descompondre", "descomprometre", "descomptar", "desconceptuar", "desconcertar", "desconfortar", "descongelar", "descongestionar", "desconhortar", "desconjuntar", "desconnectar", "descoratjar", "descordar", "descosir", "descotxar", "descrostar", "descular", "desdaurar", "desdelitar", "desdenyar", "desdibuixar", "desdinerar", "desdir", "desdoblar", "desdoblegar", "deseixir", "deselectritzar", "desembabaiar", "desembadalir", "desembadocar", "desemballestar", "desemboirar", "desembolcallar", "desembolcar", "desembolicar", "desembotir", "desembotjar", "desembotornar", "desemboçar", "desembravir", "desembrocar", "desembromallar", "desembromar", "desembullar", "desembussar", "desembutllofar", "desemmandrir", "desemmurriar", "desempallar", "desempastar", "desemperesir", "desempernar", "desempipar", "desempobrir", "desempolainar", "desempolsar", "desempolvorar", "desenamorar", "desencadenar", "desencaixar", "desencalimar", "desencalitjar", "desencallar", "desencaminar", "desencantar", "desencaparrar", "desencapotar", "desencaputxar", "desencarar", "desencarcarar", "desencarranquinar", "desencartonar", "desencastar", "desencaterinar", "desencauar", "desencavalcar", "desencavallar", "desencebar", "desencerclar", "desencercolar", "desencimbellar", "desencisar", "desenclavar", "desencoblar", "desencolar", "desencongir", "desencoratjar", "desencorbar", "desencordillar", "desencrespar", "desencrostar", "desendegar", "desendeutar", "desendogalar", "desendolcir", "desendollar", "desendropir", "desenfadar", "desenfadeir", "desenfarfegar", "desenfellonir", "desenferrissar", "desenfetgegar", "desenfilar", "desenfitar", "desenflocar", "desenfocar", "desenfrenar", "desenfuriar", "desenfurismar", "desengandulir", "desenganxar", "desenganyar", "desengatjar", "desengavanyar", "desengomar", "desengormandir", "desengorronir", "desengreixar", "desengrescar", "desengruixir", "desengrutar", "desenguantar", "desenguerxir", "desenllaminir", "desenllaçar", "desenlleganyar", "desenllepolir", "desenllorar", "desenlluernar", "desenllustrar", "desennuegar", "desennuvolar", "desenquadernar", "desenquadrar", "desenquimerar", "desenrampar", "desenredar", "desenrederar", "desenrolar", "desenrotllar", "desensabonar", "desensenyorir", "desensonyar", "desensopir", "desensuperbir", "desentaular", "desentelar", "desentendre", "desentenebrar", "desentenebrir", "desenterbolir", "desenterrar", "desentestar", "desentortolligar", "desentrampar", "desentranyar", "desentravessar", "desentrecuixar", "desentrenar", "desentristir", "desentumir", "desentusiasmar", "desenutjar", "desenvelar", "desenvernissar", "desenvescar", "desenvolupar", "desenyorar", "desequilibrar", "desertitzar", "desesmar", "desesperançar", "desesperar", "desespessir", "desestancar", "desestanyar", "desestovar", "desfaixar", "desfaiçonar", "desfanatitzar", "desfardar", "desfasar", "desfermar", "desferrar", "desficiar", "desficiejar", "desfigurar", "desfilar", "desflorir", "desfocar", "desfogar", "desfonar", "desfrarar", "desfrenar", "desfrunzir", "desfullar", "desganar", "desgastar", "desgavellar", "desglaçar", "desgraciar", "desgranar", "desgruixar", "desguarnir", "desguerxar", "desguitarrar", "deshabitar", "deshabituar", "deshidratar", "deshumanitzar", "desigualar", "desil·lusionar", "desimantar", "desincorporar", "desincrustar", "desinfatuar", "desinflamar", "desinflar", "desinhibir", "desintegrar", "desinteressar", "desintoxicar", "desionitzar", "desjunyir", "deslligar", "deslliurar", "desllodrigar", "desllogar", "deslloriguerar", "deslluir", "desllustrar", "desmagnetitzar", "desmaiar", "desmallar", "desmanegar", "desmaquillar", "desmarcar", "desmembrar", "desmillorar", "desmoralitzar", "desmorriar", "desmudar", "desmuntar", "desnacionalitzar", "desnaturar", "desniar", "desnierar", "desnivellar", "desnuar", "desnucar", "desobligar", "desobstruir", "desocupar", "desorbitar", "desordenar", "desorganitzar", "desorientar", "despacientar", "desparar", "desparellar", "despariar", "despassar", "despenjar", "despentinar", "despenyar", "despersonalitzar", "despertar", "despintar", "despistar", "despitar", "desplaçar", "desplegar", "desplomar", "despoblar", "despolir", "desposseir", "desprendre", "desprestigiar", "desprisar", "despullar", "despuntar", "desrengar", "desroentar", "dessaborir", "dessagnar", "dessecar", "dessolar", "dessoldar", "dessonillar", "dessoterrar", "dessuar", "dessucar", "destacar", "destapar", "destarotar", "destemprar", "destenyir", "desteular", "destintar", "destorçar", "destravar", "destrempar", "destrenar", "destriar", "destrossar", "destòrcer", "desunglar", "desunir", "desusar", "desvariar", "desvariejar", "desvesar", "desvestir", "desvetllar", "desviar", "desvincular", "desvitrificar", "detenir", "deteriorar", "determinar", "deturar", "devaluar", "dialitzar", "dibuixar", "diferenciar", "difondre", "diftongar", "difuminar", "dignificar", "dilatar", "diluir", "dipositar", "dirigir", "disbauxar", "disciplinar", "disculpar", "disfressar", "disgregar", "disgustar", "dislocar", "disparar", "dispersar", "disposar", "disputar", "disseminar", "dissimilar", "dissipar", "dissociar", "dissoldre", "distanciar", "distendre", "distingir", "distreure", "distribuir", "diversificar", "divertir", "dividir", "divorciar", "divulgar", "doblar", "doblegar", "doctorar", "documentar", "doldre", "domesticar", "domiciliar", "dominar", "donar", "dopar", "dreçar", "drogar", "dubtar", "dulcificar", "duplicar", "dutxar", "eclipsar", "efectuar", "efeminar", "eixamar", "eixamenar", "eixamorar", "eixamplar", "eixancar", "eixancarrar", "eixarrancar", "eixarreir", "eixorivir", "eixugar", "electritzar", "electrocutar", "elevar", "elidir", "emancipar", "embabaiar", "embadalir", "embadocar", "embajanir", "embalar", "embalbar", "embalbir", "embancar", "embarbollar", "embarcar", "embardissar", "embarracar", "embarrancar", "embarranquinar", "embarrar", "embarumar", "embarzerar", "embasardir", "embassar", "embastardir", "embellir", "embeure", "embicar", "emblanquir", "emblavir", "embofegar", "embogir", "emboirar", "embolicar", "emborbollar", "emborratxar", "emboscar", "embossar", "embotinar", "embotir", "emboçar", "embrancar", "embravir", "embretolir", "embriagar", "embrocar", "embrollar", "embromar", "embrossar", "embrunir", "embrutar", "embrutir", "embullar", "embussar", "embutllofar", "embutxacar", "emmagrir", "emmalaltir", "emmaleir", "emmallar", "emmandrir", "emmarcir", "emmaridar", "emmascarar", "emmatxucar", "emmerdar", "emmerdissar", "emmetzinar", "emmirallar", "emmotllar", "emmudir", "emmusteir", "emmustigar", "emocionar", "empadronar", "empal·lidir", "empantanar", "empantanegar", "empanxonar", "empapatxar", "emparar", "emparaular", "emparentar", "emparrar", "empastellar", "empastifar", "empastissar", "empatxar", "empedreir", "empeguntar", "empellar", "empeltar", "empenyorar", "emperesir", "emperlar", "empernar", "empetitir", "empilar", "empinar", "empipar", "empitjorar", "empitrar", "empixonar", "emplenar", "emplomallar", "empobrir", "empolainar", "empolistrar", "empolsar", "empolsegar", "empolsimar", "empolsinar", "empolvorar", "empoquir", "emporcar", "emporprar", "empotingar", "emprendre", "emprenyar", "emprovar", "enagrir", "enamorar", "enamoriscar", "enarborar", "enarbrar", "enarcar", "enardir", "enasprar", "enasprir", "encabassar", "encabir", "encaboriar", "encadarnar", "encadenar", "encaixar", "encalbir", "encalimar", "encalitjar", "encallar", "encallir", "encambrar", "encamellar", "encaminar", "encamisar", "encantar", "encaparrar", "encapellar", "encaperonar", "encaperullar", "encaperutxar", "encapirotar", "encapotar", "encapsular", "encapullar", "encaputxar", "encaramel·lar", "encarar", "encarbonar", "encarir", "encarnar", "encarranquinar", "encarregar", "encarrerar", "encarrilar", "encartonar", "encasquetar", "encastellar", "encauar", "encavallar", "encegar", "encendre", "encepar", "encertir", "encetar", "encimbellar", "enciriar", "enclaustrar", "enclotar", "encloure", "encoblar", "encofurnar", "encoixir", "encomanar", "enconar", "enconcar", "encongir", "encontrar", "encoratjar", "encorbar", "encordar", "encotillar", "encotxar", "encovar", "encrespar", "encreuar", "encrostar", "encrostimar", "encrostissar", "encruelir", "endarreriar", "endarrerir", "endegar", "endentar", "endenyar", "enderrocar", "endeutar", "endinsar", "endogalar", "endolcir", "endolentir", "endossar", "endropir", "endurir", "enemistar", "enervar", "enfadar", "enfadeir", "enfangar", "enfarfegar", "enfarinar", "enfastidir", "enfastijar", "enfellonir", "enfervorir", "enfetgegar", "enfigassar", "enfilar", "enfistular", "enfitar", "enflocar", "enflorar", "enfondir", "enfonsar", "enfonyar", "enforfoguir", "enforinyar", "enfortir", "enfosquir", "enfredar", "enfredolicar", "enfredorar", "enfredorir", "enfrontar", "enfuriar", "enfurir", "enfurismar", "engabiar", "engalavernar", "engallar", "engallardir", "engallir", "engallofir", "engalonar", "engalvanir", "enganar", "engandulir", "enganxar", "enganyar", "engatar", "engatjar", "engelosir", "enginjolar", "enginyar", "engiponar", "englotir", "engolar", "engolir", "engordir", "engorjar", "engormandir", "engorronir", "engrandir", "engreixar", "engrescar", "engrevir", "engroguir", "engronsar", "engronyar", "engrossir", "engruixar", "engruixir", "engrutar", "enguantar", "enguerxir", "enherbar", "enjoiar", "enjoiellar", "enjoncar", "enjullar", "enlairar", "enllacar", "enllaminir", "enllangorir", "enllardar", "enllardissar", "enllaçar", "enllefernar", "enllefiscar", "enllepissar", "enllepolir", "enllestir", "enlletgir", "enllistar", "enllorar", "enllordar", "enllotar", "enllustrar", "ennegrir", "ennoblir", "ennovar", "ennuegar", "ennuvolar", "enorgullar", "enquadrar", "enquibir", "enquimerar", "enrabiar", "enramar", "enrampar", "enrancir", "enrarir", "enrasar", "enravenar", "enredar", "enrederar", "enrederir", "enrellentir", "enretirar", "enrevenxinar", "enriallar", "enrigidir", "enrinxolar", "enriquir", "enrobustir", "enrocar", "enrogir", "enrolar", "enronquir", "enrosar", "enrossir", "enrotllar", "enrullar", "enrunar", "ensabonar", "ensagnar", "ensalivar", "ensangonar", "enseguir", "ensenyorir", "ensonyar", "ensopegar", "ensopir", "ensordir", "ensorrar", "ensotar", "ensulsir", "ensuperbir", "entaforar", "entatxonar", "entaular", "entebeir", "entebionar", "entelar", "entendre", "entendrir", "entenebrar", "entenebrir", "enterbolir", "enterrar", "enterrossar", "entestar", "entollar", "entonar", "entornar", "entortellar", "entortolligar", "entrampar", "entrapar", "entravessar", "entrebancar", "entregar", "entregirar", "entrellaçar", "entrelligar", "entremesclar", "entrenar", "entretenir", "entreveure", "entrevistar", "entristar", "entristir", "entumir", "enturar", "entusiasmar", "enutjar", "envanir", "envellir", "envellutar", "enverdir", "enverinar", "envermellir", "envescar", "enviar", "envigorir", "envilir", "environar", "enviscar", "enviscolar", "envitricollar", "envoltar", "enxarxar", "enxiquir", "enyorar", "equilibrar", "equivaler", "equivocar", "erigir", "eriçar", "errar", "esbadiar", "esbadinar", "esbadocar", "esbalair", "esbaldir", "esbaldregar", "esbandir", "esbardellar", "esbargir", "esbarriar", "esbarzerar", "esberlar", "esbocinar", "esboirar", "esboldregar", "esbombar", "esbombolar", "esborifar", "esborrar", "esborrifar", "esborronar", "esbotifarrar", "esbotzar", "esbrancar", "esbraonar", "esbraveir", "esbullar", "escabellar", "escabellonar", "escabotar", "escaldar", "escaldufar", "escalfar", "escalfeir", "escalivar", "escalonar", "escamarlar", "escamnar", "escampar", "escandalitzar", "escantellar", "escantonar", "escanyar", "escapar", "escarmentar", "escarrabillar", "escarxar", "escaure", "escindir", "esclafar", "esclafassar", "esclarir", "esclerosar", "escolar", "escoltar", "escometre", "escondir", "escotar", "escridar", "escridassar", "escrostar", "escrostissar", "escrostonar", "escruixir", "escuar", "escudar", "escuixar", "escular", "escurçar", "escórrer", "esdernegar", "esdevenir", "esduir", "esfacelar", "esfereir", "esfilagarsar", "esfondrar", "esfreixurar", "esfullar", "esfumar", "esgallar", "esgardissar", "esgarrar", "esgarrifar", "esgarrinxar", "esgarrinyar", "esgarronar", "esgavellar", "esglaonar", "esgotar", "esgratinyar", "esguardar", "esguerrar", "esllenegar", "esllomar", "esmadeixar", "esmalucar", "esmenar", "esmicar", "esmicolar", "esmolar", "esmorrellar", "esmorronar", "esmortir", "esmunyir", "esmussar", "espalmar", "espantar", "espanyolitzar", "espaordir", "espargir", "esparpallar", "esparpillar", "esparracar", "esparverar", "espassar", "espatllar", "espaventar", "espavilar", "especejar", "especialitzar", "espedaçar", "espellifar", "espellir", "espellissar", "espenyar", "esperançar", "esperar", "espesseir", "espessir", "espicassar", "espigar", "espinar", "espitrar", "esplaiar", "esplugar", "espolsar", "espoltrir", "esponjar", "esporuguir", "esposar", "esprémer", "espuar", "espuntar", "espunyir", "espuçar", "esqueixar", "esquerar", "esquerdar", "esquerdillar", "esquerdissar", "esquinçar", "esquitxar", "esquivar", "est", "estabilitzar", "establir", "estacionar", "estalviar", "estamordir", "estancar", "estandarditzar", "estantolar", "estanyar", "estarrufar", "estellar", "estendre", "estepitzar", "estilitzar", "estimbar", "estintolar", "estirar", "estireganyar", "estiuar", "estontolar", "estovar", "estrangeritzar", "estranyar", "estratificar", "estrenar", "estressar", "estretir", "estrinxolar", "estripar", "estroncar", "estropellar", "estrènyer", "estubar", "estufar", "esvair", "esvalotar", "esventar", "esvorar", "esvorellar", "eternitzar", "europeïtzar", "evadir", "evaporar", "exacerbar", "exaltar", "examinar", "exasperar", "excedir", "excitar", "exclamar", "excloure", "exculpar", "excusar", "exercitar", "exfoliar", "exhalar", "exhaurir", "exhibir", "exiliar", "eximir", "exornar", "expandir", "expatriar", "explicar", "exposar", "expressar", "extasiar", "extenuar", "exterioritzar", "extingir", "extraviar", "extremar", "faixar", "familiaritzar", "fanatitzar", "fastiguejar", "fatigar", "federar", "felicitar", "feminitzar", "ferir", "fiar", "ficar", "figurar", "filtrar", "fingir", "firar", "fixar", "flagel·lar", "florir", "folrar", "foraviar", "forcar", "forjar", "formalitzar", "formar", "fortificar", "fossilitzar", "fotre", "fraccionar", "fracturar", "fragmentar", "francesitzar", "franquejar", "fregar", "fregir", "frisar", "fumar", "fundar", "gabar", "gastar", "gaudir", "gelar", "generalitzar", "gestar", "ginyar", "girar", "gitar", "glaçar", "gloriejar", "governar", "graduar", "gramaticalitzar", "gratar", "gratular", "gravar", "grecitzar", "grillar", "gronxar", "gronxejar", "gronxolar", "guanyar", "guardar", "guarir", "guarnir", "guerxar", "guiar", "guillar", "habituar", "hebraïtzar", "hel·lenitzar", "hemodialitzar", "herniar", "hibridar", "hidratar", "hissar", "honorar", "honrar", "horripilar", "horroritzar", "hostatjar", "humanitzar", "humiliar", "humitejar", "identificar", "igualar", "il·luminar", "il·lusionar", "il·lustrar", "imaginar", "immergir", "immolar", "impacientar", "implicar", "imposar", "impressionar", "imprimir", "impurificar", "incarcerar", "incendiar", "inclinar", "incomodar", "incorporar", "incrementar", "incrustar", "independitzar", "indignar", "indisposar", "inebriar", "infatuar", "infectar", "infestar", "infiltrar", "inflamar", "inflar", "informar", "ingerir", "inhabilitar", "inhibir", "iniciar", "inquietar", "inscriure", "insinuar", "inspirar", "instal·lar", "instruir", "insubordinar", "insultar", "insurreccionar", "integrar", "intensificar", "interessar", "interferir", "internar", "interposar", "interrompre", "intranquil·litzar", "introduir", "inundar", "invaginar", "inventar", "ionitzar", "irritar", "islamitzar", "isolar", "jubilar", "jugar", "junyir", "justificar", "lamentar", "laxar", "lignificar", "limitar", "llampar", "llançar", "llassar", "llatinitzar", "llepar", "lletrejar", "llevar", "llicenciar", "lligar", "lliurar", "llogar", "lluir", "localitzar", "lucrar", "macerar", "malacostumar", "malavesar", "maliciar", "mallar", "malpensar", "mamar", "mancomunar", "manegar", "manejar", "manifestar", "mantenir", "maquillar", "marcir", "marejar", "marginar", "maridar", "marinejar", "mascarar", "massificar", "masturbar", "matar", "materialitzar", "matricular", "matxucar", "mecanitzar", "mediumitzar", "menar", "menjar", "mentalitzar", "menysprear", "meravellar", "merèixer", "mesclar", "metal·litzar", "metamorfosar", "meteoritzar", "migrar", "millorar", "mineralitzar", "mirar", "mobilitzar", "mocar", "moderar", "modernitzar", "modificar", "molestar", "morfondre", "morir", "morrejar", "mortificar", "mossegar", "mostrar", "moure", "mudar", "mullar", "multiplicar", "musteir", "mustiar", "mustigar", "mutilar", "nacionalitzar", "naturalitzar", "necrosar", "negar", "neguitejar", "netejar", "nonuplicar", "normalitzar", "nuar", "oblidar", "obligar", "obnubilar", "obscurir", "occidentalitzar", "occitanitzar", "ocultar", "ocupar", "ofegar", "oferir", "ofuscar", "ombrar", "omplir", "operar", "oposar", "ordenar", "orejar", "organitzar", "orgullar", "orientalitzar", "orientar", "originar", "orinar", "oscar", "oxigenar", "pacificar", "paganitzar", "pagar", "pansir", "parapetar", "parar", "parlar", "particularitzar", "partir", "passar", "passejar", "pedregar", "pedrejar", "pellar", "penjar", "pensar", "pentinar", "percaçar", "perfeccionar", "perfilar", "permetre", "persignar", "persuadir", "pessigar", "petar", "picar", "pintar", "pirar", "plantar", "plantejar", "plantificar", "podrir", "polaritzar", "polir", "pol·linitzar", "pondre", "popularitzar", "portar", "posar", "possessionar", "posticar", "postrar", "prear", "precipitar", "prendre", "preocupar", "preparar", "presentar", "prestar", "prevaler", "privar", "proclamar", "prodigar", "produir", "professionalitzar", "proletaritzar", "prometre", "pronunciar", "propagar", "propalar", "proposar", "prostituir", "prostrar", "prou", "proveir", "pujar", "punxar", "purificar", "putejar", "quadrar", "qualificar", "quallar", "quedar", "quitar", "rabejar", "radicalitzar", "rarificar", "ratificar", "reafirmar", "realitzar", "rebaixar", "rebentar", "reblir", "rebolcar", "rebullir", "recargolar", "reciclar", "reciprocar", "recloure", "recobrar", "recollir", "recolzar", "reconcentrar", "reconciliar", "reconstituir", "recordar", "recrear", "recriminar", "rectificar", "reencarnar", "reenganxar", "refer", "referir", "refermar", "reflectir", "refocil·lar", "reforçar", "refractar", "refredar", "refrenar", "refrescar", "refringir", "refugiar", "refusar", "regalar", "regelar", "regirar", "rehabilitar", "rehidratar", "reincorporar", "reinflar", "reinstal·lar", "reintegrar", "rejovenir", "relacionar", "relaxar", "rellentir", "relligar", "rellogar", "remenar", "remetre", "remirar", "remollir", "remudar", "remuntar", "rendir", "renovar", "renovellar", "rentar", "repatriar", "repenjar", "repensar", "repetir", "repintar", "replegar", "replujar", "repodrir", "reportar", "reposar", "representar", "reprimir", "reproduir", "repuntar", "rescabalar", "reservar", "resguardar", "resignar", "resinificar", "resistir", "resoldre", "responsabilitzar", "resquitar", "ressecar", "ressobinar", "restablir", "retardar", "retenir", "retintar", "retirar", "retractar", "retre", "retreure", "retrobar", "reunir", "reveixinar", "revelar", "revellir", "revenxinar", "revestir", "revifar", "reviscolar", "revoltar", "rifar", "rinxolar", "riure", "romanitzar", "rombollar", "rompre", "rostir", "rovellar", "ruboritzar", "russificar", "sacrificar", "salmorrar", "salsir", "salvar", "santificar", "satel·litzar", "secularitzar", "sedimentar", "segar", "segregar", "seguir", "sentir", "senyar", "separar", "significar", "silicificar", "sincerar", "sindicar", "singularitzar", "sinitzar", "situar", "sobrealimentar", "sobreexcitar", "sobreposar", "sobresaltar", "sobresanar", "sobresaturar", "sobtar", "socarrar", "solapar", "solar", "solaçar", "soldar", "solidaritzar", "solidificar", "sollar", "sollevar", "solvatar", "somorgollar", "soplujar", "sostreure", "sotaplujar", "sotmetre", "suberificar", "suberitzar", "subestimar", "submergir", "subscriure", "suggestionar", "sulfatar", "sulfurar", "sumar", "sumir", "superar", "tallar", "tancar", "tant", "tapar", "temperar", "tenyir", "terraplenar", "tirar", "titular", "tocar", "tombar", "torbar", "torejar", "tornar", "torrar", "trabucar", "tractar", "tranquil·litzar", "transfigurar", "transformar", "translimitar", "transmetre", "transmutar", "transparentar", "transvasar", "trasmudar", "trasplantar", "trastocar", "trastornar", "triar", "tribular", "trifurcar", "trobar", "tòrcer", "ulcerar", "ullar", "unir", "universalitzar", "untar", "vaporitzar", "velar", "venjar", "ventar", "vessar", "vestir", "viciar", "vinclar", "vincular", "vitrificar", "volar", "volatilitzar", "xalar", "xutar");
  private static final List<String> excepVerbsPotencialmentPronominals = Arrays.asList("voler");
  
  private static final List<String> verbHaver= Arrays.asList("haver");
  
  private static final Pattern NO_VERB = Pattern.compile("N.*|A.*|_GN_.*");
  
  // V[MAS][ISMNGP][PIFSC0][123][SP][MF] 
  private static final Pattern VERB= Pattern.compile("V.*");
  private static final Pattern VERB_INDSUBJ = Pattern.compile("V.[SI].*");
  private static final Pattern VERB_INDSUBJIMP = Pattern.compile("V.[MSI].*");
  private static final Pattern VERB_IMP = Pattern.compile("V.M.*");
  private static final Pattern VERB_INF = Pattern.compile("V.N.*");
  private static final Pattern VERB_INFGER = Pattern.compile("V.[NG].*");
  private static final Pattern VERB_GERUNDI = Pattern.compile("V.G.*");
  private static final Pattern VERB_PARTICIPI = Pattern.compile("V.P.*");
  private static final Pattern VERB_AUXILIAR = Pattern.compile("VA.*");
  private static final Pattern PREP_VERB_PRONOM = Pattern.compile("RN|SPS00|V.*|P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP3CP000|PP3CSD00");
  private static final Pattern PREP_VERB_PRONOM_ADV = Pattern.compile("RG.*|.*LOC_ADV.*|SPS00|V.*|P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP3CP000|PP3CSD00");
  //potser convé diferenciar la coma(,) de les cometes(") en _PUNCT_CONT -> no incloure la coma
  private static final List<String> cometes = Arrays.asList("\"", "'", "‘", "’", "“", "”", "«", "»");
  private static final Pattern VERB_PRONOM = Pattern.compile("V.*|P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP3CP000|PP3CSD00");
  //cal restringir les preposicions  
  
  private static final Pattern VERB_1S = Pattern.compile("V...1S..?");
  private static final Pattern VERB_2S = Pattern.compile("V...2S..?");
  private static final Pattern VERB_3S = Pattern.compile("V...3S..?");
  private static final Pattern VERB_1P = Pattern.compile("V...1P..?");
  private static final Pattern VERB_2P = Pattern.compile("V...2P..?");
  private static final Pattern VERB_3P = Pattern.compile("V...3P..?");
  
  private static final Pattern PRONOM_FEBLE_1S = Pattern.compile("P010S000");
  private static final Pattern PRONOM_FEBLE_2S = Pattern.compile("P020S000");
  private static final Pattern PRONOM_FEBLE_3S = Pattern.compile("P0300000");
  private static final Pattern PRONOM_FEBLE_1P = Pattern.compile("P010P000");
  private static final Pattern PRONOM_FEBLE_2P = Pattern.compile("P020P000");
  private static final Pattern PRONOM_FEBLE_3P = Pattern.compile("P0300000");
  private static final Pattern PRONOM_FEBLE_13S = Pattern.compile("P010S000|P0300000");
  private static final Pattern PRONOM_FEBLE_23S = Pattern.compile("P020S000|P0300000");
  
  private static final Pattern PRONOM_FEBLE_3S_TOTS = Pattern.compile("P.3.[^PN].*");
  
  private static final Pattern PRONOM_FEBLE = Pattern.compile("P0.{6}|PP3CN000|PP3NN000|PP3..A00|PP3CP000|PP3CSD00"); // tots els pronoms febles
  private static final Pattern PRONOM_REFLEXIU = Pattern.compile("P0.0.*"); //me te se ens us (i variants)
  //private static final Pattern PRONOM_FEBLE_GUIONET = Pattern.compile("-.+");
  
  private static final Pattern LEMMA_EN = Pattern.compile("en");
  private static final Pattern POSTAG_EN = Pattern.compile("PP3CN000");
  private static final Pattern LEMMA_HI = Pattern.compile("hi");
  private static final Pattern POSTAG_HI = Pattern.compile("PP3CN000");
  private static final Pattern LEMMA_ES = Pattern.compile("es");
  private static final Pattern POSTAG_ES = Pattern.compile("P0300000");
  private static final Pattern LEMMA_PRONOM_CI = Pattern.compile("jo|tu|ell");
  private static final Pattern POSTAG_PRONOM_CI = Pattern.compile("P0.*|PP3CP000|PP3CSD00");
  private static final Pattern LEMMA_PRONOM_CD = Pattern.compile("jo|tu|ell");
  private static final Pattern POSTAG_PRONOM_CD = Pattern.compile("P0.*|PP3CP000|PP3..A00");
  private static final Pattern POSTAG_CD = Pattern.compile("_GN_.*|N.*|DI.*|P[DI].*");
  private static final Pattern LEMMA_DE = Pattern.compile("de");
  private static final Pattern POSTAG_DE = Pattern.compile("SPS00");
  private static final Pattern POSTAG_PREPOSICIO = Pattern.compile("SPS00");
  private static final Pattern LEMMA_PREP_A_PER = Pattern.compile("a|per");
  private static final Pattern POSTAG_PRONOM_CD_3P = Pattern.compile("PP3CP000|PP3..A00");
  
  private static final Pattern POSTAG_ADVERBI = Pattern.compile("RG.*|.*LOC_ADV.*");
  private static final Pattern ANYMESDIA = Pattern.compile("any|mes|dia");
  
  private static final Pattern REFLEXIU_POSPOSAT = Pattern.compile("-[mts]|-[mts]e|'[mts]|-nos|'ns|-vos|-us",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  //private static final Pattern REFLEXIU_ANTEPOSAT = Pattern.compile("e[mts]|[mts]e|ens|us|-[mts]|-[mts]e|'[mts]|[mts]'|-nos|'ns|-vos|-us",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern REFLEXIU_ANTEPOSAT = Pattern.compile("e[mts]|[mts]e|ens|us|vos|'[mts]|[mts]'|'ns",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  
  private static final Pattern PRONOMFEBLE_POSPOSAT = Pattern.compile("['-].+");
  
  private static final Pattern SUBJECTE_PERSONAL_POSTAG = Pattern.compile("NC.*|NP.*|_GN_.*|PI.*|_possible_nompropi");
  private static final Pattern SUBJECTE_PERSONAL_NO_POSTAG = Pattern.compile("complement.*|D.*|PX.*"); //|A.*
  private static final Pattern SUBJECTE_PERSONAL_TOKEN = Pattern.compile("algú|algun|jo|mi|tu|ella?|nosaltres|vosaltres|elle?s|vost[èé]s?|vós",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern SUBJECTE_PERSONAL_NO_LEMMA = Pattern.compile("dia|any|mes|segle|dilluns|dimarts|dimecres|dijous|divendres|dissabte|diumenge|gener|febrer|març|abril|maig|juny|juliol|agost|setembre|octubre|novembre|desembre");
  // en general expressió temporal
  
  private static final Pattern SUBJECTE_PERSONAL_SING_POSTAG = Pattern.compile("N..[SN].*|_GN_.S|PI..[SN].*|_possible_nompropi|UNKNOWN");
  private static final Pattern SUBJECTE_PERSONAL_SING_TOKEN = Pattern.compile("algú|algun|jo|mi|tu|ella?|vost[èé]|vós",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern SUBJECTE_PERSONAL_PL_POSTAG = Pattern.compile("N..[PN].*|_GN_.P|PI..[PN].*|_possible_nompropi|UNKNOWN");
  private static final Pattern SUBJECTE_PERSONAL_PL_TOKEN = Pattern.compile("alguns|nosaltres|vosaltres|elle?s|vost[èé]s",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

  private static final Pattern SUBJECTE_3S_POSTAG = Pattern.compile("N..[SN].*|_GN_.S|PI..[SN].*");
  private static final Pattern SUBJECTE_3S_TOKEN = Pattern.compile("algú|algun|ella?|vost[èé]|vós",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern SUBJECTE_3S_NO_POSTAG = Pattern.compile("complement.*");
  private static final Pattern SUBJECTE_3S_NO_TOKEN = Pattern.compile("jo|tu|mi|nosaltres|vosaltres|elle?s",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  
  
  private static final Pattern TRENCA_COMPTE = Pattern.compile("PR.*|CS|CC|_PUNCT.*|.*LOC_CONJ.*");
  private static final Pattern TRENCA_COMPTE2 = Pattern.compile("SENT_START|CC|_PUNCT.*|.*LOC_CONJ.*");
  
  private static final List<String> partsCos = Arrays.asList("pit", "galta", "cap", "cor", "cara", "ull", "front", "mà", "peu", "braç", "colze", "genoll", "cabell", "llavi");
  private static final List<String> contextBaixar = Arrays.asList("fitxer", "arxiu", "paquet", "instal·lació", "versió", "programa", "programari", "software", "virus", "antivirus", "URL", "web", "pàgina", "instal·lar", "IS_URL", "imatge", "pel·lícula", "foto", "fotografia");
  
  private static final List<String> pronomJo = Arrays.asList("jo");
 // <token postag="P0.*|PP.*" postag_regexp="yes"><exception postag="_GN_.*" postag_regexp="yes"/><exception regexp="yes">jo|mi|tu|ella?|nosaltres|vosaltres|elle?s|vost[èé]s?|vós</exception><exception postag="allow_saxon_genitive">'s</exception></token>
  
   
  public ReflexiveVerbsRule(ResourceBundle messages) throws IOException {
    super.setCategory(new Category(new CategoryId("VERBS"), "Verbs"));
    setLocQualityIssueType(ITSIssueType.Grammar);
    addExamplePair(Example.wrong("El xiquet s'ha <marker>caigut</marker> de la bicicleta."),
                   Example.fixed("El xiquet ha <marker>caigut</marker> de la bicicleta."));
    addExamplePair(Example.wrong("<marker>Calleu</marker>-vos."),
                   Example.fixed("<marker>Calleu</marker>."));
  }

  
  @Override
  public String getId() {
    return "VERBS_REFLEXIUS";
  }

  @Override
  public String getDescription() {
    return "Verbs reflexius: comproveu que porten el pronom adequat.";
  }

  @Override
  public RuleMatch[] match(final AnalyzedSentence sentence) {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    loop: for (int i = 1; i < tokens.length; i++) { // ignoring token 0, i.e., SENT_START

      //exceptions, dealt with in grammar.xml
      if (tokens[i].getToken().equalsIgnoreCase("industria") || 
          tokens[i].getToken().equalsIgnoreCase("industries")) {
        continue loop;
      }
      
      //ignore uppercase words unless at the sentence start
      if (i > 1 && StringTools.startsWithUppercase(tokens[i].getToken())) {
        continue loop;
      }
      //ignore no verbs
      if (!matchPostagRegexp(tokens[i], VERB) || matchPostagRegexp(tokens[i], NO_VERB))
        continue loop;
      
      final String token = tokens[i].getToken().toLowerCase();     
      
      //COMPROVA: *donar-se compte/adonar-se
      if (i+2<tokens.length
          && tokens[i].hasLemma("donar")
          && (tokens[i+1].getToken().equals("compte") || tokens[i+2].getToken().equals("compte"))) {        
        if (!isThereReflexivePronoun(tokens, i)) 
          continue loop;
                
        //excep. Frase impersonal
        // És frase impersonal si hi ha el pronom 'es', llevat que es pugui identificar un subjecte "personal"
        if (isPhraseImpersonalVerbS(tokens, i) )  
          continue loop;
        
        // the rule matches
        final String msg = "'Donar-se compte' és una expressió incorrecta si equival a 'adonar-se'; és correcta si vol dir 'retre compte'.";
        int endPosition = 1;
        if (tokens[i+2].getToken().equals("compte")) {
          endPosition = 2;
        }
        final RuleMatch ruleMatch = new RuleMatch(this, sentence, tokens[i].getStartPos(),
            tokens[i + endPosition].getEndPos(), msg, "Possible error");
        ruleMatches.add(ruleMatch);
      }
      
      // COMPROVA: portar-se/emportar-se
      if (i+2<tokens.length
          && matchLemmaList(tokens[i], verbsPortarDur)
          && !(matchPostagRegexp(tokens[i], VERB_INF) && isThereBefore(tokens,i,LEMMA_PREP_A_PER,POSTAG_PREPOSICIO))
          && !hasVerbMultipleReadings(tokens[i]) //em duràs un mocador
          && isThereReflexivePronoun(tokens, i) // ens portem, ens hem de portar
          && isThereAfterWithoutPreposition(tokens, i, POSTAG_CD)
          && !isThereVerbBeforeList(tokens,i,verbsDeixarFer) // es deixen portar
          && !(isThereVerbBeforeList(tokens,i,verbsPotencialmentPronominals) && !isThereVerbBeforeList(tokens,i,excepVerbsPotencialmentPronominals))
          && !matchPostagRegexp(tokens[i+1], POSTAG_ADVERBI) // es porten bé
          && !matchPostagRegexp(tokens[i+2], POSTAG_ADVERBI) // hem de portar-nos bé
          && !matchLemmaRegexp(tokens[i+2], ANYMESDIA) // ens portem tres anys
          && !isPhraseImpersonalVerbSP(tokens, i) // Es va portar l'any passat
          ) {
        if (isVerbNumberPerson(tokens,i,VERB_3S)  //el vent m'ha portat les rondalles 
            && !isThereBefore(tokens, i, LEMMA_ES, POSTAG_ES)
            && isThereSubject3SBefore(tokens,i,TRENCA_COMPTE))
          continue loop;
        if (isThereNearLemma (tokens, i, partsCos))
          continue loop;
        
        // the rule matches
        String suggestion;
        if (tokens[i].hasLemma("portar")) {suggestion = "em"+token; }
          else if (token.equalsIgnoreCase("du")) {suggestion ="endú"; }
          else {suggestion= "en"+token; }
        final String msg="¿Volíeu dir <suggestion>"+suggestion+"</suggestion>?";
        final RuleMatch ruleMatch = new RuleMatch(this, sentence,
            tokens[i].getStartPos(), tokens[i].getEndPos(), msg, "Possible error");
        ruleMatches.add(ruleMatch);    
        continue loop;
      }
      
      //COMPROVA: PERÍFRASI AMB VERB PRONOMINAL: el fan *agenollar-se/agenollar
      if (i+1<tokens.length 
          && matchPostagRegexp(tokens[i], VERB_INF)
          && !matchPostagRegexp(tokens[i - 1], POSTAG_PREPOSICIO) 
          && isThereVerbBeforeListLimit(tokens,i, verbsDeixarFer,3)
          && isThereRedundantPronoun(tokens,i)
          && isThereBefore(tokens, i, LEMMA_PRONOM_CD, POSTAG_PRONOM_CD)  
          && matchRegexp(tokens[i + 1].getToken(), REFLEXIU_POSPOSAT) ) {
          // the rule matches
          final String msg = "En aquesta perífrasi verbal el pronom reflexiu posterior és redundant.";
          final RuleMatch ruleMatch = new RuleMatch(this, sentence,
              tokens[i+1].getStartPos(), tokens[i+1].getStartPos()
                  + tokens[i+1].getToken().length(), msg, "Pronom redundant");
          ruleMatches.add(ruleMatch);
          continue loop;
      }

      //VERBS PRONOMINALS: Cal que hi hagi pronom reflexiu. 
      if (matchLemmaRegexp(tokens[i], VERB_AUTO) || matchLemmaList(tokens[i],verbsPronominals)) {
        if (matchLemmaList(tokens[i],excepVerbsPronominals)) // atengué l'administració 
          continue loop;
        if (matchPostagRegexp(tokens[i], VERB_PARTICIPI) && !tokens[i-1].hasLemma("haver")) 
          continue loop;
        if (isThereVerbBeforeList(tokens,i, verbsDeixarFer)  // el fa agenollar
            && (isThereBefore(tokens, i, LEMMA_PRONOM_CD, POSTAG_PRONOM_CD)
                || isThereBefore(tokens, i, LEMMA_PRONOM_CI, POSTAG_PRONOM_CI)
                || isThereAfterWithoutPreposition(tokens, i, POSTAG_CD))) //van fer agenollar els presos
          continue loop;
        if (isThereReflexivePronoun(tokens, i)) 
          continue loop;
        // the rule matches
        final String msg = "Aquest verb és pronominal. Probablement falta un pronom.";
        final RuleMatch ruleMatch = new RuleMatch(this, sentence,
            tokens[i].getStartPos(), tokens[i].getEndPos(), msg,
            "Verb pronominal: falta un pronom");
        ruleMatches.add(ruleMatch);
        continue loop;
      }
      
      //VERBS NO PRONOMINALS: No hi ha d'haver pronom reflexiu. 
      if (matchLemmaList(tokens[i], verbsNoPronominals)) {
        if (matchLemmaList(tokens[i], excepVerbsNoPronominals))
          continue loop;        
        if (!isThereReflexivePronoun(tokens, i)) 
          continue loop;
        if (tokens[i].hasLemma("baixar") && isThereNearLemma (tokens, i, contextBaixar))
            continue loop;
        //impersonal obligació: s'ha de baixar
        if (matchLemmaList(tokens[i],verbsNoPronominalsImpersonals2)
            && isThereBefore(tokens, i, LEMMA_ES, POSTAG_ES)
            && isThereBefore(tokens, i, LEMMA_DE, POSTAG_DE)
            && isThereVerbBeforeList(tokens,i,verbHaver) )
            continue loop;
        if (isThereVerbBeforeList(tokens,i,verbsSovintAmbComplement)
            || (isThereVerbBeforeList(tokens,i,verbsPotencialmentPronominals)&&!isThereVerbBeforeList(tokens,i,excepVerbsPotencialmentPronominals))
            || isThereVerbBefore(tokens,i,VERB_AUTO)
            || isThereVerbBeforeList(tokens,i,verbsPronominals)) //et deixes caure, et fas témer, 
          continue loop;
        //FRASE IMPERSONAL
        // És frase impersonal si hi ha el pronom 'es', llevat que es pugui identificar un subjecte "personal"
        if (matchLemmaList(tokens[i],verbsNoPronominalsImpersonals)
            && isPhraseImpersonalVerbS(tokens, i) )  
          continue loop;
        if (matchLemmaList(tokens[i],verbsNoPronominalsImpersonals2)
            && isPhraseImpersonalVerbSP(tokens, i) )  
          continue loop;
        if (tokens[i].hasLemma("olorar") && isThereNearLemma (tokens, i, partsCos))
          continue loop;
        
        // the rule matches
        final String msg = "Aquest verb no és pronominal. Probablement sobra un pronom.";
        final RuleMatch ruleMatch = new RuleMatch(this, sentence,
            tokens[i].getStartPos(), tokens[i].getEndPos(),
            msg, "Verb no pronominal");
        if (tokens[i].hasLemma("créixer")) {
          ArrayList<String> replacements = new ArrayList<>();
          replacements.add("(encoratjar-se)");
          replacements.add("(animar-se)");
          replacements.add("(agafar ànim)");
          ruleMatch.setSuggestedReplacements(replacements);
        }
        ruleMatches.add(ruleMatch);
      }
      
      //VERBS DE MOVIMENT: si hi ha pronom reflexiu cal el pronom 'en'.
      if (matchLemmaList(tokens[i], verbsMoviment) && !matchPostagRegexp(tokens[i], VERB_AUXILIAR)) {
        if (matchLemmaList(tokens[i], excepVerbsMoviment)) 
          // atengué l'administració
          continue loop;
        //impersonal obligació: s'ha de baixar
        if (isThereBefore(tokens, i, LEMMA_ES, POSTAG_ES)
            && isThereBefore(tokens, i, LEMMA_DE, POSTAG_DE)
            && isThereVerbBeforeList(tokens,i,verbHaver) )
            continue loop;
        if (isThereVerbBeforeList(tokens,i,verbsSovintAmbComplement) // per venir-vos a veure
            || (isThereVerbBeforeList(tokens,i,verbsPotencialmentPronominals)&&!isThereVerbBeforeList(tokens,i,excepVerbsPotencialmentPronominals))
            || isThereVerbBefore(tokens,i,VERB_AUTO)
            || isThereVerbBeforeList(tokens,i,verbsPronominals)) //et deixes anar/pujar  
          continue loop;
        if (isVerbNumberPerson(tokens,i,VERB_3S)  //em puja al cap 
            && !isThereBefore(tokens, i, LEMMA_ES, POSTAG_ES)
            && isThereNearLemma(tokens, i, partsCos))
          continue loop;
        if (tokens[i].hasLemma("venir") || tokens[i].hasLemma("anar")) { //Em va bé
          if (i+1<tokens.length 
              && isVerbNumberPerson(tokens,i,VERB_3S) 
              && !isThereBefore(tokens, i, LEMMA_ES, POSTAG_ES)
              && matchPostagRegexp(tokens[i+1],POSTAG_ADVERBI)
              && !isThereNearWord(tokens, i, pronomJo) )
            continue loop;        
        }
        if (tokens[i].hasLemma("venir")) {
          if (i+2<tokens.length 
              && tokens[i+1].getToken().equals("de") && tokens[i+2].getToken().equals("gust"))
            continue loop;
          if (isVerbNumberPerson(tokens,i,VERB_3S) //em vingui la inspiració
              && !isThereBefore(tokens, i, LEMMA_ES, POSTAG_ES)
              && isThereAfterWithoutPreposition(tokens, i, POSTAG_CD)
              && !isThereNearWord(tokens, i, pronomJo) )
            continue loop; 
          if (isThereAfter(tokens, i, VERB_INF))
            continue loop;
        }
        if (tokens[i].hasLemma("anar")) {
          if (isThereAfter(tokens, i, VERB_GERUNDI))
            continue loop;
          if (isThereVerbAfterList(tokens,i,verbsPotencialmentPronominals)
              || isThereVerbAfter(tokens,i,VERB_AUTO)
              || isThereVerbAfterList(tokens,i,verbsPronominals))
            continue loop;
          if (isVerbNumberPerson(tokens,i,VERB_3S) 
              && !isThereBefore(tokens, i, LEMMA_ES, POSTAG_ES)
              && isThereSubject3SBefore(tokens,i,TRENCA_COMPTE))
            continue loop;
          //FRASE IMPERSONAL
          /*if (isThereBefore(tokens, i, LEMMA_ES, POSTAG_ES)
              && !isThereBefore(tokens, i, LEMMA_PRONOM_CI, POSTAG_PRONOM_CI)
              && (!isTherePersonalSubjectBefore(tokens,i,TRENCA_COMPTE) || isThereBefore(tokens, i, LEMMA_HI, POSTAG_HI)) 
              && isVerbNumberPerson(tokens,i,VERB_3S))*/
          if (isPhraseImpersonalVerbS(tokens,i))
            continue loop;
        }
        else {
          // FRASE IMPERSONAL
          if (isThereBefore(tokens, i, LEMMA_ES, POSTAG_ES)
              && !isThereBefore(tokens, i, LEMMA_PRONOM_CI, POSTAG_PRONOM_CI)
              && !isTherePersonalSubjectBefore(tokens, i,  TRENCA_COMPTE))
            continue loop;
        }
        if (isThereReflexivePronoun(tokens, i) && (!isTherePronoun(tokens, i, LEMMA_EN, POSTAG_EN))) {
          // the rule matches
          final String msg = "No useu com a pronominal aquest verb, o bé afegiu-hi el pronom 'en'."; //Cal canviar el missatge
          final RuleMatch ruleMatch = new RuleMatch(this, sentence, tokens[i].getStartPos(), 
              tokens[i].getEndPos(), msg, "Falta el pronom 'en'");
          ruleMatches.add(ruleMatch);
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  /**
   * Find appropiate pronoun pattern. (Troba el pronom feble apropiat)
   */
  @Nullable
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
   * El verb té múltiples lectures
   */ 
  private boolean hasVerbMultipleReadings (AnalyzedTokenReadings aToken) {
    return (matchPostagRegexp(aToken,VERB_1S) && matchPostagRegexp(aToken,VERB_3S))
        || (matchPostagRegexp(aToken,VERB_2S) && matchPostagRegexp(aToken,VERB_3S));
  }
  
  /**
   * Match POS tag with regular expression
   */
  private boolean matchPostagRegexp(AnalyzedTokenReadings aToken, Pattern pattern) {
    boolean matches = false;
    for (AnalyzedToken analyzedToken : aToken) {
      String posTag = analyzedToken.getPOSTag();
      if (posTag == null) {
        posTag = "UNKNOWN";
      }
      final Matcher m = pattern.matcher(posTag);
      if (m.matches()) {
        matches = true;
        break;
      }
    }
    return matches;
  }
  
  /**
   * Match POS tag 
   */
  private boolean matchPostag(AnalyzedTokenReadings aToken, String postag) {
    for (AnalyzedToken analyzedToken : aToken) {
      String p = analyzedToken.getPOSTag();
      if (p != null && p.equals(postag)) {
        return true;
      }
    }
    return false;
  }
  
  private boolean haveSamePostag(AnalyzedTokenReadings aToken, AnalyzedTokenReadings aToken2) {
    if (!aToken.getReadings().get(0).hasNoTag() 
        && !aToken2.getReadings().get(0).hasNoTag()) {
      return StringUtils.equals(aToken.getReadings().get(0).getPOSTag(), aToken2.getReadings().get(0).getPOSTag());
    }
    return false;
  }
  
  /**
   * Match lemma with regular expression
   */
  private boolean matchLemmaRegexp(AnalyzedTokenReadings aToken,
      Pattern pattern) {
    boolean matches = false;
    for (AnalyzedToken analyzedToken : aToken) {
      final String posTag = analyzedToken.getLemma();
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
   * Match lemma with String list
   */
  private boolean matchLemmaList(AnalyzedTokenReadings aToken,
      List<String> list) {
    boolean matches = false;
    for (AnalyzedToken analyzedToken : aToken) {
      if (list.contains(analyzedToken.getLemma())) {
          matches = true;
          break;
      }
    }
    return matches;
  }
  
  /**
   * Match String with regular expression
   */
  private boolean matchRegexp(String s, Pattern pattern) {
    final Matcher m = pattern.matcher(s);
    return m.matches();
  }
  
  /**
   * Checks if there is a reflexive pronoun near the verb
   */
  private boolean isThereReflexivePronoun(
      final AnalyzedTokenReadings[] tokens, int i) {
    Pattern pPronomBuscat = null;
    // 1) es queixa, se li queixa, se li'n queixa
    if (matchPostagRegexp(tokens[i], VERB_INDSUBJ)) {
      pPronomBuscat = pronomPattern(tokens[i]);
      if (pPronomBuscat != null) {
        int j = 1;
        boolean keepCounting = true;
        while (i - j > 0 && j < 4 && keepCounting) {
          if (matchPostagRegexp(tokens[i - j], pPronomBuscat)
              && matchRegexp(tokens[i -j].getToken(), REFLEXIU_ANTEPOSAT))
            return true;
          keepCounting = matchPostagRegexp(tokens[i - j],
              PRONOM_FEBLE);
          j++;
        }
      }
    }
    // 2) queixa't, queixeu-vos-hi
    if (matchPostagRegexp(tokens[i], VERB_IMP)) {
      pPronomBuscat = pronomPattern(tokens[i]);
      if (pPronomBuscat != null) {
        if (i+1<tokens.length
            && matchPostagRegexp(tokens[i + 1], pPronomBuscat)
            && matchRegexp(tokens[i + 1].getToken(), REFLEXIU_POSPOSAT))
          return true; 
      }
    }
    // 3) s'ha queixat, se li ha queixat, se li n'ha queixat.
    if (matchPostagRegexp(tokens[i], VERB_PARTICIPI)) {
      if (tokens[i - 1].hasLemma("haver")) {
        if (matchPostagRegexp(tokens[i - 1], VERB_INDSUBJ)) {
          pPronomBuscat = pronomPattern(tokens[i - 1]);
          if (pPronomBuscat != null) {
            int j = 2;
            boolean keepCounting = true;
            while (i - j > 0 && j < 5 && keepCounting) {
              if (matchPostagRegexp(tokens[i - j], pPronomBuscat)
                  && matchRegexp(tokens[i - j].getToken(), REFLEXIU_ANTEPOSAT))
                return true;
              keepCounting = matchPostagRegexp(tokens[i - j], PRONOM_FEBLE);
              j++;
            }
          }
        }
        // es podria haver endut
        else if (matchPostagRegexp(tokens[i - 1], VERB_INF)
            && matchPostagRegexp(tokens[i - 2], VERB_INDSUBJ)) {
          pPronomBuscat = pronomPattern(tokens[i - 2]);
          if (pPronomBuscat != null) {
            int j = 3;
            boolean keepCounting = true;
            while (i - j > 0 && j < 5 && keepCounting) {
              if (matchPostagRegexp(tokens[i - j], pPronomBuscat)
                  && matchRegexp(tokens[i - j].getToken(), REFLEXIU_ANTEPOSAT))
                return true;
              keepCounting = matchPostagRegexp(tokens[i - j], PRONOM_FEBLE);
              j++;
            }
          }
        }
      }
      // *havent queixat, *haver queixat
//      else if (!(matchLemmaRegexp(tokens[i - 1], VERB_HAVER) && matchPostagRegexp(
//          tokens[i - 1], VERB_INFGER)))
//        return true;
    }
    // 4) em vaig queixar, se li va queixar, se li'n va queixar, vas
    // queixar-te'n,
    // em puc queixar, ens en podem queixar, podeu queixar-vos,
    // es va queixant, va queixant-se, comences queixant-te
    // 5) no t'has de queixar, no has de queixar-te, pareu de queixar-vos,
    // comenceu a queixar-vos
    // corre a queixar-se, corre a queixar-te, vés a queixar-te
    // no hauria pogut burlar-me
    // 6) no podeu deixar de queixar-vos, no us podeu deixar de queixar
    // en teniu prou amb queixar-vos, comenceu lentament a queixar-vos
    // 7) no es va poder emportar, va decidir suïcidar-se,
    // 8) Queixar-se, queixant-vos, podent abstenir-se
    //TODO: simplify this code using code in PronomFebleDuplicate
    if (matchPostagRegexp(tokens[i], VERB_INFGER)) {
      int k = 1;
      boolean keepCounting = true;
      boolean foundVerb = false;
      while (i - k > 0 && keepCounting && !foundVerb) {
        foundVerb = matchPostagRegexp(tokens[i - k], VERB_INDSUBJIMP);
        keepCounting = matchPostagRegexp(tokens[i - k],
            PREP_VERB_PRONOM);
        if (matchPostagRegexp(tokens[i-k],VERB_INDSUBJ)
            && matchPostagRegexp(tokens[i-k+1],VERB_INFGER))
          keepCounting=false;
        if (matchPostagRegexp(tokens[i - k], VERB_INFGER) //pertanyen a grups verbals diferents
            && matchPostagRegexp(tokens[i - k + 1], PRONOM_FEBLE)
            && !matchRegexp(tokens[i - k + 1].getToken(), PRONOMFEBLE_POSPOSAT)) {
          keepCounting=false;
        }
        k++;
      }
      if (foundVerb) {
        k--;
        // us animem a queixar-vos
        if (i + 1 < tokens.length && i - k - 1 > 0) {
          if ((i - 1 > 0 && (tokens[i - 1].hasLemma("a") || tokens[i - 1].hasLemma("agradar") )
              || (i - 2 > 0 && (tokens[i - 2].hasLemma("a") || tokens[i - 2].hasLemma("agradar") )))) {
            if (haveSamePostag(tokens[i - k - 1], tokens[i + 1])) {
              return true;
            }
            //l'animem a queixar-se
            if (matchPostagRegexp(tokens[i - k - 1], POSTAG_PRONOM_CD_3P)
                && matchPostag(tokens[i + 1], "P0300000")
                && matchRegexp(tokens[i + 1].getToken(), REFLEXIU_POSPOSAT)) {
              return true;
            }
          }
        }
        
        pPronomBuscat = pronomPattern(tokens[i - k]);
        if (pPronomBuscat != null) {
          if (i+1< tokens.length
              && matchPostagRegexp(tokens[i + 1], pPronomBuscat)
              && matchRegexp(tokens[i + 1].getToken(), REFLEXIU_POSPOSAT))
            return true;
          int j = 1;
          keepCounting = true;
          while (i - j > 0 && keepCounting) {
            if (j==1 && matchPostagRegexp(tokens[i - j], pPronomBuscat))
              return true;
            if (j>1 && matchPostagRegexp(tokens[i - j], pPronomBuscat)
                && !(matchRegexp(tokens[i - j].getToken(), REFLEXIU_POSPOSAT) && j>k))
              return true;
            keepCounting = matchPostagRegexp(tokens[i - j], PREP_VERB_PRONOM)
                && (matchLemmaList(tokens[i - j], lemesEnPerifrasis) || !matchPostagRegexp(tokens[i - j], VERB))
                && !(j>k-1 && matchPostagRegexp(tokens[i - j], VERB_PARTICIPI))
                && !matchPostagRegexp(tokens[i - j], TRENCA_COMPTE2);
            if (tokens[i-j].getToken().equalsIgnoreCase("per")
                && tokens[i-j+1].getToken().equalsIgnoreCase("a")) {
              keepCounting=false;
            }
            if (matchPostagRegexp(tokens[i - j], VERB_INFGER) // pertanyen a grups verbals diferents
                && matchPostagRegexp(tokens[i - j + 1], PRONOM_FEBLE)
                && !matchRegexp(tokens[i - j + 1].getToken(), PRONOMFEBLE_POSPOSAT)) {
              keepCounting=false;
            }
            j++;
          }
        }
      } else {
        if (i+1<tokens.length
            && matchPostagRegexp(tokens[i + 1], PRONOM_REFLEXIU)
            && matchRegexp(tokens[i + 1].getToken(), REFLEXIU_POSPOSAT))
          return true;
        int j = 1;
        keepCounting = true;
        while (i - j > 0 && keepCounting) {
          if (matchPostagRegexp(tokens[i - j], PRONOM_REFLEXIU))
            return true;
          keepCounting = matchPostagRegexp(tokens[i - j],
              PREP_VERB_PRONOM);
          if (tokens[i-j].getToken().equalsIgnoreCase("per")
              && tokens[i-j+1].getToken().equalsIgnoreCase("a")) {
            keepCounting=false;
          }
          if (matchPostagRegexp(tokens[i - j], VERB_INFGER) // pertanyen a grups verbals diferents
              && matchPostagRegexp(tokens[i - j + 1], PRONOM_FEBLE)
              && !matchRegexp(tokens[i - j + 1].getToken(), PRONOMFEBLE_POSPOSAT)) {
            keepCounting=false;
          }
          j++;
        }
      }
    }
    return false;
  }
  
  /**
   * Checks if there is a desired pronoun near the verb
   */
  private boolean isTherePronoun(final AnalyzedTokenReadings[] tokens, int i,
      Pattern lemma, Pattern postag) {
    int j = 1;
    boolean keepCounting = true;
    while (i - j > 0 && keepCounting) {
      if (matchPostagRegexp(tokens[i - j], postag)
          && matchLemmaRegexp(tokens[i - j], lemma))
        return true;
      keepCounting = matchPostagRegexp(tokens[i - j], PREP_VERB_PRONOM);
      j++;
    }
    j = 1;
    keepCounting = true;
    while (i + j < tokens.length && keepCounting) {
      if (matchPostagRegexp(tokens[i + j], postag)
          && matchLemmaRegexp(tokens[i + j], lemma))
        return true;
      keepCounting = matchPostagRegexp(tokens[i + j], PREP_VERB_PRONOM);
      j++;
    }
    return false;
  }

  private boolean isThereBefore(final AnalyzedTokenReadings[] tokens,
      int i, Pattern lemma, Pattern postag) {
    int j = 1;
    boolean keepCounting = true;
    while (i - j > 0 && keepCounting) {
      if (matchPostagRegexp(tokens[i - j], postag)
          && matchLemmaRegexp(tokens[i - j], lemma))
        return true;
      keepCounting = matchPostagRegexp(tokens[i - j], PREP_VERB_PRONOM);
      j++;
    }
    return false;
  }
  
  private boolean isThereBeforePostag(final AnalyzedTokenReadings[] tokens,
      int i, Pattern postag) {
    int j = 1;
    boolean keepCounting = true;
    while (i - j > 0 && keepCounting) {
      if (matchPostagRegexp(tokens[i - j], postag))
        return true;
      keepCounting = matchPostagRegexp(tokens[i - j], PREP_VERB_PRONOM);
      j++;
    }
    return false;
  }

  private boolean isThereAfter(final AnalyzedTokenReadings[] tokens, int i, Pattern postag) {
    int j = 1;
    boolean keepCounting = true;
    while (i+j<tokens.length && keepCounting) {
      if (matchPostagRegexp(tokens[i+j], postag))
        return true;
      keepCounting = matchPostagRegexp(tokens[i+j], PREP_VERB_PRONOM_ADV) 
          || cometes.contains(tokens[i+j].getToken());
      j++;
    }
    return false;
  }
  
  private boolean isThereAfterWithoutPreposition(final AnalyzedTokenReadings[] tokens, int i, Pattern postag) {
    int j = 1;
    boolean keepCounting = true;
    while (i+j<tokens.length && keepCounting) {
      if (matchPostagRegexp(tokens[i+j], postag))
        return true;
      keepCounting = matchPostagRegexp(tokens[i+j], VERB_PRONOM)
          || cometes.contains(tokens[i+j].getToken());
      j++;
    }
    return false;
  }
  
  private boolean isThereVerbBefore(final AnalyzedTokenReadings[] tokens, int i, Pattern lemma) {
    int j = 1;
    boolean keepCounting = true;
    while (i-j>0 && keepCounting) {
      if (matchLemmaRegexp(tokens[i-j], lemma))
        return true;
      keepCounting = matchPostagRegexp(tokens[i - j],
          PREP_VERB_PRONOM);
      if ("per".equalsIgnoreCase(tokens[i-j].getToken())
          && "a".equalsIgnoreCase(tokens[i-j+1].getToken()))
        keepCounting=false;
      if (matchPostagRegexp(tokens[i-j],VERB_INDSUBJ)
          && matchPostagRegexp(tokens[i-j+1],VERB_INFGER))
        keepCounting=false;
      j++;
    }
    return false;
  }
  
  private boolean isThereVerbAfter(final AnalyzedTokenReadings[] tokens, int i, Pattern lemma) {
    int j = 1;
    boolean keepCounting = true;
    while (i+j<tokens.length && keepCounting) {
      if (matchLemmaRegexp(tokens[i+j], lemma))
        return true;
      keepCounting = matchPostagRegexp(tokens[i+j],
          PREP_VERB_PRONOM);
      j++;
    }
    return false;
  }

  private boolean isThereVerbBeforeList(final AnalyzedTokenReadings[] tokens, int i, List<String> lemmas) {
    return isThereVerbBeforeListLimit(tokens,i,lemmas,10);
  }
  
  private boolean isThereVerbBeforeListLimit(final AnalyzedTokenReadings[] tokens, int i, List<String> lemmas, int limit) {
    int j = 1;
    boolean keepCounting = true;
    while (i-j>0 && keepCounting && j<limit) {
      if (matchLemmaList(tokens[i-j], lemmas) && !tokens[i-j].hasPosTag("_possible_nompropi"))
        return true;
      keepCounting = matchPostagRegexp(tokens[i - j],
          PREP_VERB_PRONOM);
      if (tokens[i-j].getToken().equalsIgnoreCase("per")
          && tokens[i-j+1].getToken().equalsIgnoreCase("a"))
        keepCounting=false;
      if (matchPostagRegexp(tokens[i-j],VERB_INDSUBJ)
          && matchPostagRegexp(tokens[i-j+1],VERB_INFGER))
        keepCounting=false;
      j++;
    }
    return false;
  }
  
  private boolean isThereVerbAfterList(final AnalyzedTokenReadings[] tokens, int i, List<String> lemmas) {
    int j = 1;
    boolean keepCounting = true;
    while (i+j<tokens.length && keepCounting) {
      if (matchLemmaList(tokens[i+j], lemmas))
        return true;
      keepCounting = matchPostagRegexp(tokens[i+j],
          PREP_VERB_PRONOM);
      j++;
    }
    return false;
  }
  
  private boolean isThereRedundantPronoun(final AnalyzedTokenReadings[] tokens, int i) {
    if ( (isThereAfterWithoutPreposition(tokens,i,PRONOM_FEBLE_1S) && isThereBeforePostag(tokens,i,PRONOM_FEBLE_1S))
        || (isThereAfterWithoutPreposition(tokens,i,PRONOM_FEBLE_2S) && isThereBeforePostag(tokens,i,PRONOM_FEBLE_2S))
        || (isThereAfterWithoutPreposition(tokens,i,PRONOM_FEBLE_3S_TOTS) && isThereBeforePostag(tokens,i,PRONOM_FEBLE_3S_TOTS))
        || (isThereAfterWithoutPreposition(tokens,i,PRONOM_FEBLE_1P) && isThereBeforePostag(tokens,i,PRONOM_FEBLE_1P))
        || (isThereAfterWithoutPreposition(tokens,i,PRONOM_FEBLE_2P) && isThereBeforePostag(tokens,i,PRONOM_FEBLE_2P))
        || (isThereAfterWithoutPreposition(tokens,i,PRONOM_FEBLE_3P) && isThereBeforePostag(tokens,i,PRONOM_FEBLE_3P))) 
      return true;
    return false;
  }
  
  private boolean isThereNearLemma(final AnalyzedTokenReadings[] tokens, int i, List<String> lemmas) {
    int j = 1;
    boolean keepCounting = true;
    while (i+j<tokens.length && keepCounting) {
      if (matchLemmaList(tokens[i+j], lemmas))
        return true;
      keepCounting = !matchPostagRegexp(tokens[i+j], TRENCA_COMPTE);
      j++;
    }
    j = 1;
    keepCounting = true;
    while (i-j>0 && keepCounting) {
      if (matchLemmaList(tokens[i-j], lemmas))
        return true;
      keepCounting = !matchPostagRegexp(tokens[i-j], TRENCA_COMPTE);
      j++;
    }
    return false;
  }
  
  private boolean isThereNearWord(final AnalyzedTokenReadings[] tokens, int i, List<String> words) {
    int j = 1;
    boolean keepCounting = true;
    while (i+j<tokens.length && keepCounting) {
      if (words.contains(tokens[i+j].getToken().toLowerCase()))
        return true;
      keepCounting = !matchPostagRegexp(tokens[i+j], TRENCA_COMPTE);
      j++;
    }
    j = 1;
    keepCounting = true;
    while (i-j>0 && keepCounting) {
      if (words.contains(tokens[i-j].getToken().toLowerCase()))
        return true;
      keepCounting = !matchPostagRegexp(tokens[i-j], TRENCA_COMPTE);
      j++;
    }
    return false;
  }

  
  private boolean isTherePersonalSubjectBefore(final AnalyzedTokenReadings[] tokens, int i,
      Pattern pTrenca) {
    int j = 1;
    boolean keepCounting = true;
    while (i - j > 0 && keepCounting) {
      if (matchRegexp(tokens[i - j].getToken(), SUBJECTE_PERSONAL_TOKEN)
          || matchPostagRegexp(tokens[i - j], SUBJECTE_PERSONAL_POSTAG)
          && !matchPostagRegexp(tokens[i - j], SUBJECTE_PERSONAL_NO_POSTAG)
          && !matchLemmaRegexp(tokens[i-j], SUBJECTE_PERSONAL_NO_LEMMA))
        return true;
      keepCounting = !matchPostagRegexp(tokens[i - j], pTrenca);
      j++;
    }
    return false;
  }
  
  private boolean isThereSingularPersonalSubjectBefore(final AnalyzedTokenReadings[] tokens, int i,
      Pattern pTrenca) {
    int j = 1;
    boolean keepCounting = true;
    while (i - j > 0 && keepCounting) {
      if (matchRegexp(tokens[i - j].getToken(), SUBJECTE_PERSONAL_SING_TOKEN)
          || matchPostagRegexp(tokens[i - j], SUBJECTE_PERSONAL_SING_POSTAG)
          && !matchPostagRegexp(tokens[i - j], SUBJECTE_PERSONAL_NO_POSTAG)
          && !matchLemmaRegexp(tokens[i-j], SUBJECTE_PERSONAL_NO_LEMMA))
        return true;
      keepCounting = !matchPostagRegexp(tokens[i - j], pTrenca);
      if (matchLemmaRegexp(tokens[i-j], SUBJECTE_PERSONAL_NO_LEMMA)) {
        keepCounting = false;
      }
      j++;
    }
    return false;
  }
  
  private boolean isTherePluralPersonalSubjectBefore(final AnalyzedTokenReadings[] tokens, int i,
      Pattern pTrenca) {
    int j = 1;
    boolean keepCounting = true;
    while (i - j > 0 && keepCounting) {
      if (matchRegexp(tokens[i - j].getToken(), SUBJECTE_PERSONAL_PL_TOKEN)
          || matchPostagRegexp(tokens[i - j], SUBJECTE_PERSONAL_PL_POSTAG)
          && !matchPostagRegexp(tokens[i - j], SUBJECTE_PERSONAL_NO_POSTAG)
          && !matchLemmaRegexp(tokens[i-j], SUBJECTE_PERSONAL_NO_LEMMA))
        return true;
      keepCounting = !matchPostagRegexp(tokens[i - j], pTrenca);
      if (matchLemmaRegexp(tokens[i-j], SUBJECTE_PERSONAL_NO_LEMMA)) {
        keepCounting = false;
      }
      j++;
    }
    return false;
  }
  
  private boolean isThereSubject3SBefore(final AnalyzedTokenReadings[] tokens, int i,
      Pattern pTrenca) {
    int j = 1;
    boolean keepCounting = true;
    while (i - j > 0 && keepCounting) {
      if (matchRegexp(tokens[i - j].getToken(), SUBJECTE_3S_TOKEN)
          || matchPostagRegexp(tokens[i - j], SUBJECTE_3S_POSTAG)
          && !matchPostagRegexp(tokens[i - j], SUBJECTE_3S_NO_POSTAG)
          && !matchRegexp(tokens[i-j].getToken(), SUBJECTE_3S_NO_TOKEN)
          && !matchLemmaRegexp(tokens[i-j], SUBJECTE_PERSONAL_NO_LEMMA))
        return true;
      keepCounting = !matchPostagRegexp(tokens[i - j], pTrenca);
      j++;
    }
    return false;
  }
  
  private boolean isVerbNumberPerson(final AnalyzedTokenReadings[] tokens, int i, Pattern pVerb){
    int j = 0; // El verb principal pot ser conjugat
    boolean keepCounting = true;
    while (i-j>0 && keepCounting) {
      if (matchPostagRegexp(tokens[i-j], pVerb))
        return true;
      keepCounting = matchPostagRegexp(tokens[i - j],
          PREP_VERB_PRONOM);
      if (tokens[i-j].getToken().equalsIgnoreCase("per")
          && tokens[i-j+1].getToken().equalsIgnoreCase("a"))
        keepCounting=false;
      j++;
    }
    return false;
  }
  
  
  private boolean isPhraseImpersonalVerbS (final AnalyzedTokenReadings[] tokens, int i) {
    //FRASE IMPERSONAL
    // És frase impersonal si hi ha el pronom 'es', llevat que es pugui identificar un subjecte "personal".
    return isThereBefore(tokens, i, LEMMA_ES, POSTAG_ES)
    && !isThereBefore(tokens, i, LEMMA_PRONOM_CI, POSTAG_PRONOM_CI)
    && (!isThereSingularPersonalSubjectBefore(tokens,i,TRENCA_COMPTE2) || isThereBefore(tokens, i, LEMMA_HI, POSTAG_HI))
    && isVerbNumberPerson(tokens,i,VERB_3S);      
  }
  private boolean isPhraseImpersonalVerbSP (final AnalyzedTokenReadings[] tokens, int i) {
    //FRASE IMPERSONAL
    // És frase impersonal si hi ha el pronom 'es', llevat que es pugui identificar un subjecte "personal".
    return isThereBefore(tokens, i, LEMMA_ES, POSTAG_ES)    
    && !isThereBefore(tokens, i, LEMMA_PRONOM_CI, POSTAG_PRONOM_CI)
    && (  (  (isVerbNumberPerson(tokens,i,VERB_3S) && !isThereSingularPersonalSubjectBefore(tokens,i,TRENCA_COMPTE))
          || (isVerbNumberPerson(tokens,i,VERB_3P) && !isTherePluralPersonalSubjectBefore(tokens,i,TRENCA_COMPTE)) )
       || isThereBefore(tokens, i, LEMMA_HI, POSTAG_HI));      
  }
  
}
