package de.danielnaber.languagetool.dev.tools;

/**
 * 
 * Helper class for romanian diacritics correction. Many romanian texts
 * (including Romanian wikipedia) contains wrong diacritics: <b>ş</b> instead of
 * <b>ș</b> and <b>ţ</b> instead of <b>ț</b>.
 * 
 * @author Ionuț Păduraru
 * @since 14.04.2009 12:27:24
 */
public final class RomanianDiacriticsModifier {

	private RomanianDiacriticsModifier() {
		// private constructor
	}
	private static final int REPLACEMENT_BUFF_SIZE = 10 * 1024;
	private static char[] cCorrectDiacritics = null;
	private static char[] replacementBuff = null;

	/**
	 * Initialize internal buffers
	 * 
	 * @author Ionuț Păduraru
	 * @since 14.04.2009 12:32:29
	 */
	private synchronized static void initCharMap() {
		if (cCorrectDiacritics == null) {
			replacementBuff = new char[REPLACEMENT_BUFF_SIZE];
			cCorrectDiacritics = new char[Character.MAX_VALUE
					- Character.MIN_VALUE];
			char c = Character.MIN_VALUE;
			for (int i = 0; i < Character.MAX_VALUE - Character.MIN_VALUE; i++) {
				final char newC = diac(c);
				cCorrectDiacritics[i] = newC;
				c++;
			}
		}
	}

	/**
	 * Single character correction. Used internally during buffers
	 * initialization
	 * 
	 * @author Ionuț Păduraru
	 * @since 14.04.2009 12:32:52
	 * @param c
	 * @return
	 */
	private static char diac(char c) {
		switch (c) {
		case 'ş':
			c = 'ș';
			break;
		case 'ţ':
			c = 'ț';
			break;
		case 'Ţ':
			c = 'Ț';
			break;
		case 'Ş':
			c = 'Ș';
			break;
		default:
			break;
		}
		return c;
	}

	/**
	 * Romanian diactitics correction: replace <b>ş</b> with <b>ș</b> and
	 * <b>ţ</b> with <b>ț</b>(including upper-case variants). <br/>
	 * Thread-safe method.
	 * 
	 * @author Ionuț Păduraru
	 * @since 14.04.2009 12:33:39
	 * @param s
	 */
	public static synchronized String correctDiacritrics(String s) {
		if (null == s)
			return null;
		initCharMap();
		final int length = s.length();
		// check buffer size
		if (length > replacementBuff.length) {
			replacementBuff = new char[length];
		}
		// get current chars
		s.getChars(0, length, replacementBuff, 0);
		// replace
		for (int i = 0; i < length; i++) {
			replacementBuff[i] = cCorrectDiacritics[replacementBuff[i]];

		}
		// return the corrected string
		return String.valueOf(replacementBuff, 0, length);
	}

}