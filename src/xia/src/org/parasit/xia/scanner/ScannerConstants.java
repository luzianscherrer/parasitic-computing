package org.parasit.xia.scanner;

import java.util.Hashtable;
import org.parasit.xia.global.Constants;

/**
 * Provides constants used to scan Xia source code
 * @version $Revision: 1.4 $
 */
public class ScannerConstants extends Constants {
  /** Defines all valid alphanumeric characters */
  private static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz"+
                                             "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  /** Defines all valid concatenation characters */
  private static final String CONCATENATOR = "_-";
  /** Defines all valid numeric binary characters */
  public static final String NUMERIC_BIN  = "01";
  /** Defines all valid numeric decimal and hexadecimal characters */
  public static final String NUMERIC      = "0123456789";
  /** Defines all valid numeric decimal characters */
  public static final String NUMERIC_HEX  = NUMERIC+"abcdefABCDEF";
  /** Defines all valid alphanumeric, concatenatin and numeric characters */
  protected static final String ACN = ALPHANUMERIC+NUMERIC+CONCATENATOR;
  /** Defines all valid characters allowed in comments */
  protected static final String TEXT = ACN+"#@+*/^?!><.,;:()[]{} ";


  // Syntax definitions for Xia
  /** introduces comments removed by the scanner */
  public static final char    COMMENTSEPARATOR = ';';
  /** prefix to indicate that a register is a pointer to another register */
  public static final char    PREFIX_POINTER   = '*';
  /** prefix for a virtual register Xia wide */
  public static final char    PREFIX_REGISTER  = 'r';
  /** prefix for a label Xia wide */
  public static final char    PREFIX_LABEL     = 'L';
}




