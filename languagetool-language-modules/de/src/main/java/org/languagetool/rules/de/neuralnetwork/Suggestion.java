package org.languagetool.rules.de.neuralnetwork;

class Suggestion {

    private final String suggestion;

    private final boolean unsure;

    public Suggestion(String suggestion, boolean unsure) {
        this.suggestion = suggestion;
        this.unsure = unsure;
    }

    @Override
    public String toString() {
        if(unsure) {
            return suggestion + "*";
        } else {
            return suggestion;
        }
    }

    public boolean matches(String string) {
        return suggestion.equals(string);
    }

    public boolean isUnsure() {
        return unsure;
    }
}
