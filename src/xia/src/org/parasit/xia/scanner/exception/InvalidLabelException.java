package org.parasit.xia.scanner.exception;

import java.lang.Throwable;

/**
 * Will be thrown if a label contains an invalid character.
 * @version $Revision: 1.3 $
 */
public class InvalidLabelException extends InvalidArgumentException {

  /**
   * Will be thrown if a label contains an invalid character.
   * @param lineNumber The number of the line
   * @param comment The label as a string representation
   * @param position The position of the erroneous character
   */
  public InvalidLabelException (int lineNumber,
                                  String comment,
                                  int position) {
    super(lineNumber, "Illegal character in label"+
          showPosition(comment, position));
  }
}
