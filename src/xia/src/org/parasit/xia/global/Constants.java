package org.parasit.xia.global;

/**
 * Contains all the constants used to scan, resolve and cross-compile
 * Xia code. Further this class manages settings about register width,
 * used to define the minus 1 constant and the number of registers who
 * has to be allocated by the VM later on.
 * @version $Revision: 1.9 $
 */
public class Constants {

  // -------------------------------------------------------- private members
  /** Represents the default respectively the setted register width */
  private static int m_regSize = 8;
  /** Used registers, setted by ARCH Opcode. Default -1 */
  private static int m_regsUsed = -1;
  /** Represents the default respectively the setted maximum
      count of registers */
  private static int m_regMax  = 256;

  /** Represents the Version of Xia cross compiler */
  public static final String Xto4_VERSION = "Version 1.07";

  // ------------------------------------------------------- opcodes Xia wide
  // general
  /** Internal used constant */
  public static final int ARCH  =  0;
  /** Internal used constant */
  public static final int SET   =  1;
  /** Internal used constant */
  public static final int MOV   =  2;
  /** Internal used constant */
  public static final int HLT   =  3;
  /** Internal used constant */
  public static final int SPACE =  4;
  /** Internal used constant */
  public static final int LABEL =  5;

  // mathematix
  /** Internal used constant */
  public static final int ADD   =  6;
  /** Internal used constant */
  public static final int SUB   =  7;
  /** Internal used constant */
  public static final int MUL   =  8;
  /** Internal used constant */
  public static final int DIV   =  9;
  /** Internal used constant */
  public static final int MOD   = 10;

  // logical operators (bitwise)
  /** Internal used constant */
  public static final int AND   = 11;
  /** Internal used constant */
  public static final int OR    = 12;
  /** Internal used constant */
  public static final int XOR   = 13;
  /** Internal used constant */
  public static final int NOT   = 14;
  /** Internal used constant */
  public static final int SHL   = 15;
  /** Internal used constant */
  public static final int SHR   = 16;

  // jumps
  /** Internal used constant */
  public static final int JMP   = 17;
  /** Internal used constant */
  public static final int JG    = 18;
  /** Internal used constant */
  public static final int JGE   = 19;
  /** Internal used constant */
  public static final int JEQ   = 20;
  /** Internal used constant */
  public static final int JLE   = 21;
  /** Internal used constant */
  public static final int JL    = 22;
  /** Internal used constant */
  public static final int JNE   = 23;

  // general
  /** Internal used constant */
  private static final String OPCODE_ARCH  = "ARCH";
  /** Internal used constant */
  private static final String OPCODE_SET   = "SET";
  /** Internal used constant */
  private static final String OPCODE_MOV   = "MOV";
  /** Internal used constant */
  private static final String OPCODE_HLT   = "HLT";
  /** Internal used constant */
  private static final String OPCODE_SPACE = "SPACE";
  /** Internal used constant */
  private static final String OPCODE_LABEL = "LABEL";

  // mathematix
  /** Internal used constant */
  private static final String OPCODE_ADD   = "ADD";
  /** Internal used constant */
  private static final String OPCODE_SUB   = "SUB";
  /** Internal used constant */
  private static final String OPCODE_MUL   = "MUL";
  /** Internal used constant */
  private static final String OPCODE_DIV   = "DIV";
  /** Internal used constant */
  private static final String OPCODE_MOD   = "MOD";

  // logical operators (bitwise)
  /** Internal used constant */
  private static final String OPCODE_AND = "AND";
  /** Internal used constant */
  private static final String OPCODE_OR  = "OR";
  /** Internal used constant */
  private static final String OPCODE_XOR = "XOR";
  /** Internal used constant */
  private static final String OPCODE_NOT = "NOT";
  /** Internal used constant */
  private static final String OPCODE_SHL = "SHL";
  /** Internal used constant */
  private static final String OPCODE_SHR = "SHR";

  // jumps
  /** Internal used constant */
  private static final String OPCODE_JMP   = "JMP";
  /** Internal used constant */
  private static final String OPCODE_JG    = "JG";
  /** Internal used constant */
  private static final String OPCODE_JGE   = "JGE";
  /** Internal used constant */
  private static final String OPCODE_JEQ   = "JEQ";
  /** Internal used constant */
  private static final String OPCODE_JLE   = "JLE";
  /** Internal used constant */
  private static final String OPCODE_JL    = "JL";
  /** Internal used constant */
  private static final String OPCODE_JNE   = "JNE";

  // ------------------------------------------------------- opcodes 4ia wide
  /** Internal used constant */
  public static final  String SET_4ia = "SET";
  /** Internal used constant */
  public static final  String MOV_4ia = "MOV";
  /** Internal used constant */
  public static final  String ADD_4ia = "ADD";
  /** Internal used constant */
  public static final  String HLT_4ia = "HLT";

  /** Internal used constant */
  public static final String  PREFIX_DECIMAL   =  "0d";
  /** Internal used constant */
  public static final String  PREFIX_BINARY    =  "0b";
  /** Internal used constant */
  public static final String  PREFIX_HEX       =  "0x";


  //////////////////////////////////////////////////////////// public methods
  /**
   * Resolves the name of the opcode and returns its corresponding internal
   * used index.
   * Returns -1 in case no matching opcode is found.
   * @return -1 in case no matching opcode is found.
   */
  public static int getIndex(String opcode) {
    // general
    if(opcode.equalsIgnoreCase(OPCODE_ARCH))       return ARCH;
    else if(opcode.equalsIgnoreCase(OPCODE_SET))   return SET;
    else if(opcode.equalsIgnoreCase(OPCODE_MOV))   return MOV;
    else if(opcode.equalsIgnoreCase(OPCODE_HLT))   return HLT;
    else if(opcode.equalsIgnoreCase(OPCODE_SPACE)) return SPACE;
    else if(opcode.equalsIgnoreCase(OPCODE_LABEL)) return LABEL;

    // mathematix
    else if(opcode.equalsIgnoreCase(OPCODE_ADD))   return ADD;
    else if(opcode.equalsIgnoreCase(OPCODE_SUB))   return SUB;
    else if(opcode.equalsIgnoreCase(OPCODE_MUL))   return MUL;
    else if(opcode.equalsIgnoreCase(OPCODE_DIV))   return DIV;
    else if(opcode.equalsIgnoreCase(OPCODE_MOD))   return MOD;

    // logical operators (bitwise)
    else if(opcode.equalsIgnoreCase(OPCODE_AND))   return AND;
    else if(opcode.equalsIgnoreCase(OPCODE_OR))    return OR;
    else if(opcode.equalsIgnoreCase(OPCODE_XOR))   return XOR;
    else if(opcode.equalsIgnoreCase(OPCODE_NOT))   return NOT;
    else if(opcode.equalsIgnoreCase(OPCODE_SHL))   return SHL;
    else if(opcode.equalsIgnoreCase(OPCODE_SHR))   return SHR;

    // jumps
    else if(opcode.equalsIgnoreCase(OPCODE_JMP))   return JMP;
    else if(opcode.equalsIgnoreCase(OPCODE_JG))    return JG;
    else if(opcode.equalsIgnoreCase(OPCODE_JGE))   return JGE;
    else if(opcode.equalsIgnoreCase(OPCODE_JEQ))   return JEQ;
    else if(opcode.equalsIgnoreCase(OPCODE_JLE))   return JLE;
    else if(opcode.equalsIgnoreCase(OPCODE_JL))    return JL;
    else if(opcode.equalsIgnoreCase(OPCODE_JNE))   return JNE;
    else return -1;
  }

  /**
   * Returns the default respectively setted register width.
   * @return the default respectively setted register width.
   */
  public static final int getRegSize() {
    return m_regSize;
  }

  /**
   * Returns the Number of used Registers set by the User.
   * @return the Number of used Registers set by the User, -1 if
   *         the User didn't set anything (default) with the XIA Opcode ARCH
   */
  public static final int getRegsUsed() {
    return m_regsUsed;
  }

  /**
   * Sets the Number of used Registers set by the User.
   * @param the Number of used Registers set by the User.
   */
  public static final void setRegsUsed(int regsUsed) {
    m_regsUsed = regsUsed;
  }

  /**
   * Sets the register width defined by the Xia source code.
   * @param regSize the register width in bits.
   */
  public static final void setRegSize(int regSize) {
      m_regSize = regSize;
      m_regMax = ((int)Math.pow(2d, (double)regSize));
  }

  /**
   * Returns the maximum of addressable registers with setted
   * register width in bits. This means that this value
   * its calculated as the following:<br>
   * &nbsp;&nbsp;<code>2<sup>reg size</sup></code>
   * @return the maximum of addressable registers with setted
   * register width in bits.
   */
  public static final int getRegMax() {
    return m_regMax;
  }

  /**
   * Calculates and returns the value <code>MaxReg-1</code>
   * @return the calculated value <code>MaxReg-1</code>
   */
  public static final String getRegMinus1() {
    return ""+(m_regMax-1);
  }
}
