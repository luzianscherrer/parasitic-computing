package org.parasit.xia.global;

/**
 * Defines an interface for valid argument types and valid virtual
 * registers.
 * @version $Revision: 1.4 $
 */
public interface ArgumentTypes {

  /**
   * A valid constant has to be built by the following expression<br>
   * <pre>
   *   ((0d)?[0-9]+) |          ; decimal
   *   (0b[01]+)     |          ; binary
   *   (0x[0-9a-fA-F]+)         ; hexadecimal
   * </pre>
   */
  public final int ARG_TYPE_CONSTANT       = 1;
  /**
   * A valid register has to be built by the following expression<br>
   * <pre>
   *   r(0-0)+ |                ; a virtual register
   *   ip      |                ; the instruction pointer 4ia wide
   *   fl      |                ; the carry flag 4ia wide
   *   cf      |                ; the carry flag Xia wide
   *   ec      |                ; the error code Xia wide
   * </pre>
   */
  public final int ARG_TYPE_REGISTER       = 2;

  /**
   * A pointer is a symbol before a register, mostly a star (*), and
   * contains an address of another register.<br>
   * A valid pointer has to be built by the following expression<br>
   * <pre>
   *   '*'(ARG_TYPE_REGISTER)
   * </pre>
   */
  public final int ARG_TYPE_POINTER        = 4;

  /**
   * Contains alphanumeric and some special characters, used to insert
   * comments in generated 4ia
   */
  public final int ARG_TYPE_TEXT           = 8;

  /**
   * Contains alphanumeric and a few special characters, used to name labels
   */
  public final int ARG_TYPE_LABEL          = 16;

  /** The instruction pointer symbol used 4ia wide */
  public static final String  IP_4ia           = "ip";

  /** carry flag 4ia wide */
  public static final String  CARRYFLAG_4ia    = "fl";
  /** error code after program termination */
  public static final String  ERRORCODE        = "ec";

  /**
   * carry flag Xia wide. Contains value 1 in case an over/underflow occured
   * after one of the instructions <code>ADD</code>, <code>MUL</code> or
   * <code>SUB</code>. If no over/underflow occured, the value of the carry
   * flag won't be changed.<br>
   * After a <code>DIV</code> the carry flag contains the rest of the
   * division. I.e. 19 divided by 5, the carry flag contains
   * <code>19-(3*5)=4</code>.
   */
  public static final String  CARRYFLAG_Xia    = "cf";
}
