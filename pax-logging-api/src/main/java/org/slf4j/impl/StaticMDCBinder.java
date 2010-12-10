package org.slf4j.impl;

import org.slf4j.spi.MDCAdapter;
import org.ops4j.pax.logging.slf4j.Slf4jMDCAdapter;

/**
 * This class is only a stub. Real implementations are found in
 * each SLF4J binding project, e.g. slf4j-nop, slf4j-log4j12 etc.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class StaticMDCBinder {


  /**
   * The unique instance of this class.
   */
  public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

  private static final String mdcAdapterClassStr = Slf4jMDCAdapter.class.getName();

  private static final MDCAdapter mdcAdapter = new Slf4jMDCAdapter();  

  private StaticMDCBinder() {
  }

  public MDCAdapter getMDCA() {
    return mdcAdapter;
  }

  public String  getMDCAdapterClassStr() {
    return mdcAdapterClassStr;
  }
}
