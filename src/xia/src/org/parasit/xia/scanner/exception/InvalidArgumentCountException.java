package org.parasit.xia.scanner.exception;

import java.lang.Throwable;

/**
 * Will be thrown if a wrong number of arguments
 * is found for corresponding Opcode.
 * @version $Revision: 1.3 $
 */
public class InvalidArgumentCountException extends GeneralScannerException {

  /**
   * Will be thrown if a wrong number of arguments
   * is found for corresponding Opcode.
   * @param lineNumber The number of the line
   * @param opcode The opcode
   * @param expected The number of expected arguments
   * @param found The number of erroneous found arguments
   */
   public InvalidArgumentCountException(int lineNumber,
                                            String opcode,
                                            int expected,
                                            int found) {
     super(lineNumber, "Invalid number of Arguments."+
           "\n"+GeneralScannerException.IDENT_SPACES+
           "Number of arguments allowed for Opcode "+
           opcode+": "+expected+" Found: "+found);
  }
}
