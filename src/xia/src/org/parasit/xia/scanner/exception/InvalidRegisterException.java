package org.parasit.xia.scanner.exception;

import java.lang.Throwable;

/**
 * Will be thrown if an argument contains an invalid
 * register (syntactically) or register prefix.
 * @version $Revision: 1.3 $
 */
public class InvalidRegisterException extends InvalidArgumentException {

  /**
   * Will be thrown if an argument contains an invalid
   * register (syntactically) or register prefix.
   * @param lineNumber The line number
   * @param argNr The argument number
   * @param arg The Argument
   * @param position The position of the erroneous character
   */
  public InvalidRegisterException (int lineNumber,
                                        int argNr,
                                        String arg,
                                        int position) {
    super(lineNumber, "Invalid Argument. Argument nr. "+argNr+
                      " is not a valid register or register prefix"+
                      showPosition(arg, position));
  }
}
