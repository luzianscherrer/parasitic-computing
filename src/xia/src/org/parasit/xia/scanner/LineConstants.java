package org.parasit.xia.scanner;

/**
 * Represents constants to define all possible states in
 * which a Line can be.
 * @see org.parasit.xia.scanner.Line
 * @version $Revision: 1.3 $
 */
public interface LineConstants {
  /** If a line is set up but not yet validated */
  public  int NOT_VALIDATED = 0;
  /** If the line is in validation */
  public int IN_VALIDATION  = 1;
  /** If the line is valid with its opcode and all arguments */
  public int VALID          = 2;
}