/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.rules.LongSentenceRule;

public class ArabicLongSentenceRule extends LongSentenceRule {

	private static final Pattern NON_WORD_REGEX = Pattern.compile("[،؟؛.?!:;,~’'\"„“»«‚‘›‹()\\[\\]-]");

	public ArabicLongSentenceRule(ResourceBundle messages, int maxSentenceLength) {
		super(messages, maxSentenceLength, NON_WORD_REGEX);
	}

	@Override
	public final String getId() {
		return "ARABIC_TOO_LONG_SENTENCE";
	}

}
