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
package de.danielnaber.languagetool.tagging.nl;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

public class DutchTaggerTest extends TestCase {
    
  private DutchTagger tagger;
  private WordTokenizer tokenizer;
      
  public void setUp() {
    tagger = new DutchTagger();
    tokenizer = new WordTokenizer();
  }

  public void testTagger() throws IOException {
    TestTools.myAssert("Dit is een Nederlandse zin om het programma'tje te testen.", "Dit/[dit]determiner(het,nwh,nmod,pro,nparg) is/[i]noun(both,pl,[])|is/[v_root(ben,zijn)]verb(sg_heeft) een/[een]determiner(een)|een/[een]fixed_part([een])|een/[een]pre_num_adv(pl_indef)|een/[één]pronoun(nwh,thi,sg,both,both,indef) Nederlandse/[Nederlands]adjective(e) zin/[zin]noun(de,sg,[])|zin/[zin]noun(de,sg,sbar)|zin/[zin]noun(de,sg,start_app_measure)|zin/[zin]noun(de,sg,van_sbar)|zin/[zin]noun(de,sg,vp)|zin/[v_root(zin,zinnen)]verb(sg1) om/[om]adjective(pred(nonadv))|om/[om]complementizer(om)|om/[om]particle(om)|om/[om]preposition(om,[heen]) het/[het]determiner(het,nwh,nmod,pro,nparg,wkpro) programma/[programma]noun(het,sg,[])|programma/[programma]noun(het,sg,app_measure) tje/[null]null te/[te]complementizer(te)|te/[te]intensifier|te/[te]me_intensifier|te/[te]preposition(te,[],nodet)|te/[te]vp_om_intensifier|te/[te]vp_om_me_intensifier testen/[test]noun(both,pl,[])|testen/[v_root(test,testen)]verb(inf)", tokenizer, tagger);        
    TestTools.myAssert("zwijnden","zwijnden/[v_root(zwijn,zwijnen)]verb(past(pl))", tokenizer, tagger);        
  }

}
