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
package org.languagetool.tokenizers.zh;


import com.hankcs.hanlp.seg.CRF.CRFSegment;
import org.languagetool.tokenizers.Tokenizer;

import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.seg.Viterbi.ViterbiSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * A word tokenizer of Simple Chinese
 * It can also be used for TC, but the result
 * is not as good as for SC.
 */
public class SChineseWordTokenizer implements Tokenizer {

    private Segment crfSegment = new CRFSegment().enableCustomDictionary(false).enableOrganizationRecognize(true).enableCustomDictionary(true);

    @Override
    public List<String> tokenize(String text) {
        List<Term> termList = crfSegment.seg(text);
        List<String> termStringList = new ArrayList<>();
        for (Term term: termList) {
            termStringList.add(term.toString());
        }
        return termStringList;
    }

    /** Return the *Term* type tokens */
    public List<Term> tokenizeBackup(String text) {
        return crfSegment.seg(text);
    }


}
