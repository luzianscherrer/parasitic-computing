package org.parasit.xia.compiler;

import org.parasit.xia.*;

/**
 * Handles the line numbers used in 4ia code and formats well them.
 * @version $Revision: 1.3 $
 */
public class LineNumber {
  /** The line number counter */
  private int m_lineNumber;

  /**
   * This Constructor will reset the line number with 0
   */
  public LineNumber() {
    this.m_lineNumber = 0;
  }

  /**
   * Increments the internal line number counter and returns the
   * next line number well formatted as a string representation.
   * @return the next line number well formatted as a string representation.
   */
  public String getNextLineNr() {
    if(m_lineNumber<10)        return "000"+(m_lineNumber++);
    else if(m_lineNumber<100)  return "00"+(m_lineNumber++);
    else if(m_lineNumber<1000) return "0"+(m_lineNumber++);
    else                       return ""+(m_lineNumber++);
  }

  /**
   * Returns the current line number.
   * @return the current line number.
   */
  public int getCurrentLineNr() {
    return m_lineNumber;
  }
}
