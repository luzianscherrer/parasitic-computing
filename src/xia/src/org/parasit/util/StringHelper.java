package org.parasit.util;

import java.util.Vector;
import java.util.Iterator;


/**
 * StringHelper is a utility class with a converter for:
 * - String[] => String
 * - String[] => String[]
 */
public class StringHelper
{
    /** Standard String separator characters */
    static private final String SEPARATORS = ",\n";

    static public String toString(String[] strArray)
    {
        return "[" + toString(strArray, ",") + "]";
    }

    static public String toStringForUser(String[] strArray)
    {
        String result = toString(strArray, ", ");
        while (result.length() > 2 && result.substring(result.length()-2).equals(", "))
        {
            result = result.substring(0, result.length()-2);
        }
        return result;

    }

    /**
     *  This method returns a \n-separated String containing
     *  each element of an iterator
     *  @param iter The iterator to toString()
     */
    static public String toString(Iterator iter)
    {
        String retval = "";
        while(iter.hasNext())
        {
            retval += iter.next().toString() + "\n";
        }
        return retval;
    }

    /**
     * Utility-method to get one String out of a stringArray, seperated by lineSeparator.
     * @author schlegelma
     */
    public static String toString(String[] strArray, String lineSeparator){
        if (strArray == null)
        {
            return "null";
        }
        StringBuffer buf = new StringBuffer(150);
        for(int i=0; i < strArray.length; i++)
        {
            if(strArray[i] != null)
            {
                buf.append(strArray[i]);
                if(!(strArray[i].endsWith(lineSeparator)) &&
                   i<(strArray.length-1))
                {
                    buf.append(lineSeparator);
                }
            }
        }
        return buf.toString();
     }

    static public boolean isStringArray(String str)
    {
        return isStringArray(str, SEPARATORS);
    }

    static public boolean isStringArray(String str, String separators)
    {
        if (isNull(str) || isNull(separators))
        {
            return false;
        }
        for (int i = 0; i < separators.length(); i++)
        {
            if (str.indexOf(separators.charAt(i)) >= 0)
            {
                return true;
            }
        }
        return false;
    }

    static public String[] toStringArray(String str)
    {
        return toStringArray(str, SEPARATORS);
    }

    static public String[] toStringArray(String str, String separators)
    {
        if (StringHelper.isNull(str))
        {
            return null;
        }

        Vector vector = new Vector();
        int start = 0;
        int end = str.length();
        if (str.startsWith("["))
        {
            start++;
        };
        if (str.endsWith("]"))
        {
            end--;
        }

        while(true)
        {
            int next = -1;
            for (int i = 0; i < separators.length() && next==-1; i++)
            {
                next = str.indexOf(separators.charAt(i),start);
            }
            if (next == -1)
            {
                vector.add(str.substring(start, end));
                break;
            }
            vector.add(str.substring(start,next));
            start = next + 1;
        }

        String[] strArray = new String[vector.size()];
        for (int i = 0; i < strArray.length; i++)
        {
            strArray[i] = (String) vector.get(i);
        }
        return strArray;
    }

    static public String[] toStringArray(String str, String separators, int lineLength)
    {
        if (StringHelper.isNull(str))
        {
            return null;
        }

        int numOfParts = 0;
        int startIndex = 0;
        int endIndex = 0;
        boolean found = false;
        // determine num of parts
        while (startIndex < str.length())
        {
            found = false;
            if (startIndex + lineLength >= str.length())
            {
                numOfParts++;
                break;
            }
            endIndex = startIndex + lineLength;
            if (endIndex > str.length())
            {
                endIndex = str.length();
            }
            String sub = str.substring(startIndex, endIndex);
            int last = 0;
            for (int i = 0; i < separators.length(); i++)
            {
                int lastPart = sub.lastIndexOf(separators.charAt(i));
                if (lastPart+1 >= 0 && lastPart+1 > last)
                {
                    last = lastPart+1;
                    found = true;
                }
            }
            if (last == 0)
            {
                last = endIndex-startIndex;
            }
            if (last > lineLength)
            {
                last = lineLength;
            }
            numOfParts++;
            startIndex += last;
        }
        String[] strArray = new String[numOfParts];
        numOfParts = 0;
        startIndex = 0;
        // determine parts
        while (startIndex < str.length())
        {
            found = false;
            if (startIndex + lineLength >= str.length())
            {
                strArray[numOfParts] = str.substring(startIndex);
                numOfParts++;
                break;
            }
            endIndex = startIndex + lineLength;
            if (endIndex > str.length())
            {
                endIndex = str.length();
            }
            String sub = str.substring(startIndex, endIndex);
            int last = 0;
            for (int i = 0; i < separators.length(); i++)
            {
                int lastPart = sub.lastIndexOf(separators.charAt(i));
                if (lastPart+1 >= 0 && lastPart+1 > last)
                {
                    last = lastPart+1;
                    found = true;
                }
            }
            if (last == 0)
            {
                last = endIndex-startIndex;
            }
            if (last > lineLength)
            {
                last = lineLength;
            }
            strArray[numOfParts] = str.substring(startIndex, last+startIndex);
            numOfParts++;
            startIndex += last;
        }
        return strArray;
    }

    static public boolean isNull(String string)
    {
        return string == null || string.length() == 0;
    }

    static public boolean isNull(String[] strArray)
    {
        if (strArray == null || strArray.length == 0)
        {
            return true;
        }

        boolean retVal = true;
        for (int i = 0; i < strArray.length; i++)
        {
            retVal = retVal && isNull(strArray[i]);
        }
        return retVal;
    }

    public static boolean isTrimmedEmpty(Object string)
    {

        return string == null ||
        (string instanceof String && ((String)string).trim().length() == 0);
    }

    static public String[] trimStringArray(String[] inArray)
    {
        String[] outArray;

        if (inArray == null)
        {
            return null;
        }

        Vector vec = new Vector(inArray.length);

        int length = inArray.length;
        // cut to the real used length
        for(int i=0;i<inArray.length;i++)
        {
          if(!StringHelper.isTrimmedEmpty(inArray[i]))
          {
            vec.add(inArray[i]);
          }
        } // end of for-loop
        vec.trimToSize();

        if (vec.size() == 0)
        {
            outArray = null;
        }
        else
        {
            outArray = (String[])vec.toArray(new String[0]);
        }

        return outArray;
    }

    /**
     * Increase the size of a String array if it is to small
     */
    static public String[] setMinimalSizeStringArray(String[] inArray, int size)
    {
        if (inArray == null || inArray.length < size)
        {
            String[] copy = new String[size];
            for (int i = 0; inArray != null && i < inArray.length; i++)
            {
                copy[i] = inArray[i];
            }
            inArray = copy;
        }

        return inArray;
    }

    /**
     * All elements in a String array are checked if not null. If null, an
     * empty String is set
     */
    static public void noNullStringArray(String[] inArray)
    {
        for (int i = 0; inArray != null && i < inArray.length; i++)
        {
            if (inArray[i] == null)
            {
                inArray[i] = new String("");
            }
        }
    }

    /**
     * All elements in a String array are checked if not beginning with - or :.
     * If they do, the - or : sign and following blanks are removed.
     */
    static public void noStartingSign(String[] inArray)
    {
        for (int i = 0; inArray != null && i < inArray.length; i++)
        {
            inArray[i] = noStartingSign(inArray[i]);
        }
    }

    /**
     * A String is checked if not beginning with - or :.
     * If it does, the - or : sign and following blanks are removed.
     */
    static public String noStartingSign(String line)
    {
        while (line != null && line.length() > 0 && (line.charAt(0) == '-' || line.charAt(0) == ':'))
        {
            line = line.substring(1);
            while (line.length() > 0 && line.charAt(0) == ' ')
            {
                line = line.substring(1);
            }
        }
        return line;
    }

    static public boolean isEqual(String string1, String string2)
    {
        if(string1 != null)
        {
            return string1.equals(string2);
        }
        else
        {
            return string1 == string2;
        }
    }

    static public boolean isEqual(String[] array1, String[] array2)
    {
        boolean retVal = true;

        if (array1 == null || array2 == null)
        {
            retVal = (array1 == array2);
        }
        else if(array1.length != array2.length)
        {
            retVal = false;
        }
        else
        {
            for (int i=0; retVal && i<array1.length; i++)
            {
                if(!isEqual(array1[i], array2[i]))
                {
                   retVal = false;
                }
            }
        }
        return retVal;
    }

    /**
     * Removes all elements of a certain character from a String
     */
    static public String remove(String in, char c)
    {
        String toRemove = new String();
        toRemove += c;
        return remove(in, toRemove);
    }

    /**
     * Removes all elements of a certain substring from a String
     */
    static public String remove(String in, String toRemove)
    {
        if (isNull(toRemove) || isNull(in))
        {
            return in;
        }
        StringBuffer out = new StringBuffer("");
        int lengthOfRemove = toRemove.length();
        int index;
        int fromIndex = 0;
        while ((index = in.indexOf(toRemove, fromIndex)) >= 0)
        {
            out.append(in.substring(fromIndex, index));
            fromIndex = index + lengthOfRemove;
        }
        out.append(in.substring(fromIndex));
        return out.toString();
    }

    /**
     * removes all the spaces in a string
     */
    static public String removeSpaces(String inString)
    {
        int len = inString.length();
        StringBuffer sb = new StringBuffer (len-1);
        for (int i = 0; i < len; i++)
        {
            char c;
            if ((c = inString.charAt(i)) != ' ')
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * checks if a String contains only digits
     */
    static public boolean isNumeric(String in)
    {
        if (isNull(in))
        {
            return false;
        }
        for (int i = 0; i < in.length(); i++)
        {
            if (!Character.isDigit(in.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * removes all non-digit characters from a String
     */
    static public String toNumeric(String in)
    {
        if (isNull(in))
        {
            return null;
        }
        StringBuffer result = new StringBuffer(in);
        for (int i = 0; i < result.length(); i++)
        {
            if (!Character.isDigit(result.charAt(i)))
            {
                result.deleteCharAt(i);
                i--;
            }
        }
        return result.toString();
    }

    /**
     * For representation of an integer list in toString
     */
    static public String toString(int[] intList)
    {
        if (intList == null)
        {
            return "null";
        }
        StringBuffer result = new StringBuffer("[");
        for (int i = 0; i < intList.length; i++)
        {
            result.append(intList[i]);
            if (i < intList.length-1)
            {
                result.append(",");
            }
        }
        result.append("]");
        return result.toString();
    }

    /**
     * test if mitteilung contains any text in any line
     */
    static public boolean contains(String[] containingText, String containedText)
    {
        if (StringHelper.isNull(containingText) || StringHelper.isNull(containedText))
        {
            return false;
        }
        for (int i = 0; i < containingText.length; i++)
        {
            if (!StringHelper.isNull(containingText[i]) &&
                containingText[i].indexOf(containedText) >= 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * replaces all occurences of toReplaceString with replaceString in originalString.
     * @return the new String
     */
    static public String replace(String originalString,
                                 String toReplaceString,
                                 String replaceString)
    {
        int startIndex = 0;
        int index = 0;

        StringBuffer work = new StringBuffer(originalString);
        index = originalString.indexOf(toReplaceString, startIndex);
        while (index != -1)
        {
            work.replace(index, index + toReplaceString.length(), replaceString);
            originalString = work.toString();

            startIndex = index + replaceString.length();
            work = new StringBuffer(originalString);
            index = originalString.indexOf(toReplaceString, startIndex);
        }
        return originalString;
    }

    /** Utility routine to perform an efficient in-place sort of the
    * specified String array. **/
    public static void sortStringArray(String[] strings)
    {
        // Perform a heapsort (Numerical Recipes in C, 1988).
        String testString;
        int n = strings.length;
        if (n <= 1) return; // Nothing to sort
        // Index k will be decremented from initial value to 0 during
        // "hiring" (heap creation) phase. Once it reaches 0, index
        // ir is decremented from its initial value to 0 during
        // "retirement and promotion" phase.
        for (int i, j, k = (n>>1), ir = (n-1);; )
        {
            if (k > 0)
            {      // Still in hiring phase
                testString = strings[--k];
            }
            else
            {      // In retirement and promotion phase
                testString = strings[ir];  // Save end of array
                strings[ir] = strings[0];  // Retire top of heap to end of array
                if (--ir <= 0)
                {    // Done with last promotion
                    strings[0] = testString; // Restore end of array
                    break;      // All done. Exit.
                }
            }
            // Sift down testString to its proper level
            for (i = k, j = (k<<1)+1; j <= ir; )
            {
                if ((j < ir) && (strings[j].toUpperCase().
                                 compareTo(strings[j+1].toUpperCase()) < 0))
                {
                    j++;
                }
                if ((strings[j].toUpperCase().compareTo(testString.toUpperCase()) < 0))
                {
                    break;
                }
                strings[i] = strings[j];
                i = j;
                j += j+1;
            } strings[i] = testString;
        }
    }

    /**
     * Works like toString, but performs comma separated encoding"
     *
     * @param arr String array
     * @return comma separaterd string
     */

    public static String toStringCss(String[] arr){
        return "["+encodeCss(arr)+"]";
    }

    /**
     * Works like toString, but performs comma separated encoding"
     *
     * @param arr String array
     * @return comma separaterd string
     */
    public static String encodeCss(String[] arr){
        StringBuffer sb= new StringBuffer();
        for (int i=0;i<arr.length;i++)
        {
            sb.append((i>0)?",":"");
            String next = (arr[i]!=null)?arr[i]:"";
            if ((arr[i].indexOf(',')==-1) && (arr[i].indexOf('\'')==-1)){
                //add stirng
                sb.append(arr[i]);
            }
            else
            {
                //encode string and add
                sb.append("'").append(replace(arr[i],"'","''")).append("'");
            }

        }
        return sb.toString();
    }

    /**
     * Decode string array , previously encoded with toStringCss
     *
     * @param arg CSS excoded string
     * @return array of strings
     * @throws IllegalArgumentException - argument is not a CSS string
     */
    public static String[] toStringArrayCss(String arg) throws IllegalArgumentException
    {
        if (isNull(arg))
        {
            return null;
        }

        if (arg.startsWith("[") && arg.endsWith("]"))
        {
            arg = arg.substring(1,arg.length()-1);
        }

        return decodeCss(arg);

    }

    public static String[] decodeCss(String arg) throws IllegalArgumentException
    {
        if (arg==null)
        {
            throw new IllegalArgumentException("Argument must be CSS encoded array");
        }

        Vector res =new Vector();
        boolean oddComma = true;
        int endIndex=0;
        int startIndex=0;
        do
        {
            if ((endIndex==arg.length())||(oddComma && (arg.charAt(endIndex)==',')))
            {
                String str = arg.substring(startIndex,endIndex);
                if (str.indexOf("'")>=0)
                {
                    if ((str.charAt(0)!='\'')||(str.charAt(str.length()-1)!='\'')||(str.length()<=2))
                    {
                        throw new IllegalArgumentException("Argument must be CSS encoded array.Arument: "+arg+" error in position"+endIndex);
                    }
                    str = replace(str.substring(1,str.length()-1),"''","'");
                }
                res.add(str);
                startIndex=endIndex+1;
            }
            else if (arg.charAt(endIndex)=='\'')
            {
                oddComma=!oddComma;
            }
        }
        while((++endIndex)<=arg.length());

        if (!oddComma){
            throw new IllegalArgumentException("Argument must be CSS encoded array.Arument: "+arg+" error in position"+endIndex);
        }

        return (String[])res.toArray(new String[]{});
    }


    public static boolean isStringArrayCss(String arg)
    {
        boolean isOk = true;

        if (isNull(arg))
        {
            isOk = false;
        }
        else
        {
            try
            {
                toStringArrayCss(arg);
            }
            catch(IllegalArgumentException ex)
            {
                isOk = false;
            }
        }

        return isOk;
    }

    public static String repeat(String string, int times)
    {
      String retVal = string;
      if(string != null && times > 1)
      {
        for(int i=0; i<(times-1);++i)
        {
          retVal += string;
        }
      }
      return retVal;
    }



}  // end of class StringHelper
