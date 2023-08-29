/* LanguageTool, a natural language style checker
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.de;

import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.spelling.CachingWordListLoader;
import org.languagetool.synthesis.GermanSynthesizer;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.CombiningTagger;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.languagetool.tools.StringTools.uppercaseFirstChar;

/**
 * German part-of-speech tagger, requires data file in <code>de/german.dict</code> in the classpath.
 * The POS tagset is described in
 * <a href="https://github.com/languagetool-org/languagetool/blob/master/languagetool-language-modules/de/src/main/resources/org/languagetool/resource/de/tagset.txt">tagset.txt</a>
 *
 * @author Marcin Milkowski, Daniel Naber
 */
public class GermanTagger extends BaseTagger {

  private static final List<String> allAdjGruTags = new ArrayList<>();
  static {
    for (String nomAkkGenDat : Arrays.asList("NOM", "AKK", "GEN", "DAT")) {
      for (String pluSin : Arrays.asList("PLU", "SIN")) {
        for (String masFemNeu : Arrays.asList("MAS", "FEM", "NEU")) {
          for (String defIndSol : Arrays.asList("DEF", "IND", "SOL")) {
            allAdjGruTags.add("ADJ:" + nomAkkGenDat + ":" + pluSin + ":" + masFemNeu + ":GRU:" + defIndSol);
          }
        }
      }
    }
  }

  // do not add noun tags to these words, e.g. don't add noun tags to "Wegstrecken" for weg_strecken from spelling.txt:
  private static final List<String> nounTagExpansionExceptions = Arrays.asList("Wegstrecken");

  // ordered by length: 'zurück' > 'zu' + 'rück'
  private static final String[] prefixesSeparableVerbs = new String[] {"gegeneinander", "durcheinander", "nebeneinander", "übereinander", "aufeinander", "auseinander", "beieinander", "aneinander", "ineinander", "zueinander", "gegenüber", "beisammen", "gegenüber", "hernieder", "rückwärts", "wiederauf", "wiederein", "wiederher", "zufrieden", "zwangsvor", "entgegen", "hinunter", "abhanden", "aufrecht", "aufwärts", "auswärts", "beiseite", "danieder", "drauflos", "einwärts", "herunter", "hindurch", "verrückt", "vorwärts", "zunichte", "zusammen", "zwangsum", "zwischen", "abseits", "abwärts", "entlang", "hinfort", "ähnlich", "daneben", "general", "herüber", "hierher", "hierhin", "hinüber", "schwarz", "trocken", "überein", "vorlieb", "vorüber", "wichtig", "zurecht", "zuwider", "hinweg", "allein", "besser", "daheim", "doppel", "feinst", "fertig", "herauf", "heraus", "herbei", "hinauf", "hinaus", "hinein", "kaputt", "kennen", "kürzer", "mittag", "nieder", "runter", "sicher", "sitzen", "voraus", "vorbei", "vorweg", "weiter", "wieder", "zugute", "zurück", "zwangs", "abend", "blank", "brust", "dahin", "davon", "drauf", "drein", "durch", "einig", "empor", "grund", "herum", "höher", "klein", "knapp", "krank", "krumm", "kugel", "näher", "neben", "offen", "preis", "rüber", "ruhig", "statt", "still", "übrig", "umher", "unter", "voran", "zweck", "acht", "drei", "fehl", "feil", "fort", "frei", "groß", "hand", "hart", "heim", "hier", "hoch", "klar", "lahm", "miss", "nach", "nahe", "quer", "rauf", "raus", "rein", "rück", "satt", "stoß", "teil", "über", "voll", "wach", "wahr", "warm", "wert", "wohl", "auf", "aus", "bei", "ehe", "ein", "eis", "end", "her", "hin", "los", "maß", "mit", "out", "ran", "rum", "tot", "vor", "weg", "weh", "ab", "an", "da", "um", "zu"};
  private static final String prefixesSeparableVerbsRegexp = "^(gegeneinander|durcheinander|nebeneinander|übereinander|aufeinander|auseinander|beieinander|aneinander|ineinander|zueinander|gegenüber|beisammen|gegenüber|hernieder|rückwärts|wiederauf|wiederein|wiederher|zufrieden|zwangsvor|entgegen|hinunter|abhanden|aufrecht|aufwärts|auswärts|beiseite|danieder|drauflos|einwärts|herunter|hindurch|verrückt|vorwärts|zunichte|zusammen|zwangsum|zwischen|abseits|abwärts|entlang|hinfort|ähnlich|daneben|general|herüber|hierher|hierhin|hinüber|schwarz|trocken|überein|vorlieb|vorüber|wichtig|zurecht|zuwider|hinweg|allein|besser|daheim|doppel|feinst|fertig|herauf|heraus|herbei|hinauf|hinaus|hinein|kaputt|kennen|kürzer|mittag|nieder|runter|sicher|sitzen|voraus|vorbei|vorweg|weiter|wieder|zugute|zurück|zwangs|abend|blank|brust|dahin|davon|drauf|drein|durch|einig|empor|grund|herum|höher|klein|knapp|krank|krumm|kugel|näher|neben|offen|preis|rüber|ruhig|statt|still|übrig|umher|unter|voran|zweck|acht|drei|fehl|feil|fort|frei|groß|hand|hart|heim|hier|hoch|klar|lahm|miss|nach|nahe|quer|rauf|raus|rein|rück|satt|stoß|teil|über|voll|wach|wahr|warm|wert|wohl|auf|aus|bei|ehe|ein|eis|end|her|hin|los|maß|mit|not|out|ran|rum|tot|vor|weg|weh|ab|an|da|um|zu)";
  private static final String[] prefixesNonSeparableVerbs = new String[]{"be", "emp", "ent", "er", "hinter", "miss", "un", "ver", "zer"}; //Excludes "ge" (both too rare as verb prefix and prone to FP)
  private static final String prefixesNonSeparableVerbsRegexp = "^(be|emp|ent|er|hinter|miss|un|ver|zer)";
  private static final String[] prefixesVerbs = new String[] {"gegeneinander", "durcheinander", "nebeneinander", "übereinander", "aufeinander", "auseinander", "beieinander", "aneinander", "ineinander", "zueinander", "gegenüber", "beisammen", "gegenüber", "hernieder", "rückwärts", "wiederauf", "wiederein", "wiederher", "zufrieden", "zwangsvor", "entgegen", "hinunter", "abhanden", "aufrecht", "aufwärts", "auswärts", "beiseite", "danieder", "drauflos", "einwärts", "herunter", "hindurch", "verrückt", "vorwärts", "zunichte", "zusammen", "zwangsum", "zwischen", "abseits", "abwärts", "entlang", "hinfort", "ähnlich", "daneben", "general", "herüber", "hierher", "hierhin", "hinüber", "schwarz", "trocken", "überein", "vorlieb", "vorüber", "wichtig", "zurecht", "zuwider", "hinweg", "hinter", "allein", "besser", "daheim", "doppel", "feinst", "fertig", "herauf", "heraus", "herbei", "hinauf", "hinaus", "hinein", "kaputt", "kennen", "kürzer", "mittag", "nieder", "runter", "sicher", "sitzen", "voraus", "vorbei", "vorweg", "weiter", "wieder", "zugute", "zurück", "zwangs", "abend", "blank", "brust", "dahin", "davon", "drauf", "drein", "durch", "einig", "empor", "grund", "herum", "höher", "klein", "knapp", "krank", "krumm", "kugel", "näher", "neben", "offen", "preis", "rüber", "ruhig", "statt", "still", "übrig", "umher", "unter", "voran", "zweck", "miss", "acht", "drei", "fehl", "feil", "fort", "frei", "groß", "hand", "hart", "heim", "hier", "hoch", "klar", "lahm", "miss", "nach", "nahe", "quer", "rauf", "raus", "rein", "rück", "satt", "stoß", "teil", "über", "voll", "wach", "wahr", "warm", "wert", "wohl", "emp", "ent", "ver", "zer", "auf", "aus", "bei", "ehe", "ein", "eis", "end", "her", "hin", "los", "maß", "mit", "out", "ran", "rum", "tot", "vor", "weg", "weh", "be", "er", "un", "ab", "an", "da", "um", "zu"};
  private static final String prefixesVerbsRegexp = "^(gegeneinander|durcheinander|nebeneinander|übereinander|aufeinander|auseinander|beieinander|aneinander|ineinander|zueinander|gegenüber|beisammen|gegenüber|hernieder|rückwärts|wiederauf|wiederein|wiederher|zufrieden|zwangsvor|entgegen|hinunter|abhanden|aufrecht|aufwärts|auswärts|beiseite|danieder|drauflos|einwärts|herunter|hindurch|verrückt|vorwärts|zunichte|zusammen|zwangsum|zwischen|abseits|abwärts|entlang|hinfort|ähnlich|daneben|general|herüber|hierher|hierhin|hinüber|schwarz|trocken|überein|vorlieb|vorüber|wichtig|zurecht|zuwider|hinweg|hinter|allein|besser|daheim|doppel|feinst|fertig|herauf|heraus|herbei|hinauf|hinaus|hinein|kaputt|kennen|kürzer|mittag|nieder|runter|sicher|sitzen|voraus|vorbei|vorweg|weiter|wieder|zugute|zurück|zwangs|abend|blank|brust|dahin|davon|drauf|drein|durch|einig|empor|grund|herum|höher|klein|knapp|krank|krumm|kugel|näher|neben|offen|preis|rüber|ruhig|statt|still|übrig|umher|unter|voran|zweck|miss|acht|drei|fehl|feil|fort|frei|groß|hand|hart|heim|hier|hoch|klar|lahm|miss|nach|nahe|quer|rauf|raus|rein|rück|satt|stoß|teil|über|voll|wach|wahr|warm|wert|wohl|emp|ent|ver|zer|auf|aus|bei|ehe|ein|eis|end|her|hin|los|maß|mit|not|out|ran|rum|tot|vor|weg|weh|be|er|un|ab|an|da|um|zu)";
  private static final String[] partizip2contains1PluPra = new String[]{"blasen", "fahren", "fallen", "fangen", "fressen", "geben", "halten", "kommen", "laden", "lassen", "laufen", "lesen", "messen", "raten",  "schlafen", "schlagen", "sehen", "tragen", "treten"};
  private static final String[] partizip2contains1PluPrt = new String[]{"bieten", "bleiben", "fliegen", "fließen", "heben", "leiden", "meiden", "scheiden", "schließen", "schreiben", "stehen", "steigen", "streiten", "treiben", "weisen", "ziehen"};
  private static final String[] postagsPartizipEndingE = new String[]{"AKK:PLU:FEM:GRU:SOL:VER", "AKK:PLU:MAS:GRU:SOL:VER", "AKK:PLU:NEU:GRU:SOL:VER", "AKK:SIN:FEM:GRU:DEF:VER", "AKK:SIN:FEM:GRU:IND:VER", "AKK:SIN:FEM:GRU:SOL:VER", "AKK:SIN:NEU:GRU:DEF:VER", "NOM:PLU:FEM:GRU:SOL:VER", "NOM:PLU:MAS:GRU:SOL:VER", "NOM:PLU:NEU:GRU:SOL:VER", "NOM:SIN:FEM:GRU:DEF:VER", "NOM:SIN:FEM:GRU:IND:VER", "NOM:SIN:FEM:GRU:SOL:VER", "NOM:SIN:MAS:GRU:DEF:VER", "NOM:SIN:NEU:GRU:DEF:VER"};
  private static final String[] postagsPartizipEndingEm = new String[]{"DAT:SIN:MAS:GRU:SOL:VER", "DAT:SIN:NEU:GRU:SOL:VER"};
  private static final String[] postagsPartizipEndingEn = new String[]{"AKK:PLU:FEM:GRU:DEF:VER", "AKK:PLU:FEM:GRU:IND:VER", "AKK:PLU:MAS:GRU:DEF:VER", "AKK:PLU:MAS:GRU:IND:VER", "AKK:PLU:NEU:GRU:DEF:VER", "AKK:PLU:NEU:GRU:IND:VER", "AKK:SIN:MAS:GRU:DEF:VER", "AKK:SIN:MAS:GRU:IND:VER", "AKK:SIN:MAS:GRU:SOL:VER", "DAT:PLU:FEM:GRU:DEF:VER", "DAT:PLU:FEM:GRU:IND:VER", "DAT:PLU:FEM:GRU:SOL:VER", "DAT:PLU:MAS:GRU:DEF:VER", "DAT:PLU:MAS:GRU:IND:VER", "DAT:PLU:MAS:GRU:SOL:VER", "DAT:PLU:NEU:GRU:DEF:VER", "DAT:PLU:NEU:GRU:IND:VER", "DAT:PLU:NEU:GRU:SOL:VER", "DAT:SIN:FEM:GRU:DEF:VER", "DAT:SIN:FEM:GRU:IND:VER", "DAT:SIN:MAS:GRU:DEF:VER", "DAT:SIN:MAS:GRU:IND:VER", "DAT:SIN:NEU:GRU:DEF:VER", "DAT:SIN:NEU:GRU:IND:VER", "GEN:PLU:FEM:GRU:DEF:VER", "GEN:PLU:FEM:GRU:IND:VER", "GEN:PLU:MAS:GRU:DEF:VER", "GEN:PLU:MAS:GRU:IND:VER", "GEN:PLU:NEU:GRU:DEF:VER", "GEN:PLU:NEU:GRU:IND:VER", "GEN:SIN:FEM:GRU:DEF:VER", "GEN:SIN:FEM:GRU:IND:VER", "GEN:SIN:MAS:GRU:DEF:VER", "GEN:SIN:MAS:GRU:IND:VER", "GEN:SIN:MAS:GRU:SOL:VER", "GEN:SIN:NEU:GRU:DEF:VER", "GEN:SIN:NEU:GRU:IND:VER", "GEN:SIN:NEU:GRU:SOL:VER", "NOM:PLU:FEM:GRU:DEF:VER", "NOM:PLU:FEM:GRU:IND:VER", "NOM:PLU:MAS:GRU:DEF:VER", "NOM:PLU:MAS:GRU:IND:VER", "NOM:PLU:NEU:GRU:DEF:VER", "NOM:PLU:NEU:GRU:IND:VER"};
  private static final String[] postagsPartizipEndingEr = new String[]{"DAT:SIN:FEM:GRU:SOL:VER", "GEN:PLU:FEM:GRU:SOL:VER", "GEN:PLU:MAS:GRU:SOL:VER", "GEN:PLU:NEU:GRU:SOL:VER", "GEN:SIN:FEM:GRU:SOL:VER", "NOM:SIN:MAS:GRU:IND:VER", "NOM:SIN:MAS:GRU:SOL:VER", "DAT:SIN:FEM:GRU:SOL:VER", "GEN:PLU:FEM:GRU:SOL:VER", "GEN:PLU:MAS:GRU:SOL:VER", "GEN:PLU:NEU:GRU:SOL:VER", "GEN:SIN:FEM:GRU:SOL:VER", "NOM:SIN:MAS:GRU:IND:VER", "NOM:SIN:MAS:GRU:SOL:VER"};
  private static final String[] postagsPartizipEndingEs = new String[]{"AKK:SIN:NEU:GRU:IND:VER", "AKK:SIN:NEU:GRU:SOL:VER", "NOM:SIN:NEU:GRU:IND:VER", "NOM:SIN:NEU:GRU:SOL:VER"};
  private static final String[] notAVerb = new String[]{"angebot", "anteil", "aufenthalt", "ausdruck", "auswärtsspiel", "beispiel", "bereich", "besondere", "daring", "einfach", "einfachst", "endkasten", "freibetrag", "grautöne", "grüntöne", "großherzöge", "großteil", "hochhaus", "klarerweise", "maßnahme", "mitglieder", "nachricht", "nebenfach", "niederlage", "nothing", "notscheid", "preisver", "reinweiß", "schwarzweiß", "schwarzgrau", "schwarzgrün", "schwarztöne", "unbesiegt", "unmenge", "unrat", "unver", "verrückterweise", "versonnen", "vorlieb", "vorteil", "warmweiß", "wohldefiniert", "wohlergehen", "wohlgemerkt", "zuende", "zuhause", "zumal", "zuver", "darauf", "einmal", "kleinkram", "hochsicher", "ehering", "freitag", "großmeister", "handwerk", "herpes", "nachfolger"};

  private static final List<String> tagsForWeise = new ArrayList<>();
  static {
    // "kofferweise", "idealerweise" etc.
    tagsForWeise.add("ADJ:AKK:PLU:FEM:GRU:SOL");
    tagsForWeise.add("ADJ:AKK:PLU:MAS:GRU:SOL");
    tagsForWeise.add("ADJ:AKK:PLU:NEU:GRU:SOL");
    tagsForWeise.add("ADJ:AKK:SIN:FEM:GRU:DEF");
    tagsForWeise.add("ADJ:AKK:SIN:FEM:GRU:IND");
    tagsForWeise.add("ADJ:AKK:SIN:FEM:GRU:SOL");
    tagsForWeise.add("ADJ:AKK:SIN:NEU:GRU:DEF");
    tagsForWeise.add("ADJ:NOM:PLU:FEM:GRU:SOL");
    tagsForWeise.add("ADJ:NOM:PLU:MAS:GRU:SOL");
    tagsForWeise.add("ADJ:NOM:PLU:NEU:GRU:SOL");
    tagsForWeise.add("ADJ:NOM:SIN:FEM:GRU:DEF");
    tagsForWeise.add("ADJ:NOM:SIN:FEM:GRU:IND");
    tagsForWeise.add("ADJ:NOM:SIN:FEM:GRU:SOL");
    tagsForWeise.add("ADJ:NOM:SIN:MAS:GRU:DEF");
    tagsForWeise.add("ADJ:NOM:SIN:NEU:GRU:DEF");
    tagsForWeise.add("ADJ:PRD:GRU");
  }

  private final ManualTagger removalTagger;
  private static final Supplier<ExpansionInfos> expansionInfos = Suppliers.memoize(GermanTagger::initExpansionInfos);

  public static final GermanTagger INSTANCE = new GermanTagger();

  public GermanTagger() {
    super("/de/german.dict", Locale.GERMAN);
    removalTagger = (ManualTagger) ((CombiningTagger) getWordTagger()).getRemovalTagger();
  }

  private static ExpansionInfos initExpansionInfos() {
    Map<String, PrefixInfixVerb> verbInfos = new Object2ObjectOpenHashMap<>();
    Map<String, NominalizedVerb> nominalizedVerbInfos = new Object2ObjectOpenHashMap<>();
    Map<String, NominalizedGenitiveVerb> nominalizedGenVerbInfos = new Object2ObjectOpenHashMap<>();
    Map<String, List<AdjInfo>> adjInfos = new Object2ObjectOpenHashMap<>();
    String filename = "de/hunspell/spelling.txt";
    List<String> spellingWords = new CachingWordListLoader().loadWords(filename);
    for (String line : spellingWords) {
      // '/A' adds the typical adjective endings, so assume it's an adjective:
      if (line.endsWith("/PA") || line.endsWith("/AP")) {
        throw new RuntimeException("Use '/P' or '/A', but not both for a word in " + filename + ": " + line);
      } if (line.endsWith("/P")) {
        String word = line.replaceFirst("/.*", "");
        fillAdjInfos(word, "", toPA2(AdjectiveTags.tagsForAdj), adjInfos);
        fillAdjInfos(word, "e", toPA2(AdjectiveTags.tagsForAdjE), adjInfos);
        fillAdjInfos(word, "en", toPA2(AdjectiveTags.tagsForAdjEn), adjInfos);
        fillAdjInfos(word, "er", toPA2(AdjectiveTags.tagsForAdjEr), adjInfos);
        fillAdjInfos(word, "em", toPA2(AdjectiveTags.tagsForAdjEm), adjInfos);
        fillAdjInfos(word, "es", toPA2(AdjectiveTags.tagsForAdjEs), adjInfos);
      } else if (line.endsWith("/A") &&
          !line.endsWith("ste/A") &&  // don't tag e.g. "fünftjünste/A", would miss the comparative tagging
          !line.endsWith("er/A")) {   // don't tag e.g. "margenstärker/A", would miss the comparative tagging
        String word = line.replaceFirst("/.*", "");
        fillAdjInfos(word, "",  AdjectiveTags.tagsForAdj, adjInfos);
        fillAdjInfos(word, "e",  AdjectiveTags.tagsForAdjE, adjInfos);
        fillAdjInfos(word, "en", AdjectiveTags.tagsForAdjEn, adjInfos);
        fillAdjInfos(word, "er", AdjectiveTags.tagsForAdjEr, adjInfos);
        fillAdjInfos(word, "em", AdjectiveTags.tagsForAdjEm, adjInfos);
        fillAdjInfos(word, "es", AdjectiveTags.tagsForAdjEs, adjInfos);
      } else if (line.contains("_") && !line.endsWith("_in")) {
        String[] parts = line.replace("#.*", "").trim().split("_");
        String prefix = parts[0];
        String verbBaseform = parts[1];
        try {
          String[] forms = GermanSynthesizer.INSTANCE.synthesizeForPosTags(verbBaseform, s -> s.startsWith("VER:"));
          for (String form : forms) {
            if (!form.contains("ß")) {  // skip these, it's too risky to introduce old spellings like "gewußt" from the synthesizer
              verbInfos.put(prefix + form, new PrefixInfixVerb(prefix, "", verbBaseform));
            }
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        verbInfos.put(prefix + "zu" + verbBaseform, new PrefixInfixVerb(prefix, "zu", verbBaseform));  //  "zu<verb>" is not part of forms from synthesizer
        nominalizedVerbInfos.put(uppercaseFirstChar(prefix) + verbBaseform,
          new NominalizedVerb(uppercaseFirstChar(prefix), verbBaseform));
        nominalizedGenVerbInfos.put(uppercaseFirstChar(prefix) + verbBaseform + "s",
          new NominalizedGenitiveVerb(uppercaseFirstChar(prefix), verbBaseform));
      }
    }
    return new ExpansionInfos(verbInfos, nominalizedVerbInfos, nominalizedGenVerbInfos, adjInfos);
  }

  private static List<String> toPA2(List<String> tags) {
    return tags.stream().
      map(k -> k.replaceAll("ADJ:", "PA2:")).
      map(k -> k + ":VER").
      collect(Collectors.toList());
  }

  private static void fillAdjInfos(String word, String suffix, List<String> tagsForForm, Map<String, List<AdjInfo>> adjInfos) {
    List<AdjInfo> l = new ArrayList<>();
    String fullform = word + suffix;
    for (String tag : tagsForForm) {
      l.add(new AdjInfo(word, fullform, tag));
    }
    adjInfos.put(fullform, l);
  }

  private List<TaggedWord> addStem(List<TaggedWord> analyzedWordResults, String stem) {
    List<TaggedWord> result = new ArrayList<>();
    for (TaggedWord tw : analyzedWordResults) {
      String lemma = tw.getLemma();
      if (stem.length() > 0 && stem.charAt(stem.length() - 1) != '-' && tw.getPosTag().startsWith("SUB")) {
        lemma = lemma.toLowerCase();
      }
      result.add(new TaggedWord(stem + lemma, tw.getPosTag()));
    }
    return result;
  }

  //Removes the irrelevant part of dash-linked words (SSL-Zertifikat -> Zertifikat)
  private String sanitizeWord(String word) {
    String result = word;

    //Find the last part of the word that is not nothing
    //Skip words ending in a dash as they'll be misrecognized
    if (!word.endsWith("-")) {
      String[] splitWord = word.split("-");
      String lastPart = splitWord.length > 1 && !splitWord[splitWord.length - 1].trim().equals("") ? splitWord[splitWord.length - 1] : word;

      //Find only the actual important part of the word
      List<String> compoundedWord = GermanCompoundTokenizer.getStrictInstance().tokenize(lastPart);
      if (compoundedWord.size() > 1 && StringTools.startsWithUppercase(word)) {  // don't uppercase last part of e.g. "vanillig-karamelligen"
        lastPart = uppercaseFirstChar(compoundedWord.get(compoundedWord.size() - 1));
      } else {
        lastPart = compoundedWord.get(compoundedWord.size() - 1);
      }

      //Only give result if the last part is either a noun or an adjective (or adjective written in Uppercase)
      List<TaggedWord> tagged = tag(lastPart);
      if (tagged.size() > 0 && (StringUtils.startsWithAny(tagged.get(0).getPosTag(), "SUB", "ADJ") || matchesUppercaseAdjective(lastPart))) {
        result = lastPart;
      }
    }
    return result;
  }

  /**
   * Return only the first reading of the given word or {@code null}.
   */
  @Nullable
  public AnalyzedTokenReadings lookup(String word) throws IOException {
    List<AnalyzedTokenReadings> result = tag(Collections.singletonList(word), false);
    AnalyzedTokenReadings atr = result.get(0);
    if (atr.getAnalyzedToken(0).getPOSTag() == null) {
      return null;
    }
    return atr;
  }

  public List<TaggedWord> tag(String word) {
    return getWordTagger().tag(word);
  }

  private boolean matchesUppercaseAdjective(String unknownUppercaseToken) {
    List<TaggedWord> temp = getWordTagger().tag(StringTools.lowercaseFirstChar(unknownUppercaseToken));
    return temp.size() > 0 && temp.get(0).getPosTag().startsWith("ADJ");
  }

  @Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) throws IOException {
    return tag(sentenceTokens, true);
  }

  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens, boolean ignoreCase) throws IOException {
    boolean firstWord = true;
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    int idxPos = 0;

    String prevWord = null;
    for (String word : sentenceTokens) {
      List<AnalyzedToken> readings = new ArrayList<>();
      List<TaggedWord> taggerTokens = null;
      // Gender star etc:
      String genderGap = "[*:_/]";
      if (idxPos+2 < sentenceTokens.size() && sentenceTokens.get(idxPos+1).matches(genderGap)) {
        if (sentenceTokens.get(idxPos+2).matches("in(nen)?|r|e")) {  // "jede*r", "sein*e"
          taggerTokens = new ArrayList<>();
          taggerTokens.addAll(getWordTagger().tag(word));
          taggerTokens.addAll(getWordTagger().tag(word + sentenceTokens.get(idxPos+2)));
        } else if (sentenceTokens.get(idxPos+2).matches("in(nen)-[A-ZÖÄÜ][a-zöäüß-]+")) {
          // e.g. Werkstudent:innen-Zielgruppe -> take tags of 'Zielgruppe':
          String lastPart = sentenceTokens.get(idxPos+2).replaceFirst(".*-", "");
          taggerTokens = new ArrayList<>(getWordTagger().tag(lastPart));
        } else if (sentenceTokens.get(idxPos+2).matches("innen[a-zöäüß-]+")) {
          // e.g. Werkstudent:innenzielgruppe -> take tags of 'Zielgruppe':
          int idx = sentenceTokens.get(idxPos+2).lastIndexOf("innen");
          String lastPart = StringTools.uppercaseFirstChar(sentenceTokens.get(idxPos+2).substring(idx + "innen".length()));
          taggerTokens = new ArrayList<>(getWordTagger().tag(lastPart));
        }
      }
      if (taggerTokens == null) {
        taggerTokens = getWordTagger().tag(word);
      }

      //Only first iteration. Consider ":" as a potential sentence start marker
      if ((firstWord || ":".equals(prevWord)) && taggerTokens.isEmpty() && ignoreCase) { // e.g. "Das" -> "das" at start of sentence
        taggerTokens = getWordTagger().tag(word.toLowerCase());
        firstWord = !StringUtils.isAlphanumeric(word);
      } else if (pos == 0 && ignoreCase) {   // "Haben", "Sollen", "Können", "Gerade" etc. at start of sentence
        taggerTokens.addAll(getWordTagger().tag(word.toLowerCase()));
      } else if (pos > 1 && taggerTokens.isEmpty() && ignoreCase) {
        int idx = sentenceTokens.indexOf(word);
        // add lowercase token readings to words at start of direct speech
        if (idx > 2 && sentenceTokens.get(idx-1).contentEquals("„") && sentenceTokens.get(idx-3).contentEquals(":")) {
          taggerTokens.addAll(getWordTagger().tag(word.toLowerCase()));
        }
      }

      if (taggerTokens.size() > 0) { //Word known, just add analyzed token to readings
        readings.addAll(getAnalyzedTokens(taggerTokens, word));
        /*
         * Lines 263 to 287
         * do the following for non separable verbs with prefix and for verbs without any prefix:
         *   if (base) verb has tag 'VER:IMP:SIN:SFT', then add 'VER:1:SIN:PRÄ:SFT'
         *   if (base) verb has tag 'VER:1:SIN:PRÄ:SFT', then add 'VER:IMP:SIN:SFT'
         *
         * 'NON' is excluded, because for a given lemma 'VER:IMP:SIN:NON' and 'VER:1:SIN:PRÄ:NON' can differ
         * e. g. (ver)nimm is 'VER:IMP:SIN:NON', but (ver)nehm is 'VER:1:SIN:PRÄ:NON'
         */
        if (!StringUtils.startsWithAny(word.toLowerCase(), prefixesSeparableVerbs)
          && (!StringUtils.startsWithAny(word.toLowerCase(), notAVerb))
          && (word.equals(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase()) || word.equals(word.toLowerCase()))) {
            String lstPrt = "";
            String frstPrt = "";
            if (StringUtils.startsWithAny(word.toLowerCase(), prefixesNonSeparableVerbs)) {
              lstPrt = RegExUtils.removePattern(word.toLowerCase(), prefixesNonSeparableVerbsRegexp);
              frstPrt = StringUtils.removeEnd(word, lstPrt);
            } else {
              lstPrt = word;
              frstPrt = "";
            }
            List<TaggedWord> verbs = getWordTagger().tag(lstPrt);
            for (TaggedWord v : verbs) {
              if ((sentenceTokens.indexOf(word) == 0 || word.equals(word.substring(0, 1).toLowerCase() + word.substring(1)))
                && !StringUtils.equalsAny(lstPrt,"gar", "mal", "null", "trotz")) {
                  if (StringUtils.startsWithAny(v.getPosTag(), "VER:IMP:SIN:SFT") && (!readings.toString().contains("VER:1:SIN:PRÄ:SFT"))) {
                    readings.add(new AnalyzedToken(word, "VER:1:SIN:PRÄ:SFT", frstPrt.toLowerCase() + v.getLemma()));
                  }
                  if (StringUtils.startsWithAny(v.getPosTag(), "VER:1:SIN:PRÄ:SFT") && (!readings.toString().contains("VER:IMP:SIN:SFT"))) {
                    readings.add(new AnalyzedToken(word, "VER:IMP:SIN:SFT", frstPrt.toLowerCase() + v.getLemma()));
                  }
              }
            }
        }
      } else { // Word not known, try to decompose it and use the last part for POS tagging:
        PrefixInfixVerb verbInfo = expansionInfos.get().verbInfos.get(word);
        NominalizedVerb nomVerbInfo = expansionInfos.get().nominalizedVerbInfos.get(word);
        NominalizedGenitiveVerb nomGenVerbInfo = expansionInfos.get().nominalizedGenVerbInfos.get(word);
        List<AdjInfo> adjInfos = expansionInfos.get().adjInfos.get(word);
        boolean addNounTags = !nounTagExpansionExceptions.contains(word);
        //String prefixVerbLastPart = prefixedVerbLastPart(word);   // see https://github.com/languagetool-org/languagetool/issues/2740
        if (verbInfo != null) {   // e.g. "herumgeben" with "herum_geben" in spelling.txt
          if (StringTools.startsWithLowercase(verbInfo.prefix)) {
            String noPrefixForm = word.substring(verbInfo.prefix.length() + verbInfo.infix.length());   // infix can be "zu"
            List<TaggedWord> tags = tag(noPrefixForm);
            boolean isSFT = false;  // SFT = schwaches Verb
            for (TaggedWord tag : tags) {
              if (tag.getPosTag() != null && (StringUtils.startsWithAny(tag.getPosTag(), "VER:", "PA1:", "PA2:")
                && (!StringUtils.startsWithAny(tag.getPosTag(), "VER:MOD", "VER:AUX")))) { // e.g. "schicke" is verb and adjective
                String flektion = tag.getPosTag().substring(tag.getPosTag().length()-3, tag.getPosTag().length());
                if (StringUtils.startsWithAny(verbInfo.prefix, prefixesSeparableVerbs)
                  && (!StringUtils.containsAny(word, notAVerb))) {
                  if (StringUtils.startsWithAny(tag.getPosTag(),"VER:1", "VER:2", "VER:3") && (sentenceTokens.indexOf(word) == 0 || word.equals(word.substring(0, 1).toLowerCase() + word.substring(1)))) {
                    readings.add(new AnalyzedToken(word, tag.getPosTag() + ":NEB", verbInfo.prefix + tag.getLemma()));
                  } else if (!StringUtils.startsWithAny(tag.getPosTag(),"VER:IMP")) {
                    readings.add(new AnalyzedToken(word, tag.getPosTag(), verbInfo.prefix + tag.getLemma()));
                  } else if (StringUtils.startsWithAny(tag.getPosTag(),"VER:IMP:SIN") && (!readings.contains("VER:1:SIN:PRÄ"))) {
                    if (flektion.equals("SFT") || !word.matches(".*i.+")) { // Avoids 'aufnimm'
                      readings.add(new AnalyzedToken(word, "VER:1:SIN:PRÄ:" + flektion + ":NEB", verbInfo.prefix + tag.getLemma()));
                    }
                  }
                } else if (StringUtils.startsWithAny(verbInfo.prefix, prefixesNonSeparableVerbs) //Excludes "ge" (both too rare as verb prefix and prone to FP)
                  && (!StringUtils.containsAny(word, notAVerb))) {
                    if ((StringUtils.startsWithAny(tag.getPosTag(),"VER:IMP:SIN") && (!readings.contains("VER:1:SIN:PRÄ")))
                       || (StringUtils.startsWithAny(tag.getPosTag(),"VER:1:SIN:PRÄ") && (!readings.contains("VER:IMP:SIN")))) {
                         if (flektion.equals("SFT") || !word.matches(".*i.+")) { // Avoids 'zernimm'
                           readings.add(new AnalyzedToken(word, "VER:IMP:SIN" + flektion, verbInfo.prefix + tag.getLemma()));
                           readings.add(new AnalyzedToken(word, "VER:1:SIN:PRÄ:" + flektion, verbInfo.prefix + tag.getLemma()));
                         }
                    } else {
                      readings.add(new AnalyzedToken(word, tag.getPosTag(), verbInfo.prefix + tag.getLemma()));
                    }
                }
                if (tag.getPosTag().contains(":SFT")) {
                  isSFT = true;
                }
              }
            }
            if ("zu".equals(verbInfo.infix)) {
              readings.clear();
              readings.add(new AnalyzedToken(word, "VER:EIZ:" + (isSFT ? "SFT" : "NON"), verbInfo.prefix + verbInfo.verbBaseform));
            }
          }
        } else if (nomVerbInfo != null && addNounTags) {
          // e.g. "herum_geben" in spelling.txt -> "(das) Herumgeben"
          readings.add(new AnalyzedToken(word, "SUB:NOM:SIN:NEU:INF", nomVerbInfo.prefix + nomVerbInfo.verbBaseform));
          readings.add(new AnalyzedToken(word, "SUB:AKK:SIN:NEU:INF", nomVerbInfo.prefix + nomVerbInfo.verbBaseform));
          readings.add(new AnalyzedToken(word, "SUB:DAT:SIN:NEU:INF", nomVerbInfo.prefix + nomVerbInfo.verbBaseform));
        } else if (nomGenVerbInfo != null && addNounTags) {
          // e.g. "herum_geben" in spelling.txt -> "(des) Herumgebens"
          readings.add(new AnalyzedToken(word, "SUB:GEN:SIN:NEU:INF", nomGenVerbInfo.prefix + nomGenVerbInfo.verbBaseform));
        /*} else if (prefixVerbLastPart != null) {   // "aufstöhnen" etc.
          List<TaggedWord> taggedWords = getWordTagger().tag(prefixVerbLastPart);
          String firstPart = word.replaceFirst(prefixVerbLastPart + "$", "");
          for (TaggedWord taggedWord : taggedWords) {
            readings.add(new AnalyzedToken(word, taggedWord.getPosTag(), firstPart+taggedWord.getLemma()));
          }*/
        } else if (adjInfos != null) {
          for (AdjInfo adjInfo : adjInfos) {
            readings.add(new AnalyzedToken(adjInfo.fullForm, adjInfo.tag, adjInfo.baseform));
          }
        } else if (isWeiseException(word)) {   // "idealerweise" etc. but not "überweise", "eimerweise"
          for (String tag : tagsForWeise) {
            readings.add(new AnalyzedToken(word, tag, word));
          }
        } else if (!StringUtils.isAllBlank(word) && word.matches("[A-ZÖÄÜ][a-zöäüß]{2,25}mitarbeitenden?")) {
          int idx = word.indexOf("mitarbeitende");
          String firstPart = word.substring(0, idx);  // we might tag invalid words, but that should be okay
          String lastPart = word.substring(idx);
          List<TaggedWord> mitarbeitendeTags = getWordTagger().tag(StringTools.uppercaseFirstChar(lastPart));
          for (TaggedWord mitarbeitendeTag : mitarbeitendeTags) {
            readings.add(new AnalyzedToken(word, mitarbeitendeTag.getPosTag(), firstPart+"mitarbeitende"));
          }
        } else if (!StringUtils.isAllBlank(word)) {
          List<String> compoundParts = GermanCompoundTokenizer.getStrictInstance().tokenize(word);
          if (compoundParts.size() <= 1) {//Could not find simple compound parts
            // Recognize alternative imperative forms (e.g., "Geh bitte!" in addition to "Gehe bitte!")
            List<AnalyzedToken> imperativeFormList = getImperativeForm(word, sentenceTokens, pos);
            List<AnalyzedToken> substantivatedFormsList = getSubstantivatedForms(word, sentenceTokens);
            if (imperativeFormList.size() > 0) {
              readings.addAll(imperativeFormList);
            } else if (substantivatedFormsList.size() > 0) {
              readings.addAll(substantivatedFormsList);
            } else {
              if (StringUtils.startsWithAny(word, "bitter", "dunkel", "erz", "extra", "früh",
                "gemein", "hyper", "lau", "mega", "minder", "stock", "super", "tod", "ultra", "un", "ur")) {
                String lastPart = RegExUtils.removePattern(word, "^(bitter|dunkel|erz|extra|früh|gemein|grund|hyper|lau|mega|minder|stock|super|tod|ultra|u[nr]|voll)");
                if (lastPart.length() > 3) {
                  String firstPart = StringUtils.removeEnd(word, lastPart);
                  List<TaggedWord> taggedWords = getWordTagger().tag(lastPart);
                  for (TaggedWord taggedWord : taggedWords) {
                    if (!(firstPart.length() == 2 && taggedWord.getPosTag().startsWith("VER"))) {
                      readings.add(new AnalyzedToken(word, taggedWord.getPosTag(), firstPart+taggedWord.getLemma()));
                    }
                  }
                }
              }
              //Separate dash-linked words
              //Only check single word tokens and skip words containing numbers because it's unpredictable
              if (StringUtils.split(word, ' ').length == 1 && !Character.isDigit(word.charAt(0))) {
                String wordOrig = word;
                word = sanitizeWord(word);
                String wordStem = wordOrig.substring(0, wordOrig.length() - word.length());

                //Tokenize, start word uppercase if it's a result of splitting
                List<String> compoundedWord = GermanCompoundTokenizer.getStrictInstance().tokenize(word);
                if (compoundedWord.size() > 1) {
                  word = uppercaseFirstChar(compoundedWord.get(compoundedWord.size() - 1));
                } else {
                  word = compoundedWord.get(compoundedWord.size() - 1);
                }

                List<TaggedWord> linkedTaggerTokens = addStem(getWordTagger().tag(word), wordStem); //Try to analyze the last part found

                //Some words that are linked with a dash ('-') will be written in uppercase, even adjectives
                if (wordOrig.contains("-") && linkedTaggerTokens.isEmpty() && matchesUppercaseAdjective(word)) {
                  word = StringTools.lowercaseFirstChar(word);
                  linkedTaggerTokens = getWordTagger().tag(word);
                }

                word = wordOrig;

                boolean wordStartsUppercase = StringTools.startsWithUppercase(word);
                if (linkedTaggerTokens.isEmpty()) {
                  /*
                   *Verbs with certain prefixes (e. g. "ab", "ein", "zwischen") are always separable.
                   *For better performance of rules, forms like 'einlädst' and 'lädst ein' should be tagged differently.
                   *einlädst [VER:2:SIN:PRÄ:NON:NEB] ('NEB' indicates that this form can only appear in a subordinate clause)
                   *lädst ein [VER:2:SIN:PRÄ:NON] + [ZUS]
                   */
                  if (StringUtils.startsWithAny(word.toLowerCase(), prefixesVerbs)
                     && (!StringUtils.containsAny(word.toLowerCase(), notAVerb))
                     && (word.equals(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase()) || word.equals(word.toLowerCase()))) { // avoids CamelCase, ALLCAPS...
                       String lastPart = RegExUtils.removePattern(word.toLowerCase(), prefixesVerbsRegexp);
                      if (lastPart.length() > 2) { // e. g. 'kau', 'iss', 'ess'
                        String firstPart = StringUtils.removeEnd(word, lastPart);
                        //Erweiterter Infinitiv mit zu
                        if (StringUtils.startsWithAny(lastPart, "zu")) {
                          String infinitiv = StringUtils.removeStart(lastPart, "zu");
                          List<TaggedWord> infs = getWordTagger().tag(infinitiv);
                          for (TaggedWord inf : infs) {
                            if (inf.getPosTag().startsWith("VER:INF")) {
                              String pstg = RegExUtils.replaceFirst(inf.getPosTag(), "INF", "EIZ");
                              readings.add(new AnalyzedToken(word, pstg, firstPart + inf.getLemma()));
                            }
                          }
                        }
                        // Checks for postag information in the last part of given word
                        List<TaggedWord> taggedWords = getWordTagger().tag(lastPart);
                        for (TaggedWord taggedWord : taggedWords) {
                          if ((taggedWord.getPosTag().startsWith("VER") && (!taggedWord.getPosTag().startsWith("VER:PA")))
                            && (!taggedWord.getPosTag().startsWith("VER:AUX"))
                            && (!taggedWord.getPosTag().startsWith("VER:MOD"))
                            && (!firstPart.equals("un"))) { // avoids 'unbeeindruckt' -> VER.*
                              if (taggedWord.getPosTag().startsWith("VER:INF")) {
                                if (word.equals(word.substring(0, 1).toUpperCase() + word.substring(1))) {
                                  readings.add(new AnalyzedToken(word, "SUB:NOM:SIN:NEU:INF", word));
                                  readings.add(new AnalyzedToken(word, "SUB:DAT:SIN:NEU:INF", word));
                                  readings.add(new AnalyzedToken(word, "SUB:AKK:SIN:NEU:INF", word));
                                  if (sentenceTokens.indexOf(word) == 0) {
                                    readings.add(new AnalyzedToken(word, taggedWord.getPosTag(), firstPart.toLowerCase() + taggedWord.getLemma()));
                                  }
                                } else {
                                  readings.add(new AnalyzedToken(word, taggedWord.getPosTag(), firstPart.toLowerCase() + taggedWord.getLemma()));
                                }
                              } else if (taggedWord.getPosTag().startsWith("VER:IMP")) {
                                String flekt = taggedWord.getPosTag().substring(taggedWord.getPosTag().length()-3, taggedWord.getPosTag().length());
                                if ((word.equals(word.toLowerCase()) || sentenceTokens.indexOf(word) == 0)) {
                                  if (!StringUtils.equalsAny(firstPart.toLowerCase(), prefixesSeparableVerbs)) { // Separable verbs do not have imperative form.
                                    readings.add(new AnalyzedToken(word, taggedWord.getPosTag(), firstPart.toLowerCase() + taggedWord.getLemma()));
                                    if (taggedWord.getPosTag().startsWith("VER:IMP:SIN") && !readings.contains("VER:1:SIN:PRÄ")) {
                                      if (!readings.contains("VER:IMP:SIN:NON")) {
                                        readings.add(new AnalyzedToken(word, "VER:1:SIN:PRÄ:" + flekt, firstPart + taggedWord.getLemma()));
                                      }
                                    }
                                  } else if (!readings.contains("VER:1:SIN:PRÄ") && (flekt.equals("SFT") || !word.matches(".*i.+"))) {
                                    readings.add(new AnalyzedToken(word, "VER:1:SIN:PRÄ:" + flekt + ":NEB", firstPart + taggedWord.getLemma()));
                                  }
                                }
                              } else if (StringUtils.equalsAny(firstPart.toLowerCase(), prefixesSeparableVerbs)
                                && (word.equals(word.toLowerCase()) || sentenceTokens.indexOf(word) == 0)) {
                                  if (taggedWord.getPosTag().endsWith(":NEB")) {
                                    readings.add(new AnalyzedToken(word, taggedWord.getPosTag(), firstPart.toLowerCase() + taggedWord.getLemma()));
                                  } else {
                                    readings.add(new AnalyzedToken(word, taggedWord.getPosTag() + ":NEB", firstPart.toLowerCase() + taggedWord.getLemma()));
                                  }
                                  if (taggedWord.getPosTag().startsWith("VER:3:SIN:PRÄ")
                                    && (firstPart.equals("durch") || firstPart.equals("um"))) {
                                      /*
                                       / Verbs with prefixes 'durch' or 'um'
                                       / can be both separable and non separable
                                       / 'Tom läuft durch den Wald'
                                       / 'Tom durchläuft eine Durststrecke'
                                       / This avoids false alarms
                                      */
                                      if (taggedWord.getPosTag().startsWith("VER:3:SIN:PRÄ")) {
                                        readings.add(new AnalyzedToken(word, "VER:PA2:SFT", firstPart + taggedWord.getLemma()));
                                      } else {
                                        readings.add(new AnalyzedToken(word, "VER:PA2:NON", firstPart + taggedWord.getLemma()));
                                      }
                                      readings.add(new AnalyzedToken(word, "PA2:PRD:GRU:VER", word));
                                  }
                              } else if (StringUtils.equalsAny(firstPart.toLowerCase(), prefixesNonSeparableVerbs)
                                && (word.equals(word.toLowerCase()) || sentenceTokens.indexOf(word) == 0)) {
                                  readings.add(new AnalyzedToken(word, taggedWord.getPosTag(), firstPart.toLowerCase() + taggedWord.getLemma()));
                                  /*
                                   / Postag of 'lastPart' never starts with PA2: erstickt = er + stickt
                                   / Derives 'VER:PA2:SFT' and 'PA2:PRD:GRU:VER', if postag of 'lastPart' equals 'VER:3:SIN:PRÄ:SFT'
                                   / Using other postags is not safe, especially 'VER.*NON'
                                  */
                                  if (taggedWord.getPosTag().startsWith("VER:3:SIN:PRÄ:SFT")
                                    || (taggedWord.getPosTag().startsWith("VER:1:PLU:PRÄ:NON") && (StringUtils.containsAny(taggedWord.getLemma(), partizip2contains1PluPra)))
                                    || (taggedWord.getPosTag().startsWith("VER:1:PLU:PRT:NON") && (StringUtils.containsAny(taggedWord.getLemma(), partizip2contains1PluPrt)))) {
                                      if (!firstPart.equals("un")) { // Avoids 'unbeeindruckt' -> 'VER.*'
                                        String fl = taggedWord.getPosTag().substring(taggedWord.getPosTag().length()-3, taggedWord.getPosTag().length());
                                        readings.add(new AnalyzedToken(word, "VER:PA2:" + fl, firstPart + taggedWord.getLemma()));
                                      }
                                      readings.add(new AnalyzedToken(word, "PA2:PRD:GRU:VER", word));
                                  }
                              }
                          } else if (((taggedWord.getPosTag().startsWith("PA") || taggedWord.getPosTag().startsWith("VER:PA"))
                            && (word.equals(word.toLowerCase()) || sentenceTokens.indexOf(word) == 0))) {
                              if (!(firstPart.equals("un") && taggedWord.getPosTag().startsWith("VER:PA"))) {
                                readings.add(new AnalyzedToken(word, taggedWord.getPosTag(), firstPart.toLowerCase() + taggedWord.getLemma()));
                              }
                          }
                        }
                        // Checks for postag information in the last part of given word
                        /*
                         / Postag of 'lastPart' never starts with PA2: erstickter = er + stickter
                         / Derives 'PA2:[NGDA].*', if word has
                         / suffix 'e[mnrs]?' and
                         / 'middlePart' has tagging 'VER:3:SIN:PRÄ:SFT'
                         / e. g. erstickter = er + stickt + er
                        */
                        String[] partizipSuffixes = new String[]{"e", "em", "en", "er", "es"};
                        String middlePart = "";
                        String suffix = "";
                        for (String sffx : partizipSuffixes) {
                          if (lastPart.endsWith(sffx)){
                            middlePart = lastPart.substring(0, lastPart.length()-sffx.length());
                            suffix = sffx;
                          }
                        }
                        List<TaggedWord> taggedMiddle = getWordTagger().tag(middlePart);
                        for (TaggedWord taggedM : taggedMiddle) {
                          if (taggedM.getPosTag().startsWith("VER:3:SIN:PRÄ:SFT")
                            && (word.equals(word.toLowerCase()) || sentenceTokens.indexOf(word) == 0)) {
                              String lemma = word.substring(0, word.length()-suffix.length());
                              switch (suffix) {
                                case "e":
                                  for (String posEndsWithE : postagsPartizipEndingE) {
                                    readings.add(new AnalyzedToken(word, "PA2:"+posEndsWithE, lemma));
                                  }
                                  break;
                                case "em":
                                  for (String posEndsWithEm : postagsPartizipEndingEm) {
                                    readings.add(new AnalyzedToken(word, "PA2:"+posEndsWithEm, lemma));
                                  }
                                  break;
                                case "en":
                                  for (String posEndsWithEn : postagsPartizipEndingEn) {
                                    readings.add(new AnalyzedToken(word, "PA2:"+posEndsWithEn, lemma));
                                  }
                                  break;
                                case "er":
                                  for (String posEndsWithEr : postagsPartizipEndingEr) {
                                    readings.add(new AnalyzedToken(word, "PA2:"+posEndsWithEr, lemma));
                                  }
                                  break;
                                case "es":
                                  for (String posEndsWithEs : postagsPartizipEndingEs) {
                                    readings.add(new AnalyzedToken(word, "PA2:"+posEndsWithEs, lemma));
                                  }
                                  break;
                              }
                          }
                        }
                      }
                  } else {
                    readings.add(getNoInfoToken(word));
                  }
                } else {
                  if (wordStartsUppercase) { //Choose between uppercase/lowercase Lemma
                    readings.addAll(getAnalyzedTokens(linkedTaggerTokens, word));
                  } else {
                    readings.addAll(getAnalyzedTokens(linkedTaggerTokens, word, compoundedWord));
                  }
                }
              } else {
                readings.add(getNoInfoToken(word));
              }
            }
          } else if (!(idxPos+2 < sentenceTokens.size() && sentenceTokens.get(idxPos+1).equals(".") && sentenceTokens.get(idxPos+2).matches("com|net|org|de|at|ch|fr|uk|gov"))) {  // TODO: find better way to ignore domains
            // last part governs a word's POS:
            String lastPart = compoundParts.get(compoundParts.size() - 1);
            if (StringTools.startsWithUppercase(word)) {
              lastPart = uppercaseFirstChar(lastPart);
            }
            List<TaggedWord> partTaggerTokens = getWordTagger().tag(lastPart);
            if (partTaggerTokens.isEmpty()) {
              readings.add(getNoInfoToken(word));
            } else {
              List<AnalyzedToken> temp = getAnalyzedTokens(partTaggerTokens, word, compoundParts);
              String firstPart = compoundParts.get(0);
              List<String> prfxs = new ArrayList<>(Arrays.asList("ab", "abend", "abhanden", "acht", "ähnlich", "allein", "an", "auf", "aufeinander", "aufrecht", "aufwärts", "aus", "auseinander", "auswärts", "bei", "beieinander", "beisammen", "beiseite", "besser", "blank", "brust", "da", "daheim", "dahin", "daneben", "danieder", "davon", "doppel", "drauflos", "drei", "drein", "durch", "durcheinander", "ehe", "ein", "einig", "einwärts", "eis", "empor", "end", "fehl", "feil", "feinst", "fort", "frei", "gegenüber", "general", "groß", "grund", "hand", "hart", "heim", "her", "herauf", "heraus", "herbei", "hernieder", "herüber", "herum", "herunter", "hier", "hierher", "hierhin", "hin", "hinauf", "hinaus", "hindurch", "hinein", "hinüber", "hoch", "höher", "ineinander", "kaputt", "kennen", "klar", "klein", "knapp", "krank", "krumm", "kugel", "kürzer", "lahm", "los", "maß", "miss", "mit", "mittag", "nach", "nahe", "näher", "neben", "nebeneinander", "nieder", "offen", "out", "preis", "quer", "ran", "rauf", "raus", "rein", "rüber", "rück", "rückwärts", "ruhig", "rum", "runter", "satt", "schwarz", "sicher", "sitzen", "statt", "still", "stoß", "teil", "tot", "trocken", "über", "überein", "übereinander", "übrig", "um", "umher", "unter", "verrückt", "voll", "vor", "voran", "voraus", "vorbei", "vorlieb", "vorüber", "vorwärts", "vorweg", "wach", "wahr", "warm", "weg", "weh", "weiter", "wert", "wichtig", "wieder", "wiederauf", "wiederein", "wiederher", "wohl", "zu", "zueinander", "zufrieden", "zugute", "zunichte", "zurecht", "zurück", "zusammen", "zuwider", "zwangs", "zwangsum", "zwangsvor", "zweck", "zwischen"));
              if (prfxs.contains(firstPart)) {
                for (TaggedWord tag : partTaggerTokens) {
                  if (StringUtils.startsWithAny(tag.getPosTag(),"VER:1", "VER:2", "VER:3") && (sentenceTokens.indexOf(word) == 0 || word.equals(word.substring(0, 1).toLowerCase() + word.substring(1)))) {
                    if (StringUtils.endsWith(tag.getPosTag(), "NEB")) {
                      readings.add(new AnalyzedToken(word, tag.getPosTag(), firstPart + tag.getLemma()));
                    } else {
                      readings.add(new AnalyzedToken(word, tag.getPosTag() + ":NEB", firstPart + tag.getLemma()));
                    }
                  } else if (!StringUtils.startsWithAny(tag.getPosTag(),"VER:IMP")) {
                    readings.add(new AnalyzedToken(word, tag.getPosTag(), firstPart + tag.getLemma()));
                  }
                }
              } else {
                temp = temp.stream().filter(k -> !k.getPOSTag().contains("VER")).collect(Collectors.toList());
                readings.addAll(temp);
              }
            }
          }
        }
        if (readings.isEmpty()) {
          readings.add(getNoInfoToken(word));
        }
      }
      tokenReadings.add(new AnalyzedTokenReadings(readings.toArray(new AnalyzedToken[0]), pos));
      pos += word.length();
      prevWord = word;
      idxPos++;
    }
    return tokenReadings;
  }

  @Nullable
  String prefixedVerbLastPart(String word) {
    // "aufstöhnen" (auf+stöhnen) etc.
    for (String prefix : VerbPrefixes.get()) {
      if (word.startsWith(prefix)) {
        List<TaggedWord> tags = tag(word.replaceFirst("^" + prefix, ""));
        if (tags.stream().anyMatch(k -> k.getPosTag() != null && k.getPosTag().startsWith("VER"))) {
          return word.substring(prefix.length());
        }
      }
    }
    return null;
  }

  boolean isWeiseException(String word) {
    if (word.endsWith("erweise")) {  // "idealerweise" etc.
      List<TaggedWord> tags = tag(StringUtils.removeEnd(word, "erweise"));
      return tags.stream().anyMatch(k -> k.getPosTag() != null && k.getPosTag().startsWith("ADJ"));
    }
    return false;
  }

  /*
   * Tag alternative imperative forms (e.g., "Geh bitte!" in addition to "Gehe bitte!")
   * To avoid false positives and conflicts with DE_CASE the tagging is restricted to
   * [a] words at the start of a sentence ("Geh bitte!") if the sentence counts more than one word
   * [b1] words preceded by ich/ihr/er/es/sie to catch some real errors ("Er geh jetzt.") by the new rule in rulegroup SUBJECT_VERB_AGREEMENT
   * [b2] words preceded by aber/nun/jetzt (e.g., "Bitte geh!", "Jetzt sag schon!" etc.)
   * @param word to be checked
   */
  private List<AnalyzedToken> getImperativeForm(String word, List<String> sentenceTokens, int pos) {
    int idx = sentenceTokens.indexOf(word);
    String previousWord = "";
    while (--idx > -1) {
      previousWord = sentenceTokens.get(idx);
      if (!StringUtils.isWhitespace(previousWord)) {
        break;
      }
    }
    if (!(pos == 0 && sentenceTokens.size() > 1)
        && !StringUtils.equalsAnyIgnoreCase(previousWord, "ich", "er", "es", "sie", "bitte", "aber", "nun", "jetzt", "„")) {
      return Collections.emptyList();
    }
    String w = pos == 0 || "„".equals(previousWord) ? word.toLowerCase() : word;
    List<TaggedWord> taggedWithE = getWordTagger().tag(w.concat("e"));
    for (TaggedWord tagged : taggedWithE) {
      if (tagged.getPosTag().startsWith("VER:IMP:SIN")) {
        // do not overwrite manually removed tags
        if (removalTagger == null || !removalTagger.tag(w).contains(tagged)) {
          return getAnalyzedTokens(Arrays.asList(tagged), word);
        }
        break;
      }
    }
    return Collections.emptyList();
  }

  /*
   * Tag substantivated adjectives and participles, which are currently tagged not tagged correctly
   * (e.g., "Verletzter" in "Ein Verletzter kam ins Krankenhaus" needs to be tagged as "SUB:NOM:SIN:MAS")
   * @param word to be checked
   */
  private List<AnalyzedToken> getSubstantivatedForms(String word, List<String> sentenceTokens) {
    if (word.endsWith("er")) {
      if (word.matches("\\d{4}+er")) {
        // e.g. "Den 2019er Wert hatten sie geschätzt"
        List<AnalyzedToken> list = new ArrayList<>();
        for (String tag : allAdjGruTags) {
          list.add(new AnalyzedToken(word, tag, word));
        }
        return list;
      }
      List<TaggedWord> lowerCaseTags = getWordTagger().tag(word.toLowerCase());
      // do not add tag words whose lower case variant is an adverb (e.g, "Früher") to avoid false negatives for DE_CASE
      if (lowerCaseTags.stream().anyMatch(t -> t.getPosTag().startsWith("ADV"))) {
        return Collections.emptyList();
      }
      int idx = sentenceTokens.indexOf(word);
      // is followed by an uppercase word? If 'yes', the word is probably not substantivated
      while (++idx < sentenceTokens.size()) {
        String nextWord = sentenceTokens.get(idx);
        if (StringUtils.isWhitespace(nextWord)) {
          continue;
        }
        if (nextWord.length() > 0 && (Character.isUpperCase(nextWord.charAt(0)) || "als".equals(nextWord))) {
          return Collections.emptyList();
        }
        break;
      }
      String femaleForm = word.substring(0, word.length()-1);
      List<TaggedWord> taggedFemaleForm = getWordTagger().tag(femaleForm);
      boolean isSubstantivatedForm = taggedFemaleForm.stream().anyMatch(t -> t.getPosTag().equals("SUB:NOM:SIN:FEM:ADJ"));
      if (isSubstantivatedForm) {
        List<AnalyzedToken> list = new ArrayList<>();
        list.add(new AnalyzedToken(word, "SUB:NOM:SIN:MAS:ADJ", word));
        list.add(new AnalyzedToken(word, "SUB:GEN:PLU:MAS:ADJ", word));
        return list;
      }
    }
    return Collections.emptyList();
  }

  private AnalyzedToken getNoInfoToken(String word) {
    return new AnalyzedToken(word, null, null);
  }

  private List<AnalyzedToken> getAnalyzedTokens(List<TaggedWord> taggedWords, String word) {
    List<AnalyzedToken> result = new ArrayList<>();
    for (TaggedWord taggedWord : taggedWords) {
      result.add(new AnalyzedToken(word, taggedWord.getPosTag(), taggedWord.getLemma()));
    }
    return result;
  }

  private List<AnalyzedToken> getAnalyzedTokens(List<TaggedWord> taggedWords, String word, List<String> compoundParts) {
    List<AnalyzedToken> result = new ArrayList<>();
    for (TaggedWord taggedWord : taggedWords) {
      if (taggedWord.getPosTag() != null && taggedWord.getPosTag().startsWith("VER:IMP")) {
        // ignore imperative, as otherwise e.g. "zehnfach" will be interpreted as a verb (zehn + fach)
        continue;
      }
      List<String> allButLastPart = compoundParts.subList(0, compoundParts.size() - 1);
      StringBuilder lemma = new StringBuilder();
      int i = 0;
      for (String s : allButLastPart) {
        lemma.append(i == 0 ? s : StringTools.lowercaseFirstChar(s));
        i++;
      }
      lemma.append(StringTools.lowercaseFirstChar(taggedWord.getLemma()));
      result.add(new AnalyzedToken(word, taggedWord.getPosTag(), lemma.toString()));
    }
    return result;
  }

  static class PrefixInfixVerb {
    String prefix;
    String infix;
    String verbBaseform;
    PrefixInfixVerb(String prefix, String infix, String verbBaseform) {
      this.prefix = prefix;
      this.infix = infix;
      this.verbBaseform = verbBaseform;
    }
  }

  static class AdjInfo {
    String baseform;
    String fullForm;
    String tag;
    AdjInfo(String baseform, String fullForm, String tag) {
      this.baseform = baseform;
      this.fullForm = fullForm;
      this.tag = tag;
    }
  }

  static class NominalizedVerb {
    String prefix;
    String verbBaseform;
    NominalizedVerb(String prefix, String verbBaseform) {
      this.prefix = prefix;
      this.verbBaseform = verbBaseform;
    }
  }

  static class NominalizedGenitiveVerb {
    String prefix;
    String verbBaseform;
    NominalizedGenitiveVerb(String prefix, String verbBaseform) {
      this.prefix = prefix;
      this.verbBaseform = verbBaseform;
    }
  }

  static class ExpansionInfos {
    Map<String, PrefixInfixVerb> verbInfos;
    Map<String, NominalizedVerb> nominalizedVerbInfos;
    Map<String, NominalizedGenitiveVerb> nominalizedGenVerbInfos;
    Map<String, List<AdjInfo>> adjInfos;
    ExpansionInfos(Map<String, PrefixInfixVerb> verbInfos, Map<String, NominalizedVerb> nominalizedVerbInfos,
                   Map<String, NominalizedGenitiveVerb> nominalizedGenVerbInfos, Map<String, List<AdjInfo>> adjInfos) {
      this.verbInfos = verbInfos;
      this.nominalizedVerbInfos = nominalizedVerbInfos;
      this.nominalizedGenVerbInfos = nominalizedGenVerbInfos;
      this.adjInfos = adjInfos;
    }
  }

}
