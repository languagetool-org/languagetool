package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Tagalog;

public class TagalogConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Tagalog();
	}

	@Override
	protected String createSampleText() {
		return "Mula sa Wikipediang Tagalog, ang malayang ensiklopedya";
	}

}
