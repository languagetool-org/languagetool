/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.el;

import org.languagetool.Language;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import java.util.*;

/**
 * @author Nikos-Antonopoulos 
 * A rule that checks whether a word that has
 * homonyms is used correctly. Greek implementation. Loads the relevant
 * words from <code>rules/el/replace.txt</code>.
 */
public class ReplaceHomonymsRule extends AbstractSimpleReplaceRule2 {

	private static final Locale EL_LOCALE = new Locale("EL");

	public ReplaceHomonymsRule(ResourceBundle messages, Language language) {
		super(messages, language);
	}

	@Override
	public List<String> getFileNames() {
		return Arrays.asList("/el/replace.txt");
	}

	@Override
	public final String getId() {
		return "GREEK_HOMONYMS_REPLACE";
	}

	@Override
	public String getDescription() {
		return "Έλεγχος για λανθασμένη χρήση ομόηχων λέξεων σε μια πρόταση";
	}

	@Override
	public String getShort() {
		return "Λανθασμένη χρήση της λέξης";
	}

	@Override
	public String getMessage() {
		return "Μήπως εννοούσατε $suggestions?";
	}

	@Override
	public String getSuggestionsSeparator() {
		return ", ";
	}

	@Override
	public boolean isCaseSensitive() {
		return false;
	}

	@Override
	public Locale getLocale() {
		return EL_LOCALE;
	}

}
