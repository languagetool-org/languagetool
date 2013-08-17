package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Greek;

public class GreekConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Greek();
	}

	@Override
	protected String createSampleText() {
		return "Δεν υπάρχει αυτή τη στιγμή λήμμα με αυτόν τον τίτλο.";
	}
}
