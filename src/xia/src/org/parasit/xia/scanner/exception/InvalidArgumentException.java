package org.parasit.xia.scanner.exception;

import java.lang.Throwable;

/**
 * Will be thrown if a argument is generally invalid or there
 * are no further reasons.
 * @version $Revision: 1.3 $
 */
public class InvalidArgumentException extends GeneralScannerException {

  /**
   * Will be thrown if a argument is generally invalid or there
   * are no further reasons.
   * @param lineNumber The number of the line
   * @param message The error/exception message represented as a string
   */
  public InvalidArgumentException(int lineNumber, String message) {
    super(lineNumber, message);
  }
}
