package org.parasit.xia.scanner;

import org.parasit.xia.scanner.exception.ArgumentTypeNotSetException;
import org.parasit.xia.compiler.exception.NoExplicitRegisterNumberException;
import org.parasit.xia.global.ArgumentTypes;
import java.lang.Integer;

/**
 * Represents an Argument, built by the scanner and used by the
 * CodeGenerator. An Argument knows his type of content.
 * @version $Revision: 1.4 $
 */
public class Argument implements ArgumentTypes{
  private String m_value;
  private int m_type = -1;

  /**
   * Constructor to create a new Argument without type definition.
   * The argument its type has to be defined lateron by the scanner.
   * This means that this argument is in an invalid state and can't
   * be used offhand.
   * @param value the name of the Argument, the value so called.
   */
  public Argument(String value) {
    this.m_value = value;
  }

  /**
   * Constructor to create a new Argument with type definition.
   * The argument its type will be defined.
   * This means that this argument can be used upon from instantiation.
   * @param value the name of the Argument, the value so called.
   * @param type the type of the argument.
   */
  public Argument(String value, int type) {
    this.m_value = value;
    this.m_type = type;
  }

  /**
   * Returns the name (value) of the argument.
   * @return the name (value) of the argument.
   */
  public String getValue() {
    return this.m_value;
  }

  /**
   * Resets the name (value) of this argument.
   * @param the new name (value) of this argument.
   */
  public void setValue(String value) {
      this.m_value = value;
  }

  /**
   * (Re)sets the type of this argument.
   * @param the (new) type of this argument.
   */
  public void setType(int type) {
    this.m_type = type;
  }

  /**
   * Returns one of the values defined in class
   * ArgumentTypes or throws a RuntimeException in type
   * is not yet defined..
   * @return Returns one of the values defined in class
   * ArgumentTypes.
   * @throws ArgumentTypeNotSetException if the argument type is not set
   */
  public int getType() {
    // check if argument type is already defined
    if(this.m_type == -1) {
      throw new ArgumentTypeNotSetException(this.m_value);
    }
    else {
      return this.m_type;
    }
  }

  /**
   * If this arguments value is of type Register or Pointer, the explixit
   * register number will be returned. Otherwise, an
   * NoExplicitRegisterNumberException will be thrown.
   * @return the explixit register number.
   * @throws NoExplicitRegisterNumberException
   */
  public int getRegisterNumber() throws NoExplicitRegisterNumberException{
    if(this.getType() == ARG_TYPE_REGISTER) {
      return Integer.parseInt(this.getValue().substring(2));
    }
    else if(this.getType() == ARG_TYPE_POINTER) {
      return Integer.parseInt(this.getValue().substring(3));
    }

    else{
      throw new NoExplicitRegisterNumberException(this.getValue());
    }
  }

  /**
   * Overwrites method toString() inherited from super class Object.
   */
  public String toString() {
    return this.getClass().getName()+"=["+
           "m_value:"+this.m_value+", "+
           "m_type:"+this.m_type+"]";
  }
}
