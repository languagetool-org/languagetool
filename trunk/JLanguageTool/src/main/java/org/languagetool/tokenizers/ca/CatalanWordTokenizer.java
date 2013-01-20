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
package org.languagetool.tokenizers.ca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.languagetool.tagging.ca.CatalanTagger;

import org.languagetool.tokenizers.Tokenizer;


/**
 * Tokenizes a sentence into words. Punctuation and whitespace gets its own token.
 * Special treatment for hyphens and apostrophes in Catalan.
 *
 * @author Jaume Ortolà 
 */
public class CatalanWordTokenizer implements Tokenizer {

	//all possible forms of "pronoms febles" after a verb.
	private static final String PF = "('en|'hi|'ho|'l|'ls|'m|'n|'ns|'s|'t|-el|-els|-em|-en|-ens|-hi|-ho|-l|-la|-les|-li|-lo|-los|-m|-me|-n|-ne|-nos|-s|-se|-t|-te|-us|-vos)";

    private int maxPatterns = 11;
    private Pattern[] patterns = new Pattern[maxPatterns];
    
    private CatalanTagger tagger;

	public CatalanWordTokenizer() {
		
		tagger = new CatalanTagger();

        // Apostrophe at the beginning of a word. Ex.: l'home, s'estima, n'omple, hivern, etc.
        // It creates 2 tokens: <token>l'</token><token>home</token>
        patterns[0] = Pattern.compile("^([lnmtsd]')([^'\\-]*)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

        // Exceptions to (Match verb+1 pronom feble)
        // It creates 1 token: <token>qui-sap-lo</token>
        patterns[1] = Pattern.compile("^(qui-sap-lo|qui-sap-la|qui-sap-los|qui-sap-les)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

        // Match verb+3 pronoms febles (rare but possible!). Ex: Emporta-te'ls-hi.
        // It creates 4 tokens: <token>Emporta</token><token>-te</token><token>'ls</token><token>-hi</token>
        patterns[2] = Pattern.compile("^([lnmtsd]')(.{2,})"+PF+PF+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
        patterns[3] = Pattern.compile("^(.{2,})"+PF+PF+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

        // Match verb+2 pronoms febles. Ex: Emporta-te'ls. 
        // It creates 3 tokens: <token>Emporta</token><token>-te</token><token>'ls</token>
        patterns[4] = Pattern.compile("^([lnmtsd]')(.{2,})"+PF+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
        patterns[5] = Pattern.compile("^(.{2,})"+PF+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

        // match verb+1 pronom feble. Ex: Emporta't, vés-hi, porta'm.
        // It creates 2 tokens: <token>Emporta</token><token>'t</token>
        patterns[6] = Pattern.compile("^([lnmtsd]')(.{2,})"+PF+"$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
        patterns[7] = Pattern.compile("^(.+[^cbfhjkovwyzCBFHJKOVWYZ])"+PF+"$",Pattern.UNICODE_CASE);

        // d'emportar
        patterns[8] = Pattern.compile("^([lnmtsd]')(.*)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

        //contractions: al, als, pel, pels, del, dels, cal (!), cals (!) 
        patterns[9] = Pattern.compile("^(a|de|pe)(ls?)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

        //contraction: can
        patterns[10] = Pattern.compile("^(ca)(n)$",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);

	}

	/**
	 * @param text Text to tokenize
	 * @return List of tokens.
	 *         Note: a special string ##CA_APOS## is used to replace apostrophes,
	 *         and ##CA_HYPHEN## to replace hyphens.
	 */
	@Override
	public List<String> tokenize(final String text) {
		final List<String> l = new ArrayList<String>();
		final StringTokenizer st = new StringTokenizer(
				text.replaceAll("([\\p{L}])['’]([\\p{L}])", "$1##CA_APOS##$2")
						// Cases: d'1 km, és l'1 de gener, és d'1.4 kg
						.replaceAll("([dlDL])['’](1[\\s\\.,])", "$1##CA_APOS##$2")
				         //it's necessary for words like "vint-i-quatre"
						.replaceAll("([\\p{L}])-([\\p{L}])-([\\p{L}])", "$1##CA_HYPHEN##$2##CA_HYPHEN##$3") 
						.replaceAll("([\\p{L}])-([\\p{L}\\d])", "$1##CA_HYPHEN##$2")
						.replaceAll("([\\d])\\.([\\d])", "$1##CA_DECIMALPOINT##$2")
						.replaceAll("([\\d]),([\\d])","$1##CA_DECIMALCOMMA##$2")
						.replaceAll("([\\d]) ([\\d])","$1##CA_SPACE##$2")
						// allows correcting typographical errors in "ela geminada"
						.replaceAll("l\\.l", "##ELA_GEMINADA##"), 
				"\u0020\u00A0\u115f\u1160\u1680"
						+ "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
						+ "\u2008\u2009\u200A\u200B\u200c\u200d\u200e\u200f"
						+ "\u2013\u2014\u2015"
						+ "\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f"
						+ "\u205F\u2060\u2061\u2062\u2063\u206A\u206b\u206c\u206d"
						+ "\u206E\u206F\u3000\u3164\ufeff\uffa0\ufff9\ufffa\ufffb"
						+ ",.;()[]{}<>!?:/\\\"'«»„”“‘’`´…¿¡\t\n\r-", true);
		String s;
		String groupStr;

		while (st.hasMoreElements()) {
			s = st.nextToken()
					.replaceAll("##CA_APOS##", "'")
					.replaceAll("##CA_HYPHEN##", "-")
					.replaceAll("##CA_DECIMALPOINT##", ".")
					.replaceAll("##CA_DECIMALCOMMA##", ",")
					.replaceAll("##CA_SPACE##", " ")
					.replaceAll("##ELA_GEMINADA##", "l.l");
			Matcher matcher = null;
			boolean matchFound = false;
			int j = 0;
			while (j < maxPatterns && !matchFound) {
				matcher = patterns[j].matcher(s);
				matchFound = matcher.find();
				j++;
			}
			if (matchFound) {
				for (int i = 1; i <= matcher.groupCount(); i++) {
					groupStr = matcher.group(i);
					l.addAll(wordsToAdd(groupStr));
				}
			} else 
				l.addAll(wordsToAdd(s));
		}
		return l;
	}
	
	/* Splits a word containing hyphen(-) it it doesn't exist in the dictionary*/
	private List<String> wordsToAdd(String s) {
		final List<String> l = new ArrayList<String>();
		if (!s.contains("-"))
			l.add(s);
		else {
			try {
				// words containing hyphen (-) are looked up in the dictionary
				if (tagger.existsWord(s))
					l.add(s);
				else {
					// if not found, the word is split
					final StringTokenizer st2 = new StringTokenizer(s, "-",	true);
					while (st2.hasMoreElements()) 
						l.add(st2.nextToken());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return l;		
	}
}
