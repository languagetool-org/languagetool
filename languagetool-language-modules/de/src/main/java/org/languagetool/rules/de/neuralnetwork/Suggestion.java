package org.languagetool.rules.de.neuralnetwork;

class Suggestion {

    private final String suggestion;

    private final boolean unsure;

    Suggestion(String suggestion, boolean unsure) {
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

    boolean matches(String string) {
        return suggestion.equals(string);
    }

    boolean isUnsure() {
        return unsure;
    }
}
