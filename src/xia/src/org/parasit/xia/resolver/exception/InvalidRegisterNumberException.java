package org.parasit.xia.resolver.exception;

import java.lang.Throwable;

/**
 * Will be thrown if a register number in invalid, for example the
 * number is out of range.
 * @version $Revision: 1.4 $
 */
public class InvalidRegisterNumberException extends Throwable {

  /**
   * Will be thrown if a register number in invalid, for example the
   * number is out of range.
   * @param register The name of the register
   * @param lineNumber The number of the line
   * @param number The number of the Register (similar to
   *        <code>register</code> but without prefix)
   * @param range_from The lowest number for a valid register
   * @param range_to The highest number for a valid register
   */
  public InvalidRegisterNumberException(String register,
                                        int lineNumber,
                                        int number,
                                        int range_from,
                                        int range_to) {
    super("Line "+lineNumber+": Invalid register number.\n  Register "+
          register+" with number "+number+" is out of valid range.\n"+
          "  Valid range: ["+range_from+" - "+range_to+"]\n\n");
  }
}
