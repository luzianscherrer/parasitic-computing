package org.parasit.xia.scanner.exception;

import java.lang.Throwable;

/**
 * Will be thrown if a constant contains an invalid character.
 * @version $Revision: 1.3 $
 */
public class InvalidConstantException extends InvalidArgumentException {

  /**
   * Will be thrown if a constant contains an invalid character.
   * @param lineNumber The number of the line
   * @param argNr The number of the erroneuos argument
   * @param arg The corresponding argument
   * @param position The position of the erroneous character
   */
  public InvalidConstantException (int lineNumber,
                                        int argNr,
                                        String arg,
                                        int position) {
    super(lineNumber, "Argument "+argNr+" is not a valid constant"+
          showPosition(arg, position));
  }
}
