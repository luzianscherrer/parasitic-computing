package org.parasit.xia.scanner.exception;

import java.lang.Throwable;

/**
 * Will bethrown if pointer has an erroneous prefix.
 * @version $Revision: 1.3 $
 */
public class InvalidPointerException
    extends InvalidArgumentException {

  /**
   * Will bethrown if pointer has an erroneous prefix.
   * @param lineNumber The number of the line
   * @param argNr The number of the argument
   * @param arg The corresponding argument
   * @param position The position of the erroneous character
   */
  public InvalidPointerException (int lineNumber,
                                  int argNr,
                                  String arg,
                                  int position) {
    super(lineNumber, "Invalid Argument. Argument nr. "+argNr+
                      " has not a valid pointer prefix: "+arg);
  }
}
