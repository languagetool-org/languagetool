package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.SimpleGerman;

public class SimpleGermanConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new SimpleGerman();
	}
	
	@Override
	protected String createSampleText() {
		return " \"Auch wenn Deine xleinen Füße die Erde nie berührten, sind deine Spuren trotzdem da überall.\"\n \n";
	}
}
