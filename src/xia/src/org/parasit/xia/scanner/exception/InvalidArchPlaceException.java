package org.parasit.xia.scanner.exception;

import java.lang.Throwable;
/**
 * Will be thrown if the place or the number of occurences of the Tag
 * <code>ARCH</code> is invalid.
 * @version $Revision: 1.3 $
 */
public class InvalidArchPlaceException extends GeneralScannerException {

  /**
   * Will be thrown if the place or the number of occurences of the Tag
   * <code>ARCH</code> is invalid.
   * @param lineNumber The number of the line
   * @param opcode The opcode
   */
  public InvalidArchPlaceException(int lineNumber, String opcode) {
    super(lineNumber, "Invalid place for "+opcode+": This instruction "+
          "has to be placed\n"+"        at the beginning of the source "+
          "code, before any other instruction,\n"+"        and may only "+
          "be used 0 or 1 time");
  }
}
