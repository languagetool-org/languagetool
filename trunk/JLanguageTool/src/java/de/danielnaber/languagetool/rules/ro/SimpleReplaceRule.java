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
package de.danielnaber.languagetool.rules.ro;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.rules.AbstractSimpleReplaceRule;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 * 
 * Romanian implementations. Loads the list of words from
 * <code>rules/ro/replace.txt</code>.
 * 
 * @author Ionuț Păduraru
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule {

	public static final String ROMANIAN_SIMPLE_REPLACE_RULE = "RO_SIMPLE_REPLACE";
	
	private static final String FILE_NAME = "/rules/ro/replace.txt";
	// locale used on case-conversion
	private static Locale roLocale = new Locale("ro");

	public final String getFileName() {
		return FILE_NAME;
	}

	public SimpleReplaceRule(final ResourceBundle messages) throws IOException {
		super(messages);
	}

	public final String getId() {
		return ROMANIAN_SIMPLE_REPLACE_RULE;
	}

	public String getDescription() {
		// TODO: this is a very common rule type; maybe it wold be better to localize {@link AbstractSimpleReplaceRule#getDescription()}
		return "Cuvinte sau grupuri de cuvinte incorecte";
	}
	
	/**
	 * use case-insensitive matching.
	 */
	public boolean isCaseSensitive() {
		return false;
	}

	/**
	 * locale used on case-conversion
	 */
	public Locale getLocale() {
		return roLocale;
	}

}
