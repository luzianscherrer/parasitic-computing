package org.parasit.xia.scanner;

import org.parasit.xia.*;
import org.parasit.xia.scanner.exception.*;

/**
 * Represents a line generated by the scanner containing the opcode, its
 * arguments, constants or its text and the line number.
 * @version $Revision: 1.3 $
 */
public class Line implements LineConstants{
  /** The line number this line is representing */
  private int m_lineNumber;
  /** The validation state. */
  private int m_validationState;
  /** The opcode of this line */
  private String m_opcode;
  /** An array containing all arguments, constant or text */
  private Argument m_arg[];

  /**
   * This constructor instantiates a new line and sets the line number,
   * the opcode and its arguments. Furthermore, the validation state
   * will be set <code>NOT_VALIDATED</code>.
   * @param lineNumber The number of this line
   * @param opcode The opcode as a string representation (case sensitive)
   * @param arg The arguments the corresponding opcode has
   */
  public Line(int lineNumber, String opcode, Argument[] arg) {
    this.m_validationState = NOT_VALIDATED;
    this.m_lineNumber = lineNumber;
    setOpcode(opcode);
    this.m_arg = arg;
  }

  /////////////////////////////////////////////////////////// getters/setters
  /**
   * Setter method to set the opcode.
   * @param opcode The opcode to be set.
   */
  private void setOpcode(String opcode) {
    this.m_opcode = opcode;
  }
  /**
   * Getter method to get out the opcode as a string representation
   * out of this line.
   * @return opcode The opcode as a string representation
   */
  public String getOpcode() {
    this.checkIfValid();
    return this.m_opcode;
  }

  /**
   * Setter method to set an argument. The number of arguments can't
   * be changed after calling the constructor. Means the a possible
   * <code>OutOfBoundsException</code> may be thrown here.
   * @param i The number of the argument to replace. This number may be
   *        one of <code><b>[1..arguments]</b></code>
   * @param arg The argument to be set
   */
  private void setArg(int i, Argument arg) {
    this.m_arg[--i] = arg;
  }
  /**
   * Setter method to reset all arguments. Already setted
   * argument will be lost.
   * @param arg An array of arguments
   */
  public void setArgs(Argument[] args) {
    this.m_arg = args;
  }
  /**
   * Getter method to get out an argument out of this line. If this line
   * isn't in a valid state, an <code>LineNotYetValidatedException</code>
   * will be thrown. If no argument is set, <code>null</code> will
   * be returned.
   * @param i The number of the argument to return. This number may be
   *        one of <code><b>[1..arguments]</b></code>
   * @return the argument corresponding to its entered value.
   * @see org.parasit.xia.scanner.exception.LineNotYetValidatedException
   */
  public Argument getArg(int i) {
    this.checkIfValid();
    if(this.m_arg == null) {
      return null;
    }
    return this.m_arg[--i];
  }
  /**
   * Returns the number of arguments this line counts.
   * @return the number of arguments this line counts.
   */
  public int getArgCount() {
    if(m_arg == null) {
      return 0;
    }
    return this.m_arg.length;
  }

  /**
   * Returns the number of this line. If this line
   * isn't in a valid state, an <code>LineNotYetValidatedException</code>
   * will be thrown.
   * @return the number of this line.
   * @see org.parasit.xia.scanner.exception.LineNotYetValidatedException
   */
  public int getLineNumber() {
    this.checkIfValid();
    return this.m_lineNumber;
  }

  /**
   * Validates this line using the <code>LineValidator</code>. If an
   * error will be found by validating this line, a
   * <code>GeneralScannerException</code> will be thrown and the
   * validation state will be set <code>NOT_VALIDATED</code>. Otherwise
   * if this line is successfully validated, the
   * validation state will be set <code>VALID</code> and this line will
   * be returned.
   * @return This line, validated by the LineValidator.
   * @see org.parasit.xia.scanner.LineValidator#validate(Line)
   * @see org.parasit.xia.scanner.exception.GeneralScannerException
   */
  public Line validate() throws GeneralScannerException {
    this.m_validationState = IN_VALIDATION;
    try {
      LineValidator.validate(this);
    } catch(GeneralScannerException ex) {
      this.m_validationState = NOT_VALIDATED;
      throw(ex);
    }
    this.m_validationState = VALID;
    return this;
  }

  /////////////////////////////////////////////////////////// private methods
  /**
   * Throws a <code>LineNotYetValidatedException</code> if this line
   * isn't in a valid state. Otherwise, this method won't do anything.
   * @see org.parasit.xia.scanner.exception.LineNotYetValidatedException
   */
  private void checkIfValid() {
    if(this.m_validationState == NOT_VALIDATED) {
      throw new LineNotYetValidatedException(this.m_lineNumber);
    }
  }

  /**
   * Overwrites the default implementation implemented by the Class
   * <code>Object</code>. Returns a string representation of this instance.
   * @return a string representation of this instance
   */
  public String toString() {
    String str = this.getClass().getName()+"=[";
    str += "m_lineNumber:"+m_lineNumber+", ";
    str += "m_validationState:"+m_validationState+", ";
    str += "m_opcode:"+m_opcode+", ";
    str += "m_arg:";
    if(m_arg==null) str += "null";
    else for(int i=0;i<m_arg.length;++i) str += m_arg[i].toString();
    str += "]";
    return str;
  }
}