package org.parasit.xia.compiler.exception;

import java.lang.RuntimeException;

/**
 * Will be thrown if a label tried to be set twice.
 * @version $Revision: 1.5 $
 */
public class LabelAlreadyRegisteredException
       extends RuntimeException
{
  /**
   * Will be thrown if a label tried to be set twice.
   * @param labelName The name of the label
   */
  public LabelAlreadyRegisteredException(String labelName) {
    super("Label "+labelName+" is already registered");
  }
}
