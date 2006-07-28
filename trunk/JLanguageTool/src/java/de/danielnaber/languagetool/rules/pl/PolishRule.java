/**
 * 
 */
package de.danielnaber.languagetool.rules.pl;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;

/**
 * Abstract base class for Polish rules.
 * 
 * @author Marcin Milkowski
 *
 */
public abstract class PolishRule extends Rule {
	
  public final Language[] getLanguages() {
		    return new Language[] { Language.POLISH};
		  }

}
