package org.parasit.xia.resolver.exception;

import java.lang.Throwable;
import java.lang.StringBuffer;
import java.util.Enumeration;

import org.parasit.xia.global.ErrorVector;

/**
 * Will be thrown if errors occured by resolving virtual registers into
 * absolut 4ia register.
 * @version $Revision: 1.3 $
 */
public class VirtualRegisterResolverException extends Throwable {

  /** Contains all exceptions that occured
   * during resolving the virtual registers
   */
  private ErrorVector m_errVec;

  /**
   * Will be thrown if errors occured by resolving virtual registers into
   * absolut 4ia register.
   */
  public VirtualRegisterResolverException(ErrorVector errVec) {
    super("Error by mapping virtual to 4ia registers...\n"+
    "---------------------------------------"+
    "---------------------------------------\n");
    this.m_errVec = errVec;
  }

  /**
   * Returns a string representation of all errors that occured.
   * @ return a string representation of all errors that occured.
   */
  public String getMessage() {
    return super.getMessage()+this.getErrorMessages();
  }

  /**
   * Converts the vector containing all errors to a string.
   * @ return a string containing all errors.
   */
  private String getErrorMessages() {
    StringBuffer strBuf = new StringBuffer();
    for(Enumeration enum = m_errVec.elements(); enum.hasMoreElements();) {
      strBuf.append(((InvalidRegisterNumberException)enum.nextElement()).getMessage());
    }
    return strBuf.toString();
  }
}
