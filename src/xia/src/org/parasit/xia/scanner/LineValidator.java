package org.parasit.xia.scanner;

import org.parasit.xia.global.*;
import org.parasit.xia.scanner.exception.*;

import java.lang.StringBuffer;
import java.lang.NumberFormatException;


/**
 * This class validates lines with its static methods. The validation
 * steps through the following tasks:
 * <ol>
 *   <li> Check if opcode is valid
 *   <li> Check if a specific register width is set BEFORE the first
 *        Xia instruction..
 *   <li> Check each argument of the specific opcode and set the
 *        arguments type.
 *   <li> In case of an error, add the error information to an error
 *        vector who will be prompted to the user at the end of Xia
 *        source code validation
 * </ol>
 * @version $Revision: 1.4 $
 */
public class LineValidator
    extends ScannerConstants
    implements ArgumentTypes{

    private static boolean m_init = true;

  /**
   * Validates the opcode and all its arguments of the current line.
   * @return true if line is valid, false otherwise.
   */
  public static void validate(Line line) throws GeneralScannerException {
    switch(opcode(line)) {
      // ------------------------------------------------------------ general
      case ARCH: {
        if(!m_init) {
            throw new InvalidArchPlaceException(line.getLineNumber(), line.getOpcode());
        }
        else {
          if(line.getArgCount() == 1) {
            // Programmer just set Architecture
            checkArguments(line, ARG_TYPE_CONSTANT);
          } else {
            // Programmer set Architecture and number of used Registers
            checkArguments(line, ARG_TYPE_CONSTANT, ARG_TYPE_CONSTANT);
            setRegsUsed(Integer.parseInt(line.getArg(2).getValue()));
          }
          setRegSize(Integer.parseInt(line.getArg(1).getValue()));
          m_init = false;     // don't allow anymore register width changes
        }
        break;
      }
      case SET: {
        checkArguments(line, ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case MOV: {
        checkArguments(line, ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_REGISTER+ARG_TYPE_POINTER);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case HLT: {
        checkArguments(line);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case SPACE: {
        // concatenate all arguments to one...
        String string = "";
        for(int i=1;i<=line.getArgCount();++i) {
          string += line.getArg(i).getValue()+" ";
        }
        line.setArgs(new Argument[]{new Argument(string)});
        checkArguments(line, ARG_TYPE_TEXT);
        break;
      }
      case LABEL: {
        checkArguments(line, ARG_TYPE_LABEL);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      // --------------------------------------------------------- mathematix
      case ADD: {
        checkArguments(line, ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case SUB: {
        checkArguments(line, ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case MUL: {
        checkArguments(line, ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case DIV: {
        checkArguments(line, ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case MOD: {
        checkArguments(line, ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER);
        m_init = false;     // don't allow anymore register width changes
        break;
      }

      // ---------------------------------------- logical operators (bitwise)
      case AND: {
        checkArguments(line, ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case OR: {
        checkArguments(line, ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case XOR: {
        checkArguments(line, ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case NOT: {
        checkArguments(line, ARG_TYPE_REGISTER+ARG_TYPE_POINTER);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case SHL: {
        checkArguments(line, ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case SHR: {
        checkArguments(line, ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER);
        m_init = false;     // don't allow anymore register width changes
        break;
      }

      // ---------------------------------------- logical operators (bitwise)
      case JMP: {
        checkArguments(line, ARG_TYPE_LABEL);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case JG: {
        checkArguments(line, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_LABEL);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case JGE: {
        checkArguments(line, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_LABEL);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case JEQ: {
        checkArguments(line, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_LABEL);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case JLE: {
        checkArguments(line, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_LABEL);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case JL: {
        checkArguments(line, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_LABEL);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      case JNE: {
        checkArguments(line, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_CONSTANT+ARG_TYPE_REGISTER+ARG_TYPE_POINTER, ARG_TYPE_LABEL);
        m_init = false;     // don't allow anymore register width changes
        break;
      }
      // default case is never used cuz method opcode already checks if
      // opcode is valid...
    }
  }

  /**
   * Tries to resolve the Opcode string into a valid index used in switch()
   * in method validate(Line). In case Opcode couldn't be resolved, an
   * <code>org.parasit.xia.scanner.exception.InvalidOpcodeException</code>
   * will be thrown.
   * @return the corresponding index to the Opcode or throws an Exception.
   * @throws org.parasit.xia.scanner.exception.InvalidOpcodeException
   */
  private static int opcode(Line line)
      throws InvalidArgumentCountException,
             InvalidArgumentException,
             InvalidOpcodeException {
    int index = getIndex(line.getOpcode());
    if(index == -1) {
      throw new InvalidOpcodeException(line.getLineNumber(),
                                       line.getOpcode());
    }
    return index;
  }

  /**
   * Checks if the current line don't have any arguments.
   * @param line the current line who has to be checked.
   */
  private static void checkArguments(Line line)
      throws InvalidArgumentCountException {
    if(line.getArgCount() !=0) {
      throw new InvalidArgumentCountException(line.getLineNumber(),
                                              line.getOpcode() ,
                                              0, line.getArgCount());
    }
  }

  /**
   * Checks if the current line has exactly one argument and this
   * argument is of the allowed type.
   * @param line the current line who has to be checked.
   * @param type_arg1 the allowed argument type(s)
   */
  private static void checkArguments(Line line, int type_arg1)
      throws InvalidArgumentCountException,
             InvalidArgumentException
             {
    if(line.getArgCount() !=1) {
      throw new InvalidArgumentCountException(line.getLineNumber(),
                                              line.getOpcode() ,
                                              1, line.getArgCount());
    }

    checkArgument(line, 1, type_arg1);
  }

  /**
   * Checks if the current line has exactly two arguments and this
   * arguments are of the allowed type.
   * @param line the current line who has to be checked.
   * @param type_arg1 the allowed argument type(s)
   */
  private static void checkArguments(Line line, int type_arg1,
                                     int type_arg2)
      throws InvalidArgumentCountException,
             InvalidArgumentException
             {
    if(line.getArgCount() !=2) {
      throw new InvalidArgumentCountException(line.getLineNumber(),
                                              line.getOpcode() ,
                                              2, line.getArgCount());
    }

    checkArgument(line, 1, type_arg1);
    checkArgument(line, 2, type_arg2);
  }

  private static void checkArguments(Line line, int type_arg1,
                                     int type_arg2,int type_arg3)
      throws InvalidArgumentCountException,
             InvalidArgumentException
             {
    if(line.getArgCount() !=3) {
      throw new InvalidArgumentCountException(line.getLineNumber(),
                                              line.getOpcode() ,
                                              3, line.getArgCount());
    }
    checkArgument(line, 1, type_arg1);
    checkArgument(line, 2, type_arg2);
    checkArgument(line, 3, type_arg3);
  }

  /**
   *  ARG_TYPE_CONSTANT
   *  ARG_TYPE_REGISTER
   *  ARG_TYPE_POINTER
   *  ARG_TYPE_TEXT
   */
  private static void checkArgument(Line line, int argNr, int type_arg)
    throws InvalidArgumentException{
    String bin = Integer.toBinaryString(type_arg);
    // 110
    StringBuffer errorMsg = new StringBuffer();

    for(int i=bin.length();i>0;--i) {
      if(bin.substring(i-1, i).equals("1")) {
        try {
          argTypeSwitch((int)Math.pow(2d, (double)(bin.length()-i)), line, argNr);
          return;
        } catch(InvalidArgumentException ex) {
          errorMsg.append(ex.getMessage()+"\n");
        }
      }
    }

    throw new InvalidArgumentException(line.getLineNumber(),
                                       "Invalid argument.\n  Argument "+
                                       argNr+" doesn't match any of the "+
                                       "rules of allowed types.\n"+
                                        "  Allowed types: "+bin+". "+
                                        "Thrown exceptions:\n\n"+
                                        errorMsg.toString());
  }

  private static void argTypeSwitch(int type_arg, Line line, int argNr)
    throws InvalidArgumentException{
    switch(type_arg) {
      case ARG_TYPE_CONSTANT: {
        checkConstantFormat(line, argNr);
        break;
      }
      case ARG_TYPE_REGISTER: {
        checkRegisterFormat(line, argNr);
        break;
      }
      case ARG_TYPE_POINTER:  {
        checkPointerFormat(line, argNr);
        break;
      }
      case ARG_TYPE_TEXT: {
        checkTextFormat(line, argNr);
        break;
      }
      case ARG_TYPE_LABEL: {
        checkLabelFormat(line, argNr);
        break;
      }
      default : {
        throw new InvalidArgumentTypeException(line.getLineNumber(),
                                               type_arg);
      }
    }
  }

  /**
   * A constant may be in binary, decimal or hexadecimal format:
   * <code>
   * DEZIMAL: (0-9)+ or '0d'(0-9)+
   * BINAER:  '0b'(0,1)+
   * HEX:     0x(0-9, a-f, A-F)+
   * </code>
   */
  private static void checkConstantFormat(Line line, int argNr)
      throws InvalidConstantException {
    String arg = line.getArg(argNr).getValue();
    int errorPosition = 0;

    // check if argument is of type decimal without explicit radix declaration...
    errorPosition = checkString(NUMERIC, arg);

    // check if constant with explicit radix declaration
    if(errorPosition != -1) {
      // check if constant is explicit binary
      if(arg.substring(0,2).equals(PREFIX_BINARY)) {
        errorPosition = checkString(NUMERIC_BIN, arg.substring(2, arg.length()));
      }

      // check if constant is explicit decimal
      else if(arg.substring(0,2).equals(PREFIX_DECIMAL)) {
        errorPosition = checkString(NUMERIC, arg.substring(2, arg.length()));
      }

      // check if constant is explicit hexadecimal
      else if(arg.substring(0,2).equals(PREFIX_HEX)) {
        errorPosition = checkString(NUMERIC_HEX, arg.substring(2, arg.length()));
      }
    }

    if(errorPosition == -1) {
      line.getArg(argNr).setType(ARG_TYPE_CONSTANT);
    } else {
      throw new InvalidConstantException(line.getLineNumber(),
                                           argNr, arg, errorPosition+2);
    }
  }


  private static void checkRegisterFormat(Line line, int argNr)
      throws InvalidRegisterException {
    String arg = line.getArg(argNr).getValue();
    int errorPosition = 0;

    if(arg.equals(IP_4ia)    || arg.equals(CARRYFLAG_4ia) ||
       arg.equals(ERRORCODE) || arg.equals(CARRYFLAG_Xia)) {
      errorPosition = -1;
    }

    if(errorPosition != -1) {
      if(arg.substring(0, 1).equals(""+PREFIX_REGISTER)) {
        errorPosition = checkString(NUMERIC, arg.substring(1, arg.length()));
      }
    }

    if(errorPosition == -1) {
      line.getArg(argNr).setType(ARG_TYPE_REGISTER);
    } else {
      throw new InvalidRegisterException(line.getLineNumber(),
                                           argNr, arg, errorPosition+1);
    }
  }


  private static void checkPointerFormat(Line line, int argNr)
      throws InvalidPointerException{
    String arg = line.getArg(argNr).getValue();
    int errorPosition = 0;

    if(!(""+arg.charAt(0)).equals(""+PREFIX_POINTER)) {
      throw new InvalidPointerException(line.getLineNumber(),
                                  argNr,
                                  arg,
                                  0);
    }

    if(arg.equals(IP_4ia)    || arg.equals(CARRYFLAG_4ia) ||
       arg.equals(ERRORCODE) || arg.equals(CARRYFLAG_Xia)) {
      errorPosition = -1;
    }

    if(errorPosition != -1) {
      errorPosition = checkString(NUMERIC, arg.substring(2, arg.length()));
    }

    if(errorPosition == -1) {
      line.getArg(argNr).setType(ARG_TYPE_POINTER);
    } else {
      throw new InvalidPointerException(line.getLineNumber(),
                                           argNr, arg, errorPosition+2);
    }
  }


  private static void checkTextFormat(Line line, int argNr)
                      throws InvalidTextException{
    String arg = line.getArg(argNr).getValue();

    for(int i=0;i<arg.length(); ++i) {
      if(TEXT.indexOf(arg.charAt(i)) == -1 ) {
        throw new InvalidTextException(line.getLineNumber(), arg, i);
      }
    }
    line.getArg(argNr).setType(ARG_TYPE_TEXT);
  }

  private static void checkLabelFormat(Line line, int argNr)
                      throws InvalidLabelException,
                             InvalidLabelLengthException {
    String arg = line.getArg(argNr).getValue();
    int errorPosition = 0;

    // check if prefix for a label is correct...
    if(!(""+arg.charAt(0)).equals(""+PREFIX_LABEL)) {
      throw new InvalidLabelException(line.getLineNumber(),
                                  arg, 0);
    }

    // check if label length is longer that just the prefix..
    if(arg.length() == (""+PREFIX_LABEL).length()) {
      throw new InvalidLabelLengthException(line.getLineNumber(),
                                            arg, 0);
    }

    // check all characters followed by the prefix
    errorPosition = checkString(ACN, arg.substring(1, arg.length()));

    if(errorPosition == -1) {
      line.getArg(argNr).setType(ARG_TYPE_LABEL);
    } else {
      throw new InvalidLabelException(line.getLineNumber(),
                                      arg, errorPosition+1);
    }
  }


  /**
   * Checks if each character of string <code>string</code> in one
   * of characters <code>allowedChars</code>.
   * @return <code>-1</code> if each character of string
   * <code>string</code> is one of characters <code>allowedChars</code>,
   * the first non matching position of the String otherwise
   */
  private static int checkString(String allowedChars, String string) {
    for(int i=0;i<string.length(); ++i) {
      if(allowedChars.indexOf(string.charAt(i)) == -1 ) {
        return i;
      }
    }
    return -1;
  }
}
