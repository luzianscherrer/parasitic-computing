package org.parasit.xia.scanner.exception;

import java.lang.Throwable;

/**
 * Will be thrown if a length of a label is invalid.
 * @version $Revision: 1.3 $
 */
public class InvalidLabelLengthException extends InvalidArgumentException {

  /**
   * Will be thrown if a length of a label is invalid.
   * @param lineNumber The number of the line
   * @param comment The label as a string representation
   * @param position The position of the erroneous character
   */
  public InvalidLabelLengthException (int lineNumber,
                                      String comment,
                                      int position) {
    super(lineNumber, "Illegal label length"+
          showPosition(comment, position));
  }
}
