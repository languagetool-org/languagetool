package org.languagetool.rules.uk;

class RuleException {
  public enum Type { none, exception, skip }

  public final Type type;
  public final int skip;

  public RuleException(Type type) {
    this.type = type;
    this.skip = 0;
    if( type == Type.exception ) {
      TokenAgreementPrepNounExceptionHelper.logException();
    }
  }
  public RuleException(int skip) {
    this.type = Type.skip;
    this.skip = skip;
  }
}