/******************************************************************************
 *                                                                            *
 * Copyright (c) 1999-2003 Wimba S.A., All Rights Reserved.                   *
 *                                                                            *
 * COPYRIGHT:                                                                 *
 *      This software is the property of Wimba.com.                           *
 *      It cannot be copied, used, or modified without obtaining an           *
 *      authorization from the authors or a mandated member of Wimba.com.     *
 *      If such an authorization is provided, any modified version            *
 *      or copy of the software has to contain this header.                   *
 *                                                                            *
 * WARRANTIES:                                                                *
 *      This software is made available by the authors in the hope            *
 *      that it will be useful, but without any warranty.                     *
 *      Wimba.com is not liable for any consequence related to the            *
 *      use of the provided software.                                         *
 *                                                                            *
 * Class: TestJSpeex.java                                                     *
 *                                                                            *
 * Author: Marc GIMPEL                                                        *
 *                                                                            *
 * Date: 6th January 2004                                                     *
 *                                                                            *
 ******************************************************************************/

/* $Id$ */

package org.xiph.speex;

import java.io.InputStream;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;;

/**
 * JUnit Tests for JSpeex
 *
 * @author Marc Gimpel, Wimba S.A. (marc@wimba.com)
 * @version $Revision$
 */
public class TestJSpeex
  extends TestCase
{
  /**
   * Constructor
   * @param arg0
   */
  public TestJSpeex(String arg0) {
    super(arg0);
  }
  
  /**
   * Command line entrance.
   * @param args
   */
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TestJSpeex.suite());
  }
  
  ///////////////////////////////////////////////////////////////////////////
  // TestCase classes to override
  ///////////////////////////////////////////////////////////////////////////

  /**
   * 
   */
  protected void setUp() {
  }
  
  /**
   * 
   */
  protected void tearDown() {
  }
  
  /**
   * 
   */
//  protected void runTest() {
//  }
  
  /**
   * Builds the Test Suite.
   * @return the Test Suite.
   */
  public static Test suite()
  {
    return new TestSuite(TestJSpeex.class);
  }
  
  ///////////////////////////////////////////////////////////////////////////
  // Tests
  ///////////////////////////////////////////////////////////////////////////
  
  /**
   * Test
   */
  public void test1()
  {
    assertTrue("It failed", true);
  }
  
  /**
   * 
   */
  private Properties readWavFile(InputStream is)
  {
    return null;
  }
}
