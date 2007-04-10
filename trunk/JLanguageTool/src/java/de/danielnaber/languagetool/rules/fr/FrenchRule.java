/**
 * 
 */
package de.danielnaber.languagetool.rules.fr;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;

/**
 * Abstract base class for French rules.
 * 
 * @author Marcin Milkowski
 *
 */
public abstract class FrenchRule extends Rule {
  
  public final Language[] getLanguages() {
        return new Language[] { Language.FRENCH};
      }

}
