package org.parasit.xia.compiler.exception;

import java.lang.RuntimeException;

/**
 * Will be thrown if an argument isn't of type Register or Pointer
 * @version $Revision: 1.3 $
 */
public class NoExplicitRegisterNumberException
       extends RuntimeException
{
  /**
   * Will be thrown if an argument isn't of type Register or Pointer
   */
  public NoExplicitRegisterNumberException(String value) {
    super("Register "+value+" does not have an explicit register number");
  }
}
