package org.parasit.xia.scanner.exception;

import java.lang.RuntimeException;

/**
 * Will bethrown if a line is used but not yet validated.
 * @version $Revision: 1.3 $
 */
public class LineNotYetValidatedException
       extends RuntimeException {

  /**
   * Will bethrown if a line is used but not yet validated.
   * @param lineNr The number of the line
   */
  public LineNotYetValidatedException(int lineNr) {
    super("Line "+lineNr+" is not validated.");
  }
}
