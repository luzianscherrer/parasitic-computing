package org.parasit.xia.scanner.exception;

import java.lang.Throwable;

/**
 * Will be thrown if a text contains an illegal character.
 * @version $Revision: 1.3 $
 */
public class InvalidTextException extends InvalidArgumentException {

  /**
   * Will be thrown if a text contains an illegal character.
   * @param lineNumber The number of the line
   * @param comment The comment/text
   * @param position The position of the erroneous character
   */
  public InvalidTextException (int lineNumber,
                                  String comment,
                                  int position) {
    super(lineNumber, "Illegal character in Comment"+
          showPosition(comment, position));
  }
}
