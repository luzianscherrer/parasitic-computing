package org.parasit.xia.scanner.exception;

import java.lang.RuntimeException;
/**
 * Will be thrown if an Argument is used but not yet validated/initialized.
 * @version $Revision: 1.3 $
 */
public class ArgumentTypeNotSetException
       extends RuntimeException {

  /**
   * Will be thrown if an Argument is used but not yet validated/initialized.
   * @param value the string representation of the argument.
   */
  public ArgumentTypeNotSetException(String value) {
    super("Argument "+value +" has not been initialized.");
  }
}
