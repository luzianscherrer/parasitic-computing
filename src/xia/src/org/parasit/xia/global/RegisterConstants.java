package org.parasit.xia.global;

/**
 * Definitions for virtual and temporary used registers.
 * @version $Revision: 1.4 $
 */
public interface RegisterConstants {
  /** Represents the instruction pointer (ip) from 4ia */
  public static final String REG_IP     = "ip";
  /** Represents the Flag register (fl) from 4ia */
  public static final String REG_FLAG   = "fl";
  /** Represents the error code register used Xia wide */
  public static final String REG_EC     = "r2";
  /** Represents the carry flag register used Xia wide */
  public static final String REG_CF     = "r3";
  /** Represents a temporary used register Xia wide */
  public static final String REG_TMP_1  = "r4";
  /** Represents a temporary used register Xia wide */
  public static final String REG_TMP_2  = "r5";
  /** Represents a temporary used register Xia wide */
  public static final String REG_TMP_3  = "r6";
  /** Represents a temporary used register Xia wide */
  public static final String REG_TMP_4  = "r7";
  /** Represents a temporary used register Xia wide.
   * Used if macro itself uses other macro */
  public static final String REG_TMP_5  = "r8";
  /** Represents a temporary used register Xia wide.
   * Used if macro itself uses other macro */
  public static final String REG_TMP_6  = "r9";
  /** arg1 value. Used if macro don't use other macros */
  public static final String REG_TMP_7  = "r10";
  /** arg2 value. Used if macro don't use other macros */
  public static final String REG_TMP_8  = "r11";
  /** arg1 value. Used if macro itself uses other macro */
  public static final String REG_TMP_9  = "r12";
  /** arg2 value. Used if macro itself uses other macro */
  public static final String REG_TMP_10 = "r13";

  /** further register used by boolean operations */
  public static final String REG_TMP_11 = "r14";



  /** Defines the lowest Register that can be used in Xia */
  public static final int VIRTUAL_REGISTER_BEGIN = 15;
}
