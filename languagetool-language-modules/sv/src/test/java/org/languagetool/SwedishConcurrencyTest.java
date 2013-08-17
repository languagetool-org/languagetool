package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Swedish;

public class SwedishConcurrencyTest extends AbstractLanguageConcurrencyTest {

	@Override
	protected Language createLanguage() {
		return new Swedish();
	}

	@Override
	protected String createSampleText() {
		return "Om sidan har raderats, se då i raderingsloggen eller se om det finns någon diskussion om att sidan ska raderas.";
	}

}
