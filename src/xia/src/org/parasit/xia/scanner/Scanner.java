package org.parasit.xia.scanner;

import org.parasit.util.StringHelper;
import org.parasit.xia.global.ErrorVector;
import org.parasit.xia.scanner.exception.*;
import java.io.*;
import java.util.StringTokenizer;

/**
 * Represents the scanner unit in Xto4 cross compiler.
 * @version $Revision: 1.4 $
 */
public class Scanner {

  /** The name of the file to be scanned */
  private String m_fileName;
  /** The line number counter */
  private int m_lineNumber;

  /**
   * Constructor to instantiate a new Scanner Object. Doing this, the
   * filename will be set of the file who'll be scanner later on and
   * the line number counter will be resetted.
   * @param fileName The name of the file to be scanned later on
   */
  public Scanner(String fileName) {
    this.m_fileName = fileName;
    this.m_lineNumber = 0;
  }

  /**
   * Reads line by line from the source file and scans it. Each
   * successfully scanned and parsed line will be added to the return
   * vector wo'll returned if reached <code>EOF</code>.<br>
   * If the file to scan couldn't be found or another IO error occured,
   * If errors occured by parsing the Xia source code, a
   * <code>ScannerException</code> with all error information in the
   * message will be thrown.
   * @return A LineVector with all successfully parsed linezos.
   */
  public LineVector scanXia() throws IOException, ScannerException {
    BufferedReader bufRead;
    LineVector retVec = new LineVector();
    ErrorVector errors = new ErrorVector();
    String lineString;

    bufRead = new BufferedReader(new FileReader(this.m_fileName));
    Line line = null;
    while((lineString = bufRead.readLine()) != null) {
      ++m_lineNumber;
      try {
        line = this.scanLine(lineString);
      } catch(GeneralScannerException ex) {
        errors.add(ex);
      }
      if(line != null) retVec.add(line);
    }

    if(errors.size() > 0) {
      throw new ScannerException(errors);
    }
    return retVec;
  }

  /**
   * Scans the line, builds a new Line object and calls its
   * validation method.
   * @param lineString The string to be scanned
   * @returns the well validated Line object built from the
   *          <code>lineString</code>
   * @see org.parasit.xia.scanner.Line
   * @see org.parasit.xia.scanner.Line#validate()
   */
  private Line scanLine(String lineString) throws GeneralScannerException{

    // remove comments
    lineString = removeComments(lineString);
    // replace commas by spaces to simplify string tokenizing
    // commas aren't allowed anymore as separators
    // lineString = StringHelper.replace(lineString, ","," ");

    StringTokenizer strTok = new StringTokenizer(lineString);
    String Opcode;
    Argument op[];
    Line line;

    // general checks
    int tokenCount = strTok.countTokens();
    if(tokenCount == 0) return null; // empty/trashy line

    // assembling line informations...
    Opcode = strTok.nextToken();
    if(strTok.countTokens() > 0) {
      op = new Argument[strTok.countTokens()];
      for(int i=0;i<op.length; ++i) {
        op[i] = new Argument(strTok.nextToken());
      }
    } else {
      op = null;
    }

    line = new Line(m_lineNumber, Opcode, op);
    return line.validate();
  }

  /**
   * Removes characters from index of the first occurence of
   * <code>ScannerConstants.COMMENTSEPARATOR</code> till
   * <code>newLine</code> and returns this resting substring.
   * @param string The string who's comments have to be removed
   * @return a string without any comments
   * @see org.parasit.xia.scanner.ScannerConstants#COMMENTSEPARATOR
   */
  private String removeComments(String string) {
    int index = string.indexOf(ScannerConstants.COMMENTSEPARATOR);
    if(index == -1) return string;
    return string.substring(0, index);
  }
}