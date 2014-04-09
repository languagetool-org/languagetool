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
package org.languagetool.tagging.en;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.languagetool.JLanguageTool;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.TokenPoS;
import org.languagetool.tagging.TokenPoSBuilder;

/**
 * English Part-of-speech tagger.
 * The POS tagset is described in
 * <a href="https://github.com/languagetool-org/languagetool/blob/master/languagetool-language-modules/en/src/main/resources/org/languagetool/resource/en/tagset.txt">tagset.txt</a>
 * 
 * @author Marcin Milkowski
 */
public class EnglishTagger extends BaseTagger {

  @Override
  public final String getFileName() {
    return "/en/english.dict";    
  }
  
  public EnglishTagger() {
    super();
    setLocale(Locale.ENGLISH);
  }

  @Override
  public List<TokenPoS> resolvePOSTag(String posTag) {
    if (posTag == null) {
      return Collections.emptyList();
    }
    switch (posTag) {
      case JLanguageTool.SENTENCE_START_TAGNAME:
        return l(pos(JLanguageTool.SENTENCE_START_TAGNAME));
      case JLanguageTool.SENTENCE_END_TAGNAME:
        return l(pos(JLanguageTool.SENTENCE_END_TAGNAME));
      
      case "#":
      case "$":
      case ",":
      case ".":
      case ":":
      case "''":
      case "``":
        return l(pos("symbol"));
      
      case "CC":
        return l(pos("conjunction"));  // coordinating
      case "CD":
        return l(pos("number"));
      case "DT":
        return l(pos("determiner"));
      case "EX":
        return l(pos("existential_there"));  // TODO: ???
      case "FW":
        return l(pos("conjunction"));
      case "IN":
        return l(pos("conjunction"));   // subordinating
      
      case "JJ":
        return l(pos("adjective").add("degree", "positive"));
      case "JJR":
        return l(pos("adjective").add("degree", "comparative"));
      case "JJS":
        return l(pos("adjective").add("degree", "superlative"));
      
      case "MD":
        return l(pos("verb").add("type", "modal"));  //TODO: is it 'type'?
      
      case "NN":
        return l(pos("noun").add("number", "singular"));
      case "NN:U":
        return l(pos("noun").add("number", "mass"));
      case "NN:UN":
        return l(pos("noun").add("number", "used_as_mass"));  //???
      case "NNP":
        return l(pos("noun").add("type", "proper").add("number", "singular"));
      case "NNPS":
        return l(pos("noun").add("type", "proper").add("number", "plural"));
      case "NNS":
        return l(pos("noun").add("number", "plural"));
      
      case "PDT":
        return l(pos("determiner").add("type", "pre"));
      case "POS":
        return l(pos("??"));  // TODO: possessive marker
      case "PRP":
        return l(pos("pronoun").add("typ", "personal"));
      case "WP$":
      case "PRP$":
        return l(pos("pronoun").add("typ", "possessive"));
      case "RB":
        return l(pos("adverb").add("degree", "positive"));
      case "RBR":
        return l(pos("adverb").add("degree", "comparative"));
      case "RBS":
        return l(pos("adverb").add("degree", "superlative"));
      case "RP":
        return l(pos("particle"));
      case "TO":
        return l(pos("to"));  // TODO: ???
      case "UH":
        return l(pos("interjection"));
      
      case "VB":
        return l(pos("verb").add("tense", "baseform"));  // TODO: it's not exactly 'tense'
      case "VBD":
        return l(pos("verb").add("tense", "simple_past"));
      case "VBG":
        
        return l(pos("verb").add("tense", "gerund"));  // TODO: it's not exactly 'tense'
      case "VBN":
        return l(pos("verb").add("tense", "past_participle"));
      case "VBP":
        return l(
                pos("verb").add("tense", "present").add("person", "1").add("number", "singular"),
                pos("verb").add("tense", "present").add("person", "2").add("number", "singular"),
                pos("verb").add("tense", "present").add("person", "1").add("number", "plural"),
                pos("verb").add("tense", "present").add("person", "2").add("number", "plural"),
                pos("verb").add("tense", "present").add("person", "3").add("number", "plural")
                );
      case "VBZ":
        return l(pos("verb").add("tense", "present").add("person", "3").add("number", "singular"));
      
      case "WDT":
        return l(pos("determiner").add("type", "wh"));  // TODO: ???
      case "WP":
        return l(pos("pronoun").add("type", "wh"));  // TODO: ???
      case "WRB":
        return l(pos("adverb").add("type", "wh"));  // TODO: ???

      // tags introduced in disambiguation.xml:
      case "SYM":
        return l(pos("unknown"));
      
      default:
        throw new RuntimeException("posTag '" + posTag + "' not yet handled");
    }
  }
  
  private TokenPoSBuilder pos(String posType) {
    return new TokenPoSBuilder().add("pos", posType);
  }

  private List<TokenPoS> l(TokenPoSBuilder... builders) {
    List<TokenPoS> result = new ArrayList<>();
    for (TokenPoSBuilder builder : builders) {
      result.add(builder.create());
    }
    return result;
  }
}
