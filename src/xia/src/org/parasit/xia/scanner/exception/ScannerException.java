package org.parasit.xia.scanner.exception;

import java.lang.Throwable;
import java.lang.StringBuffer;
import java.util.Enumeration;

import org.parasit.xia.global.ErrorVector;

/**
 * Represents an exception with all its errors from scanning a Xia file.
 * @version $Revision: 1.3 $
 */
public class ScannerException extends Throwable {

  /** A vector containing all errors/exceptions */
  private ErrorVector m_errVec;

  /**
   * Constructor to build an exception containing
   * all its errors from scanning a Xia file.
   * @param A vector with containing all errors/exceptions.
   */
  public ScannerException(ErrorVector errVec) {
    super("Error by scanning Xia source...\n"+
    "---------------------------------------"+
    "---------------------------------------\n");
    this.m_errVec = errVec;
  }

  /**
   * Returns a string representation of all errors/exceptions.
   * @return a string representation of all errors/exceptions.
   */
  public String getMessage() {
    return super.getMessage()+this.getErrorMessages();
  }

  /**
   * Converts a vector containing all errors/exceptions to a
   * representable string.
   * @return a string representation of all errors/exceptions stored in a
   *         vector.
   */
  private String getErrorMessages() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("Encountered "+m_errVec.size()+" error"+
                  (m_errVec.size()>1?"s":"")+" which "+
                  (m_errVec.size()>1?"are":"is")+" described in the "+
                  "following list:\n\n");

    for(Enumeration enum = m_errVec.elements(); enum.hasMoreElements();) {
      strBuf.append(((GeneralScannerException)enum.nextElement()).getMessage());
    }

    return strBuf.toString();
  }
}
