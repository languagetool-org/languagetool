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
package org.languagetool.rules.pt;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;

import org.languagetool.rules.WordRepeatRule;

/**
 * Palavras que se repetem no Poruguês.
 * @author Tiago F. Santos
 * @since 3.6
 */
public class PortugueseWordRepeatRule extends WordRepeatRule {

  // Tautonym list from https://en.wikipedia.org/wiki/List_of_tautonyms
  private static final Pattern TAUTONYMS_GENUS = Pattern.compile("Aaptos|Acanthogyrus|Achatina|Agagus|Agama|Alburnus|Alces|Alle|Alosa|Amandava|Amazilia|Ameiva|Anableps|Anguilla|Anguilla|Anhinga|Anostomus|Anser|Anthias|Apus|Arcinella|Ariadne|Aspredo|Astacus|Avicularia|Axis|Badis|Bagarius|Bagre|Balanus|Banjos|Barbatula|Barbus|Basiliscus|Batasio|Belobranchus|Belone|Belonimorphis|Bidyanus|Bison|Bombina|Boops|Brama|Brosme|Bubo|Bucayana|Bufo|Buteo|Butis|Calamus|Calappa|Caleta|Callichthys|Calotes|Capoeta|Capreolus|Caracal|Carassius|Carassius|Cardinalis|Carduelis|Caretta|Casuarius|Catla|Catostomus|Cephea|Cerastes|Chaca|Chalcides|Chandramara|Chanos|Chaos|Chinchilla|Chiropotes|Chitala|Chromis|Ciconia|Cidaris|Cinclus|Citellus|Clelia|Coccothraustes|Coccothraustes|Cochlearius|Coeligena|Colius|Columella|Concholepas|Concholepas|Conger|Conta|Convoluta|Cordylus|Coscoroba|Cossus|Cotinga|Coturnix|Crangon|Cressida|Crex|Cricetus|Crocuta|Crossoptilon|Curaeus|Cyanicterus|Cygnus|Cymbium|Cynoglossus|Dama|Dario|Dentex|Devario|Diuca|Dives|Dolabrifera|Enhydris|Ensifera|Ensifera|Ensis|Erythrinus|Extra|Falcipennis|Feroculus|Ficus|Fragum|Francolinus|Furcula|Gagata|Galbula|Gallinago|Gallus|Gazella|Gazella|Gemma|Genetta|Gerbillus|Gibberulus|Giraffa|Glis|Glycimeris|Glyphis|Gobio|Goliathus|Gonorynchus|Gorilla|Grapsus|Grapsus|Grus|Gryllotalpa|Guira|Gulo|Hara|Harpa|Haustellum|Hemilepidotus|Heterophyes|Himantopus|Himantopus|Hippocampus|Hippoglossus|Hippopus|Histrio|Histrionicus|Hoolock|Hucho|Huso|Hyaena|Hypnale|Ichthyaetus|Icterus|Idea|Iguana|Indicator|Indri|Indri|Jacana|Jaculus|Janthina|Kachuga|Koilofera|Lactarius|Lagocephalus|Lagopus|Lagopus|Lagurus|Lambis|Lemmus|Lepadogaster|Lerwa|Leuciscus|Lima|Limanda|Limanda|Limosa|Liparis|Lithognathus|Lithophaga|Loa|Lota|Luscinia|Lutjanus|Lutra|Lutraria|Lynx|Macrophyllum|Manacus|Margaritifera|Marmota|Martes|Mascarinus|Mashuna|Megacephala|Melanodera|Meles|Melo|Melolontha|Melolontha|Melongena|Menidia|Mephitis|Mercenaria|Meretrix|Merluccius|Meza|Microstoma|Milvus|Milvus|Mitella|Mitra|Mitu|Modiolus|Modulus|Mola|Molossus|Molva|Monachus|Moniliformis|Mops|Mustelus|Myaka|Myospalax|Myotis|Myotis|Naja|Naja|Nangra|Nasua|Natrix|Neita|Niviventer|Notopterus|Nycticorax|Nycticorax|Oenanthe|Ogasawarana|Oliva|Ophioscincus|Oplopomus|Oreotragus|Oriolus|Pagrus|Pangasius|Papio|Pauxi|Perdix|Periphylla|Perna|Petaurista|Petronia|Phocoena|Phoenicurus|Phoxinus|Phycis|Pica|Pipa|Pipile|Pipistrellus|Pipra|Pithecia|Planorbis|Plica|Poliocephalus|Pollachius|Pollicipes|Porites|Porphyrio|Porphyrolaema|Porpita|Porzana|Pristis|Pseudobagarius|Pudu|Puffinus|Pungitius|Pyrrhocorax|Pyrrhula|Quadrula|Quelea|Rama|Ranina|Rapa|Rasbora|Rattus|Redunca|Regulus|Remora|Retropinna|Rhinobatos|Riparia|Rita|Rupicapra|Rupicola|Rutilus|Saccolaimus|Salamandra|Sarda|Scalpellum|Scincus|Scolytus|Sephanoides|Serinus|Sodreana|Solea|Sphyraena|Spinachia|Spirorbis|Spirula|Sprattus|Squatina|Staphylaea|Suiriri|Sula|Suta|Synodus|Tadorna|Tandanus|Tchagra|Telescopium|Temnurus|Terebellum|Tetradactylus|Tetrax|Therezopolis|Thymallus|Tibicen|Tinca|Todus|Torpedo|Trachurus|Trachycorystes|Trachyrinchus|Tricornis|Troglodytes|Tropheops|Tubifex|Tyrannus|Umbraculum|Uncia|Vanellus|Vanellus|Velella|Velella|Velutina|Vicugna|Villosa|Vimba|Viviparus|Volva|Vulpes|Vulpes|Xanthocephalus|Xanthostigma|Xenopirostris|Ypiranga|Zebrus|Zera|Zingel|Zingha|Zoma|Zonia|Zungaro|Zygoneura|Se");

  private static final Pattern TAUTONYMS_SPECIES = Pattern.compile("aaptos|acanthogyrus|achatina|agagus|agama|alburnus|alces|alle|alosa|amandava|amazilia|ameiva|anableps|anguilla|anguilla|anhinga|anostomus|anser|anthias|apus|arcinella|ariadne|aspredo|astacus|avicularia|axis|badis|bagarius|bagre|balanus|banjos|barbatula|barbus|basiliscus|batasio|belobranchus|belone|belonimorphis|bidyanus|bison|bombina|boops|brama|brosme|bubo|bucayana|bufo|buteo|butis|calamus|calappa|caleta|callichthys|calotes|capoeta|capreolus|caracal|carassius|carassius|cardinalis|carduelis|caretta|casuarius|catla|catostomus|cephea|cerastes|chaca|chalcides|chandramara|chanos|chaos|chinchilla|chiropotes|chitala|chromis|ciconia|cidaris|cinclus|citellus|clelia|coccothraustes|coccothraustes|cochlearius|coeligena|colius|columella|concholepas|concholepas|conger|conta|convoluta|cordylus|coscoroba|cossus|cotinga|coturnix|crangon|cressida|crex|cricetus|crocuta|crossoptilon|curaeus|cyanicterus|cygnus|cymbium|cynoglossus|dama|dario|dentex|devario|diuca|dives|dolabrifera|enhydris|ensifera|ensifera|ensis|erythrinus|extra|falcipennis|feroculus|ficus|fragum|francolinus|furcula|gagata|galbula|gallinago|gallus|gazella|gazella|gemma|genetta|gerbillus|gibberulus|giraffa|glis|glycimeris|glyphis|gobio|goliathus|gonorynchus|gorilla|grapsus|grapsus|grus|gryllotalpa|guira|gulo|hara|harpa|haustellum|hemilepidotus|heterophyes|himantopus|himantopus|hippocampus|hippoglossus|hippopus|histrio|histrionicus|hoolock|hucho|huso|hyaena|hypnale|ichthyaetus|icterus|idea|iguana|indicator|indri|indri|jacana|jaculus|janthina|kachuga|koilofera|lactarius|lagocephalus|lagopus|lagopus|lagurus|lambis|lemmus|lepadogaster|lerwa|leuciscus|lima|limanda|limanda|limosa|liparis|lithognathus|lithophaga|loa|lota|luscinia|lutjanus|lutra|lutraria|lynx|macrophyllum|manacus|margaritifera|marmota|martes|mascarinus|mashuna|megacephala|melanodera|meles|melo|melolontha|melolontha|melongena|menidia|mephitis|mercenaria|meretrix|merluccius|meza|microstoma|milvus|milvus|mitella|mitra|mitu|modiolus|modulus|mola|molossus|molva|monachus|moniliformis|mops|mustelus|myaka|myospalax|myotis|myotis|naja|naja|nangra|nasua|natrix|neita|niviventer|notopterus|nycticorax|nycticorax|oenanthe|ogasawarana|oliva|ophioscincus|oplopomus|oreotragus|oriolus|pagrus|pangasius|papio|pauxi|perdix|periphylla|perna|petaurista|petronia|phocoena|phoenicurus|phoxinus|phycis|pica|pipa|pipile|pipistrellus|pipra|pithecia|planorbis|plica|poliocephalus|pollachius|pollicipes|porites|porphyrio|porphyrolaema|porpita|porzana|pristis|pseudobagarius|pudu|puffinus|pungitius|pyrrhocorax|pyrrhula|quadrula|quelea|rama|ranina|rapa|rasbora|rattus|redunca|regulus|remora|retropinna|rhinobatos|riparia|rita|rupicapra|rupicola|rutilus|saccolaimus|salamandra|sarda|scalpellum|scincus|scolytus|sephanoides|serinus|sodreana|solea|sphyraena|spinachia|spirorbis|spirula|sprattus|squatina|staphylaea|suiriri|sula|suta|synodus|tadorna|tandanus|tchagra|telescopium|temnurus|terebellum|tetradactylus|tetrax|therezopolis|thymallus|tibicen|tinca|todus|torpedo|trachurus|trachycorystes|trachyrinchus|tricornis|troglodytes|tropheops|tubifex|tyrannus|umbraculum|uncia|vanellus|vanellus|velella|velella|velutina|vicugna|villosa|vimba|viviparus|volva|vulpes|vulpes|xanthocephalus|xanthostigma|xenopirostris|ypiranga|zebrus|zera|zingel|zingha|zoma|zonia|zungaro|zygoneura|se");

  public PortugueseWordRepeatRule(ResourceBundle messages, Language language) {
    super(messages, language);
    super.setCategory(Categories.REPETITIONS.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Duplication);
    addExamplePair(Example.wrong("Este <marker>é é</marker> apenas uma frase de exemplo."),
                   Example.fixed("Este <marker>é</marker> apenas uma frase de exemplo."));
  }

  @Override
  public String getId() {
    return "PORTUGUESE_WORD_REPEAT_RULE";
  }

  @Override
  public boolean ignore(AnalyzedTokenReadings[] tokens, int position) {
    if (wordRepetitionOf("blá", tokens, position)) {
      return true;   // "blá blá"
    }
    if (wordRepetitionOf("se", tokens, position)) {
      return true;   // "se se"
    }
    if (wordRepetitionOf("Duran", tokens, position)) {
      return true;   // "Duran Duran"
    }
    if (wordRepetitionOf("sapiens", tokens, position)) {
      return true;   // "Homo sapiens sapiens"
    }
    if (wordRepetitionOf("tuk", tokens, position)) {
      return true;   // "tuk tuk"
    }
    if (isGenus(tokens[position - 1]) && isSpecies(tokens[position])) {
      return true;
    }
    return false;
  }

  private boolean wordRepetitionOf(String word, AnalyzedTokenReadings[] tokens, int position) {
    return position > 0 && tokens[position - 1].getToken().equals(word) && tokens[position].getToken().equals(word);
  }
  
  private boolean isGenus(AnalyzedTokenReadings token) {
    return TAUTONYMS_GENUS.matcher(token.getToken()).matches();
  }
  
  private boolean isSpecies(AnalyzedTokenReadings token) {
    return TAUTONYMS_SPECIES.matcher(token.getToken()).matches();
  }

}
