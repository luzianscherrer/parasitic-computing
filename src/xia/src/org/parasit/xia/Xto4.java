package org.parasit.xia;

import org.parasit.xia.scanner.*;
import org.parasit.xia.optimizer.*;

import org.parasit.xia.global.ErrorVector;
import org.parasit.xia.global.Constants;
import org.parasit.xia.resolver.VirtualRegisterResolver;
import org.parasit.xia.resolver.exception.VirtualRegisterResolverException;
import org.parasit.xia.compiler.CodeGenerator;
import java.util.Vector;
import java.util.Enumeration;
import java.io.IOException;
import java.io.FileWriter;

import org.parasit.xia.scanner.exception.*;

/**
 * Xto4 represents the main class to transform Xia source code to 4ia code.
 * This cross compiler processes the following tasks:
 * <ol>
 *   <li> The raw Xia source code will be scanned and syntax validated.
 *        A vector will built containing a well formatted tree of lines
 *        with their 4ia relevant Opcodes and arguments.
 *   <li> Virtual registers such as Flags and instruction pointers will be
 *        resolved to absolut registers with their number and statistic
 *        information will be collected.
 *   <li> The formatted Xia source code will be translated into raw 4ia
 *        source code.
 *   <li> Code optimizing algorithms clean up the code and enhances its
 *        performance.
 * </ol>
 * In case of errors during translating Xia into 4ia, error messages
 * will be prompted to indicate the user the erroneous Xia code fragments.
 * @version $Revision: 1.6 $
 */
public class Xto4 extends Constants {

  /**
   * The main method. Instantiates all required Classes to translate
   * Xia source code into 4ia source.
   * @param args[] The name of the input file and optionally the path
   *        to de output file. In case no second argument is given, the
   *        4ia source code will be prompted out.
   */
  public static void main(String args[]) {
    welcomeMessage();
    if(args.length == 0 || args.length>2) {
      System.out.println("Usage of Xto4: java -jar Xto4.jar "+
                         "<input.xia> [output.4ia]");
      System.exit(-1);
    }

    Scanner scanner = new Scanner(args[0]);
    VirtualRegisterResolver resolver = new VirtualRegisterResolver();
    LineVector vec = null;
    String code = "";
    try {
      // scanning phase
      System.out.print("Scanning file "+args[0]+"... ");
      vec = scanner.scanXia();
      System.out.println("done");

      // resolving phase
      System.out.print("Resolving virtual registers... ");
      resolver.resolve(vec);
      System.out.println("done");

      // generation 4ia code phase
      System.out.print("Generating 4ia code... ");
      code = new CodeGenerator(vec, resolver.getHeaderInfo()).generate4ia();
      System.out.println("done");

      // optimizing 4ia code phase
      System.out.print("Optimizing 4ia code... ");
      code = new Optimizer(code).optimize();
      System.out.println("not implemented");

      if(args.length == 1 || !printToFile(args[1], code)) {
        System.out.println(code);
      }
    }

    catch(IOException ex) {
      System.out.println("error");
      System.out.println(ex.getMessage());
      System.exit(-1);
    }

    catch(ScannerException ex) {
      System.out.println("error");
      System.out.println(ex.getMessage());
    }

    catch(VirtualRegisterResolverException ex) {
      System.out.println("error");
      System.out.println(ex.getMessage());
    }
    System.exit(0);
  }


  /////////////////////////////////////////////////////////// private methods
  /**
   * Writes <code>string</code> to file with name <code>fileName</code>
   * @param fileName The name of the file
   * @param string The string you want to write in the file
   * @return <code>true</code> if operation was successful,
   *         <code>false</code> otherwise
   */
  private static boolean printToFile(String fileName, String string) {
    try {
      FileWriter fl = new FileWriter(fileName);
      fl.write(string);
      fl.close();
      return true;
    } catch(IOException ex) {
      System.out.println("Error while writing generated 4ia code to file\n"+
                         fileName+". Reason: "+ex.getMessage());
      return false;
    }
  }

  /**
   * Prints out a string containing the welcome message.
   */
  private static void welcomeMessage() {
    System.out.print(
      "Parasitic Computing, Xto4 cross compiler  "+Xto4_VERSION+"\n"+
      "Copyright (c) 2002 Luzian Scherrer, Juerg Reusser\n"+
      "Check out http://www.parasit.org for further informations.\n\n");
  }
}
