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

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatRule;

/**
 * Palavras que se repetem no Poruguês.
 * l18n from the english version, by Tiago F. Santos
 * @since 3.6
 */
public class PortugueseWordRepeatRule extends WordRepeatRule {

  public PortugueseWordRepeatRule(ResourceBundle messages, Language language) {
    super(messages, language);
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
    if (wordRepetitionOf("Duran", tokens, position)) {
      return true;   // "Duran Duran"
    }
    if (wordRepetitionOf("sapiens", tokens, position)) {
      return true;   // "Homo sapiens sapiens"
    }
    // Tautonym list from https://en.wikipedia.org/wiki/List_of_tautonyms
    if (wordRepetitionOf("aaptos", tokens, position)) {
      return true; 
    }  //aaptos
    if (wordRepetitionOf("acanthogyrus", tokens, position)) {
      return true;
    }  //acanthogyrus
    if (wordRepetitionOf("achatina", tokens, position)) {
      return true;
    }  //achatina
    if (wordRepetitionOf("agagus", tokens, position)) {
      return true;
    }  //agagus
    if (wordRepetitionOf("agama", tokens, position)) {
      return true;
    }  //agama
    if (wordRepetitionOf("alburnus", tokens, position)) {
      return true;
    }  //alburnus
    if (wordRepetitionOf("alces", tokens, position)) {
      return true;
    }  //alces
    if (wordRepetitionOf("alle", tokens, position)) {
      return true;
    }  //alle
    if (wordRepetitionOf("alosa", tokens, position)) {
      return true;
    }  //alosa
    if (wordRepetitionOf("amandava", tokens, position)) {
      return true;
    }  //amandava
    if (wordRepetitionOf("amazilia", tokens, position)) {
      return true;
    }  //amazilia
    if (wordRepetitionOf("ameiva", tokens, position)) {
      return true;
    }  //ameiva
    if (wordRepetitionOf("anableps", tokens, position)) {
      return true;
    }  //anableps
    if (wordRepetitionOf("anguilla", tokens, position)) {
      return true;
    }  //anguilla
    if (wordRepetitionOf("anguilla", tokens, position)) {
      return true;
    }  //anguilla
    if (wordRepetitionOf("anhinga", tokens, position)) {
      return true;
    }  //anhinga
    if (wordRepetitionOf("anostomus", tokens, position)) {
      return true;
    }  //anostomus
    if (wordRepetitionOf("anser", tokens, position)) {
      return true;
    }  //anser
    if (wordRepetitionOf("anthias", tokens, position)) {
      return true;
    }  //anthias
    if (wordRepetitionOf("apus", tokens, position)) {
      return true;
    }  //apus
    if (wordRepetitionOf("arcinella", tokens, position)) {
      return true;
    }  //arcinella
    if (wordRepetitionOf("ariadne", tokens, position)) {
      return true;
    }  //ariadne
    if (wordRepetitionOf("aspredo", tokens, position)) {
      return true;
    }  //aspredo
    if (wordRepetitionOf("astacus", tokens, position)) {
      return true;
    }  //astacus
    if (wordRepetitionOf("avicularia", tokens, position)) {
      return true;
    }  //avicularia
    if (wordRepetitionOf("axis", tokens, position)) {
      return true;
    }  //axis
    if (wordRepetitionOf("badis", tokens, position)) {
      return true;
    }  //badis
    if (wordRepetitionOf("bagarius", tokens, position)) {
      return true;
    }  //bagarius
    if (wordRepetitionOf("bagre", tokens, position)) {
      return true;
    }  //bagre
    if (wordRepetitionOf("balanus", tokens, position)) {
      return true;
    }  //balanus
    if (wordRepetitionOf("banjos", tokens, position)) {
      return true;
    }  //banjos
    if (wordRepetitionOf("barbatula", tokens, position)) {
      return true;
    }  //barbatula
    if (wordRepetitionOf("barbus", tokens, position)) {
      return true;
    }  //barbus
    if (wordRepetitionOf("basiliscus", tokens, position)) {
      return true;
    }  //basiliscus
    if (wordRepetitionOf("batasio", tokens, position)) {
      return true;
    }  //batasio
    if (wordRepetitionOf("belobranchus", tokens, position)) {
      return true;
    }  //belobranchus
    if (wordRepetitionOf("belone", tokens, position)) {
      return true;
    }  //belone
    if (wordRepetitionOf("belonimorphis", tokens, position)) {
      return true;
    }  //belonimorphis
    if (wordRepetitionOf("bidyanus", tokens, position)) {
      return true;
    }  //bidyanus
    if (wordRepetitionOf("bison", tokens, position)) {
      return true;
    }  //bison
    if (wordRepetitionOf("bombina", tokens, position)) {
      return true;
    }  //bombina
    if (wordRepetitionOf("boops", tokens, position)) {
      return true;
    }  //boops
    if (wordRepetitionOf("brama", tokens, position)) {
      return true;
    }  //brama
    if (wordRepetitionOf("brosme", tokens, position)) {
      return true;
    }  //brosme
    if (wordRepetitionOf("bubo", tokens, position)) {
      return true;
    }  //bubo
    if (wordRepetitionOf("bucayana", tokens, position)) {
      return true;
    }  //bucayana
    if (wordRepetitionOf("bufo", tokens, position)) {
      return true;
    }  //bufo
    if (wordRepetitionOf("buteo", tokens, position)) {
      return true;
    }  //buteo
    if (wordRepetitionOf("butis", tokens, position)) {
      return true;
    }  //butis
    if (wordRepetitionOf("calamus", tokens, position)) {
      return true;
    }  //calamus
    if (wordRepetitionOf("calappa", tokens, position)) {
      return true;
    }  //calappa
    if (wordRepetitionOf("caleta", tokens, position)) {
      return true;
    }  //caleta
    if (wordRepetitionOf("callichthys", tokens, position)) {
      return true;
    }  //callichthys
    if (wordRepetitionOf("calotes", tokens, position)) {
      return true;
    }  //calotes
    if (wordRepetitionOf("capoeta", tokens, position)) {
      return true;
    }  //capoeta
    if (wordRepetitionOf("capreolus", tokens, position)) {
      return true;
    }  //capreolus
    if (wordRepetitionOf("caracal", tokens, position)) {
      return true;
    }  //caracal
    if (wordRepetitionOf("carassius", tokens, position)) {
      return true;
    }  //carassius
    if (wordRepetitionOf("carassius", tokens, position)) {
      return true;
    }  //carassius
    if (wordRepetitionOf("cardinalis", tokens, position)) {
      return true;
    }  //cardinalis
    if (wordRepetitionOf("carduelis", tokens, position)) {
      return true;
    }  //carduelis
    if (wordRepetitionOf("caretta", tokens, position)) {
      return true;
    }  //caretta
    if (wordRepetitionOf("casuarius", tokens, position)) {
      return true;
    }  //casuarius
    if (wordRepetitionOf("catla", tokens, position)) {
      return true;
    }  //catla
    if (wordRepetitionOf("catostomus", tokens, position)) {
      return true;
    }  //catostomus
    if (wordRepetitionOf("cephea", tokens, position)) {
      return true;
    }  //cephea
    if (wordRepetitionOf("cerastes", tokens, position)) {
      return true;
    }  //cerastes
    if (wordRepetitionOf("chaca", tokens, position)) {
      return true;
    }  //chaca
    if (wordRepetitionOf("chalcides", tokens, position)) {
      return true;
    }  //chalcides
    if (wordRepetitionOf("chandramara", tokens, position)) {
      return true;
    }  //chandramara
    if (wordRepetitionOf("chanos", tokens, position)) {
      return true;
    }  //chanos
    if (wordRepetitionOf("chaos", tokens, position)) {
      return true;
    }  //chaos
    if (wordRepetitionOf("chinchilla", tokens, position)) {
      return true;
    }  //chinchilla
    if (wordRepetitionOf("chiropotes", tokens, position)) {
      return true;
    }  //chiropotes
    if (wordRepetitionOf("chitala", tokens, position)) {
      return true;
    }  //chitala
    if (wordRepetitionOf("chromis", tokens, position)) {
      return true;
    }  //chromis
    if (wordRepetitionOf("ciconia", tokens, position)) {
      return true;
    }  //ciconia
    if (wordRepetitionOf("cidaris", tokens, position)) {
      return true;
    }  //cidaris
    if (wordRepetitionOf("cinclus", tokens, position)) {
      return true;
    }  //cinclus
    if (wordRepetitionOf("citellus", tokens, position)) {
      return true;
    }  //citellus
    if (wordRepetitionOf("clelia", tokens, position)) {
      return true;
    }  //clelia
    if (wordRepetitionOf("coccothraustes", tokens, position)) {
      return true;
    }  //coccothraustes
    if (wordRepetitionOf("coccothraustes", tokens, position)) {
      return true;
    }  //coccothraustes
    if (wordRepetitionOf("cochlearius", tokens, position)) {
      return true;
    }  //cochlearius
    if (wordRepetitionOf("coeligena", tokens, position)) {
      return true;
    }  //coeligena
    if (wordRepetitionOf("colius", tokens, position)) {
      return true;
    }  //colius
    if (wordRepetitionOf("columella", tokens, position)) {
      return true;
    }  //columella
    if (wordRepetitionOf("concholepas", tokens, position)) {
      return true;
    }  //concholepas
    if (wordRepetitionOf("concholepas", tokens, position)) {
      return true;
    }  //concholepas
    if (wordRepetitionOf("conger", tokens, position)) {
      return true;
    }  //conger
    if (wordRepetitionOf("conta", tokens, position)) {
      return true;
    }  //conta
    if (wordRepetitionOf("convoluta", tokens, position)) {
      return true;
    }  //convoluta
    if (wordRepetitionOf("cordylus", tokens, position)) {
      return true;
    }  //cordylus
    if (wordRepetitionOf("coscoroba", tokens, position)) {
      return true;
    }  //coscoroba
    if (wordRepetitionOf("cossus", tokens, position)) {
      return true;
    }  //cossus
    if (wordRepetitionOf("cotinga", tokens, position)) {
      return true;
    }  //cotinga
    if (wordRepetitionOf("coturnix", tokens, position)) {
      return true;
    }  //coturnix
    if (wordRepetitionOf("crangon", tokens, position)) {
      return true;
    }  //crangon
    if (wordRepetitionOf("cressida", tokens, position)) {
      return true;
    }  //cressida,
    if (wordRepetitionOf("crex", tokens, position)) {
      return true;
    }  //crex
    if (wordRepetitionOf("cricetus", tokens, position)) {
      return true;
    }  //cricetus
    if (wordRepetitionOf("crocuta", tokens, position)) {
      return true;
    }  //crocuta
    if (wordRepetitionOf("crossoptilon", tokens, position)) {
      return true;
    }  //crossoptilon
    if (wordRepetitionOf("curaeus", tokens, position)) {
      return true;
    }  //curaeus
    if (wordRepetitionOf("cyanicterus", tokens, position)) {
      return true;
    }  //cyanicterus
    if (wordRepetitionOf("cygnus", tokens, position)) {
      return true;
    }  //cygnus
    if (wordRepetitionOf("cymbium", tokens, position)) {
      return true;
    }  //cymbium
    if (wordRepetitionOf("cynoglossus", tokens, position)) {
      return true;
    }  //cynoglossus
    if (wordRepetitionOf("dama", tokens, position)) {
      return true;
    }  //dama
    if (wordRepetitionOf("dario", tokens, position)) {
      return true;
    }  //dario
    if (wordRepetitionOf("dentex", tokens, position)) {
      return true;
    }  //dentex
    if (wordRepetitionOf("devario", tokens, position)) {
      return true;
    }  //devario
    if (wordRepetitionOf("diuca", tokens, position)) {
      return true;
    }  //diuca
    if (wordRepetitionOf("dives", tokens, position)) {
      return true;
    }  //dives
    if (wordRepetitionOf("dolabrifera", tokens, position)) {
      return true;
    }  //dolabrifera
    if (wordRepetitionOf("enhydris", tokens, position)) {
      return true;
    }  //enhydris
    if (wordRepetitionOf("ensifera", tokens, position)) {
      return true;
    }  //ensifera
    if (wordRepetitionOf("ensifera", tokens, position)) {
      return true;
    }  //ensifera
    if (wordRepetitionOf("ensis", tokens, position)) {
      return true;
    }  //ensis
    if (wordRepetitionOf("erythrinus", tokens, position)) {
      return true;
    }  //erythrinus
    if (wordRepetitionOf("extra", tokens, position)) {
      return true;
    }  //extra
    if (wordRepetitionOf("falcipennis", tokens, position)) {
      return true;
    }  //falcipennis
    if (wordRepetitionOf("feroculus", tokens, position)) {
      return true;
    }  //feroculus
    if (wordRepetitionOf("ficus", tokens, position)) {
      return true;
    }  //ficus
    if (wordRepetitionOf("fragum", tokens, position)) {
      return true;
    }  //fragum
    if (wordRepetitionOf("francolinus", tokens, position)) {
      return true;
    }  //francolinus
    if (wordRepetitionOf("furcula", tokens, position)) {
      return true;
    }  //furcula
    if (wordRepetitionOf("gagata", tokens, position)) {
      return true;
    }  //gagata
    if (wordRepetitionOf("galbula", tokens, position)) {
      return true;
    }  //galbula
    if (wordRepetitionOf("gallinago", tokens, position)) {
      return true;
    }  //gallinago
    if (wordRepetitionOf("gallus", tokens, position)) {
      return true;
    }  //gallus
    if (wordRepetitionOf("gazella", tokens, position)) {
      return true;
    }  //gazella
    if (wordRepetitionOf("gazella", tokens, position)) {
      return true;
    }  //gazella
    if (wordRepetitionOf("gemma", tokens, position)) {
      return true;
    }  //gemma
    if (wordRepetitionOf("genetta", tokens, position)) {
      return true;
    }  //genetta
    if (wordRepetitionOf("gerbillus", tokens, position)) {
      return true;
    }  //gerbillus
    if (wordRepetitionOf("gibberulus", tokens, position)) {
      return true;
    }  //gibberulus
    if (wordRepetitionOf("giraffa", tokens, position)) {
      return true;
    }  //giraffa
    if (wordRepetitionOf("glis", tokens, position)) {
      return true;
    }  //glis
    if (wordRepetitionOf("glycimeris", tokens, position)) {
      return true;
    }  //glycimeris
    if (wordRepetitionOf("glyphis", tokens, position)) {
      return true;
    }  //glyphis
    if (wordRepetitionOf("gobio", tokens, position)) {
      return true;
    }  //gobio
    if (wordRepetitionOf("goliathus", tokens, position)) {
      return true;
    }  //goliathus
    if (wordRepetitionOf("gonorynchus", tokens, position)) {
      return true;
    }  //gonorynchus
    if (wordRepetitionOf("gorilla", tokens, position)) {
      return true;
    }  //gorilla
    if (wordRepetitionOf("grapsus", tokens, position)) {
      return true;
    }  //grapsus
    if (wordRepetitionOf("grapsus", tokens, position)) {
      return true;
    }  //grapsus
    if (wordRepetitionOf("grus", tokens, position)) {
      return true;
    }  //grus
    if (wordRepetitionOf("gryllotalpa", tokens, position)) {
      return true;
    }  //gryllotalpa
    if (wordRepetitionOf("guira", tokens, position)) {
      return true;
    }  //guira
    if (wordRepetitionOf("gulo", tokens, position)) {
      return true;
    }  //gulo
    if (wordRepetitionOf("hara", tokens, position)) {
      return true;
    }  //hara
    if (wordRepetitionOf("harpa", tokens, position)) {
      return true;
    }  //harpa
    if (wordRepetitionOf("haustellum", tokens, position)) {
      return true;
    }  //haustellum
    if (wordRepetitionOf("hemilepidotus", tokens, position)) {
      return true;
    }  //hemilepidotus
    if (wordRepetitionOf("heterophyes", tokens, position)) {
      return true;
    }  //heterophyes
    if (wordRepetitionOf("himantopus", tokens, position)) {
      return true;
    }  //himantopus
    if (wordRepetitionOf("himantopus", tokens, position)) {
      return true;
    }  //himantopus
    if (wordRepetitionOf("hippocampus", tokens, position)) {
      return true;
    }  //hippocampus
    if (wordRepetitionOf("hippoglossus", tokens, position)) {
      return true;
    }  //hippoglossus
    if (wordRepetitionOf("hippopus", tokens, position)) {
      return true;
    }  //hippopus
    if (wordRepetitionOf("histrio", tokens, position)) {
      return true;
    }  //histrio
    if (wordRepetitionOf("histrionicus", tokens, position)) {
      return true;
    }  //histrionicus
    if (wordRepetitionOf("hoolock", tokens, position)) {
      return true;
    }  //hoolock
    if (wordRepetitionOf("hucho", tokens, position)) {
      return true;
    }  //hucho
    if (wordRepetitionOf("huso", tokens, position)) {
      return true;
    }  //huso
    if (wordRepetitionOf("hyaena", tokens, position)) {
      return true;
    }  //hyaena
    if (wordRepetitionOf("hypnale", tokens, position)) {
      return true;
    }  //hypnale
    if (wordRepetitionOf("ichthyaetus", tokens, position)) {
      return true;
    }  //ichthyaetus
    if (wordRepetitionOf("icterus", tokens, position)) {
      return true;
    }  //icterus
    if (wordRepetitionOf("idea", tokens, position)) {
      return true;
    }  //idea
    if (wordRepetitionOf("iguana", tokens, position)) {
      return true;
    }  //iguana
    if (wordRepetitionOf("indicator", tokens, position)) {
      return true;
    }  //indicator
    if (wordRepetitionOf("indri", tokens, position)) {
      return true;
    }  //indri
    if (wordRepetitionOf("indri", tokens, position)) {
      return true;
    }  //indri
    if (wordRepetitionOf("jacana", tokens, position)) {
      return true;
    }  //jacana
    if (wordRepetitionOf("jaculus", tokens, position)) {
      return true;
    }  //jaculus
    if (wordRepetitionOf("janthina", tokens, position)) {
      return true;
    }  //janthina
    if (wordRepetitionOf("kachuga", tokens, position)) {
      return true;
    }  //kachuga
    if (wordRepetitionOf("koilofera", tokens, position)) {
      return true;
    }  //koilofera
    if (wordRepetitionOf("lactarius", tokens, position)) {
      return true;
    }  //lactarius
    if (wordRepetitionOf("lagocephalus", tokens, position)) {
      return true;
    }  //lagocephalus
    if (wordRepetitionOf("lagopus", tokens, position)) {
      return true;
    }  //lagopus
    if (wordRepetitionOf("lagopus", tokens, position)) {
      return true;
    }  //lagopus
    if (wordRepetitionOf("lagurus", tokens, position)) {
      return true;
    }  //lagurus
    if (wordRepetitionOf("lambis", tokens, position)) {
      return true;
    }  //lambis
    if (wordRepetitionOf("lemmus", tokens, position)) {
      return true;
    }  //lemmus
    if (wordRepetitionOf("lepadogaster", tokens, position)) {
      return true;
    }  //lepadogaster
    if (wordRepetitionOf("lerwa", tokens, position)) {
      return true;
    }  //lerwa
    if (wordRepetitionOf("leuciscus", tokens, position)) {
      return true;
    }  //leuciscus
    if (wordRepetitionOf("lima", tokens, position)) {
      return true;
    }  //lima
    if (wordRepetitionOf("limanda", tokens, position)) {
      return true;
    }  //limanda
    if (wordRepetitionOf("limanda", tokens, position)) {
      return true;
    }  //limanda
    if (wordRepetitionOf("limosa", tokens, position)) {
      return true;
    }  //limosa
    if (wordRepetitionOf("liparis", tokens, position)) {
      return true;
    }  //liparis
    if (wordRepetitionOf("lithognathus", tokens, position)) {
      return true;
    }  //lithognathus
    if (wordRepetitionOf("lithophaga", tokens, position)) {
      return true;
    }  //lithophaga
    if (wordRepetitionOf("loa", tokens, position)) {
      return true;
    }  //loa
    if (wordRepetitionOf("lota", tokens, position)) {
      return true;
    }  //lota
    if (wordRepetitionOf("luscinia", tokens, position)) {
      return true;
    }  //luscinia
    if (wordRepetitionOf("lutjanus", tokens, position)) {
      return true;
    }  //lutjanus
    if (wordRepetitionOf("lutra", tokens, position)) {
      return true;
    }  //lutra
    if (wordRepetitionOf("lutraria", tokens, position)) {
      return true;
    }  //lutraria
    if (wordRepetitionOf("lynx", tokens, position)) {
      return true;
    }  //lynx
    if (wordRepetitionOf("macrophyllum", tokens, position)) {
      return true;
    }  //macrophyllum
    if (wordRepetitionOf("manacus", tokens, position)) {
      return true;
    }  //manacus
    if (wordRepetitionOf("margaritifera", tokens, position)) {
      return true;
    }  //margaritifera
    if (wordRepetitionOf("marmota", tokens, position)) {
      return true;
    }  //marmota
    if (wordRepetitionOf("martes", tokens, position)) {
      return true;
    }  //martes
    if (wordRepetitionOf("mascarinus", tokens, position)) {
      return true;
    }  //mascarinus
    if (wordRepetitionOf("mashuna", tokens, position)) {
      return true;
    }  //mashuna
    if (wordRepetitionOf("megacephala", tokens, position)) {
      return true;
    }  //megacephala
    if (wordRepetitionOf("melanodera", tokens, position)) {
      return true;
    }  //melanodera
    if (wordRepetitionOf("meles", tokens, position)) {
      return true;
    }  //meles
    if (wordRepetitionOf("melo", tokens, position)) {
      return true;
    }  //melo
    if (wordRepetitionOf("melolontha", tokens, position)) {
      return true;
    }  //melolontha
    if (wordRepetitionOf("melolontha", tokens, position)) {
      return true;
    }  //melolontha
    if (wordRepetitionOf("melongena", tokens, position)) {
      return true;
    }  //melongena
    if (wordRepetitionOf("menidia", tokens, position)) {
      return true;
    }  //menidia
    if (wordRepetitionOf("mephitis", tokens, position)) {
      return true;
    }  //mephitis
    if (wordRepetitionOf("mercenaria", tokens, position)) {
      return true;
    }  //mercenaria
    if (wordRepetitionOf("meretrix", tokens, position)) {
      return true;
    }  //meretrix
    if (wordRepetitionOf("merluccius", tokens, position)) {
      return true;
    }  //merluccius
    if (wordRepetitionOf("meza", tokens, position)) {
      return true;
    }  //meza
    if (wordRepetitionOf("microstoma", tokens, position)) {
      return true;
    }  //microstoma
    if (wordRepetitionOf("milvus", tokens, position)) {
      return true;
    }  //milvus
    if (wordRepetitionOf("milvus", tokens, position)) {
      return true;
    }  //milvus
    if (wordRepetitionOf("mitella", tokens, position)) {
      return true;
    }  //mitella
    if (wordRepetitionOf("mitra", tokens, position)) {
      return true;
    }  //mitra
    if (wordRepetitionOf("mitu", tokens, position)) {
      return true;
    }  //mitu
    if (wordRepetitionOf("modiolus", tokens, position)) {
      return true;
    }  //modiolus
    if (wordRepetitionOf("modulus", tokens, position)) {
      return true;
    }  //modulus
    if (wordRepetitionOf("mola", tokens, position)) {
      return true;
    }  //mola
    if (wordRepetitionOf("molossus", tokens, position)) {
      return true;
    }  //molossus
    if (wordRepetitionOf("molva", tokens, position)) {
      return true;
    }  //molva
    if (wordRepetitionOf("monachus", tokens, position)) {
      return true;
    }  //monachus
    if (wordRepetitionOf("moniliformis", tokens, position)) {
      return true;
    }  //moniliformis
    if (wordRepetitionOf("mops", tokens, position)) {
      return true;
    }  //mops
    if (wordRepetitionOf("mustelus", tokens, position)) {
      return true;
    }  //mustelus
    if (wordRepetitionOf("myaka", tokens, position)) {
      return true;
    }  //myaka
    if (wordRepetitionOf("myospalax", tokens, position)) {
      return true;
    }  //myospalax
    if (wordRepetitionOf("myotis", tokens, position)) {
      return true;
    }  //myotis
    if (wordRepetitionOf("myotis", tokens, position)) {
      return true;
    }  //myotis
    if (wordRepetitionOf("naja", tokens, position)) {
      return true;
    }  //naja
    if (wordRepetitionOf("naja", tokens, position)) {
      return true;
    }  //naja
    if (wordRepetitionOf("nangra", tokens, position)) {
      return true;
    }  //nangra
    if (wordRepetitionOf("nasua", tokens, position)) {
      return true;
    }  //nasua
    if (wordRepetitionOf("natrix", tokens, position)) {
      return true;
    }  //natrix
    if (wordRepetitionOf("neita", tokens, position)) {
      return true;
    }  //neita
    if (wordRepetitionOf("niviventer", tokens, position)) {
      return true;
    }  //niviventer
    if (wordRepetitionOf("notopterus", tokens, position)) {
      return true;
    }  //notopterus
    if (wordRepetitionOf("nycticorax", tokens, position)) {
      return true;
    }  //nycticorax
    if (wordRepetitionOf("nycticorax", tokens, position)) {
      return true;
    }  //nycticorax
    if (wordRepetitionOf("oenanthe", tokens, position)) {
      return true;
    }  //oenanthe
    if (wordRepetitionOf("ogasawarana", tokens, position)) {
      return true;
    }  //ogasawarana
    if (wordRepetitionOf("oliva", tokens, position)) {
      return true;
    }  //oliva
    if (wordRepetitionOf("ophioscincus", tokens, position)) {
      return true;
    }  //ophioscincus
    if (wordRepetitionOf("oplopomus", tokens, position)) {
      return true;
    }  //oplopomus
    if (wordRepetitionOf("oreotragus", tokens, position)) {
      return true;
    }  //oreotragus
    if (wordRepetitionOf("oriolus", tokens, position)) {
      return true;
    }  //oriolus
    if (wordRepetitionOf("pagrus", tokens, position)) {
      return true;
    }  //pagrus
    if (wordRepetitionOf("pangasius", tokens, position)) {
      return true;
    }  //pangasius
    if (wordRepetitionOf("papio", tokens, position)) {
      return true;
    }  //papio
    if (wordRepetitionOf("pauxi", tokens, position)) {
      return true;
    }  //pauxi
    if (wordRepetitionOf("perdix", tokens, position)) {
      return true;
    }  //perdix
    if (wordRepetitionOf("periphylla", tokens, position)) {
      return true;
    }  //periphylla
    if (wordRepetitionOf("perna", tokens, position)) {
      return true;
    }  //perna
    if (wordRepetitionOf("petaurista", tokens, position)) {
      return true;
    }  //petaurista
    if (wordRepetitionOf("petronia", tokens, position)) {
      return true;
    }  //petronia
    if (wordRepetitionOf("phocoena", tokens, position)) {
      return true;
    }  //phocoena
    if (wordRepetitionOf("phoenicurus", tokens, position)) {
      return true;
    }  //phoenicurus
    if (wordRepetitionOf("phoxinus", tokens, position)) {
      return true;
    }  //phoxinus
    if (wordRepetitionOf("phycis", tokens, position)) {
      return true;
    }  //phycis
    if (wordRepetitionOf("pica", tokens, position)) {
      return true;
    }  //pica
    if (wordRepetitionOf("pipa", tokens, position)) {
      return true;
    }  //pipa
    if (wordRepetitionOf("pipile", tokens, position)) {
      return true;
    }  //pipile
    if (wordRepetitionOf("pipistrellus", tokens, position)) {
      return true;
    }  //pipistrellus
    if (wordRepetitionOf("pipra", tokens, position)) {
      return true;
    }  //pipra
    if (wordRepetitionOf("pithecia", tokens, position)) {
      return true;
    }  //pithecia
    if (wordRepetitionOf("planorbis", tokens, position)) {
      return true;
    }  //planorbis
    if (wordRepetitionOf("plica", tokens, position)) {
      return true;
    }  //plica
    if (wordRepetitionOf("poliocephalus", tokens, position)) {
      return true;
    }  //poliocephalus
    if (wordRepetitionOf("pollachius", tokens, position)) {
      return true;
    }  //pollachius
    if (wordRepetitionOf("pollicipes", tokens, position)) {
      return true;
    }  //pollicipes
    if (wordRepetitionOf("porites", tokens, position)) {
      return true;
    }  //porites
    if (wordRepetitionOf("porphyrio", tokens, position)) {
      return true;
    }  //porphyrio
    if (wordRepetitionOf("porphyrolaema", tokens, position)) {
      return true;
    }  //porphyrolaema
    if (wordRepetitionOf("porpita", tokens, position)) {
      return true;
    }  //porpita
    if (wordRepetitionOf("porzana", tokens, position)) {
      return true;
    }  //porzana
    if (wordRepetitionOf("pristis", tokens, position)) {
      return true;
    }  //pristis
    if (wordRepetitionOf("pseudobagarius", tokens, position)) {
      return true;
    }  //pseudobagarius
    if (wordRepetitionOf("pudu", tokens, position)) {
      return true;
    }  //pudu
    if (wordRepetitionOf("puffinus", tokens, position)) {
      return true;
    }  //puffinus
    if (wordRepetitionOf("pungitius", tokens, position)) {
      return true;
    }  //pungitius
    if (wordRepetitionOf("pyrrhocorax", tokens, position)) {
      return true;
    }  //pyrrhocorax
    if (wordRepetitionOf("pyrrhula", tokens, position)) {
      return true;
    }  //pyrrhula
    if (wordRepetitionOf("quadrula", tokens, position)) {
      return true;
    }  //quadrula
    if (wordRepetitionOf("quelea", tokens, position)) {
      return true;
    }  //quelea
    if (wordRepetitionOf("rama", tokens, position)) {
      return true;
    }  //rama
    if (wordRepetitionOf("ranina", tokens, position)) {
      return true;
    }  //ranina
    if (wordRepetitionOf("rapa", tokens, position)) {
      return true;
    }  //rapa
    if (wordRepetitionOf("rasbora", tokens, position)) {
      return true;
    }  //rasbora
    if (wordRepetitionOf("rattus", tokens, position)) {
      return true;
    }  //rattus
    if (wordRepetitionOf("redunca", tokens, position)) {
      return true;
    }  //redunca
    if (wordRepetitionOf("regulus", tokens, position)) {
      return true;
    }  //regulus
    if (wordRepetitionOf("remora", tokens, position)) {
      return true;
    }  //remora
    if (wordRepetitionOf("retropinna", tokens, position)) {
      return true;
    }  //retropinna
    if (wordRepetitionOf("rhinobatos", tokens, position)) {
      return true;
    }  //rhinobatos
    if (wordRepetitionOf("riparia", tokens, position)) {
      return true;
    }  //riparia
    if (wordRepetitionOf("rita", tokens, position)) {
      return true;
    }  //rita
    if (wordRepetitionOf("rupicapra", tokens, position)) {
      return true;
    }  //rupicapra
    if (wordRepetitionOf("rupicola", tokens, position)) {
      return true;
    }  //rupicola
    if (wordRepetitionOf("rutilus", tokens, position)) {
      return true;
    }  //rutilus
    if (wordRepetitionOf("saccolaimus", tokens, position)) {
      return true;
    }  //saccolaimus
    if (wordRepetitionOf("salamandra", tokens, position)) {
      return true;
    }  //salamandra
    if (wordRepetitionOf("sarda", tokens, position)) {
      return true;
    }  //sarda
    if (wordRepetitionOf("scalpellum", tokens, position)) {
      return true;
    }  //scalpellum
    if (wordRepetitionOf("scincus", tokens, position)) {
      return true;
    }  //scincus
    if (wordRepetitionOf("scolytus", tokens, position)) {
      return true;
    }  //scolytus
    if (wordRepetitionOf("sephanoides", tokens, position)) {
      return true;
    }  //sephanoides
    if (wordRepetitionOf("serinus", tokens, position)) {
      return true;
    }  //serinus
    if (wordRepetitionOf("sodreana", tokens, position)) {
      return true;
    }  //sodreana
    if (wordRepetitionOf("solea", tokens, position)) {
      return true;
    }  //solea
    if (wordRepetitionOf("sphyraena", tokens, position)) {
      return true;
    }  //sphyraena
    if (wordRepetitionOf("spinachia", tokens, position)) {
      return true;
    }  //spinachia
    if (wordRepetitionOf("spirorbis", tokens, position)) {
      return true;
    }  //spirorbis
    if (wordRepetitionOf("spirula", tokens, position)) {
      return true;
    }  //spirula
    if (wordRepetitionOf("sprattus", tokens, position)) {
      return true;
    }  //sprattus
    if (wordRepetitionOf("squatina", tokens, position)) {
      return true;
    }  //squatina
    if (wordRepetitionOf("staphylaea", tokens, position)) {
      return true;
    }  //staphylaea
    if (wordRepetitionOf("suiriri", tokens, position)) {
      return true;
    }  //suiriri
    if (wordRepetitionOf("sula", tokens, position)) {
      return true;
    }  //sula
    if (wordRepetitionOf("suta", tokens, position)) {
      return true;
    }  //suta
    if (wordRepetitionOf("synodus", tokens, position)) {
      return true;
    }  //synodus
    if (wordRepetitionOf("tadorna", tokens, position)) {
      return true;
    }  //tadorna
    if (wordRepetitionOf("tandanus", tokens, position)) {
      return true;
    }  //tandanus
    if (wordRepetitionOf("tchagra", tokens, position)) {
      return true;
    }  //tchagra
    if (wordRepetitionOf("telescopium", tokens, position)) {
      return true;
    }  //telescopium
    if (wordRepetitionOf("temnurus", tokens, position)) {
      return true;
    }  //temnurus
    if (wordRepetitionOf("terebellum", tokens, position)) {
      return true;
    }  //terebellum
    if (wordRepetitionOf("tetradactylus", tokens, position)) {
      return true;
    }  //tetradactylus
    if (wordRepetitionOf("tetrax", tokens, position)) {
      return true;
    }  //tetrax
    if (wordRepetitionOf("therezopolis", tokens, position)) {
      return true;
    }  //therezopolis
    if (wordRepetitionOf("thymallus", tokens, position)) {
      return true;
    }  //thymallus
    if (wordRepetitionOf("tibicen", tokens, position)) {
      return true;
    }  //tibicen
    if (wordRepetitionOf("tinca", tokens, position)) {
      return true;
    }  //tinca
    if (wordRepetitionOf("todus", tokens, position)) {
      return true;
    }  //todus
    if (wordRepetitionOf("torpedo", tokens, position)) {
      return true;
    }  //torpedo
    if (wordRepetitionOf("trachurus", tokens, position)) {
      return true;
    }  //trachurus
    if (wordRepetitionOf("trachycorystes", tokens, position)) {
      return true;
    }  //trachycorystes
    if (wordRepetitionOf("trachyrinchus", tokens, position)) {
      return true;
    }  //trachyrinchus
    if (wordRepetitionOf("tricornis", tokens, position)) {
      return true;
    }  //tricornis
    if (wordRepetitionOf("troglodytes", tokens, position)) {
      return true;
    }  //troglodytes
    if (wordRepetitionOf("tropheops", tokens, position)) {
      return true;
    }  //tropheops
    if (wordRepetitionOf("tubifex", tokens, position)) {
      return true;
    }  //tubifex
    if (wordRepetitionOf("tyrannus", tokens, position)) {
      return true;
    }  //tyrannus
    if (wordRepetitionOf("umbraculum", tokens, position)) {
      return true;
    }  //umbraculum
    if (wordRepetitionOf("uncia", tokens, position)) {
      return true;
    }  //uncia
    if (wordRepetitionOf("vanellus", tokens, position)) {
      return true;
    }  //vanellus
    if (wordRepetitionOf("vanellus", tokens, position)) {
      return true;
    }  //vanellus
    if (wordRepetitionOf("velella", tokens, position)) {
      return true;
    }  //velella
    if (wordRepetitionOf("velella", tokens, position)) {
      return true;
    }  //velella
    if (wordRepetitionOf("velutina", tokens, position)) {
      return true;
    }  //velutina
    if (wordRepetitionOf("vicugna", tokens, position)) {
      return true;
    }  //vicugna
    if (wordRepetitionOf("villosa", tokens, position)) {
      return true;
    }  //villosa
    if (wordRepetitionOf("vimba", tokens, position)) {
      return true;
    }  //vimba
    if (wordRepetitionOf("viviparus", tokens, position)) {
      return true;
    }  //viviparus
    if (wordRepetitionOf("volva", tokens, position)) {
      return true;
    }  //volva
    if (wordRepetitionOf("vulpes", tokens, position)) {
      return true;
    }  //vulpes
    if (wordRepetitionOf("vulpes", tokens, position)) {
      return true;
    }  //vulpes
    if (wordRepetitionOf("xanthocephalus", tokens, position)) {
      return true;
    }  //xanthocephalus
    if (wordRepetitionOf("xanthostigma", tokens, position)) {
      return true;
    }  //xanthostigma
    if (wordRepetitionOf("xenopirostris", tokens, position)) {
      return true;
    }  //xenopirostris
    if (wordRepetitionOf("ypiranga", tokens, position)) {
      return true;
    }  //ypiranga
    if (wordRepetitionOf("zebrus", tokens, position)) {
      return true;
    }  //zebrus
    if (wordRepetitionOf("zera", tokens, position)) {
      return true;
    }  //zera
    if (wordRepetitionOf("zingel", tokens, position)) {
      return true;
    }  //zingel
    if (wordRepetitionOf("zingha", tokens, position)) {
      return true;
    }  //zingha
    if (wordRepetitionOf("zoma", tokens, position)) {
      return true;
    }  //zoma
    if (wordRepetitionOf("zonia", tokens, position)) {
      return true;
    }  //zonia
    if (wordRepetitionOf("zungaro", tokens, position)) {
      return true;
    }  //zungaro
    if (wordRepetitionOf("zygoneura", tokens, position)) {
      return true;
    }  //zygoneura
    return false;
  }

  private boolean posIsIn(AnalyzedTokenReadings[] tokens, int position, String... posTags) {
    if (position >= 0 && position < tokens.length) {
      for (String posTag : posTags) {
        if (tokens[position].hasPartialPosTag(posTag)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean wordRepetitionOf(String word, AnalyzedTokenReadings[] tokens, int position) {
    return position > 0 && tokens[position - 1].getToken().equals(word) && tokens[position].getToken().equals(word);
  }

}
