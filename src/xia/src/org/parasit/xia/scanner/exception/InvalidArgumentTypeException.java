package org.parasit.xia.scanner.exception;

import java.lang.Throwable;

/**
 * Will be thrown if a argument type is not supported to its argument.
 * @version $Revision: 1.3 $
 */
public class InvalidArgumentTypeException extends InvalidArgumentException {

  /**
   * Will be thrown if a argument type is not supported to its argument.
   * @param lineNumber The number of the line
   * @param argType The type of the argument
   */
  public InvalidArgumentTypeException (int lineNumber,
                                        int arg_type) {
    super(lineNumber, "Argument type nr. "+arg_type+" is not supported.");
  }
}
