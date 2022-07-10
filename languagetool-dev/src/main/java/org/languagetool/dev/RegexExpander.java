/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.languagetool.tools.StringTools.uppercaseFirstChar;

/**
 * "Expand" regular expressions like {@code [Ss](?:e[gx]|áb)} to make them readable.
 * To use this, you need a list of "all" words, e.g. exported (using 'unmunch') from the spell
 * checker's *.dic files. Then copy the regexp in question to `String regex = ...` here,
 * set the file to the list of words at `String wordListFile = ...` and run the main
 * method to print the expanded regex. Note that words that are not in your list of
 * words but that the regex would match will be missing from the new regex.
 */
public class RegexExpander {

  private final static List<String> regexesOrEntities = Arrays.asList(
      "   <!ENTITY barbarismos \"(?:s(?:t(?:r(?:ip(?:tease[rs]?|per|s)?|ess.*|ogonoff|aight)|a(?:nd(?:ard|s)?|ccato|rtup|ff)|o(?:ryboard|kes|ck|p)|i(?:ck|lb)|e(?:nt|p))|p(?:r(?:int(?:er|s)?|ead|ay|ue)|in(?:naker|s)?|a[ms]?|ot)|u(?:b(?:-holding|woofer)|s(?:pense|hi)|perstar|doku|rf)|h(?:ar(?:eware|-pei)|o(?:[tw]|pping)|iatsu|ekel|unt)|a(?:(?:xhor|loo)n|mple[rs]?|shimi|vart)|c(?:herz(?:and)?o|rewball|o[np]e|at)|n(?:o(?:wboard|oker|b)|ack(?:-bar)?)|o(?:(?:cialit|ttovoc|ftwar)e|ul)|m(?:o(?:(?:kin)?g|rzando)|ash)|l(?:i(?:ck|de|p)|alom|ogan)|e(?:t(?:-point|ter)?|xy)|w(?:eat(?:shirt|er)|ing)|i(?:decar|evert|ngle|c)|fo(?:rzand|gat)o|k(?:ipper|ate)|quash)|c(?:a(?:n(?:ta(?:bile|ta)|yoning|iche)|r(?:diofitness|paccio|ré)|(?:che-sex|lzon)e|m(?:p(?:us)?|ber)|t(?:ering|gut)|sh-and-carry)|o(?:r(?:don-bleu|pus)|u(?:lomb|ntry)|(?:spla|wbo)y|(?:ck|v)er|loratura|ntinuum|okie)|h(?:a(?:r(?:leston|treuse)|ise-longue)|e(?:ong-sam|ck-in|ddar)|utney)|l(?:ear(?:ance|s)?|u(?:ster|b)|ipboard|oisonné|arke)|r(?:o(?:issan|que)t|evasse)|zar(?:s?ti|ina)|(?:élad|ân)on|u(?:mbia|p)|ódex|ent)|p(?:a(?:nach(?:és?|es?)|t(?:chouli|hos)|intball|parazzi|hoehoe|lmier|anga|rsec|ssim)|i(?:n(?:s(?:cher)?|ce-nez|g-pong|-?up)?|(?:anofort|ckl)e|ercing|dgin|lé)|o(?:st(?:er(?:iori|s)|-it)|(?:odl|ch|is)e|ltergeist|t-?pourri|grom|p)|e(?:(?:tit-suiss|nc)e|restroika|corino)|r(?:i(?:ori|se)|omenade|essing|áxis)|lay(?:b(?:ack|oy)|station|maker)|hot(?:o(?:-finish|maton)|s)?|u(?:tter|zzle|nk|b))|t(?:a(?:(?:sk-forc|wni)e|l(?:k-show|iban)|ke(?:away|s)?|n(?:dem|k)|volatura|ekwondo|i-chi|blet)|u(?:t(?:(?:ilimúnd|t)i|u)|rbo-diesel|pperware|grik)|r(?:i(?:p(?:-hop|lex)|al)|a(?:velling|iler)|emolo)|i(?:me(?:-sharing|share)|e-break|ramisu|cket)|e(?:le(?:marketing|x)|(?:rylen)?e|chno|flon)|o(?:p(?:(?:les)?s)?|ner|fu|ri)|h(?:esaur(?:us|i)|ink)|(?:w(?:ee|is)|-shir)t|sunami|ópos)|b(?:a(?:by-(?:sitter|doll|grow)|(?:varois|din)e|ckup|ht|rn)|r(?:e(?:akdance|nt)|u(?:shing|nch)|ainstorming|ie)|e(?:ta-tester|nedictus|cquerel|agle|bop)|o(?:bsleigh|dyboard|nsai|xers|ate|rt)|i(?:t(?:map|s)?|odesign|g-bang|p)|u(?:ngee-jumping)|l(?:ister|ague|ues|og)|yte)|k(?:i(?:l(?:o(?:(?:vol|wat)t|b(?:yte|it))|im|t)|t(?:chenette|s(?:ch)?)?|ckbox(?:ing|er)|n[ag]|butz|p)|a(?:r(?:a(?:oke|té)|bovanet|ting)|lash(?:nikov|s)?|mikaze|sba)|r(?:(?:ípto|emli)n|aft|ill)|wa(?:(?:ch|nz)a|shiorkor)|e(?:f(?:fieh|ir)|tchup)|un(?:g-fu|a)|yat)|r(?:o(?:c(?:k(?:er|s)?|aille)|(?:entge|ll-o)n|ttweiler|quefort|aming|okie|deo)|e(?:a(?:lpolitik|dy-made)|p(?:rise|s)|dneck|ggae|m)|i(?:n(?:forzando|ggit|k)|ckettsia|tardando|ff)|a(?:l(?:lentando|enti)|p(?:per|s)?|fting|ve)|é(?:veillon|gie|tro)|öntgen|ubato)|m(?:a(?:t(?:ch-point|rioska)|r(?:keting|chand)|(?:estos|mb)o|(?:st?e|yo)r|na(?:ger|t)|gnificat|jorette|xwell|quis)|i(?:l(?:curie|ady)|s(?:erere|ter)|ndfulness|crofarad)|o(?:d(?:e(?:rato|m)|us)|hair)|e(?:morandum|dley)|u(?:sic-hall|esli)|vdol)|f(?:o(?:[bg]|x(?:-terrier|trot)?|r(?:tran|int)|ndue|yer|lk)|a(?:i(?:t-divers|r-play)|rad(?:ay|s)?|nfreluche|twa|x)|l(?:a(?:sh(?:es)?|menco|t)|i(?:p-flop|nt)|ute)|u(?:n(?:board|ky?)|gato|ton)|r(?:anchising|eeware)|ermata|itness|öhn)|a(?:n(?:ti(?:-(?:establishment|apartheid|dumping)|trust)|gström)|p(?:felstrudel|paratchik|artheid|lomb)|l(?:legr(?:ett)?o|zheimer|ibi)|c(?:celerandos?|id-jazz)|uto(?:pullman|cross)|git(?:-?prop|ato)|yurveda|mabile|irbus)|d(?:o(?:p(?:ing|pler)|wnhill|car|jo|ng)|r(?:ive(?:[rs]|-in)?|ugstore)|e(?:sign(?:er)?|ficit|bye)|i(?:s(?:cman|eur)|rham)|u(?:mping|plex|ce)|a(?:nzón|tcha))|g(?:i(?:ga(?:byte|watt)|rlsband|lbert|nseng)|o(?:(?:odwil|spe)l|belet|uda)|l(?:a(?:snost|mour)|ide)|u(?:aracha|lag)|ru(?:yèr|ng)e|a[ly]|eyser)|h(?:a(?:b(?:it(?:at|us)|anera)|(?:m-ioc-chon|shta)g|rd-rock)|i(?:p(?:p(?:ie|y)|-hop)|drospeed)|o(?:rseball|lding|mo)|eavy-metal|usky|ype)|v(?:i(?:de(?:ocl(?:ip(?:es)?|ub)|s)?|(?:t(?:rin|a)|ntag|vac)e|brato)|e(?:r(?:nissage|sus)|lcro|gan)|o(?:lt(?:e-face|s)?|yeur)|audeville)|l(?:e(?:[dk]|a(?:sing|d)|itmotiv|gato)|o(?:c(?:kout|us)|oping|ess|gin)|a(?:rghetto|ser|mé|ts)|i(?:ngerie|fting))|o(?:ff(?:s(?:hore|et)|ice-boy|line)|s(?:tpolitik|sobuco)|u(?:tsider|guiya)|verbooking|n-?line|ersted|rigami|pus)|j(?:a(?:m(?:-session|boree)|c(?:kpot|uzzi)|zz)|o(?:int-ventur|ul)e|u(?:kebox|nkie)|et-(?:lag|set)|iu-jitsu)|w(?:a(?:(?:lkie-talki|ffl)e|rrant|sp|d)|e(?:b(?:er|s)?|stern)|i(?:ld-card|ndsurf)|o(?:rkshop|n)|hist)|i(?:n(?:ter(?:f(?:eron|ace)|net)|f(?:otainment|luenza)|-octavo|s)|(?:bid|t)em|mpedimenta|ppon|d)|n(?:e(?:(?:cessair|w-ag)e|(?:tspli)?t)|o(?:menklatura|ir)|apalm|uance|ylon)|e(?:n(?:s(?:alada|emble)|tente)|r(?:satz|g)|mmenthal|cstasy|vasé|dam)|qu(?:a(?:lifying|ntum|rk)|i(?:lohertz|che))|z(?:e(?:itgeist|kel|n)|apping)|y(?:u(?:ppie|an)|ang|eti|in)|u(?:ndergroun|ploa)d)\">\n",
      "   <!ENTITY barbarismos2 \"b(?:irdwatching|lockchains?|odyboarders?)|backdoors?|bots?|c(?:hipset|rowdfunding)s?|desktops?|DNA|dominatrix(?:es)?|draft|geocach(?:ing|ers?)|h(?:atchback|ijab|otspot|overboard)s?|icebergs?|jetpacks?|k(?:ernels?|evlar)|m(?:alware|illennial)s?|n(?:etworking|otch|uggets?)|overclock(?:ings?)|p(?:arkour|hishing|odcast|unchline)s?|RNA|s(?:martwatch(?:es)|ext(?:ing|ortion)|tormtroppers?|treaming)|trackpads?|w(?:ebsite|halewatching|oks?)\">\n");
  private final static String wordListFile = "/home/dnaber/lt/pt-words.txt";
  private final static Set<String> printed = new HashSet<>();

  public static void main(String[] args) throws IOException {
    //Pattern tempP = Pattern.compile("(?:e(?:stere|ur|g)|(?:cent|sac)r|a(?:str|udi)|t(?:erm|urb)|f(?:il|ot)|i(?:ntr|d)|bronc|labi|mon|vas|zo)o");
    //System.out.println(tempP.matcher("estereo").matches());
    //System.exit(0);
    List<String> lines = Files.readAllLines(Paths.get(wordListFile));
    for (String s : regexesOrEntities) {
      if (s.contains("<!ENTITY")) {
        s = s.replaceFirst("<!ENTITY .*? \"(.*)\">", "$1").trim();
      }
      System.out.println(s);
      System.out.println("=>");
      Pattern p = Pattern.compile(s);
      int i = 0;
      for (String line : lines) {
        line = line.trim();
        boolean lcMatch = false;
        boolean ucMatch = false;
        if (p.matcher(line).matches()) {
          lcMatch = true;
        }
        if (StringTools.startsWithLowercase(line) && p.matcher(uppercaseFirstChar(line)).matches()) {
          ucMatch = true;
        }
        if (lcMatch && ucMatch) {
          printToken(i, "[" + uppercaseFirstChar(line).charAt(0) + StringTools.lowercaseFirstChar(line).charAt(0) + "]" + line.substring(1));
          i++;
        } else if (lcMatch && !printed.contains(line)) {
          printToken(i, line);
          i++;
        } else if (ucMatch && !printed.contains(uppercaseFirstChar(line))) {
          printToken(i, uppercaseFirstChar(line));
          i++;
        }
      }
      System.out.println("\n");
    }
  }

  private static void printToken(int i, String s) {
    if (i == 0) {
      System.out.print(s);
    } else {
      System.out.print("|" + s);
    }
    printed.add(s);
  }

}
