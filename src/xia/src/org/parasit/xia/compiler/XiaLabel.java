package org.parasit.xia.compiler;

import org.parasit.xia.*;

/**
 * Represents a label used Xia wide.
 * @version $Revision: 1.3 $
 */
public class XiaLabel
{
  /** The name of the label */
  private String m_labelName;
  /** The line number corresponding to this label */
  private Integer m_lineNumber;

  /**
   * Constructor who sets the label name and the line number.*
   * @param labelName  The name of the label.
   * @param lineNumber The line number corresponding to this label
   */
  public XiaLabel(String labelName, int lineNumber)
  {
    this.m_labelName  = labelName;
    this.m_lineNumber = new Integer(lineNumber);
  }

  /**
   * Sets the name of this XiaLabel
   * @param labelName The name of the label
   */
  public void setName(String labelName) {
    this.m_labelName = labelName;
  }

  /**
   * Returns the name of this XiaLabel.
   * @return the name of this XiaLabel.
   */
  public String getName() {
    return m_labelName;
  }

  /**
   * Sets the corresponding line number of this XiaLabel.
   * @param lineNumber the corresponding line number of this XiaLabel.
   */
  public void setLineNumber(int lineNumber) {
    this.m_lineNumber = new Integer(lineNumber);
  }

  /**
   * Returns the line number of this XiaLabel.
   * @return the line number of this XiaLabel.
   */
  public Integer getLineNumber() {
    return m_lineNumber;
  }

  /**
   * Returns <code>true</code> if the label from Xia is already assigned
   * to a line number from 4ia, <code>false</code> if not.
   */
  public boolean isAssigned() {
    return m_lineNumber != null;
  }
}
