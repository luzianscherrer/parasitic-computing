package org.parasit.xia.compiler;

import org.parasit.xia.scanner.Argument;
import org.parasit.xia.scanner.Line;
import org.parasit.xia.scanner.LineVector;
import org.parasit.xia.global.*;
import org.parasit.util.StringHelper;
import java.lang.StringBuffer;
import java.util.Enumeration;


/**
 * Compiles Xia Operations into 4ia code.&nbspFor translations from an Xia
 * Opcode to 4ia instructions, please take a look to the Opcode method.
 * CodeGenerator steps through the following tasks:
 * <ol>
 *   <li> By creating a new instance of CodeGenerator, the constructor gets
 *        a vector with operations and the header informations who will be
 *        placed at the beginning of the generated code.
 *   <li> An enumeration enumerates through all the lines and calls the
 *        method according to the opcode found in each line. These member
 *        methods generate 4ia code and adds it to a buffer, represented
 *        by an inner class called 'Buffer'.
 *   <li> An instance of LabelHandler handles labels generated within the
 *        macros in CodeGenerator to allow jumping.
 *   <li> After each line of the Xia vector is beeing processed, the
 *        label handler resolves its registered labels into absolut line
 *        numbers 4ia wide.
 *   <li> The generated code with its resolved labels will be returned.
 * </ol>
 * @version $Revision: 1.12 $
 */
public class CodeGenerator extends Constants
                           implements ArgumentTypes,
                                      RegisterConstants{

  /** buffers generated 4ia code in a temptaty buffer who also formats 4ia
   *  code
   */
  private Buffer buffer;

  /** member instance to handle labels */
  private LabelHandler m_labelHandler;

  /** Represents the Xia input soure to generate 4ia code */
  private LineVector m_inputVector;

  /** label used to check and handle divisions by zero */
  private final String DIVBYZERO_LABEL = "DBZ";

  /** label used to check and handle divisions by zero */
  private final String CHECK_DIV_PRECONDITIONS = "CDPC";

  /** indicates whether to add code to check division by zero or not */
  private boolean divisionUsed = false;


  /**
   * Creates a new instance of CodeGenerator and instantiates all
   * CodeGenerator relevant helperclasses such as an innerclass Buffer and
   * a label handler, sets the input vector with Xia lines and writes
   * initializing 4ia code to the code buffer.
   * @param vec the LineVector with the parsed Xia code
   * @param headerInformation the header string puttet at the beginning
   * of the 4ia code who informes about the number of used registers in the
   * program and other statistic infos...
   */
  public CodeGenerator(LineVector vec, String headerInformation) {
    buffer = new Buffer(headerInformation);
    this.m_inputVector = vec;
    m_labelHandler = new LabelHandler();
    buffer.writeLine(SET_4ia, REG_EC, 0, "Error code = 0 setzen zu Beginn des Programmes.");
  }

  /**
   * Generates 4ia code, resolves all setted labels to 4ia code positions
   * and returns the generated Xia source;
   * @return the generated 4ia source.
   */
  public String generate4ia() {

    // generate 4ia code for each Xia soure line...
    for(Enumeration enum = m_inputVector.elements(); enum.hasMoreElements();){
      this.compile((Line)enum.nextElement());
    }

    // check if divisionCheck code is needed...
    if(divisionUsed) {
      this.addDivByZeroCheckCode();
    }

    return this.m_labelHandler.resolveLabels(buffer.getString());
  }

  /**
   * For each opcode, this method will be called. It determines which
   * member method has to process the corresponding opcode with its
   * arguments.
   */
  private void compile(Line line) {
    switch(getIndex(line.getOpcode())) {
      // ---------- general
      case SET: {
        this.SET(line.getArg(1), line.getArg(2));
        break;
      }
      case MOV: {
        this.MOV(line.getArg(1), line.getArg(2));
        break;
      }
      case HLT: {
        this.HLT();
        break;
      }
      case SPACE: {
        this.SPACE(line.getArg(1));
        break;
      }
      case LABEL: {
        this.LABEL(line.getArg(1).getValue());
        break;
      }

      // ---------- mathematix
      case ADD: {
        this.ADD(line.getArg(1), line.getArg(2));
        break;
      }
      case SUB: {
        this.SUB(line.getArg(1), line.getArg(2));
        break;
      }
      case MUL: {
        this.MUL(line.getArg(1), line.getArg(2));
        break;
      }
      case DIV: {
        this.DIV(line.getArg(1), line.getArg(2));
        break;
      }
      case MOD: {
        this.MOD(line.getArg(1), line.getArg(2));
        break;
      }
      // ---------- logical operators (bitwise)
      case AND: {
        this.AND(line.getArg(1), line.getArg(2));
        break;
      }
      case OR: {
        this.OR(line.getArg(1), line.getArg(2));
        break;
      }
      case XOR: {
        this.XOR(line.getArg(1), line.getArg(2));
        break;
      }
      case NOT: {
        this.NOT(line.getArg(1));
        break;
      }
      case SHL: {
        this.SHL(line.getArg(1), line.getArg(2));
        break;
      }
      case SHR: {
        this.SHR(line.getArg(1), line.getArg(2));
        break;
      }

      // ---------- jumps
      case JMP: {
        this.JMP(line.getArg(1));
        break;
      }
      case JG: {
        this.JG(line.getArg(1), line.getArg(2), line.getArg(3).getValue());
        break;
      }
      case JGE: {
        this.JGE(line.getArg(1), line.getArg(2), line.getArg(3).getValue());
        break;
      }
      case JEQ:{
        this.JEQ(line.getArg(1), line.getArg(2), line.getArg(3).getValue());
        break;
      }
      case JLE: {
        this.JLE(line.getArg(1), line.getArg(2), line.getArg(3).getValue());
        break;
      }
      case JL: {
        this.JL(line.getArg(1), line.getArg(2), line.getArg(3).getValue());
        break;
      }
      case JNE: {
        this.JNE(line.getArg(1), line.getArg(2), line.getArg(3).getValue());
        break;
      }
    }
  }

  //////////////////////////////////////////// instructions with no arguments
  /**
   * Represents one by one the HLT instruction 4ia wide. For further
   * information about this, please take a look to the 4ia specifications.
   * <br><br><b>Used registers:</b> <code>none</code>
   */
  private void HLT()
  {
    buffer.writeLine(HLT_4ia, "Stoppt die VM");
  }

  //////////////////////////////////////////// instructions with one argument
  /**
   * Adds comment written from the user in a Xia source code.
   * <br><br><b>Used registers:</b> <code>none</code>
   * @param arg contains the comment the user entered in the Xia source code
   */
  private void SPACE(Argument arg) {
    this.SPACE(arg.getValue());
  }

  /**
   * Adds comment either from another macro or a comment line Xia wide.
   * <br><br><b>Used registers:</b> <code>none</code>
   * @param arg contains the string who will be placed on a separate 4ia
   *        without increasing and useing a line number.
   */
  private void SPACE(String string) {
    buffer.writeLine(string);
  }

  /**
   * Represents a label used in a macro or a in a Xia source code.
   * The name of the label will be registered in the label handler who will
   * check if this label is already used or not.
   * Used by another macro, a label may be called anywise contrary to a label
   * used in Xia code who has to fulfill to a well defined syntax.
   * <br><br><b>Used registers:</b> <code>none</code>
   * @param label The name of the label under witch it will be registered in
   *        the label handler.
   */
  private void LABEL(String label) {
    m_labelHandler.addLabel(new XiaLabel(label, buffer.getCurrentLineNr()-1));
  }

  /**
   * Jumps to the label. The label handler will resolve the label strings
   * into absolut line numbers used in 4ia. This method is called if
   * a JUMP has to be processed in Xia source code.
   * <br><br><b>Used registers:</b> <code>none</code>
   * @param arg The argument containing the name of the label.
   */
  private void JMP(Argument arg) {
    this.JMP(arg.getValue());

  }

  /**
   * Jumps to the label. The label handler will resolve the label strings
   * into absolut line numbers used in 4ia. This method is called if
   * either a JUMP has to be processed in Xia source code or another macro
   * makes a jump.
   * <br><br><b>Used registers:</b> <code>none</code>
   */
  private void JMP(String label) {
    buffer.writeLine(SET_4ia, REG_IP, label, "Unbedingter Sprung zu Zeile "+label);
  }

  /////////////////////////////////////////// instructions with two arguments
  /**
   * Sets the register to witch argument <code>arg1</code> points with the
   * constant represented by argument <code>arg2</code>.
   * First, this method resolves the argument <code>arg1</code> so the
   * resolved temporary register will be no pointer but rather an absolut
   * register.
   * Then, the value from <code>arg2</code> will be set in <code>arg1</code>.
   * Finally, the value from the themporary used register will be copied back
   * to the register who <code>arg1</code> points to.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_7</code>&nbsp;&nbsp;value of argument 1
   * </ul>
   * @param arg1 the argument who represents the register the constant
   * <code>arg2</code> will be 'copied' (set) into.
   */
  private void SET(Argument arg1, Argument arg2) {
    moveArgToTemp(arg1, REG_TMP_7);
    buffer.writeLine(SET_4ia, REG_TMP_7, arg2.getValue());
    moveTempToArg(arg1, REG_TMP_7);
  }

  /**
   * Represents one by one the MOV instruction 4ia wide. For further
   * information about this, please take a look to the 4ia specifications.
   * <br><br><b>Used registers:</b> <code>none</code>
   * @param arg1 the argument who represents the destination register
   *        or pointer.
   * @param arg2 the argument who represents the soruce register or pointer.
   */
  private void MOV(Argument arg1, Argument arg2)
  {
    buffer.writeLine(MOV_4ia, arg1.getValue(), arg2.getValue(), "Reg. Wert "+arg2.getValue()+" in Reg. "+arg1.getValue()+" kopieren");
  }

  /**
   * Adds the value of the register to witch <code>arg1</code> points to
   * to the value of the register to witch argument <code>arg2</code>
   * points to.
   * First, this method resolves the arguments <code>arg1</code> and
   * <code>arg2</code> so the resolved temporary registers will be no
   * pointers but rather absolut registers.
   * Then, the value from <code>arg2</code> will be added to
   * <code>arg1</code>.
   * Finally, the result value from the themporary used register will be
   * copied back to the register who <code>arg1</code> points to.
   * Represents the ADD instruction 4ia wide. For further information about
   * this, please take a look to the 4ia specifications.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_7</code>&nbsp;&nbsp;value of argument 1
   *   <li><code>REG_TMP_8</code>&nbsp;&nbsp;value of argument 2
   * </ul>
   */
  private void ADD(Argument arg1, Argument arg2) {
    moveArgToTemp(arg1, REG_TMP_7);
    moveArgToTemp(arg2, REG_TMP_8);
    buffer.writeLine(ADD_4ia, REG_TMP_7, REG_TMP_8, "Addition");
    moveTempToArg(arg1, REG_TMP_7);
  }

  /**
   * Subtracts the value of the register to witch <code>arg2</code> points
   * to from the value of the register to witch argument <code>arg1</code>
   * points to.
   * First, this method resolves the arguments <code>arg1</code> and
   * <code>arg2</code> so the resolved temporary registers will be no
   * pointers or constants but rather absolut registers.
   * Then, the following algorithm will be processed:<br>
   * <pre>
   *   int i = arg2;
   *   while(i > 0) --arg1;
   * </pre><br>
   * Finally, the result value from the themporary used register will
   * be copied back to the register who <code>arg1</code> points to.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_1</code>&nbsp;&nbsp;decrementation constant
   *   <li><code>REG_TMP_7</code>&nbsp;&nbsp;value of argument 1
   *   <li><code>REG_TMP_8</code>&nbsp;&nbsp;value of argument 2
   * </ul>
   */
  private void SUB(Argument arg1, Argument arg2) {
    moveArgToTemp(arg1, REG_TMP_7);
    moveArgToTemp(arg2, REG_TMP_8);
    String SUBLabel[] = m_labelHandler.getTemporaryLabel(3);

    buffer.writeLine(SET_4ia, REG_TMP_1, getRegMinus1(), "Integerkonstante fuer -1 Subtraktion");
    buffer.writeLine(SET_4ia, REG_IP, SUBLabel[1],"Erste Subtraktion ueberspringen");
    LABEL(SUBLabel[0]);
    buffer.writeLine(ADD_4ia, REG_TMP_7, REG_TMP_1, "Register "+REG_TMP_7+" dekrementieren");
    LABEL(SUBLabel[1]);
    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Flag zuruecksetzen auf 0");
    buffer.writeLine(ADD_4ia, REG_TMP_8, REG_TMP_1, "Zaehler "+REG_TMP_8+" dekrementieren");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Schlaufe verlassen falls Zaehler "+REG_TMP_8+" auf 0");
    buffer.writeLine(SET_4ia, REG_IP, SUBLabel[2]);
    buffer.writeLine(SET_4ia, REG_IP, SUBLabel[0]);
    // end of subtraction loop
    LABEL(SUBLabel[2]);
    moveTempToArg(arg1, REG_TMP_7);
  }

  /**
   * Multiplicates the value of register to witch <code>arg1</code> points to
   * with the value of the register to witch argument <code>arg2</code>
   * points to.
   * First, this method resolves the arguments <code>arg1</code> and
   * <code>arg2</code> so the resolved registers will be no pointer or
   * constant but rather absolut registers.
   * Then, the following algorithm will be processed:<br>
   * <pre>
   *   int i = arg2;
   *   while(i > 0){
   *     arg1 += arg1;
   *     --arg2;
   *   }
   * </pre>
   * Finally, the result value from the themporary used register will
   * be copied back to the register who <code>arg1</code> points to.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_1</code>&nbsp;&nbsp;decrementation constant
   *   <li><code>REG_TMP_2</code>&nbsp;&nbsp;result register
   *   <li><code>REG_TMP_7</code>&nbsp;&nbsp;value of argument 1
   *   <li><code>REG_TMP_8</code>&nbsp;&nbsp;value of argument 2
   * </ul>
   */
  private void MUL(Argument arg1, Argument arg2) {
    moveArgToTemp(arg1, REG_TMP_7);
    moveArgToTemp(arg2, REG_TMP_8);
    String MULLabel[] = m_labelHandler.getTemporaryLabel(2);

    SPACE("Beginn der Multiplikation");
    buffer.writeLine(SET_4ia, REG_TMP_2, 0, "Resultat Register initialisieren");
    buffer.writeLine(SET_4ia, REG_TMP_1, getRegMinus1(), "Integerkonstante fuer -1 Subtraktion");
    buffer.writeLine(SET_4ia, REG_IP, MULLabel[1], "  Addition wiederholen");
    LABEL(MULLabel[0]);
    buffer.writeLine(ADD_4ia, REG_TMP_2, REG_TMP_7, "Addition");
    LABEL(MULLabel[1]);
    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Flag zuruecksetzen auf 0");
    buffer.writeLine(ADD_4ia, REG_TMP_8, REG_TMP_1, "Zaehler dekrementieren, Flag setzen falls Unterlauf");

    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Schlaufe verlassen falls Flag gesetzt");
    buffer.writeLine(SET_4ia, REG_IP, buffer.getCurrentLineNr()+1, "  Schlaufe verlassen");
    buffer.writeLine(SET_4ia, REG_IP, MULLabel[0], "  Addition wiederholen");

    moveTempToArg(arg1, REG_TMP_2);
  }

  /**
   * Divides the value of register to witch <code>arg1</code> points to
   * by the value of the register to witch argument <code>arg2</code>
   * points to  and copies the result to the register to witch
   * <code>arg1</code> points to, the remainder to <code>REG_CF</code>.
   * <br><b>Division by zero</b><br>
   * By trying to divide <code>arg1</code> by zero, the program will be
   * stopped and error code register <code>REG_EC</code> will be set.<br>
   * First, this method resolves the arguments <code>arg1</code> and
   * <code>arg2</code> so the resolved registers will be no pointer or
   * constant but rather absolut registers.
   * Then, the following algorithm will be processed:<br>
   * <pre>
   *   resultat = -1;
   *   while(dividend >= 0) {
   *     dividend -= divisor;
   *     resultat++;
   *   }
   *   rest = divisor - (-1 * dividend);
   * </pre>
   * Finally, the result value from the themporary used register will
   * be copied back to the register who <code>arg1</code> points to.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_1</code>&nbsp;&nbsp;decrementation constant
   *   <li><code>REG_TMP_2</code>&nbsp;&nbsp;incrementation constant
   *   <li><code>REG_TMP_3</code>&nbsp;&nbsp;inner loop counter
   *   <li><code>REG_TMP_4</code>&nbsp;&nbsp;result register

   *   <li><code>REG_TMP_7</code>&nbsp;&nbsp;value of argument 1
   *   <li><code>REG_TMP_8</code>&nbsp;&nbsp;value of argument 2
   * </ul>
   * <!-- *************************************************************** -->
   *      Be careful by changing/adding temporary registers cuz DIV
   *      is included by other macros like SHR!
   * <!-- *************************************************************** -->
   * @param arg1 dividend
   * @param arg2 divisor
   * @see org.parasit.xia.compiler.CodeGenerator#addDivByZeroCheckCode()
   * @see org.parasit.xia.compiler.CodeGenerator#checkDivByZero(String, int)
   */
  private void DIV(Argument arg1, Argument arg2) {
    divisionUsed = true; // will add code to check division by zero
    String DIVLabel[] = m_labelHandler.getTemporaryLabel(8);
    moveArgToTemp(arg1, REG_TMP_7);
    moveArgToTemp(arg2, REG_TMP_8); // Innerer Schlaufenzaehler
    checkDivByZero(REG_TMP_8, buffer.getCurrentLineNr());

    buffer.writeLine(SET_4ia, REG_TMP_1, getRegMinus1(), "Subtraktionskonstante -1");
    buffer.writeLine(SET_4ia, REG_TMP_2, 1, "Additionskonstante 1");
    buffer.writeLine(SET_4ia, REG_TMP_4, 0, "Resultat Register mit 0 initialisieren");
    LABEL(DIVLabel[0]);
    buffer.writeLine(MOV_4ia, REG_TMP_3, REG_TMP_8, "Innerer Schlaufenzaehler initialisieren ("+REG_TMP_3+")");
    JMP(DIVLabel[2]);
    LABEL(DIVLabel[1]);
    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Flag loeschen");
    buffer.writeLine(ADD_4ia, REG_TMP_7, REG_TMP_1, "Dekrement Dividend");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Bei Dividend > 0 weiter");
    buffer.writeLine(SET_4ia, REG_IP, DIVLabel[4], "Bei Dividend == 0 ende");
    LABEL(DIVLabel[2]);
    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Flag loeschen");
    buffer.writeLine(ADD_4ia, REG_TMP_3, REG_TMP_1, "Dekrement innerer Schlaufenzaehler");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Ueberlauf verarbeiten fuer Auswahl in Zeile "+(buffer.getCurrentLineNr()+1)+"/"+(buffer.getCurrentLineNr()+2));
    buffer.writeLine(SET_4ia, REG_IP, DIVLabel[3], "  Bei Schlaufenzaehler == 0 ende innere Schlaufe");
    buffer.writeLine(SET_4ia, REG_IP, DIVLabel[1], "  Bei Schlaufenzaehler > 0 weiter");
    LABEL(DIVLabel[3]);
    buffer.writeLine(ADD_4ia, REG_TMP_4, REG_TMP_2, "Inkrement Quotient");
    buffer.writeLine(SET_4ia, REG_IP, DIVLabel[0], "Innere Schlaufe neu beginnen");
    LABEL(DIVLabel[4]);
    // Copy result back to arg1
    moveTempToArg(arg1, REG_TMP_4);
    // Computation of remainder starts here
    buffer.writeLine(MOV_4ia, REG_CF, REG_TMP_8, "Divisor ins Carry-Register kopieren");
    JMP(DIVLabel[5]);
    LABEL(DIVLabel[7]);
    buffer.writeLine(ADD_4ia, REG_CF, REG_TMP_1, "Kopie von Divisor dekrementieren");
    LABEL(DIVLabel[5]);
    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Flag loeschen");
    buffer.writeLine(ADD_4ia, REG_TMP_3, REG_TMP_1, "Schlaufenzaehler dekrementieren");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Ueberlaufcheck");
    JMP(DIVLabel[6]);
    JMP(DIVLabel[7]);
    LABEL(DIVLabel[6]);
    buffer.writeLine(ADD_4ia, REG_CF, REG_TMP_1, "Carry-Reg. dekrementieren -> Divisionsrest");
  }

  /**
   * This method calls the DIV method and copies the division rest in the
   * register where <code>arg1</code> points to.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_5</code>&nbsp;&nbsp;decrementation constant
   *   <li><code>REG_TMP_6</code>&nbsp;&nbsp;incrementation constant
   *   <li>All registers used by the Division
   * </ul>
   * <!-- *************************************************************** -->
   *     Be careful by changing/adding temporary registers cuz MOD uses DIV
   * <!-- *************************************************************** -->
   * @param arg1 dividend
   * @param arg2 divisor
   * @see org.parasit.xia.compiler.CodeGenerator#DIV(Argument, Argument)
   */
  private void MOD(Argument arg1, Argument arg2) {
    moveArgToTemp(arg1, REG_TMP_5);
    moveArgToTemp(arg2, REG_TMP_6);
    SPACE("Division aufrufen:");
    SPACE("Divisionsrest steht danach in Carry Flag ("+REG_CF+")");
    DIV(new Argument(REG_TMP_5, ARG_TYPE_REGISTER), new Argument(REG_TMP_6, ARG_TYPE_REGISTER));
    SPACE("Divisionsrest in "+arg1.getValue()+" kopieren");
    moveTempToArg(arg1, REG_CF);
  }

  /**
   * Bitwise AND of the values of registers to witch <code>arg1</code>
   * and <code>arg2</code> points to. The overflow bit will be lost in case
   * the highest two bits are set.
   * The result value from the temporary used register will
   * be copied back to the register who <code>arg1</code> points to.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_1</code>&nbsp;&nbsp;decrementation constant
   *   <li><code>REG_TMP_2</code>&nbsp;&nbsp;incrementation constant '1'
   *   <li><code>REG_TMP_3</code>&nbsp;&nbsp;register containing the result
   *   <li><code>REG_TMP_4</code>&nbsp;&nbsp;most significant bit
   *                             <code>arg1</code>
   *   <li><code>REG_TMP_5</code>&nbsp;&nbsp;most significant bit
   *                             <code>arg2</code>
   *   <li><code>REG_TMP_6</code>&nbsp;&nbsp;value of argument 1
   *   <li><code>REG_TMP_7</code>&nbsp;&nbsp;value of argument 2
   *   <li><code>REG_TMP_8</code>&nbsp;&nbsp;constant for fix addition
   *                             (only most significant bit is set)
   *   <li><code>REG_TMP_9</code>&nbsp;&nbsp;Loop counter
   *                       (val: registerWidth -1)
   * </ul>
   * <!-- *************************************************************** -->
   *      Be careful by changing/adding temporary registers cuz OR
   *      will use this macro!
   * <!-- *************************************************************** -->
   * @param arg1 operand 1
   * @param arg2 operand 2
   * @see org.parasit.xia.compiler.CodeGenerator#addDivByZeroCheckCode()
   * @see org.parasit.xia.compiler.CodeGenerator#checkDivByZero(String, int)
   */
  private void AND(Argument arg1, Argument arg2) {
    String ANDLabel[] = m_labelHandler.getTemporaryLabel(2);
    moveArgToTemp(arg1, REG_TMP_6);   // Operand 1
    moveArgToTemp(arg2, REG_TMP_7);   // Operand 2

    buffer.writeLine(SET_4ia, REG_TMP_8, getRegMax()/2, "Konstante fuer Fixaddition (hoechstes Bit auf 1)");
    buffer.writeLine(SET_4ia, REG_TMP_2, 1, "Konstante fuer Fixaddition (tiefstes Bit auf 1)");
    buffer.writeLine(SET_4ia, REG_TMP_1, getRegMinus1(), "Konstante fuer Fixaddition (maximale Ganzzahl)");

    buffer.writeLine(SET_4ia, REG_TMP_9, getRegSize()-1, "Schlaufenzaehler (Registerbreite - 1)");

    LABEL(ANDLabel[0]);
    buffer.writeLine(ADD_4ia, REG_TMP_3, REG_TMP_3, "Resultat nach links schieben");

    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Flag zuruecksetzen");
    buffer.writeLine(MOV_4ia, REG_TMP_4, REG_TMP_6, "Aktuelles hoechstwertiges Bit von Op 1 ermitteln");
    buffer.writeLine(ADD_4ia, REG_TMP_4, REG_TMP_8);
    buffer.writeLine(ADD_4ia, REG_FLAG, REG_FLAG, "Flag mit 2 multiplizieren, optimierter bedingter");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Sprung");
    buffer.writeLine(SET_4ia, REG_TMP_4, 0);
    buffer.writeLine(ADD_4ia, REG_IP, REG_TMP_2);
    buffer.writeLine(MOV_4ia, REG_TMP_4, REG_TMP_8);

    buffer.writeLine(SET_4ia, REG_FLAG, 0);
    buffer.writeLine(MOV_4ia, REG_TMP_5, REG_TMP_7, "Aktuelles hoechstw. Bit von Op 2 ermitteln");
    buffer.writeLine(ADD_4ia, REG_TMP_5, REG_TMP_8);
    buffer.writeLine(ADD_4ia, REG_FLAG, REG_FLAG, "s. oben");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG);
    buffer.writeLine(SET_4ia, REG_TMP_5, 0);
    buffer.writeLine(ADD_4ia, REG_IP, REG_TMP_2);
    buffer.writeLine(MOV_4ia, REG_TMP_5, REG_TMP_8);

    buffer.writeLine(SET_4ia, REG_FLAG, 0);
    buffer.writeLine(ADD_4ia, REG_TMP_4, REG_TMP_5, "Addition der ermittelten hoechstwertigen Bits");
    buffer.writeLine(ADD_4ia, REG_TMP_3, REG_FLAG, "Flag enth. Resultat \"AND\" der obigen zwei Bits");

    buffer.writeLine(ADD_4ia, REG_TMP_6, REG_TMP_6, "Operand 1 nach links schieben");
    buffer.writeLine(ADD_4ia, REG_TMP_7, REG_TMP_7, "Operand 2 nach links schieben");

    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Flag loeschen");
    buffer.writeLine(ADD_4ia, REG_TMP_9, REG_TMP_1, "Schlaufenzaehler dekrementieren");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Bei Ueberlauf Schlaufe wiederholen:");
    buffer.writeLine(SET_4ia, REG_IP, ANDLabel[1], "Srung ans Ende (Zeile "+ANDLabel[1]+")");
    buffer.writeLine(SET_4ia, REG_IP, ANDLabel[0], "Sprung zu Zeile "+ANDLabel[0]);
    LABEL(ANDLabel[1]);
    moveTempToArg(arg1, REG_TMP_3);

  }

  /**
   * Bitwise XOR of the values of registers to witch <code>arg1</code>
   * and <code>arg2</code> points to. <br>
   * The result value from the temporary used register will
   * be copied back to the register who <code>arg1</code> points to.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_1</code>&nbsp;&nbsp;decrementation constant
   *   <li><code>REG_TMP_2</code>&nbsp;&nbsp;incrementation constant '1'
   *   <li><code>REG_TMP_3</code>&nbsp;&nbsp;register containing the result
   *   <li><code>REG_TMP_4</code>&nbsp;&nbsp;most significant bit
   *                             <code>arg1</code>
   *   <li><code>REG_TMP_5</code>&nbsp;&nbsp;most significant bit
   *                             <code>arg2</code>
   *   <li><code>REG_TMP_6</code>&nbsp;&nbsp;value of argument 1
   *   <li><code>REG_TMP_7</code>&nbsp;&nbsp;value of argument 2
   *   <li><code>REG_TMP_8</code>&nbsp;&nbsp;constant for fix addition
   *                             (only most significant bit is set)
   *   <li><code>REG_TMP_9</code>&nbsp;&nbsp;Loop counter
   *                       (val: registerWidth -1)
   * </ul>
   * <!-- *************************************************************** -->
   *      Be careful by changing/adding temporary registers cuz OR and NOT
   *      will use this macro!
   * <!-- *************************************************************** -->
   * @param arg1 operand 1
   * @param arg2 operand 2
   * @see org.parasit.xia.compiler.CodeGenerator#addDivByZeroCheckCode()
   * @see org.parasit.xia.compiler.CodeGenerator#checkDivByZero(String, int)
   */
  private void XOR(Argument arg1, Argument arg2) {
    String XORLabel[] = m_labelHandler.getTemporaryLabel(2);
    moveArgToTemp(arg1, REG_TMP_6);   // Operand 1
    moveArgToTemp(arg2, REG_TMP_7);   // Operand 2

    buffer.writeLine(SET_4ia, REG_TMP_8, getRegMax()/2, "Konstante fuer Fixaddition (hoechstes Bit auf 1)");
    buffer.writeLine(SET_4ia, REG_TMP_2, 1, "Konstante fuer Fixaddition (tiefstes Bit auf 1)");
    buffer.writeLine(SET_4ia, REG_TMP_1, getRegMinus1(), "Konstante fuer Fixaddition (maximale Ganzzahl)");

    buffer.writeLine(SET_4ia, REG_TMP_9, getRegSize()-1, "Schlaufenzaehler (Registerbreite - 1)");

    LABEL(XORLabel[0]);
    buffer.writeLine(ADD_4ia, REG_TMP_3, REG_TMP_3, "Resultat nach links schieben");

    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Flag zuruecksetzen");
    buffer.writeLine(MOV_4ia, REG_TMP_4, REG_TMP_6, "Aktuelles hoechstwertiges Bit von Op 1 ermitteln");
    buffer.writeLine(ADD_4ia, REG_TMP_4, REG_TMP_8);
    buffer.writeLine(ADD_4ia, REG_FLAG, REG_FLAG, "Flag mit 2 multiplizieren, optimierter bedingter");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Sprung");
    buffer.writeLine(SET_4ia, REG_TMP_4, 0);
    buffer.writeLine(ADD_4ia, REG_IP, REG_TMP_2);
    buffer.writeLine(MOV_4ia, REG_TMP_4, REG_TMP_8);

    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Flag zueruecksetzen");
    buffer.writeLine(MOV_4ia, REG_TMP_5, REG_TMP_7, "Aktuelles hoechstwertiges Bit von Op 2 ermitteln");
    buffer.writeLine(ADD_4ia, REG_TMP_5, REG_TMP_8);
    buffer.writeLine(ADD_4ia, REG_FLAG, REG_FLAG, "s. oben");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG);
    buffer.writeLine(SET_4ia, REG_TMP_5, 0);
    buffer.writeLine(ADD_4ia, REG_IP, REG_TMP_2);
    buffer.writeLine(MOV_4ia, REG_TMP_5, REG_TMP_8);

    buffer.writeLine(ADD_4ia, REG_TMP_4, REG_TMP_5, "Addition der zwei ermittelten hoechstw. Bits");
    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Flag loeschen");
    buffer.writeLine(ADD_4ia, REG_TMP_4, REG_TMP_8, "Hoechstwertiges Bit nach Flag transferieren");
    buffer.writeLine(ADD_4ia, REG_TMP_3, REG_FLAG, "Flag enth. Resultat \"XOR\" der obigen zwei Bits");

    buffer.writeLine(ADD_4ia, REG_TMP_6, REG_TMP_6, "Operand 1 nach links schieben");
    buffer.writeLine(ADD_4ia, REG_TMP_7, REG_TMP_7, "Operand 2 nach links schieben");

    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Flag loeschen");
    buffer.writeLine(ADD_4ia, REG_TMP_9, REG_TMP_1, "Schlaufenzaehler dekrementieren");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Bei Ueberlauf Schlaufe wiederholen:");
    buffer.writeLine(SET_4ia, REG_IP, XORLabel[1], "Srung ans Ende (Zeile "+XORLabel[1]+")");
    buffer.writeLine(SET_4ia, REG_IP, XORLabel[0], "Sprung zu Zeile "+XORLabel[0]);
    LABEL(XORLabel[1]);
    moveTempToArg(arg1, REG_TMP_3);
  }

  /**
   * Bitwise OR of the values of registers to witch <code>arg1</code>
   * and <code>arg2</code> points to.<br>
   * This macro uses the further macros <code>AND</code> and
   * <code>XOR</code> like the following therm:<br>
   * <code>((a XOR 1) AND (b XOR 1)) XOR 1</code><br>
   * Note: 1 is the maximum count of addressable registers -1. For this,
   * check also the methode Constants.getRegMinus1().<br>
   * The result value from the temporary used register will
   * be copied back to the register who <code>arg1</code> points to.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_10</code>&nbsp;&nbsp;value of argument 1
   *   <li><code>REG_TMP_11</code>&nbsp;&nbsp;value of argument 2
   * </ul>
   * @param arg1 operand 1
   * @param arg2 operand 2
   * @see org.parasit.xia.compiler.CodeGenerator#AND(Argument, Argument)
   * @see org.parasit.xia.compiler.CodeGenerator#XOR(Argument, Argument)
   * @see org.parasit.xia.global.Constants#getRegMinus1()
   */
  private void OR(Argument arg1, Argument arg2) {
    moveArgToTemp(arg1, REG_TMP_10);   // Operand 1
    moveArgToTemp(arg2, REG_TMP_11);   // Operand 2

    XOR(new Argument(REG_TMP_10, ARG_TYPE_REGISTER), new Argument(""+getRegMinus1(), ARG_TYPE_CONSTANT));
    XOR(new Argument(REG_TMP_11, ARG_TYPE_REGISTER), new Argument(""+getRegMinus1(), ARG_TYPE_CONSTANT));
    AND(new Argument(REG_TMP_10, ARG_TYPE_REGISTER), new Argument(REG_TMP_11, ARG_TYPE_REGISTER));
    XOR(new Argument(REG_TMP_10, ARG_TYPE_REGISTER), new Argument(""+getRegMinus1(), ARG_TYPE_CONSTANT));
    moveTempToArg(arg1, REG_TMP_10);
  }

  /**
   * Bitwise NOT of the value of register to witch <code>arg1</code>
   * points to.<br>
   * This macro uses the further macro <code>XOR</code> like the
   * following therm:<br>
   * <code>a XOR 1</code><br>
   * Note: 1 is the maximum count of addressable registers -1. For this,
   * check also the methode Constants.getRegMinus1().<br>
   * The result value from the temporary used register will
   * be copied back to the register who <code>arg1</code> points to.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_10</code>&nbsp;&nbsp;value of argument 1
   * </ul>
   * @param arg1 operand 1
   * @see org.parasit.xia.compiler.CodeGenerator#XOR(Argument, Argument)
   * @see org.parasit.xia.global.Constants#getRegMinus1()
   */
  private void NOT(Argument arg1) {
    moveArgToTemp(arg1, REG_TMP_10);   // Operand 1
    XOR(new Argument(REG_TMP_10, ARG_TYPE_REGISTER), new Argument(""+getRegMinus1(), ARG_TYPE_CONSTANT));
    moveTempToArg(arg1, REG_TMP_10);
  }

  /**
   * Shifts left one bit <code>arg2</code> times the value from
   * argument <code>arg1</code>.
   * By an overflow, the involved bits will be lost.<br>
   * First, this method resolves the arguments <code>arg1</code> and
   * <code>arg2</code> so the resolved registers will be no pointer or
   * constant but rather absolut registers.
   * Then, the following algorithm will be processed:<br>
   * <pre>
   *   for(int i=1; i<=arg2; ++i) {
   *     arg1 += arg1;
   *   }
   * </pre>
   *
   * In case of an overflow by shifting to the left, the carry flag
   * <code>cf</code> will be set to 1.
   * Finally, the result value from the themporary used register will
   * be copied back to the register who <code>arg1</code> points to.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_1</code>&nbsp;&nbsp; decrementation constant
   *   <li><code>REG_TMP_2</code>&nbsp;&nbsp; inner loop counter ->
   *       number of shifts to the left
   *   <li><code>REG_TMP_7</code>&nbsp;&nbsp;value of argument 1
   *   <li><code>REG_TMP_8</code>&nbsp;&nbsp;value of argument 2
   * </ul>
   * @param arg1 value who will be shifted
   * @param arg2 number of shifts to the left by one bit
   */
  private void SHL(Argument arg1, Argument arg2) {
    String SHLLabel[] = m_labelHandler.getTemporaryLabel(4);
    moveArgToTemp(arg1, REG_TMP_7);
    moveArgToTemp(arg2, REG_TMP_8);

    buffer.writeLine(SET_4ia, REG_CF, 0, "Carry flag Xia Seitig zuruecksetzen");
    buffer.writeLine(SET_4ia, REG_TMP_1, getRegMinus1(), "Subtraktionskonstante -1");
    buffer.writeLine(MOV_4ia, REG_TMP_2, REG_TMP_8, "Anzahl shifts ("+arg2.getValue()+") initialisieren");
    buffer.writeLine(SET_4ia, REG_IP, SHLLabel[2], "erstes SHL ueberspringen -> falls 0 shifts gewuenscht");
    LABEL(SHLLabel[0]);
    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Ueberlauf flag auf 0 setzen");
    buffer.writeLine(ADD_4ia, REG_TMP_7, REG_TMP_7, "shift left "+REG_TMP_7+" um 1 bit");

    // checken ob SHL overflow verursachte oder bereits verursachte...
    buffer.writeLine(ADD_4ia, REG_IP, REG_CF, "Checken ob cf bereits gesetzt");
    buffer.writeLine(SET_4ia, REG_IP, SHLLabel[1], "  cf noch nicht gesetzt: neu checken");
    buffer.writeLine(SET_4ia, REG_IP, SHLLabel[2], "  cf bereits gesetzt: weiterfahren ohne check");
    LABEL(SHLLabel[1]);
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Checken ob shift left overflow verursachte");
    buffer.writeLine(SET_4ia, REG_IP, SHLLabel[2], "  kein overflow erfolgt -> weiterfahren");
    buffer.writeLine(SET_4ia, REG_CF, 1, "SHL verursachte overflow. cf setzen");

    LABEL(SHLLabel[2]);
    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Unterlauf flag auf 0 setzen");
    buffer.writeLine(ADD_4ia, REG_TMP_2, REG_TMP_1, "Schlaufenzahler dekrementieren");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Checken ob Schlaufe beendet");
    buffer.writeLine(SET_4ia, REG_IP, SHLLabel[3], "  SHL verlassen");
    buffer.writeLine(SET_4ia, REG_IP, SHLLabel[0], "  SHL wiederholen");
    LABEL(SHLLabel[3]);
    moveTempToArg(arg1, REG_TMP_7);
  }

  /**
   * Shifts right one bit <code>arg2</code> times the value from
   * argument <code>arg1</code>. By an underflow, the involved bits
   * will be lost.<br>
   * First, this method resolves the arguments <code>arg1</code> and
   * <code>arg2</code> so the resolved registers will be no pointer or
   * constant but rather absolut registers.
   * Then, the following algorithm will be processed:<br>
   * <pre>
   *   for(int i=1; i<=arg2;++i) {
   *     arg1 = arg1 / 2;
   *   }
   * </pre>
   * <code>arg1</code> contains the result of the shift right operation.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_1</code>&nbsp;&nbsp; decrementation constant
   *   <li><code>REG_TMP_5</code>&nbsp;&nbsp; inner loop counter ->
   *       number of shifts to the right
   *   <li><code>REG_TMP_9</code>&nbsp;&nbsp;value of argument 1
   *   <li><code>REG_TMP_10</code>&nbsp;&nbsp;value of argument 2
   * </ul>
   * <!-- *************************************************************** -->
   *      Be careful by changing temporary registers cuz SHR uses DIV macro!
   * <!-- *************************************************************** -->
   * @param arg1 value who will be shifted
   * @param arg2 number of shifts to the right by one bit
   */
  private void SHR(Argument arg1, Argument arg2) {
    String SHRLabel[] = m_labelHandler.getTemporaryLabel(4);
    moveArgToTemp(arg1, REG_TMP_9);
    moveArgToTemp(arg2, REG_TMP_10);

    buffer.writeLine(SET_4ia, REG_TMP_1, getRegMinus1(), "Subtraktionskonstante -1");
    buffer.writeLine(MOV_4ia, REG_TMP_5, REG_TMP_10, "Anzahl shifts ("+arg2.getValue()+") initialisieren");
    buffer.writeLine(SET_4ia, REG_IP, SHRLabel[1], "erstes SHR ueberspringen -> falls 0 shifts gewuenscht");
    LABEL(SHRLabel[0]);

    SPACE("shift right "+REG_TMP_9+" um 1 bit");
    DIV(new Argument(REG_TMP_9, ARG_TYPE_REGISTER), new Argument("2", ARG_TYPE_CONSTANT));

    LABEL(SHRLabel[1]);
    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Unterlauf flag auf 0 setzen");
    buffer.writeLine(ADD_4ia, REG_TMP_5, REG_TMP_1, "Schlaufenzahler dekrementieren");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Checken ob Schlaufe beendet");
    buffer.writeLine(SET_4ia, REG_IP, SHRLabel[2], "  SHL verlassen");
    buffer.writeLine(SET_4ia, REG_IP, SHRLabel[0], "  SHL wiederholen");
    LABEL(SHRLabel[2]);
    moveTempToArg(arg1, REG_TMP_9);
    LABEL(SHRLabel[3]);
  }

  ////////////////////////////////////////// instructions with tree arguments
  /**
   * Conditional jump to label <code>label</code> if condition for kind
   * <code>KIND</code> occurs. KIND can be one of <code>JEQ</code>,
   * <code>JGE</code>, <code>JG</code> or <code>JNE</code>. This macro
   * works like the following algorithm shows:
   * <pre>
   *     while(!arg1_overflow && !arg2_overflow) {
   *       ++arg1;
   *       ++arg2;
   *     }
   *
   *     if((arg1_overflow && !arg2_overflow && KIND == JG)  ||
   *        (!arg1_overflow && arg2_overflow && KIND == JNE) ||
   *        (arg1_overflow && arg2_overflow &&
   *          (KIND == JGE || KIND == JEQ))){
   *       return true;
   *     } else {
   *       return false;
   *     }
   * </pre>
   * Note: return true means the instruction pointer will jump to the label,
   *       false means the program will continue below this macro code.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_2</code>&nbsp;&nbsp; incrementation constant
   *   <li><code>REG_TMP_7</code>&nbsp;&nbsp;value of argument 1
   *   <li><code>REG_TMP_8</code>&nbsp;&nbsp;value of argument 2
   * </ul>
   * @param arg1 operand 1
   * @param arg2 operand 2
   * @param label the label where the IP jumps in case condition occurs.
   * @param KIND The kind of comparison
   * @see org.parasit.xia.global.Constants#JEQ
   * @see org.parasit.xia.global.Constants#JGE
   * @see org.parasit.xia.global.Constants#JG
   * @see org.parasit.xia.global.Constants#JNE
   */
  private void JMP(Argument arg1, Argument arg2, String label, int KIND) {
    moveArgToTemp(arg1, REG_TMP_7);
    moveArgToTemp(arg2, REG_TMP_8);
    String JGlabel[] = m_labelHandler.getTemporaryLabel(5);

    // Initialisierungen
    buffer.writeLine(SET_4ia, REG_TMP_2, 1, "Register "+REG_TMP_2+" mit Wert 1 zum inkrementieren");

    // T4 inkrementieren
    LABEL(JGlabel[0]);
    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Flag zuruecksetzen auf 0");
    buffer.writeLine(ADD_4ia, REG_TMP_7, REG_TMP_2, "Register "+REG_TMP_7+" inkrementieren");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Jump zu Verzweigung ob "+REG_TMP_7+" Ueberlauf oder nicht");
    buffer.writeLine(SET_4ia, REG_IP, JGlabel[4], "  Kein Ueberlauf: weiterfahren mit "+REG_TMP_8+" inkrementieren");
    buffer.writeLine(SET_4ia, REG_IP, JGlabel[1], "  Ueberlauf: checken ob "+REG_TMP_8+" auch Ueberlauf...");

    // inkrement T5 falls T4=0
    LABEL(JGlabel[4]);
    buffer.writeLine(ADD_4ia, REG_TMP_8, REG_TMP_2, "Register "+REG_TMP_8+" inkrementieren");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Jump zu Verzweigung ob "+REG_TMP_8+" Ueberlauf oder nicht");
    buffer.writeLine(SET_4ia, REG_IP, JGlabel[0], "  "+REG_TMP_8+" auch kein Ueberlauf: naechster Durchlauf");
    if(KIND == JNE) {
      buffer.writeLine(SET_4ia, REG_IP, JGlabel[3], "  Abbruch: Wert "+arg2.getValue()+" grosser als "+arg1.getValue());
    } else {
      buffer.writeLine(SET_4ia, REG_IP, JGlabel[2], "  Abbruch: Wert in Register "+arg2.getValue()+" grosser als in "+arg1.getValue());
    }


    // increment T5 in case T4==1
    LABEL(JGlabel[1]);
    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Flag zuruecksetzen auf 0");
    buffer.writeLine(ADD_4ia, REG_TMP_8, REG_TMP_2, "Register "+REG_TMP_8+" inkrementieren");
    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Jump zu Verzweigung ob "+REG_TMP_8+" Ueberlauf oder nicht");
    if(KIND == JEQ) {
      buffer.writeLine(SET_4ia, REG_IP, JGlabel[2], "  Abbruch: Wert in "+arg1.getValue()+" ist groesser als der in "+arg2.getValue());
      buffer.writeLine(SET_4ia, REG_IP, JGlabel[3], "  Abbruch: Wert in "+arg2.getValue()+" grosser als in "+arg1.getValue());
    } else if(KIND == JGE) {
      buffer.writeLine(SET_4ia, REG_IP, JGlabel[3], "  Abbruch: Wert in "+arg1.getValue()+" ist groesser als der in "+arg2.getValue());
      buffer.writeLine(SET_4ia, REG_IP, JGlabel[3], "  Abbruch: Wert in "+arg2.getValue()+" grosser als in "+arg1.getValue());
    }else if(KIND == JG || KIND == JNE) {
      buffer.writeLine(SET_4ia, REG_IP, JGlabel[3], "  Abbruch: Wert in "+arg1.getValue()+" ist groesser als der in "+arg2.getValue());
      buffer.writeLine(SET_4ia, REG_IP, JGlabel[2], "  Abbruch: Wert in "+arg2.getValue()+" grosser als in "+arg1.getValue());
    }
    // Abbruch return 'true'
    LABEL(JGlabel[3]);
    buffer.writeLine(SET_4ia, REG_IP, label);

    // Abbruch return 'false'
    LABEL(JGlabel[2]);
  }

  /**
   * Jumps to the label <code></code> in case the value of the register who
   * <code>arg1</code> points to is greater than the value of the register
   * who <code>arg2</code> points to.
   * Calls the macro JMP as the following:<br>
   * <code>JMP(arg1, arg2, label, JG);</code>
   * @param arg1 operand 1
   * @param arg2 operand 2
   * @param label the label where the IP jumps in case condition occurs.
   * @see org.parasit.xia.global.Constants#JG
   * @see org.parasit.xia.compiler.CodeGenerator#JMP(Argument, Argument, String, int)
   */
  private void JG(Argument arg1, Argument arg2, String label)
  {
    this.JMP(arg1, arg2, label, JG);
  }

  /**
   * Jumps to the label <code></code> in case the value of the register who
   * <code>arg1</code> points to is greater or equals to the value of the
   * register who <code>arg2</code> points to.
   * Calls the macro JMP as the following:<br>
   * <code>JMP(arg1, arg2, label, JGE);</code>
   * @param arg1 operand 1
   * @param arg2 operand 2
   * @param label the label where the IP jumps in case condition occurs.
   * @see org.parasit.xia.global.Constants#JGE
   * @see org.parasit.xia.compiler.CodeGenerator#JMP(Argument, Argument, String, int)
   */
  private void JGE(Argument arg1, Argument arg2, String label)
  {
    this.JMP(arg1, arg2, label, JGE);
  }

  /**
   * Jumps to the label <code></code> in case the value of the register who
   * <code>arg1</code> points to is equals the value of the register who
   * <code>arg2</code> points to.
   * Calls the macro JMP as the following:<br>
   * <code>JMP(arg1, arg2, label, JEQ);</code>
   * @param arg1 operand 1
   * @param arg2 operand 2
   * @param label the label where the IP jumps in case condition occurs.
   * @see org.parasit.xia.global.Constants#JEQ
   * @see org.parasit.xia.compiler.CodeGenerator#JMP(Argument, Argument, String, int)
   */
  private void JEQ(Argument arg1, Argument arg2, String label)
  {
    this.JMP(arg1, arg2, label, JEQ);
  }

  /**
   * Jumps to the label <code></code> in case the value of the register who
   * <code>arg1</code> points to is less or equals the value of the
   * register who <code>arg2</code> points to.
   * Calls the macro JMP as the following:<br>
   * <code>JMP(arg2, arg1, label, JGE);</code>
   * @param arg1 operand 1
   * @param arg2 operand 2
   * @param label the label where the IP jumps in case condition occurs.
   * @see org.parasit.xia.global.Constants#JGE
   * @see org.parasit.xia.compiler.CodeGenerator#JMP(Argument, Argument, String, int)
   */
  private void JLE(Argument arg1, Argument arg2, String label)
  {
    this.JMP(arg2, arg1, label, JGE);
  }

  /**
   * Jumps to the label <code></code> in case the value of the register who
   * <code>arg1</code> points to is less than the value of the
   * register who <code>arg2</code> points to.
   * Calls the macro JMP as the following:<br>
   * <code>JMP(arg2, arg1, label, JG);</code>
   * @param arg1 operand 1
   * @param arg2 operand 2
   * @param label the label where the IP jumps in case condition occurs.
   * @see org.parasit.xia.global.Constants#JG
   * @see org.parasit.xia.compiler.CodeGenerator#JMP(Argument, Argument, String, int)
   */
  private void JL(Argument arg1, Argument arg2, String label)
  {
    this.JMP(arg2, arg1, label, JG);
  }

  /**
   * Jumps to the label <code></code> in case the value of the register who
   * <code>arg1</code> points to not equals the value of the register who
   * <code>arg2</code> points to.
   * Calls the macro JMP as the following:<br>
   * <code>JMP(arg1, arg2, label, JNE);</code>
   * @param arg1 operand 1
   * @param arg2 operand 2
   * @param label the label where the IP jumps in case condition occurs.
   * @see org.parasit.xia.global.Constants#JNE
   * @see org.parasit.xia.compiler.CodeGenerator#JMP(Argument, Argument, String, int)
   */
  private void JNE(Argument arg1, Argument arg2, String label)
  {
    this.JMP(arg1, arg2, label, JNE);
  }

  /**
   * Checks of which kind the argument <code>arg</code> is and copies
   * respectively sets its value to the temporary used register
   * <code>register</code>
   * @param arg the argument used Xia wide
   * @param register the temporarily used register.
   */
  private void moveArgToTemp(Argument arg, String register) {
    if(arg.getType() == ARG_TYPE_CONSTANT) {
      buffer.writeLine(SET_4ia, register, arg.getValue(), "Konstante in temp. Reg. "+register+" laden");
    }else if(arg.getType() == ARG_TYPE_REGISTER) {
      buffer.writeLine(MOV_4ia, register, arg.getValue(), "Wert von "+arg.getValue()+" in tmp. Register "+register+" kopieren");
    } else if(arg.getType() == ARG_TYPE_POINTER) {
      buffer.writeLine(MOV_4ia, register, arg.getValue(), "Wert von "+arg.getValue()+" in temp. Reg. "+register+" kopieren");
    }
  }

  /**
   * Copies the value of temporary used register <code>register</code> back
   * to the register used in regular XIA.
   * @param arg Argument with Xia wide used register
   * @param register temporary register used instead of register
   *        defined in argument
   */
  private void moveTempToArg(Argument arg, String register) {
    buffer.writeLine(MOV_4ia, arg.getValue(), register, "Wert von temp. Reg. "+register+" zurueck kopieren in "+arg.getValue());
  }


  /**
   * Routine who will be added for each division. In this macro, the IP
   * will jump to a further macro who will be added once and will check
   * if the divisor is zero or not.
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_1</code>&nbsp;&nbsp;return addess in case
   *                             divisor isn't zero
   *   <li><code>REG_TMP_2</code>&nbsp;&nbsp;value who has to be checked
   * </ul>
   * @param register The name of the register who has to be ckecked to zero
   * @param continueLine the line number where the IP jumps in case the
   *                     divisor is NOT zero.
   * @see org.parasit.xia.compiler.CodeGenerator#addDivByZeroCheckCode()
   */
  private void checkDivByZero(String register, int continueLine) {
    String[] cdbz = m_labelHandler.getTemporaryLabel(1);
    SPACE("");
    SPACE("check ob Division mit 0");
    buffer.writeLine(MOV_4ia, REG_TMP_2, register, "Register, welches auf 0 getestet werden soll");
    buffer.writeLine(SET_4ia, REG_TMP_1, cdbz[0], "Aktuelle Zeilennummer in "+REG_TMP_1+" fuer Ruecksprung von zero-check");
    JMP(CHECK_DIV_PRECONDITIONS);
    LABEL(cdbz[0]);
  }

  /**
   * Routine who will be added once for several divisions and who checks if
   * a division by zero ocurs. In case that the divisor IS zero, the error
   * code register will be set with value <code>1</code>. Otherwise, the
   * program will be continued...
   * <br><br><b>Used registers:</b>
   * <ul>
   *   <li><code>REG_TMP_1</code>&nbsp;&nbsp;return addess in case
   *                             divisor isn't zero
   *   <li><code>REG_TMP_2</code>&nbsp;&nbsp;value who has to be checked
   *   <li><code>REG_TMP_3</code>&nbsp;&nbsp;decrementation constant
   * </ul>
   * @see org.parasit.xia.global.RegisterConstants#REG_EC
   * @see org.parasit.xia.global.RegisterConstants#REG_EC
   * @see org.parasit.xia.compiler.CodeGenerator#checkDivByZero(String, int)
   * @see org.parasit.xia.global.Constants#getRegMinus1()
   */
  private void addDivByZeroCheckCode() {
    LABEL(CHECK_DIV_PRECONDITIONS);
    SPACE("");
    SPACE("Routine, die ueberprueft, ob Division ");
    SPACE("mit 0 vorliegt. Wert aus Register "+REG_TMP_2);
    SPACE("wird auf 0 geprueft. Falls dies der Fall ");
    SPACE("ist, dann Sprung zu Zeile "+DIVBYZERO_LABEL+" fuer Programmstopp ");
    SPACE("und ErrorFlag=1 setzen.");
    buffer.writeLine(SET_4ia, REG_TMP_3, getRegMinus1(), "Register "+REG_TMP_3+" zum addieren von "+getRegMinus1()+" um "+REG_TMP_2);
    buffer.writeLine(SET_4ia, REG_FLAG, 0, "Ueberlauf register "+REG_FLAG+" auf 0 setzen");
    buffer.writeLine(ADD_4ia, REG_TMP_2, REG_TMP_3, "Register "+REG_TMP_2+" addieren um "+getRegMinus1());

    buffer.writeLine(ADD_4ia, REG_IP, REG_FLAG, "Checken ob Addition Ueberlauf verursachte");
    buffer.writeLine(SET_4ia, REG_IP, DIVBYZERO_LABEL, "Register "+REG_TMP_2+" war 0: Abbruch");
    buffer.writeLine(MOV_4ia, REG_IP, REG_TMP_1, "Weiterfahren mit der Division");

    LABEL(DIVBYZERO_LABEL);
    buffer.writeLine(SET_4ia, REG_EC, 1, "Error code setzen: Division mit 0");
    HLT();
  }

  ////////////////////////////////////////////////////// private class Buffer
  /**
   * Represents a buffer where all generated 4ia code will be written in.
   * This class handles itself the forgiven line numbers 4ia wide by using
   * an instance of LineNumber and formats the generated lines well like
   * the following sheme:<br>
   * <code>[lineNumber]: [Opcode] &lt;arg1&gt;, &lt;arg2&gt; &lt;; comment&gt;</code>
   * @see org.parasit.xia.compiler.LineNumber
   */
  private class Buffer
  {
    private StringBuffer strBuf;
    private String OP1LENGTH = "     ";
    private String OP2LENGTH = "          ";

    private LineNumber m_lineNumber;


    /**
     * Creates a new instance of lineNumber
     */
    public Buffer(String headerInformation) {
      this.m_lineNumber = new LineNumber();
      strBuf = new StringBuffer(400);
      strBuf.append(headerInformation);
    }

    /**
     * Writes a well formatted line in the internal StringBuffer.
     * @param opcode the name of the Opcode
     * @param val1 the value of argument 1
     * @param val2 the value of argument 1
     * @param comment comments if existing
     */
    public void writeLine(String opcode,
                          String val1,
                          String val2,
                          String comment) {
      if(!StringHelper.isTrimmedEmpty(val1)) {
        // check if val1 is NOT longer than OP1LENGTH
        // (prevents to StringOutOfBoundsException)
        if(OP1LENGTH.length()>=(""+val1).length()) {
          val1 = ""+val1+","+(OP1LENGTH.substring
                             (0, OP1LENGTH.length()-(""+val1).length()));
        }
      } else {
        val1 = OP1LENGTH+" ";
      }

      if(!StringHelper.isTrimmedEmpty(val1)) {
        // check if val1 is NOT longer than OP1LENGTH
        // (prevents to StringOutOfBoundsException)
        if(OP2LENGTH.length()>=(""+val2).length()) {
          val2 = ""+val2+(OP2LENGTH.substring
                         (0, OP2LENGTH.length()-(""+val2).length()));
        }
      } else {
        val2 = OP2LENGTH;
      }

      if(StringHelper.isTrimmedEmpty(comment)) {
        comment = "";
      } else {
        comment = " ; "+comment;
      }
      strBuf.append(m_lineNumber.getNextLineNr()+": "+opcode+" "+val1+val2+comment+"\n");
    }

    /**
     * Delegater class to writeLine(String, String, String, String)
     * @see org.parasit.xia.compiler.CodeGenerator$Buffer#writeLine(String, String, String, String)
     */
    public void writeLine(String text) {
      strBuf.append("      "+OP1LENGTH+OP2LENGTH+"      ; "+text+"\n");
    }

    /**
     * Delegater class to writeLine(String, String, String, String)
     * @see org.parasit.xia.compiler.CodeGenerator$Buffer#writeLine(String, String, String, String)
     */
    public void writeLine(String opcode, String comment) {
      writeLine(opcode, null, null, comment);
    }
    /**
     * Delegater class to writeLine(String, String, String, String)
     * @see org.parasit.xia.compiler.CodeGenerator$Buffer#writeLine(String, String, String, String)
     */
    public void writeLine(String opcode, String val1, String val2) {
      writeLine(opcode, val1, val2, null);
    }

    /**
     * Delegater class to writeLine(String, String, String, String)
     * @see org.parasit.xia.compiler.CodeGenerator$Buffer#writeLine(String, String, String, String)
     */
    public void writeLine(String opcode, int val1, String val2) {
      writeLine(opcode, ""+val1, val2, null);
    }

    /**
     * Delegater class to writeLine(String, String, String, String)
     * @see org.parasit.xia.compiler.CodeGenerator$Buffer#writeLine(String, String, String, String)
     */
    public void writeLine(String opcode, String val1, int val2) {
      writeLine(opcode, val1, ""+val2, null);
    }

    /**
     * Delegater class to writeLine(String, String, String, String)
     * @see org.parasit.xia.compiler.CodeGenerator$Buffer#writeLine(String, String, String, String)
     */
    public void writeLine(String opcode, int val1, int val2) {
      writeLine(opcode, ""+val1, ""+val2, null);
    }

    // ---------------------------------------------------- commented linezos
    /**
     * Delegater class to writeLine(String, String, String, String)
     * @see org.parasit.xia.compiler.CodeGenerator$Buffer#writeLine(String, String, String, String)
     */
    public void writeLine(String opcode, int val1,
                                 String val2, String comment) {
      writeLine(opcode, ""+val1, ""+val2, comment);
    }

    /**
     * Delegater class to writeLine(String, String, String, String)
     * @see org.parasit.xia.compiler.CodeGenerator$Buffer#writeLine(String, String, String, String)
     */
    public void writeLine(String opcode, String val1,
                                 int val2, String comment) {
      writeLine(opcode, ""+val1, ""+val2, comment);
    }

    /**
     * Delegater class to writeLine(String, String, String, String)
     * @see org.parasit.xia.compiler.CodeGenerator$Buffer#writeLine(String, String, String, String)
     */
    public void writeLine(String opcode, int val1,
                                 int val2, String comment) {
      writeLine(opcode, ""+val1, ""+val2, comment);
    }

    /**
     * Returns a string representation of the internal StringBuffer
     * containing all the generated 4ia code linezos.
     * @return a string representation of the internal StringBuffer
     *         containing all the generated 4ia code linezos.
     */
    public String getString() {
      return strBuf.toString();
    }

    /**
     * Delegator to LineNumber
     * @return current line number
     * @see org.parasit.xia.compiler.LineNumber#getCurrentLineNr()
     */
    public int getCurrentLineNr() {
      return this.m_lineNumber.getCurrentLineNr();
    }
  }
}
