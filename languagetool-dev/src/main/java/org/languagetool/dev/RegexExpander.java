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

  private final static String regex = "(?:c(?:a(?:m(?:i(?:nhos?|sas)|a(?:rada|s)?|elos|po)|r(?:t(?:ilha|as?)|ác?ter|amujo|ros?|ne)|s(?:t(?:el(?:hanos|o)|igo)|as?|os?|ca)|p(?:it(?:a(?:is|l)|ães)|richos?|as)|b(?:reiros|otino|eça)|l(?:deiradas|çado|or)|ch(?:orrinho|aça)|(?:ntinh|gaç)o|u(?:dal|sas)|valos?|tarse|ça|os)|o(?:n(?:t(?:r(?:abandistas|ole)|estar)|f(?:iança|orto)|se(?:lhos|nso))|m(?:p(?:romisso|anhia)|bate|ida|um|o)?|(?:b(?:r(?:anç)?|ertur)|us|v)a|i(?:sa(?:-ruim)?|ros)|adjutor)|h(?:a(?:ma(?:riz)?|nce|ves)|e(?:(?:iro|que)s|fes?)|oque|uva|á)|r(?:i(?:a(?:(?:nça|do)s|tura)|m(?:inoso|es?)|se)|ase)|e(?:r(?:t(?:ezas?|as)|imônia)|go(?:nha)?|ntro)|l(?:a(?:ro-escuro|sse)|i(?:entel|m)a|ube)|i(?:n(?:e(?:asta|mas?)|co)|úmes|pó)|u(?:l(?:pado|tura)|ra)|á(?:lculo)?|éu)|a(?:l(?:g(?:u(?:ma?|ns)|as)|(?:caid|arm|fac)e|t(?:ernativas|a)|m(?:oço|a)|deias|vará|i)|m(?:(?:bulante|igo|pla)s|a(?:nuense|rgura)|or)|t(?:a(?:ques?|lhos)|mosfera|rasos?|eus|é)|p(?:e(?:tite|nas)|o(?:ios?|sta)|lausos)|r(?:istocracias|t(?:ista|e)|voredo|ma)|s(?:s(?:i(?:stentes|m)|adura)|pas)?|(?:g(?:ricultur|end)|ind)a|c(?:(?:úmul|ord)o|ademias)|(?:n(?:imai|o)|forismo)s|u(?:to(?:móvel|r)|las)|qu(?:el(?:as?|es?)|i)|b(?:ertura|rigo)|ju(?:ste|da)|z(?:uis|ar)|(?:vanç)?o|dmirar|í)|d(?:e(?:s(?:e(?:(?:mpreg|rt)o|jar)|(?:graçad|leix|tin)o|c(?:ulpas?|anso)|(?:vio|sa)s|pedida|ordem|ar)|(?:u(?:se)?|talhe)s|n(?:úncias|tro)|certo|ver|z)?|i(?:v(?:i(?:dendos|nas?)|ergências)|f(?:iculdades|erenças?)|s(?:[st]o|farce)|a(?:(?:bo)?s)?|(?:plom|et)a|rec?tor|álogo)|o(?:u(?:tr(?:ina|os)|s)|r(?:mi(?:mos|r))?|(?:ming|l)o|enças?|çura|ce|is)?|u(?:(?:que|a)s|vidar)|(?:r(?:am|og)|ívid)a|a(?:dos|ta)|úvidas?)|e(?:s(?:t(?:(?:(?:údi|ud)o|ratégia)s|a(?:tísticas)?|ômago|es?)|c(?:ol(?:as|ha)|ândalo|ravos|apar)|p(?:erança|écies|aços?)|s(?:es?|a)|forços)|m(?:p(?:a(?:t(?:es|ia)|das)|(?:lastr|reg)o|enhos?)|bargo)?|n(?:c(?:arregados|ontro)|fermidades|ganos?|ergia|tre)|(?:x(?:ager|empl)|goísm)o|(?:vidência|lefante)s|pi(?:demias?|sódio)|r(?:ros?|mos|va)|conomia|quipes?)?|p(?:r(?:o(?:gr(?:esso|ama)|blemas?|dígio|tetor|vas?)|a(?:z(?:er|o)|xe)|incípios?|essas?)|e(?:r(?:(?:plexidade|spec?tiva|íodo)s|igo|da)|s(?:soa[ls]?|ar)|nedos|ixe)|o(?:r(?:tugueses|que|ém)?|lí(?:ticas?|cias)|(?:esi|t)a|der|sse|is|vo)|a(?:(?:ciênci|lavr)a|r(?:tidos?|a)|ssagem|pel|ís|i)|i(?:o(?:lhos|r)|stas)|úblico|lanos)|b(?:a(?:n(?:(?:h(?:eir)?o|quete|ana)s|[cd]o)|l(?:a(?:nça|s)|iza)|r(?:racas|co)|i(?:le|a)|gatelas|se)|o(?:n(?:ança|de|zo|s)|t(?:equim|a)|m(?:bas)?|(?:rd)?a|gas)|e(?:l(?:(?:ez)?a|os)|rnarda|ijo|m)|r(?:aços?|uxas)|i(?:cho|rra)|álsamo|uracos)|m(?:e(?:n(?:digo|ina)|d(?:ida|o)|s(?:as|mo)|ios?|lhor)|o(?:t(?:orista|ivos)|l(?:éstia|hos)|delos?|rte|ça)|a(?:l(?:andro|es)?|(?:nhã)?s|i(?:or|s)|rgem)|(?:á(?:scar|go)|úsic)a|u(?:danças|itas?|lher)|é(?:dicos?|todos?)|i(?:lagre|ster)|ãos)|r(?:e(?:c(?:u(?:rsos?|os)|ei(?:tas|o))|f(?:(?:erência|ugiado)s|orma)|g(?:ist(?:ros?|o)|ra)|s(?:postas?|ervas)|m(?:édi|ors)o|i)|a(?:ci(?:ocínios|smo)|ças|paz)|i(?:queza|scos?)|osas|ua)|s(?:e(?:n(?:ti(?:do|r)|ador)|g(?:redos?|unda)|r(?:viço|eia)?|mpre|quer|is)|u(?:b(?:tileza|sídio)s|rf)|o(?:(?:cia)?l|bre|no|m)|i(?:na(?:is|l)|mpatia)|a(?:ngue|ída|las)|ó)|t(?:e(?:(?:r(?:apêutic|r)|cnologi)a|m(?:or|po)|stemunhas)|r(?:a(?:balho|gédia)|(?:eva|ê)s|istezas?|áfico|opa)|a(?:(?:ref|nt)a|l(?:vez)?|mbém)|(?:o[dl]|ip)o)|n(?:e(?:nhu(?:ma?|ns)|s(?:t[ae]|sa)|g(?:ros|ar)|l(?:es?|a)|m)|o(?:(?:v(?:idade|o)|t(?:íci)?a)?s|mes?|ite)?|a(?:(?:scente)?s|da)?|i(?:nguém|sso)|úmeros)|f(?:o(?:r(?:ças?|mas?|tuna)|me)|a(?:l(?:ar|ta)|mília|to)|i(?:dalgos|lósofo|m)|u(?:(?:tur|m)o|gir)|e(?:stas?|bre)|é(?:rias)?|órmulas?)|v(?:e(?:r(?:(?:dadeir|s)os)?|stígio|detas)|i(?:n(?:gança|ho)|slumbrar|zinhos|da)|a(?:ci(?:lar|na)|idades|gas?|lor)|o(?:ltas?|z)|éu)|i(?:(?:m(?:possívei|agen)|deologia)s|n(?:teresse|dícios|imigo)|gual)|g(?:r(?:a(?:ndes?|des|ça)|upos)|o(?:nçalinh|vern|st)o|arantias?)|l(?:i(?:nguagem|mites?|vros?|ames|xo)|u(?:[az]|tas?|gar)|eis?|á)|o(?:u(?:tr(?:os?|a)|ro)?|(?:bstácul|lh)os|fensa|rdem|nde)?|h(?:o(?:me(?:ns|m)|nra|je)|ipótese)|qu(?:a(?:(?:is|l)quer|se)|em?)|j(?:o(?:rnai|go)s|ustiça|á)|á(?:r(?:vore|ea)|lbum|gua)|u(?:m(?:as?)?|topias)|é(?:pocas?)?|ânimo)";
  private final static String wordListFile = "/home/dnaber/lt/pt-words.txt";
  private final static Set<String> printed = new HashSet<>();

  public static void main(String[] args) throws IOException {
    Pattern p = Pattern.compile(regex);
    //System.out.println(p.matcher("Sáb").matches());
    //System.exit(0);
    List<String> lines = Files.readAllLines(Paths.get(wordListFile));
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
