package org.parasit.xia.scanner.exception;

import java.lang.Throwable;

/**
 * Will be thrown if a wrong number of token follow an opcode.
 * @version $Revision: 1.3 $
 */
public class InvalidTokenCountException extends GeneralScannerException {

  /**
   * Will be thrown if a wrong number of token follow an opcode.
   * @param lineNumber The number of the line
   * @param tokenFound The number of found tokens
   * @param lineNumber The number of maximally allowed tokens
   */
  public InvalidTokenCountException(int lineNumber,
                                    int tokenFound,
                                    int maxTokens) {
    super(lineNumber, "Invalid number of Tokens.\n"+
          GeneralScannerException.IDENT_SPACES+"  Found "+tokenFound+
          " tokens, maximum allowed "+maxTokens+".");
  }
}
