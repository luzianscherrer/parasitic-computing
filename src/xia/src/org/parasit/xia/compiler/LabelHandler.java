package org.parasit.xia.compiler;

import org.parasit.xia.compiler.exception.LabelAlreadyRegisteredException;
import org.parasit.util.StringHelper;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Handles all symbolic labels used in Xia source code and resolves them
 * into absolut line numbers.
 * @version $Revision: 1.4 $
 */
public class LabelHandler
{
  /** Contains all used added labels and is used to replacce
    * them by absolut line numbers by resolving the Xia labels
    */
  private Hashtable m_labelTable;

  /**
   * Each time a new variable is added to the variable table,
   * the variable counter will be incremented to know actual
   * count of used variables.
   */
  private int m_labelCounter;
  /** Garantees that each generated label is unique */
  private int m_generatorIndex;


  /**
   * This constructor reates an instance of LabelHandler and resets it.
   */
  public LabelHandler() {
    reset();
  }

  /**
   * Resets the LabelHandelr so the instance can be reused arbitrary timez.
   */
  public void reset() {
    m_labelTable = new Hashtable();
    m_labelCounter = 0;
    m_generatorIndex = 0;
  }
  /**
   *
   * @return <code>null</code> if variable with name
   * <code>variable</code> is not registered, else the Label.
   */
  public XiaLabel getLabel(String labelName) {
    return (XiaLabel)m_labelTable.get(labelName);
  }

  /**
   * Adds a label to the LabelHandler's label index. Will be used
   * to resolve the label names into line numbers 4ia wide lateron.
   * @param label The label that has to be added.
   */
  public void addLabel(XiaLabel label) {
    if(m_labelTable.get(label.getName()) != null) {
      throw new LabelAlreadyRegisteredException(label.getName());
    }
    ++m_labelCounter;
    m_labelTable.put(label.getName(), label);
  }

  /**
   * Checks if the label with name <code>labelName</code> already exists.
   * @param labelName the name of the label that has to be tested.
   * @return <code>true</code> if the label already exists,
   *         <code>false</code> otherwise.
   */
  public boolean exists(String labelName) {
    return m_labelTable.get(labelName) != null;
  }

  /**
   * Generates a temporary label by using the System time.
   * @return a temporary label with its prefix.
   */
  public String getTemporaryLabel() {
    String tmp = "L"+System.currentTimeMillis()+(++m_generatorIndex);
    return tmp.substring(tmp.length()-8, tmp.length());
  }

  /**
   * Used by the CodeGenerator to easier generate internal used labelz.
   * @param count The number of label that have to be generated.
   * @return A String array with unique label names.
   * @param count The number of labels that have to be generated and
   *        returned.
   */
  public String[] getTemporaryLabel(int count) {
    String str[] = new String[count];
    for(int i=0;i<count;++i) str[i] = this.getTemporaryLabel();
    return str;
  }

  /**
   * For each setted label, this method searches the 4ia raw source code
   * string for it and replaces all labels by their setted line number.
   * @param input the raw 4ia source code with unresolved labels
   * @return 4ia source code with all labels resolved to their corresponding
   *         line numbers.
   * @todo Don't replace labels in commented text
   */
  public String resolveLabels(String input) {
    String label;
    String lineNr;
    for(Enumeration enum = this.m_labelTable.keys();enum.hasMoreElements();) {
      label = (String)enum.nextElement();

      lineNr = ""+((XiaLabel)this.m_labelTable.get(label)).getLineNumber();
      lineNr = lineNr+StringHelper.repeat(" ", label.length()-lineNr.length());
      input = StringHelper.replace(input, label, lineNr);
    }
    return input;
  }
}
