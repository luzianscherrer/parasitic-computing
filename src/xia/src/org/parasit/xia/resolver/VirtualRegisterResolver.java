package org.parasit.xia.resolver;

import org.parasit.xia.scanner.*;
import org.parasit.xia.global.*;
import org.parasit.xia.resolver.exception.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.lang.StringBuffer;

/**
 * Resolves all virtual registers used Xia with in absolut registers used
 * 4ia wide and generates some statistic informations about number of
 * used registers and their addresses.
 * Furthermore, this resolver validates the addresses of the used registers
 * and shows up errors about wrongly used registers.
 * @version $Revision: 1.6 $
 */
public class VirtualRegisterResolver extends ScannerConstants
                                     implements ArgumentTypes,
                                                RegisterConstants{
  /** Represents a vector with all created errors by resolving the code */
  private ErrorVector m_errVec;
  /**  */
  private Hashtable m_usedRegisters;
  /** Represents the lowest used register */
  private int m_LowestRegisterUsed  = -1;
  /** Represents the highest used register */
  private int m_highestRegisterUsed = -1;

  /**
   * Initializes the resolver.
   */
  public VirtualRegisterResolver() {
    m_errVec = new ErrorVector();
    m_usedRegisters = new Hashtable();
  }

  /**
   * Resolves all virtual registers into real registers used 4ia wide.
   * @param lineVector the vector containing references to the lines
   *        with its arguments
   * @throws VirtualRegisterResolverException if an error occurs
   */
  public void resolve(LineVector lineVector)
      throws VirtualRegisterResolverException {
    for(Enumeration enum = lineVector.elements(); enum.hasMoreElements();){
      resolveSpecialRegisters((Line)enum.nextElement());
    }

    if(m_errVec.size() > 0) {
      throw new VirtualRegisterResolverException(m_errVec);
    }
  }

  /**
   * returns assembled informations about current Xto4 settigns and
   * some infos about used ergisters in inputted file.
   * @return assembled header information.
   */
  public String getHeaderInfo() {
    return this.assembleHeaderInformation();
  }

  /**
   * Checks if an argument is a virtal register like for example
   * the carry flag or the error code and translates it the the real
   * register name used 4ia wide.
   * @param line the line who has to be checked and translated as necessary.
   */
  private void resolveSpecialRegisters(Line line) {
    for(int i=1;i<=line.getArgCount();++i) {
      Argument arg = line.getArg(i);
      if(arg.getType() == ARG_TYPE_REGISTER) {
        if(arg.getValue().equals(IP_4ia)) {
          line.getArg(i).setValue(REG_IP);
        }
        else if(arg.getValue().equals(CARRYFLAG_4ia)) {
          line.getArg(i).setValue(REG_FLAG);
        }
        else if(arg.getValue().equals(ERRORCODE)) {
          line.getArg(i).setValue(REG_EC);
        }
        else if(arg.getValue().equals(CARRYFLAG_Xia)) {
          line.getArg(i).setValue(REG_CF);
        }
        else if(arg.getValue().charAt(0) == PREFIX_REGISTER) {
          line.getArg(i).setValue(get4iaReg(arg.getValue(), line));
        }
      }
      else if(arg.getType() == ARG_TYPE_POINTER){
        if(arg.getValue().equals(PREFIX_POINTER+IP_4ia)) {
          line.getArg(i).setValue(PREFIX_POINTER+REG_IP);
        }
        else if(arg.getValue().equals(PREFIX_POINTER+CARRYFLAG_4ia)) {
          line.getArg(i).setValue(PREFIX_POINTER+REG_FLAG);
        }
        else if(arg.getValue().equals(PREFIX_POINTER+ERRORCODE)) {
          line.getArg(i).setValue(PREFIX_POINTER+REG_EC);
        }
        else if(arg.getValue().equals(PREFIX_POINTER+CARRYFLAG_Xia)) {
          line.getArg(i).setValue(PREFIX_POINTER+REG_CF);
        }
        else if(arg.getValue().charAt(0) == PREFIX_POINTER) {
          line.getArg(i).setValue(get4iaPointer(arg.getValue(), line));
        }
      }
    }
  }

  /**
   * Checks if the register number is in valid range and returns
   * the resolved register name.
   */
  private String get4iaReg(String XiaReg, Line line) {
    checkIfRegNrIsValid(XiaReg, line, Integer.parseInt(XiaReg.substring(1)));
    return XiaReg;
  }

  /**
   * Checks if the pointer number is in valid range and returns
   * the resolved pointer name.
   */
  private String get4iaPointer(String XiaReg, Line line) {
    checkIfRegNrIsValid(XiaReg, line, Integer.parseInt(XiaReg.substring(2)));
    return XiaReg;
  }

  /**
   * Checks if the actual register number is in the valid range of
   * usable registers
   * @param XiaReg The name of the register
   * @param line the corresponding line object to <code>XiaReg</code>
   * @param number the number of the registers
   */
  private void checkIfRegNrIsValid(String XiaReg, Line line, int number) {
    if(number < VIRTUAL_REGISTER_BEGIN ||
       number > getRegMax()) {
       m_errVec.add(
         new InvalidRegisterNumberException(XiaReg,
                                            line.getLineNumber(),
                                            number,
                                            VIRTUAL_REGISTER_BEGIN,
                                            getRegMax()));
    } else {
      this.addUsedRegister(XiaReg, number);
    }

  }

  /**
   * Assembles headerinformations about compiled settings of Xto4 and
   * Xia code statistics of current inputted file and returns a beautifully
   * styled string of it :)
   * @return headerinformations about compiled settings of Xto4 and Xia code
   * statistics of current inputted file
   */
  private String assembleHeaderInformation() {
    return
    "; This 4ia code has been compiled by Xto4 cross compiler, "+
    Xto4_VERSION+"\n"+
    "; Copyright (c) 2002 Luzian Scherrer, Juerg Reusser\n"+
    "; Check out http://www.parasit.org for further informations.\n;\n"+
    "; VM settings to run the following code:\n"+
    ";   Lowest useable register : "+PREFIX_REGISTER+VIRTUAL_REGISTER_BEGIN+"\n"+
    ";   Really used registers   : "+getUsedRegistersCount()+"\n"+
    ";     lowest  register used : "+PREFIX_REGISTER+getLowestRegisterUsed()+"\n"+
    ";     highest register used : "+PREFIX_REGISTER+getHighestRegisterUsed()+"\n"+
    ";   Register width in bits  : "+getRegSize()+"\n"+
    ";   Addressable registers   : "+getRegMax()+"\n"+
    ";   decrementing constant   : "+getRegMinus1()+"\n"+
    ";   Opcodes used 4ia wide   : "+SET_4ia+", "+MOV_4ia +", "+
                                     ADD_4ia+", "+HLT_4ia+"\n;\n"+
    "; NOTE: Statistics considers only directly addressed registers.\n\n"+
    "; Initialization parameters for the virtual machine\n"+
    "ARCH "+getRegSize()+" "+((getRegsUsed()==-1)?
                                 (getHighestRegisterUsed()+1):
                                 getRegsUsed())+"\n\n";
  }

  /**
   * Returns the number of registers actually used in resolved 4ia code
   * @return the number of registers actually used in resolved 4ia code
   */
  private int getUsedRegistersCount() {
    return m_usedRegisters.size();
  }

  /**
   * Adds the current register <code>XiaReg</code> to the hashtable of
   * used registers and checks if actual register is the lowest respectively
   * the highest number of used registers. This if current register wasn't
   * already added before.
   * @param XiaReg the name of the register
   * @param number the number of the added register
   */
  private void addUsedRegister(String XiaReg, int number) {
    // check if already added before...
    if(m_usedRegisters.get(XiaReg) == null) {
      // check if initialization or number ist new lowest register
      if(this.getLowestRegisterUsed() == -1 ||
         number < this.getLowestRegisterUsed()) {
        this.setLowestRegisterUsed(number);
      }

      // check if initialization or number ist new highest register
      if(this.getHighestRegisterUsed() == -1 ||
         number > this.getHighestRegisterUsed()) {
        this.setHighestRegisterUsed(number);
      }

      // add register to hashtable with used registers
      m_usedRegisters.put(XiaReg, ""+1);
    } else {
      // increments the number of times this register has
      // been used direct and explicitly
      m_usedRegisters.put(XiaReg,
                          ""+(1+Integer.parseInt(
                            ""+m_usedRegisters.get(XiaReg))));
    }
  }

  /**
   * Return the number of the lowest register used.
   * @return the number of the lowest register used.
   */
  private int getLowestRegisterUsed() {
    return m_LowestRegisterUsed;
  }
  /**
   * Sets the number of the lowest register used.
   * @param lowest the number of the lowest register used.
   */
  private void setLowestRegisterUsed(int lowest) {
    this.m_LowestRegisterUsed = lowest;
  }

  /**
   * Return the number of the highest register used.
   * @return the number of the highest register used.
   */
  private int getHighestRegisterUsed() {
    return m_highestRegisterUsed;
  }
  /**
   * Sets the number of the highest register used.
   * @param highest the number of the highest register used.
   */
  private void setHighestRegisterUsed(int highest) {
    this.m_highestRegisterUsed = highest;
  }
}
