package org.languagetool.commandline;

/**
 * Thrown when an unknown command-line parameter is specified.
 */
public class UnknownParameterException extends RuntimeException {

  public UnknownParameterException(String message) {
    super(message);
  }

}
