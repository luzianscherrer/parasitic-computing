package org.parasit.xia.scanner.exception;

import java.lang.Throwable;

/**
 * Will bethrown if an opcode is invalid.
 * @version $Revision: 1.3 $
 */
public class InvalidOpcodeException extends GeneralScannerException {


  /**
   * Will bethrown if an opcode is invalid.
   * @param lineNumber The number of the line
   * @param opcode The invalid opcode
   */
  public InvalidOpcodeException(int lineNumber, String opcode) {
    super(lineNumber, "Invalid Opcode: "+opcode);
  }
}
