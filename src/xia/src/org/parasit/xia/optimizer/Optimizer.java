package org.parasit.xia.optimizer;

/**
 * Optimizes 4ia code.
 * <font color="#ff0000">Is not implemented in this version.</font><br>
 * The optimizing algorithms will recognized and eliminate the following
 * listed cases:
 * <ul>
 *   <li>Jumps to other jumps
 *   <li>Temporary used registers who's values will be stored in other
 *       temporary registers.
 *   <li>Several used code routines will be replaced by a similar
 *       code routine who can be used by all other code fragments.
 *   <li>and so on...
 * </ul>
 * @version $Revision: 1.4 $
 */
public class Optimizer {
  private String m_code;

  /**
   * Constructor to set the string with the 4ia code who has to be
   * optimized.
   */
  public Optimizer(String code) {
    this.m_code = code;
  }

  /**
   * Will optimize the 4ia code and return it's optimized version.
   * @return optimized string representation of the processed 4ia code.
   */
  public String optimize() {
    return this.m_code;
  }
}
