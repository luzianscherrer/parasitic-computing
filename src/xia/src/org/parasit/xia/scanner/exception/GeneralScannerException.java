package org.parasit.xia.scanner.exception;

import java.lang.Throwable;

/**
 * A global exception who will be extended by several specific scanner
 * exceptions. This class contains a package private method
 * <code>showPosition</code> who will show the error character.
 * @version $Revision: 1.4 $
 */
public class GeneralScannerException extends Throwable {
  /**
   *The number of spaces all following error lines have to be incremented.
   */
  public static final String IDENT_SPACES = "         ";

  /**
   * A global exception who will be extended by several specific scanner
   * exceptions.
   * @param lineNumber The line number
   * @param message The error message.
   */
  public GeneralScannerException(int lineNumber, String message) {
    super("line "+lineNumber+": "+message+"\n");
  }

  /**
   * Shows the position/character witch produced the error.
   * @param argument The erroneous argument with its string
   * @param nr The position of th erroneous character
   * @return a string with the erroneous character and a pointer to it.
   */
  protected static String showPosition(String argument, int nr) {
    String ret = ":\n"+IDENT_SPACES+"Argument:      "+argument+
                  "\n"+IDENT_SPACES+"Scanner Error: ";
    for(int i=0;i<nr;++i) {
      ret += " ";
    }
    return ret+"^";
  }
}
