package de.danielnaber.languagetool.language;

/**
 * Constants for contributors who contribute to more than one language (use to avoid duplication).
 */
class Contributors {

    private Contributors() {
    }

    static final Contributor MARCIN_MILKOWSKI = new Contributor("Marcin Miłkowski");
    static {
        MARCIN_MILKOWSKI.setUrl("http://marcinmilkowski.pl");
    }

    static final Contributor DANIEL_NABER = new Contributor("Daniel Naber");
    static {
        DANIEL_NABER.setUrl("http://www.danielnaber.de");
    }

    static final Contributor DOMINIQUE_PELLE = new Contributor("Dominique Pellé");
    static {
        DOMINIQUE_PELLE.setUrl("http://dominiko.livejournal.com/tag/lingvoilo");
    }

}
