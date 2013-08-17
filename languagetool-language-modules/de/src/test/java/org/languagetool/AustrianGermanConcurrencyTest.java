package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.AustrianGerman;

public class AustrianGermanConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new AustrianGerman();
	}
	
	@Override
	protected String createSampleText() {
		return " \"Auch wenn Deine xleinen Füße die Erde nie berührten, sind deine Spuren trotzdem da überall.\"\n \n";
	}
}
