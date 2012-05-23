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
package org.languagetool.synthesis.el;

import org.languagetool.AnalyzedToken;
import org.languagetool.JLanguageTool;
import org.languagetool.synthesis.BaseSynthesizer;

import java.io.IOException;

/**
 *
 * @author Panagiotis Minos <pminos@gmail.com>
 */
public class GreekSynthesizer extends BaseSynthesizer {

    private static final String RESOURCE_FILENAME = "/el/el_synth.dict";
    private static final String TAGS_FILE_NAME = "/el/el_tags.txt";

    public GreekSynthesizer() {
        super(JLanguageTool.getDataBroker().getResourceDir() + RESOURCE_FILENAME,
                JLanguageTool.getDataBroker().getResourceDir() + TAGS_FILE_NAME);
    }

    @Override
    public String[] synthesize(final AnalyzedToken token, final String posTag) throws IOException {
        //System.out.println(token + "\t" + posTag);
        for (String s : super.synthesize(token, posTag)) {
            //System.out.println(s);
        }
        return super.synthesize(token, posTag);
    }

    @Override
    public String[] synthesize(final AnalyzedToken token, final String posTag,
            final boolean posTagRegExp) throws IOException {
        //System.out.println(token + "\t" + posTag + "\t" + posTagRegExp);
        for (String s : super.synthesize(token, posTag, posTagRegExp)) {
            //System.out.println(s);
        }
        return super.synthesize(token, posTag, posTagRegExp);
    }
}
